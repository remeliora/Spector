package com.example.spector.converter;

import com.example.spector.domain.enums.DataType;
import com.example.spector.domain.enums.EventType;
import com.example.spector.domain.enums.MessageType;
import com.example.spector.event.EventDispatcher;
import com.example.spector.event.EventMessage;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TypeCasterFactory {
    private final EventDispatcher eventDispatcher;
//    private static final Logger logger = LoggerFactory.getLogger(TypeCasterFactory.class);

    public TypeCaster<?> getTypeCaster(DataType dataType) {
        switch (dataType) {
            case INTEGER -> {
                return new IntegerTypeCaster(eventDispatcher);
            }
            case DOUBLE -> {
                return new DoubleTypeCaster(eventDispatcher);
            }
            case LONG -> {
                return new LongTypeCaster(eventDispatcher);
            }
            case STRING -> {
                return new StringTypeCaster(eventDispatcher);
            }
            default -> {
//                logger.error("Unsupported data type: {}", dataType);
                eventDispatcher.dispatch(EventMessage.log(EventType.SYSTEM, MessageType.ERROR,
                        "Неподдерживаемый тип данных: " + dataType));
                throw new IllegalArgumentException("Unsupported data type: " + dataType);
            }
        }
    }
}
