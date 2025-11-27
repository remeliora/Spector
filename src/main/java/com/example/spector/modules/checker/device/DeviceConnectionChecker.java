package com.example.spector.modules.checker.device;

import com.example.spector.domain.enums.EventType;
import com.example.spector.domain.enums.MessageType;
import com.example.spector.modules.event.EventDispatcher;
import com.example.spector.modules.event.EventMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.InetAddress;

@Service
@RequiredArgsConstructor
public class DeviceConnectionChecker {
    private final EventDispatcher eventDispatcher;

    // Метод проверки доступности по IP
    public boolean isAvailableByIP(String ipAddress) {
        try {
            boolean reachable = InetAddress.getByName(ipAddress).isReachable(5000);
            if (!reachable) {
//                System.out.println("Device " + ipAddress + " is not reachable by IP.");
//                eventDispatcher.dispatch(EventMessage.log(EventType.SYSTEM, MessageType.ERROR,
//                        "Устройство " + ipAddress + " не доступно IP."));
                eventDispatcher.dispatch(EventMessage.log(EventType.DEVICE, MessageType.ERROR,
                        "Устройство " + ipAddress + " не доступно IP."));
            }

            return reachable;
        } catch (IOException e) {
//            System.out.println("Error checking network reachability: " + e.getMessage());
            eventDispatcher.dispatch(EventMessage.log(EventType.SYSTEM, MessageType.ERROR,
                    "Ошибка при проверке подключения: " + e.getMessage()));
            eventDispatcher.dispatch(EventMessage.log(EventType.DEVICE, MessageType.ERROR,
                    "Ошибка при проверке подключения: " + e.getMessage()));

            return false;
        }
    }
}
