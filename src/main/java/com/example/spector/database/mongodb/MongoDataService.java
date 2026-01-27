package com.example.spector.database.mongodb;

import com.example.spector.domain.devicedata.DeviceData;
import com.example.spector.domain.enums.EventType;
import com.example.spector.domain.enums.MessageType;
import com.example.spector.modules.event.EventDispatcher;
import com.example.spector.modules.event.EventMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MongoDataService {
    private final MongoTemplate deviceDataMongoTemplate;
    private final EventDispatcher eventDispatcher;

    public void createDeviceDataCollection(String deviceName) {
        if (!deviceDataMongoTemplate.collectionExists(deviceName)) {
            deviceDataMongoTemplate.createCollection(deviceName);
            eventDispatcher.dispatch(EventMessage.log(EventType.DEVICE, MessageType.INFO,
                    "MongoDB: хранилище " + deviceName + " создано"));
        }
    }

    public void saveDeviceData(String deviceName, DeviceData deviceData) {
        deviceDataMongoTemplate.save(deviceData, deviceName);
//        eventDispatcher.dispatch(EventMessage.log(EventType.DEVICE, MessageType.INFO,
//                "MongoDB: результаты сохранены"));
    }
}
