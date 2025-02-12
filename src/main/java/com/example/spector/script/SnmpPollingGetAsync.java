package com.example.spector.script;

import com.example.spector.checker.device.DeviceConnectionChecker;
import com.example.spector.checker.threshold.ThresholdChecker;
import com.example.spector.checker.threshold.ThresholdCheckerFactory;
import com.example.spector.converter.TypeCaster;
import com.example.spector.converter.TypeCasterFactory;
import com.example.spector.database.dao.DAOService;
import com.example.spector.database.mongodb.EnumeratedStatusService;
import com.example.spector.database.postgres.DataBaseService;
import com.example.spector.domain.dto.DeviceDTO;
import com.example.spector.domain.dto.DeviceTypeDTO;
import com.example.spector.domain.dto.ParameterDTO;
import com.example.spector.domain.dto.ThresholdDTO;
import com.example.spector.domain.enums.DataType;
import com.example.spector.domain.enums.EventType;
import com.example.spector.domain.enums.MessageType;
import com.example.spector.event.EventDispatcher;
import com.example.spector.event.EventMessage;
import com.example.spector.snmp.SNMPService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.Variable;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.*;

@Component
@RequiredArgsConstructor
public class SnmpPollingGetAsync {   // Класс скрипта опроса по протоколу SNMP
    private final DataBaseService dataBaseService;
    private final DAOService daoService;
    private final EnumeratedStatusService enumeratedStatusService;
    private final DeviceConnectionChecker deviceConnectionChecker;
    private final SNMPService snmpService;
    private final EventDispatcher eventDispatcher;
    private final ConcurrentMap<Long, LocalDateTime> schedule = new ConcurrentHashMap<>();
//    private static final Logger logger = LoggerFactory.getLogger(SnmpPollingGetAsync.class);
//    private static final Logger deviceLogger = LoggerFactory.getLogger("DeviceLogger");


    @Transactional
    public void pollDevices() {
        List<DeviceDTO> deviceDTOList = dataBaseService.getDeviceDTOByIsEnableTrue();

        eventDispatcher.dispatch(EventMessage.log(EventType.SYSTEM, MessageType.INFO,
                "Кол-во устройств: " + deviceDTOList.size()));
        eventDispatcher.dispatch(EventMessage.log(EventType.SYSTEM, MessageType.INFO,
                "Ожидание завершения всех задач опроса..."));

        deviceDTOList.forEach(this::pollDeviceAsync);

        // Ожидание завершения всех задач
        eventDispatcher.dispatch(EventMessage.log(EventType.SYSTEM, MessageType.INFO,
                "Задачи опроса проведены для всех устройств."));
    }

    @Async("taskExecutor")
    public CompletableFuture<Void> pollDeviceAsync(DeviceDTO deviceDTO) {
        // Проверка на наличие файла устройства и его создание
        MDC.put("deviceName", deviceDTO.getName());
        long startTime = System.currentTimeMillis();
        daoService.prepareDAO(deviceDTO);
        try {
            retryPollDevice(deviceDTO); // Добавляем ретраи с задержками
        } catch (IOException | TimeoutException e) {
            eventDispatcher.dispatch(EventMessage.log(EventType.SYSTEM, MessageType.ERROR,
                    "IOException | TimeoutException во время опроса" + deviceDTO.getName() + ": " + e));
        } finally {
            long endTime = System.currentTimeMillis();
            eventDispatcher.dispatch(EventMessage.log(EventType.DEVICE, MessageType.INFO,
                    "Опрос " + deviceDTO.getName() + " занял " + (endTime - startTime) + " мс."));
            MDC.clear();
        }

        return CompletableFuture.completedFuture(null);
    }

