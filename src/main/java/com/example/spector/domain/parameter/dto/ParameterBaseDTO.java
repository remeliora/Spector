package com.example.spector.domain.parameter.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ParameterBaseDTO {
    private Long id;

    private String name;

    private String address;

    private String metric;

    private String description;
}
