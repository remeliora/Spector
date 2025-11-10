package com.example.spector.controller;

import com.example.spector.database.mongodb.HistoricalDataService;
import com.example.spector.domain.dto.device.rest.DeviceWithActiveParametersDTO;
import com.example.spector.domain.dto.devicedata.graph.ChartDataRequestDTO;
import com.example.spector.domain.dto.devicedata.graph.ChartDataResponseDTO;
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
@Tag(name = "Graph Data", description = "API для получения данных, необходимых для построения графиков")
public class GraphController {
    private final GraphService graphService;
    private final HistoricalDataService historicalDataService;

    /**
     * GET /api/v1/main/graphs/devices-with-active-parameters
     */
    @Operation(
            summary = "Получить список устройств с их активными параметрами",
            description = "Возвращает список устройств, каждый из которых содержит список его активных параметров. " +
                          "Используется для выбора параметров для отображения на графиках."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Список устройств с активными параметрами успешно получен",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = DeviceWithActiveParametersDTO.class)
                    )
            )
    })
    @GetMapping("/devices-with-active-parameters")
    public ResponseEntity<List<DeviceWithActiveParametersDTO>> getDevicesWithActiveParameters() {
        List<DeviceWithActiveParametersDTO> result = graphService.getDevicesWithActiveParameters();
        return ResponseEntity.ok(result);
    }

    @Operation(
            summary = "Получить исторические данные для графиков",
            description = "Возвращает исторические данные для указанных параметров устройств за заданный период. Временные метки преобразуются в +10:00."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Исторические данные успешно получены",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ChartDataResponseDTO.class)
                    )
            )
    })
    @PostMapping("/data") // POST, так как тело запроса может быть большим
    public ResponseEntity<ChartDataResponseDTO> getChartData(@RequestBody ChartDataRequestDTO requestDTO) {
        ChartDataResponseDTO result = historicalDataService.getChartData(requestDTO);
        return ResponseEntity.ok(result);
    }
}
