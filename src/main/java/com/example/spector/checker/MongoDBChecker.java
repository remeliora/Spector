package com.example.spector.checker;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MongoDBChecker implements DBChecker{
    @Qualifier("deviceDataMongoTemplate")
    private final MongoTemplate deviceDataMongoTemplate;
    private static final Logger logger = LoggerFactory.getLogger(DeviceConnectionChecker.class);
    @Override
    public boolean isAccessible(int retryCount) {
        int attempts = 0;

        while (attempts < retryCount) {
            try {
                deviceDataMongoTemplate.executeCommand("{ ping: 1 }");
//                System.out.println("Соединение с MongoDB успешно установлено.");
                logger.info("Соединение с MongoDB успешно установлено.");
                return true;
            } catch (Exception e) {
                attempts++;
//                System.out.println("Попытка " + attempts + " подключения к MongoDB не удалась: " + e.getMessage());
                logger.error("Попытка {} подключения к MongoDB базе данных не удалась: {}", attempts, e.getMessage());
            }
        }

//        System.out.println("Ошибка подключения к MongoDB после " + retryCount + " попыток.");
        logger.error("Ошибка подключения к MongoDB базе данных после {} попыток.", retryCount);
        return false;
    }
}
