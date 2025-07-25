package com.example.spector.domain.dto.enumeration;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
@Schema(description = "Базовое DTO перечисляемого статуса")
public class EnumeratedStatusBaseDTO {
    @Schema(
            description = "Наименование перечисляемого статуса",
            example = "upsBatteryStatus",
            required = true,
            maxLength = 100)
    @NotBlank(message = "Enumerated Status is required")
    @Size(max = 100, message = "Name cannot exceed 100 characters")
    private String name;

    @Schema(
            description = "Количество элементов в словаре",
            example = "4",
            required = true)
    @NotNull(message = "Count is required")
    private Integer count;
}
