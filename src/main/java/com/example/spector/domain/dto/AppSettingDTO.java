package com.example.spector.domain.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AppSettingDTO {
    Long id;
    Boolean pollActive;
    Boolean alarmActive;
    Integer pollPeriod;
}