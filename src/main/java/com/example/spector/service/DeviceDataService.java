package com.example.spector.service;

import com.example.spector.domain.DeviceData;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DeviceDataService {
    private final MongoTemplate mongoTemplate;

    public void createDeviceDataCollection(String deviceName) {
        if (!mongoTemplate.collectionExists(deviceName)) {
            mongoTemplate.createCollection(deviceName);
            System.out.println("Collection created: " + deviceName);
        } else {
            System.out.println("Collection already exists: " + deviceName);
        }
    }

    public void saveDeviceData(String deviceName, DeviceData deviceData) {
        mongoTemplate.save(deviceData, deviceName);
        System.out.println("Data written to collection: " + deviceName);
    }
}
