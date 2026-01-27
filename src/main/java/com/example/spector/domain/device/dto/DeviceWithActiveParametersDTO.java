package com.example.spector.domain.device.dto;

import com.example.spector.domain.parameter.dto.ParameterByDeviceTypeDTO;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class DeviceWithActiveParametersDTO {
    private Long id;
    private String name;
    private String ipAddress;
    private List<ParameterByDeviceTypeDTO> parameters;
}
