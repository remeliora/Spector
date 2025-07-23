package com.example.spector.controller.rest;

import com.example.spector.domain.dto.appsetting.AppSettingDTO;
import com.example.spector.service.appsetting.AggregationAppSettingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/main/settings")
@RequiredArgsConstructor
public class AppSettingController {
    private final AggregationAppSettingService aggregationAppSettingService;

    @GetMapping
    public ResponseEntity<AppSettingDTO> getSettings() {
        return ResponseEntity.ok(aggregationAppSettingService.getSettings());
    }

    @PutMapping
    public ResponseEntity<AppSettingDTO> updateSettings(@RequestBody AppSettingDTO updateDTO) {
        return ResponseEntity.ok(aggregationAppSettingService.updateSettings(updateDTO));
    }

    @PostMapping("/reset")
    public ResponseEntity<AppSettingDTO> resetSettings() {
        aggregationAppSettingService.resetToDefaults();
        return ResponseEntity.ok(aggregationAppSettingService.getSettings());
    }
}
