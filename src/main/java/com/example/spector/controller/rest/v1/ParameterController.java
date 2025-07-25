package com.example.spector.controller.rest.v1;

import com.example.spector.domain.dto.parameter.rest.ParameterBaseDTO;
import com.example.spector.domain.dto.parameter.rest.ParameterCreateDTO;
import com.example.spector.domain.dto.parameter.rest.ParameterDetailDTO;
import com.example.spector.service.parameter.AggregationParameterService;
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
    private final AggregationParameterService aggregationParameterService;

    /**
     * GET /api/main/device-types/{deviceTypeId}/parameters
     */
    // Получение списка с фильтрацией
    @GetMapping
    public List<ParameterBaseDTO> getParameters(@PathVariable("deviceTypeId") Long deviceTypeId) {
        return aggregationParameterService.getParameterByDeviceType(deviceTypeId);
    }

    /**
     * GET /api/main/device-types/{deviceTypeId}/parameters/{parameterId}
     */
    // Получение деталей параметра
    @GetMapping("/{parameterId}")
    public ParameterDetailDTO getParameterDetails(
            @PathVariable Long deviceTypeId,
            @PathVariable Long parameterId) {
        return aggregationParameterService.getParameterDetails(deviceTypeId, parameterId);
    }

    // Создание параметра
    @PostMapping
    public ResponseEntity<ParameterDetailDTO> createParameter(
            @PathVariable Long deviceTypeId,
            @RequestBody @Valid ParameterCreateDTO createDTO) {
        ParameterDetailDTO createParameter = aggregationParameterService.createParameter(deviceTypeId, createDTO);

        return ResponseEntity
                .created(URI.create("/api/main/device-types/" + deviceTypeId
                                    + "/parameters/" + createParameter.getId()))
                .body(createParameter);
    }

    // Обновление параметра
    @PutMapping("/{parameterId}")
    public ParameterDetailDTO updateParameter(
            @PathVariable Long deviceTypeId,
            @PathVariable Long parameterId,
            @RequestBody @Valid ParameterDetailDTO updateDTO) {
        return aggregationParameterService.updateParameter(deviceTypeId, parameterId, updateDTO);
    }

    // Удаление параметра
    @DeleteMapping("/{parameterId}")
    public ResponseEntity<Void> deleteParameter(
            @PathVariable Long deviceTypeId,
            @PathVariable Long parameterId) {
        aggregationParameterService.deleteParameter(deviceTypeId, parameterId);

        return ResponseEntity.noContent().build();
    }
}
