package com.example.spector.checker.db;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PostgresChecker implements DBChecker {
    private final JdbcTemplate jdbcTemplate;
    private static final Logger logger = LoggerFactory.getLogger(PostgresChecker.class);
    @Override
    public boolean isAccessible(int retryCount) {
        int attempts = 0;

        while (attempts < retryCount) {
            try {
                jdbcTemplate.queryForObject("SELECT 1", Integer.class);
//                System.out.println("Соединение с Postgres базой данных успешно установлено.");
                logger.info("Соединение с Postgres базой данных успешно установлено.");
                return true;
            } catch (Exception e) {
                attempts++;
//                System.out.println("Попытка " + attempts + " подключения к Postgres базе данных не удалась: " + e.getMessage());
                logger.error("Попытка {} подключения к Postgres базе данных не удалась: {}", attempts, e.getMessage());
            }
        }

//        System.out.println("Ошибка подключения к Postgres базе данных после " + retryCount + " попыток.");
        logger.error("Ошибка подключения к Postgres базе данных после {} попыток.", retryCount);
        return false;
    }
}
