package com.example.spector.repositories;

import com.example.spector.domain.Device;
import com.example.spector.domain.DeviceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeviceRepository extends JpaRepository<Device, Long> {
    Device findByName(String name);
    Device findByIpAddress(String ipAddress);
    List<Device> findDeviceByDeviceType(DeviceType deviceType);
    List<Device> findDeviceByIsEnableTrue();
    Device save(Device device);
}
