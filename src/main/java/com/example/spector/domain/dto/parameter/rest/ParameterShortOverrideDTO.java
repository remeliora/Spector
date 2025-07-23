package com.example.spector.domain.dto.parameter.rest;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ParameterShortOverrideDTO {
    private Long id;

    @NotBlank(message = "Parameter is required")
    private String name;

    @NotNull(message = "Device type ID is required")
    private Long deviceTypeId;
}
