package com.example.spector.service;

import com.example.spector.domain.dto.enums.EnumDTO;
import com.example.spector.domain.enums.AlarmType;
import com.example.spector.domain.enums.DataType;
import com.example.spector.mapper.BaseDTOConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EnumService {
    private final BaseDTOConverter baseDTOConverter;

    public List<EnumDTO> getAlarmTypes() {
        return Arrays.stream(AlarmType.values())
                .map(e -> new EnumDTO(e.name(), e.getDisplayName()))
                .collect(Collectors.toList());
    }

    public List<EnumDTO> getDataTypes() {
        return Arrays.stream(DataType.values())
                .map(e -> new EnumDTO(e.name(), e.getDisplayName()))
                .collect(Collectors.toList());
    }
}
