package com.example.spector.domain.dto.device.rest;

import com.example.spector.domain.dto.devicetype.rest.DeviceTypeShortDTO;
import com.example.spector.domain.dto.enums.EnumDTO;
import com.example.spector.domain.dto.parameter.rest.ParameterByDeviceTypeDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@Schema(description = "DTO с деталями устройства и доступными справочниками")
public class DeviceDetailWithLookupsDTO {
    @Schema(description = "Детали устройства")
    private DeviceDetailDTO device;

    @Schema(description = "Список доступных типов устройств")
    private List<DeviceTypeShortDTO> deviceTypes;

    @Schema(description = "Список доступных локаций")
    private List<String> locations;

    @Schema(description = "Список доступных типов тревог")
    private List<EnumDTO> alarmTypes;

    @Schema(description = "Список доступных параметров для устройства")
    private List<ParameterByDeviceTypeDTO> parameters;
}
