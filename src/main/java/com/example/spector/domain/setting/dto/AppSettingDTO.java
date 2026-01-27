package com.example.spector.domain.setting.dto;

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
}