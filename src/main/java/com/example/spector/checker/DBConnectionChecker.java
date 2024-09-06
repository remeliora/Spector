package com.example.spector.checker;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DBConnectionChecker {
    private final JdbcTemplate jdbcTemplate;

    /**
     * Проверка доступности базы данных с возможностью ретрая.
     *
     * @param retryCount Количество попыток подключения в случае неудачи.
     * @return Объект DBConnectionResult с информацией о результате проверки.
     */

    public DBConnectionResult isDBAccessible(int retryCount) {
        int attempts = 0;
        
        while (attempts < retryCount) {
            try {
                jdbcTemplate.queryForObject("SELECT 1", Integer.class);
                System.out.println("Соединение с базой данных успешно установлено.");

                return new DBConnectionResult(true, "Соединение с базой данных успешно установлено.");

            } catch (Exception e) {
                attempts++;
                System.out.println("Попытка {" + attempts + "} подключения к базе данных не удалась: {" + e.getMessage() + "}");

                if (attempts >= retryCount) {
                    System.out.println("Ошибка подключения к базе данных после {" + retryCount + "} попыток: {" + e.getMessage() + "}");
                    return new DBConnectionResult(false, "Ошибка подключения к базе данных после " + retryCount + " попыток: " + e.getMessage());
                }
            }
        }
        return new DBConnectionResult(false, "Неизвестная ошибка подключения к базе данных.");
    }
}
