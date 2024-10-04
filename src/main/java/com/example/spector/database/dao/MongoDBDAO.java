package com.example.spector.database.dao;

import com.example.spector.domain.DeviceData;
import com.example.spector.domain.dto.DeviceDTO;
import com.example.spector.database.mongodb.DeviceDataService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Component
@Qualifier("mongoDBDAO")
@RequiredArgsConstructor
public class MongoDBDAO implements DAO {
    private final DeviceDataService deviceDataService;

    @Override
    public void prepareDAO(DeviceDTO deviceDTO) {
        deviceDataService.createDeviceDataCollection(deviceDTO.getName());
    }

    @Override
    public void writeData(DeviceDTO deviceDTO, Map<String, Object> snmpData) {
        DeviceData deviceData = new DeviceData();

        //  Устанавливаем данные из snmpData в объект DeviceData
        deviceData.setDeviceId((Long) snmpData.get("deviceId"));
        deviceData.setDeviceName((String) snmpData.get("deviceName"));
        deviceData.setDeviceIp((String) snmpData.get("deviceIp"));
        deviceData.setLastPollingTime((LocalDateTime) snmpData.get("lastPollingTime"));

        Map<String, Object> parameters = new HashMap<>(snmpData);
        parameters.remove("deviceId");
        parameters.remove("deviceName");
        parameters.remove("deviceIp");
        parameters.remove("lastPollingTime");

        deviceData.setParameters(parameters);

        //  Вызываем метод сервиса для создания записи в базе данных MongoDB
        deviceDataService.saveDeviceData(deviceDTO.getName(), deviceData);
    }
}
