package com.example.spector.repositories;

import com.example.spector.domain.DeviceType;
import com.example.spector.domain.Parameter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ParameterRepository extends JpaRepository<Parameter, Long> {
    List<Parameter> findParameterByDeviceType(DeviceType deviceType);
}
