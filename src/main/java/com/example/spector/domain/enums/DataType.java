package com.example.spector.domain.enums;

public enum DataType {
    INTEGER(Integer.class),
    DOUBLE(Double.class),
    LONG(Long.class),
    STRING(String.class);

    private final Class<?> javaClass;
    DataType(Class<?> javaClass) {
        this.javaClass = javaClass;
    }

    public Class<?> getJavaClass() {
        return javaClass;
    }
}
