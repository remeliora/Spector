package com.example.spector.database.dao;

import com.example.spector.domain.dto.DeviceDTO;
import com.example.spector.domain.enums.EventType;
import com.example.spector.domain.enums.MessageType;
import com.example.spector.event.EventDispatcher;
import com.example.spector.event.EventMessage;
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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

@Component
@Qualifier("jsonDAO")
@RequiredArgsConstructor
public class JsonDAO implements DAO {
    private final EventDispatcher eventDispatcher;
    private static final Logger logger = LoggerFactory.getLogger(JsonDAO.class);
    private static final Logger deviceLogger = LoggerFactory.getLogger("DeviceLogger");

    //  Метод проверки наличия JSON-файла устройства и его создания
    @Override
    public void prepareDAO(DeviceDTO deviceDTO) {
//        MDC.put("deviceName", deviceDTO.getName());

        Path dirPath = Paths.get("logs/JsonFiles");
        try {
            Files.createDirectories(dirPath);
        } catch (IOException e) {
//            e.printStackTrace();
//            logger.error("Ошибка при создании директории для JSON-файлов", e);
            eventDispatcher.dispatch(EventMessage.log(EventType.SYSTEM, MessageType.ERROR,
                    "Ошибка при создании директории для JSON-файлов: " + e));

            return;
        }

//        String filePath = "logs/JsonFiles/" + deviceDTO.getName() + ".json";
        Path filePath = dirPath.resolve(deviceDTO.getName() + ".json");
        File deviceFileName = filePath.toFile();

        try {
            if (deviceFileName.createNewFile()) {
//                System.out.println("File created: " + deviceFileName);
//                logger.info("File created: {}", deviceFileName);
//                deviceLogger.info("File created: {}", deviceFileName);
                eventDispatcher.dispatch(EventMessage.log(EventType.DEVICE, MessageType.INFO,
                        "Файл создан: " + deviceFileName));
            }
        } catch (IOException e) {
//            e.printStackTrace();
//            logger.error("Failed to create or check JSON file: {}", filePath, e);
            eventDispatcher.dispatch(EventMessage.log(EventType.SYSTEM, MessageType.ERROR,
                    "Ошибка при создании/проверке JSON-файла: " + e));
        }
    }

    //  Метод записи данных в Json-файл устройства
    @Override
    public void writeData(DeviceDTO deviceDTO, Map<String, Object> snmpData) {
        Path filePath = Paths.get("logs/JsonFiles", deviceDTO.getName() + ".json");
        File deviceFileName = filePath.toFile();

        if (snmpData == null && snmpData.isEmpty()) {
//            logger.error("SNMP data is null or empty for device: {}", deviceDTO.getName());
            eventDispatcher.dispatch(EventMessage.log(EventType.SYSTEM, MessageType.ERROR,
                    "Ошибка: данные SNMP отсутствуют для: " + deviceDTO.getName()));

            return;
        }
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

            // Преобразование данных в формат JSON
            String jsonData = objectMapper.writeValueAsString(snmpData);

            // Запись данных в файл
            Files.writeString(deviceFileName.toPath(), jsonData);
        } catch (IOException e) {
//            e.printStackTrace();
//            System.out.println("Error writing SNMP data to file: " + e.getMessage());
//            logger.error("Error writing SNMP data to file: {}", e.getMessage());
            eventDispatcher.dispatch(EventMessage.log(EventType.SYSTEM, MessageType.ERROR,
                    "Ошибка при записи данных SNMP в файл: " + e.getMessage()));
        }
    }
}
