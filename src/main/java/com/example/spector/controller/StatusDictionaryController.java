package com.example.spector.controller;

import com.example.spector.domain.statusdictionary.dto.StatusDictionaryBaseDTO;
import com.example.spector.domain.statusdictionary.dto.StatusDictionaryCreateDTO;
import com.example.spector.domain.statusdictionary.dto.StatusDictionaryDetailDTO;
import com.example.spector.domain.statusdictionary.dto.StatusDictionaryUpdateDTO;
import com.example.spector.modules.event.EventDispatcher;
import com.example.spector.service.StatusDictionaryService;
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
public class StatusDictionaryController {
    private final StatusDictionaryService statusDictionaryService;
    private final ClientIpExtractor clientIpExtractor;
    private final EventDispatcher eventDispatcher;

    // Получение списка (без изменений)
    @GetMapping
    public List<StatusDictionaryBaseDTO> getStatusDictionaries() {
        return statusDictionaryService.getStatusDictionaries();
    }

    /**
     * GET /api/v1/main/status-dictionaries/{id}
     */
    @GetMapping("/{id}")
    public StatusDictionaryDetailDTO getStatusDictionaryDetail(
            @PathVariable Long id) {
        return statusDictionaryService.getStatusDictionaryDetail(id);
    }

    @PostMapping
    public ResponseEntity<StatusDictionaryDetailDTO> createStatusDictionary(
            @RequestBody @Valid StatusDictionaryCreateDTO createDTO, HttpServletRequest request) {
        String clientIp = clientIpExtractor.extract(request);
        StatusDictionaryDetailDTO created = statusDictionaryService.createStatusDictionary(createDTO, clientIp, eventDispatcher);

        return ResponseEntity
                .created(URI.create("/api/v1/main/status-dictionaries/" + created.getId()))
                .body(created);
    }

    /**
     * PUT /api/v1/main/status-dictionaries/{id}
     */
    @PutMapping("/{id}")
    public StatusDictionaryDetailDTO updateStatusDictionary(
            @PathVariable Long id,
            @RequestBody @Valid StatusDictionaryUpdateDTO updateDTO, HttpServletRequest request) {
        String clientIp = clientIpExtractor.extract(request);

        return statusDictionaryService.updateStatusDictionary(id, updateDTO, clientIp, eventDispatcher);
    }

    /**
     * DELETE /api/v1/main/
     * /{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStatusDictionary(
            @PathVariable Long id, HttpServletRequest request) {
        String clientIp = clientIpExtractor.extract(request);
        statusDictionaryService.deleteStatusDictionary(id, clientIp, eventDispatcher);

        return ResponseEntity.noContent().build();
    }
}
