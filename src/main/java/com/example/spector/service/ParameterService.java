package com.example.spector.service;

import com.example.spector.domain.device.Device;
import com.example.spector.domain.devicetype.DeviceType;
import com.example.spector.domain.enums.DataType;
import com.example.spector.domain.enums.EventType;
import com.example.spector.domain.enums.MessageType;
import com.example.spector.domain.override.DeviceParameterOverride;
import com.example.spector.domain.parameter.Parameter;
import com.example.spector.domain.parameter.dto.*;
import com.example.spector.domain.statusdictionary.StatusDictionary;
import com.example.spector.mapper.BaseDTOConverter;
import com.example.spector.modules.event.EventDispatcher;
import com.example.spector.modules.event.EventMessage;
import com.example.spector.repositories.*;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ParameterService {
    private final BaseDTOConverter baseDTOConverter;
    private final DeviceRepository deviceRepository;
    private final ParameterRepository parameterRepository;
    private final DeviceTypeRepository deviceTypeRepository;
    private final StatusDictionaryRepository statusDictionaryRepository;
    private final DeviceParameterOverrideRepository deviceParameterOverrideRepository;

    // Получение списка с фильтрацией
    public List<ParameterBaseDTO> getParameterByDeviceType(Long deviceTypeId) {
        DeviceType deviceType = deviceTypeRepository.findById(deviceTypeId)
                .orElseThrow(() -> new EntityNotFoundException("Device type not found with id: " + deviceTypeId));

        return parameterRepository.findParameterByDeviceType(deviceType).stream()
                .map(parameter -> baseDTOConverter.toDTO(parameter, ParameterBaseDTO.class))
                .toList();
    }

    // Получение деталей параметра
    public ParameterDetailDTO getParameterDetails(Long deviceTypeId, Long parameterId) {
        Parameter parameter = parameterRepository.findParameterByIdAndDeviceTypeId(parameterId, deviceTypeId)
                .orElseThrow(() -> new EntityNotFoundException("Parameter not found with id: " + parameterId +
                                                               " for device type: " + deviceTypeId));

        ParameterDetailDTO dto = baseDTOConverter.toDTO(parameter, ParameterDetailDTO.class);

        // Загружаем активные устройства для этого параметра
        List<Long> activeDeviceIds = deviceParameterOverrideRepository.findByParameterIdAndIsActiveTrue(parameterId)
                .stream()
                .map(override -> override.getDevice().getId())
                .toList();

        dto.setActiveDevicesId(activeDeviceIds);

        return dto;
    }

    public List<ParameterByDeviceTypeDTO> getParametersByType(Long deviceTypeId) {
        DeviceType deviceType = deviceTypeRepository.findById(deviceTypeId)
                .orElseThrow(() -> new EntityNotFoundException("Device type not found"));

        return parameterRepository.findParameterByDeviceType(deviceType).stream()
                .map(parameter -> baseDTOConverter.toDTO(parameter, ParameterByDeviceTypeDTO.class))
                .toList();
    }

    //================
    //      CRUD
    //================

    // Создание нового параметр
    @Transactional
    public ParameterDetailDTO createParameter(Long deviceTypeId, ParameterCreateDTO createDTO,
                                              String clientIp, EventDispatcher eventDispatcher) {
        // 1. Находим тип устройства
        DeviceType deviceType = deviceTypeRepository.findById(deviceTypeId)
                .orElseThrow(() -> new EntityNotFoundException("Device type not found with id: " + deviceTypeId));

        // 2. Находим словарь, если передан ID
        StatusDictionary statusDictionary = null;
        if (createDTO.getStatusDictionaryId() != null) {
            statusDictionary = statusDictionaryRepository.findById(createDTO.getStatusDictionaryId())
                    .orElseThrow(() -> new EntityNotFoundException("Status dictionary not found with id: "
                                                                   + createDTO.getStatusDictionaryId()));
        }

        // 3. Создаем и сохраняем параметр
        Parameter newParameter = new Parameter();
        newParameter.setName(createDTO.getName());
        newParameter.setAddress(createDTO.getAddress());
        newParameter.setMetric(createDTO.getMetric());
        newParameter.setAdditive(createDTO.getAdditive());
        newParameter.setCoefficient(createDTO.getCoefficient());
        newParameter.setDescription(createDTO.getDescription());
        newParameter.setDataType(createDTO.getDataType());
        newParameter.setDeviceType(deviceType);
        newParameter.setStatusDictionary(statusDictionary);

        Parameter savedParameter = parameterRepository.save(newParameter);

        // 4. Получаем все устройства этого типа
        List<Device> devices = deviceRepository.findDeviceByDeviceTypeId(deviceTypeId);

        // 5. Создаем переопределения для всех устройств
        List<DeviceParameterOverride> overrides = devices
                .stream()
                .map(device -> {
                    DeviceParameterOverride deviceParameterOverride = new DeviceParameterOverride();
                    deviceParameterOverride.setDevice(device);
                    deviceParameterOverride.setParameter(savedParameter);
                    // Устанавливаем активность, если устройство в списке
                    deviceParameterOverride.setIsActive(createDTO.getActiveDevicesId().contains(device.getId()));

                    return deviceParameterOverride;
                })
                .toList();

        // 6. Сохраняем все переопределения
        deviceParameterOverrideRepository.saveAll(overrides);

        String message = String.format("IP %s: User created parameter: name='%s', address='%s', metric='%s', type='%s'",
                clientIp, newParameter.getName(), newParameter.getAddress(),
                newParameter.getMetric(), newParameter.getDataType());
        EventMessage event = EventMessage.log(EventType.REQUEST, MessageType.INFO, message);
        eventDispatcher.dispatch(event);

        // 7. Возвращаем DTO
        ParameterDetailDTO result = baseDTOConverter.toDTO(savedParameter, ParameterDetailDTO.class);
        result.setActiveDevicesId(createDTO.getActiveDevicesId());

        return result;
    }

    // Обновление параметра
    @Transactional
    public ParameterDetailDTO updateParameter(Long deviceTypeId, Long parameterId, ParameterUpdateDTO updateDTO,
                                              String clientIp, EventDispatcher eventDispatcher) {
        // 1. Проверяем существование параметра
        if (!parameterId.equals(updateDTO.getId())) {
            throw new IllegalArgumentException("ID in path and body must match");
        }

        Parameter existingParameter = parameterRepository.findParameterByIdAndDeviceTypeId(parameterId, deviceTypeId)
                .orElseThrow(() -> new EntityNotFoundException("Parameter not found with id: " + parameterId +
                                                               " for device type: " + deviceTypeId));

        String oldName = existingParameter.getName();
        String oldAddress = existingParameter.getAddress();
        String oldMetric = existingParameter.getMetric();
        Double oldAdditive = existingParameter.getAdditive();
        Double oldCoefficient = existingParameter.getCoefficient();
        String oldDescription = existingParameter.getDescription();
        DataType oldDataType = existingParameter.getDataType();
        Long oldStatusDictId = existingParameter.getStatusDictionary() != null ?
                existingParameter.getStatusDictionary().getId() : null;

        // 2. Находим новый словарь, если передан ID
        StatusDictionary statusDictionary = null;
        if (updateDTO.getStatusDictionaryId() != null) {
            statusDictionary = statusDictionaryRepository.findById(updateDTO.getStatusDictionaryId())
                    .orElseThrow(() -> new EntityNotFoundException("Status dictionary not found with id: "
                                                                   + updateDTO.getStatusDictionaryId()));
        }

        // 3. Обновляем только разрешенные поля
        existingParameter.setName(updateDTO.getName());
        existingParameter.setAddress(updateDTO.getAddress());
        existingParameter.setMetric(updateDTO.getMetric());
        existingParameter.setAdditive(updateDTO.getAdditive());
        existingParameter.setCoefficient(updateDTO.getCoefficient());
        existingParameter.setDescription(updateDTO.getDescription());
        existingParameter.setDataType(updateDTO.getDataType());
        existingParameter.setStatusDictionary(statusDictionary);

        Parameter updatedParameter = parameterRepository.save(existingParameter);

        // 4. Получаем все устройства этого типа
        List<Device> devices = deviceRepository.findDeviceByDeviceTypeId(deviceTypeId);

        String changes = "";
        if (!Objects.equals(oldName, updateDTO.getName())) {
            changes += String.format("name: '%s' -> '%s', ", oldName, updateDTO.getName());
        }
        if (!Objects.equals(oldAddress, updateDTO.getAddress())) {
            changes += String.format("address: '%s' -> '%s', ", oldAddress, updateDTO.getAddress());
        }
        if (!Objects.equals(oldMetric, updateDTO.getMetric())) {
            changes += String.format("metric: '%s' -> '%s', ", oldMetric, updateDTO.getMetric());
        }
        if (!Objects.equals(oldAdditive, updateDTO.getAdditive())) {
            changes += String.format("additive: '%s' -> '%s', ", oldAdditive, updateDTO.getAdditive());
        }
        if (!Objects.equals(oldCoefficient, updateDTO.getCoefficient())) {
            changes += String.format("coefficient: '%s' -> '%s', ", oldCoefficient, updateDTO.getCoefficient());
        }
        if (!Objects.equals(oldDescription, updateDTO.getDescription())) {
            changes += String.format("description: '%s' -> '%s', ", oldDescription, updateDTO.getDescription());
        }
        if (!Objects.equals(oldDataType, updateDTO.getDataType())) {
            changes += String.format("dataType: '%s' -> '%s', ", oldDataType, updateDTO.getDataType());
        }
        if (!Objects.equals(oldStatusDictId, updateDTO.getStatusDictionaryId())) {
            changes += String.format("statusDictionaryId: %s -> %s, ", oldStatusDictId,
                    updateDTO.getStatusDictionaryId());
        }

        if (!changes.isEmpty()) {
            changes = changes.substring(0, changes.length() - 2); // Убираем последнюю запятую
            String message = String.format("IP %s: User updated parameter ID %d: %s",
                    clientIp, parameterId, changes);
            EventMessage event = EventMessage.log(EventType.REQUEST, MessageType.INFO, message);
            eventDispatcher.dispatch(event);
        }

        // 5. Получаем существующие переопределения
        List<DeviceParameterOverride> existingOverrides = deviceParameterOverrideRepository
                .findByParameterId(parameterId);

        // 6. Создаем или обновляем переопределения
        Map<Long, DeviceParameterOverride> existingOverrideMap = existingOverrides.stream()
                .collect(Collectors.toMap(
                        override -> override.getDevice().getId(),
                        Function.identity()
                ));

        List<DeviceParameterOverride> updatedOverrides = new ArrayList<>();

        for (Device device : devices) {
            DeviceParameterOverride override;

            if (existingOverrideMap.containsKey(device.getId())) {
                // Обновляем существующее переопределение
                override = existingOverrideMap.get(device.getId());
            } else {
                // Создаем новое переопределение
                override = new DeviceParameterOverride();
                override.setDevice(device);
                override.setParameter(updatedParameter);
            }

            // Устанавливаем активность
            override.setIsActive(updateDTO.getActiveDevicesId().contains(device.getId()));
            updatedOverrides.add(override);
        }

        // 7. Сохраняем обновленные переопределения
        deviceParameterOverrideRepository.saveAll(updatedOverrides);

        // 8. Возвращаем DTO
        ParameterDetailDTO result = baseDTOConverter.toDTO(updatedParameter, ParameterDetailDTO.class);
        result.setActiveDevicesId(updateDTO.getActiveDevicesId());

        return result;
    }

    // Удаление параметра
    @Transactional
    public void deleteParameter(Long deviceTypeId, Long parameterId, String clientIp, EventDispatcher eventDispatcher) {
        Parameter existingParameter = parameterRepository.findParameterByIdAndDeviceTypeId(parameterId, deviceTypeId)
                .orElseThrow(() -> new EntityNotFoundException("Parameter not found with id: " + parameterId +
                                                               " for device type: " + deviceTypeId));

        String name = existingParameter.getName();
        String address = existingParameter.getAddress();
        String metric = existingParameter.getMetric();

        parameterRepository.deleteById(parameterId);

        String message = String.format("IP %s: User deleted parameter ID %d, device type ID %d: name='%s', " +
                                       "address='%s', metric='%s'",
                clientIp, parameterId, deviceTypeId, name, address, metric);
        EventMessage event = EventMessage.log(EventType.REQUEST, MessageType.INFO, message);
        eventDispatcher.dispatch(event);
    }
}
