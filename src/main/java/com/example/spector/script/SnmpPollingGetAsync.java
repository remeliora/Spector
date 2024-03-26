package com.example.spector.script;

import com.example.spector.converter.TypeCaster;
import com.example.spector.converter.TypeCasterFactory;
import com.example.spector.domain.Device;
import com.example.spector.domain.Parameter;
import com.example.spector.domain.Threshold;
import com.example.spector.domain.enums.DataType;
import com.example.spector.service.DAOService;
import com.example.spector.service.DataBaseService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.event.ResponseListener;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.*;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class SnmpPollingGetAsync {   // Класс скрипта опроса по протоколу SNMP
    private final DataBaseService dataBaseService;
    private final DAOService daoService;
    private final Map<Long, LocalDateTime> schedule;

    public void pollDevices() {
        //  Вызов списка всех устройств из БД для опроса (с параметром isEnable = true)
        List<Device> devicesToPoll = (List<Device>) dataBaseService.getDeviceByIsEnableTrue();

        for (Device device : devicesToPoll) {
            //  Проверка на наличие файла устройства и его создание
            daoService.prepareDAO(device);

            // Проверка времени прошедшего с последнего цикла опроса
            if (!isReadyToPoll(device)) {
                // Вызов метода snmpPoll для каждого устройства
                snmpPoll(device);
            }
        }
    }

    private boolean isReadyToPoll(Device device) {
        if (!schedule.containsKey(device.getId())) {
            schedule.put(device.getId(), LocalDateTime.now());
            System.out.println("Device: " + device.getName() + " - Is Time For Polling: " + schedule.getOrDefault(device.getId(), LocalDateTime.now()));

            return true;
        } else {
            LocalDateTime lastPullingTime = schedule.get(device.getId());
            LocalDateTime currentTime = LocalDateTime.now();
            int pollingPeriod = device.getPeriod();
            boolean isTimeForPolling = Duration.between(lastPullingTime, currentTime).toSeconds() >= pollingPeriod;

            if (isTimeForPolling) {
                System.out.println("Device: " + device.getName() + " - Last Pulling Time: " + schedule.getOrDefault(device.getId(), lastPullingTime) + " - Current Time: " + currentTime);

                return true;
            } else {
                return false;
            }
        }
    }

    @Transactional
    public Map<String, Object> snmpPoll(Device device) {
        Long deviceId = device.getId();
        String deviceName = device.getName();
        String deviceIp = device.getIpAddress();

        LocalDateTime lastPollingTime = LocalDateTime.now();

        schedule.put(device.getId(), lastPollingTime);

        Set<Parameter> parameters = device.getDeviceType().getParameters();
        Map<String, Object> snmpData = new HashMap<>();

        snmpData.put("deviceId", deviceId);
        snmpData.put("deviceName", deviceName);
        snmpData.put("deviceIp", deviceIp);
        snmpData.put("lastPollingTime", lastPollingTime);

        //  Отладочный вывод
        System.out.println("Parameters to Poll: " + parameters.size());

        for (Parameter parameter : parameters) {
            OID oid = new OID(parameter.getAddress());
            PDU pdu = new PDU();
            pdu.add(new VariableBinding(oid));
            pdu.setType(PDU.GET);

            //  Отладочный вывод
            System.out.println("Performing SNMP GET for Parameter: " + parameter.getName());

            VariableBinding result = performSnmpGet(deviceIp, pdu);

            //  Отладочные выводы
            System.out.println("Parameter Address: " + parameter.getAddress());
            System.out.println("Result Variable: " + result.getVariable());

            //  Обрабатываем значения переменных в соответствии с их типом данных
            Variable variable = result.getVariable();
            List<Threshold> thresholds = (List<Threshold>) dataBaseService.getThresholdsByParameterAndIsEnableTrue(parameter);
            //  Получаем тип данных параметра
            DataType dataType = parameter.getDataType();
            //  Применяем cast к полученным значениям
            TypeCaster<?> typeCaster = TypeCasterFactory.getTypeCaster(dataType);
            Object castValue = castTo(dataType, variable, typeCaster);
            System.out.println("Result Variable: " + castValue);
            //  Применяем модификаторы к значениям
            Object processedValue = applyModifications(dataType, castValue, parameter.getAdditive(), parameter.getCoefficient());
            System.out.println("Result Variable: " + processedValue);
            //  Проверяем значения с порогами
            checkThresholds(processedValue, thresholds, device);
            snmpData.put(parameter.getName(), processedValue);
        }

        daoService.writeData(device, snmpData);

        return snmpData;
    }

    private void checkThresholds(Object processedValue, List<Threshold> thresholds, Device device) {
        for (Threshold threshold : thresholds) {
            if (threshold.getDevice().getId().equals(device.getId())) {
                double lowValue = threshold.getLowValue();
                double highValue = threshold.getHighValue();

                if ((double) processedValue < lowValue || (double) processedValue > highValue) {
                    System.out.println("Threshold crossed: Parameter " + threshold.getParameter().getName() +
                            " with value " + processedValue + " is out of range [" + lowValue + ", " + highValue + "]");
                } else {
                    System.out.println("Threshold successful");
                }
            }
        }
    }

    private Object applyModifications(DataType dataType, Object castValue, Double additive, Double coefficient) {
        switch (dataType) {
            case INTEGER -> {
                castValue = (int) (((int) castValue + additive) * coefficient);
            }
            case DOUBLE -> {
                castValue = (((double) castValue + additive) * coefficient);
            }
            case LONG -> {
                castValue = (long) (((long) castValue + additive) * coefficient);
            }
            default -> throw new IllegalArgumentException("Unsupported data type: " + dataType);
        }

        return castValue;
    }

    private <T> T castTo(DataType dataType, Variable variable, TypeCaster<T> typeCaster) {
        return typeCaster.cast(variable);
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
}