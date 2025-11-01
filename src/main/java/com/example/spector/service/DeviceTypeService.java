package com.example.spector.service;

import com.example.spector.domain.DeviceType;
import com.example.spector.domain.dto.device.rest.DeviceByDeviceTypeDTO;
import com.example.spector.domain.dto.devicetype.rest.DeviceTypeBaseDTO;
import com.example.spector.domain.dto.devicetype.rest.DeviceTypeCreateDTO;
import com.example.spector.domain.dto.devicetype.rest.DeviceTypeDetailDTO;
import com.example.spector.domain.dto.devicetype.rest.DeviceTypeUpdateDTO;
import com.example.spector.domain.dto.parameter.rest.ParameterByDeviceTypeDTO;
import com.example.spector.mapper.BaseDTOConverter;
import com.example.spector.repositories.DeviceTypeRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DeviceTypeService {
    private final BaseDTOConverter baseDTOConverter;
    private final DeviceService deviceService;
    private final ParameterService parameterService;
    private final DeviceTypeRepository deviceTypeRepository;

    // Получение списка с фильтрацией
    public List<DeviceTypeBaseDTO> getDeviceTypes(Optional<String> classFilter) {
        List<DeviceType> deviceTypes = classFilter
                .map(deviceTypeRepository::findDeviceTypeByClassName)
                .orElseGet(deviceTypeRepository::findAll);

        return deviceTypes.stream()
                .map(deviceType -> baseDTOConverter.toDTO(deviceType, DeviceTypeBaseDTO.class))
                .toList();
    }

    // Получение деталей типа устройства
    public DeviceTypeDetailDTO getDeviceTypeDetail(Long id) {
        return deviceTypeRepository.findById(id)
                .map(deviceType -> baseDTOConverter.toDTO(deviceType, DeviceTypeDetailDTO.class))
                .orElseThrow(() -> new EntityNotFoundException("Device type not found"));
    }

    public List<String> getUniqueClassNames() {
        return deviceTypeRepository.findAll()
                .stream()
                .map(DeviceType::getClassName)
                .filter(className -> className != null && !className.isEmpty())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    //================
    //      CRUD
    //================

    // Создание нового типа устройства
    @Transactional
    public DeviceTypeDetailDTO createDeviceType(DeviceTypeCreateDTO createDTO) {
        DeviceType newDeviceType = baseDTOConverter.toEntity(createDTO, DeviceType.class);
        DeviceType savedDeviceType = deviceTypeRepository.save(newDeviceType);

        return baseDTOConverter.toDTO(savedDeviceType, DeviceTypeDetailDTO.class);
    }

    // Обновление типа устройства
    @Transactional
    public DeviceTypeDetailDTO updateDeviceType(Long id, DeviceTypeUpdateDTO updateDTO) {
        if (!id.equals(updateDTO.getId())) {
            throw new IllegalArgumentException("ID in path and body must match");
        }

        DeviceType existingDeviceType = deviceTypeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Device type not found"));

        // Обновляем только разрешенные поля
        existingDeviceType.setName(updateDTO.getName());
        existingDeviceType.setClassName(updateDTO.getClassName());
        existingDeviceType.setDescription(updateDTO.getDescription());

        DeviceType updatedDeviceType = deviceTypeRepository.save(existingDeviceType);

        return baseDTOConverter.toDTO(updatedDeviceType, DeviceTypeDetailDTO.class);
    }

    // Удаление типа устройства
    @Transactional
    public void deleteDeviceType(Long id) {
        if (!deviceTypeRepository.existsById(id)) {
            throw new EntityNotFoundException("Device type not found");
        }
        deviceTypeRepository.deleteById(id);
    }

    public List<DeviceByDeviceTypeDTO> getListDevicesByType(Long deviceTypeId) {
        validateDeviceTypeExists(deviceTypeId);
        return deviceService.getDevicesByType(deviceTypeId);
    }

    public List<ParameterByDeviceTypeDTO> getListParametersByType(Long deviceTypeId) {
        validateDeviceTypeExists(deviceTypeId);
        return parameterService.getParametersByType(deviceTypeId);
    }

    private void validateDeviceTypeExists(Long deviceTypeId) {
        if (!deviceTypeRepository.existsById(deviceTypeId)) {
            throw new EntityNotFoundException("Device type not found with id: " + deviceTypeId);
        }
    }
}
