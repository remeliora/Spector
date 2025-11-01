package com.example.spector.modules.cache;

import com.example.spector.domain.DeviceCurrentData;
import com.example.spector.modules.datapattern.ParameterData;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RealTimeDataService {
    private final DeviceCurrentDataCache deviceCurrentDataCache; // Ваш кэширующий сервис
    private final ApplicationEventPublisher eventPublisher; // Для публикации события

    /**
     * Обновляет кэш актуальных данных и публикует событие.
     *
     * @param deviceId ID устройства.
     * @param snmpData Полные данные, полученные в SnmpPoller (после DAOService.writeData).
     */
    public void updateAndNotify(Long deviceId, Map<String, Object> snmpData) {
        // Преобразуем snmpData в DeviceCurrentData
        DeviceCurrentData currentData = mapToCurrentData(deviceId, snmpData);

        // Обновляем кэш
        deviceCurrentDataCache.updateCurrentData(deviceId, currentData);

        // Публикуем событие
        DeviceDataUpdatedEvent event = new DeviceDataUpdatedEvent(this, deviceId, currentData);
        eventPublisher.publishEvent(event);

//        System.out.println("Отправлено обновление для устройства " + deviceId + " в кэш и опубликовано событие.");
    }

    // Вспомогательный метод для преобразования
    private DeviceCurrentData mapToCurrentData(Long deviceId, Map<String, Object> snmpData) {
        DeviceCurrentData currentData = new DeviceCurrentData();
        // Устанавливаем нужные поля из snmpData
        currentData.setDeviceId((Long) snmpData.get("deviceId"));
        currentData.setDeviceName((String) snmpData.get("deviceName"));
        currentData.setDeviceIp((String) snmpData.get("deviceIp"));
        currentData.setLocation((String) snmpData.get("location"));
        currentData.setStatus((String) snmpData.get("status"));
        currentData.setLastPollingTime((LocalDateTime) snmpData.get("lastPollingTime"));

        // Приведение типов к List<ParameterData> может потребовать осторожности
        @SuppressWarnings("unchecked")
        List<ParameterData> params = (List<ParameterData>) snmpData.get("parameters");
        currentData.setParameters(params);

        return currentData;
    }
}
