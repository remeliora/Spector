package com.example.spector.database.dao;

import com.example.spector.domain.Device;
import com.example.spector.domain.dto.DeviceDTO;

import java.util.Map;

public interface DAO {
    //  Метод проверки наличия файла устройства и его создания
    void prepareDAO(DeviceDTO deviceDTO);

    //  Метод записи данных в файл
    void writeData(DeviceDTO deviceDTO, Map<String, Object> snmpData);
}
