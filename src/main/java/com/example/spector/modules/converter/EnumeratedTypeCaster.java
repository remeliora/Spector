package com.example.spector.modules.converter;

import com.example.spector.database.mongodb.EnumeratedStatusService;
import com.example.spector.domain.dto.parameter.ParameterDTO;
import com.example.spector.domain.enums.EventType;
import com.example.spector.domain.enums.MessageType;
import com.example.spector.modules.event.EventDispatcher;
import com.example.spector.modules.event.EventMessage;
import lombok.RequiredArgsConstructor;
import org.snmp4j.smi.Variable;

import java.util.Map;

@RequiredArgsConstructor
public class EnumeratedTypeCaster implements TypeCaster<String> {
    private final EventDispatcher eventDispatcher;
    private final EnumeratedStatusService enumeratedStatusService;

    @Override
    public String cast(ParameterDTO parameterDTO, Variable variable) {
        if (variable == null) {
            eventDispatcher.dispatch(EventMessage.log(EventType.SYSTEM, MessageType.ERROR,
                    "Пустое значение!"));
            return null;
        }

        try {
            int intValue = variable.toInt();
            // Получаем словарь статусов для конкретного параметра
            Map<Integer, String> statusMap = enumeratedStatusService.getStatusName(parameterDTO.getName());

            return statusMap.getOrDefault(intValue, "UNKNOWN(" + intValue + ")");
        } catch (Exception e) {
            eventDispatcher.dispatch(EventMessage.log(EventType.SYSTEM, MessageType.ERROR,
                    "Невозможно конвертировать в ENUMERATED: " + variable));

            return "CONVERSION_ERROR";
        }
    }
}
