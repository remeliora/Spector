package com.example.spector.mapper;

import com.example.spector.domain.Device;
import com.example.spector.domain.dto.device.DeviceDTO;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DeviceDTOConverter implements ConverterDTO<Device, DeviceDTO> {
    private final ModelMapper modelMapper;

    @Override
    public DeviceDTO convertToDTO(Device device) {
        return modelMapper.map(device, DeviceDTO.class);
    }

    @Override
    public Device convertToEntity(DeviceDTO deviceDTO) {
        return modelMapper.map(deviceDTO, Device.class);
    }
}
