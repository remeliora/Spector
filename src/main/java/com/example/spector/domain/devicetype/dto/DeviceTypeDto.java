package com.example.spector.domain.devicetype.dto;

import com.example.spector.domain.devicetype.DeviceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Value;

/**
 * DTO for {@link DeviceType}
 */
@Value
public class DeviceTypeDto {
    Long id;

    @Size(max = 100)
    @NotBlank
    String name;

    @Size(max = 50)
    String className;

    @Size
    String description;
}