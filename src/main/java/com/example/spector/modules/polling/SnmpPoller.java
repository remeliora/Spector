package com.example.spector.modules.polling;

import com.example.spector.database.dao.DAOService;
import com.example.spector.database.postgres.PollingDataService;
import com.example.spector.domain.device.dto.DeviceDTO;
import com.example.spector.domain.enums.AlarmType;
import com.example.spector.domain.enums.EventType;
import com.example.spector.domain.enums.MessageType;
import com.example.spector.domain.parameter.dto.ParameterDTO;
import com.example.spector.domain.setting.dto.AppSettingDTO;
import com.example.spector.domain.threshold.dto.ThresholdDTO;
import com.example.spector.modules.cache.RealTimeDataService;
import com.example.spector.modules.converter.VariableCaster;
import com.example.spector.modules.datapattern.BaseSNMPData;
import com.example.spector.modules.datapattern.BaseSNMPStatus;
import com.example.spector.modules.datapattern.ParameterData;
import com.example.spector.modules.datapattern.ResultValue;
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
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class SnmpPoller {
    private final PollingDataService pollingDataService;
    private final DAOService daoService;
    private final SNMPService snmpService;
    private final EventDispatcher eventDispatcher;
    private final VariableCaster variableCaster;
    private final ParameterHandlerFactory parameterHandlerFactory;
    private final BaseSNMPData baseSNMPData;
    private final BaseSNMPStatus baseSNMPStatus;
    private final RealTimeDataService realTimeDataService;

    /**
     * Выполняет один цикл опроса для устройства с указанным ID.
     *
     * @param deviceId ID устройства.
     */
    @Transactional
    @Async("taskExecutor")
    public void pollDevice(Long deviceId) {
        // 1. Загрузка актуальной конфигурации устройства из БД
        DeviceDTO currentDevice = pollingDataService.getDeviceById(deviceId);

        MDC.put("deviceName", currentDevice.getName());

//        eventDispatcher.dispatch(EventMessage.log(EventType.DEVICE, MessageType.INFO,
//                "Начало опроса устройства"));

        // 2. Проверка существования и статуса включения на момент начала опроса
        if (currentDevice == null) {
            eventDispatcher.dispatch(EventMessage.log(EventType.SYSTEM, MessageType.ERROR,
                    "Устройство не найдено в БД. Пропуск опроса."));
            eventDispatcher.dispatch(EventMessage.log(EventType.DEVICE, MessageType.ERROR,
                    "Устройство " + currentDevice.getName() + " не найдено в БД. Пропуск опроса."));

            return;
        }

        boolean wasEnabledAtStartOfPoll = currentDevice.getIsEnable();
        if (!wasEnabledAtStartOfPoll) {
//            eventDispatcher.dispatch(EventMessage.log(EventType.DEVICE, MessageType.INFO,
//                    "Устройство выключено. Пропуск опроса."));

            return;
        }

        // Подготовка структуры выходных данных
        daoService.prepareDAO(currentDevice);

        // 3. Загрузка настроек приложения
        AppSettingDTO appSettingDTO = pollingDataService.getAppSetting();

        // 4. Подготовка структуры данных устройства
        Map<String, Object> snmpData = baseSNMPData.defaultSNMPDeviceData(currentDevice);

        try {
            // 5. Проверка доступности по IP
            if (snmpService.isAvailableBySNMP(currentDevice.getIpAddress())) {
                snmpData.put("status", "OK");

                // 6. Выполнение SNMP опроса параметров
                Map<String, Object> additionalData = performSnmpPoll(currentDevice, appSettingDTO);

                // 7. Определение общего статуса устройства
                List<ParameterData> parameterDataList = (List<ParameterData>) additionalData.get("parameters");
                String deviceStatus = baseSNMPStatus.determineStatus(parameterDataList);
                snmpData.put("status", deviceStatus);
                snmpData.putAll(additionalData);

            } else {
                // Устройство не доступно по IP
                snmpData.put("status", "ERROR");
                eventDispatcher.dispatch(EventMessage.log(EventType.SYSTEM, MessageType.ERROR,
                        currentDevice.getName() + ": отсутствует соединение с устройством!"));
                eventDispatcher.dispatch(EventMessage.log(EventType.DEVICE, MessageType.ERROR,
                        "Устройство не доступно. Пропуск..."));
                eventDispatcher.dispatch((EventMessage.db(EventType.DB, MessageType.ERROR, AlarmType.EVERYWHERE,
                        appSettingDTO.getAlarmActive(), currentDevice.getPeriod(),
                        currentDevice.getName() + ": отсутствует соединение с устройством!")));
            }

        } catch (Exception e) { // Обработка исключений общего опроса
            eventDispatcher.dispatch(EventMessage.log(EventType.SYSTEM, MessageType.ERROR,
                    currentDevice.getName() + ": ошибка во время опроса - " + e));
            eventDispatcher.dispatch(EventMessage.log(EventType.DEVICE, MessageType.ERROR,
                    "Ошибка во время опроса: " + e));

            // Устанавливаем статус ERROR для устройства в случае фатальной ошибки опроса
            snmpData.put("status", "ERROR");
        }

        // 8. Проверка статуса устройства ещё раз перед записью.
        // Загружаем актуальное состояние после завершения опроса (успешного или неуспешного)
        DeviceDTO currentDeviceAfterPoll = pollingDataService.getDeviceById(deviceId);
        MDC.put("deviceName", currentDeviceAfterPoll.getName());
        boolean isStillEnabledAfterPoll = currentDeviceAfterPoll != null && currentDeviceAfterPoll.getIsEnable();

        if (isStillEnabledAfterPoll) {
            // 9. Запись результатов (только если устройство всё ещё включено)
            // Запись данных (в MongoDB)
            daoService.writeData(currentDevice, snmpData);

            realTimeDataService.updateAndNotify(deviceId, snmpData);
        } else {
            eventDispatcher.dispatch(EventMessage.log(EventType.SYSTEM, MessageType.INFO,
                    "Устройство " + currentDeviceAfterPoll.getName() +
                    " выключено/удалено после опроса."));
            eventDispatcher.dispatch(EventMessage.log(EventType.DEVICE, MessageType.INFO,
                    "Устройство выключено/удалено после опроса."));
        }

        // 10. Публикация события завершения опроса.
        // Публикуем событие с тем статусом, который был на *момент начала* опроса
//        eventDispatcher.dispatch(EventMessage.log(EventType.DEVICE, MessageType.INFO,
//                "Опрос устройства завершён."));
    }

    /**
     * Выполняет SNMP опрос параметров устройства.
     *
     * @param device        Устройство для опроса.
     * @param appSettingDTO Настройки приложения.
     * @return Map с результатами опроса (например, "parameters": List<ParameterData>).
     * @throws IOException В случае ошибки SNMP.
     */
    private Map<String, Object> performSnmpPoll(DeviceDTO device, AppSettingDTO appSettingDTO) throws IOException {
        List<ParameterData> parameterDataList = new ArrayList<>();

        // 1. Загрузка параметров устройства
        List<ParameterDTO> parameterDTOList = pollingDataService.getActiveParametersForDevice(device.getId());
//        eventDispatcher.dispatch(EventMessage.log(EventType.DEVICE, MessageType.INFO,
//                "Кол-во параметров: " + parameterDTOList.size()));

        // 2. Создание и использование SNMP сессии
        try (Snmp snmp = new Snmp(new DefaultUdpTransportMapping())) {
            snmp.listen();

            for (ParameterDTO parameterDTO : parameterDTOList) {
                try {
                    // Вызов метода опроса одного параметра
                    pollParameter(device, parameterDTO, snmp, appSettingDTO, parameterDataList);
                } catch (Exception e) {
                    // В случае ошибки создаем запись с соответствующим статусом
                    ParameterData errorParamData = baseSNMPData.defaultSNMPParameterData(parameterDTO);
                    errorParamData.setValue(null);
                    errorParamData.setStatus("ERROR");
                    parameterDataList.add(errorParamData);
                }
            }
        } catch (IOException e) {
            eventDispatcher.dispatch(EventMessage.log(EventType.SYSTEM, MessageType.ERROR,
                    device.getName() + ": ошибка во время опроса - " + e));
            eventDispatcher.dispatch(EventMessage.log(EventType.DEVICE, MessageType.ERROR,
                    "Ошибка во время опроса: " + e));
            throw e; // Перебрасываем, чтобы обработать выше
        }

        return Map.of("parameters", parameterDataList);
    }

    /**
     * Выполняет SNMP GET запрос для одного параметра и обрабатывает результат.
     *
     * @param device            Устройство.
     * @param parameterDTO      Параметр для опроса.
     * @param snmp              Активная SNMP сессия.
     * @param appSettingDTO     Настройки приложения.
     * @param parameterDataList Список, в который добавляются результаты.
     */
    private void pollParameter(DeviceDTO device, ParameterDTO parameterDTO, Snmp snmp,
                               AppSettingDTO appSettingDTO, List<ParameterData> parameterDataList) throws IOException {
        OID oid = new OID(parameterDTO.getAddress());
        PDU pdu = new PDU();
        pdu.add(new VariableBinding(oid));
        pdu.setType(PDU.GET);

        VariableBinding result = snmpService.performSnmpGet(device.getIpAddress(), pdu, snmp);
        ParameterData parameterData = baseSNMPData.defaultSNMPParameterData(parameterDTO);

        if (result == null || result.getVariable() == null) {
            eventDispatcher.dispatch(EventMessage.log(EventType.SYSTEM, MessageType.ERROR,
                    device.getName() + ": " + parameterDTO.getDescription() + " - Данные отсутствуют"));
            eventDispatcher.dispatch(EventMessage.log(EventType.DEVICE, MessageType.ERROR,
                    parameterDTO.getDescription() + ": данные отсутствуют"));
            eventDispatcher.dispatch(EventMessage.db(EventType.DB, MessageType.ERROR, AlarmType.EVERYWHERE,
                    appSettingDTO.getAlarmActive(), device.getPeriod(),
                    device.getName() + ": " + parameterDTO.getDescription() + " - Данные отсутствуют"));

            parameterData.setValue(null);
            parameterData.setStatus("NO_DATA");
            parameterDataList.add(parameterData);
            return; // Возвращаемся, чтобы не обрабатывать дальше
        }

        Variable variable = result.getVariable();
        // Загрузка порогов для параметра
        List<ThresholdDTO> thresholdDTOList = pollingDataService.getThresholdsByParameterDTOAndIsEnableTrue(parameterDTO);
        Object castValue = variableCaster.convert(parameterDTO, variable);

        ParameterHandler parameterHandler = parameterHandlerFactory.getParameterHandler(parameterDTO);
        ResultValue resultValue = parameterHandler.handleParameter(device, parameterDTO, castValue, thresholdDTOList, appSettingDTO);

//        eventDispatcher.dispatch(EventMessage.log(EventType.DEVICE, MessageType.INFO,
//                "Параметр - " + parameterDTO.getDescription() + ": " + resultValue.getValue()));

        parameterData.setValue(resultValue.getValue());
        parameterData.setStatus(resultValue.getStatus());
        parameterDataList.add(parameterData);
    }
}
