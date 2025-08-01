package com.example.spector.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AlarmType {
    INACTION("Не отслеживать"),
    TELEGRAM("Только в Telegram"),
    DATABASE("Только в БД"),
    EVERYWHERE("Сигнализировать везде");

    private final String displayName;

}
