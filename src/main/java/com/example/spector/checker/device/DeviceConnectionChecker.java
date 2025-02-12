package com.example.spector.checker.device;

import com.example.spector.domain.enums.EventType;
import com.example.spector.domain.enums.MessageType;
import com.example.spector.event.EventDispatcher;
import com.example.spector.event.EventMessage;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.InetAddress;

@Service
@RequiredArgsConstructor
public class DeviceConnectionChecker {
    private final EventDispatcher eventDispatcher;
//    private static final Logger logger = LoggerFactory.getLogger(DeviceConnectionChecker.class);
//    private static final Logger deviceLogger = LoggerFactory.getLogger("DeviceLogger");

    // Метод проверки доступности по IP
    public boolean isAvailableByIP(String ipAddress) {
        try {
            boolean reachable = InetAddress.getByName(ipAddress).isReachable(3000);
            if (!reachable) {
//                System.out.println("Device " + ipAddress + " is not reachable by IP.");
//                logger.error("Device {} is not reachable by IP.", ipAddress);
                eventDispatcher.dispatch(EventMessage.log(EventType.SYSTEM, MessageType.ERROR,
                        "Устройство " + ipAddress + " не доступно IP."));
//                deviceLogger.error("Device {} is not reachable by IP.", ipAddress);
                eventDispatcher.dispatch(EventMessage.log(EventType.DEVICE, MessageType.ERROR,
                        "Устройство " + ipAddress + " не доступно IP."));
            }

            return reachable;
        } catch (IOException e) {
//            System.out.println("Error checking network reachability: " + e.getMessage());
//            logger.error("Error checking network reachability: {}", e.getMessage());
            eventDispatcher.dispatch(EventMessage.log(EventType.SYSTEM, MessageType.ERROR,
                    "Ошибка при проверке подключения: " + e.getMessage()));
//            deviceLogger.error("Error checking network reachability: {}", e.getMessage());
            eventDispatcher.dispatch(EventMessage.log(EventType.DEVICE, MessageType.ERROR,
                    "Ошибка при проверке подключения: " + e.getMessage()));

            return false;
        }
    }
}
