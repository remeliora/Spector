package com.example.spector.controller;

import com.example.spector.domain.dto.statusdictionary.StatusDictionaryBaseDTO;
import com.example.spector.domain.dto.statusdictionary.StatusDictionaryCreateDTO;
import com.example.spector.domain.dto.statusdictionary.StatusDictionaryDetailDTO;
import com.example.spector.domain.dto.statusdictionary.StatusDictionaryUpdateDTO;
import com.example.spector.modules.event.EventDispatcher;
import com.example.spector.service.StatusDictionaryService;
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

@RestController
@RequestMapping("/api/v1/main/status-dictionaries")
@RequiredArgsConstructor
@Tag(name = "Status Dictionary Management", description = "API для управления словарями статусов")
public class StatusDictionaryController {
    private final StatusDictionaryService statusDictionaryService;
    private final ClientIpExtractor clientIpExtractor;
    private final EventDispatcher eventDispatcher;

    // Получение списка (без изменений)
    @Operation(
            summary = "Получить список словарями статусов",
            description = "Возвращает список всех доступных словарей статусов с количеством элементов")
    @ApiResponse(
            responseCode = "200",
            description = "Список статусов успешно получен",
            content = @Content(schema = @Schema(implementation = StatusDictionaryBaseDTO.class, type = "array")))
    @GetMapping
    public List<StatusDictionaryBaseDTO> getStatusDictionaries() {
        return statusDictionaryService.getStatusDictionaries();
    }

    /**
     * GET /api/v1/main/status-dictionaries/{id}
     */
    @Operation(
            summary = "Получить детали словаря статусов",
            description = "Возвращает полную информацию о словаре статусов по ID")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Детали словаря успешно получены",
                    content = @Content(schema = @Schema(implementation = StatusDictionaryDetailDTO.class))),
            @ApiResponse(
                    responseCode = "404",
                    description = "Словарь с указанным ID не найден")})
    @GetMapping("/{id}")
    public StatusDictionaryDetailDTO getStatusDictionaryDetail(
            @Parameter(description = "ID словаря статусов", required = true, example = "1")
            @PathVariable Long id) {
        return statusDictionaryService.getStatusDictionaryDetail(id);
    }

    @Operation(
            summary = "Создать новый словарь статусов",
            description = "Создает новый словарь статусов")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Словарь успешно создан",
                    content = @Content(schema = @Schema(implementation = StatusDictionaryDetailDTO.class))),
            @ApiResponse(
                    responseCode = "400",
                    description = "Неверные данные запроса"),
            @ApiResponse(
                    responseCode = "409",
                    description = "Конфликт: статус с таким именем уже существует")})
    @PostMapping
    public ResponseEntity<StatusDictionaryDetailDTO> createStatusDictionary(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "DTO для создания нового словаря статусов",
                    required = true,
                    content = @Content(schema = @Schema(implementation = StatusDictionaryCreateDTO.class)))
            @RequestBody @Valid StatusDictionaryCreateDTO createDTO,  HttpServletRequest request) {
        String clientIp = clientIpExtractor.extract(request);
        StatusDictionaryDetailDTO created = statusDictionaryService.createStatusDictionary(createDTO, clientIp, eventDispatcher);

        return ResponseEntity
                .created(URI.create("/api/v1/main/status-dictionaries/" + created.getId()))
                .body(created);
    }

    /**
     * PUT /api/v1/main/status-dictionaries/{id}
     */
    @Operation(
            summary = "Обновить словарь статусов",
            description = "Обновляет словарь статусов по ID")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Статус успешно обновлен",
                    content = @Content(schema = @Schema(implementation = StatusDictionaryDetailDTO.class))),
            @ApiResponse(
                    responseCode = "400",
                    description = "Неверные данные запроса"),
            @ApiResponse(
                    responseCode = "404",
                    description = "Статус с указанным ID не найден")})
    @PutMapping("/{id}")
    public StatusDictionaryDetailDTO updateStatusDictionary(
            @Parameter(description = "ID словаря статусов", required = true, example = "1")
            @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "DTO с обновленными данными словаря статусов",
                    required = true,
                    content = @Content(schema = @Schema(implementation = StatusDictionaryDetailDTO.class)))
            @RequestBody @Valid StatusDictionaryUpdateDTO updateDTO, HttpServletRequest request) {
        String clientIp = clientIpExtractor.extract(request);

        return statusDictionaryService.updateStatusDictionary(id, updateDTO, clientIp, eventDispatcher);
    }

    /**
     * DELETE /api/v1/main/
     * /{id}
     */
    @Operation(
            summary = "Удалить словарь статусов",
            description = "Удаляет словарь статусов по ID")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Словарь успешно удален"),
            @ApiResponse(
                    responseCode = "404",
                    description = "Словарь с указанным ID не найден")})
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStatusDictionary(
            @Parameter(description = "ID словаря статусов", required = true, example = "1")
            @PathVariable Long id, HttpServletRequest request) {
        String clientIp = clientIpExtractor.extract(request);
        statusDictionaryService.deleteStatusDictionary(id, clientIp, eventDispatcher);

        return ResponseEntity.noContent().build();
    }
}
