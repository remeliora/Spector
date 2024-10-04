package com.example.spector.database.mongodb;

import com.example.spector.domain.DeviceData;
import com.example.spector.domain.dto.DeviceDataDTO;
import com.example.spector.mapper.DeviceDataDTOConverter;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DeviceDataService {
    @Qualifier("deviceDataMongoTemplate")
    private final MongoTemplate deviceDataMongoTemplate;
    private final DeviceDataDTOConverter deviceDataDTOConverter;
    private static final Logger deviceLogger = LoggerFactory.getLogger("DeviceLogger");

    public void createDeviceDataCollection(String deviceName) {
        if (!deviceDataMongoTemplate.collectionExists(deviceName)) {
            deviceDataMongoTemplate.createCollection(deviceName);
//            System.out.println("Collection created: " + deviceName);
            deviceLogger.info("Collection created: {}", deviceName);
        }
//        else {
////            System.out.println("Collection already exists: " + deviceName);
////            logger.info("Collection already exists: {}", deviceName);
//        }
    }

    public void saveDeviceData(String deviceName, DeviceData deviceData) {
        deviceDataMongoTemplate.save(deviceData, deviceName);
//        System.out.println("Data written to collection: " + deviceName);
        deviceLogger.info("Data written to collection: {}", deviceName);
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