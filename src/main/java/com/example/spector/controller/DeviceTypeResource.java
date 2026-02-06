package com.example.spector.controller;

import com.example.spector.domain.devicetype.dto.DeviceTypeCreateDtoV1;
import com.example.spector.domain.devicetype.dto.DeviceTypeDto;
import com.example.spector.service.DeviceTypeServiceV1;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v2/main/device-types")
@RequiredArgsConstructor
public class DeviceTypeResource {

    private final DeviceTypeServiceV1 deviceTypeServiceV1;

    @GetMapping("/{id}")
    public DeviceTypeDto getOne(@PathVariable Long id) {
        return deviceTypeServiceV1.getOne(id);
    }

    @GetMapping
    public List<DeviceTypeDto> getAll() {
        return deviceTypeServiceV1.getAll();
    }

//    @GetMapping("/{id}/devices")
//    public List<DeviceByDeviceTypeDTO> getDevicesByType(@PathVariable Long id) {
//        return deviceService.getDevicesByType(id);
//    }

//    @GetMapping("/{id}/parameters")
//    public List<ParameterByDeviceTypeDTO> getParametersByType(@PathVariable Long id) {
//        return parameterService.getParametersByType(id);
//    }

    @PostMapping
    public ResponseEntity<DeviceTypeDto> create(@RequestBody @Valid DeviceTypeCreateDtoV1 dto) {
        DeviceTypeDto create = deviceTypeServiceV1.create(dto);
        return ResponseEntity
                .created(URI.create("/api/v2/main/device-types" + create.getId()))
                .body(create);
    }

    @PutMapping("/{id}")
    public DeviceTypeDto update(@PathVariable Long id, @RequestBody @Valid DeviceTypeDto dto) {
        return deviceTypeServiceV1.update(id, dto);
    }

    @PatchMapping("/{id}")
    public DeviceTypeDto patch(@PathVariable Long id, @RequestBody JsonNode patchNode) throws IOException {
        return deviceTypeServiceV1.patch(id, patchNode);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        deviceTypeServiceV1.delete(id);
        return ResponseEntity.noContent().build();
    }
}
