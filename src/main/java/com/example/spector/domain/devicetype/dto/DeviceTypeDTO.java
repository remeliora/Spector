package com.example.spector.domain.devicetype.dto;

import com.example.spector.domain.device.dto.DeviceDTO;
import com.example.spector.domain.parameter.dto.ParameterDTO;
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
