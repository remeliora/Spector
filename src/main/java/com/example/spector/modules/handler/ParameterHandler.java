package com.example.spector.modules.handler;

import com.example.spector.domain.device.Device;
import com.example.spector.domain.parameter.Parameter;
import com.example.spector.domain.setting.AppSetting;
import com.example.spector.domain.threshold.Threshold;
import com.example.spector.modules.datapattern.ResultValue;

import java.util.List;

public interface ParameterHandler {
    ResultValue handleParameter(Device device, Parameter parameter, Object value,
                                List<Threshold> thresholds, AppSetting appSetting);
}
