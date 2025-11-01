package com.example.spector.modules.handler;

import com.example.spector.modules.datapattern.ResultValue;
import com.example.spector.domain.dto.appsetting.AppSettingDTO;
import com.example.spector.domain.dto.device.DeviceDTO;
import com.example.spector.domain.dto.parameter.ParameterDTO;
import com.example.spector.domain.dto.threshold.ThresholdDTO;

import java.util.List;

public interface ParameterHandler {
    ResultValue handleParameter(DeviceDTO deviceDTO, ParameterDTO parameterDTO, Object value,
                                List<ThresholdDTO> thresholds, AppSettingDTO appSettingDTO);
}
