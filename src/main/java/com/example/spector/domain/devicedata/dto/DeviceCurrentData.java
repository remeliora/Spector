package com.example.spector.domain.devicedata.dto;

import com.example.spector.modules.datapattern.ParameterData;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class DeviceCurrentData {
    private Long deviceId;
    private String deviceName;
    private String deviceIp;
    private String location;
    private String status;
    private LocalDateTime lastPollingTime;
    private List<ParameterData> parameters;
}
