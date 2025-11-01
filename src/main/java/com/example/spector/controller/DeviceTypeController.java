package com.example.spector.controller;

import com.example.spector.domain.dto.device.rest.DeviceByDeviceTypeDTO;
import com.example.spector.domain.dto.devicetype.rest.DeviceTypeBaseDTO;
import com.example.spector.domain.dto.devicetype.rest.DeviceTypeCreateDTO;
import com.example.spector.domain.dto.devicetype.rest.DeviceTypeDetailDTO;
import com.example.spector.domain.dto.devicetype.rest.DeviceTypeUpdateDTO;
import com.example.spector.domain.dto.parameter.rest.ParameterByDeviceTypeDTO;
import com.example.spector.service.DeviceTypeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Device Types Management", description = "API для управления типами устройств")
public class DeviceTypeController {
    private final DeviceTypeService deviceTypeService;

    /**
     * GET /api/v1/main/device-types
     */
    @Operation(
            summary = "Получить список типов устройств",
            description = "Возвращает список типов устройств с возможностью фильтрации по имени класса")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Успешное получение списка",
                    content = @Content(schema = @Schema(implementation = DeviceTypeBaseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Неверные параметры запроса")})
    // Получение списка с фильтрацией
    @GetMapping
    public List<DeviceTypeBaseDTO> getDeviceTypes(
            @Parameter(description = "Имя класса для фильтрации", example = "Сервер")
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
    @Operation(
            summary = "Получить детали типа устройства",
            description = "Возвращает полную информацию о типе устройства по его идентификатору")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Тип устройства найден",
                    content = @Content(schema = @Schema(implementation = DeviceTypeDetailDTO.class))),
            @ApiResponse(responseCode = "404", description = "Тип устройства не найден")})
    // Получение деталей типа устройства
    @GetMapping("/{deviceTypeId}")
    public DeviceTypeDetailDTO getDeviceTypeDetail(
            @Parameter(description = "ID типа устройства", required = true, example = "1")
            @PathVariable Long deviceTypeId) {
        return deviceTypeService.getDeviceTypeDetail(deviceTypeId);
    }

    /**
     * GET /api/v1/main/device-types/{deviceTypeId}/devices-list
     */
    @Operation(
            summary = "Получить список устройств по типу",
            description = "Возвращает список устройств, принадлежащих указанному типу")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Список устройств получен",
                    content = @Content(schema = @Schema(implementation = DeviceByDeviceTypeDTO.class))),
            @ApiResponse(responseCode = "404", description = "Тип устройства не найден")})
    // Получение списка устройств по типу
    @GetMapping("/{deviceTypeId}/devices-list")
    public List<DeviceByDeviceTypeDTO> getListDevicesByType(
            @Parameter(description = "ID типа устройства", required = true, example = "1")
            @PathVariable Long deviceTypeId) {
        return deviceTypeService.getListDevicesByType(deviceTypeId);
    }

    /**
     * GET /api/v1/main/device-types/{deviceTypeId}/parameters-list
     */
    @Operation(
            summary = "Получить список параметров по типу устройства",
            description = "Возвращает список параметров, доступных для указанного типа устройства")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Список параметров получен",
                    content = @Content(schema = @Schema(implementation = ParameterByDeviceTypeDTO.class))),
            @ApiResponse(responseCode = "404", description = "Тип устройства не найден")})
    // Получение списка устройств по типу
    @GetMapping("/{deviceTypeId}/parameters-list")
    public List<ParameterByDeviceTypeDTO> getListParametersByType(
            @Parameter(description = "ID типа устройства", required = true, example = "1")
            @PathVariable Long deviceTypeId) {
        return deviceTypeService.getListParametersByType(deviceTypeId);
    }

    @Operation(
            summary = "Создать новый тип устройства",
            description = "Создает новый тип устройства и возвращает его детали")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Тип устройства успешно создан",
                    content = @Content(schema = @Schema(implementation = DeviceTypeDetailDTO.class))),
            @ApiResponse(responseCode = "400", description = "Неверные данные запроса"),
            @ApiResponse(responseCode = "409", description = "Конфликт (например, тип с таким именем уже существует)")})
    // Создание типа устройства
    @PostMapping
    public ResponseEntity<DeviceTypeDetailDTO> createDeviceType(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Данные для создания типа устройства",
                    required = true,
                    content = @Content(schema = @Schema(implementation = DeviceTypeCreateDTO.class)))
            @RequestBody @Valid DeviceTypeCreateDTO createDTO) {
        DeviceTypeDetailDTO created = deviceTypeService.createDeviceType(createDTO);

        return ResponseEntity
                .created(URI.create("/api/v1/main/device-types/" + created.getId()))
                .body(created);
    }

    @Operation(
            summary = "Обновить тип устройства",
            description = "Обновляет существующий тип устройства и возвращает его актуальные детали")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Тип устройства успешно обновлен",
                    content = @Content(schema = @Schema(implementation = DeviceTypeDetailDTO.class))),
            @ApiResponse(responseCode = "400", description = "Неверные данные запроса"),
            @ApiResponse(responseCode = "404", description = "Тип устройства не найден")})
    // Обновление типа устройства
    @PutMapping("/{deviceTypeId}")
    public DeviceTypeDetailDTO updateDeviceType(
            @Parameter(description = "ID типа устройства", required = true, example = "1")
            @PathVariable Long deviceTypeId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Новые данные типа устройства",
                    required = true,
                    content = @Content(schema = @Schema(implementation = DeviceTypeDetailDTO.class)))
            @RequestBody @Valid DeviceTypeUpdateDTO updateDTO) {
        return deviceTypeService.updateDeviceType(deviceTypeId, updateDTO);
    }

    @Operation(
            summary = "Удалить тип устройства",
            description = "Удаляет тип устройства по его идентификатору")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Тип устройства успешно удален"),
            @ApiResponse(responseCode = "404", description = "Тип устройства не найден")})
    // Удаление типа устройства
    @DeleteMapping("/{deviceTypeId}")
    public ResponseEntity<Void> deleteDeviceType(
            @Parameter(description = "ID типа устройства", required = true, example = "1")
            @PathVariable Long deviceTypeId) {
        deviceTypeService.deleteDeviceType(deviceTypeId);

        return ResponseEntity.noContent().build();
    }
}
