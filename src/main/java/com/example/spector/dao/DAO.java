package com.example.spector.dao;

import com.example.spector.domain.Device;

import java.util.Map;

public interface DAO {
    //  Метод проверки наличия файла устройства и его создания
    void preparingData(Device device);

    //  Метод записи данных в файл
    void writeData(Device device, Map<String, Object> snmpData);
}
