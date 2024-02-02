package com.example.spector.script;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.event.ResponseListener;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.Address;
import org.snmp4j.smi.GenericAddress;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SnmpGetAsyn {
//    private static final Logger log = LogManager.getLogger(SnmpGetAsyn.class);
    public static final int DEFAULT_VERSION = SnmpConstants.version2c;
    public static final String DEFAULT_PROTOCOL = "udp";
    public static final int DEFAULT_PORT = 161;
    public static final long DEFAULT_TIMEOUT = 3 * 1000L;
    public static final int DEFAULT_RETRY = 3;

    /**
     *communityTarget
     * @param community
     * @return CommunityTarget
     */
    public static CommunityTarget createDefault(String ip, String community) {
        Address address = GenericAddress.parse(DEFAULT_PROTOCOL + ":" + ip
                + "/" + DEFAULT_PORT);
        CommunityTarget target = new CommunityTarget();
        target.setCommunity(new OctetString(community));
        target.setAddress(address);
        target.setVersion(DEFAULT_VERSION);
        target.setTimeout(DEFAULT_TIMEOUT); // milliseconds
        target.setRetries(DEFAULT_RETRY);
        return target;
    }

    /**
     * @param ip
     * @param community
     */
    public static void snmpAsynGetList(String ip, String community, List<String> oidList) {
        CommunityTarget target = createDefault(ip, community);
        Snmp snmp = null;
        try {
            DefaultUdpTransportMapping transport = new DefaultUdpTransportMapping();
            snmp = new Snmp(transport);
            snmp.listen();

            PDU pdu = new PDU();
            for (String oid : oidList) {
                pdu.add(new VariableBinding(new OID(oid)));
            }

            final CountDownLatch latch = new CountDownLatch(1);
            ResponseListener listener = new ResponseListener() {
                public void onResponse(ResponseEvent event) {
                    ((Snmp) event.getSource()).cancel(event.getRequest(), this);
                    PDU response = event.getResponse();
                    PDU request = event.getRequest();
                    System.out.println("[request]:" + request);
                    if (response == null) {
                        System.out.println("[ERROR]: response is null");
//                        log.error("");
                    } else if (response.getErrorStatus() != 0) {
                        System.out.println("[ERROR]: response status"
                                + response.getErrorStatus() + " Text:"
                                + response.getErrorStatusText());
//                        log.error("");
                    } else {
                        System.out.println("Received response Success!");
//                        log.info("Данныые успешно получены!");
                        for (int i = 0; i < response.size(); i++) {
                            VariableBinding vb = response.get(i);
                            System.out.println(vb.getOid() + " = "
                                    + vb.getVariable());
                        }
                        System.out.println("SNMP Asyn GetList OID finished. ");
                        latch.countDown();
                        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
//                            log.info("Скрипт завершил работу!");
                        }));
                    }
                }
            };

            pdu.setType(PDU.GET);
            snmp.send(pdu, target, null, listener);
//            log.info("Скрипт начал работу!");
            System.out.println("asyn send pdu wait for response...");

            boolean wait = latch.await(30, TimeUnit.SECONDS);
            System.out.println("latch.await =:" + wait);


            snmp.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("SNMP Asyn GetList Exception:" + e);
        }
    }

    /**
     * @param args
     */
    public static void main(String[] args) {

        String ip = "192.168.1.35";
        String community = "public";

        List<String> oidList = new ArrayList<String>();
        oidList.add(".1.3.6.1.2.1.33.1.2.5.0");
        //oidList.add(".1.3.6.1.2.1.1.1.0");
//        oidList.add(".1.3.6.1.2.1.1.2.0");
        //oidList.add(".1.3.6.1.2.1.1.3.0");
//        oidList.add(".1.3.6.1.2.1.1.4.0");
        //oidList.add(".1.3.6.1.2.1.1.5.0");
//        oidList.add(".1.3.6.1.2.1.1.6.0");
//        oidList.add(".1.3.6.1.2.1.1.7.0");
//        oidList.add(".1.3.6.1.2.1.2.2.1.2.1");
//        oidList.add(".1.3.6.1.2.1.25.2.3.1.5.3");
//        oidList.add(".1.3.6.1.2.1.25.2.3.1.5.4");
//        oidList.add(".1.3.6.1.2.1.25.2.3.1.6.2");
        //oidList.add(".1.3.6.1.2.1.25.2.3.1.6.3");
//        oidList.add(".1.3.6.1.2.1.25.2.3.1.6.4");

        SnmpGetAsyn.snmpAsynGetList(ip, community, oidList);

    }
}