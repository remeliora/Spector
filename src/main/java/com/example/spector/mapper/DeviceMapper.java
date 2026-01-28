package com.example.spector.mapper;

import com.example.spector.domain.device.Device;
import com.example.spector.domain.device.DeviceDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface DeviceMapper {
    @Mapping(source = "deviceTypeId", target = "deviceType.id")
    Device toEntity(DeviceDto deviceDto);

    @Mapping(source = "deviceType.id", target = "deviceTypeId")
    DeviceDto toDeviceDto(Device device);
}