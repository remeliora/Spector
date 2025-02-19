package com.example.spector.database.mongodb;

import com.example.spector.domain.EnumeratedStatus;
import com.example.spector.domain.enums.EventType;
import com.example.spector.domain.enums.MessageType;
import com.example.spector.event.EventDispatcher;
import com.example.spector.event.EventMessage;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class EnumeratedStatusService {
    private final MongoTemplate enumeratedStatusMongoTemplate;
    public EnumeratedStatusService(@Qualifier("databaseEnumeratedStatusMongoTemplate") MongoTemplate enumeratedStatusMongoTemplate, EventDispatcher eventDispatcher) {
        this.enumeratedStatusMongoTemplate = enumeratedStatusMongoTemplate;
        this.eventDispatcher = eventDispatcher;
    }
    private final EventDispatcher eventDispatcher;
    private static final Logger logger = LoggerFactory.getLogger(EnumeratedStatusService.class);
    private static final Logger deviceLogger = LoggerFactory.getLogger("DeviceLogger");
    public Map<Integer, String> getStatusName(String parameterName) {
        // Проверка, существует ли коллекция с таким именем
        if (!enumeratedStatusMongoTemplate.collectionExists(parameterName)) {
//            logger.error("No Collection found for parameter: {}", parameterName);
            eventDispatcher.dispatch(EventMessage.log(EventType.SYSTEM, MessageType.ERROR,
                    "Коллекции статусного параметра (" + parameterName + ") не найдена"));
//            deviceLogger.error("No Collection found for parameter: {}", parameterName);
            eventDispatcher.dispatch(EventMessage.log(EventType.DEVICE, MessageType.ERROR,
                    "Коллекции статусного параметра (" + parameterName + ") не найдена"));
            throw new IllegalArgumentException("No Collection found for parameter: " + parameterName);
        }
        // Находим коллекцию по имени параметра
        List<EnumeratedStatus> statusList = enumeratedStatusMongoTemplate.findAll(EnumeratedStatus.class, parameterName);

        if (!statusList.isEmpty()) {
            // Предполагаем, что в коллекции только один документ с перечислениями
            EnumeratedStatus status = statusList.get(0);
            return status.getEnumValues(); // Возвращаем карту значений статусов
        } else {
//            logger.error("No status found for parameter: {}", parameterName);
            eventDispatcher.dispatch(EventMessage.log(EventType.SYSTEM, MessageType.ERROR,
                    "Не найден статус параметра " + parameterName));
//            deviceLogger.error("No status found for parameter: {}", parameterName);
            eventDispatcher.dispatch(EventMessage.log(EventType.DEVICE, MessageType.ERROR,
                    "Не найден статус параметра " + parameterName));
            throw new IllegalArgumentException("No status found for parameter: " + parameterName);
        }
    }
}
