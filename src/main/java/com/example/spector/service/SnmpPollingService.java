package com.example.spector.service;

import com.example.spector.checker.DBConnectionChecker;
import com.example.spector.checker.DBConnectionResult;
import com.example.spector.script.SnmpPollingGetAsync;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SnmpPollingService {   //Сервис для взаимодействия со скриптом опроса
    private final SnmpPollingGetAsync snmpPollingGetAsync;
    private final DBConnectionChecker dbConnectionChecker;

    @Async
    @Scheduled(fixedDelay = 10000)
    public void executePolling() {

        // Проверка подключения к базе данных перед началом опроса устройств
        DBConnectionResult dbResult = dbConnectionChecker.isDBAccessible(3); // 3 попытки подключения

        if (!dbResult.isSuccess()) {
            System.out.println("База данных недоступна: " + dbResult.getMessage());

            return;
        }

        snmpPollingGetAsync.pollDevices();
    }
}
