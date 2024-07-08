package com.example.spector.domain.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class DeviceTypeDTO {
    private Long id;
    private String name;
    private String description;
    private Set<Long> deviceId;
    private Set<Long> parameterId;
}
