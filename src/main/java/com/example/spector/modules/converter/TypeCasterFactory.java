package com.example.spector.modules.converter;

import com.example.spector.database.postgres.PollingDataService;
import com.example.spector.domain.enums.DataType;
import com.example.spector.domain.enums.EventType;
import com.example.spector.domain.enums.MessageType;
import com.example.spector.modules.event.EventDispatcher;
import com.example.spector.modules.event.EventMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TypeCasterFactory {
    private final EventDispatcher eventDispatcher;
    private final PollingDataService pollingDataService;

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
            case ENUMERATED -> {
                return new EnumeratedTypeCaster(eventDispatcher, pollingDataService);
            }
            default -> {
                eventDispatcher.dispatch(EventMessage.log(EventType.SYSTEM, MessageType.ERROR,
                        "Неподдерживаемый тип данных: " + dataType));
                throw new IllegalArgumentException("Unsupported data type: " + dataType);
            }
        }
    }
}
