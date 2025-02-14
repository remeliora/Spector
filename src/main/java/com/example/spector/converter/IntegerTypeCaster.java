package com.example.spector.converter;

import com.example.spector.domain.enums.EventType;
import com.example.spector.domain.enums.MessageType;
import com.example.spector.event.EventDispatcher;
import com.example.spector.event.EventMessage;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snmp4j.smi.*;

@RequiredArgsConstructor
public class IntegerTypeCaster implements TypeCaster<Integer> {
    private final EventDispatcher eventDispatcher;

    @Override
    public Integer cast(Variable variable) {
        if (variable == null) {
            eventDispatcher.dispatch(EventMessage.log(EventType.SYSTEM, MessageType.ERROR,
                    "Пустое значение!"));

            return null;
        }

        if (variable instanceof Integer32) {
            return variable.toInt();
        } else if (variable instanceof Counter32) {
            return (int) ((Counter32) variable).getValue();
        } else if (variable instanceof Gauge32) {
            return (int) ((Gauge32) variable).getValue();
        } else if (variable instanceof Counter64) {
            return (int) ((Counter64) variable).getValue();
        } else if (variable instanceof UnsignedInteger32) {
            return (int) variable.toLong();
        } else {
            eventDispatcher.dispatch(EventMessage.log(EventType.SYSTEM, MessageType.ERROR,
                    "Невозможно конвертировать в INTEGER: " + variable.getClass().getSimpleName() + " = " + variable));
            throw new IllegalArgumentException("Unsupported Variable type for integer casting: " + variable.getClass().getSimpleName());
        }
    }
}
