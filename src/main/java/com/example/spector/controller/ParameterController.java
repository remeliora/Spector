package com.example.spector.controller;

import com.example.spector.domain.dto.parameter.rest.*;
import com.example.spector.service.ParameterService;
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
@RequestMapping("/api/v1/main/device-types/{deviceTypeId}/parameters")
@RequiredArgsConstructor
@Tag(name = "Parameter Management", description = "API для управления параметрами устройств по типам устройств")
public class ParameterController {
    private final ParameterService parameterService;

    /**
     * GET /api/v1/main/device-types/{deviceTypeId}/parameters
     */
    // Получение списка с фильтрацией
    @Operation(summary = "Получить список параметров",
            description = "Возвращает список параметров для указанного типа устройства.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список параметров успешно получен",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ParameterBaseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Тип устройства не найден", content = @Content)
    })
    @GetMapping
    public List<ParameterBaseDTO> getParameters(
            @Parameter(description = "Идентификатор типа устройства", example = "1", required = true)
            @PathVariable("deviceTypeId") Long deviceTypeId) {
        return parameterService.getParameterByDeviceType(deviceTypeId);
    }

    /**
     * GET /api/v1/main/device-types/{deviceTypeId}/parameters/{parameterId}
     */
    // Получение деталей параметра
    @Operation(summary = "Получить детали параметра",
            description = "Возвращает полную информацию о параметре по его идентификатору и типу устройства.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Детали параметра успешно получены",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ParameterDetailDTO.class))),
            @ApiResponse(responseCode = "404", description = "Параметр или тип устройства не найден", content = @Content)
    })
    @GetMapping("/{parameterId}")
    public ParameterDetailDTO getParameterDetails(
            @Parameter(description = "Идентификатор типа устройства", example = "1", required = true)
            @PathVariable Long deviceTypeId,
            @Parameter(description = "Идентификатор параметра", example = "5", required = true)
            @PathVariable Long parameterId) {
        return parameterService.getParameterDetails(deviceTypeId, parameterId);
    }

    // Создание параметра
    @Operation(summary = "Создать параметр", description = "Создает новый параметр для указанного типа устройства.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Параметр успешно создан",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ParameterDetailDTO.class))),
            @ApiResponse(responseCode = "400", description = "Неверный формат данных", content = @Content),
            @ApiResponse(responseCode = "404", description = "Тип устройства не найден", content = @Content)
    })
    @PostMapping
    public ResponseEntity<ParameterDetailDTO> createParameter(
            @Parameter(description = "Идентификатор типа устройства", example = "1", required = true)
            @PathVariable Long deviceTypeId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Данные для создания параметра", required = true,
                    content = @Content(schema = @Schema(implementation = ParameterCreateDTO.class)))
            @RequestBody @Valid ParameterCreateDTO createDTO) {
        ParameterDetailDTO createParameter = parameterService.createParameter(deviceTypeId, createDTO);

        return ResponseEntity
                .created(URI.create("/api/v1/main/device-types/" + deviceTypeId
                                    + "/parameters/" + createParameter.getId()))
                .body(createParameter);
    }

    // Обновление параметра
    @Operation(summary = "Обновить параметр", description = "Обновляет информацию о существующем параметре.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Параметр успешно обновлен",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ParameterDetailDTO.class))),
            @ApiResponse(responseCode = "400", description = "Неверный формат данных", content = @Content),
            @ApiResponse(responseCode = "404", description = "Параметр или тип устройства не найден", content = @Content)
    })
    @PutMapping("/{parameterId}")
    public ParameterDetailDTO updateParameter(
            @Parameter(description = "Идентификатор типа устройства", example = "1", required = true)
            @PathVariable Long deviceTypeId,
            @Parameter(description = "Идентификатор параметра", example = "5", required = true)
            @PathVariable Long parameterId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Данные для обновления параметра", required = true,
                    content = @Content(schema = @Schema(implementation = ParameterUpdateDTO.class)))
            @RequestBody @Valid ParameterUpdateDTO updateDTO) {
        return parameterService.updateParameter(deviceTypeId, parameterId, updateDTO);
    }

    // Удаление параметра
    @Operation(summary = "Удалить параметр", description = "Удаляет параметр по его идентификатору и типу устройства.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Параметр успешно удален"),
            @ApiResponse(responseCode = "404", description = "Параметр или тип устройства не найден", content = @Content)
    })
    @DeleteMapping("/{parameterId}")
    public ResponseEntity<Void> deleteParameter(
            @Parameter(description = "Идентификатор типа устройства", example = "1", required = true)
            @PathVariable Long deviceTypeId,
            @Parameter(description = "Идентификатор параметра", example = "5", required = true)
            @PathVariable Long parameterId) {
        parameterService.deleteParameter(deviceTypeId, parameterId);

        return ResponseEntity.noContent().build();
    }
}
