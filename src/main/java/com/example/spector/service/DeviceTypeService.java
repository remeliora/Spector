package com.example.spector.service;

import com.example.spector.domain.DeviceType;
import com.example.spector.repositories.DeviceTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DeviceTypeService {
    @Autowired
    public DeviceTypeRepository deviceTypeRepository;

    public DeviceType createDeviceType(DeviceType deviceType) {
        return deviceTypeRepository.save(deviceType);
    }

    public Iterable<DeviceType> getAllDeviceTypes() {
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
