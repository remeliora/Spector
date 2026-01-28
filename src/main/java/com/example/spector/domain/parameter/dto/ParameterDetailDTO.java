package com.example.spector.domain.parameter.dto;

import com.example.spector.domain.enums.DataType;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ParameterDetailDTO {
    private Long id;

    private String name;

    private String address;

    private String metric;

    private Double additive;

    private Double coefficient;

    private String description;

    private DataType dataType;

    private List<Long> activeDevicesId;

    private Long statusDictionaryId;
}
