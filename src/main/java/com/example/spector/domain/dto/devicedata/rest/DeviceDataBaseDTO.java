package com.example.spector.domain.dto.devicedata.rest;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class DeviceDataBaseDTO {
    private String id;
    private Long deviceId;
    private String deviceName;
    private String deviceIp;
    private Boolean isEnable;
    private String location;
    private String status;
    //    private LocalDateTime lastPollingTime;
}
