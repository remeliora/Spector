package com.example.spector.handler;

import com.example.spector.domain.dto.DeviceDTO;
import com.example.spector.domain.dto.ParameterDTO;
import com.example.spector.domain.dto.ThresholdDTO;

import java.util.List;

public interface ParameterHandler {
    Object handleParameter(DeviceDTO deviceDTO, ParameterDTO parameterDTO, Object value, List<ThresholdDTO> thresholds);
}
