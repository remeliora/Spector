package com.example.spector.controller;

import com.example.spector.domain.parameter.dto.ParameterShortDTO;
import com.example.spector.domain.threshold.dto.ThresholdBaseDTO;
import com.example.spector.domain.threshold.dto.ThresholdCreateDTO;
import com.example.spector.domain.threshold.dto.ThresholdDetailDTO;
import com.example.spector.domain.threshold.dto.ThresholdUpdateDTO;
import com.example.spector.modules.event.EventDispatcher;
import com.example.spector.service.ThresholdService;
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
@RequestMapping("/api/v1/main/devices/{deviceId}/thresholds")
@RequiredArgsConstructor
public class ThresholdController {
    private final ThresholdService thresholdService;
    private final ClientIpExtractor clientIpExtractor;
    private final EventDispatcher eventDispatcher;

    /**
     * GET /api/v1/main/devices/{deviceId}/thresholds
     */
    //Получение списка с фильтрацией
    @GetMapping
    public List<ThresholdBaseDTO> getThresholds(
            @PathVariable("deviceId") Long deviceId) {
        return thresholdService.getThresholdByDevice(deviceId);
    }

    /**
     * GET /api/v1/main/devices/{deviceId}/thresholds/{thresholdId}
     */
    // Получение деталей порога
    @GetMapping("/{thresholdId}")
    public ThresholdDetailDTO getThresholdDetails(
            @PathVariable Long deviceId,
            @PathVariable Long thresholdId) {
        return thresholdService.getThresholdDetails(deviceId, thresholdId);
    }

    /**
     * GET /api/v1/main/devices/{deviceId}/thresholds/available-parameters
     */
    // Получение списка доступных параметров для порога
    @GetMapping("/available-parameters")
    public List<ParameterShortDTO> getAvailableParameters(
            @PathVariable Long deviceId) {
        return thresholdService.getAvailableParametersForDevice(deviceId);
    }

    // Создание порога
    @PostMapping
    public ResponseEntity<ThresholdDetailDTO> createThreshold(
            @PathVariable Long deviceId,
            @RequestBody @Valid ThresholdCreateDTO createDTO, HttpServletRequest request) {
        String clientIp = clientIpExtractor.extract(request);
        ThresholdDetailDTO createThreshold = thresholdService.createThreshold(deviceId, createDTO,
                clientIp, eventDispatcher);

        return ResponseEntity
                .created(URI.create("/api/v1/main/devices/" + deviceId
                                    + "/thresholds/" + createThreshold.getId()))
                .body(createThreshold);
    }

    // Обновление порога
    @PutMapping("/{thresholdId}")
    public ThresholdDetailDTO updateThreshold(
            @PathVariable Long deviceId,
            @PathVariable Long thresholdId,
            @RequestBody @Valid ThresholdUpdateDTO updateDTO, HttpServletRequest request) {
        String clientIp = clientIpExtractor.extract(request);

        return thresholdService.updateThreshold(deviceId, thresholdId, updateDTO, clientIp, eventDispatcher);
    }

    // Удаление порога
    @DeleteMapping("/{thresholdId}")
    public ResponseEntity<Void> deleteThreshold(
            @PathVariable Long deviceId,
            @PathVariable Long thresholdId, HttpServletRequest request) {
        String clientIp = clientIpExtractor.extract(request);
        thresholdService.deleteThreshold(deviceId, thresholdId, clientIp, eventDispatcher);

        return ResponseEntity.noContent().build();
    }
}
