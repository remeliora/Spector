package com.example.spector.database.mongodb;

import com.example.spector.domain.Device;
import com.example.spector.domain.Parameter;
import com.example.spector.domain.dto.devicedata.graph.*;
import com.example.spector.domain.dto.parameter.rest.ParameterByDeviceTypeDTO;
import com.example.spector.mapper.BaseDTOConverter;
import com.example.spector.repositories.DeviceParameterOverrideRepository;
import com.example.spector.repositories.DeviceRepository;
import com.example.spector.repositories.ParameterRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HistoricalDataService {
    private final MongoTemplate mongoTemplate;
    private final DeviceRepository deviceRepository;
    private final ParameterRepository parameterRepository; // Нужен для получения DTO параметра
    private final DeviceParameterOverrideRepository deviceParameterOverrideRepository;
    private final BaseDTOConverter baseDTOConverter;

    // Зона, в которой клиент (и фронтенд) ожидает видеть время
    private static final ZoneId CLIENT_ZONE = ZoneOffset.ofHours(10);

    public ChartDataResponseDTO getChartData(ChartDataRequestDTO requestDTO) {
        List<SeriesDataDTO> allSeries = new ArrayList<>();

        for (DeviceParameterRequestDTO deviceReq : requestDTO.getDevices()) {
            Long deviceId = deviceReq.getDeviceId();

            // 1. Найти устройство по ID, чтобы получить его имя
            Device device = deviceRepository.findById(deviceId)
                    .orElseThrow(() -> new EntityNotFoundException("Device not found: " + deviceId));
            String deviceName = device.getName();

            // 2. Определить, какие параметры нужно получить
            List<Long> paramIdsToFetch = deviceReq.getParameterIds();
            if (paramIdsToFetch == null || paramIdsToFetch.isEmpty()) {
                // Если список пуст, получить все активные параметры для этого устройства
                paramIdsToFetch = deviceParameterOverrideRepository.findByDeviceIdAndIsActiveTrue(deviceId)
                        .stream()
                        .map(override -> override.getParameter().getId())
                        .collect(Collectors.toList());
            }

            // 3. Прочитать исторические данные для этих параметров,
            //    преобразуем Instant (воспринимаемый как +10) в LocalDateTime в UTC для поиска в MongoDB
            LocalDateTime mongoSearchFrom = convertInstantToMongoSearchTime(requestDTO.getFrom());
            LocalDateTime mongoSearchTo = convertInstantToMongoSearchTime(requestDTO.getTo());

            List<ChartDataPoint> dataPoints = readHistoricalData(
                    deviceName, paramIdsToFetch, mongoSearchFrom, mongoSearchTo
            );

            // 4. Сгруппировать точки по параметру и создать серию для каждого
            Map<Long, List<ChartDataPoint>> groupedByParam = dataPoints.stream()
                    .collect(Collectors.groupingBy(ChartDataPoint::getParameterId));

            for (Map.Entry<Long, List<ChartDataPoint>> entry : groupedByParam.entrySet()) {
                Long paramId = entry.getKey();

                // Получить DTO параметра для имени и метрики
                Parameter param = parameterRepository.findById(paramId)
                        .orElseThrow(() -> new EntityNotFoundException("Parameter not found: " + paramId));
                ParameterByDeviceTypeDTO paramDTO = baseDTOConverter.toDTO(param, ParameterByDeviceTypeDTO.class);

                SeriesDataDTO series = new SeriesDataDTO();
                series.setDeviceId(deviceId);
                series.setDeviceName(deviceName);
                series.setParameterId(paramId);
                series.setParameterName(paramDTO.getName());

                // Преобразовать точки в формат Highcharts [[timestamp +10, value], ...]
                // (timestamp в ChartDataPoint уже в +10)
                List<List<Object>> highchartsData = entry.getValue().stream()
                        .map(point -> List.of(point.getTimestamp(), point.getValue()))
                        .collect(Collectors.toList());
                series.setData(highchartsData);

                allSeries.add(series);
            }
        }

        ChartDataResponseDTO response = new ChartDataResponseDTO();
        response.setSeries(allSeries);
        return response;
    }

    /**
     * Преобразует Instant (воспринимаемый как время в CLIENT_ZONE) в LocalDateTime в UTC для поиска в MongoDB.
     * Пример: Instant(10:00 +10) -> LocalDateTime(00:00 UTC)
     */
    private LocalDateTime convertInstantToMongoSearchTime(Instant instant) {
        if (instant == null) return null;
        // Представляем Instant как время в CLIENT_ZONE
        LocalDateTime clientLocalDateTime = instant.atZone(CLIENT_ZONE).toLocalDateTime();
        // Вычисляем смещение для этого времени в CLIENT_ZONE
        ZoneOffset clientOffset = CLIENT_ZONE.getRules().getOffset(clientLocalDateTime);
        // Преобразуем в LocalDateTime в UTC: вычитаем смещение
        return clientLocalDateTime.minusSeconds(clientOffset.getTotalSeconds());
    }

    /**
     * Читает исторические данные для заданных параметров устройства за временной промежуток.
     *
     * @param deviceName   Имя устройства (нужно для определения коллекции).
     * @param parameterIds Список ID параметров для поиска.
     * @param mongoFrom    LocalDateTime (в UTC) начала периода для поиска в MongoDB.
     * @param mongoTo      LocalDateTime (в UTC) конца периода для поиска в MongoDB.
     * @return Список точек данных в формате [timestamp +10, value, parameterId].
     */
    private List<ChartDataPoint> readHistoricalData(String deviceName, List<Long> parameterIds, LocalDateTime mongoFrom, LocalDateTime mongoTo) {
        Query query = new Query();
        query.addCriteria(Criteria.where("lastPollingTime").gte(mongoFrom).lte(mongoTo));

        // Найти коллекцию по имени устройства
        String collectionName = deviceName;

        // Выполнить запрос
        List<Document> documents = mongoTemplate.find(query, Document.class, collectionName);

        // Обработать результаты
        List<ChartDataPoint> points = new ArrayList<>();
        for (Document doc : documents) {
            // --- ИСПРАВЛЕНО: получаем Date и преобразуем в LocalDateTime в UTC ---
            java.util.Date date = doc.getDate("lastPollingTime"); // Получаем Date
            if (date == null) continue; // Пропускаем, если lastPollingTime нет

            // Преобразуем Date в Instant, затем в LocalDateTime в UTC
            LocalDateTime mongoLocalDateTime = date.toInstant().atZone(ZoneOffset.UTC).toLocalDateTime();
            // --- КОНЕЦ ИСПРАВЛЕНИЯ ---

            // Преобразуем LocalDateTime из UTC в CLIENT_ZONE и обратно в Instant
            Instant displayInstant = mongoLocalDateTime.atZone(ZoneOffset.UTC).withZoneSameInstant(CLIENT_ZONE).toInstant();

            // Конвертируем в epoch milliseconds для Highcharts (это будет время в +10)
            Long displayTimestamp = displayInstant.toEpochMilli();

            List<Map<String, Object>> params = (List<Map<String, Object>>) doc.get("parameters");
            if (params == null) continue;

            for (Map<String, Object> param : params) {
                // Извлекаем _id как Number, затем приводим к long, затем к Long
                Number idNumber = (Number) param.get("_id");
                if (idNumber == null) continue;
                Long paramId = idNumber.longValue();

                if (parameterIds.contains(paramId)) {
                    Object value = param.get("value");
                    // Создаём точку с timestamp в CLIENT_ZONE (+10)
                    points.add(new ChartDataPoint(displayTimestamp, value, paramId));
                }
            }
        }

        // Сортировать точки по времени (на всякий случай)
        points.sort((a, b) -> Long.compare(a.getTimestamp(), b.getTimestamp()));

        return points;
    }
}
