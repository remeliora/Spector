package com.example.spector.domain.dto.devicetype.rest;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Детализированное DTO типа устройства")
public class DeviceTypeDetailDTO {
    @Schema(description = "Уникальный идентификатор типа устройства", example = "1")
    private Long id;

    @Schema(description = "Наименование типа устройства",
            example = "Вычислительный сервер",
            required = true,
            maxLength = 100)
    private String name;

    @Schema(description = "Имя класса типа устройства",
            example = "Сервис",
            maxLength = 50)
    private String className;

    @Schema(description = "Описание типа устройства",
            example = "Firewall",
            maxLength = 500)
    private String description;
}
