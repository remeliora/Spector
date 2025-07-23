package com.example.spector.domain.dto.threshold.rest;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ThresholdCreateDTO {
    //    @NotNull(message = "Low Value is required")
    private Double lowValue;

    //    @NotNull(message = "Match Exact is required")
    private String matchExact;

    //    @NotNull(message = "High Value is required")
    private Double highValue;

    @NotNull(message = "Is Enable Status is required")
    private Boolean isEnable;

    @NotNull(message = "Parameter ID is required")
    private Long parameterId;
}
