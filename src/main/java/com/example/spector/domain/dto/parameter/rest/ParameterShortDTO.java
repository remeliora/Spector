package com.example.spector.domain.dto.parameter.rest;

import com.example.spector.domain.enums.DataType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@Schema(description = "Краткий DTO параметра")
public class ParameterShortDTO {
    @Schema(description = "Уникальный идентификатор параметра",
            example = "1")
    private Long id;

    @Schema(description = "Наименование параметра",
            example = "hrPhysicalMemoryUsed",
            required = true,
            maxLength = 100)
    private String name;

    @Schema(description = "Описание параметра",
            example = "Количество используемой оперативной памяти",
            maxLength = 500)
    private String description;

    @Schema(description = "Тип данных параметра",
            required = true)
    private DataType dataType;

    @Schema(description = "Словарь перечисления (например, для выбора значений)",
            example = "{\"0\":\"Down\",\"1\":\"Up\"}")
    private Map<Integer, String> enumeration;
}
