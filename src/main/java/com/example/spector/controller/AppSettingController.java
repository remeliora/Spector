package com.example.spector.controller;

import com.example.spector.domain.setting.AppSettingRestDto;
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
    public ResponseEntity<AppSettingRestDto> getSettings() {
        return ResponseEntity.ok(appSettingService.getSettings());
    }

    @PutMapping
    public ResponseEntity<AppSettingRestDto> updateSettings(
            @RequestBody AppSettingRestDto updateDTO, HttpServletRequest request) {
        String clientIp = clientIpExtractor.extract(request);
        AppSettingRestDto result = appSettingService.updateSettings(updateDTO, clientIp, eventDispatcher);

        return ResponseEntity.ok(result);
    }

    @PostMapping("/reset")
    public ResponseEntity<AppSettingRestDto> resetSettings(HttpServletRequest request) {
        String clientIp = clientIpExtractor.extract(request);
        appSettingService.resetToDefaults(clientIp, eventDispatcher);

        return ResponseEntity.ok(appSettingService.getSettings());
    }
}
