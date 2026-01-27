package com.example.spector.controller;

import com.example.spector.domain.parameter.dto.ParameterBaseDTO;
import com.example.spector.domain.parameter.dto.ParameterCreateDTO;
import com.example.spector.domain.parameter.dto.ParameterDetailDTO;
import com.example.spector.domain.parameter.dto.ParameterUpdateDTO;
import com.example.spector.modules.event.EventDispatcher;
import com.example.spector.service.ParameterService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/main/device-types/{deviceTypeId}/parameters")
@RequiredArgsConstructor
public class ParameterController {
    private final ParameterService parameterService;
    private final ClientIpExtractor clientIpExtractor;
    private final EventDispatcher eventDispatcher;

    /**
     * GET /api/v1/main/device-types/{deviceTypeId}/parameters
     */
    // Получение списка с фильтрацией
    @GetMapping
    public List<ParameterBaseDTO> getParameters(
            @PathVariable("deviceTypeId") Long deviceTypeId) {
        return parameterService.getParameterByDeviceType(deviceTypeId);
    }

    /**
     * GET /api/v1/main/device-types/{deviceTypeId}/parameters/{parameterId}
     */
    // Получение деталей параметра
    @GetMapping("/{parameterId}")
    public ParameterDetailDTO getParameterDetails(
            @PathVariable Long deviceTypeId,
            @PathVariable Long parameterId) {
        return parameterService.getParameterDetails(deviceTypeId, parameterId);
    }

    // Создание параметра
    @PostMapping
    public ResponseEntity<ParameterDetailDTO> createParameter(
            @PathVariable Long deviceTypeId,
            @RequestBody @Valid ParameterCreateDTO createDTO, HttpServletRequest request) {
        String clientIp = clientIpExtractor.extract(request);
        ParameterDetailDTO createParameter = parameterService.createParameter(deviceTypeId, createDTO,
                clientIp, eventDispatcher);

        return ResponseEntity
                .created(URI.create("/api/v1/main/device-types/" + deviceTypeId
                                    + "/parameters/" + createParameter.getId()))
                .body(createParameter);
    }

    // Обновление параметра
    @PutMapping("/{parameterId}")
    public ParameterDetailDTO updateParameter(
            @PathVariable Long deviceTypeId,
            @PathVariable Long parameterId,
            @RequestBody @Valid ParameterUpdateDTO updateDTO, HttpServletRequest request) {
        String clientIp = clientIpExtractor.extract(request);

        return parameterService.updateParameter(deviceTypeId, parameterId, updateDTO, clientIp, eventDispatcher);
    }

    // Удаление параметра
    @DeleteMapping("/{parameterId}")
    public ResponseEntity<Void> deleteParameter(
            @PathVariable Long deviceTypeId,
            @PathVariable Long parameterId, HttpServletRequest request) {
        String clientIp = clientIpExtractor.extract(request);
        parameterService.deleteParameter(deviceTypeId, parameterId, clientIp, eventDispatcher);

        return ResponseEntity.noContent().build();
    }
}
