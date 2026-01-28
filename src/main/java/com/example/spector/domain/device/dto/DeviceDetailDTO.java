package com.example.spector.domain.device.dto;

import com.example.spector.domain.enums.AlarmType;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class DeviceDetailDTO {
    private Long id;

    private String name;

    private String ipAddress;

    private Long deviceTypeId;

    private String description;

    private String location;

    private Integer period;

    private AlarmType alarmType;

    private Boolean isEnable;

    private List<Long> activeParametersId;
}
