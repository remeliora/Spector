package com.example.spector.repositories.data;

import com.example.spector.domain.DeviceData;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface DeviceDataRepository extends MongoRepository<DeviceData, String> {

    List<DeviceData> findByDeviceName(String deviceName);

    //  Метод для проверки существования записи по имени устройства
    boolean existsByDeviceName(String deviceName);
}
