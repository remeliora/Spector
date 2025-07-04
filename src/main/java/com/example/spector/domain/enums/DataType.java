package com.example.spector.domain.enums;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum DataType {
    INTEGER("Целое число"),
    DOUBLE("Дробное число"),
    LONG("Большое целое число"),
    STRING("Строковое значение");

    private final String displayName;

    public String getDisplayName() {
        return displayName;
    }
}
