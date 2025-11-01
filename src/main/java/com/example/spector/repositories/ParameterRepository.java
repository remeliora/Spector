package com.example.spector.repositories;

import com.example.spector.domain.DeviceType;
import com.example.spector.domain.Parameter;
import com.example.spector.domain.enums.DataType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ParameterRepository extends JpaRepository<Parameter, Long> {
    List<Parameter> findParameterByDeviceType(DeviceType deviceType);

    Optional<Parameter> findParameterByIdAndDeviceTypeId(Long id, Long deviceTypeId);

    boolean existsParameterByIdAndDeviceTypeId(Long id, Long deviceTypeId);

    List<Parameter> findByStatusDictionaryId(Long statusDictionaryId);
}
