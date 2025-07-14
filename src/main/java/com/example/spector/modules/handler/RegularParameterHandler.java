package com.example.spector.modules.handler;

import com.example.spector.domain.ResultValue;
import com.example.spector.domain.dto.AppSettingDTO;
import com.example.spector.domain.dto.device.DeviceDTO;
import com.example.spector.domain.dto.parameter.ParameterDTO;
import com.example.spector.domain.dto.threshold.ThresholdDTO;
import com.example.spector.domain.enums.AlarmType;
import com.example.spector.domain.enums.DataType;
import com.example.spector.domain.enums.EventType;
import com.example.spector.domain.enums.MessageType;
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
    public ResultValue handleParameter(DeviceDTO deviceDTO, ParameterDTO parameterDTO, Object value,
                                       List<ThresholdDTO> thresholds, AppSettingDTO appSettingDTO) {
        Object processedValue = applyModifications(DataType.valueOf(parameterDTO.getDataType()), value,
                parameterDTO.getAdditive(), parameterDTO.getCoefficient());
        String status = "OK";

        // Фильтруем пороги для текущего устройства
        List<ThresholdDTO> deviceThresholds = thresholds.stream()
                .filter(t -> t.getDevice().getId().equals(deviceDTO.getId()))
                .toList();

        // Если порогов нет для текущего устройства - сразу статус INACTIVE
        if (deviceThresholds.isEmpty()) {
            status = "INACTIVE";
        } else {
            for (ThresholdDTO thresholdDTO : deviceThresholds) {
                double lowValue = thresholdDTO.getLowValue();
                double highValue = thresholdDTO.getHighValue();

                if ((double) processedValue < lowValue || (double) processedValue > highValue) {
                    status = "ERROR";
                    eventDispatcher.dispatch(EventMessage.log(EventType.SYSTEM, MessageType.ERROR,
                            deviceDTO.getName() + ": " + parameterDTO.getDescription() + " = " +
                            processedValue + ". Допустимый диапазон [" + lowValue + "; " + highValue + "]"));
                    eventDispatcher.dispatch(EventMessage.log(EventType.DEVICE, MessageType.ERROR,
                            thresholdDTO.getParameter().getName() + " = " + processedValue +
                            ". Допустимый диапазон [" + lowValue + "; " + highValue + "]"));
                    eventDispatcher.dispatch(EventMessage.db(EventType.DB, MessageType.ERROR, AlarmType.EVERYWHERE,
                            appSettingDTO.getAlarmActive(), deviceDTO.getPeriod(),
                            deviceDTO.getName() + ": " + parameterDTO.getDescription() + " = " +
                            processedValue + " [" + lowValue + "; " + highValue + "]"));
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

        return value;
    }
}
