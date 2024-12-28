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
    private final Semaphore semaphore = new Semaphore(10);
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(10);
    private final ConcurrentMap<Long, LocalDateTime> schedule = new ConcurrentHashMap<>();
    private static final Logger logger = LoggerFactory.getLogger(SnmpPollingGetAsync.class);
    private static final Logger deviceLogger = LoggerFactory.getLogger("DeviceLogger");


    @Transactional
    public void pollDevices() {
        List<DeviceDTO> deviceDTOList = dataBaseService.getDeviceDTOByIsEnableTrue();
        logger.info("Devices to Poll: {}", deviceDTOList.size());
        logger.info("Waiting for all polling tasks to complete...");

        // Асинхронный опрос устройств
        // Запуск задач с задержками, чтобы избежать пиковых нагрузок
        for (DeviceDTO deviceDTO : deviceDTOList) {
            scheduler.schedule(() -> pollDeviceAsync(deviceDTO), 3, TimeUnit.SECONDS); // можно добавить интервал для задержки
        }

        // Ожидание завершения всех задач
        logger.info("Polling tasks scheduled for all devices.");
    }

    @Async("taskExecutor")
    public CompletableFuture<Void> pollDeviceAsync(DeviceDTO deviceDTO) {
        // Проверка на наличие файла устройства и его создание
        daoService.prepareDAO(deviceDTO);
        MDC.put("deviceName", deviceDTO.getName());

        try {
            semaphore.acquire();    // Ограничиваем число одновременных потоков
            retryPollDevice(deviceDTO); // Добавляем ретраи с задержками
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Polling interrupted for device {}: ", deviceDTO.getName(), e);
        } catch (IOException e) {
            logger.error("IOException during polling device {}: ", deviceDTO.getName(), e);
        } finally {
            MDC.clear();
            semaphore.release();    // Освобождаем семафор
        }

        return CompletableFuture.completedFuture(null);
    }

    // Механизм ретраев
    @Retryable(
            value = { IOException.class },
            maxAttempts = 3,
            backoff = @Backoff(delay = 5000))  // Ретрай с задержкой 3 секунды
    public void retryPollDevice(DeviceDTO deviceDTO) throws IOException {
        if (isReadyToPoll(deviceDTO)) {
            if (deviceConnectionChecker.isAvailableByIP(deviceDTO.getIpAddress())) {
                Map<String, Object> snmpData = snmpPoll(deviceDTO);
                daoService.writeData(deviceDTO, snmpData);
            } else {
                logger.error("Device {} is not available. Skipping...", deviceDTO.getName());
                deviceLogger.error("Device {} is not available. Skipping...", deviceDTO.getName());
            }
        }
    }

    private boolean isReadyToPoll(DeviceDTO deviceDTO) {
        Long deviceId = deviceDTO.getId();
        LocalDateTime currentTime = LocalDateTime.now();

        // Сставим метку времени первого опроса, если устройство не найдено в расписании
        LocalDateTime lastPullingTime = schedule.get(deviceId);

        // Устройство опрашивается впервые
        if (lastPullingTime == null) {
//            System.out.println("Device: " + deviceDTO.getName() + " - Is Time For Polling: " + currentTime);
            deviceLogger.info("Device: {} - Is Time For Polling: {}", deviceDTO.getName(), currentTime);
            schedule.put(deviceId, currentTime);

            return true;
        }

        // Проверка, прошло ли достаточно времени для следующего опроса
        int pollingPeriod = deviceDTO.getPeriod();
        boolean isTimeForPolling = Duration.between(lastPullingTime, currentTime).toSeconds() >= pollingPeriod;

        if (isTimeForPolling) {
//            System.out.println("Device: " + deviceDTO.getName() + " - Last Pulling Time: " + lastPullingTime + " - Current Time: " + currentTime);
            deviceLogger.info("Device: {} - Last Pulling Time: {} - Current Time: {}", deviceDTO.getName(), lastPullingTime, currentTime);
            schedule.put(deviceId, currentTime);

            return true;
        } else {
//            System.out.println("Device: " + deviceDTO.getName() + " - Not Yet Time For Polling. Last Polling Time: " + lastPullingTime + " - Current Time: " + currentTime);
            deviceLogger.info("Device: {} - Not Yet Time For Polling. Last Polling Time: {} - Current Time: {}", deviceDTO.getName(), lastPullingTime, currentTime);

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
        deviceLogger.info("Starting SNMP poll for device: {} ({})", deviceDTO.getName(), deviceDTO.getIpAddress());

        // Загружаем полный объект DeviceTypeDTO с параметрами
        DeviceTypeDTO deviceTypeDTO = dataBaseService.loadDeviceTypeWithParameters(deviceDTO.getDeviceType().getId());
        List<ParameterDTO> parameterDTOList = deviceTypeDTO.getParameter();

//        System.out.println("Parameters to Poll: " + parameterDTOList.size());
//        logger.info("Parameters to Poll: {}", parameterDTOList.size());
        deviceLogger.info("Parameters to Poll: {}", parameterDTOList.size());

        // Используем try-with-resources для правильного закрытия ресурса Snmp
        try (Snmp snmp = new Snmp(new DefaultUdpTransportMapping())) {
            snmp.listen();

            List<CompletableFuture<Void>> futureParameterList = parameterDTOList.stream()
                    .map(parameterDTO -> CompletableFuture.runAsync(() -> {
                        try {
                            pollParameterAsync(deviceDTO, parameterDTO, snmpData, snmp);
                        } catch (Exception e) {
                            e.printStackTrace();
                            logger.error("Error polling parameter {} for device {}: ", parameterDTO.getName(), deviceDTO.getName(), e);
                            deviceLogger.error("Error polling parameter {} for device {}: ", parameterDTO.getName(), deviceDTO.getName(), e);
                        }
                    }))
                    .toList();

            CompletableFuture<Void> allOf = CompletableFuture.allOf(futureParameterList.toArray(new CompletableFuture[0]));
            // Ожидание завершения всех SNMP-запросов
//            logger.info("Waiting for all polling tasks to complete...");
            allOf.get();
//            logger.info("All polling tasks completed.");

        } catch (IOException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
            logger.error("Error during SNMP polling for device {}: ", deviceDTO.getName(), e);
            deviceLogger.error("Error during SNMP polling for device {}: ", deviceDTO.getName(), e);
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

            deviceLogger.info("Parameter ({}): {}", parameterDTO.getName(), processedValue);

            // Потокобезопасная запись данных
            synchronized (snmpData) {
                snmpData.put(parameterDTO.getName(), processedValue);
            }

        } catch (Exception e) {
            logger.error("Error polling parameter {} for device {}: ", parameterDTO.getName(), deviceDTO.getName(), e);
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
                        logger.error("Enum value not found for key: {} in parameter: {}", intValue, parameterDTO.getName());

                        return "Unknown status";
                    });
        } else {
            logger.error("Cast value is not an integer: {}", castValue);

            return "Invalid status";
        }
    }

    private Object applyModifications(DataType dataType, Object castValue,
                                      Double additive, Double coefficient) {
        switch (dataType) {
            case INTEGER -> castValue = (int) (((int) castValue + additive) * coefficient);
            case DOUBLE -> castValue = (((double) castValue + additive) * coefficient);
            case LONG -> castValue = (long) (((long) castValue + additive) * coefficient);
            default -> {
                logger.error("Unsupported data type: {}", dataType);
                deviceLogger.error("Unsupported data type: {}", dataType);
                throw new IllegalArgumentException("Unsupported data type: " + dataType);
            }
        }

        return castValue;
    }

    private <T> T castTo(DataType dataType, Variable variable, TypeCaster<T> typeCaster) {
        return typeCaster.cast(variable);
    }
}