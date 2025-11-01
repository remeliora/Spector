package com.example.spector.modules.datapattern;

import com.example.spector.domain.dto.device.DeviceDTO;
import com.example.spector.domain.dto.parameter.ParameterDTO;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class BaseSNMPData {
    public Map<String, Object> defaultSNMPDeviceData(DeviceDTO deviceDTO) {
        Map<String, Object> snmpData = new ConcurrentHashMap<>();
        snmpData.put("deviceId", deviceDTO.getId());
        snmpData.put("deviceName", deviceDTO.getName());
        snmpData.put("deviceIp", deviceDTO.getIpAddress());
        snmpData.put("location", deviceDTO.getLocation());
        snmpData.put("lastPollingTime", LocalDateTime.now());

        return snmpData;
    }

    public ParameterData defaultSNMPParameterData(ParameterDTO parameterDTO) {
        ParameterData parameterData = new ParameterData();
        parameterData.setId(parameterDTO.getId());
        parameterData.setName(parameterDTO.getName());
        parameterData.setDescription(parameterDTO.getDescription());
        parameterData.setMetric(parameterDTO.getMetric());

        return parameterData;
    }
}
