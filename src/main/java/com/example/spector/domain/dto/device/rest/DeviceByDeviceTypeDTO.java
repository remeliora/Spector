package com.example.spector.domain.dto.device.rest;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "DTO устройства по типу устройства")
public class DeviceByDeviceTypeDTO {
    @Schema(description = "Уникальный идентификатор устройства", example = "1")
    private Long id;

    @Schema(description = "Наименование устройства",
            example = "Коммуникационный сервер",
            required = true,
            maxLength = 100)
    @NotBlank(message = "Device is required")
    @Size(max = 100, message = "Name cannot exceed 100 characters")
    private String name;

    @Schema(description = "IP-адрес устройства",
            example = "192.168.1.1",
            required = true,
            pattern = "^\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}$")
    @NotBlank(message = "IP is required")
    @Pattern(regexp = "^\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}$",
            message = "Invalid IP address format")
    private String ipAddress;
}
