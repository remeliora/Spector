package com.example.spector.domain.dto.parameter.rest;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "DTO параметра по типу устройства")
public class ParameterByDeviceTypeDTO {
    @Schema(description = "Уникальный идентификатор параметра", example = "1")
    private Long id;

    @Schema(description = "Наименование параметра",
            example = "hrPhysicalMemoryUsed",
            required = true,
            maxLength = 100)
    @NotBlank(message = "Parameter is required")
    @Size(max = 100, message = "Name cannot exceed 100 characters")
    private String name;

    @Schema(description = "Описание параметра",
            example = "Количество используемой оперативной памяти",
            maxLength = 500)
    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;
}
