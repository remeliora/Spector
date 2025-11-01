package com.example.spector.controller;

import com.example.spector.domain.dto.parameter.rest.ParameterShortDTO;
import com.example.spector.domain.dto.threshold.rest.ThresholdBaseDTO;
import com.example.spector.domain.dto.threshold.rest.ThresholdCreateDTO;
import com.example.spector.domain.dto.threshold.rest.ThresholdDetailDTO;
import com.example.spector.domain.dto.threshold.rest.ThresholdUpdateDTO;
import com.example.spector.service.ThresholdService;
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
@RequestMapping("/api/v1/main/devices/{deviceId}/thresholds")
@RequiredArgsConstructor
@Tag(name = "Threshold Management", description = "API для управления порогами устройств")
public class ThresholdController {
    private final ThresholdService thresholdService;

    /**
     * GET /api/v1/main/devices/{deviceId}/thresholds
     */
    //Получение списка с фильтрацией
    @Operation(summary = "Получить список порогов устройства",
            description = "Возвращает список всех порогов, связанных с указанным устройством.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список порогов успешно получен",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ThresholdBaseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Устройство с указанным ID не найдено")
    })
    @GetMapping
    public List<ThresholdBaseDTO> getThresholds(
            @Parameter(description = "Уникальный идентификатор устройства", example = "1", required = true)
            @PathVariable("deviceId") Long deviceId) {
        return thresholdService.getThresholdByDevice(deviceId);
    }

    /**
     * GET /api/v1/main/devices/{deviceId}/thresholds/{thresholdId}
     */
    // Получение деталей порога
    @Operation(summary = "Получить детали порога",
            description = "Возвращает полные сведения о конкретном пороге устройства по его ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Детали порога успешно получены",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ThresholdDetailDTO.class))),
            @ApiResponse(responseCode = "404", description = "Порог или устройство не найдено")
    })
    @GetMapping("/{thresholdId}")
    public ThresholdDetailDTO getThresholdDetails(
            @Parameter(description = "Уникальный идентификатор устройства", example = "1", required = true)
            @PathVariable Long deviceId,
            @Parameter(description = "Уникальный идентификатор порога", example = "1", required = true)
            @PathVariable Long thresholdId) {
        return thresholdService.getThresholdDetails(deviceId, thresholdId);
    }

    /**
     * GET /api/v1/main/devices/{deviceId}/thresholds/available-parameters
     */
    // Получение списка доступных параметров для порога
    @Operation(summary = "Получить доступные параметры для порога",
            description = "Возвращает список параметров, доступных для создания или привязки к порогу для указанного устройства.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список параметров успешно получен",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ParameterShortDTO.class))),
            @ApiResponse(responseCode = "404", description = "Устройство с указанным ID не найдено")
    })
    @GetMapping("/available-parameters")
    public List<ParameterShortDTO> getAvailableParameters(
            @Parameter(description = "Уникальный идентификатор устройства", example = "1", required = true)
            @PathVariable Long deviceId) {
        return thresholdService.getAvailableParametersForDevice(deviceId);
    }

    // Создание порога
    @Operation(summary = "Создать новый порог",
            description = "Создаёт новый порог для указанного устройства.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Порог успешно создан",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ThresholdDetailDTO.class))),
            @ApiResponse(responseCode = "400", description = "Некорректные данные для создания порога"),
            @ApiResponse(responseCode = "404", description = "Устройство с указанным ID не найдено")
    })
    @PostMapping
    public ResponseEntity<ThresholdDetailDTO> createThreshold(
            @Parameter(description = "Уникальный идентификатор устройства", example = "1", required = true)
            @PathVariable Long deviceId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Данные для создания порога",
                    required = true,
                    content = @Content(schema = @Schema(implementation = ThresholdCreateDTO.class))
            )
            @RequestBody @Valid ThresholdCreateDTO createDTO) {
        ThresholdDetailDTO createThreshold = thresholdService.createThreshold(deviceId, createDTO);

        return ResponseEntity
                .created(URI.create("/api/v1/main/devices/" + deviceId
                                    + "/thresholds/" + createThreshold.getId()))
                .body(createThreshold);
    }

    // Обновление порога
    @Operation(summary = "Обновить порог",
            description = "Обновляет данные существующего порога устройства.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Порог успешно обновлён",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ThresholdDetailDTO.class))),
            @ApiResponse(responseCode = "400", description = "Некорректные данные для обновления порога"),
            @ApiResponse(responseCode = "404", description = "Порог или устройство не найдено")
    })
    @PutMapping("/{thresholdId}")
    public ThresholdDetailDTO updateThreshold(
            @Parameter(description = "Уникальный идентификатор устройства", example = "1", required = true)
            @PathVariable Long deviceId,
            @Parameter(description = "Уникальный идентификатор порога", example = "1", required = true)
            @PathVariable Long thresholdId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Данные для обновления порога",
                    required = true,
                    content = @Content(schema = @Schema(implementation = ThresholdDetailDTO.class))
            )
            @RequestBody @Valid ThresholdUpdateDTO updateDTO) {
        return thresholdService.updateThreshold(deviceId, thresholdId, updateDTO);
    }

    // Удаление порога
    @Operation(summary = "Удалить порог",
            description = "Удаляет указанный порог устройства.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Порог успешно удалён"),
            @ApiResponse(responseCode = "404", description = "Порог или устройство не найдено")
    })
    @DeleteMapping("/{thresholdId}")
    public ResponseEntity<Void> deleteThreshold(
            @Parameter(description = "Уникальный идентификатор устройства", example = "1", required = true)
            @PathVariable Long deviceId,
            @Parameter(description = "Уникальный идентификатор порога", example = "1", required = true)
            @PathVariable Long thresholdId) {
        thresholdService.deleteThreshold(deviceId, thresholdId);

        return ResponseEntity.noContent().build();
    }
}
