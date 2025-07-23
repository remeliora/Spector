package com.example.spector.controller.rest;

import com.example.spector.domain.dto.enumeration.EnumeratedStatusAvailableDTO;
import com.example.spector.domain.dto.enumeration.EnumeratedStatusBaseDTO;
import com.example.spector.domain.dto.enumeration.EnumeratedStatusCreateDTO;
import com.example.spector.domain.dto.enumeration.EnumeratedStatusDetailDTO;
import com.example.spector.service.enumeration.AggregationEnumeratedStatusService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/main/enumerations")
@RequiredArgsConstructor
public class EnumeratedStatusController {
    private final AggregationEnumeratedStatusService aggregationEnumeratedStatusService;

    /**
     * GET /api/main/enumerations
     */
    // Получение списка
    @GetMapping
    public List<EnumeratedStatusBaseDTO> getEnumeratedStatus() {
        return aggregationEnumeratedStatusService.getEnumeratedStatuses();
    }

    /**
     * GET /api/main/enumerations/{collectionName}
     */
    // Получение деталей словаря
    @GetMapping("/{collectionName}")
    public EnumeratedStatusDetailDTO getEnumeratedStatusDetail(
            @PathVariable String collectionName) {
        return aggregationEnumeratedStatusService.getEnumeratedStatusesDetail(collectionName);
    }

    /**
     * GET /api/main/enumerations/available-parameters
     */
    // Получение списка доступных параметров (dataType = ENUMERATED)
    @GetMapping("/available-parameters")
    public List<EnumeratedStatusAvailableDTO> getAvailableParameters() {
        return aggregationEnumeratedStatusService.getAvailableParameters();
    }

    // Создание словаря
    @PostMapping
    public ResponseEntity<EnumeratedStatusDetailDTO> createEnumeratedStatus(
            @RequestBody @Valid EnumeratedStatusCreateDTO createDTO) {
        EnumeratedStatusDetailDTO created = aggregationEnumeratedStatusService.createEnumeratedStatus(createDTO);

        return ResponseEntity
                .created(URI.create("/api/main/enumerations" + created.getName()))
                .body(created);
    }

    // Обновление словаря
    @PutMapping("/{collectionName}")
    public EnumeratedStatusDetailDTO updateEnumeratedStatus(
            @PathVariable String collectionName,
            @RequestBody @Valid EnumeratedStatusDetailDTO updateDTO) {
        return aggregationEnumeratedStatusService.updateEnumeratedStatus(collectionName, updateDTO);
    }

    // Удаление словаря
    @DeleteMapping("/{collectionName}")
    public ResponseEntity<Void> deleteEnumeratedStatus(@PathVariable String collectionName) {
        aggregationEnumeratedStatusService.deleteEnumeratedStatus(collectionName);

        return ResponseEntity.noContent().build();
    }
}
