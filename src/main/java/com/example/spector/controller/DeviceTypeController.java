package com.example.spector.controller;

import com.example.spector.domain.device.dto.DeviceByDeviceTypeDTO;
import com.example.spector.domain.devicetype.dto.DeviceTypeBaseDTO;
import com.example.spector.domain.devicetype.dto.DeviceTypeCreateDTO;
import com.example.spector.domain.devicetype.dto.DeviceTypeDetailDTO;
import com.example.spector.domain.devicetype.dto.DeviceTypeUpdateDTO;
import com.example.spector.domain.parameter.dto.ParameterByDeviceTypeDTO;
import com.example.spector.modules.event.EventDispatcher;
import com.example.spector.service.DeviceTypeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/main/device-types")
@RequiredArgsConstructor
public class DeviceTypeController {
    private final DeviceTypeService deviceTypeService;
    private final ClientIpExtractor clientIpExtractor;
    private final EventDispatcher eventDispatcher;

    /**
     * GET /api/v1/main/device-types
     */
    // Получение списка с фильтрацией
    @GetMapping
    public List<DeviceTypeBaseDTO> getDeviceTypes(
            @RequestParam(name = "className", required = false) String className) {
        return deviceTypeService.getDeviceTypes(Optional.ofNullable(className));
    }

    /**
     * GET /api/v1/main/device-types/unique-class-names
     */
    @GetMapping("/unique-class-names")
    public List<String> getUniqueClassNames() {
        return deviceTypeService.getUniqueClassNames();
    }

    /**
     * GET /api/v1/main/device-types/{deviceTypeId}
     */
    // Получение деталей типа устройства
    @GetMapping("/{deviceTypeId}")
    public DeviceTypeDetailDTO getDeviceTypeDetail(
            @PathVariable Long deviceTypeId) {
        return deviceTypeService.getDeviceTypeDetail(deviceTypeId);
    }

    /**
     * GET /api/v1/main/device-types/{deviceTypeId}/devices-list
     */
    // Получение списка устройств по типу
    @GetMapping("/{deviceTypeId}/devices-list")
    public List<DeviceByDeviceTypeDTO> getListDevicesByType(
            @PathVariable Long deviceTypeId) {
        return deviceTypeService.getListDevicesByType(deviceTypeId);
    }

    /**
     * GET /api/v1/main/device-types/{deviceTypeId}/parameters-list
     */
    // Получение списка устройств по типу
    @GetMapping("/{deviceTypeId}/parameters-list")
    public List<ParameterByDeviceTypeDTO> getListParametersByType(
            @PathVariable Long deviceTypeId) {
        return deviceTypeService.getListParametersByType(deviceTypeId);
    }

    // Создание типа устройства
    @PostMapping
    public ResponseEntity<DeviceTypeDetailDTO> createDeviceType(
            @RequestBody @Valid DeviceTypeCreateDTO createDTO, HttpServletRequest request) {
        String clientIp = clientIpExtractor.extract(request);
        DeviceTypeDetailDTO created = deviceTypeService.createDeviceType(createDTO, clientIp, eventDispatcher);

        return ResponseEntity
                .created(URI.create("/api/v1/main/device-types/" + created.getId()))
                .body(created);
    }

    // Обновление типа устройства
    @PutMapping("/{deviceTypeId}")
    public DeviceTypeDetailDTO updateDeviceType(
            @PathVariable Long deviceTypeId,
            @RequestBody @Valid DeviceTypeUpdateDTO updateDTO, HttpServletRequest request) {
        String clientIp = clientIpExtractor.extract(request);

        return deviceTypeService.updateDeviceType(deviceTypeId, updateDTO, clientIp, eventDispatcher);
    }

    // Удаление типа устройства
    @DeleteMapping("/{deviceTypeId}")
    public ResponseEntity<Void> deleteDeviceType(
            @PathVariable Long deviceTypeId, HttpServletRequest request) {
        String clientIp = clientIpExtractor.extract(request);
        deviceTypeService.deleteDeviceType(deviceTypeId, clientIp, eventDispatcher);

        return ResponseEntity.noContent().build();
    }
}
