package com.example.spector.domain.dto.statusdictionary;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Setter
@Getter
@Schema(description = "DTO для обновления словаря статусов")
public class StatusDictionaryUpdateDTO {
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
    @NotBlank(message = "Name is required")
    @Size(max = 100, message = "Name cannot exceed 100 characters")
    private String name;

    @Schema(
            description = "Значения перечисления (ключ-значение)",
            example = "{\"1\": \"Неизвестно\", \"2\": \"В норме\", \"3\": \"Низкий заряд\", \"4\": \"Разряжены\"}",
            required = true)
    @NotEmpty
    private Map<@NotNull Integer, @NotNull String> enumValues;
}
