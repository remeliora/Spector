package com.example.spector.script;

import com.example.spector.database.dao.DAOService;
import com.example.spector.database.postgres.DataBaseService;
import com.example.spector.domain.dto.*;
import com.example.spector.domain.enums.AlarmType;
import com.example.spector.domain.enums.EventType;
import com.example.spector.domain.enums.MessageType;
import com.example.spector.modules.checker.device.DeviceConnectionChecker;
import com.example.spector.modules.converter.VariableCaster;
import com.example.spector.modules.event.EventDispatcher;
import com.example.spector.modules.event.EventMessage;
import com.example.spector.modules.handler.ParameterHandler;
import com.example.spector.modules.handler.ParameterHandlerFactory;
import com.example.spector.modules.snmp.SNMPService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
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
import java.util.concurrent.*;

@Component
@RequiredArgsConstructor
public class SnmpPollingGetAsync {   // Класс скрипта опроса по протоколу SNMP
    private final DataBaseService dataBaseService;
    private final DAOService daoService;
    private final DeviceConnectionChecker deviceConnectionChecker;
    private final SNMPService snmpService;
    private final EventDispatcher eventDispatcher;
    private final VariableCaster variableCaster;
    private final ConcurrentMap<Long, LocalDateTime> schedule = new ConcurrentHashMap<>();
    private final ParameterHandlerFactory parameterHandlerFactory;

    @Transactional
    public void pollDevices() {
        AppSettingDTO appSettingDTO = dataBaseService.getAppSetting();
        List<DeviceDTO> deviceDTOList = dataBaseService.getDeviceDTOByIsEnableTrue();
        eventDispatcher.dispatch(EventMessage.log(EventType.SYSTEM, MessageType.INFO,
                "Кол-во устройств: " + deviceDTOList.size()));
        eventDispatcher.dispatch(EventMessage.log(EventType.SYSTEM, MessageType.INFO,
                "Ожидание завершения всех задач опроса..."));
//        deviceDTOList.forEach(this::pollDeviceAsync);
        deviceDTOList.forEach(deviceDTO -> pollDeviceAsync(deviceDTO, appSettingDTO));
        // Ожидание завершения всех задач
        eventDispatcher.dispatch(EventMessage.log(EventType.SYSTEM, MessageType.INFO,
                "Задачи опроса проведены для всех устройств."));
    }

    @Async("taskExecutor")
    public CompletableFuture<Void> pollDeviceAsync(DeviceDTO deviceDTO, AppSettingDTO appSettingDTO) {
        // Проверка на наличие файла устройства и его создание
        MDC.put("deviceName", deviceDTO.getName());
        long startTime = System.currentTimeMillis();
        daoService.prepareDAO(deviceDTO);
        try {
            retryPollDevice(deviceDTO, appSettingDTO); // Добавляем ретраи с задержками
        } catch (IOException | TimeoutException e) {
            eventDispatcher.dispatch(EventMessage.log(EventType.SYSTEM, MessageType.ERROR,
                    "IOException | TimeoutException во время опроса" + deviceDTO.getName() + ": " + e));
        } finally {
            long endTime = System.currentTimeMillis();
            eventDispatcher.dispatch(EventMessage.log(EventType.DEVICE, MessageType.INFO,
                    "Опрос занял " + (endTime - startTime) + " мс."));
            MDC.clear();
        }

        return CompletableFuture.completedFuture(null);
    }

