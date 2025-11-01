package com.example.spector.domain.dto.devicetype.rest;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Краткий DTO типа устройства")
public class DeviceTypeShortDTO {
    @Schema(description = "Уникальный идентификатор типа устройства", example = "1")
    private Long id;

    @Schema(description = "Наименование типа устройства",
            example = "Server",
            required = true,
            maxLength = 100)
    private String name;

    @Schema(description = "Описание типа устройства", example = "Сервер общего назначения", maxLength = 500)
    private String description;
}
