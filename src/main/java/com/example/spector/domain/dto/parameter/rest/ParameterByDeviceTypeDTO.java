package com.example.spector.domain.dto.parameter.rest;

import io.swagger.v3.oas.annotations.media.Schema;
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
    private String name;

    @Schema(description = "Описание параметра",
            example = "Количество используемой оперативной памяти",
            maxLength = 500)
    private String description;
}
