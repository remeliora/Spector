package com.example.spector.domain.dto.devicedata;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Map;

@Setter
@Getter
public class DeviceDataDTO {
    private String id;
    private Long deviceId;
    private String deviceName;
    private String deviceIp;
    private LocalDateTime lastPollingTime;
    private Map<String, Object> parameters;
}
