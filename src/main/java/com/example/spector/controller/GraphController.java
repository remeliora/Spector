package com.example.spector.controller;

import com.example.spector.database.mongodb.HistoricalDataService;
import com.example.spector.domain.device.dto.DeviceWithActiveParametersDTO;
import com.example.spector.domain.devicedata.dto.graph.ChartDataRequestDTO;
import com.example.spector.domain.devicedata.dto.graph.ChartDataResponseDTO;
import com.example.spector.service.GraphService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/main/graphs")
@RequiredArgsConstructor
public class GraphController {
    private final GraphService graphService;
    private final HistoricalDataService historicalDataService;

    /**
     * GET /api/v1/main/graphs/devices-with-active-parameters
     */
    @GetMapping("/devices-with-active-parameters")
    public ResponseEntity<List<DeviceWithActiveParametersDTO>> getDevicesWithActiveParameters() {
        List<DeviceWithActiveParametersDTO> result = graphService.getDevicesWithActiveParameters();
        return ResponseEntity.ok(result);
    }

    @PostMapping("/data") // POST, так как тело запроса может быть большим
    public ResponseEntity<ChartDataResponseDTO> getChartData(@RequestBody ChartDataRequestDTO requestDTO) {
        ChartDataResponseDTO result = historicalDataService.getChartData(requestDTO);
        return ResponseEntity.ok(result);
    }
}
