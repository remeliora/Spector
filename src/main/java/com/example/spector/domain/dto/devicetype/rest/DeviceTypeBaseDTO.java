package com.example.spector.domain.dto.devicetype.rest;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeviceTypeBaseDTO {
    private Long id;

    @NotBlank(message = "Name is required")
    @Size(max = 100, message = "Name cannot exceed 100 characters")
    private String name;

    @Size(max = 50, message = "Class name cannot exceed 500 characters")
    private String className;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;
}
