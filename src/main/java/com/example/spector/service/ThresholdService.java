package com.example.spector.service;

import com.example.spector.domain.device.Device;
import com.example.spector.domain.devicetype.DeviceType;
import com.example.spector.domain.parameter.dto.ParameterShortDTO;
import com.example.spector.domain.threshold.dto.ThresholdBaseDTO;
import com.example.spector.domain.threshold.dto.ThresholdCreateDTO;
import com.example.spector.domain.threshold.dto.ThresholdDetailDTO;
import com.example.spector.domain.threshold.dto.ThresholdUpdateDTO;
import com.example.spector.domain.enums.DataType;
import com.example.spector.domain.enums.EventType;
import com.example.spector.domain.enums.MessageType;
import com.example.spector.domain.override.DeviceParameterOverride;
import com.example.spector.domain.parameter.Parameter;
import com.example.spector.domain.statusdictionary.StatusDictionary;
import com.example.spector.domain.threshold.Threshold;
import com.example.spector.mapper.BaseDTOConverter;
import com.example.spector.modules.event.EventDispatcher;
import com.example.spector.modules.event.EventMessage;
import com.example.spector.repositories.DeviceParameterOverrideRepository;
import com.example.spector.repositories.DeviceRepository;
import com.example.spector.repositories.ParameterRepository;
import com.example.spector.repositories.ThresholdRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ThresholdService {
    private final BaseDTOConverter baseDTOConverter;
    private final DeviceRepository deviceRepository;
    private final ThresholdRepository thresholdRepository;
    private final ParameterRepository parameterRepository;
    private final DeviceParameterOverrideRepository deviceParameterOverrideRepository;

    // Получение списка с фильтрацией
    public List<ThresholdBaseDTO> getThresholdByDevice(Long deviceId) {
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new EntityNotFoundException("Device type not found with id: " + deviceId));

        return thresholdRepository.findThresholdByDevice(device).stream()
                .map(threshold -> baseDTOConverter.toDTO(threshold, ThresholdBaseDTO.class))
                .toList();
    }

    // Получение деталей порога
    public ThresholdDetailDTO getThresholdDetails(Long deviceId, Long thresholdId) {
        Threshold threshold = thresholdRepository.findThresholdByIdAndDeviceId(thresholdId, deviceId)
                .orElseThrow(() -> new EntityNotFoundException("Threshold not found with id: " + thresholdId +
                                                               " for device type: " + deviceId));

        return baseDTOConverter.toDTO(threshold, ThresholdDetailDTO.class);
    }

    // Получение списка доступных параметров для порога
    @Transactional
    public List<ParameterShortDTO> getAvailableParametersForDevice(Long deviceId) {
        // 1. Получаем устройство
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new EntityNotFoundException("Устройство не найдено"));

        // 2. Получаем тип устройства
        DeviceType deviceType = device.getDeviceType();
        if (deviceType == null) {
            throw new IllegalStateException("Тип устройства не назначен");
        }

        // 3. Получаем все параметры для этого типа устройства
        List<Parameter> allParameters = parameterRepository.findParameterByDeviceType(deviceType);

        // 4. Получаем пороги устройства
        List<Threshold> deviceThresholds = thresholdRepository.findThresholdByDevice(device);

        // 5. Собираем ID параметров с существующими порогами
        Set<Long> usedParameterIds = deviceThresholds.stream()
                .map(threshold -> threshold.getParameter().getId())
                .collect(Collectors.toSet());

        // 6. Получаем ID неактивных параметров для данного устройства
        List<DeviceParameterOverride> deviceOverrides = deviceParameterOverrideRepository
                .findByDeviceId(deviceId);

        // 7. Создаем мапу для быстрого доступа к активности параметров
        Map<Long, Boolean> parameterActiveStatus = deviceOverrides.stream()
                .collect(Collectors.toMap(
                        override -> override.getParameter().getId(),
                        DeviceParameterOverride::getIsActive
                ));

        // 6. Фильтруем, сортируем и конвертируем в DTO
        return allParameters.stream()
                .filter(parameter -> !usedParameterIds.contains(parameter.getId()))
                .filter(parameter -> {
                    // Если для параметра есть переопределение, проверяем его активность
                    // Если переопределения нет, считаем параметр активным (true)
                    Boolean isActive = parameterActiveStatus.get(parameter.getId());
                    return isActive == null || isActive; // Если null, то активен, если true, то активен
                })
                .sorted(Comparator.comparing(Parameter::getName))
                .map(parameter -> {
                    ParameterShortDTO dto = new ParameterShortDTO();
                    dto.setId(parameter.getId());
                    dto.setName(parameter.getName());
                    dto.setDescription(parameter.getDescription());
                    dto.setDataType(parameter.getDataType());

                    if (parameter.getDataType() == DataType.ENUMERATED && parameter.getStatusDictionary() != null) {
                        try {
                            StatusDictionary dict = parameter.getStatusDictionary();
                            dto.setEnumeration(dict.getEnumValues());
                        } catch (Exception e) {
                            System.out.println("Failed to load statuses for: " + parameter.getName() + e);
                        }
                    }

                    return dto;
                })
                .collect(Collectors.toList());
    }

    //================
    //      CRUD
    //================

    // Создание нового порога
    @Transactional
    public ThresholdDetailDTO createThreshold(Long deviceId, ThresholdCreateDTO createDTO,
                                              String clientIp, EventDispatcher eventDispatcher) {
        Parameter parameter = parameterRepository.findById(createDTO.getParameterId())
                .orElseThrow(() -> new EntityNotFoundException("Parameter not found"));

        // Для ENUM параметров проверяем matchExact
        if (parameter.getDataType() == DataType.ENUMERATED) {
            createDTO.setLowValue(null);
            createDTO.setHighValue(null);

            if (createDTO.getMatchExact() == null) {
                throw new IllegalArgumentException("Status value required for ENUM parameter");
            }

            // Дополнительная проверка, что статус существует
            StatusDictionary dict = parameter.getStatusDictionary();
            if (dict == null) {
                throw new IllegalStateException("Status dictionary not assigned to parameter: " + parameter.getName());
            }

            // Проверяем, существует ли введённое название статуса
            boolean statusExists = dict.getEnumValues().values().stream()
                    .anyMatch(status -> status.equalsIgnoreCase(createDTO.getMatchExact()));

            if (!statusExists) {
                throw new IllegalArgumentException("Неверное название статуса: " + createDTO.getMatchExact());
            }
        } else {
            // Для не-ENUM параметров обнуляем
            createDTO.setMatchExact(null);
        }

        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new EntityNotFoundException("Device not found with id: " + deviceId));

        Threshold newThreshold = new Threshold();
        newThreshold.setLowValue(createDTO.getLowValue());
        newThreshold.setMatchExact(createDTO.getMatchExact());
        newThreshold.setHighValue(createDTO.getHighValue());
        newThreshold.setIsEnable(createDTO.getIsEnable());
        newThreshold.setParameter(parameter);
        newThreshold.setDevice(device);

        Threshold savedThreshold = thresholdRepository.save(newThreshold);

        String message;
        if (parameter.getDataType() == DataType.ENUMERATED) {
            message = String.format("IP %s: User created threshold for device '%s': parameter='%s', matchExact='%s', enabled=%s",
                    clientIp, device.getName(), parameter.getName(), createDTO.getMatchExact(), createDTO.getIsEnable());
        } else {
            message = String.format("IP %s: User created threshold for device '%s': parameter='%s', lowValue=%.2f, highValue=%.2f, enabled=%s",
                    clientIp, device.getName(), parameter.getName(), createDTO.getLowValue(), createDTO.getHighValue(), createDTO.getIsEnable());
        }
        EventMessage event = EventMessage.log(EventType.REQUEST, MessageType.INFO, message);
        eventDispatcher.dispatch(event);

        return baseDTOConverter.toDTO(savedThreshold, ThresholdDetailDTO.class);
    }

    // Обновление порога
    @Transactional
    public ThresholdDetailDTO updateThreshold(Long deviceId, Long thresholdId, ThresholdUpdateDTO updateDTO,
                                              String clientIp, EventDispatcher eventDispatcher) {
        // 1. Проверка совпадения ID
        if (!thresholdId.equals(updateDTO.getId())) {
            throw new IllegalArgumentException("ID in path and body must match");
        }

        // 2. Поиск существующего порога
        Threshold existingThreshold = thresholdRepository.findThresholdByIdAndDeviceId(thresholdId, deviceId)
                .orElseThrow(() -> new EntityNotFoundException("Threshold not found with id: " + thresholdId +
                                                               " for device: " + deviceId));

        Double oldLowValue = existingThreshold.getLowValue();
        Double oldHighValue = existingThreshold.getHighValue();
        String oldMatchExact = existingThreshold.getMatchExact();
        Boolean oldIsEnable = existingThreshold.getIsEnable();
        Long oldParameterId = existingThreshold.getParameter().getId();

        // 3. Валидация для ENUM-параметров
        if (existingThreshold.getParameter().getDataType() == DataType.ENUMERATED) {
            updateDTO.setLowValue(null);
            updateDTO.setHighValue(null);

            if (updateDTO.getMatchExact() == null) {
                throw new IllegalArgumentException("Status value required for ENUM parameter");
            }

            StatusDictionary dict = existingThreshold.getParameter().getStatusDictionary();
            if (dict == null) {
                throw new IllegalStateException("Status dictionary not assigned to parameter: " +
                                                existingThreshold.getParameter().getName());
            }

            boolean statusExists = dict.getEnumValues().values().stream()
                    .anyMatch(status -> status.equalsIgnoreCase(updateDTO.getMatchExact()));

            if (!statusExists) {
                throw new IllegalArgumentException("Неверное название статуса: " + updateDTO.getMatchExact());
            }
        }
        // 4. Валидация для числовых параметров
        else {
            updateDTO.setMatchExact(null);

            if (updateDTO.getLowValue() == null || updateDTO.getHighValue() == null) {
                throw new IllegalArgumentException("Both low and high values are required for numeric parameter");
            }

            if (updateDTO.getLowValue() > updateDTO.getHighValue()) {
                throw new IllegalArgumentException("Low value must be less than or equal to high value");
            }
        }

        // 5. Обновление полей
        existingThreshold.setLowValue(updateDTO.getLowValue());
        existingThreshold.setMatchExact(updateDTO.getMatchExact());
        existingThreshold.setHighValue(updateDTO.getHighValue());
        existingThreshold.setIsEnable(updateDTO.getIsEnable());
        if (!existingThreshold.getParameter().getId().equals(updateDTO.getParameterId())) {
            Parameter newParameter = parameterRepository.findById(updateDTO.getParameterId())
                    .orElseThrow(() -> new EntityNotFoundException("Parameter not found with id: " +
                                                                   updateDTO.getParameterId()));
            existingThreshold.setParameter(newParameter);
        }

        Threshold updatedThreshold = thresholdRepository.save(existingThreshold);

        String changes = "";
        if (!Objects.equals(oldLowValue, updateDTO.getLowValue())) {
            changes += String.format("lowValue: %.2f -> %.2f, ", oldLowValue, updateDTO.getLowValue());
        }
        if (!Objects.equals(oldHighValue, updateDTO.getHighValue())) {
            changes += String.format("highValue: %.2f -> %.2f, ", oldHighValue, updateDTO.getHighValue());
        }
        if (!Objects.equals(oldMatchExact, updateDTO.getMatchExact())) {
            changes += String.format("matchExact: '%s' -> '%s', ", oldMatchExact, updateDTO.getMatchExact());
        }
        if (!Objects.equals(oldIsEnable, updateDTO.getIsEnable())) {
            changes += String.format("enabled: %s -> %s, ", oldIsEnable, updateDTO.getIsEnable());
        }
        if (!Objects.equals(oldParameterId, updateDTO.getParameterId())) {
            changes += String.format("parameter: %d -> %d, ", oldParameterId, updateDTO.getParameterId());
        }

        if (!changes.isEmpty()) {
            changes = changes.substring(0, changes.length() - 2); // Убираем последнюю запятую
            String message = String.format("IP %s: User updated threshold ID %d: %s",
                    clientIp, thresholdId, changes);
            EventMessage event = EventMessage.log(EventType.REQUEST, MessageType.INFO, message);
            eventDispatcher.dispatch(event);
        }

        return baseDTOConverter.toDTO(updatedThreshold, ThresholdDetailDTO.class);
    }

    // Удаление порога
    @Transactional
    public void deleteThreshold(Long deviceId, Long thresholdId, String clientIp, EventDispatcher eventDispatcher) {
        Threshold existingThreshold = thresholdRepository.findThresholdByIdAndDeviceId(thresholdId, deviceId)
                .orElseThrow(() -> new EntityNotFoundException("Threshold not found with id: " + thresholdId +
                                                               " for device: " + deviceId));

        String parameterName = existingThreshold.getParameter().getName();
        String deviceName = existingThreshold.getDevice().getName();

        thresholdRepository.deleteById(thresholdId);

        String message = String.format("IP %s: User deleted threshold for device '%s': parameter='%s'",
                clientIp, deviceName, parameterName);
        EventMessage event = EventMessage.log(EventType.REQUEST, MessageType.INFO, message);
        eventDispatcher.dispatch(event);
    }
}
