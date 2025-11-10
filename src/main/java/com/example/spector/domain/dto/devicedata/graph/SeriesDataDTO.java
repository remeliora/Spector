package com.example.spector.domain.dto.devicedata.graph;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class SeriesDataDTO {
    private Long deviceId;
    private String deviceName;
    private Long parameterId;
    private String parameterName;
    private List<List<Object>> data;
}
