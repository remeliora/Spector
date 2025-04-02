package com.example.spector.domain.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

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
    private Boolean isEnumeratedStatus;
    private String description;
    private String dataType;
    private List<ThresholdDTO> thresholdId;
}
