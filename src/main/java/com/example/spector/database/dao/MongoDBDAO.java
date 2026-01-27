package com.example.spector.database.dao;

import com.example.spector.database.mongodb.MongoDataService;
import com.example.spector.domain.devicedata.DeviceData;
import com.example.spector.modules.datapattern.ParameterData;
import com.example.spector.domain.device.dto.DeviceDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
@Qualifier("mongoDBDAO")
@RequiredArgsConstructor
public class MongoDBDAO implements DAO {
    private final MongoDataService mongoDataService;

    @Override
    public void prepareDAO(DeviceDTO deviceDTO) {
        mongoDataService.createDeviceDataCollection(deviceDTO.getName());
    }

    @Override
    public void writeData(DeviceDTO deviceDTO, Map<String, Object> snmpData) {
        DeviceData deviceData = new DeviceData();

        //  Устанавливаем данные из snmpData в объект DeviceData
        deviceData.setDeviceId((Long) snmpData.get("deviceId"));
        deviceData.setDeviceName((String) snmpData.get("deviceName"));
        deviceData.setDeviceIp((String) snmpData.get("deviceIp"));
        deviceData.setLocation((String) snmpData.get("location"));
        deviceData.setStatus((String) snmpData.get("status"));
        deviceData.setLastPollingTime((LocalDateTime) snmpData.get("lastPollingTime"));

        // Извлекаем список параметров, который мы сохранили под ключом "parameters"
        List<ParameterData> parameterDataList = (List<ParameterData>) snmpData.get("parameters");
        deviceData.setParameters(parameterDataList);

        //  Вызываем метод сервиса для создания записи в базе данных MongoDB
        mongoDataService.saveDeviceData(deviceDTO.getName(), deviceData);
    }

    @Override
    public Optional<DeviceData> readData(DeviceDTO deviceDTO) {
        return Optional.empty();
    }
}
