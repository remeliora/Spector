package com.example.spector.service;

import com.example.spector.domain.Device;
import com.example.spector.domain.DeviceCurrentData;
import com.example.spector.domain.dto.devicedata.rest.DeviceDataBaseDTO;
import com.example.spector.domain.dto.devicedata.rest.DeviceDataDetailDTO;
import com.example.spector.mapper.BaseDTOConverter;
import com.example.spector.modules.cache.DeviceCurrentDataCache;
import com.example.spector.repositories.DeviceRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DeviceDataService {
    private final DeviceRepository deviceRepository;
    private final BaseDTOConverter baseDTOConverter;
    private final DeviceCurrentDataCache deviceCurrentDataCache;

    public List<DeviceDataBaseDTO> getDeviceDataSummary(Optional<String> locationFilter) {
        List<Device> devices = locationFilter
                .map(deviceRepository::findDeviceByLocation)
                .orElseGet(deviceRepository::findAll);

        List<DeviceDataBaseDTO> result = new ArrayList<>(devices.size());
        for (Device device : devices) {
            result.add(mapToBaseDTO(device));
        }

        return result;
    }

    private DeviceDataBaseDTO mapToBaseDTO(Device device) {
        DeviceDataBaseDTO deviceDataBaseDTO = new DeviceDataBaseDTO();
        deviceDataBaseDTO.setDeviceId(device.getId());
        deviceDataBaseDTO.setDeviceName(device.getName());
        deviceDataBaseDTO.setDeviceIp(device.getIpAddress());
        deviceDataBaseDTO.setIsEnable(device.getIsEnable());
        deviceDataBaseDTO.setLocation(device.getLocation());

        if (Boolean.TRUE.equals(device.getIsEnable())) {
            // --- Читаем из кэша вместо JSON ---
            DeviceCurrentData cachedData = deviceCurrentDataCache.getCurrentData(device.getId());
            if (cachedData != null) {
                deviceDataBaseDTO.setStatus(cachedData.getStatus());
            } else {
                deviceDataBaseDTO.setStatus("NOT_TRACKED"); // или "LOADING", если опрос только начался
            }
        } else {
            deviceDataBaseDTO.setStatus("DISABLED");
        }

        return deviceDataBaseDTO;
    }

    public DeviceDataDetailDTO getDeviceDataDetails(Long deviceId) {
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new EntityNotFoundException("Устройство (" + deviceId + ") не найдено"));

        if (!Boolean.TRUE.equals(device.getIsEnable())) {
            throw new IllegalStateException("Устройство выключено: " + deviceId);
        }

        // --- Читаем из кэша
        DeviceCurrentData cachedData = deviceCurrentDataCache.getCurrentData(deviceId);
        if (cachedData == null) {
            throw new EntityNotFoundException("Данные устройства ещё не получены или кэш пуст для " + device.getName());
        }

        // Преобразуем DeviceCurrentData в DeviceDataDetailDTO
        return baseDTOConverter.toDTO(cachedData, DeviceDataDetailDTO.class);
    }
}
