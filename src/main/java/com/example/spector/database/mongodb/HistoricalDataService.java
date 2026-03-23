package com.example.spector.database.mongodb;

import com.example.spector.domain.device.Device;
import com.example.spector.domain.devicedata.dto.graph.*;
import com.example.spector.domain.parameter.Parameter;
import com.example.spector.domain.parameter.dto.ParameterByDeviceTypeDTO;
import com.example.spector.mapper.BaseDTOConverter;
import com.example.spector.repositories.DeviceParameterOverrideRepository;
import com.example.spector.repositories.DeviceRepository;
import com.example.spector.repositories.ParameterRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
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

            Instant fromUTC = requestDTO.getFrom();
            Instant toUTC = requestDTO.getTo();

            // Вызываем умный метод чтения с авто-агрегацией
            List<ChartDataPoint> dataPoints = readHistoricalDataWithAggregation(
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
     * Умный метод чтения: решает, использовать ли агрегацию, исходя из диапазона дат.
     * Порог: 48 часов (2 суток).
     */
    private List<ChartDataPoint> readHistoricalDataWithAggregation(String deviceName, List<Long> parameterIds, Instant fromUTC, Instant toUTC) {
        if (fromUTC == null || toUTC == null) {
            return readRawData(deviceName, parameterIds, fromUTC, toUTC);
        }

        long durationHours = Duration.between(fromUTC, toUTC).toHours();

        // Если период больше 48 часов, включаем агрегацию по часу
        if (durationHours > 48) {
            return readAggregatedData(deviceName, parameterIds, fromUTC, toUTC);
        } else {
            // Иначе читаем сырые данные для максимальной точности
            return readRawData(deviceName, parameterIds, fromUTC, toUTC);
        }
    }

    /**
     * Чтение сырых данных (для коротких периодов).
     * Оригинальная логика с небольшими улучшениями безопасности типов.
     */
    private List<ChartDataPoint> readRawData(String deviceName, List<Long> parameterIds, Instant fromUTC, Instant toUTC) {
        Query query = new Query();
        Criteria criteria = Criteria.where("lastPollingTime");

        if (fromUTC != null) {
            criteria = criteria.gte(Date.from(fromUTC));
        }
        if (toUTC != null) {
            criteria = criteria.lte(Date.from(toUTC));
        }

        query.addCriteria(criteria);

        // Опционально: можно добавить лимит, если вдруг запрос слишком большой,
        // но логика выше должна это предотвращать.
        // query.limit(50000);

        List<Document> documents = mongoTemplate.find(query, Document.class, deviceName);
        return parseDocuments(documents, parameterIds);
    }

    /**
     * Чтение агрегированных данных (для длинных периодов).
     * Группирует данные по 1 часу и считает среднее значение (AVG).
     * Использует MongoDB Aggregation Framework.
     */
    private List<ChartDataPoint> readAggregatedData(String deviceName, List<Long> parameterIds, Instant fromUTC, Instant toUTC) {
        // 1. MATCH: Фильтр по времени
        Criteria timeCriteria = Criteria.where("lastPollingTime");
        if (fromUTC != null) timeCriteria = timeCriteria.gte(Date.from(fromUTC));
        if (toUTC != null) timeCriteria = timeCriteria.lte(Date.from(toUTC));

        MatchOperation match = Aggregation.match(timeCriteria);

        // 2. UNWIND: Раскрываем массив parameters
        UnwindOperation unwind = Aggregation.unwind("parameters");

        // 3. MATCH: Фильтр по ID параметров
        MatchOperation filterParams = Aggregation.match(Criteria.where("parameters._id").in(parameterIds));

        // 4. GROUP: Группировка по часу и ID параметра
        // Мы формируем операцию группировки вручную через Document, чтобы использовать $dateTrunc,
        // так как стандартный builder не поддерживает сложные выражения в поле _id напрямую.

        // Создаем документ для поля _id группы: { _id: { dateTrunc: ..., paramId: ... } }
        Document groupIdDoc = new Document();
        // Добавляем выражение $dateTrunc для округления времени до часа
        groupIdDoc.append("bucketTime", new Document("$dateTrunc",
                new Document("date", "$lastPollingTime")
                        .append("unit", "hour")
                        .append("timezone", "UTC")));
        // Добавляем поле параметра
        groupIdDoc.append("paramId", "$parameters._id");

        // Создаем документ всей операции $group
        Document groupDoc = new Document("$group", new Document("_id", groupIdDoc)
                .append("avgValue", new Document("$avg", "$parameters.value"))
        );

        // Превращаем Document в AggregationOperation
        AggregationOperation groupOp = context -> groupDoc;

        // 5. PROJECT: Формирование итогового вида (вытаскиваем поля из _id)
        ProjectionOperation project = Aggregation.project()
                .andExpression("_id.bucketTime").as("timestamp")
                .and("avgValue").as("value")
                .and("_id.paramId").as("parameterId");

        Aggregation aggregation = Aggregation.newAggregation(match, unwind, filterParams, groupOp, project);

        // Выполнение
        AggregationResults<Document> results = mongoTemplate.aggregate(aggregation, deviceName, Document.class);

        List<ChartDataPoint> points = new ArrayList<>();
        for (Document doc : results.getMappedResults()) {
            Object tsObj = doc.get("timestamp");
            Long timestamp = null;

            if (tsObj instanceof Date) {
                timestamp = ((Date) tsObj).getTime();
            } else if (tsObj instanceof Long) {
                timestamp = (Long) tsObj;
            } else if (tsObj instanceof Number) {
                timestamp = ((Number) tsObj).longValue();
            }

            Number valNum = (Number) doc.get("value");
            // paramId лежит в поле parameterId благодаря проекции
            Long paramId = doc.getLong("parameterId");

            if (timestamp != null && valNum != null && paramId != null) {
                points.add(new ChartDataPoint(timestamp, valNum.doubleValue(), paramId));
            }
        }

        points.sort(Comparator.comparingLong(ChartDataPoint::getTimestamp));
        return points;
    }

    /**
     * Вспомогательный метод парсинга сырых документов.
     */
    private List<ChartDataPoint> parseDocuments(List<Document> documents, List<Long> parameterIds) {
        List<ChartDataPoint> points = new ArrayList<>();

        for (Document doc : documents) {
            Date date = doc.getDate("lastPollingTime");
            if (date == null) continue;
            Long timestamp = date.getTime();

            List<Map<String, Object>> params = (List<Map<String, Object>>) doc.get("parameters");
            if (params == null) continue;

            for (Map<String, Object> param : params) {
                Number idNumber = (Number) param.get("_id");
                if (idNumber == null) continue;
                Long paramId = idNumber.longValue();

                if (parameterIds.contains(paramId)) {
                    Object valueObj = param.get("value");
                    if (valueObj instanceof Number) {
                        points.add(new ChartDataPoint(timestamp, ((Number) valueObj).doubleValue(), paramId));
                    }
                }
            }
        }

        points.sort(Comparator.comparingLong(ChartDataPoint::getTimestamp));
        return points;
    }
}