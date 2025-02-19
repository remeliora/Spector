package com.example.spector.handler;

import com.example.spector.database.mongodb.EnumeratedStatusService;
import com.example.spector.domain.dto.DeviceDTO;
import com.example.spector.domain.dto.ParameterDTO;
import com.example.spector.domain.dto.ThresholdDTO;
import com.example.spector.domain.enums.EventType;
import com.example.spector.domain.enums.MessageType;
import com.example.spector.event.EventDispatcher;
import com.example.spector.event.EventMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class EnumeratedParameterHandler implements ParameterHandler {
    private final EventDispatcher eventDispatcher;
    private final EnumeratedStatusService enumeratedStatusService;

    @Override
    public Object handleParameter(DeviceDTO deviceDTO, ParameterDTO parameterDTO,
                                  Object value, List<ThresholdDTO> thresholdDTOList) {
        Integer intValue = (Integer) value;
        Map<Integer, String> statusMap = enumeratedStatusService.getStatusName(parameterDTO.getName());
        // Преобразуем фактическое значение в статусную строку
        String actualStatus = statusMap.getOrDefault(intValue, "Неизвестный ключ");

        for (ThresholdDTO thresholdDTO : thresholdDTOList) {
            if (thresholdDTO.getDevice().getId().equals(deviceDTO.getId())) {
                int matchExact = thresholdDTO.getMatchExact();
                // Преобразуем допустимое значение порога в статусную строку, если возможно
                String allowedStatus = statusMap.getOrDefault(matchExact, String.valueOf(matchExact));
                if (matchExact != intValue) {
                    eventDispatcher.dispatch(EventMessage.log(EventType.SYSTEM, MessageType.ERROR,
                            "Нарушение порога: " + thresholdDTO.getParameter().getName() +
                                    " = " + actualStatus  + ". Допустимое значение [" + allowedStatus + "]"));
                    eventDispatcher.dispatch(EventMessage.log(EventType.DEVICE, MessageType.ERROR,
                            "Нарушение порога: " + thresholdDTO.getParameter().getName() +
                                    " = " + actualStatus + ". Допустимое значение [" + allowedStatus + "]"));
                }
            }
        }

//        // Преобразуем число в строку-статус
//        String status = statusMap.get(intValue);
//        if (status == null) {
//            eventDispatcher.dispatch(EventMessage.log(EventType.SYSTEM, MessageType.ERROR,
//                    "Не найдено значение ключа " + intValue + " для: " + parameterDTO.getName()));
//            status = "Неизвестный ключ";
//        }
        return actualStatus;
    }
}
