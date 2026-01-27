package com.example.spector.domain.threshold.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ThresholdUpdateDTO {
    @NotNull(message = "ID is required for update")
    private Long id;

    private Double lowValue;

    private String matchExact;

    private Double highValue;

    @NotNull(message = "Is Enable Status is required")
    private Boolean isEnable;

    @NotNull(message = "Parameter ID is required")
    private Long parameterId;
}
