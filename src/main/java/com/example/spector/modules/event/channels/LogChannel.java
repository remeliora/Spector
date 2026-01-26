package com.example.spector.modules.event.channels;

import com.example.spector.modules.event.EventMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class LogChannel implements EventChannel {
    private static final Logger logger = LoggerFactory.getLogger(LogChannel.class);
    private static final Logger deviceLogger = LoggerFactory.getLogger("DeviceLogger");
    private static final Logger requestLogger = LoggerFactory.getLogger("REQUEST_LOGGER");

    @Override
    public void handle(EventMessage event) {
        try {
//            if (event.getEventType() == EventType.SYSTEM) {
//                logSystemEvent(event);
//            } else if (event.getEventType() == EventType.DEVICE) {
//                logDeviceEvent(event);
//            }
            switch (event.getEventType()) {
                case SYSTEM -> logSystemEvent(event);
                case DEVICE -> logDeviceEvent(event);
                case REQUEST -> logRequestEvent(event); // Новое событие
                default -> {
                }
            }
        } finally {
//            MDC.clear();
        }
    }

    private void logRequestEvent(EventMessage event) {
        switch (event.getMessageType()) {
            case INFO -> requestLogger.info(formatMessage(event));
            case ERROR -> requestLogger.error(formatMessage(event));
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
