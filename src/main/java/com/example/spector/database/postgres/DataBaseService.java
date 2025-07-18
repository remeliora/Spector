package com.example.spector.database.postgres;

import com.example.spector.domain.Device;
import com.example.spector.domain.DeviceParameterOverride;
import com.example.spector.domain.Parameter;
import com.example.spector.domain.Threshold;
import com.example.spector.domain.dto.AppSettingDTO;
import com.example.spector.domain.dto.device.DeviceDTO;
import com.example.spector.domain.dto.parameter.ParameterDTO;
import com.example.spector.domain.dto.threshold.ThresholdDTO;
import com.example.spector.mapper.BaseDTOConverter;
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
    private final DeviceParameterOverrideRepository deviceParameterOverrideRepository;

    private final BaseDTOConverter baseDTOConverter;

    public AppSettingDTO getAppSetting() {
        return appSettingRepository.findFirstBy()
                .map(appSetting -> baseDTOConverter.toDTO(appSetting, AppSettingDTO.class))
                .orElseThrow(() -> new RuntimeException("Настройки не найдены"));
    }

    public List<DeviceDTO> getDeviceDTOByIsEnableTrue() {
        return deviceRepository.findDeviceByIsEnableTrue().stream()
                .map(device -> baseDTOConverter.toDTO(device, DeviceDTO.class))
                .collect(Collectors.toList());
    }

    public List<ThresholdDTO> getThresholdsByParameterDTOAndIsEnableTrue(ParameterDTO parameterDTO) {
        List<Threshold> thresholds = thresholdRepository.findThresholdByParameterIdAndIsEnableTrue(parameterDTO.getId());
        return thresholds.stream()
                .map(threshold -> baseDTOConverter.toDTO(threshold, ThresholdDTO.class))
                .collect(Collectors.toList());
    }

    public List<ParameterDTO> getActiveParametersForDevice(Long deviceId) {
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new RuntimeException("Device not found"));

        List<Parameter> deviceTypeParameters = parameterRepository.findParameterByDeviceType(device.getDeviceType());

        List<DeviceParameterOverride> overrides = device.getDeviceParameterOverrides();

        return deviceTypeParameters
                .stream()
                .filter(parameter -> isParameterActive(parameter, overrides))
                .map(parameter -> baseDTOConverter.toDTO(parameter, ParameterDTO.class))
                .collect(Collectors.toList());
    }

    private boolean isParameterActive(Parameter parameter, List<DeviceParameterOverride> overrides) {
        return overrides
                .stream()
                .filter(o -> o.getParameter().equals(parameter))
                .findFirst()
                .map(DeviceParameterOverride::getIsActive)
                .orElse(true);
    }
}
