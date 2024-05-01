package com.example.spector.service;

import com.example.spector.script.SnmpPollingGetAsync;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class SnmpPollingService {   //Сервис для взаимодействия со скриптом опроса
    @Autowired
    private SnmpPollingGetAsync snmpPollingGetAsync;

//    @Scheduled(fixedRate = 10000)
//    @Async
//    public void executePolling() {
//        snmpPollingGetAsync.pollDevices();
//    }
}
