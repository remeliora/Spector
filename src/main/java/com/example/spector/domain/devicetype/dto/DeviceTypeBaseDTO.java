package com.example.spector.domain.devicetype.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeviceTypeBaseDTO {
    private Long id;

    private String name;

    private String className;

    private String description;
}
