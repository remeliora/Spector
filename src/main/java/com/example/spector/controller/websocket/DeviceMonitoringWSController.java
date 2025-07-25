package com.example.spector.controller.websocket;

import com.example.spector.service.device.DevicePushService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class DeviceMonitoringWSController {
    private final DevicePushService devicePushService;

    @MessageMapping("/devices/{id}/details")
//    @SendTo("/topic/monitoring/summary")
    public void onDetailsRequest(@DestinationVariable Long id) {
        devicePushService.pushDeviceDetails(id);
    }

    @MessageMapping("/devices/{id}/details/subscribe")
    public void subscribeToDetails(@DestinationVariable Long id) {
        devicePushService.subscribeToDetails(id);
        devicePushService.pushDeviceDetails(id); // Первый запрос
    }

    @MessageMapping("/devices/{id}/details/unsubscribe")
    public void unsubscribeFromDetails(@DestinationVariable Long id) {
        devicePushService.unsubscribeFromDetails(id);
    }

    @MessageMapping("/devices/summary/request")
    public void requestAllSummaries() {
        devicePushService.pushAllSummaries(); // Принудительная отправка
    }
}
