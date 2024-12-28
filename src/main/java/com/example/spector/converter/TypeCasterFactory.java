package com.example.spector.converter;

import com.example.spector.domain.enums.DataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TypeCasterFactory {
    private static final Logger logger = LoggerFactory.getLogger(TypeCasterFactory.class);

    public static TypeCaster<?> getTypeCaster(DataType dataType) {
        switch (dataType) {
            case INTEGER -> {
                return new IntegerTypeCaster();
            }
            case DOUBLE -> {
                return new DoubleTypeCaster();
            }
            case LONG -> {
                return new LongTypeCaster();
            }
            case STRING -> {
                return new StringTypeCaster();
            }
            default -> {
                logger.error("Unsupported data type: {}", dataType);
                throw new IllegalArgumentException("Unsupported data type: " + dataType);
            }
        }
    }
}
