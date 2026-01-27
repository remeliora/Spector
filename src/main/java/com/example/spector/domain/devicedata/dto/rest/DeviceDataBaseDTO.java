package com.example.spector.domain.devicedata.dto.rest;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
public class DeviceDataBaseDTO {
    private Long deviceId;
    private String deviceName;
    private String deviceIp;
    private Boolean isEnable;
    private String location;
    private String status;
    private LocalDateTime lastPollingTime;
}
