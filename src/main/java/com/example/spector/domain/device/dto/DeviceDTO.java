package com.example.spector.domain.device.dto;

import com.example.spector.domain.threshold.dto.ThresholdDTO;
import com.example.spector.domain.devicetype.dto.DeviceTypeDTO;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class DeviceDTO {
    private Long id;
    private String name;
    private String ipAddress;
    private DeviceTypeDTO deviceType;
    private String description;
    private String location;
    private Integer period;
    private String alarmType;
    private Boolean isEnable;
    private List<ThresholdDTO> threshold;
}
