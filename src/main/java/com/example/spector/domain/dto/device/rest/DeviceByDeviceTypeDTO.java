package com.example.spector.domain.dto.device.rest;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeviceByDeviceTypeDTO {
    private Long id;
    private String name;
    private String ipAddress;
}
