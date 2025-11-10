package com.example.spector.domain.dto.devicedata.graph;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class ChartDataResponseDTO {
    private List<SeriesDataDTO> series;
}
