package com.example.spector.repositories;

import com.example.spector.domain.DeviceParameterOverride;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeviceParameterOverrideRepository extends JpaRepository<DeviceParameterOverride, Long> {
    List<DeviceParameterOverride> findByParameterId(Long parameterId);

    List<DeviceParameterOverride> findByParameterIdAndIsActiveTrue(Long parameterId);

    List<DeviceParameterOverride> findByDeviceId(Long deviceId);

    List<DeviceParameterOverride> findByDeviceIdAndIsActiveTrue(Long deviceId);
}