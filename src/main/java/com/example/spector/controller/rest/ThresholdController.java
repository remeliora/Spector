package com.example.spector.controller.rest;

import com.example.spector.domain.dto.parameter.rest.ParameterShortDTO;
import com.example.spector.domain.dto.threshold.rest.ThresholdBaseDTO;
import com.example.spector.domain.dto.threshold.rest.ThresholdCreateDTO;
import com.example.spector.domain.dto.threshold.rest.ThresholdDetailDTO;
import com.example.spector.service.threshold.AggregationThresholdService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/main/devices/{deviceId}/thresholds")
@RequiredArgsConstructor
public class ThresholdController {
    private final AggregationThresholdService aggregationThresholdService;

    /**
     * GET /api/main/devices/{deviceId}/thresholds
     */
    //Получение списка с фильтрацией
    @GetMapping
    public List<ThresholdBaseDTO> getThresholds(@PathVariable("deviceId") Long deviceId) {
        return aggregationThresholdService.getThresholdByDevice(deviceId);
    }

    /**
     * GET /api/main/devices/{deviceId}/thresholds/{thresholdId}
     */
    // Получение деталей порога
    @GetMapping("/{thresholdId}")
    public ThresholdDetailDTO getThresholdDetails(
            @PathVariable Long deviceId,
            @PathVariable Long thresholdId) {
        return aggregationThresholdService.getThresholdDetails(deviceId, thresholdId);
    }

    /**
     * GET /api/main/devices/{deviceId}/thresholds/available-parameters
     */
    // Получение списка доступных параметров для порога
    @GetMapping("/available-parameters")
    public List<ParameterShortDTO> getAvailableParameters(@PathVariable Long deviceId) {
        return aggregationThresholdService.getAvailableParametersForDevice(deviceId);
    }

    // Создание порога
    @PostMapping
    public ResponseEntity<ThresholdDetailDTO> createThreshold(
            @PathVariable Long deviceId,
            @RequestBody @Valid ThresholdCreateDTO createDTO) {
        ThresholdDetailDTO createThreshold = aggregationThresholdService.createThreshold(deviceId, createDTO);

        return ResponseEntity
                .created(URI.create("/api/main/devices/" + deviceId
                                    + "/thresholds/" + createThreshold.getId()))
                .body(createThreshold);
    }

    // Обновление порога
    @PutMapping("/{thresholdId}")
    public ThresholdDetailDTO updateThreshold(
            @PathVariable Long deviceId,
            @PathVariable Long thresholdId,
            @RequestBody @Valid ThresholdDetailDTO updateDTO) {
        return aggregationThresholdService.updateThreshold(deviceId, thresholdId, updateDTO);
    }

    // Удаление порога
    @DeleteMapping("/{thresholdId}")
    public ResponseEntity<Void> deleteThreshold(
            @PathVariable Long deviceId,
            @PathVariable Long thresholdId) {
        aggregationThresholdService.deleteThreshold(deviceId, thresholdId);

        return ResponseEntity.noContent().build();
    }
}
