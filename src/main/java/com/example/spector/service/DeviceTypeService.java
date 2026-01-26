package com.example.spector.service;

import com.example.spector.domain.DeviceType;
import com.example.spector.domain.dto.device.rest.DeviceByDeviceTypeDTO;
import com.example.spector.domain.dto.devicetype.rest.DeviceTypeBaseDTO;
import com.example.spector.domain.dto.devicetype.rest.DeviceTypeCreateDTO;
import com.example.spector.domain.dto.devicetype.rest.DeviceTypeDetailDTO;
import com.example.spector.domain.dto.devicetype.rest.DeviceTypeUpdateDTO;
import com.example.spector.domain.dto.parameter.rest.ParameterByDeviceTypeDTO;
import com.example.spector.domain.enums.EventType;
import com.example.spector.domain.enums.MessageType;
import com.example.spector.mapper.BaseDTOConverter;
import com.example.spector.modules.event.EventDispatcher;
import com.example.spector.modules.event.EventMessage;
import com.example.spector.repositories.DeviceTypeRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
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
    public DeviceTypeDetailDTO createDeviceType(DeviceTypeCreateDTO createDTO,
                                                String clientIp, EventDispatcher eventDispatcher) {
        DeviceType newDeviceType = baseDTOConverter.toEntity(createDTO, DeviceType.class);
        DeviceType savedDeviceType = deviceTypeRepository.save(newDeviceType);

        String message = String.format("IP %s: User created device type: name='%s', class='%s'",
                clientIp, savedDeviceType.getName(), savedDeviceType.getClassName());
        EventMessage event = EventMessage.log(EventType.REQUEST, MessageType.INFO, message);
        eventDispatcher.dispatch(event);

        return baseDTOConverter.toDTO(savedDeviceType, DeviceTypeDetailDTO.class);
    }

    // Обновление типа устройства
    @Transactional
    public DeviceTypeDetailDTO updateDeviceType(Long id, DeviceTypeUpdateDTO updateDTO,
                                                String clientIp, EventDispatcher eventDispatcher) {
        if (!id.equals(updateDTO.getId())) {
            throw new IllegalArgumentException("ID in path and body must match");
        }

        DeviceType existingDeviceType = deviceTypeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Device type not found"));

        String oldName = existingDeviceType.getName();
        String oldClassName = existingDeviceType.getClassName();
        String oldDescription = existingDeviceType.getDescription();

        // Обновляем только разрешенные поля
        existingDeviceType.setName(updateDTO.getName());
        existingDeviceType.setClassName(updateDTO.getClassName());
        existingDeviceType.setDescription(updateDTO.getDescription());

        DeviceType updatedDeviceType = deviceTypeRepository.save(existingDeviceType);

        String changes = "";
        if (!Objects.equals(oldName, updateDTO.getName())) {
            changes += String.format("name: '%s' -> '%s', ", oldName, updateDTO.getName());
        }
        if (!Objects.equals(oldClassName, updateDTO.getClassName())) {
            changes += String.format("class: '%s' -> '%s', ", oldClassName, updateDTO.getClassName());
        }
        if (!Objects.equals(oldDescription, updateDTO.getDescription())) {
            changes += String.format("description: '%s' -> '%s', ", oldDescription, updateDTO.getDescription());
        }

        if (!changes.isEmpty()) {
            changes = changes.substring(0, changes.length() - 2); // Убираем последнюю запятую
            String message = String.format("IP %s: User updated device type ID %d: %s",
                    clientIp, id, changes);
            EventMessage event = EventMessage.log(EventType.REQUEST, MessageType.INFO, message);
            eventDispatcher.dispatch(event);
        }

        return baseDTOConverter.toDTO(updatedDeviceType, DeviceTypeDetailDTO.class);
    }

    // Удаление типа устройства
    @Transactional
    public void deleteDeviceType(Long id, String clientIp, EventDispatcher eventDispatcher) {
        DeviceType existingDeviceType = deviceTypeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Device type not found"));

        String name = existingDeviceType.getName();

        deviceTypeRepository.deleteById(id);

        String message = String.format("IP %s: User deleted device type ID %d: name='%s'",
                clientIp, id, name);
        EventMessage event = EventMessage.log(EventType.REQUEST, MessageType.INFO, message);
        eventDispatcher.dispatch(event);
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
