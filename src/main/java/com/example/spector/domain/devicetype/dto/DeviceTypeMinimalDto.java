package com.example.spector.domain.devicetype.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Value;

/**
 * DTO for {@link com.example.spector.domain.devicetype.DeviceType}
 */
@Value
public class DeviceTypeMinimalDto {
    Long id;
    @Size(max = 50)
    @NotBlank
    String name;
    @Size(max = 30)
    @NotBlank
    String className;
}