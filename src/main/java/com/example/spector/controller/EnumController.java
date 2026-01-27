package com.example.spector.controller;

import com.example.spector.domain.enums.dto.EnumDTO;
import com.example.spector.service.EnumService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/main/enums")
@RequiredArgsConstructor
public class EnumController {
    private final EnumService enumService;

    /**
     * GET /api/main/enums/alarm-types
     */
    // Получение значений Enum - Alarm Types
    @GetMapping("/alarm-types")
    public List<EnumDTO> getAlarmTypes() {
        return enumService.getAlarmTypes();
    }

    /**
     * GET /api/main/enums/data-types
     */
    // Получение значений Enum - Data Types
    @GetMapping("/data-types")
    public List<EnumDTO> getDataTypes() {
        return enumService.getDataTypes();
    }
}
