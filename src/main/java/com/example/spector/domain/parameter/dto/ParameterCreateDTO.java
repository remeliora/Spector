package com.example.spector.domain.parameter.dto;

import com.example.spector.domain.enums.DataType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Schema(description = "DTO для создания параметра")
public class ParameterCreateDTO {
    @Schema(description = "Наименование параметра",
            example = "hrPhysicalMemoryUsed",
            required = true,
            maxLength = 100)
    @NotBlank(message = "Parameter is required")
    @Size(max = 100, message = "Name cannot exceed 100 characters")
    private String name;

    @Schema(description = "OID параметра в формате SNMP",
            example = "1.3.6.1.2.1.25.2.3.1.5.1",
            required = true)
    @NotBlank(message = "OID is required")
    @Pattern(regexp = "^\\d+(?:\\.\\d+)*$",
            message = "Invalid OID format (example: '1.3.6.1.2.1.X')")
    private String address;

    @Schema(description = "Единица измерения параметра",
            example = "Bytes",
            maxLength = 10)
    @Size(max = 10, message = "Metric cannot exceed 50 characters")
    private String metric;

    @Schema(description = "Аддитивный коэффициент для преобразования значения",
            example = "0.0")
    private Double additive;

    @Schema(description = "Множитель для преобразования значения",
            example = "1.0")
    private Double coefficient;

    @Schema(description = "Описание параметра",
            example = "Количество используемой оперативной памяти",
            maxLength = 500)
    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    @Schema(description = "Тип данных параметра",
            required = true)
    @NotNull(message = "Data Type is required")
    private DataType dataType;

    @Schema(description = "Список идентификаторов активных устройств",
            example = "[1, 2, 4]")
    private List<Long> activeDevicesId = new ArrayList<>();

    @Schema(description = "Идентификатор словаря статуса",
            example = "1")
    private Long statusDictionaryId;
}
