package com.example.spector.controller.rest;

import com.example.spector.domain.dto.devicetype.rest.DeviceTypeBaseDTO;
import com.example.spector.domain.dto.devicetype.rest.DeviceTypeCreateDTO;
import com.example.spector.domain.dto.devicetype.rest.DeviceTypeDetailDTO;
import com.example.spector.service.devicetype.AggregationDeviceTypeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/main/device-types")
@RequiredArgsConstructor
public class DeviceTypeController {
    private final AggregationDeviceTypeService aggregationDeviceTypeService;

    /**
     * GET /api/main/device-types
     */
    // Получение списка с фильтрацией
    @GetMapping
    public List<DeviceTypeBaseDTO> getDeviceTypes(
            @RequestParam(name = "className", required = false) String className
    ) {
        return aggregationDeviceTypeService.getDeviceTypes(Optional.ofNullable(className));
    }

    /**
     * GET /api/main/device-types/{deviceTypeId}
     */
    // Получение деталей типа устройства
    @GetMapping("/{deviceTypeId}")
    public DeviceTypeDetailDTO getDeviceTypeDetail(@PathVariable Long deviceTypeId) {
        return aggregationDeviceTypeService.getDeviceTypeDetail(deviceTypeId);
    }

    // Создание типа устройства
    @PostMapping
    public ResponseEntity<DeviceTypeDetailDTO> createDeviceType(
            @RequestBody @Valid DeviceTypeCreateDTO createDTO) {
        DeviceTypeDetailDTO created = aggregationDeviceTypeService.createDeviceType(createDTO);

        return ResponseEntity
                .created(URI.create("/api/main/device-types/" + created.getId()))
                .body(created);
    }

    // Обновление типа устройства
    @PutMapping("/{deviceTypeId}")
    public DeviceTypeDetailDTO updateDeviceType(
            @PathVariable Long deviceTypeId,
            @RequestBody @Valid DeviceTypeDetailDTO updateDTO) {
        return aggregationDeviceTypeService.updateDeviceType(deviceTypeId, updateDTO);
    }

    // Удаление типа устройства
    @DeleteMapping("/{deviceTypeId}")
    public ResponseEntity<Void> deleteDeviceType(@PathVariable Long deviceTypeId) {
        aggregationDeviceTypeService.deleteDeviceType(deviceTypeId);

        return ResponseEntity.noContent().build();
    }
}
