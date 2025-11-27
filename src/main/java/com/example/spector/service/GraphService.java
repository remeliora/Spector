package com.example.spector.service;

import com.example.spector.domain.Device;
import com.example.spector.domain.DeviceParameterOverride;
import com.example.spector.domain.Parameter;
import com.example.spector.domain.dto.device.rest.DeviceWithActiveParametersDTO;
import com.example.spector.domain.dto.parameter.rest.ParameterByDeviceTypeDTO;
import com.example.spector.domain.enums.DataType;
import com.example.spector.mapper.BaseDTOConverter;
import com.example.spector.repositories.DeviceParameterOverrideRepository;
import com.example.spector.repositories.DeviceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GraphService {
    private final DeviceRepository deviceRepository;
    private final BaseDTOConverter baseDTOConverter;
    private final DeviceParameterOverrideRepository deviceParameterOverrideRepository;

    /**
     * Получает список всех устройств с их активными параметрами.
     *
     * @return DTO, содержащий список устройств, каждое из которых содержит список активных параметров.
     */
    public List<DeviceWithActiveParametersDTO> getDevicesWithActiveParameters() {
        // 1. Получаем все устройства
        List<Device> allDevices = deviceRepository.findAll();

        // 2. Преобразуем каждое устройство в DTO, включая активные параметры
        return allDevices.stream()
                .map(this::mapDeviceToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Вспомогательный метод для маппинга Device в DeviceWithActiveParametersDTO.
     *
     * @param device Устройство из БД.
     * @return DTO с информацией об устройстве и его активных параметрах.
     */
    private DeviceWithActiveParametersDTO mapDeviceToDTO(Device device) {
        DeviceWithActiveParametersDTO dto = new DeviceWithActiveParametersDTO();
        dto.setId(device.getId());
        dto.setName(device.getName());
        dto.setIpAddress(device.getIpAddress());

        // 3. Получаем активные параметры для этого устройства
        List<Parameter> activeParameters = deviceParameterOverrideRepository.findByDeviceIdAndIsActiveTrue(device.getId())
                .stream()
                .map(DeviceParameterOverride::getParameter) // Получаем сам параметр из переопределения
                .filter(parameter ->
                        parameter.getDataType() != DataType.ENUMERATED && parameter.getDataType() != DataType.STRING)
                .toList();

        // 4. Преобразуем активные параметры в DTO
        List<ParameterByDeviceTypeDTO> parameterDTOs = activeParameters.stream()
                .map(param -> baseDTOConverter.toDTO(param, ParameterByDeviceTypeDTO.class))
                .collect(Collectors.toList());

        dto.setParameters(parameterDTOs);

        return dto;
    }
}
