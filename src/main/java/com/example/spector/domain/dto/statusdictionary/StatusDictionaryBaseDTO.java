package com.example.spector.domain.dto.statusdictionary;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
@Schema(description = "Базовое DTO словаря статусов")
public class StatusDictionaryBaseDTO {
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
            description = "Количество элементов в словаре",
            example = "4",
            required = true)
    private Integer count;
}
