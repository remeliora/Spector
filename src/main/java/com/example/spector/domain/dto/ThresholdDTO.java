package com.example.spector.domain.dto;

import lombok.Getter;
import lombok.Setter;
@Setter
@Getter
public class ThresholdDTO {
    private Long id;
    private Double lowValue;
    private Double highValue;
    private Boolean isEnable;
    private ParameterDTO parameter;
    private DeviceDTO device;
}
