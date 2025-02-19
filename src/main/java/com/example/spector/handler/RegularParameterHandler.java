package com.example.spector.handler;

import com.example.spector.domain.dto.DeviceDTO;
import com.example.spector.domain.dto.ParameterDTO;
import com.example.spector.domain.dto.ThresholdDTO;
import com.example.spector.domain.enums.DataType;
import com.example.spector.domain.enums.EventType;
import com.example.spector.domain.enums.MessageType;
import com.example.spector.event.EventDispatcher;
import com.example.spector.event.EventMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class RegularParameterHandler implements ParameterHandler {
    private final EventDispatcher eventDispatcher;

    @Override
    public Object handleParameter(DeviceDTO deviceDTO, ParameterDTO parameterDTO,
                                  Object value, List<ThresholdDTO> thresholds) {
        Object processedValue = applyModifications(DataType.valueOf(parameterDTO.getDataType()), value,
                parameterDTO.getAdditive(), parameterDTO.getCoefficient());

        for (ThresholdDTO thresholdDTO : thresholds) {
            if (thresholdDTO.getDevice().getId().equals(deviceDTO.getId())) {
                double lowValue = thresholdDTO.getLowValue();
                double highValue = thresholdDTO.getHighValue();

                if ((double) processedValue < lowValue || (double) processedValue > highValue) {
                    eventDispatcher.dispatch(EventMessage.log(EventType.SYSTEM, MessageType.ERROR,
                            "Нарушение порога: " + thresholdDTO.getParameter().getName() +
                            " = " + processedValue + ". Допустимый диапазон [" + lowValue + "; " + highValue + "]"));
                    eventDispatcher.dispatch(EventMessage.log(EventType.DEVICE, MessageType.ERROR,
                            "Нарушение порога: " + thresholdDTO.getParameter().getName() +
                                    " = " + processedValue + ". Допустимый диапазон [" + lowValue + "; " + highValue + "]"));
                }
            }
        }

        return processedValue;
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
