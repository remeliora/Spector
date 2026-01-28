package com.example.spector.domain.threshold.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ThresholdBaseDTO {
    private Long id;

    private Double lowValue;

    private String matchExact;

    private Double highValue;

    private Boolean isEnable;

    private Long parameterId;
}
