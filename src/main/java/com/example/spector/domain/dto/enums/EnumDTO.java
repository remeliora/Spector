package com.example.spector.domain.dto.enums;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
@Schema(description = "DTO для представления элементов перечисления (enum)")
public class EnumDTO {
    @Schema(
            description = "Техническое имя значения enum",
            required = true)
    private String name;

    @Schema(
            description = "Отображаемое имя значения enum",
            required = true)
    private String displayName;

    public EnumDTO(String name, String displayName) {
        this.name = name;
        this.displayName = displayName;
    }
}


