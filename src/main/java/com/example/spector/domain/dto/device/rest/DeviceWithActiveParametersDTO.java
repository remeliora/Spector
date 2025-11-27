package com.example.spector.domain.dto.device.rest;

import com.example.spector.domain.dto.parameter.rest.ParameterByDeviceTypeDTO;
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
