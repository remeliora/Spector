package com.example.spector.domain.dto;

import com.example.spector.domain.dto.device.DeviceDTO;
import com.example.spector.domain.dto.parameter.ParameterDTO;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ThresholdDTO {
    private Long id;
    private Double lowValue;
    private Integer matchExact;
    private Double highValue;
    private Boolean isEnable;
    private ParameterDTO parameter;
    private DeviceDTO device;
}
