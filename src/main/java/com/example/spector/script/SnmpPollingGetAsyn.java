package com.example.spector.script;

import com.example.spector.domain.Device;
import com.example.spector.domain.Parameter;
import com.example.spector.service.DataBaseService;
import org.snmp4j.PDU;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.VariableBinding;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class SnmpPollingGetAsyn {   //Файл скрипта опроса по протоколу SNMP
    @Autowired
    private DataBaseService dataBaseService;

    public void pollDevices() {
        //Вызов списка всех устройств из БД для опроса
        List<Device> devicesToPoll = (List<Device>) dataBaseService.getAllDevices();

        for (Device device : devicesToPoll) {
            String deviceName = device.getName();

            //Проверка на наличие JSON-файла устройства
            if (!doesJsonFileExist(deviceName)) {
                createJsonFile(deviceName);
            }
            //Проверка включенности устройства
            if (!device.getIsEnable()) {
                continue;
            }
            //Проверка времени прошедшего с последнего цикла опроса
            if (!isTimeForPolling(device)) {
                continue;
            }


        }
    }

    //Метод проверки наличия JSON-файла устройства
    private boolean doesJsonFileExist(String deviceName) {
        File deviceFileName = new File("logs/JsonFiles/" + deviceName + ".json");

        return deviceFileName.exists();
    }

    //Метод создания JSON-файла устройства
    private void createJsonFile(String deviceName) {
        File deviceFileName = new File("logs/JsonFiles/" + deviceName + ".json");

        try {
            deviceFileName.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Метод проверки времени прошедшего с последнего цикла опроса
    private boolean isTimeForPolling(Device device) {
        File deviceFileName = new File("logs/JsonFiles/" + device.getName() + ".json");

        // Если файл не существует, или устройство выключено, просто возвращаем true для начала опроса
        if (!deviceFileName.exists() || !device.getIsEnable()) {
            return true;
        }

        // Файл существует, устройство включено, проверяем время последнего опроса
        LocalDateTime currentTime = LocalDateTime.now();
        LocalDateTime lastPollingTime = readLastPollingTimeFromFile(deviceFileName);

        // Период опроса в секундах (или другой удобной временной единице)
        int pollingPeriod = device.getPeriod();

        // Проверка, прошло ли достаточно времени с последнего опроса
        return Duration.between(lastPollingTime, currentTime).toSeconds() >= pollingPeriod;
    }

    private Map<String, Object> smmpPoll(Device device) {
        String deviceId = device.getId().toString();
        String deviceName = device.getName();
        String deviceIp = device.getIpAddress();

        LocalDateTime lastPollingTime = LocalDateTime.now();
        File deviceFileName = new File("logs/JsonFiles/" + device.getName() + ".json");
        if (deviceFileName.exists()) {
            lastPollingTime = readLastPollingTimeFromFile(deviceFileName);
        }

        List<Parameter> parameters = (List<Parameter>) device.getDeviceType().getParameters();
        Map<String, Object> snmpData = new HashMap<>();

        snmpData.put("deviceId", deviceId);
        snmpData.put("deviceName", deviceName);
        snmpData.put("deviceIp", deviceIp);
        snmpData.put("lastPollingTime", lastPollingTime);

        for (Parameter parameter : parameters) {
            OID oid = new OID(parameter.getAddress());
            PDU pdu = new PDU();
            pdu.add(new VariableBinding(oid));
            pdu.setType(PDU.GET);

            VariableBinding result = performSnmpGet(deviceIp, pdu);

            snmpData.put(parameter.getAddress(), result.getVariable());
        }

        writeLastPollingTimeToFile(deviceFileName, lastPollingTime);

        return snmpData;
    }

    private VariableBinding performSnmpGet(String deviceIp, PDU pdu) {
        return null;
    }

    private LocalDateTime readLastPollingTimeFromFile(File deviceFileName) {
        try {
            String content = new String(Files.readAllBytes(deviceFileName.toPath()));

            // Проверяем, не пуста ли строка
            if (content.trim().isEmpty()) {
                return null;    // Возвращаем null, чтобы указать отсутствие данных
            }

            DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;

            return LocalDateTime.parse(content, formatter);

        } catch (IOException | DateTimeParseException e) {
            e.printStackTrace();
            return null;    // Возвращаем null в случае ошибки чтения или парсинга
        }
    }
    private void writeLastPollingTimeToFile(File deviceFileName, LocalDateTime lastPollingTime) {
        try {
            String formattedTime = lastPollingTime.format(DateTimeFormatter.ISO_DATE_TIME);

            // Проверяем, не пуста ли строка
            if (!formattedTime.trim().isEmpty()) {
                Files.write(deviceFileName.toPath(), formattedTime.getBytes());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
