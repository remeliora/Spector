package com.example.spector.checker;

import lombok.RequiredArgsConstructor;
import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.InetAddress;

@Service
@RequiredArgsConstructor
public class DeviceConnectionChecker {
    // Метод проверки доступности по IP
    public boolean isAvailableByIP(String ipAddress) {
        try {
            boolean reachable = InetAddress.getByName(ipAddress).isReachable(3000);
            if (reachable) {
                System.out.println("Device " + ipAddress + " is reachable by IP.");
            } else {
                System.out.println("Device " + ipAddress + " is not reachable by IP.");
            }

            return reachable;
        } catch (IOException e) {
            System.out.println("Error checking network reachability: " + e.getMessage());

            return false;
        }
    }

    // Метод проверки доступности по SNMP
    public boolean isAvailableBySNMP(String ipAddress) {
        try {
            Snmp snmp = new Snmp(new DefaultUdpTransportMapping());
            snmp.listen();

            CommunityTarget target = new CommunityTarget();
            target.setCommunity(new OctetString("public"));
            target.setAddress(new UdpAddress(ipAddress + "/161"));
            target.setVersion(SnmpConstants.version1);
            target.setRetries(3);
            target.setTimeout(1500);

            PDU pdu = new PDU();
            pdu.setType(PDU.GET);

            ResponseEvent<?> responseEvent = snmp.send(pdu, target);

            snmp.close();

            if (responseEvent.getResponse() != null && responseEvent.getResponse().getErrorStatus() == PDU.noError) {
                System.out.println("Device " + ipAddress + " is reachable by SNMP.");

                return true;
            } else {
                System.out.println("Device " + ipAddress + " is not reachable by SNMP.");

                return false;
            }
        } catch (IOException e) {
            System.out.println("SNMP access error: " + e.getMessage());

            return false;
        }
    }

    // Общий метод проверки доступности
    public boolean isDeviceAvailable(String ipAddress) {
        boolean availableByIP = isAvailableByIP(ipAddress);
        boolean availableBySNMP = isAvailableBySNMP(ipAddress);

        if (availableByIP || availableBySNMP) {
            System.out.println("Device " + ipAddress + " is available.");
            return true;
        } else {
            System.out.println("Device " + ipAddress + " is not available.");
            return false;
        }
    }
}
