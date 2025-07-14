package com.example.spector.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DataType {
    INTEGER("Целое число"),
    DOUBLE("Дробное число"),
    LONG("Большое целое число"),
    STRING("Строковое значение");

    private final String displayName;

}
