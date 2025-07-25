package com.example.spector.domain.dto.devicetype.rest;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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
    @NotBlank(message = "Name is required")
    @Size(max = 100, message = "Name cannot exceed 100 characters")
    private String name;

    @Schema(description = "Имя класса типа устройства",
            example = "Сервис",
            maxLength = 50)
    @Size(max = 50, message = "Class name cannot exceed 500 characters")
    private String className;

    @Schema(description = "Описание типа устройства",
            example = "Firewall",
            maxLength = 500)
    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;
}
