package com.example.spector.domain.dto.enumeration;

import com.example.spector.domain.enums.DataType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "DTO доступного параметра с типом ENUMERATED")
public class EnumeratedStatusAvailableDTO {
    @Schema(
            description = "Наименование параметра",
            example = "hrPhysicalMemoryUsed",
            required = true,
            maxLength = 100)
    @NotBlank(message = "Parameter is required")
    @Size(max = 100, message = "Name cannot exceed 100 characters")
    private String name;

    @Schema(
            description = "Описание параметра",
            example = "Количество используемой оперативной памяти",
            maxLength = 500)
    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    @Schema(
            description = "Тип данных параметра",
            required = true,
            implementation = DataType.class)
    @NotNull(message = "Data Type is required")
    private DataType dataType;
}
