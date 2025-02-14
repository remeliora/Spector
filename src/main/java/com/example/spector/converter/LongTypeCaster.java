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
public class LongTypeCaster implements TypeCaster<Long> {
    private final EventDispatcher eventDispatcher;

    @Override
    public Long cast(Variable variable) {
        if (variable == null) {
            eventDispatcher.dispatch(EventMessage.log(EventType.SYSTEM, MessageType.ERROR,
                    "Пустое значение!"));

            return null;
        }

        if (variable instanceof Integer32) {
            return (long) variable.toInt();
        } else if (variable instanceof Counter32) {
            return ((Counter32) variable).getValue();
        } else if (variable instanceof Gauge32) {
            return ((Gauge32) variable).getValue();
        } else if (variable instanceof Counter64) {
            return ((Counter64) variable).getValue();
        } else if (variable instanceof UnsignedInteger32) {
            return variable.toLong();
        } else {
            eventDispatcher.dispatch(EventMessage.log(EventType.SYSTEM, MessageType.ERROR,
                    "Невозможно конвертировать в LONG: " + variable.getClass().getSimpleName() + " = " + variable));
            throw new IllegalArgumentException("Unsupported Variable type for long casting: " + variable.getClass().getSimpleName());
        }
    }
}
