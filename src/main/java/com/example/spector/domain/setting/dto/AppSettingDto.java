package com.example.spector.domain.setting.dto;

import com.example.spector.domain.setting.AppSetting;
import jakarta.validation.constraints.NotNull;
import lombok.Value;

/**
 * DTO for {@link AppSetting}
 */
@Value
public class AppSettingDto {
    Long id;

    @NotNull(message = "Poll active status is required")
    Boolean pollActive;

    @NotNull(message = "Alarm active status is required")
    Boolean alarmActive;
}