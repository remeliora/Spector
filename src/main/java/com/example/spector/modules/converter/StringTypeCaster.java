package com.example.spector.modules.converter;

import com.example.spector.domain.enums.EventType;
import com.example.spector.domain.enums.MessageType;
import com.example.spector.domain.parameter.Parameter;
import com.example.spector.modules.event.EventDispatcher;
import com.example.spector.modules.event.EventMessage;
import lombok.RequiredArgsConstructor;
import org.snmp4j.smi.Variable;

@RequiredArgsConstructor
public class StringTypeCaster implements TypeCaster<String> {
    private final EventDispatcher eventDispatcher;

    @Override
    public String cast(Parameter parameter, Variable variable) {
        if (variable == null) {
            eventDispatcher.dispatch(EventMessage.log(EventType.SYSTEM, MessageType.ERROR,
                    "Пустое значение!"));

            return null;
        }

        return variable.toString();
    }
}
