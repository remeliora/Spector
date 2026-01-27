package com.example.spector.controller;

import com.example.spector.domain.device.dto.DeviceCreateDTO;
import com.example.spector.domain.device.dto.DeviceDetailDTO;
import com.example.spector.domain.device.dto.DeviceUpdateDTO;
import com.example.spector.domain.devicedata.dto.rest.DeviceDataBaseDTO;
import com.example.spector.domain.devicedata.dto.rest.DeviceDataDetailDTO;
import com.example.spector.domain.devicetype.dto.DeviceTypeShortDTO;
import com.example.spector.modules.event.EventDispatcher;
import com.example.spector.service.DeviceDataService;
import com.example.spector.service.DeviceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/main/devices")
@RequiredArgsConstructor
public class DeviceMonitoringController {
    private final DeviceDataService deviceDataService;
    private final DeviceService deviceService;
    private final ClientIpExtractor clientIpExtractor;
    private final EventDispatcher eventDispatcher;

    /**
     * GET /api/v1/main/devices/monitoring
     */
    // Получение списка с фильтрацией
    @GetMapping("/monitoring")
    public List<DeviceDataBaseDTO> getDevices(
            @RequestParam(name = "location", required = false) String location) {
        return deviceDataService.getDeviceDataSummary(Optional.ofNullable(location));
    }

    @GetMapping("/unique-locations")
    public List<String> getUniqueLocations() {
        return deviceService.getUniqueLocations();
    }

    /**
     * GET /api/v1/main/devices/monitoring/{deviceId}
     */
    // Получение деталей устройства с данными в реальном времени
    @GetMapping("/monitoring/{deviceId}")
    public DeviceDataDetailDTO getDeviceDataDetail(@PathVariable("deviceId") Long id) {
        return deviceDataService.getDeviceDataDetails(id);
    }

    /**
     * GET /api/v1/main/devices/available-device-types
     */
    // Получение списка всех типов устройств для устройства
    @GetMapping("/available-device-types")
    public List<DeviceTypeShortDTO> getAvailableDeviceTypes() {
        return deviceService.getAvailableDeviceTypes();
    }

    /**
     * PUT /api/v1/main/devices/{deviceId}/enable
     */
    // Включение устройства
    @PutMapping("/{deviceId}/enable")
    public ResponseEntity<Void> enableDevice(
            @PathVariable("deviceId") Long id, HttpServletRequest request) {
        String clientIp = clientIpExtractor.extract(request);
        deviceService.setEnable(id, true, clientIp, eventDispatcher);

        return ResponseEntity.noContent().build();
    }

    /**
     * PUT /api/v1/main/devices/{deviceId}/disable
     */
    // Выключение устройства
    @PutMapping("/{deviceId}/disable")
    public ResponseEntity<Void> disableDevice(
            @PathVariable("deviceId") Long id, HttpServletRequest request) {
        String clientIp = clientIpExtractor.extract(request);
        deviceService.setEnable(id, false, clientIp, eventDispatcher);

        return ResponseEntity.noContent().build();
    }

    /**
     * GET /api/v1/main/devices/{deviceId}
     */
    // Получение деталей устройства
    @GetMapping("/{deviceId}")
    public DeviceDetailDTO getDeviceDetail(
            @PathVariable Long deviceId) {
        return deviceService.getDeviceDetail(deviceId);
    }

    // Создание устройства
    @PostMapping
    public ResponseEntity<DeviceDetailDTO> createDevice(
            @RequestBody DeviceCreateDTO createDTO, HttpServletRequest request) {
        String clientIp = clientIpExtractor.extract(request);
        DeviceDetailDTO created = deviceService.createDevice(createDTO, clientIp, eventDispatcher);

        return ResponseEntity
                .created(URI.create("/api/v1/main/devices/" + created.getId()))
                .body(created);
    }

    // Обновление устройства
    @PutMapping("/{deviceId}")
    public DeviceDetailDTO updateDevice(
            @PathVariable Long deviceId,
            @RequestBody DeviceUpdateDTO updateDTO, HttpServletRequest request) {
        String clientIp = clientIpExtractor.extract(request);

        return deviceService.updateDevice(deviceId, updateDTO, clientIp, eventDispatcher);
    }

    // Удаление устройства
    @DeleteMapping("/{deviceId}")
    public ResponseEntity<Void> deleteDevice(
            @PathVariable Long deviceId, HttpServletRequest request) {
        String clientIp = clientIpExtractor.extract(request);
        deviceService.deleteDevice(deviceId, clientIp, eventDispatcher);

        return ResponseEntity.noContent().build();
    }
}
