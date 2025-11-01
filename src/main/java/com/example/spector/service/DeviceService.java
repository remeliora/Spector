package com.example.spector.service;

import com.example.spector.domain.Device;
import com.example.spector.domain.DeviceParameterOverride;
import com.example.spector.domain.DeviceType;
import com.example.spector.domain.Parameter;
import com.example.spector.domain.dto.device.rest.*;
import com.example.spector.domain.dto.devicetype.rest.DeviceTypeShortDTO;
import com.example.spector.domain.dto.enums.EnumDTO;
import com.example.spector.domain.dto.parameter.rest.ParameterByDeviceTypeDTO;
import com.example.spector.mapper.BaseDTOConverter;
import com.example.spector.modules.polling.PollingManager;
import com.example.spector.repositories.DeviceParameterOverrideRepository;
import com.example.spector.repositories.DeviceRepository;
import com.example.spector.repositories.DeviceTypeRepository;
import com.example.spector.repositories.ParameterRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DeviceService {
    private final DeviceRepository deviceRepository;
    private final BaseDTOConverter baseDTOConverter;
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .enable(SerializationFeature.INDENT_OUTPUT);
    private final ParameterRepository parameterRepository;
    private final DeviceTypeRepository deviceTypeRepository;
    private final DeviceParameterOverrideRepository deviceParameterOverrideRepository;
    private final PollingManager pollingManager;
    private final EnumService enumService;

    // Включить / выключить устройство
    public void setEnable(Long deviceId, boolean enabled) {
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new EntityNotFoundException("Устройство не найдено: " + deviceId));
        device.setIsEnable(enabled);
        deviceRepository.save(device);

        if (enabled) {
            pollingManager.startPolling(deviceId); // Запускаем опрос для включенного устройства
        } else {
            pollingManager.stopPolling(deviceId); // Останавливаем опрос для выключенного устройства
        }
    }

    public List<String> getUniqueLocations() {
        return deviceRepository.findAll()
                .stream()
                .map(Device::getLocation)
                .filter(location -> location != null && !location.isEmpty())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    public DeviceDetailDTO getDeviceDetail(Long id) {
        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Device not found"));

        DeviceDetailDTO dto = baseDTOConverter.toDTO(device, DeviceDetailDTO.class);

        List<Long> activeParameterIds = deviceParameterOverrideRepository.findByDeviceIdAndIsActiveTrue(id)
                .stream()
                .map(override -> override.getParameter().getId())
                .toList();

        dto.setActiveParametersId(activeParameterIds);

        return dto;
    }

    public List<DeviceByDeviceTypeDTO> getDevicesByType(Long deviceTypeId) {
        return deviceRepository.findDeviceByDeviceTypeId(deviceTypeId).stream()
                .map(device -> baseDTOConverter.toDTO(device, DeviceByDeviceTypeDTO.class))
                .toList();
    }

    @Transactional
    public List<DeviceTypeShortDTO> getAvailableDeviceTypes() {
        return deviceTypeRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(DeviceType::getName))
                .map(deviceType -> baseDTOConverter.toDTO(deviceType, DeviceTypeShortDTO.class))
                .collect(Collectors.toList());
    }

    //================
    //      CRUD
    //================

    // Создание нового устройства
    @Transactional
    public DeviceDetailDTO createDevice(DeviceCreateDTO createDTO) {
        // 1. Проверяем существование типа устройства
        DeviceType deviceType = deviceTypeRepository.findById(createDTO.getDeviceTypeId())
                .orElseThrow(() -> new EntityNotFoundException("Device type not found"));

        // 2. Создаем устройство вручную
        Device newDevice = new Device();
        newDevice.setName(createDTO.getName());
        newDevice.setIpAddress(createDTO.getIpAddress());
        newDevice.setDeviceType(deviceType);
        newDevice.setDescription(createDTO.getDescription());
        newDevice.setLocation(createDTO.getLocation());
        newDevice.setPeriod(createDTO.getPeriod());
        newDevice.setAlarmType(createDTO.getAlarmType());
        newDevice.setIsEnable(createDTO.getIsEnable());

        Device savedDevice = deviceRepository.save(newDevice);

        // 3. Получаем все параметры этого типа
        List<Parameter> parameters = parameterRepository.findParameterByDeviceType(deviceType);

        // 4. Создаем переопределения для всех параметров
        List<DeviceParameterOverride> overrides = parameters
                .stream()
                .map(parameter -> {
                    DeviceParameterOverride deviceParameterOverride = new DeviceParameterOverride();
                    deviceParameterOverride.setDevice(savedDevice);
                    deviceParameterOverride.setParameter(parameter);
                    deviceParameterOverride.setIsActive(createDTO.getActiveParametersId().contains(parameter.getId()));

                    return deviceParameterOverride;
                })
                .toList();

        // 5. Сохраняем все переопределения
        deviceParameterOverrideRepository.saveAll(overrides);

        // 6. Возвращаем DTO
        DeviceDetailDTO result = baseDTOConverter.toDTO(savedDevice, DeviceDetailDTO.class);
        result.setActiveParametersId(createDTO.getActiveParametersId());

        return result;
    }

    // Обновление устройства
    @Transactional
    public DeviceDetailDTO updateDevice(Long id, DeviceUpdateDTO updateDTO) {
        // 1. Проверяем существование устройства
        if (!id.equals(updateDTO.getId())) {
            throw new IllegalArgumentException("ID in path and body must match");
        }

        Device existingDevice = deviceRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Device not found"));

        // Запоминаем старый тип устройства
        DeviceType oldDeviceType = existingDevice.getDeviceType();

        // 2. Обновляем только разрешенные поля
        existingDevice.setName(updateDTO.getName());
        existingDevice.setIpAddress(updateDTO.getIpAddress());
        existingDevice.setDescription(updateDTO.getDescription());
        existingDevice.setLocation(updateDTO.getLocation());
        existingDevice.setPeriod(updateDTO.getPeriod());
        existingDevice.setAlarmType(updateDTO.getAlarmType());
        existingDevice.setIsEnable(updateDTO.getIsEnable());

        // Обновление типа устройства (если изменился)
        DeviceType newDeviceType = oldDeviceType;
        if (!oldDeviceType.getId().equals(updateDTO.getDeviceTypeId())) {
            newDeviceType = deviceTypeRepository.findById(updateDTO.getDeviceTypeId())
                    .orElseThrow(() -> new EntityNotFoundException("Device type not found"));
            existingDevice.setDeviceType(newDeviceType);
        }

        Device updatedDevice = deviceRepository.save(existingDevice);

        // 3. Получаем существующие переопределения
        List<DeviceParameterOverride> existingOverrides = deviceParameterOverrideRepository.findByDeviceId(id);

        // 4. Создаем или обновляем переопределения
        Map<Long, DeviceParameterOverride> existingOverridesMap = existingOverrides
                .stream()
                .collect(Collectors.toMap(
                        override -> override.getParameter().getId(),
                        Function.identity()
                ));

        // 5. Получаем все параметры для нового типа устройства
        List<Parameter> parameters = parameterRepository.findParameterByDeviceType(newDeviceType);

        List<DeviceParameterOverride> updatedOverrides = new ArrayList<>();

        for (Parameter parameter : parameters) {
            DeviceParameterOverride override;
            if (existingOverridesMap.containsKey(parameter.getId())) {
                // Обновляем существующее переопределение
                override = existingOverridesMap.get(parameter.getId());
            } else {
                override = new DeviceParameterOverride();
                override.setDevice(updatedDevice);
                override.setParameter(parameter);
            }

            // Устанавливаем активность
            override.setIsActive(updateDTO.getActiveParametersId().contains(parameter.getId()));
            updatedOverrides.add(override);
        }

        // 6. Сохраняем обновленные переопределения
        deviceParameterOverrideRepository.saveAll(updatedOverrides);

        // Если тип устройства изменился, удаляем старые переопределения
        if (!oldDeviceType.getId().equals(newDeviceType.getId())) {
            List<Long> oldParametersId = parameters
                    .stream()
                    .map(Parameter::getId)
                    .toList();

            List<DeviceParameterOverride> obsoleteOverrides = existingOverrides
                    .stream()
                    .filter(override -> !oldParametersId.contains(override.getParameter().getId()))
                    .toList();

            deviceParameterOverrideRepository.deleteAll(obsoleteOverrides);
        }

        // 7. Возвращаем DTO
        DeviceDetailDTO result = baseDTOConverter.toDTO(updatedDevice, DeviceDetailDTO.class);
        result.setActiveParametersId(updateDTO.getActiveParametersId());

        return result;
    }

    @Transactional
    public void deleteDevice(Long id) {
        if (!deviceRepository.existsById(id)) {
            throw new IllegalArgumentException("Device not found");
        }
        deviceRepository.deleteById(id);
    }

    public Boolean getIsEnable(Long deviceId) {
        return deviceRepository.findById(deviceId)
                .map(Device::getIsEnable)
                .orElse(false); // или null, если устройство не найдено
    }

    // --- Новый метод для получения устройства с lookups ---
    public DeviceDetailWithLookupsDTO getDeviceDetailWithLookups(Long deviceId) {
        // 1. Получаем основные данные устройства
        DeviceDetailDTO device = getDeviceDetail(deviceId);

        Long deviceTypeId = device.getDeviceTypeId();

        // 2. Получаем lookup-данные
        List<DeviceTypeShortDTO> deviceTypes = getAvailableDeviceTypes();
        List<String> locations = getUniqueLocations();
        List<EnumDTO> alarmTypes = getAllAlarmTypesAsDTO();
        List<ParameterByDeviceTypeDTO> activeParameters = getParametersByTypeAsDTO(deviceTypeId);

        // 3. Создаём и заполняем DTO вручную
        DeviceDetailWithLookupsDTO result = new DeviceDetailWithLookupsDTO();
        result.setDevice(device);
        result.setDeviceTypes(deviceTypes);
        result.setLocations(locations);
        result.setAlarmTypes(alarmTypes);
        result.setParameters(activeParameters);

        return result;
    }

    private List<EnumDTO> getAllAlarmTypesAsDTO() {
        return enumService.getAlarmTypes();
    }

    private List<ParameterByDeviceTypeDTO> getParametersByTypeAsDTO(Long deviceTypeId) {
        // Получаем DeviceType для проверки существования
        DeviceType deviceType = deviceTypeRepository.findById(deviceTypeId)
                .orElseThrow(() -> new EntityNotFoundException("Device type not found with id: " + deviceTypeId));

        // Получаем параметры напрямую через репозиторий
        List<Parameter> parameters = parameterRepository.findParameterByDeviceType(deviceType);

        // Конвертируем в DTO
        return parameters.stream()
                .map(parameter -> baseDTOConverter.toDTO(parameter, ParameterByDeviceTypeDTO.class))
                .collect(Collectors.toList());
    }
}
