package com.example.spector.repositories;

import com.example.spector.domain.Device;
import com.example.spector.domain.Parameter;
import com.example.spector.domain.Threshold;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ThresholdRepository extends JpaRepository<Threshold, Long> {
    List<Threshold> findThresholdByDevice(Device device);
    List<Threshold> findThresholdByParameter(Parameter parameter);
}
