package com.example.spector.service.devicetype;

import com.example.spector.domain.DeviceType;
import com.example.spector.domain.dto.devicetype.rest.DeviceTypeBaseDTO;
import com.example.spector.domain.dto.devicetype.rest.DeviceTypeCreateDTO;
import com.example.spector.domain.dto.devicetype.rest.DeviceTypeDetailDTO;
import com.example.spector.mapper.BaseDTOConverter;
import com.example.spector.repositories.DeviceTypeRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AggregationDeviceTypeService {
    private final DeviceTypeRepository deviceTypeRepository;
    private final BaseDTOConverter baseDTOConverter;

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
    public DeviceTypeDetailDTO updateDeviceType(Long id, DeviceTypeDetailDTO updateDTO) {
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
}
