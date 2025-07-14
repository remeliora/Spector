package com.example.spector.domain.dto.device;

import com.example.spector.domain.dto.threshold.ThresholdDTO;
import com.example.spector.domain.dto.devicetype.DeviceTypeDTO;
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
