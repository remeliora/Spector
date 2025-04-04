package com.example.spector.domain.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class DeviceTypeDTO {
    private Long id;
    private String name;
    private String className;
    private String description;
    private List<DeviceDTO> device;
    private List<ParameterDTO> parameter;
}
