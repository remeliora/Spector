package com.example.spector.event.channels;

import com.example.spector.domain.enums.EventType;
import com.example.spector.event.EventMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

@Component
public class LogChannel implements EventChannel {
    private static final Logger logger = LoggerFactory.getLogger(LogChannel.class);
    private static final Logger deviceLogger = LoggerFactory.getLogger("DeviceLogger");

    @Override
    public void handle(EventMessage event) {
        try {
            if (event.getEventType() == EventType.SYSTEM) {
                logSystemEvent(event);
            } else if (event.getEventType() == EventType.DEVICE /*&& event.getDeviceName() != null*/) {
//                MDC.put("deviceName", event.getDeviceName());
                logDeviceEvent(event);
            }
        } finally {
//            MDC.clear();
        }
    }

    private void logDeviceEvent(EventMessage event) {
        switch (event.getMessageType()) {
            case INFO -> deviceLogger.info(formatMessage(event));
            case ERROR -> deviceLogger.error(formatMessage(event));
        }
    }

    private void logSystemEvent(EventMessage event) {
        switch (event.getMessageType()) {
            case INFO -> logger.info(formatMessage(event));
            case ERROR -> logger.error(formatMessage(event));
        }
    }

    private String formatMessage(EventMessage event) {
        return event.getMessage();
    }
}
