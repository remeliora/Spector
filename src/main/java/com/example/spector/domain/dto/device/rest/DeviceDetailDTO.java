package com.example.spector.domain.dto.device.rest;

import com.example.spector.domain.enums.AlarmType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Schema(description = "DTO для детального представления устройства")
public class DeviceDetailDTO {
    @Schema(description = "Уникальный идентификатор устройства", example = "1")
    private Long id;

    @Schema(description = "Наименование устройства", example = "Server-01")
    private String name;

    @Schema(description = "IP-адрес устройства", example = "192.168.1.100")
    private String ipAddress;

    @Schema(description = "Идентификатор типа устройства", example = "1")
    private Long deviceTypeId;

    @Schema(description = "Описание устройства", example = "Main application server")
    private String description;

    @Schema(description = "Локация устройства", example = "Data Center 1")
    private String location;

    @Schema(description = "Период опроса устройства в секундах", example = "30")
    private Integer period;

    @Schema(description = "Тип тревоги")
    private AlarmType alarmType;

    @Schema(description = "Статус активности устройства", example = "true")
    private Boolean isEnable;

    @Schema(description = "Список идентификаторов активных параметров", example = "[1, 2, 4]")
    private List<Long> activeParametersId;
}
