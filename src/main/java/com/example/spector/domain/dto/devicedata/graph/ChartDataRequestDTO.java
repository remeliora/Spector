package com.example.spector.domain.dto.devicedata.graph;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

@Setter
@Getter
public class ChartDataRequestDTO {
    private List<DeviceParameterRequestDTO> devices;
    private Instant from;
    private Instant to;
}
