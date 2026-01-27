package com.example.spector.modules.handler;

import com.example.spector.modules.datapattern.ResultValue;
import com.example.spector.domain.setting.dto.AppSettingDTO;
import com.example.spector.domain.device.dto.DeviceDTO;
import com.example.spector.domain.parameter.dto.ParameterDTO;
import com.example.spector.domain.threshold.dto.ThresholdDTO;

import java.util.List;

public interface ParameterHandler {
    ResultValue handleParameter(DeviceDTO deviceDTO, ParameterDTO parameterDTO, Object value,
                                List<ThresholdDTO> thresholds, AppSettingDTO appSettingDTO);
}
