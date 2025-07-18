package com.example.spector.database.mongodb;

import com.example.spector.domain.EnumeratedStatus;
import com.example.spector.domain.enums.EventType;
import com.example.spector.domain.enums.MessageType;
import com.example.spector.modules.event.EventDispatcher;
import com.example.spector.modules.event.EventMessage;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
        this.objectMapper = objectMapper;
    }

    public Map<Integer, String> getStatusName(String parameterName) {
        // Сначала пробуем MongoDB
        try {
            Map<Integer, String> mongoResult = loadFromMongo(parameterName);

            return mongoResult;
        } catch (Exception mongoEx) {
            eventDispatcher.dispatch(EventMessage.log(EventType.SYSTEM, MessageType.ERROR,
                    "MongoDB недоступна, пробуем файловый кэш для " + parameterName));

            try {
                return loadFromFile(parameterName);
            } catch (Exception fileEx) {
                eventDispatcher.dispatch(EventMessage.log(EventType.SYSTEM, MessageType.ERROR,
                        "Не удалось загрузить словарь статусов ни из MongoDB, ни из файла"));
                throw new IllegalStateException("No available dictionary source for: " + parameterName);
            }
        }
    }

    private Map<Integer, String> loadFromMongo(String parameterName) {
        if (!enumeratedStatusMongoTemplate.collectionExists(parameterName)) {
            throw new IllegalArgumentException("No Collection found for parameter: " + parameterName);
        }

        List<EnumeratedStatus> statusList = enumeratedStatusMongoTemplate.findAll(
                EnumeratedStatus.class, parameterName);

        if (statusList.isEmpty()) {
            throw new IllegalArgumentException("No status found for parameter: " + parameterName);
        }

        return statusList.get(0).getEnumValues();
    }

    private Map<Integer, String> loadFromFile(String parameterName) throws IOException {
        Path filePath = dictionariesDir.resolve(parameterName + ".json");
        if (!Files.exists(filePath)) {
            throw new FileNotFoundException("Dictionary file not found: " + filePath);
        }

        String content = Files.readString(filePath, StandardCharsets.UTF_8);
        return objectMapper.readValue(content, new TypeReference<>() {});
    }

}
