package com.example.spector.database.postgres;

import com.example.spector.domain.device.Device;
import com.example.spector.domain.override.DeviceParameterOverride;
import com.example.spector.domain.parameter.Parameter;
import com.example.spector.domain.setting.dto.AppSettingDTO;
import com.example.spector.domain.device.dto.DeviceDTO;
import com.example.spector.domain.parameter.dto.ParameterDTO;
import com.example.spector.domain.threshold.dto.ThresholdDTO;
import com.example.spector.domain.enums.DataType;
import com.example.spector.domain.setting.AppSetting;
import com.example.spector.domain.statusdictionary.StatusDictionary;
import com.example.spector.domain.threshold.Threshold;
import com.example.spector.mapper.BaseDTOConverter;
import com.example.spector.repositories.AppSettingRepository;
import com.example.spector.repositories.DeviceRepository;
import com.example.spector.repositories.ParameterRepository;
import com.example.spector.repositories.ThresholdRepository;
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

    private final BaseDTOConverter baseDTOConverter;

    public AppSettingDTO getAppSetting() {
        return appSettingRepository.findFirstBy()
                .map(appSetting -> baseDTOConverter.toDTO(appSetting, AppSettingDTO.class))
                .orElseThrow(() -> new RuntimeException("Настройки не найдены"));
    }

    public AppSetting getAppSettingEntity() {
        // Используем findFirstBy(), как в AppSettingService
        return appSettingRepository.findFirstBy().orElse(null);
    }

    public boolean isPollingActive() {
        AppSetting setting = getAppSettingEntity();
        // Возвращаем true по умолчанию, если настройки не найдены или pollActive=null
        return setting != null && Boolean.TRUE.equals(setting.getPollActive());
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

    public Map<Integer, String> getStatusDictionaryForParameter(Long parameterId) {
        Optional<Parameter> paramOpt = parameterRepository.findById(parameterId);

        if (paramOpt.isPresent()) {
            Parameter param = paramOpt.get();
            // Проверяем тип данных
            if (param.getDataType() == DataType.ENUMERATED) {
                StatusDictionary dictionary = param.getStatusDictionary();
                if (dictionary != null) {
                    return dictionary.getEnumValues();
                } else {
                    return Map.of();
                }
            } else {
                return Map.of();
            }
        }

        return Map.of();
    }
}
