package com.example.spector.service;

import com.example.spector.domain.devicedata.dto.DeviceCurrentData;
import com.example.spector.domain.parameter.dto.ParameterDataDTO;
import com.example.spector.domain.websocket.DeviceDataStatusDTO;
import com.example.spector.domain.websocket.ParameterDataStatusDTO;
import com.example.spector.modules.datapattern.ParameterData;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WebSocketNotificationService {
    private final SimpMessagingTemplate messagingTemplate;

    public void notifyDeviceStatus(Long deviceId, String status, Boolean isEnable) {
        DeviceDataStatusDTO dto = new DeviceDataStatusDTO();
        dto.setDeviceId(deviceId);
        dto.setStatus(status);
        dto.setIsEnable(isEnable);

        messagingTemplate.convertAndSend("/topic/devices", dto);
    }

    // Отправка обновления для страницы деталей
    public void notifyDeviceParameters(DeviceCurrentData currentData) {
        ParameterDataStatusDTO dto = new ParameterDataStatusDTO();
        dto.setDeviceId(currentData.getDeviceId());
        dto.setDeviceName(currentData.getDeviceName());
        dto.setStatus(currentData.getStatus());

        // --- Проверка на null ---
        List<ParameterData> params = currentData.getParameters();
        List<ParameterDataDTO> paramDTOs = new ArrayList<>(); // Инициализируем пустым списком

        if (params != null) { // Если список параметров не null
            paramDTOs = params.stream()
                    .map(param -> {
                        ParameterDataDTO p = new ParameterDataDTO();
                        p.setId(param.getId());
                        p.setName(param.getName());
                        p.setValue(param.getValue() != null ? param.getValue().toString() : null);
                        p.setMetric(param.getMetric());
                        p.setDescription(param.getDescription());
                        p.setStatus(param.getStatus());
                        return p;
                    })
                    .collect(Collectors.toList());
        } // else: оставляем пустой список, если params == null

        dto.setParameters(paramDTOs);
        messagingTemplate.convertAndSend("/topic/device/" + currentData.getDeviceId(), dto);
    }
}
