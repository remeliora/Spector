package com.example.spector.domain.threshold.dto;

import com.example.spector.domain.threshold.Threshold;
import jakarta.validation.constraints.NotNull;
import lombok.Value;

/**
 * DTO for {@link Threshold}
 */
@Value
public class ThresholdDto {
    Long id;

    Double lowValue;

    String matchExact;

    Double highValue;

    @NotNull(message = "Is Enable Status is required")
    Boolean isEnable;

    @NotNull(message = "Parameter ID is required")
    Long parameterId;

    Long deviceId;
}