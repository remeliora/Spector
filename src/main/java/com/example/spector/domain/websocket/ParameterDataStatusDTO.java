package com.example.spector.domain.websocket;

import com.example.spector.domain.parameter.dto.ParameterDataDTO;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class ParameterDataStatusDTO {
    private Long deviceId;
    private String deviceName;
    private String status;
    private List<ParameterDataDTO> parameters;
}
