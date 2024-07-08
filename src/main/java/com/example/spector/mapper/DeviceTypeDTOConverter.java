package com.example.spector.mapper;

import com.example.spector.domain.DeviceType;
import com.example.spector.domain.dto.DeviceTypeDTO;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DeviceTypeDTOConverter implements ConverterDTO<DeviceType, DeviceTypeDTO> {
    private final ModelMapper modelMapper;
    @Override
    public DeviceTypeDTO convertToDTO(DeviceType deviceType) {
        return modelMapper.map(deviceType, DeviceTypeDTO.class);
    }

    @Override
    public DeviceType convertToEntity(DeviceTypeDTO deviceTypeDTO) {
        return modelMapper.map(deviceTypeDTO, DeviceType.class);
    }
}
