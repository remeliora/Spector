package com.example.spector.service.device;

import com.example.spector.domain.Device;
import com.example.spector.domain.DeviceType;
import com.example.spector.domain.dto.device.rest.DeviceCreateDTO;
import com.example.spector.domain.dto.device.rest.DeviceDetailDTO;
import com.example.spector.mapper.BaseDTOConverter;
import com.example.spector.repositories.DeviceRepository;
import com.example.spector.repositories.DeviceTypeRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AggregationDeviceService {
    private final DeviceRepository deviceRepository;
    private final DeviceTypeRepository deviceTypeRepository;
    private final BaseDTOConverter baseDTOConverter;

    // Включить / выключить устройство
    public void setEnable(Long deviceId, boolean enabled) {
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new EntityNotFoundException("Устройство не найдено: " + deviceId));
        device.setIsEnable(enabled);
        deviceRepository.save(device);
    }

    public DeviceDetailDTO getDeviceDetail(Long id) {
        return deviceRepository.findById(id)
                .map(device -> baseDTOConverter.toDTO(device, DeviceDetailDTO.class))
                .orElseThrow(() -> new EntityNotFoundException("Device not found"));
    }

    //================
    //      CRUD
    //================

    // Создание нового устройства
    @Transactional
    public DeviceDetailDTO createDevice(DeviceCreateDTO createDTO) {
        Device newDevice = baseDTOConverter.toEntity(createDTO, Device.class);
        Device savedDevice = deviceRepository.save(newDevice);

        return baseDTOConverter.toDTO(savedDevice, DeviceDetailDTO.class);
    }

    // Обновление устройства
    @Transactional
    public DeviceDetailDTO updateDevice(Long id, DeviceDetailDTO updateDTO) {
        if (!id.equals(updateDTO.getId())) {
            throw new IllegalArgumentException("ID in path and body must match");
        }

        Device existingDevice = deviceRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Device not found"));

        // Обновляем только разрешенные поля
        existingDevice.setName(updateDTO.getName());
        existingDevice.setIpAddress(updateDTO.getIpAddress());
        // Обновление типа устройства (если изменился)
        if (!existingDevice.getDeviceType().getId().equals(updateDTO.getDeviceTypeId())) {
            DeviceType newType = deviceTypeRepository.findById(updateDTO.getDeviceTypeId())
                    .orElseThrow(() -> new EntityNotFoundException("Тип устройства не найден"));
            existingDevice.setDeviceType(newType);
        }
        existingDevice.setDescription(updateDTO.getDescription());
        existingDevice.setLocation(updateDTO.getLocation());
        existingDevice.setPeriod(updateDTO.getPeriod());
        existingDevice.setAlarmType(updateDTO.getAlarmType());
        existingDevice.setIsEnable(updateDTO.getIsEnable());

        Device updatedDevice = deviceRepository.save(existingDevice);

        return baseDTOConverter.toDTO(updatedDevice, DeviceDetailDTO.class);
    }

    @Transactional
    public void deleteDevice(Long id) {
        if (!deviceRepository.existsById(id)) {
            throw new IllegalArgumentException("Device not found");
        }
        deviceRepository.deleteById(id);
    }
}
