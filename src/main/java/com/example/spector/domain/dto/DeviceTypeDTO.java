package com.example.spector.domain.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
public class DeviceTypeDTO {
    private Long id;
    private String name;
    private String description;
    private List<DeviceDTO> device;
    private List<ParameterDTO> parameter;
}
