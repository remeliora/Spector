package com.example.spector.modules.datapattern;

import com.example.spector.domain.device.Device;
import com.example.spector.domain.parameter.Parameter;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class BaseSNMPData {
    public Map<String, Object> defaultSNMPDeviceData(Device device) {
        Map<String, Object> snmpData = new ConcurrentHashMap<>();
        snmpData.put("deviceId", device.getId());
        snmpData.put("deviceName", device.getName());
        snmpData.put("deviceIp", device.getIpAddress());
        snmpData.put("location", device.getLocation());
        snmpData.put("lastPollingTime", LocalDateTime.now());

        return snmpData;
    }

    public ParameterData defaultSNMPParameterData(Parameter parameter) {
        ParameterData parameterData = new ParameterData();
        parameterData.setId(parameter.getId());
        parameterData.setName(parameter.getName());
        parameterData.setDescription(parameter.getDescription());
        parameterData.setMetric(parameter.getMetric());

        return parameterData;
    }
}
