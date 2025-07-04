package com.example.spector.controller.rest;

import com.example.spector.domain.dto.device.rest.DeviceCreateDTO;
import com.example.spector.domain.dto.device.rest.DeviceDetailDTO;
import com.example.spector.domain.dto.devicedata.rest.DeviceDataBaseDTO;
import com.example.spector.domain.dto.devicedata.rest.DeviceDataDetailDTO;
import com.example.spector.service.device.AggregationDeviceService;
import com.example.spector.service.devicedata.AggregationDeviceDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/main/devices")
@RequiredArgsConstructor
public class DeviceMonitoringController {
    private final AggregationDeviceDataService aggregationDeviceDataService;
    private final AggregationDeviceService aggregationDeviceService;

    /**
     * GET /api/main/devices/monitoring
     */
    // Получение списка с фильтрацией
    @GetMapping("/monitoring")
    public List<DeviceDataBaseDTO> getDevices(
            @RequestParam(name = "location", required = false) String location
    ) {
        return aggregationDeviceDataService.getDeviceDataSummary(Optional.ofNullable(location));
    }

    /**
     * GET /api/main/devices/monitoring/{deviceId}
     */
    // Получение деталей устройства с данными в реальном времени
    @GetMapping("/monitoring/{deviceId}")
    public DeviceDataDetailDTO getDeviceDataDetail(@PathVariable("deviceId") Long id) {
        return aggregationDeviceDataService.getDeviceDataDetails(id);
    }

    /**
     * PUT /api/main/devices/{deviceId}/enable
     */
    // Включение устройства
    @PutMapping("/{deviceId}/enable")
    public ResponseEntity<Void> enableDevice(@PathVariable("deviceId") Long id) {
        aggregationDeviceService.setEnable(id, true);
        return ResponseEntity.noContent().build();
    }

    /**
     * PUT /api/main/devices/{deviceId}/disable
     */
    @PutMapping("/{deviceId}/disable")
    // Выключение устройства
    public ResponseEntity<Void> disableDevice(@PathVariable("deviceId") Long id) {
        aggregationDeviceService.setEnable(id, false);
        return ResponseEntity.noContent().build();
    }

    /**
     * GET /api/main/devicees/{deviceId}
     */
    // Получение деталей устройства
    @GetMapping("/{deviceId}")
    public DeviceDetailDTO getDeviceDetail(@PathVariable Long deviceId) {
        return aggregationDeviceService.getDeviceDetail(deviceId);
    }

    // Создание устройства
    @PostMapping("/{deviceId}")
    public ResponseEntity<DeviceDetailDTO> createDevice(
            @RequestBody DeviceCreateDTO createDTO) {
        DeviceDetailDTO created = aggregationDeviceService.createDevice(createDTO);

        return ResponseEntity
                .created(URI.create("/api/main/devices/" + created.getId()))
                .body(created);
    }

    // Обновление устройства
    @PutMapping("/{deviceId}")
    public DeviceDetailDTO updateDevice(
            @PathVariable Long deviceId,
            @RequestBody DeviceDetailDTO updateDTO) {
        return aggregationDeviceService.updateDevice(deviceId, updateDTO);
    }

    // Удаление устройства
    @DeleteMapping("/{deviceId}")
    public ResponseEntity<Void> deleteDevice(@PathVariable Long deviceId) {
        aggregationDeviceService.deleteDevice(deviceId);

        return ResponseEntity.noContent().build();
    }
}
