package com.example.spector.script;

import com.example.spector.checker.DeviceConnectionChecker;
import com.example.spector.converter.TypeCaster;
import com.example.spector.converter.TypeCasterFactory;
import com.example.spector.domain.dto.DeviceDTO;
import com.example.spector.domain.dto.DeviceTypeDTO;
import com.example.spector.domain.dto.ParameterDTO;
import com.example.spector.domain.dto.ThresholdDTO;
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
import java.util.concurrent.*;

@Component
@RequiredArgsConstructor
public class SnmpPollingGetAsync {   // Класс скрипта опроса по протоколу SNMP
    private final DataBaseService dataBaseService;
    private final DAOService daoService;
    private final DeviceConnectionChecker deviceConnectionChecker;
    private final ConcurrentMap<Long, LocalDateTime> schedule = new ConcurrentHashMap<>();

    @Transactional
    public void pollDevices() {
        List<DeviceDTO> deviceDTOList = dataBaseService.getDeviceDTOByIsEnableTrue();

        List<CompletableFuture<Void>> futureDeviceList = deviceDTOList.stream()
                .map(deviceDTO -> CompletableFuture.runAsync(() -> preparePoll(deviceDTO)))
                .toList();

        CompletableFuture<Void> allOf = CompletableFuture.allOf(futureDeviceList.toArray(new CompletableFuture[0]));
        try {
            allOf.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    public void preparePoll(DeviceDTO deviceDTO) {
        //  Проверка на наличие файла устройства и его создание
        daoService.prepareDAO(deviceDTO);

        // Проверка времени прошедшего с последнего цикла опроса
        if (isReadyToPoll(deviceDTO)) {
            if (deviceConnectionChecker.isDeviceAvailable(deviceDTO.getIpAddress())) {
                Map<String, Object> snmpData = snmpPoll(deviceDTO);
                daoService.writeData(deviceDTO, snmpData);
            } else {
                System.out.println("Device " + deviceDTO.getName() + " is not available. Skipping...");
            }
        }
    }

    private boolean isReadyToPoll(DeviceDTO deviceDTO) {
        Long deviceId = deviceDTO.getId();
        LocalDateTime currentTime = LocalDateTime.now();

        // Пытаемся поставить метку времени первого опроса, если устройство не найдено в расписании
        LocalDateTime lastPullingTime = schedule.putIfAbsent(deviceId, currentTime);

        // Устройство опрашивается впервые
        if (lastPullingTime == null) {
            System.out.println("Device: " + deviceDTO.getName() + " - Is Time For Polling: " + currentTime);

            return true;
        }

        // Проверка, прошло ли достаточно времени для следующего опроса
        int pollingPeriod = deviceDTO.getPeriod();
        boolean isTimeForPolling = Duration.between(lastPullingTime, currentTime).toSeconds() >= pollingPeriod;

        if (isTimeForPolling) {
            System.out.println("Device: " + deviceDTO.getName() + " - Last Pulling Time: " + lastPullingTime + " - Current Time: " + currentTime);
            schedule.put(deviceId, currentTime);

            return true;
        } else {
            System.out.println("Device: " + deviceDTO.getName() + " - Not Yet Time For Polling. Last Polling Time: " + lastPullingTime + " - Current Time: " + currentTime);

            return false;
        }
    }

    private Map<String, Object> snmpPoll(DeviceDTO deviceDTO) {

        Map<String, Object> snmpData = new HashMap<>();

        snmpData.put("deviceId", deviceDTO.getId());
        snmpData.put("deviceName", deviceDTO.getName());
        snmpData.put("deviceIp", deviceDTO.getIpAddress());
        snmpData.put("lastPollingTime", LocalDateTime.now());

        schedule.put(deviceDTO.getId(), LocalDateTime.now());

        // Загружаем полный объект DeviceTypeDTO с параметрами
        DeviceTypeDTO deviceTypeDTO = dataBaseService.loadDeviceTypeWithParameters(deviceDTO.getDeviceType().getId());

        List<ParameterDTO> parameterDTOList = deviceTypeDTO.getParameter();

        //  Отладочный вывод
        System.out.println("Parameters to Poll: " + parameterDTOList.size());

        List<CompletableFuture<Void>> futureParameterList = parameterDTOList.stream()
                .map(parameterDTO -> CompletableFuture.runAsync(() -> prepareSNMPPoll(deviceDTO, parameterDTO, snmpData)))
                .toList();

        CompletableFuture<Void> allOf = CompletableFuture.allOf(futureParameterList.toArray(new CompletableFuture[0]));
        try {
            allOf.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        return snmpData;
    }

    private void prepareSNMPPoll(DeviceDTO deviceDTO, ParameterDTO parameterDTO, Map<String, Object> snmpData) {
        OID oid = new OID(parameterDTO.getAddress());
        PDU pdu = new PDU();
        pdu.add(new VariableBinding(oid));
        pdu.setType(PDU.GET);

//        //  Отладочный вывод
//        System.out.println("Performing SNMP GET for Parameter: " + parameterDTO.getName());

        VariableBinding result = performSnmpGet(deviceDTO.getIpAddress(), pdu);

//        //  Отладочные выводы
        System.out.println("Parameter Address: " + parameterDTO.getAddress());
//        System.out.println("Result Variable: " + result.getVariable());

        //  Обрабатываем значения переменных в соответствии с их типом данных
        Variable variable = result.getVariable();
        List<ThresholdDTO> thresholdDTOList = dataBaseService.getThresholdsByParameterDTOAndIsEnableTrue(parameterDTO);
        //  Получаем тип данных параметра

        DataType dataType = DataType.valueOf(parameterDTO.getDataType());
        //  Применяем cast к полученным значениям
        TypeCaster<?> typeCaster = TypeCasterFactory.getTypeCaster(dataType);
        Object castValue = castTo(dataType, variable, typeCaster);
//        System.out.println("Result Variable: " + castValue);
        //  Применяем модификаторы к значениям
        Object processedValue = applyModifications(dataType, castValue, parameterDTO.getAdditive(), parameterDTO.getCoefficient());
        System.out.println("Result Variable: " + processedValue);
        //  Проверяем значения с порогами
        checkThresholds(processedValue, thresholdDTOList, deviceDTO);
        snmpData.put(parameterDTO.getName(), processedValue);
    }

    private void checkThresholds(Object processedValue, List<ThresholdDTO> thresholdDTOList, DeviceDTO deviceDTO) {
        for (ThresholdDTO thresholdDTO : thresholdDTOList) {
            if (thresholdDTO.getDevice().getId().equals(deviceDTO.getId())) {
                double lowValue = thresholdDTO.getLowValue();
                double highValue = thresholdDTO.getHighValue();

                if ((double) processedValue < lowValue || (double) processedValue > highValue) {
                    System.out.println("Threshold crossed: Parameter " + thresholdDTO.getParameter().getName() +
                            " with value " + processedValue + " is out of range [" + lowValue + ", " + highValue + "]");
                } else {
                    System.out.println("Threshold successful");
                }
            }
        }
    }

    private Object applyModifications(DataType dataType, Object castValue,
                                      Double additive, Double coefficient) {
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
            default -> throw new IllegalArgumentException("Unsupported data type: "
                    + dataType);
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