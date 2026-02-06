package com.example.spector.mapper;

import com.example.spector.domain.parameter.dto.ParameterCreateDtoV1;
import com.example.spector.domain.override.DeviceParameterOverride;
import com.example.spector.domain.parameter.dto.ParameterMinimalDto;
import com.example.spector.domain.parameter.Parameter;
import com.example.spector.domain.parameter.dto.ParameterDto;
import org.mapstruct.*;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface ParameterMapper {
    @Mapping(source = "statusDictionaryId", target = "statusDictionary.id")
    Parameter toEntity(ParameterDto parameterDto);

    @Mapping(source = "statusDictionary.id", target = "statusDictionaryId")
    ParameterDto toParameterDto(Parameter parameter);

    ParameterMinimalDto toParameterMinimalDto(Parameter parameter);

    @Mapping(source = "statusDictionaryId", target = "statusDictionary.id")
    Parameter toEntity(ParameterCreateDtoV1 parameterCreateDtoV1);

    @Mapping(target = "deviceParameterOverrideIds", expression = "java(deviceParameterOverridesToDeviceParameterOverrideIds(parameter.getDeviceParameterOverrides()))")
    @Mapping(source = "statusDictionary.id", target = "statusDictionaryId")
    ParameterCreateDtoV1 toParameterCreateDtoV1(Parameter parameter);

    default List<Long> deviceParameterOverridesToDeviceParameterOverrideIds(List<DeviceParameterOverride> deviceParameterOverrides) {
        return deviceParameterOverrides.stream().map(DeviceParameterOverride::getId).toList();
    }

    @InheritConfiguration(name = "toEntity")
    Parameter updateWithNull(ParameterDto parameterDto, @MappingTarget Parameter parameter);
}