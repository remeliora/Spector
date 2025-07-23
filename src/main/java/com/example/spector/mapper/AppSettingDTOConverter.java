package com.example.spector.mapper;

import com.example.spector.domain.AppSetting;
import com.example.spector.domain.dto.appsetting.AppSettingDTO;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AppSettingDTOConverter implements ConverterDTO<AppSetting, AppSettingDTO> {
    private final ModelMapper modelMapper;

    @Override
    public AppSettingDTO convertToDTO(AppSetting appSetting) {
        return modelMapper.map(appSetting, AppSettingDTO.class);
    }

    @Override
    public AppSetting convertToEntity(AppSettingDTO appSettingDTO) {
        return modelMapper.map(appSettingDTO, AppSetting.class);
    }
}
