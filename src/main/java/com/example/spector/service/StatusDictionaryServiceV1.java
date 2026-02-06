package com.example.spector.service;

import com.example.spector.domain.parameter.Parameter;
import com.example.spector.domain.statusdictionary.StatusDictionary;
import com.example.spector.domain.statusdictionary.dto.StatusDictionaryCreateDtoV1;
import com.example.spector.domain.statusdictionary.dto.StatusDictionaryDto;
import com.example.spector.domain.statusdictionary.dto.StatusDictionarySummaryDto;
import com.example.spector.mapper.StatusDictionaryMapper;
import com.example.spector.repositories.ParameterRepository;
import com.example.spector.repositories.StatusDictionaryRepository;
import com.example.spector.service.filter.StatusDictionaryFilter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class StatusDictionaryServiceV1 {

    private final StatusDictionaryMapper statusDictionaryMapper;
    private final StatusDictionaryRepository statusDictionaryRepository;
    private final ParameterRepository parameterRepository;
    private final ObjectMapper objectMapper;

    @Transactional(readOnly = true)
    public StatusDictionaryDto getOne(Long id) {
        Optional<StatusDictionary> statusDictionaryOptional = statusDictionaryRepository.findById(id);
        return statusDictionaryMapper.toStatusDictionaryDto(statusDictionaryOptional.orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Entity with id `%s` not found".formatted(id))));
    }

    @Transactional(readOnly = true)
    public List<StatusDictionarySummaryDto> getAll(StatusDictionaryFilter filter) {
        Specification<StatusDictionary> spec = filter.toSpecification();
        List<StatusDictionary> statusDictionaries = statusDictionaryRepository.findAll(spec);
        return statusDictionaries.stream()
                .map(statusDictionaryMapper::toStatusDictionarySummaryDto)
                .toList();
    }

    @Transactional
    public StatusDictionaryDto create(StatusDictionaryCreateDtoV1 dto) {
        if (statusDictionaryRepository.findStatusDictionaryByName(dto.getName()).isPresent()) {
            throw new IllegalArgumentException("Status Dictionary already exists with name: " + dto.getName());
        }
        StatusDictionary statusDictionary = statusDictionaryMapper.toEntity(dto);
        StatusDictionary resultStatusDictionary = statusDictionaryRepository.save(statusDictionary);
        return statusDictionaryMapper.toStatusDictionaryDto(resultStatusDictionary);
    }

    @Transactional
    public StatusDictionaryDto update(Long id, StatusDictionaryDto dto) {
        StatusDictionary statusDictionary = statusDictionaryRepository.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Entity with id `%s` not found".formatted(id)));
        statusDictionaryMapper.updateWithNull(dto, statusDictionary);
        StatusDictionary resultStatusDictionary = statusDictionaryRepository.save(statusDictionary);
        return statusDictionaryMapper.toStatusDictionaryDto(resultStatusDictionary);
    }

    @Transactional
    public StatusDictionaryDto patch(Long id, JsonNode patchNode) throws IOException {
        StatusDictionary statusDictionary = statusDictionaryRepository.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Entity with id `%s` not found".formatted(id)));

        StatusDictionaryDto statusDictionaryDto = statusDictionaryMapper.toStatusDictionaryDto(statusDictionary);
        objectMapper.readerForUpdating(statusDictionaryDto).readValue(patchNode);
        statusDictionaryMapper.updateWithNull(statusDictionaryDto, statusDictionary);

        StatusDictionary resultStatusDictionary = statusDictionaryRepository.save(statusDictionary);
        return statusDictionaryMapper.toStatusDictionaryDto(resultStatusDictionary);
    }

    @Transactional
    public void delete(Long id) {
        StatusDictionary statusDictionary = statusDictionaryRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Entity with id `%s` not found".formatted(id)));
        if (statusDictionary != null) {
            List<Parameter> parametersUsingDict = parameterRepository.findByStatusDictionaryId(statusDictionary.getId());
            parametersUsingDict.forEach(param -> param.setStatusDictionary(null));
            parameterRepository.saveAll(parametersUsingDict);

            statusDictionaryRepository.delete(statusDictionary);
        }
    }
}
