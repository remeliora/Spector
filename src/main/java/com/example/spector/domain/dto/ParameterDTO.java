package com.example.spector.domain.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class ParameterDTO {
    private Long id;
    private String name;
    private String address;
    private DeviceTypeDTO deviceType;
    private String metric;
    private Double additive;
    private Double coefficient;
    private String description;
    private String dataType;
    private Set<Long> thresholdId;
}
