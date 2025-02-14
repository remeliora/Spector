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
public class DoubleTypeCaster implements TypeCaster<Double> {
    private final EventDispatcher eventDispatcher;
//    private static final Logger logger = LoggerFactory.getLogger(DoubleTypeCaster.class);

    @Override
    public Double cast(Variable variable) {
        if (variable == null) {
//            logger.error("Variable is empty");
            eventDispatcher.dispatch(EventMessage.log(EventType.SYSTEM, MessageType.ERROR,
                    "Пустое значение!"));

            return null;
        }

        if (variable instanceof Integer32) {
            return (double) variable.toInt();
        } else if (variable instanceof Counter32) {
            return (double) ((Counter32) variable).getValue();
        } else if (variable instanceof Gauge32) {
            return (double) ((Gauge32) variable).getValue();
        } else if (variable instanceof Counter64) {
            return (double) ((Counter64) variable).getValue();
        } else if (variable instanceof UnsignedInteger32) {
            return (double) variable.toLong();
        } else {
//            logger.error("Unsupported Variable type for double casting: {} with value: {}",
//                    variable.getClass().getSimpleName(), variable);
            eventDispatcher.dispatch(EventMessage.log(EventType.SYSTEM, MessageType.ERROR,
                    "Невозможно конвертировать в DOUBLE: " + variable.getClass().getSimpleName() + " = " + variable));
            throw new IllegalArgumentException("Unsupported Variable type for double casting: " + variable.getClass().getSimpleName());
        }
    }
}
