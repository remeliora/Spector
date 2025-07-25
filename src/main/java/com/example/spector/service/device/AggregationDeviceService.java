package com.example.spector.service.device;

import com.example.spector.domain.Device;
import com.example.spector.domain.DeviceParameterOverride;
import com.example.spector.domain.DeviceType;
import com.example.spector.domain.Parameter;
import com.example.spector.domain.dto.device.rest.DeviceByDeviceTypeDTO;
import com.example.spector.domain.dto.device.rest.DeviceCreateDTO;
import com.example.spector.domain.dto.device.rest.DeviceDetailDTO;
import com.example.spector.domain.dto.devicetype.rest.DeviceTypeShortDTO;
import com.example.spector.mapper.BaseDTOConverter;
import com.example.spector.repositories.DeviceParameterOverrideRepository;
import com.example.spector.repositories.DeviceRepository;
import com.example.spector.repositories.DeviceTypeRepository;
import com.example.spector.repositories.ParameterRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AggregationDeviceService {
    private final DeviceRepository deviceRepository;
    private final BaseDTOConverter baseDTOConverter;
    private final ObjectMapper objectMapper= new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .enable(SerializationFeature.INDENT_OUTPUT);
    private final ParameterRepository parameterRepository;
    private final DeviceTypeRepository deviceTypeRepository;
    private final DeviceParameterOverrideRepository deviceParameterOverrideRepository;

    // Включить / выключить устройство
    public void setEnable(Long deviceId, boolean enabled) {
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new EntityNotFoundException("Устройство не найдено: " + deviceId));
        device.setIsEnable(enabled);
        deviceRepository.save(device);
        updateJsonStatus(device, enabled);
    }

    private void updateJsonStatus(Device device, boolean enabled) {
        Path jsonPath = Paths.get("data/JSON/devices", device.getName() + ".json");
        if (!Files.exists(jsonPath)) return;

        try {
            // 1. Читаем JSON как Map
            Map<String, Object> jsonData = objectMapper.readValue(
                    jsonPath.toFile(),
                    new TypeReference<>() {}
            );

            // 2. Меняем только нужные поля
            jsonData.put("status", enabled ? "LOADING" : "DISABLED");
            if (!enabled) {
                jsonData.put("parameters", List.of());
            }

            // 3. Записываем обратно
            objectMapper.writeValue(jsonPath.toFile(), jsonData);
        } catch (IOException e) {
            System.out.println("Ошибка обновления статуса в JSON для " + device.getName() + ": " + e.getMessage());
        }
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
//        return deviceRepository.findById(id)
//                .map(device -> baseDTOConverter.toDTO(device, DeviceDetailDTO.class))
//                .orElseThrow(() -> new EntityNotFoundException("Device not found"));
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
//        Device newDevice = baseDTOConverter.toEntity(createDTO, Device.class);
//        Device savedDevice = deviceRepository.save(newDevice);
//
//        return baseDTOConverter.toDTO(savedDevice, DeviceDetailDTO.class);
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
    public DeviceDetailDTO updateDevice(Long id, DeviceDetailDTO updateDTO) {
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
//        if (!existingDevice.getDeviceType().getId().equals(updateDTO.getDeviceTypeId())) {
//            DeviceType newDeviceType = deviceTypeRepository.findById(updateDTO.getDeviceTypeId())
//                    .orElseThrow(() -> new EntityNotFoundException("Device not found with id: " +
//                                                                   updateDTO.getDeviceTypeId()));
//            existingDevice.setDeviceType(newDeviceType);
//        }
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

//        return baseDTOConverter.toDTO(updatedDevice, DeviceDetailDTO.class);
        return result;
    }

    @Transactional
    public void deleteDevice(Long id) {
        if (!deviceRepository.existsById(id)) {
            throw new IllegalArgumentException("Device not found");
        }
        deviceRepository.deleteById(id);
    }
}
