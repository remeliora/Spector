package com.example.spector.script;

import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.TransportMapping;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.Address;
import org.snmp4j.smi.GenericAddress;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.transport.DefaultUdpTransportMapping;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class SnmpScan {
    public static void main(String[] args) {
        // Замените значения ниже на свои
        String baseIpAddress = "192.168.1."; // Базовый IP-адрес вашей сети
        String community = "public"; // SNMP community строка
        int startIpRange = 1; // Начальный IP адрес для сканирования
        int endIpRange = 255; // Конечный IP адрес для сканирования
        int retries = 2; // Количество попыток
        int timeout = 1000; // Таймаут в миллисекундах

        try {
            TransportMapping<?> transport = new DefaultUdpTransportMapping();
            Snmp snmp = new Snmp(transport);
            transport.listen();

            CommunityTarget target = new CommunityTarget();
            target.setCommunity(new OctetString(community));
            target.setRetries(retries);
            target.setTimeout(timeout);
            target.setVersion(SnmpConstants.version1);

            // Определите OID, который вы хотите запросить
            OID oid = new OID("1.3.6.1.2.1.1.1.0"); // Пример OID для sysDescr
//            OID oid = new OID("1.3.6.1.2.1.33.1.2.1.0"); // Пример OID для sysDescr

            PDU pdu = new PDU();
            pdu.add(new org.snmp4j.smi.VariableBinding(oid));
            pdu.setType(PDU.GET);


//            List<String> ipList = new ArrayList<String>();
//            ipList.add("192.168.1.41");
//            ipList.add("192.168.1.42");
//            ipList.add("192.168.1.40");
//            ipList.add("192.168.1.35");
//            ipList.add("192.168.1.43");
//            ipList.add("192.168.1.32");


//            for (String ipValue : ipList) {
            for (int i = startIpRange; i <= endIpRange; i++) {
                String targetAddressStr = baseIpAddress + i + "/161";
//                String targetAddressStr = ipValue + "/161";
                Address targetAddress = GenericAddress.parse(targetAddressStr);
                target.setAddress(targetAddress);

                ResponseEvent event = snmp.send(pdu, target);
                if (event != null && event.getResponse() != null) {
                    System.out.println("Устройство найдено на " + targetAddressStr + ". Ответ: " +
                            event.getResponse().get(0).getVariable());
//                } else {
//                    // Если не удалось получить ответ, отправляем SNMP-ловушку
//                    PDU trapPDU = new PDU();
//                    trapPDU.setType(PDU.TRAP);
//                    trapPDU.add(new org.snmp4j.smi.VariableBinding(SnmpConstants.sysUpTime));
//                    snmp.send(trapPDU, target);
//                    System.out.println("Ловушка отправлена на " + targetAddressStr);
                }
            }

            snmp.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
