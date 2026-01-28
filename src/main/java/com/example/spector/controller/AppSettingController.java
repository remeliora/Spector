package com.example.spector.controller;

import com.example.spector.domain.setting.dto.AppSettingDto;
import com.example.spector.modules.event.EventDispatcher;
import com.example.spector.service.AppSettingService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/main/settings")
@RequiredArgsConstructor
public class AppSettingController {
    private final AppSettingService appSettingService;
    private final EventDispatcher eventDispatcher;
    private final ClientIpExtractor clientIpExtractor;

    @GetMapping
    public ResponseEntity<AppSettingDto> getSettings() {
        return ResponseEntity.ok(appSettingService.getSettings());
    }

    @PutMapping
    public ResponseEntity<AppSettingDto> updateSettings(
            @RequestBody AppSettingDto updateDTO, HttpServletRequest request) {
        String clientIp = clientIpExtractor.extract(request);
        AppSettingDto result = appSettingService.updateSettings(updateDTO, clientIp, eventDispatcher);

        return ResponseEntity.ok(result);
    }

    @PostMapping("/reset")
    public ResponseEntity<AppSettingDto> resetSettings(HttpServletRequest request) {
        String clientIp = clientIpExtractor.extract(request);
        appSettingService.resetToDefaults(clientIp, eventDispatcher);

        return ResponseEntity.ok(appSettingService.getSettings());
    }
}
