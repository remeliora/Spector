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
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DevicePushService {
    private final AggregationDeviceDataService aggregationDeviceDataService;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Каждые 5 секунт пушим summary-обновления всем подписчикам.
     */
    @Scheduled(fixedDelayString = "${monitoring.push.delay:5000}")
    public void pushAllSummaries() {
        List<DeviceDataBaseDTO> summaries = aggregationDeviceDataService.getDeviceDataSummary(Optional.empty());
        for (DeviceDataBaseDTO s : summaries) {
            DeviceSummaryMessage msg = new DeviceSummaryMessage();
            msg.setDeviceId(s.getDeviceId());
            msg.setStatus(s.getStatus());
            msg.setIsEnable(s.getIsEnable());
            messagingTemplate.convertAndSend("/topic/monitoring/summary", msg);
//            System.out.println("PUSH summary: " + msg);

        }
    }

    /**
     * Пушим детали конкретного устройства.
     * Можно вызывать из контроллера WebSocket или на событие фронтенда.
     */
    public void pushDeviceDetails(Long deviceId) {
        DeviceDataDetailDTO detail = aggregationDeviceDataService.getDeviceDataDetails(deviceId);
        DeviceDetailMessage msg = new DeviceDetailMessage();
        msg.setDeviceId(deviceId);
        msg.setParameters(detail.getParameters());
        messagingTemplate.convertAndSend("/topic/monitoring/" + deviceId + "/details", msg);
    }
}
