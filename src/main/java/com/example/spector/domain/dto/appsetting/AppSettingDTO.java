package com.example.spector.domain.dto.appsetting;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "DTO настроек приложения")
public class AppSettingDTO {
    @Schema(
            description = "Уникальный идентификатор настроек",
            example = "1",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    Long id;

    @Schema(
            description = "Флаг активности опроса устройств",
            example = "true",
            required = true
    )
    @NotNull(message = "Poll active status is required")
    Boolean pollActive;

    @Schema(
            description = "Флаг активности системы оповещений",
            example = "false",
            required = true
    )
    @NotNull(message = "Alarm active status is required")
    Boolean alarmActive;

    @Schema(
            description = "Период опроса устройств в секундах",
            example = "30",
            required = true,
            minimum = "15"
    )
    @NotNull(message = "Poll period is required")
    @Min(value = 15, message = "Poll period must be at least 15 seconds")
    Integer pollPeriod;
}