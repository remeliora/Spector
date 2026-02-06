package com.example.spector.service;

import com.example.spector.domain.parameter.dto.ParameterCreateDtoV1;
import com.example.spector.domain.parameter.dto.ParameterMinimalDto;
import com.example.spector.domain.parameter.Parameter;
import com.example.spector.domain.parameter.dto.ParameterDto;
import com.example.spector.mapper.ParameterMapper;
import com.example.spector.repositories.ParameterRepository;
import com.example.spector.service.filter.ParameterFilter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class ParameterServiceV1 {

    private final ParameterMapper parameterMapper;

    private final ParameterRepository parameterRepository;

    private final ObjectMapper objectMapper;

    public ParameterDto getOne(Long id) {
        Optional<Parameter> parameterOptional = parameterRepository.findById(id);
        return parameterMapper.toParameterDto(parameterOptional.orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Entity with id `%s` not found".formatted(id))));
    }

    public List<ParameterMinimalDto> getAll(ParameterFilter filter) {
        Specification<Parameter> spec = filter.toSpecification();
        List<Parameter> parameters = parameterRepository.findAll(spec);
        return parameters.stream()
                .map(parameterMapper::toParameterMinimalDto)
                .toList();
    }

    public ParameterCreateDtoV1 create(ParameterCreateDtoV1 dto) {
        Parameter parameter = parameterMapper.toEntity(dto);
        Parameter resultParameter = parameterRepository.save(parameter);
        return parameterMapper.toParameterCreateDtoV1(resultParameter);
    }

    public ParameterDto update(Long id, ParameterDto dto) {
        Parameter parameter = parameterRepository.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Entity with id `%s` not found".formatted(id)));
        parameterMapper.updateWithNull(dto, parameter);
        Parameter resultParameter = parameterRepository.save(parameter);
        return parameterMapper.toParameterDto(resultParameter);
    }

    public ParameterDto patch(Long id, JsonNode patchNode) throws IOException {
        Parameter parameter = parameterRepository.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Entity with id `%s` not found".formatted(id)));

        ParameterDto parameterDto = parameterMapper.toParameterDto(parameter);
        objectMapper.readerForUpdating(parameterDto).readValue(patchNode);
        parameterMapper.updateWithNull(parameterDto, parameter);

        Parameter resultParameter = parameterRepository.save(parameter);
        return parameterMapper.toParameterDto(resultParameter);
    }

    public ParameterDto delete(Long id) {
        Parameter parameter = parameterRepository.findById(id).orElse(null);
        if (parameter != null) {
            parameterRepository.delete(parameter);
        }
        return parameterMapper.toParameterDto(parameter);
    }
}
