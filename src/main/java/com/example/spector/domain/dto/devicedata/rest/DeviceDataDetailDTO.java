package com.example.spector.domain.dto.devicedata.rest;

import com.example.spector.domain.dto.parameter.ParameterDataDTO;
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
