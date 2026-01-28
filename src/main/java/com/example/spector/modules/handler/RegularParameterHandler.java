package com.example.spector.modules.handler;

import com.example.spector.domain.device.Device;
import com.example.spector.domain.enums.AlarmType;
import com.example.spector.domain.enums.DataType;
import com.example.spector.domain.enums.EventType;
import com.example.spector.domain.enums.MessageType;
import com.example.spector.domain.parameter.Parameter;
import com.example.spector.domain.setting.AppSetting;
import com.example.spector.domain.threshold.Threshold;
import com.example.spector.modules.datapattern.ResultValue;
import com.example.spector.modules.event.EventDispatcher;
import com.example.spector.modules.event.EventMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class RegularParameterHandler implements ParameterHandler {
    private final EventDispatcher eventDispatcher;

    @Override
    public ResultValue handleParameter(Device device, Parameter parameter, Object value,
                                       List<Threshold> thresholds, AppSetting appSetting) {
        DataType dataType = parameter.getDataType();
        // Применяем модификации только для числовых типов
        Object processedValue = (dataType != DataType.STRING)
                ? applyModifications(dataType, value, parameter.getAdditive(), parameter.getCoefficient())
                : value;

        String status = "OK";

        // Фильтруем пороги для текущего устройства
        List<Threshold> deviceThresholds = thresholds.stream()
                .filter(t -> t.getDevice().getId().equals(device.getId()))
                .toList();

        // Если порогов нет для текущего устройства - сразу статус INACTIVE
        if (deviceThresholds.isEmpty()) {
            status = "INACTIVE";
        } else {
            for (Threshold threshold : deviceThresholds) {
                double doubleValue = ((Number) processedValue).doubleValue();
                double lowValue = threshold.getLowValue();
                double highValue = threshold.getHighValue();

                if (doubleValue < lowValue || doubleValue > highValue) {
                    status = "ERROR";
                    eventDispatcher.dispatch(EventMessage.log(EventType.SYSTEM, MessageType.ERROR,
                            device.getName() + ": " + parameter.getDescription() + " = " +
                            doubleValue + ". Допустимый диапазон [" + lowValue + "; " + highValue + "]"));
                    eventDispatcher.dispatch(EventMessage.log(EventType.DEVICE, MessageType.ERROR,
                            threshold.getParameter().getName() + " = " + doubleValue +
                            ". Допустимый диапазон [" + lowValue + "; " + highValue + "]"));
                    eventDispatcher.dispatch(EventMessage.db(EventType.DB, MessageType.ERROR, AlarmType.EVERYWHERE,
                            appSetting.getAlarmActive(), device.getPeriod(),
                            device.getName() + ": " + parameter.getDescription() + " = " +
                            doubleValue + " [" + lowValue + "; " + highValue + "]"));
                }
            }
        }

        return new ResultValue(processedValue, status);
    }

    private Object applyModifications(DataType dataType, Object value,
                                      Double additive, Double coefficient) {
        switch (dataType) {
            case INTEGER -> value = (int) (((int) value + additive) * coefficient);
            case DOUBLE -> value = (((double) value + additive) * coefficient);
            case LONG -> value = (long) (((long) value + additive) * coefficient);
            case STRING -> {
            }
            default -> {
                eventDispatcher.dispatch(EventMessage.log(EventType.SYSTEM, MessageType.ERROR,
                        "Неподдерживаемый тип данных: " + dataType));
                eventDispatcher.dispatch(EventMessage.log(EventType.DEVICE, MessageType.ERROR,
                        "Неподдерживаемый тип данных: " + dataType));
                throw new IllegalArgumentException("Неподдерживаемый тип данных: " + dataType);
            }
        }

        return value;
    }
}
