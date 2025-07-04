package com.example.spector.domain.websocket;

import com.example.spector.domain.dto.parameter.ParameterDataDTO;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class DeviceDetailMessage {
    private Long deviceId;
    private List<ParameterDataDTO> parameters;
}
