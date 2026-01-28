package com.example.spector.domain.parameter.dto;

import com.example.spector.domain.enums.DataType;
import com.example.spector.domain.parameter.Parameter;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Value;

import java.util.List;

/**
 * DTO for {@link Parameter}
 */
@Value
public class ParameterDto {
    @NotNull
    Long id;

    @Size(max = 100)
    @NotBlank
    String name;

    @Pattern(regexp = "^\\d+(?:\\.\\d+)*$")
    @NotBlank
    String address;

    @Size(max = 10)
    String metric;

    Double additive;

    Double coefficient;

    @Size(max = 500)
    String description;

    @NotNull
    DataType dataType;

    Long statusDictionaryId;

    List<Long> activeDevicesId;
}