package com.example.spector.database.mongodb;

import com.example.spector.domain.DeviceData;
import com.example.spector.domain.enums.EventType;
import com.example.spector.domain.enums.MessageType;
import com.example.spector.modules.event.EventDispatcher;
import com.example.spector.modules.event.EventMessage;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

@Service
public class DeviceDataService {
    private final MongoTemplate deviceDataMongoTemplate;
    private final EventDispatcher eventDispatcher;

    public DeviceDataService(@Qualifier("databaseDeviceDataMongoTemplate") MongoTemplate deviceDataMongoTemplate,
                             EventDispatcher eventDispatcher) {
        this.deviceDataMongoTemplate = deviceDataMongoTemplate;
        this.eventDispatcher = eventDispatcher;
    }

    public void createDeviceDataCollection(String deviceName) {
        if (!deviceDataMongoTemplate.collectionExists(deviceName)) {
            deviceDataMongoTemplate.createCollection(deviceName);
            eventDispatcher.dispatch(EventMessage.log(EventType.DEVICE, MessageType.INFO,
                    "MongoDB: хранилище " + deviceName + " создано"));
        }
    }

    public void saveDeviceData(String deviceName, DeviceData deviceData) {
        deviceDataMongoTemplate.save(deviceData, deviceName);
        eventDispatcher.dispatch(EventMessage.log(EventType.DEVICE, MessageType.INFO,
                "MongoDB: результаты сохранены"));
    }
}
