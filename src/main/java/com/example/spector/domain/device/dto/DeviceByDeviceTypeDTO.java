package com.example.spector.domain.device.dto;

import io.swagger.v3.oas.annotations.media.Schema;
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
    private String name;

    @Schema(description = "IP-адрес устройства",
            example = "192.168.1.1",
            required = true,
            pattern = "^\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}$")
    private String ipAddress;
}
