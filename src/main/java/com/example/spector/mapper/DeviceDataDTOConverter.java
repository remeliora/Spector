package com.example.spector.mapper;

import com.example.spector.domain.DeviceData;
import com.example.spector.domain.dto.devicedata.DeviceDataDTO;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DeviceDataDTOConverter implements ConverterDTO<DeviceData, DeviceDataDTO> {
    private final ModelMapper modelMapper;

    @Override
    public DeviceDataDTO convertToDTO(DeviceData deviceData) {
        return modelMapper.map(deviceData, DeviceDataDTO.class);
    }

    @Override
    public DeviceData convertToEntity(DeviceDataDTO deviceDataDTO) {
        return modelMapper.map(deviceDataDTO, DeviceData.class);
    }
}
