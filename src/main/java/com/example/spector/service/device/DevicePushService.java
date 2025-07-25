package com.example.spector.service.device;

import com.example.spector.domain.dto.devicedata.rest.DeviceDataBaseDTO;
import com.example.spector.domain.dto.devicedata.rest.DeviceDataDetailDTO;
import com.example.spector.domain.websocket.DeviceDetailMessage;
import com.example.spector.domain.websocket.DeviceSummaryMessage;
import com.example.spector.service.devicedata.AggregationDeviceDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class DevicePushService {
    private final SimpMessagingTemplate messagingTemplate;
    private final AggregationDeviceDataService aggregationDeviceDataService;
    private Map<Long, DeviceDataBaseDTO> lastStates = new ConcurrentHashMap<>();
    private final Set<Long> activeDetailSubscriptions = ConcurrentHashMap.newKeySet();

    /**
     * Каждые 5 секунт пушим summary-обновления всем подписчикам.
     */
    @Scheduled(fixedDelay = 5000)
    public void pushAllSummaries() {
        List<DeviceDataBaseDTO> summaries = aggregationDeviceDataService.getDeviceDataSummary(Optional.empty());
        for (DeviceDataBaseDTO summary : summaries) {
            DeviceDataBaseDTO lastState = lastStates.get(summary.getDeviceId());
            if (lastState == null || !lastState.getStatus().equals(summary.getStatus())
                || !lastState.getIsEnable().equals(summary.getIsEnable())) {
                DeviceSummaryMessage msg = new DeviceSummaryMessage();
                msg.setDeviceId(summary.getDeviceId());
                msg.setStatus(summary.getStatus());
                msg.setIsEnable(summary.getIsEnable());
                messagingTemplate.convertAndSend("/topic/monitoring/summary", msg);
                lastStates.put(summary.getDeviceId(), summary);
//                System.out.println("Pushed update for device " + summary.getDeviceId() + ": "+ msg);
            }
        }
    }

    /**
     * Пушим детали конкретного устройства.
     * Можно вызывать из контроллера WebSocket или на событие фронтенда.
     */
    // Обновление деталей для активных устройств
    @Scheduled(fixedDelay = 1000)
    public void pushActiveDeviceDetails() {
        activeDetailSubscriptions.forEach(this::pushDeviceDetails);
    }

    private void sendSummary(DeviceDataBaseDTO summary) {
        DeviceSummaryMessage msg = new DeviceSummaryMessage();
        msg.setDeviceId(summary.getDeviceId());
        msg.setStatus(summary.getStatus());
        msg.setIsEnable(summary.getIsEnable());
        messagingTemplate.convertAndSend("/topic/monitoring/summary", msg);
        lastStates.put(summary.getDeviceId(), summary);
    }

    public void pushDeviceDetails(Long deviceId) {
        try {
            DeviceDataDetailDTO detail = aggregationDeviceDataService.getDeviceDataDetails(deviceId);
            DeviceDetailMessage msg = new DeviceDetailMessage();
            msg.setDeviceId(deviceId);
            msg.setParameters(detail.getParameters());
            messagingTemplate.convertAndSend("/topic/monitoring/" + deviceId + "/details", msg);
        } catch (Exception e) {
            System.out.println("Failed to push details for device " + deviceId + e);
        }
    }

    public void subscribeToDetails(Long deviceId) {
        activeDetailSubscriptions.add(deviceId);
    }

    public void unsubscribeFromDetails(Long deviceId) {
        activeDetailSubscriptions.remove(deviceId);
    }
}
