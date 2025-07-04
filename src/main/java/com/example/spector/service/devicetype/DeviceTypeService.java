package com.example.spector.service.devicetype;

import com.example.spector.domain.DeviceType;
import com.example.spector.repositories.DeviceTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DeviceTypeService {
    public final DeviceTypeRepository deviceTypeRepository;

    public DeviceType createDeviceType(DeviceType deviceType) {
        return deviceTypeRepository.save(deviceType);
    }

    public List<DeviceType> getAllDeviceTypes() {
        return deviceTypeRepository.findAll();
    }

    public DeviceType getDeviceTypeById(Long deviceTypeId) {
        return deviceTypeRepository.findById(deviceTypeId).orElse(null);
    }

    public DeviceType updateDeviceType(Long deviceTypeId, DeviceType deviceType) {
        DeviceType updatedDeviceType = deviceTypeRepository.findById(deviceTypeId).orElse(null);
        if (updatedDeviceType != null) {
            updatedDeviceType.setName(deviceType.getName());
            updatedDeviceType.setDescription(deviceType.getDescription());

            return deviceTypeRepository.save(updatedDeviceType);
        } else {
            return null;
        }
    }

    public void deleteDeviceType(Long deviceTypeId) {
        deviceTypeRepository.deleteById(deviceTypeId);
    }
}
