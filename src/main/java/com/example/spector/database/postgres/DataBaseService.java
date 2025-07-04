package com.example.spector.database.postgres;

import com.example.spector.domain.DeviceType;
import com.example.spector.domain.Threshold;
import com.example.spector.domain.dto.*;
import com.example.spector.domain.dto.device.DeviceDTO;
import com.example.spector.domain.dto.devicetype.DeviceTypeDTO;
import com.example.spector.domain.dto.parameter.ParameterDTO;
import com.example.spector.mapper.*;
import com.example.spector.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DataBaseService {  //Файл для работы с репозиториями сущностей БД
    private final DeviceTypeRepository deviceTypeRepository;
    private final DeviceRepository deviceRepository;
    private final ParameterRepository parameterRepository;
    private final ThresholdRepository thresholdRepository;
    private final AppSettingRepository appSettingRepository;

    private final BaseDTOConverter baseDTOConverter;

    public AppSettingDTO getAppSetting() {
        return appSettingRepository.findFirstBy()
                .map(appSetting -> baseDTOConverter.toDTO(appSetting, AppSettingDTO.class))
                .orElseThrow(() -> new RuntimeException("Настройки не найдены"));
    }

    public DeviceType getDeviceTypeById(Long id) {
        return deviceTypeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Device Type not found"));
    }

    public DeviceTypeDTO convertDeviceTypeToDTO(DeviceType deviceType) {
        return baseDTOConverter.toDTO(deviceType, DeviceTypeDTO.class);
    }

    public List<DeviceDTO> getDeviceDTOByIsEnableTrue() {
        return deviceRepository.findDeviceByIsEnableTrue().stream()
                .map(device -> baseDTOConverter.toDTO(device, DeviceDTO.class))
                .collect(Collectors.toList());
    }

    public List<ParameterDTO> getParameterDTOByDeviceType(DeviceType deviceType) {
        return parameterRepository.findParameterByDeviceType(deviceType).stream()
                .map(parameter -> baseDTOConverter.toDTO(parameter, ParameterDTO.class))
                .collect(Collectors.toList());
    }

    public List<ThresholdDTO> getThresholdsByParameterDTOAndIsEnableTrue(ParameterDTO parameterDTO) {
        List<Threshold> thresholds = thresholdRepository.findThresholdByParameterIdAndIsEnableTrue(parameterDTO.getId());
        return thresholds.stream()
                .map(threshold -> baseDTOConverter.toDTO(threshold, ThresholdDTO.class))
                .collect(Collectors.toList());
    }

    public DeviceTypeDTO loadDeviceTypeWithParameters(Long deviceTypeId) {
        // Загружаем тип устройства
        DeviceType deviceType = getDeviceTypeById(deviceTypeId);
        // Преобразуем тип устройства в DTO
        DeviceTypeDTO deviceTypeDTO = convertDeviceTypeToDTO(deviceType);
        // Загружаем параметры этого типа устройства и преобразуем параметры в DTO
        List<ParameterDTO> parameterDTOList = getParameterDTOByDeviceType(deviceType);
        // Устанавливаем параметры в DTO типа устройства
        deviceTypeDTO.setParameter(parameterDTOList);

        return deviceTypeDTO;
    }
}
