package com.example.spector.checker;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.InetAddress;

@Service
@RequiredArgsConstructor
public class DeviceConnectionChecker {
//    private final SNMPService snmpService;
    private static final Logger logger = LoggerFactory.getLogger(DeviceConnectionChecker.class);

    // Метод проверки доступности по IP
    public boolean isAvailableByIP(String ipAddress) {
        try {
            boolean reachable = InetAddress.getByName(ipAddress).isReachable(3000);
            if (!reachable) {
//                System.out.println("Device " + ipAddress + " is not reachable by IP.");
                logger.error("Device {} is not reachable by IP.", ipAddress);
            }

            return reachable;
        } catch (IOException e) {
//            System.out.println("Error checking network reachability: " + e.getMessage());
            logger.error("Error checking network reachability: {}", e.getMessage());

            return false;
        }
    }

//    // Метод проверки доступности по SNMP
//    public boolean isAvailableBySNMP(String ipAddress) {
//        return snmpService.isAvailableBySNMP(ipAddress);
//    }

    // Общий метод проверки доступности
//    public boolean isDeviceAvailable(String ipAddress) {
//        boolean availableByIP = isAvailableByIP(ipAddress);
//        boolean availableBySNMP = isAvailableBySNMP(ipAddress);
//
//        if (availableByIP || availableBySNMP) {
//            System.out.println("Device " + ipAddress + " is available.");
//            return true;
//        } else {
//            System.out.println("Device " + ipAddress + " is not available.");
//            return false;
//        }
//    }
}
