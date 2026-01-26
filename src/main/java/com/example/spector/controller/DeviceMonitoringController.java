package com.example.spector.controller;

import com.example.spector.domain.dto.device.rest.DeviceCreateDTO;
import com.example.spector.domain.dto.device.rest.DeviceDetailDTO;
import com.example.spector.domain.dto.device.rest.DeviceUpdateDTO;
import com.example.spector.domain.dto.devicedata.rest.DeviceDataBaseDTO;
import com.example.spector.domain.dto.devicedata.rest.DeviceDataDetailDTO;
import com.example.spector.domain.dto.devicetype.rest.DeviceTypeShortDTO;
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
@Tag(name = "Device Management", description = "API для управления устройствами и мониторинга")
public class DeviceMonitoringController {
    private final DeviceDataService deviceDataService;
    private final DeviceService deviceService;
    private final ClientIpExtractor clientIpExtractor;
    private final EventDispatcher eventDispatcher;

    /**
     * GET /api/v1/main/devices/monitoring
     */
    // Получение списка с фильтрацией
    @Operation(summary = "Получить список данных устройств",
            description = "Возвращает список данных устройств с возможностью фильтрации по локации.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список данных устройств успешно получен",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = DeviceDataBaseDTO.class)))
    })
    @GetMapping("/monitoring")
    public List<DeviceDataBaseDTO> getDevices(
            @Parameter(description = "Фильтр по локации устройства", example = "Data Center 1", required = false)
            @RequestParam(name = "location", required = false) String location) {
        return deviceDataService.getDeviceDataSummary(Optional.ofNullable(location));
    }

    @Operation(summary = "Получить уникальные локации",
            description = "Возвращает список всех уникальных локаций устройств.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список локаций успешно получен",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = String.class)))
    })
    @GetMapping("/unique-locations")
    public List<String> getUniqueLocations() {
        return deviceService.getUniqueLocations();
    }

    /**
     * GET /api/v1/main/devices/monitoring/{deviceId}
     */
    // Получение деталей устройства с данными в реальном времени
    @Operation(summary = "Получить детали устройства с данными мониторинга",
            description = "Возвращает полные сведения о конкретном устройстве и его данных в реальном времени.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Детали устройства успешно получены",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = DeviceDataDetailDTO.class))),
            @ApiResponse(responseCode = "404", description = "Устройство с указанным ID не найдено")
    })
    @GetMapping("/monitoring/{deviceId}")
    public DeviceDataDetailDTO getDeviceDataDetail(@PathVariable("deviceId") Long id) {
        return deviceDataService.getDeviceDataDetails(id);
    }

    /**
     * GET /api/v1/main/devices/available-device-types
     */
    // Получение списка всех типов устройств для устройства
    @Operation(summary = "Получить доступные типы устройств",
            description = "Возвращает список всех доступных типов устройств.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список типов устройств успешно получен",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = DeviceTypeShortDTO.class)))
    })
    @GetMapping("/available-device-types")
    public List<DeviceTypeShortDTO> getAvailableDeviceTypes() {
        return deviceService.getAvailableDeviceTypes();
    }

    /**
     * PUT /api/v1/main/devices/{deviceId}/enable
     */
    // Включение устройства
    @Operation(summary = "Включить устройство",
            description = "Активирует устройство, включает его в мониторинг.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Устройство успешно включено"),
            @ApiResponse(responseCode = "404", description = "Устройство с указанным ID не найдено")
    })
    @PutMapping("/{deviceId}/enable")
    public ResponseEntity<Void> enableDevice(
            @Parameter(description = "Уникальный идентификатор устройства", example = "1", required = true)
            @PathVariable("deviceId") Long id, HttpServletRequest request) {
        String clientIp = clientIpExtractor.extract(request);
        deviceService.setEnable(id, true, clientIp, eventDispatcher);

        return ResponseEntity.noContent().build();
    }

    /**
     * PUT /api/v1/main/devices/{deviceId}/disable
     */
    // Выключение устройства
    @Operation(summary = "Выключить устройство",
            description = "Деактивирует устройство, исключает его из мониторинга.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Устройство успешно выключено"),
            @ApiResponse(responseCode = "404", description = "Устройство с указанным ID не найдено")
    })
    @PutMapping("/{deviceId}/disable")
    public ResponseEntity<Void> disableDevice(
            @Parameter(description = "Уникальный идентификатор устройства", example = "1", required = true)
            @PathVariable("deviceId") Long id, HttpServletRequest request) {
        String clientIp = clientIpExtractor.extract(request);
        deviceService.setEnable(id, false, clientIp, eventDispatcher);

        return ResponseEntity.noContent().build();
    }

    /**
     * GET /api/v1/main/devices/{deviceId}
     */
    // Получение деталей устройства
    @Operation(summary = "Получить детали устройства",
            description = "Возвращает основные сведения о конкретном устройстве.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Детали устройства успешно получены",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = DeviceDetailDTO.class))),
            @ApiResponse(responseCode = "404", description = "Устройство с указанным ID не найдено")
    })
    @GetMapping("/{deviceId}")
    public DeviceDetailDTO getDeviceDetail(
            @Parameter(description = "Уникальный идентификатор устройства", example = "1", required = true)
            @PathVariable Long deviceId) {
        return deviceService.getDeviceDetail(deviceId);
    }

    // Создание устройства
    @Operation(summary = "Создать новое устройство",
            description = "Создаёт новое устройство с указанными параметрами.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Устройство успешно создано",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = DeviceDetailDTO.class))),
            @ApiResponse(responseCode = "400", description = "Некорректные данные для создания устройства")
    })
    @PostMapping
    public ResponseEntity<DeviceDetailDTO> createDevice(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Данные для создания устройства",
                    required = true,
                    content = @Content(schema = @Schema(implementation = DeviceCreateDTO.class))
            )
            @RequestBody DeviceCreateDTO createDTO, HttpServletRequest request) {
        String clientIp = clientIpExtractor.extract(request);
        DeviceDetailDTO created = deviceService.createDevice(createDTO, clientIp, eventDispatcher);

        return ResponseEntity
                .created(URI.create("/api/v1/main/devices/" + created.getId()))
                .body(created);
    }

    // Обновление устройства
    @Operation(summary = "Обновить устройство",
            description = "Обновляет данные существующего устройства.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Устройство успешно обновлено",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = DeviceDetailDTO.class))),
            @ApiResponse(responseCode = "400", description = "Некорректные данные для обновления устройства"),
            @ApiResponse(responseCode = "404", description = "Устройство с указанным ID не найдено")
    })
    @PutMapping("/{deviceId}")
    public DeviceDetailDTO updateDevice(
            @Parameter(description = "Уникальный идентификатор устройства", example = "1", required = true)
            @PathVariable Long deviceId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Данные для обновления устройства",
                    required = true,
                    content = @Content(schema = @Schema(implementation = DeviceUpdateDTO.class))
            )
            @RequestBody DeviceUpdateDTO updateDTO, HttpServletRequest request) {
        String clientIp = clientIpExtractor.extract(request);

        return deviceService.updateDevice(deviceId, updateDTO, clientIp, eventDispatcher);
    }

    // Удаление устройства
    @Operation(summary = "Удалить устройство",
            description = "Удаляет устройство с указанным ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Устройство успешно удалено"),
            @ApiResponse(responseCode = "404", description = "Устройство с указанным ID не найдено")
    })
    @DeleteMapping("/{deviceId}")
    public ResponseEntity<Void> deleteDevice(
            @Parameter(description = "Уникальный идентификатор устройства", example = "1", required = true)
            @PathVariable Long deviceId, HttpServletRequest request) {
        String clientIp = clientIpExtractor.extract(request);
        deviceService.deleteDevice(deviceId, clientIp, eventDispatcher);

        return ResponseEntity.noContent().build();
    }
}
