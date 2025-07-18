package com.example.spector.database.dao;

import com.example.spector.domain.DeviceData;
import com.example.spector.domain.dto.device.DeviceDTO;
import com.example.spector.domain.enums.EventType;
import com.example.spector.domain.enums.MessageType;
import com.example.spector.modules.event.EventDispatcher;
import com.example.spector.modules.event.EventMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Optional;

@Component
@Qualifier("jsonDAO")
@RequiredArgsConstructor
public class JsonDAO implements DAO {
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
            .enable(SerializationFeature.INDENT_OUTPUT);
    private final EventDispatcher eventDispatcher;

    //  Метод проверки наличия JSON-файла устройства и его создания
    @Override
    public void prepareDAO(DeviceDTO deviceDTO) {
        Path dirPath = Paths.get("data/JSON/devices");
        try {
            Files.createDirectories(dirPath);
        } catch (IOException e) {
            eventDispatcher.dispatch(EventMessage.log(EventType.SYSTEM, MessageType.ERROR,
                    "Ошибка при создании директории для JSON-файлов: " + e));

            return;
        }

        Path filePath = dirPath.resolve(deviceDTO.getName() + ".json");
        File deviceFileName = filePath.toFile();

        try {
            if (deviceFileName.createNewFile()) {
                eventDispatcher.dispatch(EventMessage.log(EventType.DEVICE, MessageType.INFO,
                        "Файл создан: " + deviceFileName));
            }
        } catch (IOException e) {
            eventDispatcher.dispatch(EventMessage.log(EventType.SYSTEM, MessageType.ERROR,
                    "Ошибка при создании/проверке JSON-файла: " + e));
        }
    }

    //  Метод записи данных в Json-файл устройства
    @Override
    public void writeData(DeviceDTO deviceDTO, Map<String, Object> snmpData) {
        Path filePath = Paths.get("data/JSON/devices", deviceDTO.getName() + ".json");
        File deviceFileName = filePath.toFile();

        if (snmpData == null || snmpData.isEmpty()) {
            eventDispatcher.dispatch(EventMessage.log(EventType.SYSTEM, MessageType.ERROR,
                    "Ошибка: данные SNMP отсутствуют для: " + deviceDTO.getName()));

            return;
        }

        try {
            // Преобразование данных в формат JSON
            String jsonData = objectMapper.writeValueAsString(snmpData);

            // Запись данных в файл
            Files.writeString(deviceFileName.toPath(), jsonData);
        } catch (IOException e) {
            eventDispatcher.dispatch(EventMessage.log(EventType.SYSTEM, MessageType.ERROR,
                    "Ошибка при записи данных SNMP в файл: " + e.getMessage()));
        }
    }

    @Override
    public Optional<DeviceData> readData(DeviceDTO deviceDTO) {
        Path filePath = Paths.get("data/JSON/devices", deviceDTO.getName() + ".json");
        if (!Files.exists(filePath)) {
            return Optional.empty();
        }

        try {
            DeviceData deviceData = objectMapper.readValue(filePath.toFile(), DeviceData.class);

            return Optional.of(deviceData);
        } catch (IOException e) {
            eventDispatcher.dispatch(EventMessage.log(EventType.SYSTEM, MessageType.ERROR,
                    "Ошибка при чтении JSON-файла для " + deviceDTO.getName() + ": " + e.getMessage()));

            return Optional.empty();
        }
    }
}
