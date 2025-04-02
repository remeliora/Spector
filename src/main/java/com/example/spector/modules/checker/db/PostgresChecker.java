package com.example.spector.modules.checker.db;

import com.example.spector.domain.enums.EventType;
import com.example.spector.domain.enums.MessageType;
import com.example.spector.modules.event.EventDispatcher;
import com.example.spector.modules.event.EventMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PostgresChecker implements DBChecker {
    private final JdbcTemplate jdbcTemplate;
    private final EventDispatcher eventDispatcher;

    @Override
    public boolean isAccessible(int retryCount) {
        int attempts = 0;

        while (attempts < retryCount) {
            try {
                jdbcTemplate.queryForObject("SELECT 1", Integer.class);
//                System.out.println("Соединение с Postgres базой данных успешно установлено.");
                eventDispatcher.dispatch(EventMessage.log(EventType.SYSTEM, MessageType.INFO,
                        "Postgres: соединение успешно установлено."));

                return true;
            } catch (Exception e) {
                attempts++;
//                System.out.println("Попытка " + attempts + " подключения к Postgres базе данных не удалась: " + e.getMessage());
                eventDispatcher.dispatch(EventMessage.log(EventType.SYSTEM, MessageType.ERROR,
                        "Postgres: " + attempts + "-я попытка подключения не удалась: " + e.getMessage()));
            }
        }

//        System.out.println("Ошибка подключения к Postgres базе данных после " + retryCount + " попыток.");
        eventDispatcher.dispatch(EventMessage.log(EventType.SYSTEM, MessageType.ERROR,
                "Postgres: не удалось подключиться к БД после " + retryCount + " попыток."));
        return false;
    }
}
