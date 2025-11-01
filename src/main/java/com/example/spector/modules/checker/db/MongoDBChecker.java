package com.example.spector.modules.checker.db;

import com.example.spector.domain.enums.EventType;
import com.example.spector.domain.enums.MessageType;
import com.example.spector.modules.event.EventDispatcher;
import com.example.spector.modules.event.EventMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MongoDBChecker implements DBChecker {
    private final MongoTemplate deviceDataMongoTemplate;
    private final EventDispatcher eventDispatcher;

    @Override
    public boolean isAccessible(int retryCount) {
        int attempts = 0;

        while (attempts < retryCount) {
            try {
                deviceDataMongoTemplate.executeCommand("{ ping: 1 }");
//                System.out.println("Соединение с MongoDB успешно установлено.");
//                eventDispatcher.dispatch(EventMessage.log(EventType.SYSTEM, MessageType.INFO,
//                        "MongoDB: соединение успешно установлено."));

                return true;
            } catch (Exception e) {
                attempts++;
//                System.out.println("Попытка " + attempts + " подключения к MongoDB не удалась: " + e.getMessage());
                eventDispatcher.dispatch(EventMessage.log(EventType.SYSTEM, MessageType.ERROR,
                        "MongoDB: " + attempts + "-я попытка подключения не удалась: " + e.getMessage()));
            }
        }

//        System.out.println("Ошибка подключения к MongoDB после " + retryCount + " попыток.");
        eventDispatcher.dispatch(EventMessage.log(EventType.SYSTEM, MessageType.ERROR,
                "MongoDB: не удалось подключиться к БД после " + retryCount + " попыток."));

        return false;
    }
}
