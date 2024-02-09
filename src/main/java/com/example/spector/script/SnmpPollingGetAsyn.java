    package com.example.spector.script;

    import com.example.spector.domain.Device;
    import com.example.spector.domain.Parameter;
    import com.example.spector.service.DataBaseService;
    import com.fasterxml.jackson.databind.JsonNode;
    import com.fasterxml.jackson.databind.ObjectMapper;
    import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
    import jakarta.transaction.Transactional;
    import org.snmp4j.CommunityTarget;
    import org.snmp4j.PDU;
    import org.snmp4j.Snmp;
    import org.snmp4j.event.ResponseEvent;
    import org.snmp4j.event.ResponseListener;
    import org.snmp4j.mp.SnmpConstants;
    import org.snmp4j.smi.*;
    import org.snmp4j.transport.DefaultUdpTransportMapping;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.stereotype.Component;

    import java.io.File;
    import java.io.IOException;
    import java.nio.file.Files;
    import java.time.Duration;
    import java.time.LocalDateTime;
    import java.time.format.DateTimeFormatter;
    import java.util.HashMap;
    import java.util.List;
    import java.util.Map;
    import java.util.Set;
    import java.util.concurrent.CountDownLatch;
    import java.util.concurrent.TimeUnit;

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
                // Проверка времени прошедшего с последнего цикла опроса
                System.out.println("Device: " + device.getName() + " - Is Time For Polling: " + isTimeForPolling(device));

                if (!isTimeForPolling(device)) {
                    continue;
                }

                // Вызов метода smnpPoll для каждого устройства
                smnpPoll(device);
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
                if (deviceFileName.createNewFile()) {
                    System.out.println("File created: " + deviceFileName);
                } else {
                    System.out.println("File already exists: " + deviceFileName);
                }
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

            // Проверка, что lastPollingTime не равен null
            if (lastPollingTime == null) {
                return true;
            }

            // Период опроса в секундах
            int pollingPeriod = device.getPeriod();

            // Отладочные выводы
            System.out.println("Current Time: " + currentTime);
            System.out.println("Last Polling Time: " + lastPollingTime);
            System.out.println("Polling Period: " + pollingPeriod);

            // Проверка, прошло ли достаточно времени с последнего опроса
            boolean isTimeForPolling = Duration.between(lastPollingTime, currentTime).toSeconds() >= pollingPeriod;
            System.out.println("Is Time For Polling: " + isTimeForPolling);

            return isTimeForPolling;
        }

        @Transactional
        public Map<String, Object> smnpPoll(Device device) {
            String deviceId = device.getId().toString();
            String deviceName = device.getName();
            String deviceIp = device.getIpAddress();

            File deviceFileName = new File("logs/JsonFiles/" + device.getName() + ".json");
            LocalDateTime lastPollingTime = readLastPollingTimeFromFile(deviceFileName);

            // Если lastPollingTime равен null (файл только что создан или не содержит данных),
            // устанавливаем lastPollingTime в текущее время
            if (lastPollingTime == null) {
                lastPollingTime = LocalDateTime.now();
            }

            Set<Parameter> parameters = device.getDeviceType().getParameters();
            Map<String, Object> snmpData = new HashMap<>();

            snmpData.put("deviceId", deviceId);
            snmpData.put("deviceName", deviceName);
            snmpData.put("deviceIp", deviceIp);
            snmpData.put("lastPollingTime", lastPollingTime);

            // Отладочные выводы
            System.out.println("Parameters to Poll: " + parameters.size());

            for (Parameter parameter : parameters) {
                OID oid = new OID(parameter.getAddress());
                PDU pdu = new PDU();
                pdu.add(new VariableBinding(oid));
                pdu.setType(PDU.GET);

                // Отладочный вывод
                System.out.println("Performing SNMP GET for Parameter: " + parameter.getName());

                VariableBinding result = performSnmpGet(deviceIp, pdu);

                // Отладочные выводы
                System.out.println("Parameter Address: " + parameter.getAddress());
                System.out.println("Result Variable: " + result.getVariable());

                snmpData.put(parameter.getAddress(), result.getVariable());
            }

            writeLastPollingTimeToFile(deviceFileName, snmpData);

            return snmpData;
        }

        private VariableBinding performSnmpGet(String deviceIp, PDU pdu) {
            CountDownLatch latch = new CountDownLatch(1);
            VariableBinding result = new VariableBinding();

            try {
                Snmp snmp = new Snmp(new DefaultUdpTransportMapping());
                snmp.listen();

                CommunityTarget target = new CommunityTarget();
                target.setCommunity(new OctetString("public"));
                target.setAddress(new UdpAddress(deviceIp + "/161"));
                target.setVersion(SnmpConstants.version1);
                target.setRetries(3);
                target.setTimeout(1500);

                ResponseListener listener = new ResponseListener() {
                    @Override
                    public <A extends Address> void onResponse(ResponseEvent<A> responseEvent) {
                        ((Snmp) responseEvent.getSource()).cancel(responseEvent.getRequest(), this);
                        PDU response = responseEvent.getResponse();

                        if (response != null && response.getErrorStatus() == PDU.noError) {
                            result.setVariable(response.getVariableBindings().stream().findFirst().get().getVariable());
                        }

                        latch.countDown();
                    }
                };

                snmp.send(pdu, target, null, listener);

                latch.await(3, TimeUnit.SECONDS);

                snmp.close();

            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }

            return result;
        }

        private LocalDateTime readLastPollingTimeFromFile(File deviceFileName) {
            try {
                String content = new String(Files.readAllBytes(deviceFileName.toPath()));

                // Отладочный вывод
                System.out.println("Content read from file: " + content);

                // Проверяем, не пуста ли строка
                if (content.trim().isEmpty()) {
                    // Отладочный вывод
                    System.out.println("Content is empty. Returning null.");
                    return null;    // Возвращаем null, чтобы указать отсутствие данных
                }

                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode rootNode = objectMapper.readTree(content);

                // Извлекаем значение "lastPollingTime" как строку
                String lastPollingTimeString = rootNode.path("lastPollingTime").asText();

                // Проверяем, не пуста ли строка
                if (lastPollingTimeString.trim().isEmpty()) {
                    // Отладочный вывод
                    System.out.println("Last polling time string is empty. Returning null.");
                    return null;
                }

                DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;

                LocalDateTime lastPollingTime = LocalDateTime.parse(lastPollingTimeString, formatter);

                // Отладочный вывод
                System.out.println("Parsed last polling time: " + lastPollingTime);

                return lastPollingTime;

            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Error reading content from file: " + e.getMessage());
            }

            return null;    // Возвращаем null в случае ошибки чтения или парсинга
        }

        private void writeLastPollingTimeToFile(File deviceFileName, Map<String, Object> snmpData) {
            try {
                if (snmpData != null && !snmpData.isEmpty()) {
                    // Создаем ObjectMapper и регистрируем в нем модуль JavaTimeModule
                    ObjectMapper objectMapper = new ObjectMapper();
                    objectMapper.registerModule(new JavaTimeModule());

                    // Преобразование данных в формат JSON
                    String jsonData = objectMapper.writeValueAsString(snmpData);

                    // Запись данных в файл
                    Files.write(deviceFileName.toPath(), jsonData.getBytes());
                } else {
                    System.out.println("SNMP data is null or empty");
                }
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Error writing SNMP data to file: " + e.getMessage());
            }
        }

    }