package com.example.spector.domain.dto.appsetting;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AppSettingDTO {
    Long id;

    @NotNull(message = "Poll active status is required")
    Boolean pollActive;

    @NotNull(message = "Alarm active status is required")
    Boolean alarmActive;

    @NotNull(message = "Poll period is required")
    @Min(value = 15, message = "Poll period must be at least 15 seconds")
    Integer pollPeriod;
}