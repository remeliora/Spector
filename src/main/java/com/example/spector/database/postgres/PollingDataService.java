package com.example.spector.database.postgres;

import com.example.spector.domain.device.Device;
import com.example.spector.domain.enums.DataType;
import com.example.spector.domain.override.DeviceParameterOverride;
import com.example.spector.domain.parameter.Parameter;
import com.example.spector.domain.setting.AppSetting;
import com.example.spector.domain.statusdictionary.StatusDictionary;
import com.example.spector.domain.threshold.Threshold;
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

    public AppSetting getAppSetting() {
        return appSettingRepository.findFirstBy().orElse(null);
    }

    public boolean isPollingActive() {
        AppSetting setting = getAppSetting();
        // Возвращаем true по умолчанию, если настройки не найдены или pollActive=null
        return setting != null && Boolean.TRUE.equals(setting.getPollActive());
    }

    public List<Device> getDeviceByIsEnableTrue() {
        return deviceRepository.findDeviceByIsEnableTrue();
    }

    public Device getDeviceById(Long id) {
        return deviceRepository.findById(id).orElse(null); // Возвращаем null, если устройство не найдено
    }

    public List<Parameter> getActiveParametersForDevice(Long deviceId) {
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new RuntimeException("Device not found for id: " + deviceId));

        List<Parameter> deviceTypeParameters = parameterRepository.findParameterByDeviceType(device.getDeviceType());

        List<DeviceParameterOverride> overrides = device.getDeviceParameterOverrides();

        return deviceTypeParameters
                .stream()
                .filter(parameter -> isParameterActive(parameter, overrides))
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

    public List<Threshold> getThresholdsByParameterDTOAndIsEnableTrue(Parameter parameter) {
        return thresholdRepository.findThresholdByParameterIdAndIsEnableTrue(parameter.getId());
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
