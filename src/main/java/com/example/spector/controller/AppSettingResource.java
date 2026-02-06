package com.example.spector.controller;

import com.example.spector.domain.setting.dto.AppSettingDto;
import com.example.spector.service.AppSettingServiceV1;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v2/main/settings")
@RequiredArgsConstructor
public class AppSettingResource {

    private final AppSettingServiceV1 appSettingServiceV1;

    // Полная информация о настройках приложения
    @GetMapping
    public AppSettingDto getAll() {
        return appSettingServiceV1.getAll();
    }

    // Обновление всех полей настроек
    @PutMapping
    public AppSettingDto update(@RequestBody @Valid AppSettingDto dto) {
        return appSettingServiceV1.update(dto);
    }
}
