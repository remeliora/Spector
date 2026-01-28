package com.example.spector.mapper;

import com.example.spector.domain.parameter.Parameter;
import com.example.spector.domain.parameter.dto.ParameterDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface ParameterMapper {
    @Mapping(source = "statusDictionaryId", target = "statusDictionary.id")
    Parameter toEntity(ParameterDto parameterDto);

    @Mapping(source = "statusDictionary.id", target = "statusDictionaryId")
    ParameterDto toParameterDto(Parameter parameter);

}