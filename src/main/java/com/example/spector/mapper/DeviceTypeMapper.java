package com.example.spector.mapper;

import com.example.spector.domain.devicetype.DeviceType;
import com.example.spector.domain.devicetype.dto.DeviceTypeDto;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface DeviceTypeMapper {
    DeviceType toEntity(DeviceTypeDto deviceTypeDto);

    DeviceTypeDto toDeviceTypeDto(DeviceType deviceType);
}