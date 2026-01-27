package com.example.spector.domain.devicedata.dto.rest;

import com.example.spector.domain.parameter.dto.ParameterDataDTO;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Setter
@Getter
public class DeviceDataDetailDTO {
    private Long deviceId;
    private String deviceName;
    private String deviceIp;
    private Boolean isEnable;
    private String location;
    private String status;
    private LocalDateTime lastPollingTime;
    private List<ParameterDataDTO> parameters;
}
