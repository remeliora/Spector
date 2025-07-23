package com.example.spector.repositories;

import com.example.spector.domain.Device;
import com.example.spector.domain.DeviceType;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeviceRepository extends JpaRepository<Device, Long> {
    @NonNull
    List<Device> findAll();
    List<Device> findDeviceByIsEnableTrue();
    List<Device> findDeviceByLocation(String location);
    List<Device> findDeviceByDeviceTypeId(Long deviceTypeId);
}
