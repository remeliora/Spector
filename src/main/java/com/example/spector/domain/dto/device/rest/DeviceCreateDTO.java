package com.example.spector.domain.dto.device.rest;

import com.example.spector.domain.enums.AlarmType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Schema(description = "DTO для создания устройства")
public class DeviceCreateDTO {
    @Schema(description = "Наименование устройства",
            example = "Server-01",
            required = true, maxLength = 100)
    @NotBlank(message = "Device name is required")
    @Size(max = 100, message = "Name cannot exceed 100 characters")
    private String name;

    @Schema(description = "IP-адрес устройства",
            example = "192.168.1.100",
            required = true)
    @NotBlank(message = "IP address is required")
    @Pattern(regexp = "^\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}$",
            message = "Invalid IP address format")
    private String ipAddress;

    @Schema(description = "Идентификатор типа устройства",
            example = "1",
            required = true)
    @NotNull(message = "Device type ID is required")
    private Long deviceTypeId;

    @Schema(description = "Описание устройства",
            example = "Main application server",
            maxLength = 500)
    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    @Schema(description = "Локация устройства",
            example = "Data Center 1",
            maxLength = 100)
    @Size(max = 100, message = "Location cannot exceed 100 characters")
    private String location;

    @Schema(description = "Период опроса устройства в секундах",
            example = "30",
            required = true,
            minimum = "15",
            maximum = "60")
    @NotNull(message = "Period is required")
    @Min(value = 15, message = "Period must be at least 15 seconds")
    @Max(value = 60, message = "Period cannot exceed 60 seconds")
    private Integer period;

    @Schema(description = "Тип тревоги",
            required = true)
    @NotNull(message = "Alarm Type is required")
    private AlarmType alarmType;

    @Schema(description = "Статус активности устройства",
            example = "true",
            required = true)
    @NotNull(message = "Is Enable status is required")
    private Boolean isEnable;

    @Schema(description = "Список идентификаторов активных параметров",
            example = "[1, 2, 4]")
    private List<Long> activeParametersId;
}
