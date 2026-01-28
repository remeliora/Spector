package com.example.spector.mapper;

import com.example.spector.domain.setting.AppSetting;
import com.example.spector.domain.setting.dto.AppSettingDto;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface AppSettingMapper {
    AppSetting toEntity(AppSettingDto appSettingRestDto);

    AppSettingDto toAppSettingDto(AppSetting appSetting);
}