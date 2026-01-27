package com.example.spector.domain.parameter.dto;

import com.example.spector.domain.enums.DataType;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class ParameterShortDTO {
    private Long id;

    private String name;

    private String description;

    private DataType dataType;

    private Map<Integer, String> enumeration;
}
