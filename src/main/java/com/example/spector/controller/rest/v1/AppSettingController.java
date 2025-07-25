package com.example.spector.controller.rest.v1;

import com.example.spector.domain.dto.appsetting.AppSettingDTO;
import com.example.spector.service.appsetting.AggregationAppSettingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/main/settings")
@RequiredArgsConstructor
@Tag(name = "Application Settings", description = "API для управления настройками приложения")
public class AppSettingController {
    private final AggregationAppSettingService aggregationAppSettingService;

    /**
     * GET /api/main/settings
     */
    @Operation(
            summary = "Получить текущие настройки приложения",
            description = "Возвращает все текущие настройки приложения")
    @ApiResponse(
            responseCode = "200",
            description = "Настройки успешно получены",
            content = @Content(schema = @Schema(implementation = AppSettingDTO.class)))
    @GetMapping
    public ResponseEntity<AppSettingDTO> getSettings() {
        return ResponseEntity.ok(aggregationAppSettingService.getSettings());
    }

    @Operation(
            summary = "Обновить настройки приложения",
            description = "Обновляет настройки приложения и возвращает актуальные значения")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Настройки успешно обновлены",
                    content = @Content(schema = @Schema(implementation = AppSettingDTO.class))),
            @ApiResponse(
                    responseCode = "400",
                    description = "Неверные параметры запроса")})
    @PutMapping
    public ResponseEntity<AppSettingDTO> updateSettings(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "DTO с обновлёнными значениями настроек",
                    required = true,
                    content = @Content(schema = @Schema(implementation = AppSettingDTO.class)))
            @RequestBody AppSettingDTO updateDTO) {
        return ResponseEntity.ok(aggregationAppSettingService.updateSettings(updateDTO));
    }

    @Operation(
            summary = "Сбросить настройки к значениям по умолчанию",
            description = "Восстанавливает настройки приложения к значениям по умолчанию")
    @ApiResponse(
            responseCode = "200",
            description = "Настройки сброшены к значениям по умолчанию",
            content = @Content(schema = @Schema(implementation = AppSettingDTO.class)))
    @PostMapping("/reset")
    public ResponseEntity<AppSettingDTO> resetSettings() {
        aggregationAppSettingService.resetToDefaults();
        return ResponseEntity.ok(aggregationAppSettingService.getSettings());
    }
}
