package com.example.spector.database.mongodb;

import com.example.spector.domain.DeviceData;
import com.example.spector.domain.dto.DeviceDataDTO;
import com.example.spector.domain.enums.EventType;
import com.example.spector.domain.enums.MessageType;
import com.example.spector.mapper.DeviceDataDTOConverter;
import com.example.spector.modules.event.EventDispatcher;
import com.example.spector.modules.event.EventMessage;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class DeviceDataService {
    private final MongoTemplate deviceDataMongoTemplate;
    private final DeviceDataDTOConverter deviceDataDTOConverter;
    private final EventDispatcher eventDispatcher;

    public DeviceDataService(@Qualifier("databaseDeviceDataMongoTemplate") MongoTemplate deviceDataMongoTemplate,
                             DeviceDataDTOConverter deviceDataDTOConverter, EventDispatcher eventDispatcher) {
        this.deviceDataMongoTemplate = deviceDataMongoTemplate;
        this.deviceDataDTOConverter = deviceDataDTOConverter;
        this.eventDispatcher = eventDispatcher;
    }

    public void createDeviceDataCollection(String deviceName) {
        if (!deviceDataMongoTemplate.collectionExists(deviceName)) {
            deviceDataMongoTemplate.createCollection(deviceName);
            eventDispatcher.dispatch(EventMessage.log(EventType.DEVICE, MessageType.INFO,
                    "Хранилище " + deviceName  + " создано"));
        }
    }

    public void saveDeviceData(String deviceName, DeviceData deviceData) {
        deviceDataMongoTemplate.save(deviceData, deviceName);
        eventDispatcher.dispatch(EventMessage.log(EventType.DEVICE, MessageType.INFO,
                "Данные сохранены"));
    }

    public List<DeviceDataDTO> getParametersByDeviceName(String deviceName) {
        List<DeviceData> deviceDataList = deviceDataMongoTemplate.findAll(DeviceData.class, deviceName);
//        logger.log(Level.INFO, "Found {} device data entries for device name {}" + deviceDataList.size(), deviceName);
        List<DeviceDataDTO> deviceDataDTOList = new ArrayList<>();

        for (DeviceData deviceData : deviceDataList) {
            if (deviceData.getParameters() != null) {
                DeviceDataDTO deviceDataDTO = deviceDataDTOConverter.convertToDTO(deviceData);
                deviceDataDTOList.add(deviceDataDTO);
            }
        }

//        logger.log(Level.INFO, "Returning {} device data DTOs" + deviceDataDTOList.size());
        return deviceDataDTOList;
    }
}
