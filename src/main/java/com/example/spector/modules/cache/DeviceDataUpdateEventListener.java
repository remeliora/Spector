package com.example.spector.modules.cache;

import com.example.spector.domain.DeviceCurrentData;
import com.example.spector.repositories.DeviceRepository;
import com.example.spector.service.DeviceService;
import com.example.spector.service.WebSocketNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class DeviceDataUpdateEventListener {
    private final WebSocketNotificationService webSocketNotificationService;
    private final DeviceService deviceService;

    @EventListener
    public void handleDeviceDataUpdated(DeviceDataUpdatedEvent event) {
        Long deviceId = event.getDeviceId();
        DeviceCurrentData data = event.getCurrentData();

        // Получаем актуальное состояние isEnable из БД
        Boolean isEnable = deviceService.getIsEnable(deviceId);

        // Отправляем обновление для главной страницы
        webSocketNotificationService.notifyDeviceStatus(deviceId, data.getStatus(), isEnable);

        // Отправляем обновление для страницы деталей
        webSocketNotificationService.notifyDeviceParameters(data);
    }
}
