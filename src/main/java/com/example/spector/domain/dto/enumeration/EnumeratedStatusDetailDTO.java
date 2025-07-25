package com.example.spector.domain.dto.enumeration;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@Schema(description = "Детализированное DTO перечисляемого статуса")
public class EnumeratedStatusDetailDTO {
    @Schema(
            description = "Наименование перечисляемого статуса",
            example = "upsBatteryStatus",
            required = true,
            maxLength = 100)
    @NotBlank(message = "Enumerated Status is required")
    @Size(max = 100, message = "Name cannot exceed 100 characters")
    private String name;

    @Schema(
            description = "Значения перечисления (ключ-значение)",
            example = "{\"1\": \"Неизвестно\", \"2\": \"В норме\", \"3\": \"Низкий заряд\", \"4\": \"Разряжены\"}",
            required = true)
    @NotEmpty
    private Map<@NotNull Integer, @NotNull String> enumValues;
}
