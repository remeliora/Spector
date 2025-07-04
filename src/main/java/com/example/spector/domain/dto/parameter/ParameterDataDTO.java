package com.example.spector.domain.dto.parameter;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ParameterDataDTO {
    private Long id;
    private String name;
    private Object value;
    private String metric;
    private String description;
    private String status;
}