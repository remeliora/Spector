package com.example.spector.controller;

import com.example.spector.domain.statusdictionary.dto.StatusDictionaryCreateDtoV1;
import com.example.spector.domain.statusdictionary.dto.StatusDictionaryDto;
import com.example.spector.domain.statusdictionary.dto.StatusDictionarySummaryDto;
import com.example.spector.service.StatusDictionaryServiceV1;
import com.example.spector.service.filter.StatusDictionaryFilter;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v2/main/status-dictionaries")
@RequiredArgsConstructor
public class StatusDictionaryResource {

    private final StatusDictionaryServiceV1 statusDictionaryServiceV1;
//    private final ClientIpExtractor clientIpExtractor;
//    private final EventDispatcher eventDispatcher;

    // Полная информация о словаре по id
    @GetMapping("/{id}")
    public StatusDictionaryDto getOne(@PathVariable Long id) {
        return statusDictionaryServiceV1.getOne(id);
    }

    // Общая информация о всех словарях по id (фильтрация по алфавиту)
    @GetMapping
    public List<StatusDictionarySummaryDto> getAll(@ParameterObject @ModelAttribute StatusDictionaryFilter filter) {
        return statusDictionaryServiceV1.getAll(filter);
    }

    // Создание словаря
    @PostMapping
    public ResponseEntity<StatusDictionaryDto> create(@RequestBody @Valid StatusDictionaryCreateDtoV1 dto) {
        StatusDictionaryDto created = statusDictionaryServiceV1.create(dto);
        return ResponseEntity
                .created(URI.create("/api/v1/main/statusDictionaries/" + created.getId()))
                .body(created);
    }

    // Обновление всех полей словаря
    @PutMapping("/{id}")
    public StatusDictionaryDto update(@PathVariable Long id, @RequestBody @Valid StatusDictionaryDto dto) {
        return statusDictionaryServiceV1.update(id, dto);
    }

    // Частичное обновление полей словаря
    @PatchMapping("/{id}")
    public StatusDictionaryDto patch(@PathVariable Long id, @RequestBody JsonNode patchNode) throws IOException {
        return statusDictionaryServiceV1.patch(id, patchNode);
    }

    // Удаление словаря (удаление связей с параметрами)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        statusDictionaryServiceV1.delete(id);
        return ResponseEntity.noContent().build();
    }
}
