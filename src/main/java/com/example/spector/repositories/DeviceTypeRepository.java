package com.example.spector.repositories;

import com.example.spector.domain.devicetype.DeviceType;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeviceTypeRepository extends JpaRepository<DeviceType, Long> {
    @NonNull
    List<DeviceType> findAll();

    List<DeviceType> findDeviceTypeByClassName(String className);

}
