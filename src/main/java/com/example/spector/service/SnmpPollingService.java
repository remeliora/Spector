package com.example.spector.service;

import com.example.spector.domain.enums.EventType;
import com.example.spector.domain.enums.MessageType;
import com.example.spector.modules.checker.db.DBChecker;
import com.example.spector.modules.event.EventDispatcher;
import com.example.spector.modules.event.EventMessage;
import com.example.spector.script.SnmpPollingGetAsync;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class SnmpPollingService {   //Сервис для взаимодействия со скриптом опроса
    private final SnmpPollingGetAsync snmpPollingGetAsync;
    private final List<DBChecker> dbCheckers; // Внедряются все реализации DBChecker
    private final AppSettingService appSettingService;
    private final EventDispatcher eventDispatcher;

    private ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

    private volatile long pollPeriod;  // Переменная для хранения текущего периода

    @PostConstruct
    public void init() {
        // Получаем первоначальный период из базы данных
        updatePollPeriod();

        // Планируем выполнение задачи с первоначальной периодичностью
        schedulePollingTask();
    }

    private void updatePollPeriod() {
        // Обновляем период опроса из базы данных
        this.pollPeriod = appSettingService.getPollPeriod() * 1000; // период в миллисекундах
    }

    private void schedulePollingTask() {
        // Планируем выполнение задачи с периодичностью pollPeriod
        scheduledExecutorService.scheduleWithFixedDelay(this::executePolling, 0, pollPeriod, TimeUnit.MILLISECONDS);
    }

    @Scheduled(fixedRate = 5000)  // Задача обновления периодичности
    public void refreshPollPeriod() {
        // Проверяем и обновляем период с базы данных
        long newPollPeriod = appSettingService.getPollPeriod() * 1000;
        if (newPollPeriod != pollPeriod) {
            pollPeriod = newPollPeriod;
            eventDispatcher.dispatch(EventMessage.log(EventType.SYSTEM, MessageType.INFO,
                    "Период опроса обновлён: " + (pollPeriod / 1000) + " сек"));
            // Перезапускаем задачу с новым периодом
            scheduledExecutorService.shutdownNow();
            scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();  // переинициализируем планировщик
            schedulePollingTask();
        }
    }

    @Async
//    @Scheduled(fixedRateString = "#{@appSettingService.getPollPeriod() * 1000}")
    public void executePolling() {
        if (!appSettingService.isPollingActive()) {
            eventDispatcher.dispatch(EventMessage.log(EventType.SYSTEM, MessageType.INFO,
                    "Опрос отключен в настройках."));

            return;
        }
        // Проверка подключения к базам данных
        for (DBChecker dbChecker : dbCheckers) {
            if (!dbChecker.isAccessible(3)) {
//                System.out.println("Одна из баз данных недоступна, завершение опроса.");
                eventDispatcher.dispatch(EventMessage.log(EventType.SYSTEM, MessageType.ERROR,
                        "Проблема с доступом к БД, завершение опроса."));

                return;
            }
        }
        snmpPollingGetAsync.pollDevices();
    }
}
