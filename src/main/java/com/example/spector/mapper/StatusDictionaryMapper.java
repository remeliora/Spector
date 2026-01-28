package com.example.spector.mapper;

import com.example.spector.domain.statusdictionary.StatusDictionary;
import com.example.spector.domain.statusdictionary.dto.StatusDictionaryDto;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface StatusDictionaryMapper {
    StatusDictionary toEntity(StatusDictionaryDto statusDictionaryDto);

    StatusDictionaryDto toStatusDictionaryDto(StatusDictionary statusDictionary);
}