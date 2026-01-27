package com.example.spector.domain.parameter.dto;

import com.example.spector.domain.enums.DataType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Schema(description = "DTO для детального представления параметра")
public class ParameterDetailDTO {
    @Schema(description = "Уникальный идентификатор параметра",
            example = "1")
    private Long id;

    @Schema(description = "Наименование параметра",
            example = "hrPhysicalMemoryUsed")
    private String name;

    @Schema(description = "Адрес (OID) параметра",
            example = "1.3.6.1.2.1.25.2.3.1.5.1")
    private String address;

    @Schema(description = "Единица измерения параметра",
            example = "Bytes")
    private String metric;

    @Schema(description = "Аддитивный коэффициент",
            example = "0.0")
    private Double additive;

    @Schema(description = "Множитель",
            example = "1.0")
    private Double coefficient;

    @Schema(description = "Описание параметра",
            example = "Количество используемой оперативной памяти")
    private String description;

    @Schema(description = "Тип данных параметра")
    private DataType dataType;

    @Schema(description = "Список идентификаторов активных устройств",
            example = "[1, 2, 4]")
    private List<Long> activeDevicesId;

    @Schema(description = "Идентификатор словаря статуса",
            example = "1")
    private Long statusDictionaryId;
}