    // Механизм ретраев
    @Retryable(
            value = { IOException.class, TimeoutException.class },
            backoff = @Backoff(delay = 1000, multiplier = 2))  // Ретрай с задержкой в секундах
    public void retryPollDevice(DeviceDTO deviceDTO) throws IOException, TimeoutException {
        if (isReadyToPoll(deviceDTO)) {
            if (deviceConnectionChecker.isAvailableByIP(deviceDTO.getIpAddress())) {
                Map<String, Object> snmpData = snmpPoll(deviceDTO);
                daoService.writeData(deviceDTO, snmpData);
            } else {
//                logger.error("Устройство {} не доступно. Пропуск...", deviceDTO.getName());
                eventDispatcher.dispatch(EventMessage.log(EventType.SYSTEM, MessageType.ERROR,
                        "Устройство " + deviceDTO.getName() + " не доступно. Пропуск..."));

//                deviceLogger.error("Устройство {} не доступно. Пропуск...", deviceDTO.getName());
                eventDispatcher.dispatch(EventMessage.log(EventType.DEVICE, MessageType.ERROR,
                        "Устройство " + deviceDTO.getName() + " не доступно. Пропуск..."));
            }
        }
    }

    private boolean isReadyToPoll(DeviceDTO deviceDTO) {
        Long deviceId = deviceDTO.getId();
        LocalDateTime currentTime = LocalDateTime.now();
        // Ставим метку времени первого опроса, если устройство не найдено в расписании
        LocalDateTime lastPullingTime = schedule.get(deviceId);
        int pollingPeriod = deviceDTO.getPeriod();
//        deviceLogger.info("Период опроса: {} сек.", pollingPeriod);
        eventDispatcher.dispatch(EventMessage.log(EventType.DEVICE, MessageType.INFO,
                "Период опроса: " + pollingPeriod + " сек."));

        // Устройство опрашивается впервые
        if (lastPullingTime == null) {
            schedule.put(deviceId, currentTime);
//            deviceLogger.info("Время первого пороса: {}", currentTime);
            eventDispatcher.dispatch(EventMessage.log(EventType.DEVICE, MessageType.INFO,
                    "Время первого пороса: " + currentTime));

            return true;
        }

        long secondsSinceLastPoll = Duration.between(lastPullingTime, currentTime).toSeconds();
//        deviceLogger.info("Прошло: {} сек.", secondsSinceLastPoll);
        eventDispatcher.dispatch(EventMessage.log(EventType.DEVICE, MessageType.INFO,
                "Прошло: " + secondsSinceLastPoll + " сек."));

        if (secondsSinceLastPoll >= deviceDTO.getPeriod()) {
            schedule.put(deviceId, currentTime);
//            deviceLogger.info("Device: {} - Last Pulling Time updated to: {}", deviceDTO.getName(), currentTime);

            return true;
        } else {
//            deviceLogger.info("Device: {} - Not Yet Time For Polling. Last Polling Time: {} - Current Time: {}", deviceDTO.getName(), lastPullingTime, currentTime);

            return false;
        }
    }

