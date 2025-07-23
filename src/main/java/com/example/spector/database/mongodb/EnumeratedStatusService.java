package com.example.spector.database.mongodb;

import com.example.spector.domain.EnumeratedStatus;
import com.example.spector.domain.enums.EventType;
import com.example.spector.domain.enums.MessageType;
import com.example.spector.modules.event.EventDispatcher;
import com.example.spector.modules.event.EventMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class EnumeratedStatusService {
    private final EventDispatcher eventDispatcher;
    private final Path dictionariesDir = Paths.get("data/JSON/enumerations");
    private final ObjectMapper objectMapper;
    private final MongoTemplate enumeratedStatusMongoTemplate;

    public EnumeratedStatusService(@Qualifier("databaseEnumeratedStatusMongoTemplate")
                                   MongoTemplate enumeratedStatusMongoTemplate, EventDispatcher eventDispatcher,
                                   ObjectMapper objectMapper) {
        this.enumeratedStatusMongoTemplate = enumeratedStatusMongoTemplate;
        this.eventDispatcher = eventDispatcher;
        this.objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .enable(SerializationFeature.INDENT_OUTPUT);

        // Проверяем доступность директории при инициализации
        if (!Files.exists(dictionariesDir)) {
            eventDispatcher.dispatch(EventMessage.log(EventType.SYSTEM, MessageType.ERROR,
                    "Директория со словарями не существует: " + dictionariesDir.toAbsolutePath()));
        } else if (!Files.isDirectory(dictionariesDir)) {
            eventDispatcher.dispatch(EventMessage.log(EventType.SYSTEM, MessageType.ERROR,
                    "Указанный путь не является директорией: " + dictionariesDir.toAbsolutePath()));
        } else if (!Files.isReadable(dictionariesDir)) {
            eventDispatcher.dispatch(EventMessage.log(EventType.SYSTEM, MessageType.ERROR,
                    "Нет прав на чтение директории: " + dictionariesDir.toAbsolutePath()));
        }
    }

    // Получение словаря по имени параметра
    public Map<Integer, String> getStatusName(String parameterName) {
        try {
            Map<Integer, String> mongoResult = loadFromMongo(parameterName);

            return mongoResult;
        } catch (Exception mongoEx) {
            eventDispatcher.dispatch(EventMessage.log(EventType.SYSTEM, MessageType.ERROR,
                    "MongoDB: " + mongoEx.getMessage() + ", пробуем файловый кэш для " + parameterName));

            try {
                Map<Integer, String> fileResult = loadFromFile(parameterName);

                return fileResult;
            } catch (Exception fileEx) {
                eventDispatcher.dispatch(EventMessage.log(EventType.SYSTEM, MessageType.ERROR,
                        "Ошибка загрузки из файла: " + fileEx.getMessage()));
                throw new IllegalStateException("Нет словаря для: " + parameterName, fileEx);
            }
        }
    }

    // Загрузка словаря из MongoDB (enumerated_status)
    private Map<Integer, String> loadFromMongo(String parameterName) {
        if (!enumeratedStatusMongoTemplate.collectionExists(parameterName)) {
            throw new IllegalArgumentException("Не найдена Коллекция для: " + parameterName);
        }

        List<EnumeratedStatus> statusList = enumeratedStatusMongoTemplate.findAll(
                EnumeratedStatus.class, parameterName);

        if (statusList.isEmpty()) {
            throw new IllegalArgumentException("Не найден статус для: " + parameterName);
        }

        return statusList.get(0).getEnumValues();
    }

    // Загрузка словаря из файла
    private Map<Integer, String> loadFromFile(String parameterName) throws IOException {
        Path filePath = dictionariesDir.resolve(parameterName + ".json");

        if (!Files.exists(filePath)) {
            throw new FileNotFoundException("Файл словаря не найден: " + filePath.toAbsolutePath());
        }

        try {
            String content = Files.readString(filePath, StandardCharsets.UTF_8);
            JsonNode rootNode = objectMapper.readTree(content);

            // Проверяем наличие поля enumValues
            if (!rootNode.has("enumValues")) {
                throw new IOException("Отсутствует обязательное поле 'enumValues'");
            }

            JsonNode enumValuesNode = rootNode.get("enumValues");

            // Преобразуем в Map<Integer, String>
            Map<Integer, String> resultMap = new HashMap<>();
            enumValuesNode.fields().forEachRemaining(entry -> {
                try {
                    int key = Integer.parseInt(entry.getKey());
                    resultMap.put(key, entry.getValue().asText());
                } catch (NumberFormatException e) {
                    eventDispatcher.dispatch(EventMessage.log(EventType.SYSTEM, MessageType.ERROR,
                            "Некорректный ключ статуса: " + entry.getKey() + " в файле " + parameterName));
                }
            });

            if (resultMap.isEmpty()) {
                throw new IOException("Словарь статусов пуст");
            }

            return resultMap;
        } catch (JsonProcessingException e) {
            throw new IOException("Ошибка парсинга JSON файла: " + e.getMessage(), e);
        }
    }

    // Сохранение словаря в файл
    public void safeToFile(String parameterName, Map<Integer, String> enumValues) throws IOException {
        Path filePath = dictionariesDir.resolve(parameterName + ".json");

        Map<String, Object> data = new HashMap<>();
        data.put("name", parameterName);
        data.put("enumValues", enumValues);

        String jsonContent = objectMapper.writeValueAsString(data);
        Files.writeString(filePath, jsonContent, StandardCharsets.UTF_8);
    }

    // Удаление файла словаря
    public void deleteFile(String parameterName) throws IOException {
        Path filePath = dictionariesDir.resolve(parameterName + ".json");
        Files.deleteIfExists(filePath);
    }

    // Обновление файла словаря
    public void updateFile(String parameterName, Map<Integer, String> enumValues) throws IOException {
        safeToFile(parameterName, enumValues);
    }
}
