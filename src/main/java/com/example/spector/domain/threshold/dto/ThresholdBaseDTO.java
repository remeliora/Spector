package com.example.spector.domain.threshold.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Базовый DTO порога")
public class ThresholdBaseDTO {
    @Schema(description = "Уникальный идентификатор порога",
            example = "1")
    private Long id;

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
            example = "true")
    private Boolean isEnable;

    @Schema(description = "Идентификатор связанного параметра",
            example = "1")
    private Long parameterId;
}
