package com.example.spector.database.postgres;

import com.example.spector.domain.*;
import com.example.spector.domain.dto.appsetting.AppSettingDTO;
import com.example.spector.domain.dto.device.DeviceDTO;
import com.example.spector.domain.dto.parameter.ParameterDTO;
import com.example.spector.domain.dto.threshold.ThresholdDTO;
import com.example.spector.mapper.BaseDTOConverter;
import com.example.spector.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PollingDataService {
    private final DeviceRepository deviceRepository;
    private final ParameterRepository parameterRepository;
    private final ThresholdRepository thresholdRepository;
    private final AppSettingRepository appSettingRepository;
    private final StatusDictionaryRepository statusDictionaryRepository;

    private final BaseDTOConverter baseDTOConverter;

    public AppSettingDTO getAppSetting() {
        return appSettingRepository.findFirstBy()
                .map(appSetting -> baseDTOConverter.toDTO(appSetting, AppSettingDTO.class))
                .orElseThrow(() -> new RuntimeException("Настройки не найдены"));
    }

    public List<DeviceDTO> getDeviceByIsEnableTrue() {
        return deviceRepository.findDeviceByIsEnableTrue().stream()
                .map(device -> baseDTOConverter.toDTO(device, DeviceDTO.class))
                .collect(Collectors.toList());
    }

    public DeviceDTO getDeviceById(Long id) {
        return deviceRepository.findById(id)
                .map(device -> baseDTOConverter.toDTO(device, DeviceDTO.class))
                .orElse(null); // Возвращаем null, если устройство не найдено
    }

    public List<ParameterDTO> getActiveParametersForDevice(Long deviceId) {
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new RuntimeException("Device not found for id: " + deviceId));

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

    public List<ThresholdDTO> getThresholdsByParameterDTOAndIsEnableTrue(ParameterDTO parameterDTO) {
        List<Threshold> thresholds = thresholdRepository.findThresholdByParameterIdAndIsEnableTrue(parameterDTO.getId());
        return thresholds.stream()
                .map(threshold -> baseDTOConverter.toDTO(threshold, ThresholdDTO.class))
                .collect(Collectors.toList());
    }

    public Map<Integer, String> getStatusName(String name) {
        Optional<StatusDictionary> statusOpt = statusDictionaryRepository.findStatusDictionaryByName(name);
        if (statusOpt.isPresent()) {
            return statusOpt.get().getEnumValues(); // Возвращаем Map из JSONB
        }
        System.out.println("Словарь статусов '" + name + "' не найден в PostgreSQL.");
        return Map.of(); // Возвращаем пустой Map, если не найден
    }
}
