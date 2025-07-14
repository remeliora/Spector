package com.example.spector.controller.rest;

import com.example.spector.domain.dto.enums.EnumDTO;
import com.example.spector.service.EnumService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/main/enums")
@RequiredArgsConstructor
public class EnumController {
    private final EnumService enumService;

    @GetMapping("/alarm-types")
    public List<EnumDTO> getAlarmTypes() {
        return enumService.getAlarmTypes();
    }

    @GetMapping("/data-types")
    public List<EnumDTO> getDataTypes() {
        return enumService.getDataTypes();
    }
}
