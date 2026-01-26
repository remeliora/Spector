package com.example.spector.service;

import com.example.spector.domain.Device;
import com.example.spector.domain.DeviceParameterOverride;
import com.example.spector.domain.DeviceType;
import com.example.spector.domain.Parameter;
import com.example.spector.domain.dto.device.rest.DeviceByDeviceTypeDTO;
import com.example.spector.domain.dto.device.rest.DeviceCreateDTO;
import com.example.spector.domain.dto.device.rest.DeviceDetailDTO;
import com.example.spector.domain.dto.device.rest.DeviceUpdateDTO;
import com.example.spector.domain.dto.devicetype.rest.DeviceTypeShortDTO;
import com.example.spector.domain.enums.AlarmType;
import com.example.spector.domain.enums.EventType;
import com.example.spector.domain.enums.MessageType;
import com.example.spector.mapper.BaseDTOConverter;
import com.example.spector.modules.event.EventDispatcher;
import com.example.spector.modules.event.EventMessage;
import com.example.spector.modules.polling.PollingManager;
import com.example.spector.repositories.DeviceParameterOverrideRepository;
import com.example.spector.repositories.DeviceRepository;
import com.example.spector.repositories.DeviceTypeRepository;
import com.example.spector.repositories.ParameterRepository;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DeviceService {
    private final MongoClient mongoClient;
    private final MongoTemplate mongoTemplate;
    private final PollingManager pollingManager;
    private final DeviceRepository deviceRepository;
    private final ParameterRepository parameterRepository;
    private final DeviceTypeRepository deviceTypeRepository;
    private final WebSocketNotificationService webSocketNotificationService;
    private final DeviceParameterOverrideRepository deviceParameterOverrideRepository;
    private final BaseDTOConverter baseDTOConverter;

    // Включить / выключить устройство
    public void setEnable(Long deviceId, boolean enabled, String clientIp, EventDispatcher eventDispatcher) {
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new EntityNotFoundException("Устройство не найдено: " + deviceId));
        boolean wasEnabled = device.getIsEnable();
        device.setIsEnable(enabled);
        deviceRepository.save(device);

        if (enabled) {
            pollingManager.startPolling(deviceId); // Запускаем опрос для включенного устройства
            String message = String.format("IP %s: User enabled device: name='%s', IP='%s'",
                    clientIp, device.getName(), device.getIpAddress());
            EventMessage event = EventMessage.log(EventType.REQUEST, MessageType.INFO, message);
            eventDispatcher.dispatch(event);
        } else {
            pollingManager.stopPolling(deviceId); // Останавливаем опрос для выключенного устройства
            webSocketNotificationService.notifyDeviceStatus(deviceId, "DISABLED", false);
            String message = String.format("IP %s: User disabled device: name='%s', IP='%s'",
                    clientIp, device.getName(), device.getIpAddress());
            EventMessage event = EventMessage.log(EventType.REQUEST, MessageType.INFO, message);
            eventDispatcher.dispatch(event);
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
    public DeviceDetailDTO createDevice(DeviceCreateDTO createDTO, String clientIp, EventDispatcher eventDispatcher) {
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

        String message = String.format("IP %s: User created device: name='%s', IP='%s', type='%s', location='%s'",
                clientIp, newDevice.getName(), newDevice.getIpAddress(), newDevice.getDeviceType().getName(),
                newDevice.getLocation());
        EventMessage event = EventMessage.log(EventType.REQUEST, MessageType.INFO, message);
        eventDispatcher.dispatch(event);

        // 6. Возвращаем DTO
        DeviceDetailDTO result = baseDTOConverter.toDTO(savedDevice, DeviceDetailDTO.class);
        result.setActiveParametersId(createDTO.getActiveParametersId());

        return result;
    }

    // Обновление устройства
    @Transactional
    public DeviceDetailDTO updateDevice(Long id, DeviceUpdateDTO updateDTO, String clientIp,
                                        EventDispatcher eventDispatcher) {
        // 1. Проверяем существование устройства
        if (!id.equals(updateDTO.getId())) {
            throw new IllegalArgumentException("ID in path and body must match");
        }

        Device existingDevice = deviceRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Device not found"));

        // Запоминаем старые значения
        String oldDeviceName = existingDevice.getName();
        String oldIpAddress = existingDevice.getIpAddress();
        String oldLocation = existingDevice.getLocation();
        String oldDescription = existingDevice.getDescription();
        DeviceType oldDeviceType = existingDevice.getDeviceType();
        Integer oldPeriod = existingDevice.getPeriod();
        AlarmType oldAlarmType = existingDevice.getAlarmType();
        Boolean oldIsEnable = existingDevice.getIsEnable();

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

            if (existingDevice.getThresholds() != null && !existingDevice.getThresholds().isEmpty()) {
                // Удаляем все пороги, связанные с устройством
                existingDevice.getThresholds().clear();
            }
        }

        Device updatedDevice = deviceRepository.save(existingDevice);

        String changes = "";
        if (!Objects.equals(oldDeviceName, updateDTO.getName())) {
            changes += String.format("name: '%s' -> '%s', ", oldDeviceName, updateDTO.getName());
            // Переименовываем коллекцию MongoDB
            renameMongoCollection(oldDeviceName, updateDTO.getName());
        }
        if (!Objects.equals(oldIpAddress, updateDTO.getIpAddress())) {
            changes += String.format("IP: '%s' -> '%s', ", oldIpAddress, updateDTO.getIpAddress());
        }
        if (!Objects.equals(oldLocation, updateDTO.getLocation())) {
            changes += String.format("location: '%s' -> '%s', ", oldLocation, updateDTO.getLocation());
        }
        if (!Objects.equals(oldDescription, updateDTO.getDescription())) {
            changes += String.format("description: '%s' -> '%s', ", oldDescription, updateDTO.getDescription());
        }
        if (!Objects.equals(oldDeviceType.getId(), newDeviceType.getId())) {
            changes += String.format("type: '%s' -> '%s', ", oldDeviceType.getName(), newDeviceType.getName());
        }
        if (!Objects.equals(oldPeriod, updateDTO.getPeriod())) {
            changes += String.format("period: %d -> %d, ", oldPeriod, updateDTO.getPeriod());
        }
        if (!Objects.equals(oldAlarmType, updateDTO.getAlarmType())) {
            changes += String.format("alarmType: %s -> %s, ", oldAlarmType, updateDTO.getAlarmType());
        }
        if (!Objects.equals(oldIsEnable, updateDTO.getIsEnable())) {
            changes += String.format("isEnabled: %s -> %s, ", oldIsEnable, updateDTO.getIsEnable());
        }

        if (!changes.isEmpty()) {
            changes = changes.substring(0, changes.length() - 2); // Убираем последнюю запятую
            String message = String.format("IP %s: User updated device ID %d: %s",
                    clientIp, id, changes);
            EventMessage event = EventMessage.log(EventType.REQUEST, MessageType.INFO, message);
            eventDispatcher.dispatch(event);
        }

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
    public void deleteDevice(Long id, String clientIp, EventDispatcher eventDispatcher) {
        Device existingDevice = deviceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Device not found"));

        String name = existingDevice.getName();
        String ipAddress = existingDevice.getIpAddress();

        deviceRepository.deleteById(id);

        String message = String.format("IP %s: User deleted device ID %d: name='%s', IP='%s'",
                clientIp, id, name, ipAddress);
        EventMessage event = EventMessage.log(EventType.REQUEST, MessageType.INFO, message);
        eventDispatcher.dispatch(event);
    }

    public Boolean getIsEnable(Long deviceId) {
        return deviceRepository.findById(deviceId)
                .map(Device::getIsEnable)
                .orElse(false); // или null, если устройство не найдено
    }

    private void renameMongoCollection(String oldDeviceName, String newDeviceName) {
        String oldCollectionName = oldDeviceName;
        String newCollectionName = newDeviceName;

        String dbName = mongoTemplate.getDb().getName(); // имя вашей БД

        // Путь к коллекции: база.коллекция
        String oldNs = dbName + "." + oldCollectionName;
        String newNs = dbName + "." + newCollectionName;

        // Выполняем команду в admin базе
        MongoDatabase adminDb = mongoClient.getDatabase("admin");

        Document command = new Document("renameCollection", oldNs)
                .append("to", newNs)
                .append("dropTarget", true);

        try {
            adminDb.runCommand(command);
        } catch (Exception e) {
            throw new RuntimeException("Failed to rename collection via admin command", e);
        }
    }
}
