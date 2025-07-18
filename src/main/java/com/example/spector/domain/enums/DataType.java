package com.example.spector.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DataType {
    INTEGER("Целочисленный"),
    DOUBLE("Дробный"),
    LONG("Большое целое число"),
    STRING("Строковый"),
    ENUMERATED("Перечисляемый");

    private final String displayName;

}
