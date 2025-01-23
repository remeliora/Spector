package com.example.spector.service;

import com.example.spector.checker.db.DBChecker;
import com.example.spector.script.SnmpPollingGetAsync;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SnmpPollingService {   //Сервис для взаимодействия со скриптом опроса
    private final SnmpPollingGetAsync snmpPollingGetAsync;
    private final List<DBChecker> dbCheckers; // Внедряются все реализации DBChecker
    private static final Logger logger = LoggerFactory.getLogger(SnmpPollingService.class);

    @Async
    @Scheduled(fixedDelay = 15000)
    public void executePolling() {

        // Проверка подключения к базам данных
        for (DBChecker dbChecker : dbCheckers) {
            if (!dbChecker.isAccessible(3)) {
//                System.out.println("Одна из баз данных недоступна, завершение опроса.");
                logger.error("Проблема с доступом к БД, завершение опроса.");
                return;
            }
        }

        snmpPollingGetAsync.pollDevices();
    }
}
