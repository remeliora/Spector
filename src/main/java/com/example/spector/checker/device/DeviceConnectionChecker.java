package com.example.spector.checker.device;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.InetAddress;

@Service
@RequiredArgsConstructor
public class DeviceConnectionChecker {
    private static final Logger logger = LoggerFactory.getLogger(DeviceConnectionChecker.class);
    private static final Logger deviceLogger = LoggerFactory.getLogger("DeviceLogger");

    // Метод проверки доступности по IP
    public boolean isAvailableByIP(String ipAddress) {
        try {
            boolean reachable = InetAddress.getByName(ipAddress).isReachable(3000);
            if (!reachable) {
//                System.out.println("Device " + ipAddress + " is not reachable by IP.");
                logger.error("Device {} is not reachable by IP.", ipAddress);
                deviceLogger.error("Device {} is not reachable by IP.", ipAddress);
            }

            return reachable;
        } catch (IOException e) {
//            System.out.println("Error checking network reachability: " + e.getMessage());
            logger.error("Error checking network reachability: {}", e.getMessage());
            deviceLogger.error("Error checking network reachability: {}", e.getMessage());

            return false;
        }
    }
}
