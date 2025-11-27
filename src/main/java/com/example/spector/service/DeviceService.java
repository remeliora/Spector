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
import com.example.spector.mapper.BaseDTOConverter;
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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
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
    public void setEnable(Long deviceId, boolean enabled) {
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new EntityNotFoundException("Устройство не найдено: " + deviceId));
        boolean wasEnabled = device.getIsEnable();
        device.setIsEnable(enabled);
        deviceRepository.save(device);

        if (enabled) {
            pollingManager.startPolling(deviceId); // Запускаем опрос для включенного устройства
        } else {
            pollingManager.stopPolling(deviceId); // Останавливаем опрос для выключенного устройства
            webSocketNotificationService.notifyDeviceStatus(deviceId, "DISABLED", false);
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

        // Запоминаем старое имя устройства
        String oldDeviceName = existingDevice.getName();

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

            if (existingDevice.getThresholds() != null && !existingDevice.getThresholds().isEmpty()) {
                // Удаляем все пороги, связанные с устройством
                // Предполагается, что в репозитории ThresholdRepository есть метод deleteByDeviceId
                // Если нет, можно использовать deviceRepository.deleteAll(existingDevice.getThresholds())
                // или обнулить связи вручную и сохранить
                existingDevice.getThresholds().clear();
            }
        }

        Device updatedDevice = deviceRepository.save(existingDevice);

        if (!oldDeviceName.equals(updateDTO.getName())) {
            renameMongoCollection(oldDeviceName, updateDTO.getName());
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

    // Вспомогательный метод для формирования имени коллекции по имени устройства
    private String getCollectionNameForDeviceByName(String deviceName) {
        // Очищаем имя устройства от недопустимых символов для имени коллекции
        return deviceName.replaceAll("[^a-zA-Z0-9_-]", "_");
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
