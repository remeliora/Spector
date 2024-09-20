package com.example.spector.snmp;

import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.smi.VariableBinding;

public interface SNMPService {
    boolean isAvailableBySNMP(String ipAddress);
    VariableBinding performSnmpGet(String deviceIp, PDU pdu, Snmp snmp);
}
