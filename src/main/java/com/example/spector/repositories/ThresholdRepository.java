package com.example.spector.repositories;

import com.example.spector.domain.Device;
import com.example.spector.domain.Threshold;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ThresholdRepository extends JpaRepository<Threshold, Long> {
    List<Threshold> findThresholdByParameterIdAndIsEnableTrue(Long parameterId);

    List<Threshold> findThresholdByDevice(Device device);

    Optional<Threshold> findThresholdByIdAndDeviceId(Long id, Long deviceId);

    boolean existsThresholdByIdAndDeviceId(Long id, Long deviceId);
}
