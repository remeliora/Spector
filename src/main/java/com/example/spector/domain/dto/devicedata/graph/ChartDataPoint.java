package com.example.spector.domain.dto.devicedata.graph;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ChartDataPoint {
    private Long timestamp;
    private Object value;
    private Long parameterId;

    public ChartDataPoint(Long timestamp, Object value, Long parameterId) {
        this.timestamp = timestamp;
        this.value = value;
        this.parameterId = parameterId;
    }
}
