package com.example.spector.converter;

import com.example.spector.domain.enums.DataType;

public class TypeCasterFactory {
    public static TypeCaster getTypeCaster(DataType dataType) {
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
            default -> throw new IllegalArgumentException("Unsupported data type: " + dataType);
        }
    }
}
