package com.example.spector.controller.rest;

import com.example.spector.domain.dto.enumeration.EnumeratedStatusAvailableDTO;
import com.example.spector.domain.dto.enumeration.EnumeratedStatusBaseDTO;
import com.example.spector.domain.dto.enumeration.EnumeratedStatusCreateDTO;
import com.example.spector.domain.dto.enumeration.EnumeratedStatusDetailDTO;
import com.example.spector.service.enumeration.AggregationEnumeratedStatusService;
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

@RestController
@RequestMapping("/api/v1/main/enumerations")
@RequiredArgsConstructor
@Tag(name = "Enumerated Status Management", description = "API для управления перечисляемыми статусами (словарями)")
public class EnumeratedStatusController {
    private final AggregationEnumeratedStatusService aggregationEnumeratedStatusService;

    /**
     * GET /api/main/enumerations
     */
    @Operation(
            summary = "Получить список перечисляемых статусов",
            description = "Возвращает список всех доступных перечисляемых статусов с количеством элементов")
    @ApiResponse(
            responseCode = "200",
            description = "Список статусов успешно получен",
            content = @Content(schema = @Schema(implementation = EnumeratedStatusBaseDTO.class, type = "array")))
    // Получение списка
    @GetMapping
    public List<EnumeratedStatusBaseDTO> getEnumeratedStatus() {
        return aggregationEnumeratedStatusService.getEnumeratedStatuses();
    }

    /**
     * GET /api/main/enumerations/{collectionName}
     */
    @Operation(
            summary = "Получить детали перечисляемого статуса",
            description = "Возвращает полную информацию о перечисляемом статусе по имени коллекции")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Детали словаря успешно получены",
                    content = @Content(schema = @Schema(implementation = EnumeratedStatusDetailDTO.class))),
            @ApiResponse(
                    responseCode = "404",
                    description = "Статус с указанным именем коллекции не найден")})
    // Получение деталей словаря
    @GetMapping("/{collectionName}")
    public EnumeratedStatusDetailDTO getEnumeratedStatusDetail(
            @Parameter(description = "Имя коллекции статусов", required = true, example = "upsBatteryStatus")
            @PathVariable String collectionName) {
        return aggregationEnumeratedStatusService.getEnumeratedStatusesDetail(collectionName);
    }

    /**
     * GET /api/main/enumerations/available-parameters
     */
    @Operation(
            summary = "Получить доступные параметры с типом ENUMERATED",
            description = "Возвращает список параметров, которые используют перечисляемые статусы")
    @ApiResponse(
            responseCode = "200",
            description = "Список параметров успешно получен",
            content = @Content(schema = @Schema(implementation = EnumeratedStatusAvailableDTO.class, type = "array")))
    // Получение списка доступных параметров (dataType = ENUMERATED)
    @GetMapping("/available-parameters")
    public List<EnumeratedStatusAvailableDTO> getAvailableParameters() {
        return aggregationEnumeratedStatusService.getAvailableParameters();
    }

    @Operation(
            summary = "Создать новый перечисляемый статус",
            description = "Создает новый словарь перечисляемых значений")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Статус успешно создан",
                    content = @Content(schema = @Schema(implementation = EnumeratedStatusDetailDTO.class))),
            @ApiResponse(
                    responseCode = "400",
                    description = "Неверные данные запроса"),
            @ApiResponse(
                    responseCode = "409",
                    description = "Конфликт: статус с таким именем уже существует")})
    // Создание словаря
    @PostMapping
    public ResponseEntity<EnumeratedStatusDetailDTO> createEnumeratedStatus(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "DTO для создания нового перечисляемого статуса",
                    required = true,
                    content = @Content(schema = @Schema(implementation = EnumeratedStatusCreateDTO.class)))
            @RequestBody @Valid EnumeratedStatusCreateDTO createDTO) {
        EnumeratedStatusDetailDTO created = aggregationEnumeratedStatusService.createEnumeratedStatus(createDTO);

        return ResponseEntity
                .created(URI.create("/api/main/enumerations" + created.getName()))
                .body(created);
    }

    @Operation(
            summary = "Обновить перечисляемый статус",
            description = "Обновляет словарь перечисляемых значений по имени коллекции")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Статус успешно обновлен",
                    content = @Content(schema = @Schema(implementation = EnumeratedStatusDetailDTO.class))),
            @ApiResponse(
                    responseCode = "400",
                    description = "Неверные данные запроса"),
            @ApiResponse(
                    responseCode = "404",
                    description = "Статус с указанным именем коллекции не найден")})
    // Обновление словаря
    @PutMapping("/{collectionName}")
    public EnumeratedStatusDetailDTO updateEnumeratedStatus(
            @Parameter(description = "Имя коллекции статусов", required = true, example = "upsBatteryStatus")
            @PathVariable String collectionName,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "DTO с обновленными данными перечисляемого статуса",
                    required = true,
                    content = @Content(schema = @Schema(implementation = EnumeratedStatusDetailDTO.class)))
            @RequestBody @Valid EnumeratedStatusDetailDTO updateDTO) {
        return aggregationEnumeratedStatusService.updateEnumeratedStatus(collectionName, updateDTO);
    }

    @Operation(
            summary = "Удалить перечисляемый статус",
            description = "Удаляет словарь перечисляемых значений по имени коллекции")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Статус успешно удален"),
            @ApiResponse(
                    responseCode = "404",
                    description = "Статус с указанным именем коллекции не найден"),
            @ApiResponse(
                    responseCode = "409",
                    description = "Невозможно удалить: статус используется в параметрах")})
    // Удаление словаря
    @DeleteMapping("/{collectionName}")
    public ResponseEntity<Void> deleteEnumeratedStatus(
            @Parameter(description = "Имя коллекции статусов", required = true, example = "upsBatteryStatus")
            @PathVariable String collectionName) {
        aggregationEnumeratedStatusService.deleteEnumeratedStatus(collectionName);

        return ResponseEntity.noContent().build();
    }
}
