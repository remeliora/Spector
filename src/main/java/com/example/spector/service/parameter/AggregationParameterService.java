package com.example.spector.service.parameter;

import com.example.spector.domain.Device;
import com.example.spector.domain.DeviceParameterOverride;
import com.example.spector.domain.DeviceType;
import com.example.spector.domain.Parameter;
import com.example.spector.domain.dto.parameter.rest.ParameterBaseDTO;
import com.example.spector.domain.dto.parameter.rest.ParameterByDeviceTypeDTO;
import com.example.spector.domain.dto.parameter.rest.ParameterCreateDTO;
import com.example.spector.domain.dto.parameter.rest.ParameterDetailDTO;
import com.example.spector.mapper.BaseDTOConverter;
import com.example.spector.repositories.DeviceParameterOverrideRepository;
import com.example.spector.repositories.DeviceRepository;
import com.example.spector.repositories.DeviceTypeRepository;
import com.example.spector.repositories.ParameterRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AggregationParameterService {
    private final ParameterRepository parameterRepository;
    private final BaseDTOConverter baseDTOConverter;
    private final DeviceTypeRepository deviceTypeRepository;
    private final DeviceRepository deviceRepository;
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

//        return baseDTOConverter.toDTO(parameter, ParameterDetailDTO.class);
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
    public ParameterDetailDTO createParameter(Long deviceTypeId, ParameterCreateDTO createDTO) {
        // 1. Находим тип устройства
        DeviceType deviceType = deviceTypeRepository.findById(deviceTypeId)
                .orElseThrow(() -> new EntityNotFoundException("Device type not found with id: " + deviceTypeId));

        // 2. Создаем и сохраняем параметр
//        Parameter newParameter = baseDTOConverter.toEntity(createDTO, Parameter.class);
        Parameter newParameter = new Parameter();
        newParameter.setName(createDTO.getName());
        newParameter.setAddress(createDTO.getAddress());
        newParameter.setMetric(createDTO.getMetric());
        newParameter.setAdditive(createDTO.getAdditive());
        newParameter.setCoefficient(createDTO.getCoefficient());
        newParameter.setDescription(createDTO.getDescription());
        newParameter.setDataType(createDTO.getDataType());
        newParameter.setDeviceType(deviceType);

        Parameter savedParameter = parameterRepository.save(newParameter);

        // 3. Получаем все устройства этого типа
        List<Device> devices = deviceRepository.findDeviceByDeviceTypeId(deviceTypeId);

        // 4. Создаем переопределения для всех устройств
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

        // 5. Сохраняем все переопределения
        deviceParameterOverrideRepository.saveAll(overrides);

        // 6. Возвращаем DTO
        ParameterDetailDTO result = baseDTOConverter.toDTO(savedParameter, ParameterDetailDTO.class);
        result.setActiveDevicesId(createDTO.getActiveDevicesId());

//        return baseDTOConverter.toDTO(savedParameter, ParameterDetailDTO.class);
        return result;
    }

    // Обновление параметра
    @Transactional
    public ParameterDetailDTO updateParameter(Long deviceTypeId, Long parameterId,
                                               ParameterDetailDTO updateDTO) {
        // 1. Проверяем существование параметра
        if (!parameterId.equals(updateDTO.getId())) {
            throw new IllegalArgumentException("ID in path and body must match");
        }

        Parameter existingParameter = parameterRepository.findParameterByIdAndDeviceTypeId(parameterId, deviceTypeId)
                .orElseThrow(() -> new EntityNotFoundException("Parameter not found with id: " + parameterId +
                                                               " for device type: " + deviceTypeId));

        // 2. Обновляем только разрешенные поля
        existingParameter.setName(updateDTO.getName());
        existingParameter.setAddress(updateDTO.getAddress());
        existingParameter.setMetric(updateDTO.getMetric());
        existingParameter.setAdditive(updateDTO.getAdditive());
        existingParameter.setCoefficient(updateDTO.getCoefficient());
        existingParameter.setDescription(updateDTO.getDescription());
        existingParameter.setDataType(updateDTO.getDataType());

        Parameter updatedParameter = parameterRepository.save(existingParameter);

        // 3. Получаем все устройства этого типа
        List<Device> devices = deviceRepository.findDeviceByDeviceTypeId(deviceTypeId);

        // 4. Получаем существующие переопределения
        List<DeviceParameterOverride> existingOverrides = deviceParameterOverrideRepository
                .findByParameterId(parameterId);

        // 5. Создаем или обновляем переопределения
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

        // 6. Сохраняем обновленные переопределения
        deviceParameterOverrideRepository.saveAll(updatedOverrides);

        // 7. Возвращаем DTO
        ParameterDetailDTO result = baseDTOConverter.toDTO(updatedParameter, ParameterDetailDTO.class);
        result.setActiveDevicesId(updateDTO.getActiveDevicesId());

//        return baseDTOConverter.toDTO(savedParameter, ParameterDetailDTO.class);
        return result;
    }

    // Удаление параметра
    @Transactional
    public void deleteParameter(Long deviceTypeId, Long parameterId) {
        if (!parameterRepository.existsParameterByIdAndDeviceTypeId(parameterId, deviceTypeId)) {
            throw new EntityNotFoundException("Parameter not found with id: " + parameterId +
                                              " for device type: " + deviceTypeId);
        }
        parameterRepository.deleteById(parameterId);
    }

}
