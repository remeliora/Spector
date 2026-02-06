package com.example.spector.mapper;

import com.example.spector.domain.devicetype.DeviceType;
import com.example.spector.domain.devicetype.dto.DeviceTypeCreateDtoV1;
import com.example.spector.domain.devicetype.dto.DeviceTypeDto;
import com.example.spector.domain.devicetype.dto.DeviceTypeMinimalDto;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface DeviceTypeMapper {
    DeviceType toEntity(DeviceTypeDto deviceTypeDto);

    DeviceTypeDto toDeviceTypeDto(DeviceType deviceType);

    DeviceType updateWithNull(DeviceTypeDto deviceTypeDto, @MappingTarget DeviceType deviceType);

    DeviceTypeMinimalDto toDeviceTypeMinimalDto(DeviceType deviceType);

    DeviceType toEntity(DeviceTypeCreateDtoV1 deviceTypeCreateDtoV1);

    DeviceTypeCreateDtoV1 toDeviceTypeCreateDtoV1(DeviceType deviceType);

}