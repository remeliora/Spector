package com.example.spector.modules.polling;

import com.example.spector.database.postgres.PollingDataService;
import com.example.spector.domain.dto.device.DeviceDTO;
import com.example.spector.domain.enums.EventType;
import com.example.spector.domain.enums.MessageType;
import com.example.spector.modules.event.EventDispatcher;
import com.example.spector.modules.event.EventMessage;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

@Service
public class PollingManager {
    private final SnmpPoller snmpPoller;
    private final EventDispatcher eventDispatcher;
    private final PollingDataService pollingDataService;
    private final TaskScheduler taskScheduler;

    private final Map<Long, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();

    public PollingManager(SnmpPoller snmpPoller,
                          EventDispatcher eventDispatcher,
                          PollingDataService pollingDataService,
                          @Qualifier("devicePollingTaskScheduler") TaskScheduler taskScheduler) {
        this.snmpPoller = snmpPoller;
        this.eventDispatcher = eventDispatcher;
        this.pollingDataService = pollingDataService;
        this.taskScheduler = taskScheduler;
    }

    @PostConstruct
    public void initializeScheduler() {
        eventDispatcher.dispatch(EventMessage.log(EventType.SYSTEM, MessageType.INFO,
                "Инициализация планировщика..."));

        // Проверяем, активен ли опрос глобально
        if (pollingDataService.isPollingActive()) {
            List<DeviceDTO> activeDevices = pollingDataService.getDeviceByIsEnableTrue();
            for (DeviceDTO device : activeDevices) {
                startPolling(device.getId());
            }
        } else {
            eventDispatcher.dispatch(EventMessage.log(EventType.SYSTEM, MessageType.INFO,
                    "Опрос отключён. Инициализация задач пропущена."));
        }

        eventDispatcher.dispatch(EventMessage.log(EventType.SYSTEM, MessageType.INFO,
                "Инициализация завершена"));
    }

    /**
     * Глобальный запуск опроса для всех активных устройств.
     * Вызывается, когда pollActive становится true.
     */
    public void startAllPolling() {
        eventDispatcher.dispatch(EventMessage.log(EventType.SYSTEM, MessageType.INFO,
                "Запуск опроса для всех активных устройств. Синхронизация задач..."));

        // Останавливаем все текущие задачи, чтобы избежать дубликатов
        stopAllPolling();

        // Получаем активные устройства и запускаем опрос для них
        List<DeviceDTO> activeDevices = pollingDataService.getDeviceByIsEnableTrue();
        for (DeviceDTO device : activeDevices) {
            // Проверяем снова, чтобы убедиться, что isEnable не изменился
            if (Boolean.TRUE.equals(device.getIsEnable()) && pollingDataService.isPollingActive()) {
                startPolling(device.getId());
            }
        }

        eventDispatcher.dispatch(EventMessage.log(EventType.SYSTEM, MessageType.INFO,
                "Синхронизация задач завершена. Опрос включен."));
    }

    /**
     * Глобальная остановка опроса для всех устройств.
     * Вызывается, когда pollActive становится false.
     */
    public void stopAllPolling() {
        eventDispatcher.dispatch(EventMessage.log(EventType.SYSTEM, MessageType.INFO,
                "Остановка всех задач..."));

        // Проходим по копии ключей, чтобы избежать ConcurrentModificationException
        for (Long deviceId : new java.util.ArrayList<>(scheduledTasks.keySet())) {
            stopPolling(deviceId);
        }

        eventDispatcher.dispatch(EventMessage.log(EventType.SYSTEM, MessageType.INFO,
                "Все задачи остановлены.")); // Добавили сообщение об окончании
    }

    public void startPolling(Long deviceId) {
        // Проверяем, активен ли глобальный опрос
        if (!pollingDataService.isPollingActive()) {
            scheduledTasks.remove(deviceId);
            return;
        }

        // Используем PollingDataService для получения актуального состояния
        DeviceDTO device = pollingDataService.getDeviceById(deviceId);
        if (device == null || !Boolean.TRUE.equals(device.getIsEnable())) {
            stopPolling(deviceId);

            return;
        }

        stopPolling(deviceId);

        Runnable pollingTask = () -> snmpPoller.pollDevice(deviceId);

        Instant startTime = LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant();
        ScheduledFuture<?> scheduledFuture = taskScheduler.schedule(pollingTask, startTime);

        scheduledTasks.put(deviceId, scheduledFuture);
    }

    public void stopPolling(Long deviceId) {
        ScheduledFuture<?> task = scheduledTasks.remove(deviceId);

        if (task != null && !task.isCancelled()) {
            task.cancel(false);
        }
    }

    public void reschedulePolling(Long deviceId) {
        System.out.println("PollingManager: Перепланирование опроса для устройства: " + deviceId);

        // Используем PollingDataService для получения актуального состояния
        DeviceDTO currentDevice = pollingDataService.getDeviceById(deviceId);
        if (currentDevice == null) {
            stopPolling(deviceId);

            return;
        }
        // Проверяем глобальный статус опроса
        if (pollingDataService.isPollingActive() && Boolean.TRUE.equals(currentDevice.getIsEnable())) {
            startPolling(deviceId);
        } else {
            stopPolling(deviceId);
        }
    }

    // Фоновый поток, проверяющий завершённые задачи.
    // Проверяем каждые 5 секунд (можно настроить)
    @Scheduled(fixedDelay = 5000)
    public void checkCompletedTasks() {
        // Проходим по копии ключей, чтобы избежать ConcurrentModificationException при удалении из Map
        for (Long deviceId : scheduledTasks.keySet()) {
            ScheduledFuture<?> task = scheduledTasks.get(deviceId);
            if (task != null && task.isDone()) { // Проверяем, завершена ли задача
                // Проверяем глобальный статус опроса и статус устройства
                if (pollingDataService.isPollingActive()) { // Если глобальный опрос ВКЛЮЧЕН
                    DeviceDTO currentDevice = pollingDataService.getDeviceById(deviceId);
                    if (currentDevice != null && Boolean.TRUE.equals(currentDevice.getIsEnable())) {
                        int periodSeconds = currentDevice.getPeriod();
                        Runnable nextPollingTask = () -> snmpPoller.pollDevice(deviceId);
                        Instant nextTime = LocalDateTime.now()
                                .plusSeconds(periodSeconds)
                                .atZone(ZoneId.systemDefault())
                                .toInstant();
                        ScheduledFuture<?> scheduledFuture = taskScheduler.schedule(nextPollingTask, nextTime);
                        scheduledTasks.put(deviceId, scheduledFuture);
                    } else {
                        // Устройство выключено или удалено
                        scheduledTasks.remove(deviceId); // Удаляем из мапы, задача уже завершена
                    }
                } else {
                    // Глобальный опрос ВЫКЛЮЧЕН
                    scheduledTasks.remove(deviceId); // Удаляем из мапы, задача уже завершена
                }
            }
        }
    }

    @PreDestroy
    public void shutdown() {
        eventDispatcher.dispatch(EventMessage.log(EventType.SYSTEM, MessageType.INFO,
                "Остановка всех запланированных задач опроса устройств..."));

        stopAllPolling();

        eventDispatcher.dispatch(EventMessage.log(EventType.SYSTEM, MessageType.INFO,
                "Все задачи опроса устройств остановлены"));
    }
}
