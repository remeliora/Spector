package com.example.spector.mapper;

import com.example.spector.domain.Parameter;
import com.example.spector.domain.dto.ParameterDTO;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;

@Component
@RequiredArgsConstructor
public class ParameterDTOConverter implements ConverterDTO<Parameter, ParameterDTO> {
    private final ModelMapper modelMapper;
    @Override
    public ParameterDTO convertToDTO(Parameter parameter) {
        return modelMapper.map(parameter, ParameterDTO.class);
    }

    @Override
    public Parameter convertToEntity(ParameterDTO parameterDTO) {
        return modelMapper.map(parameterDTO, Parameter.class);
    }
}
