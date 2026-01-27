package com.example.spector.domain.threshold.dto;

import com.example.spector.domain.device.dto.DeviceDTO;
import com.example.spector.domain.parameter.dto.ParameterDTO;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ThresholdDTO {
    private Long id;
    private Double lowValue;
    private String matchExact;
    private Double highValue;
    private Boolean isEnable;
    private ParameterDTO parameter;
    private DeviceDTO device;
}
