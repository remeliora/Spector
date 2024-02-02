package com.example.spector.script;

import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.*;

import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.snmp4j.transport.DefaultTcpTransportMapping;

import java.net.InetAddress;

public class SnmpScanner {
    private static final String COMMUNITY = "public";
    private static final String OID_DEVICE_TYPE = "1.3.6.1.2.1.25.3.2.1.2";

    public static void main(String[] args) throws Exception {

        // Инициализируем библиотеку SNMP
//        Snmp snmp = new Snmp(new DefaultTcpTransportMapping());
        Snmp snmp = new Snmp(new DefaultUdpTransportMapping());


        // Создаем целевой объект
        CommunityTarget target = new CommunityTarget();
        target.setCommunity(new OctetString(COMMUNITY));
        target.setAddress(new UdpAddress(InetAddress.getByName("192.168.1.1"), 161));
        target.setVersion(SnmpConstants.version2c);

        // Создаем запрос
        PDU pdu = new PDU();
        pdu.add(new VariableBinding(new OID(OID_DEVICE_TYPE)));
        pdu.setType(PDU.GET);

        // Выполняем запрос
        ResponseEvent response = snmp.send(pdu, target);

        // Обрабатываем ответ
        if (response.getResponse() != null) {
            for (VariableBinding vb : response.getResponse().getVariableBindings()) {
                System.out.println(vb.getOid() + " = " + vb.getVariable());
            }
        } else {
            System.out.println("No response from device");
            System.out.println("Request failed with response: " + response);
            if (response.getError() != null) {
                System.out.println("Error: " + response.getError().getMessage());
            }
        }


        // Завершаем соединение
        snmp.close();
    }
}
