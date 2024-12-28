package com.example.spector.service;

import com.example.spector.domain.Device;
import com.example.spector.repositories.DeviceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DeviceService {
    private final DeviceRepository deviceRepository;

    public Device createDevice(Device device) {
        return deviceRepository.save(device);
    }

    public List<Device> getAllDevices() {
        return deviceRepository.findAll();
    }

    public Device getDeviceById(Long deviceId) {
        return deviceRepository.findById(deviceId).orElse(null);
    }

    public Device updateDevice(Long deviceId, Device device) {
        Device updatedDevice = deviceRepository.findById(deviceId).orElse(null);
        if (updatedDevice != null) {
            updatedDevice.setName(device.getName());
            updatedDevice.setIpAddress(device.getIpAddress());
            updatedDevice.setDescription(device.getDescription());
            updatedDevice.setPeriod(device.getPeriod());
            updatedDevice.setAlarmType(device.getAlarmType());
            updatedDevice.setIsEnable(device.getIsEnable());

            return deviceRepository.save(updatedDevice);
        } else {
            return null;
        }
    }

    public void deleteDevice(Long deviceId) {
        deviceRepository.deleteById(deviceId);
    }


}
