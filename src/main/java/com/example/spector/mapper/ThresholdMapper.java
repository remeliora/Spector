package com.example.spector.mapper;

import com.example.spector.domain.threshold.Threshold;
import com.example.spector.domain.threshold.dto.ThresholdDto;
import org.mapstruct.*;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface ThresholdMapper {
    @Mapping(source = "deviceId", target = "device.id")
    @Mapping(source = "parameterId", target = "parameter.id")
    Threshold toEntity(ThresholdDto thresholdDto);

    @InheritInverseConfiguration(name = "toEntity")
    ThresholdDto toThresholdRestDto(Threshold threshold);
}