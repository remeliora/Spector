package com.example.spector.domain.dto.statusdictionary;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@Schema(description = "Детализированное DTO словаря статусов")
public class StatusDictionaryDetailDTO {
    @Schema(
            description = "ID словаря статусов",
            example = "1",
            required = true)
    private Long id;

    @Schema(
            description = "Наименование словаря статусов",
            example = "upsBatteryStatus",
            required = true,
            maxLength = 100)
    private String name;

    @Schema(
            description = "Значения перечисления (ключ-значение)",
            example = "{\"1\": \"Неизвестно\", \"2\": \"В норме\", \"3\": \"Низкий заряд\", \"4\": \"Разряжены\"}",
            required = true)
    private Map<Integer, String> enumValues;
}
