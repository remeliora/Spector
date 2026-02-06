package com.example.spector.mapper;

import com.example.spector.domain.statusdictionary.StatusDictionary;
import com.example.spector.domain.statusdictionary.dto.StatusDictionaryCreateDtoV1;
import com.example.spector.domain.statusdictionary.dto.StatusDictionaryDto;
import com.example.spector.domain.statusdictionary.dto.StatusDictionarySummaryDto;
import org.mapstruct.*;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface StatusDictionaryMapper {
    StatusDictionaryDto toStatusDictionaryDto(StatusDictionary statusDictionary);

    @Mapping(target = "count", expression = "java(statusDictionary.getEnumValues() != null ? statusDictionary.getEnumValues().size() : 0)")
    StatusDictionarySummaryDto toStatusDictionarySummaryDto(StatusDictionary statusDictionary);

    StatusDictionary toEntity(StatusDictionaryDto statusDictionaryDto);

    StatusDictionary updateWithNull(StatusDictionaryDto statusDictionaryDto, @MappingTarget StatusDictionary statusDictionary);

    StatusDictionary toEntity(StatusDictionaryCreateDtoV1 statusDictionaryCreateDtoV1);

    StatusDictionaryCreateDtoV1 toStatusDictionaryCreateDtoV1(StatusDictionary statusDictionary);
}