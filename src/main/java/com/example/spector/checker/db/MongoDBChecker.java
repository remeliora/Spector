package com.example.spector.checker.db;

import com.example.spector.domain.enums.EventType;
import com.example.spector.domain.enums.MessageType;
import com.example.spector.event.EventDispatcher;
import com.example.spector.event.EventMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

@Component
public class MongoDBChecker implements DBChecker {
    private final MongoTemplate deviceDataMongoTemplate;
    private final EventDispatcher eventDispatcher;

    public MongoDBChecker(@Qualifier("databaseDeviceDataMongoTemplate") MongoTemplate deviceDataMongoTemplate, EventDispatcher eventDispatcher) {
        this.deviceDataMongoTemplate = deviceDataMongoTemplate;
        this.eventDispatcher = eventDispatcher;
    }
//    private static final Logger logger = LoggerFactory.getLogger(MongoDBChecker.class);

    @Override
    public boolean isAccessible(int retryCount) {
        int attempts = 0;

        while (attempts < retryCount) {
            try {
                deviceDataMongoTemplate.executeCommand("{ ping: 1 }");
//                System.out.println("Соединение с MongoDB успешно установлено.");
//                logger.info("Соединение с MongoDB успешно установлено.");
                eventDispatcher.dispatch(EventMessage.log(EventType.SYSTEM, MessageType.INFO,
                        "Соединение с MongoDB успешно установлено."));

                return true;
            } catch (Exception e) {
                attempts++;
//                System.out.println("Попытка " + attempts + " подключения к MongoDB не удалась: " + e.getMessage());
//                logger.error("Попытка {} подключения к MongoDB базе данных не удалась: {}", attempts, e.getMessage());
                eventDispatcher.dispatch(EventMessage.log(EventType.SYSTEM, MessageType.ERROR,
                        "Попытка " + attempts + " подключения к MongoDB не удалась: " + e.getMessage()));
            }
        }

//        System.out.println("Ошибка подключения к MongoDB после " + retryCount + " попыток.");
//        logger.error("Ошибка подключения к MongoDB базе данных после {} попыток.", retryCount);
        eventDispatcher.dispatch(EventMessage.log(EventType.SYSTEM, MessageType.ERROR,
                "Ошибка подключения к базе данных MongoDB после " + retryCount + " попыток."));

        return false;
    }
}
