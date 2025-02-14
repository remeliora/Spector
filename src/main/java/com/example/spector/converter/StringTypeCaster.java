package com.example.spector.converter;

import com.example.spector.domain.enums.EventType;
import com.example.spector.domain.enums.MessageType;
import com.example.spector.event.EventDispatcher;
import com.example.spector.event.EventMessage;
import lombok.RequiredArgsConstructor;
import org.snmp4j.smi.Variable;

@RequiredArgsConstructor
public class StringTypeCaster implements TypeCaster<String> {
    private final EventDispatcher eventDispatcher;
//    private static final Logger logger = LoggerFactory.getLogger(StringTypeCaster.class);

    @Override
    public String cast(Variable variable) {
        if (variable == null) {
//            logger.error("Variable is empty");
            eventDispatcher.dispatch(EventMessage.log(EventType.SYSTEM, MessageType.ERROR,
                    "Пустое значение!"));

            return null;
        }

        return variable.toString();
    }
}
