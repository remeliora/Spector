package com.example.spector.checker.db;

import com.example.spector.domain.enums.EventType;
import com.example.spector.domain.enums.MessageType;
import com.example.spector.event.EventDispatcher;
import com.example.spector.event.EventMessage;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PostgresChecker implements DBChecker {
    private final JdbcTemplate jdbcTemplate;
    private final EventDispatcher eventDispatcher;
//    private static final Logger logger = LoggerFactory.getLogger(PostgresChecker.class);
    @Override
    public boolean isAccessible(int retryCount) {
        int attempts = 0;

        while (attempts < retryCount) {
            try {
                jdbcTemplate.queryForObject("SELECT 1", Integer.class);
//                System.out.println("Соединение с Postgres базой данных успешно установлено.");
//                logger.info("Соединение с Postgres базой данных успешно установлено.");
                eventDispatcher.dispatch(EventMessage.log(EventType.SYSTEM, MessageType.INFO,
                        "Соединение с Postgres успешно установлено."));

                return true;
            } catch (Exception e) {
                attempts++;
//                System.out.println("Попытка " + attempts + " подключения к Postgres базе данных не удалась: " + e.getMessage());
//                logger.error("Попытка {} подключения к Postgres не удалась: {}", attempts, e.getMessage());
                eventDispatcher.dispatch(EventMessage.log(EventType.SYSTEM, MessageType.ERROR,
                        "Попытка " + attempts + " подключения к Postgres не удалась: " + e.getMessage()));
            }
        }

//        System.out.println("Ошибка подключения к Postgres базе данных после " + retryCount + " попыток.");
//        logger.error("Ошибка подключения к базе данных Postgres после {} попыток.", retryCount);
        eventDispatcher.dispatch(EventMessage.log(EventType.SYSTEM, MessageType.ERROR,
                "Ошибка подключения к базе данных Postgres после " + retryCount + " попыток."));
        return false;
    }
}
