package com.example.spector.modules.datapattern;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ParameterData {
    private Long id;
    private String name;
    private Object value;
    private String metric;
    private String description;
    private String status;
}
