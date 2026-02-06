package com.example.spector.repositories;

import com.example.spector.domain.devicetype.DeviceType;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeviceTypeRepository extends JpaRepository<DeviceType, Long>, JpaSpecificationExecutor<DeviceType> {
    @NonNull
    List<DeviceType> findAll();

    List<DeviceType> findDeviceTypeByClassName(String className);

    Optional<DeviceType> findByName(String name);

    Boolean existsByName(String name);
}