    private Map<String, Object> snmpPoll(DeviceDTO deviceDTO) {
        ConcurrentMap<String, Object> snmpData = new ConcurrentHashMap<>();
        snmpData.put("deviceId", deviceDTO.getId());
        snmpData.put("deviceName", deviceDTO.getName());
        snmpData.put("deviceIp", deviceDTO.getIpAddress());
        snmpData.put("lastPollingTime", LocalDateTime.now());

//        logger.info("Starting SNMP poll for device: {} ({})", deviceDTO.getName(), deviceDTO.getIpAddress());
//        deviceLogger.info("Starting SNMP poll for device: {} ({})", deviceDTO.getName(), deviceDTO.getIpAddress());

        // Загружаем полный объект DeviceTypeDTO с параметрами
        DeviceTypeDTO deviceTypeDTO = dataBaseService.loadDeviceTypeWithParameters(deviceDTO.getDeviceType().getId());
        List<ParameterDTO> parameterDTOList = deviceTypeDTO.getParameter();

//        System.out.println("Parameters to Poll: " + parameterDTOList.size());
//        logger.info("Parameters to Poll: {}", parameterDTOList.size());
//        deviceLogger.info("Кол-во параметров: {}", parameterDTOList.size());
        eventDispatcher.dispatch(EventMessage.log(EventType.DEVICE, MessageType.INFO,
                "Кол-во параметров: " + parameterDTOList.size()));

        // Используем try-with-resources для правильного закрытия ресурса Snmp
        try (Snmp snmp = new Snmp(new DefaultUdpTransportMapping())) {
            snmp.listen();

            List<CompletableFuture<Void>> futureParameterList = parameterDTOList.stream()
                    .map(parameterDTO -> CompletableFuture.runAsync(() -> {
                        try {
                            pollParameterAsync(deviceDTO, parameterDTO, snmpData, snmp);
                        } catch (Exception e) {
                            e.printStackTrace();
//                            logger.error("Ошибка опроса параметра {} у {}: ", parameterDTO.getName(), deviceDTO.getName(), e);
                            eventDispatcher.dispatch(EventMessage.log(EventType.SYSTEM, MessageType.ERROR,
                                    "Ошибка опроса параметра " + parameterDTO.getName() + " у " + deviceDTO.getName() + ": " + e));

//                            deviceLogger.error("Ошибка опроса параметра: {} ", parameterDTO.getName(), e);
                            eventDispatcher.dispatch(EventMessage.log(EventType.DEVICE, MessageType.ERROR,
                                    "Ошибка опроса параметра " + parameterDTO.getName() + " у " + deviceDTO.getName() + ": " + e));
                        }
                    }))
                    .toList();

            CompletableFuture<Void> allOf = CompletableFuture.allOf(futureParameterList.toArray(new CompletableFuture[0]));

            allOf.get();
        } catch (IOException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
//            logger.error("Ошибка во время опроса {}: ", deviceDTO.getName(), e);
            eventDispatcher.dispatch(EventMessage.log(EventType.SYSTEM, MessageType.ERROR,
                    "Ошибка во время опроса " + deviceDTO.getName() + ": " + e));

//            deviceLogger.error("Ошибка во время опроса: ", e);
            eventDispatcher.dispatch(EventMessage.log(EventType.DEVICE, MessageType.ERROR,
                    "Ошибка во время опроса: " + e));
            Thread.currentThread().interrupt(); // Сбрасываем флаг прерывания
        }

        return snmpData;
    }

    @Async("taskExecutor")
    public CompletableFuture<Void> pollParameterAsync(DeviceDTO deviceDTO, ParameterDTO parameterDTO, Map<String, Object> snmpData, Snmp snmp) {
        MDC.put("deviceName", deviceDTO.getName());
        try {
            OID oid = new OID(parameterDTO.getAddress());
            PDU pdu = new PDU();
            pdu.add(new VariableBinding(oid));
            pdu.setType(PDU.GET);

            VariableBinding result = snmpService.performSnmpGet(deviceDTO.getIpAddress(), pdu, snmp);
//        System.out.println("Parameter Address: " + parameterDTO.getAddress());
//        deviceLogger.info("Parameter Address: {}", parameterDTO.getAddress());
            if (result == null || result.getVariable() == null) {
//                logger.warn("Пустое значение параметра {} у устройства {}", parameterDTO.getName(), deviceDTO.getName());
                eventDispatcher.dispatch(EventMessage.log(EventType.SYSTEM, MessageType.ERROR,
                        "Пустое значение параметра " + parameterDTO.getName() + " у " + deviceDTO.getName()));

                return CompletableFuture.completedFuture(null);
            }

            Variable variable = result.getVariable();

            List<ThresholdDTO> thresholdDTOList = dataBaseService.getThresholdsByParameterDTOAndIsEnableTrue(parameterDTO);
            DataType dataType = DataType.valueOf(parameterDTO.getDataType());

            TypeCaster<?> typeCaster = TypeCasterFactory.getTypeCaster(dataType);
            Object castValue = castTo(dataType, variable, typeCaster);
            ThresholdChecker checker = ThresholdCheckerFactory.getThresholdChecker(parameterDTO);

            Object processedValue;
            if (parameterDTO.getIsEnumeratedStatus()) {
                Integer intValue = (Integer) castValue;
                checker.checkThresholds(intValue, thresholdDTOList, deviceDTO);
                processedValue = processEnumeratedStatus(parameterDTO, castValue);
            } else {
                processedValue = processRegularParameter(parameterDTO, castValue);
                checker.checkThresholds(processedValue, thresholdDTOList, deviceDTO);
            }

//            deviceLogger.info("{}: {}", parameterDTO.getDescription(), processedValue);
            eventDispatcher.dispatch(EventMessage.log(EventType.DEVICE, MessageType.INFO,
                    parameterDTO.getDescription() + ": " + processedValue));

            snmpData.put(parameterDTO.getName(), processedValue);
        } catch (Exception e) {
//            logger.error("Ошибка опроса параметра {} у {}: ", parameterDTO.getName(), deviceDTO.getName(), e);
            eventDispatcher.dispatch(EventMessage.log(EventType.SYSTEM, MessageType.ERROR,
                    "Ошибка опроса параметра " + parameterDTO.getName() + " у " + deviceDTO.getName() + ": " + e));
//            deviceLogger.error("Ошибка опроса параметра: {}", parameterDTO.getName(), e);
            eventDispatcher.dispatch(EventMessage.log(EventType.DEVICE, MessageType.ERROR,
                    "Ошибка опроса параметра " + parameterDTO.getName() + " у " + deviceDTO.getName() + ": " + e));
        } finally {
            MDC.clear();  // Очищаем MDC после завершения
        }

        return CompletableFuture.completedFuture(null);
    }

