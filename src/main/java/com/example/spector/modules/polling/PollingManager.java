package com.example.spector.modules.polling;

import com.example.spector.domain.dto.device.DeviceDTO;
import com.example.spector.domain.enums.EventType;
import com.example.spector.domain.enums.MessageType;
import com.example.spector.modules.event.EventDispatcher;
import com.example.spector.modules.event.EventMessage;
import com.example.spector.database.postgres.PollingDataService;
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
        List<DeviceDTO> activeDevices = pollingDataService.getDeviceByIsEnableTrue();
/*        eventDispatcher.dispatch(EventMessage.log(EventType.SYSTEM, MessageType.INFO,
                "Кол-во устройств для опроса: " + activeDevices.size()));*/

        for (DeviceDTO device : activeDevices) {
            startPolling(device.getId());
        }
        eventDispatcher.dispatch(EventMessage.log(EventType.SYSTEM, MessageType.INFO,
                "Инициализация завершена"));
    }

    public void startPolling(Long deviceId) {
//        System.out.println("PollingManager: Планирование опроса для включенного устройства: " + deviceId);

        // Используем PollingDataService для получения актуального состояния
        DeviceDTO device = pollingDataService.getDeviceById(deviceId);
        if (device == null || !device.getIsEnable()) {
//            System.out.println("PollingManager: Устройство " + deviceId +
//                               " не найдено или выключено. Не планируем опрос.");
            scheduledTasks.remove(deviceId);
            return;
        }

        stopPolling(deviceId);

        Runnable pollingTask = () -> snmpPoller.pollDevice(deviceId);

        Instant startTime = LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant();
        ScheduledFuture<?> scheduledFuture = taskScheduler.schedule(pollingTask, startTime);

        scheduledTasks.put(deviceId, scheduledFuture);
//        System.out.println("PollingManager: Запланирована задача опроса для устройства " + deviceId +
//                           " (ID задачи: " + scheduledFuture.hashCode() + ").");
    }

    public void stopPolling(Long deviceId) {
//        System.out.println("PollingManager: Остановка опроса для устройства: " + deviceId);

        ScheduledFuture<?> task = scheduledTasks.remove(deviceId);

        if (task != null && !task.isCancelled()) {
            task.cancel(false);
//            System.out.println("PollingManager: Задача опроса для устройства " + deviceId + " отменена.");
        } else {
//            System.out.println("PollingManager: Задача опроса для устройства " + deviceId +
//                               " не найдена или уже отменена.");
        }
    }

    public void reschedulePolling(Long deviceId) {
        System.out.println("PollingManager: Перепланирование опроса для устройства: " + deviceId);

        // Используем PollingDataService для получения актуального состояния
        DeviceDTO currentDevice = pollingDataService.getDeviceById(deviceId);
        if (currentDevice == null) {
            System.out.println("PollingManager: Устройство " + deviceId +
                               " не найдено в БД при попытке перепланирования. Отменяем задачу.");
            stopPolling(deviceId);
            return;
        }

        if (currentDevice.getIsEnable()) {
            stopPolling(deviceId);
            startPolling(deviceId);
        } else {
            stopPolling(deviceId);
        }
    }

    // Фоновый поток, проверяющий завершённые задачи.
    // Проверяем каждые 5 секунд (можно настроить)
    @Scheduled(fixedDelay = 5000)
    public void checkCompletedTasks() {
        // Проходим по копии ключей, чтобы избежать ConcurrentModificationException при удалении из мапы
        for (Long deviceId : scheduledTasks.keySet()) {
            ScheduledFuture<?> task = scheduledTasks.get(deviceId);
            if (task != null && task.isDone()) { // Проверяем, завершена ли задача
//                System.out.println("PollingManager: Задача для устройства " + deviceId + " завершена.");

                // Проверяем статус устройства в БД
                DeviceDTO currentDevice = pollingDataService.getDeviceById(deviceId);
                if (currentDevice != null && currentDevice.getIsEnable()) {
                    // Устройство всё ещё включено, планируем следующий опрос
                    int periodSeconds = currentDevice.getPeriod();
//                    System.out.println("PollingManager: Устройство " + deviceId +
//                                       " всё ещё включено. Планируем следующий опрос через " +
//                                       periodSeconds + " сек.");

                    Runnable nextPollingTask = () -> snmpPoller.pollDevice(deviceId);

                    Instant nextTime = LocalDateTime.now()
                            .plusSeconds(periodSeconds)
                            .atZone(ZoneId.systemDefault())
                            .toInstant();

                    ScheduledFuture<?> scheduledFuture = taskScheduler.schedule(nextPollingTask, nextTime);

                    scheduledTasks.put(deviceId, scheduledFuture);
//                    System.out.println("PollingManager: Запланирована следующая задача опроса для устройства " + deviceId
//                                       + " через " + periodSeconds + " сек. (ID задачи: "
//                                       + scheduledFuture.hashCode() + ").");
                } else {
                    // Устройство выключено или удалено, удаляем задачу из мапы
//                    System.out.println("PollingManager: Устройство " + deviceId
//                                       + " выключено/удалено. Удаляем задачу из планировщика.");
                    scheduledTasks.remove(deviceId);
                }
            }
        }
    }

    @PreDestroy
    public void shutdown() {
        eventDispatcher.dispatch(EventMessage.log(EventType.SYSTEM, MessageType.INFO,
                "Остановка всех запланированных задач опроса устройств..."));
        for (Map.Entry<Long, ScheduledFuture<?>> entry : scheduledTasks.entrySet()) {
            ScheduledFuture<?> task = entry.getValue();
            if (!task.isCancelled()) {
                task.cancel(false);
                /*System.out.println("PollingManager: Задача для устройства " + entry.getKey() + " отменена.");*/
            }
        }
        scheduledTasks.clear();
        eventDispatcher.dispatch(EventMessage.log(EventType.SYSTEM, MessageType.INFO,
                "Все задачи опроса устройств остановлены"));
    }
}
