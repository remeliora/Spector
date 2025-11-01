package com.example.spector.domain.dto.parameter.rest;

import com.example.spector.domain.dto.device.rest.DeviceByDeviceTypeDTO;
import com.example.spector.domain.dto.enums.EnumDTO;
import com.example.spector.domain.dto.statusdictionary.StatusDictionaryShortDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@Schema(description = "DTO с деталями параметра и доступными справочниками")
public class ParameterDetailWithLookupsDTO {
    @Schema(description = "Детали параметра")
    private ParameterDetailDTO parameter;

    @Schema(description = "Список доступных типов данных")
    private List<EnumDTO> dataTypes;

    @Schema(description = "Список доступных словарей статусов")
    private List<StatusDictionaryShortDTO> statusDictionaries;

    @Schema(description = "Список доступных устройств")
    private List<DeviceByDeviceTypeDTO> devices;
}
