package com.example.spector.domain.dto.parameter.rest;

import com.example.spector.domain.enums.DataType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class ParameterShortDTO {
    private Long id;

    @NotBlank(message = "Parameter is required")
    @Size(max = 100, message = "Name cannot exceed 100 characters")
    private String name;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    @NotNull(message = "Data Type is required")
    private DataType dataType;    //перечисляемый

    private Map<Integer, String> enumeration;
}