    private Object processRegularParameter(ParameterDTO parameterDTO, Object castValue) {
        return applyModifications(DataType.valueOf(parameterDTO.getDataType()), castValue, parameterDTO.getAdditive(), parameterDTO.getCoefficient());
    }

    private Object processEnumeratedStatus(ParameterDTO parameterDTO, Object castValue) {
        if (castValue instanceof Integer intValue) {

            // Получаем карту статусов для параметра
            Map<Integer, String> statusMap = enumeratedStatusService.getStatusName(parameterDTO.getName());

            return Optional.ofNullable(statusMap.get(intValue))
                    .orElseGet(() -> {
//                        logger.error("Не найдено значение ключа {} для: {}", intValue, parameterDTO.getName());
                        eventDispatcher.dispatch(EventMessage.log(EventType.SYSTEM, MessageType.ERROR,
                                "Не найдено значение ключа " + intValue + " для: " + parameterDTO.getName()));

                        return "Неизвестный ключ";
                    });
        } else {
//            logger.error("Значение не является целым числом: {}", castValue);
            eventDispatcher.dispatch(EventMessage.log(EventType.SYSTEM, MessageType.ERROR,
                    "Значение не является целым числом: " + castValue));

            return "Недопустимый ключ";
        }
    }

    private Object applyModifications(DataType dataType, Object castValue,
                                      Double additive, Double coefficient) {
        switch (dataType) {
            case INTEGER -> castValue = (int) (((int) castValue + additive) * coefficient);
            case DOUBLE -> castValue = (((double) castValue + additive) * coefficient);
            case LONG -> castValue = (long) (((long) castValue + additive) * coefficient);
            default -> {
//                logger.error("Неподдерживаемый тип данных: {}", dataType);
                eventDispatcher.dispatch(EventMessage.log(EventType.SYSTEM, MessageType.ERROR,
                        "Неподдерживаемый тип данных: " + dataType));
//                deviceLogger.error("Неподдерживаемый тип данных: {}", dataType);
                eventDispatcher.dispatch(EventMessage.log(EventType.DEVICE, MessageType.ERROR,
                        "Неподдерживаемый тип данных: " + dataType));
                throw new IllegalArgumentException("Неподдерживаемый тип данных: " + dataType);
            }
        }

        return castValue;
    }

    private <T> T castTo(DataType dataType, Variable variable, TypeCaster<T> typeCaster) {
        if (variable == null) {
//            logger.warn("Значение null, невозможно преобразовать в {}", dataType);
            eventDispatcher.dispatch(EventMessage.log(EventType.SYSTEM, MessageType.ERROR,
                    "Значение null, невозможно преобразовать в " + dataType));
            return null;
        }
        return typeCaster.cast(variable);
    }
}