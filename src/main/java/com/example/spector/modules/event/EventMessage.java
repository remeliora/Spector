package com.example.spector.modules.event;

import com.example.spector.domain.enums.AlarmType;
import com.example.spector.domain.enums.EventType;
import com.example.spector.domain.enums.MessageType;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class EventMessage {
    private final boolean alarmActive;
    private final EventType eventType;
    private final MessageType messageType;
    private final AlarmType alarmType;
    private final String message;
    private final Integer period;
//    private final String deviceName;

//    @Singular
//    private final Map<String, Object> details;

    //  Фабричный метод для логов (без лишних аргументов)
    public static EventMessage log(EventType eventType, MessageType messageType, String message
            /*, String deviceName*/) {
        return EventMessage.builder()
                .eventType(eventType)
                .messageType(messageType)
                .message(message)
//                .deviceName(deviceName)
                .build();
    }

    //  Фабричный метод для базы данных (со всеми аргументами)
    public static EventMessage db(EventType eventType, MessageType messageType, AlarmType alarmType,
                                  boolean alarmActive, Integer period, String message
            /*, Map<String, Object> details*/) {
        return EventMessage.builder()
                .eventType(eventType)
                .messageType(messageType)
                .alarmType(alarmType)
                .alarmActive(alarmActive)
                .period(period)
                .message(message)
//                .details(details)
                .build();
    }
}
