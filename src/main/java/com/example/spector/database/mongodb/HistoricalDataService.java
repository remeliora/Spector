package com.example.spector.database.mongodb;

import com.example.spector.domain.device.Device;
import com.example.spector.domain.parameter.Parameter;
import com.example.spector.domain.devicedata.dto.graph.*;
import com.example.spector.domain.parameter.dto.ParameterByDeviceTypeDTO;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HistoricalDataService {
    private final MongoTemplate mongoTemplate;
    private final DeviceRepository deviceRepository;
    private final ParameterRepository parameterRepository;
    private final DeviceParameterOverrideRepository deviceParameterOverrideRepository;
    private final BaseDTOConverter baseDTOConverter;

    public ChartDataResponseDTO getChartData(ChartDataRequestDTO requestDTO) {
        List<SeriesDataDTO> allSeries = new ArrayList<>();

        for (DeviceParameterRequestDTO deviceReq : requestDTO.getDevices()) {
            Long deviceId = deviceReq.getDeviceId();

            Device device = deviceRepository.findById(deviceId)
                    .orElseThrow(() -> new EntityNotFoundException("Device not found: " + deviceId));
            String deviceName = device.getName();

            List<Long> paramIdsToFetch = deviceReq.getParameterIds();
            if (paramIdsToFetch == null || paramIdsToFetch.isEmpty()) {
                paramIdsToFetch = deviceParameterOverrideRepository.findByDeviceIdAndIsActiveTrue(deviceId)
                        .stream()
                        .map(override -> override.getParameter().getId())
                        .collect(Collectors.toList());
            }

            // Используем Instant как есть — уже в UTC
            Instant fromUTC = requestDTO.getFrom();
            Instant toUTC = requestDTO.getTo();

            List<ChartDataPoint> dataPoints = readHistoricalData(
                    deviceName, paramIdsToFetch, fromUTC, toUTC
            );

            Map<Long, List<ChartDataPoint>> groupedByParam = dataPoints.stream()
                    .collect(Collectors.groupingBy(ChartDataPoint::getParameterId));

            for (Map.Entry<Long, List<ChartDataPoint>> entry : groupedByParam.entrySet()) {
                Long paramId = entry.getKey();

                Parameter param = parameterRepository.findById(paramId)
                        .orElseThrow(() -> new EntityNotFoundException("Parameter not found: " + paramId));
                ParameterByDeviceTypeDTO paramDTO = baseDTOConverter.toDTO(param, ParameterByDeviceTypeDTO.class);

                SeriesDataDTO series = new SeriesDataDTO();
                series.setDeviceId(deviceId);
                series.setDeviceName(deviceName);
                series.setParameterId(paramId);
                series.setParameterName(paramDTO.getName());

                List<List<Object>> highchartsData = entry.getValue().stream()
                        .map(point -> List.of(point.getTimestamp(), point.getValue())) // timestamp в UTC
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
     * Читает исторические данные для заданных параметров устройства за временной промежуток.
     *
     * @param deviceName   Имя устройства.
     * @param parameterIds Список ID параметров для поиска.
     * @param fromUTC      Instant начала периода (в UTC) для поиска в MongoDB.
     * @param toUTC        Instant конца периода (в UTC) для поиска в MongoDB.
     * @return Список точек данных в формате [timestamp UTC, value, parameterId].
     */
    private List<ChartDataPoint> readHistoricalData(String deviceName, List<Long> parameterIds, Instant fromUTC, Instant toUTC) {
        Query query = new Query();

        Criteria criteria = new Criteria();
        if (fromUTC != null || toUTC != null) {
            criteria = Criteria.where("lastPollingTime");
            if (fromUTC != null) {
                criteria = criteria.gte(Date.from(fromUTC));
            }
            if (toUTC != null) {
                criteria = criteria.lte(Date.from(toUTC));
            }
        }

        if (criteria.getCriteriaObject().size() > 0) {
            query.addCriteria(criteria);
        }

        String collectionName = deviceName;
        List<Document> documents = mongoTemplate.find(query, Document.class, collectionName);

        List<ChartDataPoint> points = new ArrayList<>();
        for (Document doc : documents) {
            Date date = doc.getDate("lastPollingTime");
            if (date == null) continue;

            // Получаем timestamp в UTC (в миллисекундах)
            Long timestamp = date.getTime();

            List<Map<String, Object>> params = (List<Map<String, Object>>) doc.get("parameters");
            if (params == null) continue;

            for (Map<String, Object> param : params) {
                Number idNumber = (Number) param.get("_id");
                if (idNumber == null) continue;
                Long paramId = idNumber.longValue();

                if (parameterIds.contains(paramId)) {
                    Object value = param.get("value");
                    // Создаём точку с timestamp в UTC
                    points.add(new ChartDataPoint(timestamp, value, paramId));
                }
            }
        }

        points.sort((a, b) -> Long.compare(a.getTimestamp(), b.getTimestamp()));
        return points;
    }
}
