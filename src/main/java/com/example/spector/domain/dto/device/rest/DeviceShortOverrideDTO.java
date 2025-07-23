package com.example.spector.domain.dto.device.rest;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeviceShortOverrideDTO {
    private Long id;

    @NotBlank(message = "Device is required")
    private String name;

    @NotNull(message = "Device type ID is required")
    private Long deviceTypeId;
}