    // Механизм ретраев
    @Retryable(
            value = { IOException.class, TimeoutException.class },
            backoff = @Backoff(delay = 1000, multiplier = 2))  // Ретрай с задержкой в секундах
    public void retryPollDevice(DeviceDTO deviceDTO, AppSettingDTO appSettingDTO) throws IOException, TimeoutException {
        if (isReadyToPoll(deviceDTO)) {
            if (deviceConnectionChecker.isAvailableByIP(deviceDTO.getIpAddress())) {
                Map<String, Object> snmpData = snmpPoll(deviceDTO, appSettingDTO);
                daoService.writeData(deviceDTO, snmpData);
            } else {
                eventDispatcher.dispatch(EventMessage.log(EventType.SYSTEM, MessageType.ERROR,
                        deviceDTO.getName() + ": отсутствует соединение с устройством!"));
                eventDispatcher.dispatch(EventMessage.log(EventType.DEVICE, MessageType.ERROR,
                        "Устройство не доступно. Пропуск..."));
                eventDispatcher.dispatch((EventMessage.db(EventType.DB, MessageType.ERROR, AlarmType.EVERYWHERE,
                        appSettingDTO.getAlarmActive(), deviceDTO.getPeriod(),
                        deviceDTO.getName() + ": отсутствует соединение с устройством!")));
            }
        }
    }

    private boolean isReadyToPoll(DeviceDTO deviceDTO) {
        Long deviceId = deviceDTO.getId();
        LocalDateTime currentTime = LocalDateTime.now();
        // Ставим метку времени первого опроса, если устройство не найдено в расписании
        LocalDateTime lastPullingTime = schedule.get(deviceId);
        int pollingPeriod = deviceDTO.getPeriod();
        eventDispatcher.dispatch(EventMessage.log(EventType.DEVICE, MessageType.INFO,
                "Период опроса: " + pollingPeriod + " сек."));

        // Устройство опрашивается впервые
        if (lastPullingTime == null) {
            schedule.put(deviceId, currentTime);
            eventDispatcher.dispatch(EventMessage.log(EventType.DEVICE, MessageType.INFO,
                    "Время первого пороса: " + currentTime));

            return true;
        }

        long secondsSinceLastPoll = Duration.between(lastPullingTime, currentTime).toSeconds();
        eventDispatcher.dispatch(EventMessage.log(EventType.DEVICE, MessageType.INFO,
                "Прошло: " + secondsSinceLastPoll + " сек."));

        if (secondsSinceLastPoll >= deviceDTO.getPeriod()) {
            schedule.put(deviceId, currentTime);

            return true;
        } else {
            return false;
        }
    }

    private Map<String, Object> snmpPoll(DeviceDTO deviceDTO, AppSettingDTO appSettingDTO) {
        ConcurrentMap<String, Object> snmpData = new ConcurrentHashMap<>();
        snmpData.put("deviceId", deviceDTO.getId());
        snmpData.put("deviceName", deviceDTO.getName());
        snmpData.put("deviceIp", deviceDTO.getIpAddress());
        snmpData.put("lastPollingTime", LocalDateTime.now());
        // Загружаем полный объект DeviceTypeDTO с параметрами
        DeviceTypeDTO deviceTypeDTO = dataBaseService.loadDeviceTypeWithParameters(deviceDTO.getDeviceType().getId());
        List<ParameterDTO> parameterDTOList = deviceTypeDTO.getParameter();
        eventDispatcher.dispatch(EventMessage.log(EventType.DEVICE, MessageType.INFO,
                "Кол-во параметров: " + parameterDTOList.size()));

        // Используем try-with-resources для правильного закрытия ресурса Snmp
        try (Snmp snmp = new Snmp(new DefaultUdpTransportMapping())) {
            snmp.listen();

            List<CompletableFuture<Void>> futureParameterList = parameterDTOList.stream()
                    .map(parameterDTO -> CompletableFuture.runAsync(() -> {
                        try {
                            pollParameterAsync(deviceDTO, parameterDTO, snmpData, snmp, appSettingDTO);
                        } catch (Exception e) {
                            e.printStackTrace();
                            eventDispatcher.dispatch(EventMessage.log(EventType.SYSTEM, MessageType.ERROR,
                                    deviceDTO.getName() + ": " + parameterDTO.getName() +
                                    " - ошибка опроса: " + e));
                            eventDispatcher.dispatch(EventMessage.log(EventType.DEVICE, MessageType.ERROR,
                                    parameterDTO.getName() +  " - ошибка опроса: " + e));
                        }
                    }))
                    .toList();

            CompletableFuture<Void> allOf = CompletableFuture
                    .allOf(futureParameterList.toArray(new CompletableFuture[0]));

            allOf.get();
        } catch (IOException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
            eventDispatcher.dispatch(EventMessage.log(EventType.SYSTEM, MessageType.ERROR,
                    deviceDTO.getName() + ": ошибка во время опроса - " + e));
            eventDispatcher.dispatch(EventMessage.log(EventType.DEVICE, MessageType.ERROR,
                    "Ошибка во время опроса: " + e));
            Thread.currentThread().interrupt(); // Сбрасываем флаг прерывания
        }

        return snmpData;
    }

