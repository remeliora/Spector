package com.example.spector.domain.threshold.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "DTO для создания порога")
public class ThresholdCreateDTO {
    @Schema(description = "Нижнее пороговое значение",
            example = "10.5")
    private Double lowValue;

    @Schema(description = "Точное значение для сравнения (если применимо)",
            example = "critical")
    private String matchExact;

    @Schema(description = "Верхнее пороговое значение",
            example = "90.0")
    private Double highValue;

    @Schema(description = "Статус активности порога",
            example = "true",
            required = true)
    @NotNull(message = "Is Enable Status is required")
    private Boolean isEnable;

    @Schema(description = "Идентификатор связанного параметра",
            example = "1",
            required = true)
    @NotNull(message = "Parameter ID is required")
    private Long parameterId;
}
