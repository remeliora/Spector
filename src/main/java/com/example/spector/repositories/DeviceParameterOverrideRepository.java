package com.example.spector.repositories;

import com.example.spector.domain.DeviceParameterOverride;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DeviceParameterOverrideRepository extends JpaRepository<DeviceParameterOverride, Long> {
}