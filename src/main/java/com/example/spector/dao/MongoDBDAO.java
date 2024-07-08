package com.example.spector.dao;

import com.example.spector.domain.Device;
import com.example.spector.domain.DeviceData;
import com.example.spector.service.DeviceDataService;
import lombok.RequiredArgsConstructor;
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
    public void prepareDAO(Device device) {
        deviceDataService.createDeviceDataCollection(device.getName());
    }

    @Override
    public void writeData(Device device, Map<String, Object> snmpData) {
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
        deviceDataService.saveDeviceData(device.getName(), deviceData);
    }
}
