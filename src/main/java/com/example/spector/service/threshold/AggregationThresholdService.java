package com.example.spector.service.threshold;

import com.example.spector.domain.Device;
import com.example.spector.domain.DeviceType;
import com.example.spector.domain.Parameter;
import com.example.spector.domain.Threshold;
import com.example.spector.domain.dto.parameter.rest.ParameterShortDTO;
import com.example.spector.domain.dto.threshold.rest.ThresholdBaseDTO;
import com.example.spector.domain.dto.threshold.rest.ThresholdCreateDTO;
import com.example.spector.domain.dto.threshold.rest.ThresholdDetailDTO;
import com.example.spector.mapper.BaseDTOConverter;
import com.example.spector.repositories.DeviceRepository;
import com.example.spector.repositories.ParameterRepository;
import com.example.spector.repositories.ThresholdRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AggregationThresholdService {
    private final ThresholdRepository thresholdRepository;
    private final BaseDTOConverter baseDTOConverter;
    private final DeviceRepository deviceRepository;
    private final ParameterRepository parameterRepository;

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

        // 6. Фильтруем, сортируем и конвертируем в DTO
        return allParameters.stream()
                .filter(parameter -> !usedParameterIds.contains(parameter.getId()))
                .sorted(Comparator.comparing(Parameter::getName))
                .map(parameter -> baseDTOConverter.toDTO(parameter, ParameterShortDTO.class))
                .collect(Collectors.toList());
    }

    //================
    //      CRUD
    //================

    // Создание нового порога
    @Transactional
    public ThresholdDetailDTO createThreshold(Long deviceId, ThresholdCreateDTO createDTO) {
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new EntityNotFoundException("Device not found with id: " + deviceId));

        Threshold newThreshold = baseDTOConverter.toEntity(createDTO, Threshold.class);
        newThreshold.setDevice(device);

        Threshold savedThreshold = thresholdRepository.save(newThreshold);

        return  baseDTOConverter.toDTO(savedThreshold, ThresholdDetailDTO.class);
    }

    // Обновление порога
    @Transactional
    public ThresholdDetailDTO updateThreshold(Long deviceId, Long thresholdId, ThresholdDetailDTO updateDTO) {
        if (!thresholdId.equals(updateDTO.getId())) {
            throw new IllegalArgumentException("ID in path and body must match");
        }

        Threshold existingThreshold = thresholdRepository.findThresholdByIdAndDeviceId(thresholdId, deviceId)
                .orElseThrow(() -> new EntityNotFoundException("Threshold not found with id: " + thresholdId +
                                                               " for device: " + deviceId));

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

        return  baseDTOConverter.toDTO(updatedThreshold, ThresholdDetailDTO.class);
    }

    // Удаление порога
    @Transactional
    public void deleteThreshold(Long deviceId, Long thresholdId) {
        if (!thresholdRepository.existsThresholdByIdAndDeviceId(thresholdId, deviceId)) {
            throw new EntityNotFoundException("Threshold not found with id: " + thresholdId +
                                              " for device: " + deviceId);
        }
        thresholdRepository.deleteById(thresholdId);
    }
}
