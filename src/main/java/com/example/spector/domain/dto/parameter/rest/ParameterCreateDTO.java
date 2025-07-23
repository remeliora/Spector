package com.example.spector.domain.dto.parameter.rest;

import com.example.spector.domain.enums.DataType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class ParameterCreateDTO {
    @NotBlank(message = "Parameter is required")
    @Size(max = 100, message = "Name cannot exceed 100 characters")
    private String name;

    @NotBlank(message = "OID is required")
    @Pattern(regexp = "^\\d+(?:\\.\\d+)*$",
            message = "Invalid OID format (example: '1.3.6.1.2.1.X')")
    private String address;

    @Size(max = 10, message = "Metric cannot exceed 50 characters")
    private String metric;

    //    @NotNull(message = "Additive is required")
    private Double additive;

    //    @NotNull(message = "Coefficient is required")
    //    @DecimalMin(value = "0.0", message = "Coefficient cannot be negative")
    private Double coefficient;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    @NotNull(message = "Data Type is required")
    private DataType dataType;

    private List<Long> activeDevicesId = new ArrayList<>();
}
