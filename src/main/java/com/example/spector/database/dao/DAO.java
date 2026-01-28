package com.example.spector.database.dao;

import com.example.spector.domain.device.Device;
import com.example.spector.domain.devicedata.DeviceData;

import java.util.Map;
import java.util.Optional;

public interface DAO {
    //  Метод проверки наличия файла устройства и его создания
    void prepareDAO(Device device);

    //  Метод записи данных в файл
    void writeData(Device device, Map<String, Object> snmpData);

    Optional<DeviceData> readData(Device device);
}
