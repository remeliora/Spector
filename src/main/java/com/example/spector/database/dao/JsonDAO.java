package com.example.spector.database.dao;

import com.example.spector.domain.dto.DeviceDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;
@Component
@Qualifier("jsonDAO")
@RequiredArgsConstructor
public class JsonDAO implements DAO {
    private final ObjectMapper objectMapper;
    private static final Logger logger = LoggerFactory.getLogger(JsonDAO.class);
    private static final Logger deviceLogger = LoggerFactory.getLogger("DeviceLogger");

    //  Метод проверки наличия JSON-файла устройства и его создания
    @Override
    public void prepareDAO(DeviceDTO deviceDTO) {
        MDC.put("deviceName", deviceDTO.getName());

        String filePath = "logs/JsonFiles/" + deviceDTO.getName() + ".json";
        File deviceFileName = new File(filePath);

        try {
            if (deviceFileName.createNewFile()) {
//                System.out.println("File created: " + deviceFileName);
//                logger.info("File created: {}", deviceFileName);
                deviceLogger.info("File created: {}", deviceFileName);
            } else {
//                System.out.println("File already exists: " + deviceFileName);
//                logger.info("File already exists: {}", deviceFileName);
//                deviceLogger.info("File already exists: {}", deviceFileName);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //  Метод записи данных в Json-файл устройства
    @Override
    public void writeData(DeviceDTO deviceDTO, Map<String, Object> snmpData) {
        File deviceFileName = new File("logs/JsonFiles/" + deviceDTO.getName() + ".json");
        try {
            if (snmpData != null && !snmpData.isEmpty()) {
                // Создаем ObjectMapper и регистрируем в нем модуль JavaTimeModule
                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.registerModule(new JavaTimeModule());
                objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

                // Преобразование данных в формат JSON
                String jsonData = objectMapper.writeValueAsString(snmpData);

                // Запись данных в файл
                Files.write(deviceFileName.toPath(), jsonData.getBytes());
            } else {
//                System.out.println("SNMP data is null or empty");
                logger.error("SNMP data is null or empty");
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error writing SNMP data to file: " + e.getMessage());
            logger.error("Error writing SNMP data to file: {}", e.getMessage());
        }
    }
}
