package com.example.spector.domain.dto;

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
    private Integer period;
    private String alarmType;
    private Boolean isEnable;
    private List<ThresholdDTO> threshold;
}