    @Async("taskExecutor")
    public CompletableFuture<Void> pollParameterAsync(DeviceDTO deviceDTO, ParameterDTO parameterDTO,
                                                      Map<String, Object> snmpData, Snmp snmp,
                                                      AppSettingDTO appSettingDTO) {
        MDC.put("deviceName", deviceDTO.getName());
        try {
            OID oid = new OID(parameterDTO.getAddress());
            PDU pdu = new PDU();
            pdu.add(new VariableBinding(oid));
            pdu.setType(PDU.GET);

            VariableBinding result = snmpService.performSnmpGet(deviceDTO.getIpAddress(), pdu, snmp);
            if (result == null || result.getVariable() == null) {
                eventDispatcher.dispatch(EventMessage.log(EventType.SYSTEM, MessageType.ERROR,
                        deviceDTO.getName() + ": " + parameterDTO.getDescription() + " - Данные отсутствуют"));
                eventDispatcher.dispatch(EventMessage.log(EventType.DEVICE, MessageType.ERROR,
                        parameterDTO.getDescription() + ": данные отсутствуют"));
                eventDispatcher.dispatch(EventMessage.db(EventType.DB, MessageType.ERROR, AlarmType.EVERYWHERE,
                        appSettingDTO.getAlarmActive(), deviceDTO.getPeriod(),
                        deviceDTO.getName() + ": " + parameterDTO.getDescription() + " - Данные отсутствуют"));

                return CompletableFuture.completedFuture(null);
            }

            Variable variable = result.getVariable();

            List<ThresholdDTO> thresholdDTOList = dataBaseService.getThresholdsByParameterDTOAndIsEnableTrue(parameterDTO);
            Object castValue = variableCaster.convert(parameterDTO, variable);

            ParameterHandler parameterHandler = parameterHandlerFactory.getParameterHandler(parameterDTO);
            Object processedValue = parameterHandler.handleParameter(deviceDTO, parameterDTO, castValue,
                    thresholdDTOList, appSettingDTO);
            eventDispatcher.dispatch(EventMessage.log(EventType.DEVICE, MessageType.INFO,
                    "Параметр - " + parameterDTO.getDescription() + ": " + processedValue));

            snmpData.put(parameterDTO.getName(), processedValue);
        } catch (Exception e) {
            eventDispatcher.dispatch(EventMessage.log(EventType.SYSTEM, MessageType.ERROR,
                    deviceDTO.getName() + ": " + parameterDTO.getDescription() + " - Данные повреждены: " + e));
            eventDispatcher.dispatch(EventMessage.log(EventType.DEVICE, MessageType.ERROR,
                    parameterDTO.getName() + " - Данные повреждены: " + e));
            eventDispatcher.dispatch(EventMessage.db(EventType.DB, MessageType.ERROR, AlarmType.EVERYWHERE,
                    appSettingDTO.getAlarmActive(), deviceDTO.getPeriod(),
                    deviceDTO.getName() + ": " + parameterDTO.getDescription() + " - Данные повреждены"));
        } finally {
            MDC.clear();  // Очищаем MDC после завершения
        }

        return CompletableFuture.completedFuture(null);
    }
}