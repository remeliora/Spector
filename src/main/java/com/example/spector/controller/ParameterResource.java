package com.example.spector.controller;

import com.example.spector.domain.parameter.dto.ParameterCreateDtoV1;
import com.example.spector.domain.parameter.dto.ParameterDto;
import com.example.spector.domain.parameter.dto.ParameterMinimalDto;
import com.example.spector.service.ParameterServiceV1;
import com.example.spector.service.filter.ParameterFilter;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v2/main/device-types/{deviceTypeId}/parameters")
@RequiredArgsConstructor
public class ParameterResource {

    private final ParameterServiceV1 parameterServiceV1;

    @GetMapping("/{id}")
    public ParameterDto getOne(@PathVariable Long id) {
        return parameterServiceV1.getOne(id);
    }

    @GetMapping
    public List<ParameterMinimalDto> getAll(@ParameterObject @ModelAttribute ParameterFilter filter) {
        return parameterServiceV1.getAll(filter);
    }

    @PostMapping
    public ParameterCreateDtoV1 create(@RequestBody @Valid ParameterCreateDtoV1 dto) {
        return parameterServiceV1.create(dto);
    }

    @PutMapping("/{id}")
    public ParameterDto update(@PathVariable Long id, @RequestBody @Valid ParameterDto dto) {
        return parameterServiceV1.update(id, dto);
    }

    @PatchMapping("/{id}")
    public ParameterDto patch(@PathVariable Long id, @RequestBody JsonNode patchNode) throws IOException {
        return parameterServiceV1.patch(id, patchNode);
    }

    @DeleteMapping("/{id}")
    public ParameterDto delete(@PathVariable Long id) {
        return parameterServiceV1.delete(id);
    }
}
