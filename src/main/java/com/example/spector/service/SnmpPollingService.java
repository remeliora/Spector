package com.example.spector.service;

import com.example.spector.script.SnmpPollingGetAsync;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SnmpPollingService {   //Сервис для взаимодействия со скриптом опроса
    private final SnmpPollingGetAsync snmpPollingGetAsync;

    @Scheduled(fixedRate = 10000)
    @Async
    public void executePolling() {
        snmpPollingGetAsync.pollDevices();
    }
}
