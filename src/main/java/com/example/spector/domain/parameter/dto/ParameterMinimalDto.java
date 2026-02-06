package com.example.spector.domain.parameter.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Value;

/**
 * DTO for {@link com.example.spector.domain.parameter.Parameter}
 */
@Value
public class ParameterMinimalDto {
    Long id;
    @Size(max = 100)
    @NotBlank
    String name;
    @NotBlank
    @Pattern(regexp = "^\\d+(?:\\.\\d+)*$")
    String address;
    @Size(max = 10)
    String metric;
    @Size(max = 500)
    String description;
}