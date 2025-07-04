package com.example.spector.database.dao;

import com.example.spector.domain.DeviceData;
import com.example.spector.domain.dto.device.DeviceDTO;

import java.util.Map;
import java.util.Optional;

public interface DAO {
    //  Метод проверки наличия файла устройства и его создания
    void prepareDAO(DeviceDTO deviceDTO);

    //  Метод записи данных в файл
    void writeData(DeviceDTO deviceDTO, Map<String, Object> snmpData);

    Optional<DeviceData> readData(DeviceDTO deviceDTO);
}
