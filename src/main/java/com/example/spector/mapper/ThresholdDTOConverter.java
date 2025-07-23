package com.example.spector.mapper;

import com.example.spector.domain.Threshold;
import com.example.spector.domain.dto.threshold.ThresholdDTO;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ThresholdDTOConverter implements ConverterDTO<Threshold, ThresholdDTO> {
    private final ModelMapper modelMapper;

    @Override
    public ThresholdDTO convertToDTO(Threshold threshold) {
        return modelMapper.map(threshold, ThresholdDTO.class);
    }

    @Override
    public Threshold convertToEntity(ThresholdDTO thresholdDTO) {
        return modelMapper.map(thresholdDTO, Threshold.class);
    }
}
