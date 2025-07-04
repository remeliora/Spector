package com.example.spector.service.devicedata;

import com.example.spector.database.dao.DAO;
import com.example.spector.domain.Device;
import com.example.spector.domain.DeviceData;
import com.example.spector.domain.dto.device.DeviceDTO;
import com.example.spector.domain.dto.devicedata.rest.DeviceDataBaseDTO;
import com.example.spector.domain.dto.devicedata.rest.DeviceDataDetailDTO;
import com.example.spector.mapper.BaseDTOConverter;
import com.example.spector.repositories.DeviceRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AggregationDeviceDataService {
    private final DeviceRepository deviceRepository;
    private final BaseDTOConverter baseDTOConverter;

    @Qualifier("jsonDAO")
    private final DAO jsonDAO;

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
        deviceDataBaseDTO.setId(device.getId().toString());
        deviceDataBaseDTO.setDeviceId(device.getId());
        deviceDataBaseDTO.setDeviceName(device.getName());
        deviceDataBaseDTO.setDeviceIp(device.getIpAddress());
        deviceDataBaseDTO.setIsEnable(device.getIsEnable());
        deviceDataBaseDTO.setLocation(device.getLocation());

        if (Boolean.TRUE.equals(device.getIsEnable())) {
            DeviceDTO deviceDTO = baseDTOConverter.toDTO(device, DeviceDTO.class);
            Optional<DeviceData> deviceData = jsonDAO.readData(deviceDTO);
            deviceDataBaseDTO.setStatus(deviceData.map(DeviceData::getStatus).orElse("NOT_TRACKED"));
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

        DeviceDTO deviceDTO = baseDTOConverter.toDTO(device, DeviceDTO.class);
        DeviceData deviceData = jsonDAO.readData(deviceDTO)
                .orElseThrow(() -> new EntityNotFoundException(
                        "JSON‑файл не найден или пуст для " + device.getName()));

        return baseDTOConverter.toDTO(deviceData, DeviceDataDetailDTO.class);
    }
}
