package com.example.spector.service;

import com.example.spector.domain.DeviceData;
import com.example.spector.domain.dto.DeviceDataDTO;
import com.example.spector.mapper.DeviceDataDTOConverter;
import com.example.spector.repositories.DeviceDataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@Service
@RequiredArgsConstructor
public class DeviceDataService {
    private final MongoTemplate mongoTemplate;
    private final DeviceDataRepository deviceDataRepository;
    private final DeviceDataDTOConverter deviceDataDTOConverter;

    private static final Logger logger = Logger.getLogger(ThresholdService.class.getName());

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

    public List<DeviceDataDTO> getParametersByDeviceName(String deviceName) {
        List<DeviceData> deviceDataList = mongoTemplate.findAll(DeviceData.class, deviceName);
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
