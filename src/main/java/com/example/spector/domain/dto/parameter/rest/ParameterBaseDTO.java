package com.example.spector.domain.dto.parameter.rest;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Базовый DTO параметра")
public class ParameterBaseDTO {
    @Schema(description = "Уникальный идентификатор параметра", example = "1")
    private Long id;

    @Schema(description = "Наименование параметра", example = "hrPhysicalMemoryUsed")
    private String name;

    @Schema(description = "Адрес (OID) параметра", example = "1.3.6.1.2.1.25.2.3.1.5.1")
    private String address;

    @Schema(description = "Единица измерения параметра", example = "Bytes")
    private String metric;

    @Schema(description = "Описание параметра", example = "Количество используемой оперативной памяти")
    private String description;
}
