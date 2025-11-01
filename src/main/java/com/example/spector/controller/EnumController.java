package com.example.spector.controller;

import com.example.spector.domain.dto.enums.EnumDTO;
import com.example.spector.service.EnumService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/main/enums")
@RequiredArgsConstructor
@Tag(name = "Enums Management", description = "API для получения значений перечислений (enum)")
public class EnumController {
    private final EnumService enumService;

    /**
     * GET /api/main/enums/alarm-types
     */
    @Operation(
            summary = "Получить типы тревог",
            description = "Возвращает список возможных типов тревог/оповещений")
    @ApiResponse(
            responseCode = "200",
            description = "Список типов тревог успешно получен",
            content = @Content(schema = @Schema(implementation = EnumDTO.class, type = "array")))
    // Получение значений Enum - Alarm Types
    @GetMapping("/alarm-types")
    public List<EnumDTO> getAlarmTypes() {
        return enumService.getAlarmTypes();
    }

    /**
     * GET /api/main/enums/data-types
     */
    @Operation(
            summary = "Получить типы данных",
            description = "Возвращает список возможных типов данных для параметров")
    @ApiResponse(
            responseCode = "200",
            description = "Список типов данных успешно получен",
            content = @Content(schema = @Schema(implementation = EnumDTO.class, type = "array")))
    // Получение значений Enum - Data Types
    @GetMapping("/data-types")
    public List<EnumDTO> getDataTypes() {
        return enumService.getDataTypes();
    }
}
