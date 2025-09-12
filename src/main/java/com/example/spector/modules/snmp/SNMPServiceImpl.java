package com.example.spector.modules.snmp;

import com.example.spector.domain.enums.EventType;
import com.example.spector.domain.enums.MessageType;
import com.example.spector.modules.event.EventDispatcher;
import com.example.spector.modules.event.EventMessage;
import lombok.RequiredArgsConstructor;
import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.event.ResponseListener;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.Address;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Component
@RequiredArgsConstructor
public class SNMPServiceImpl implements SNMPService {
    private final EventDispatcher eventDispatcher;

    @Override
    public boolean isAvailableBySNMP(String ipAddress) {
        try (Snmp snmp = new Snmp(new DefaultUdpTransportMapping())){
            snmp.listen();

            CommunityTarget<UdpAddress> target = createTarget(ipAddress);

            PDU pdu = new PDU();
            pdu.setType(PDU.GET);

            ResponseEvent<?> responseEvent = snmp.send(pdu, target);
//            System.out.println("Device " + ipAddress + " is not reachable by SNMP.");
            return responseEvent.getResponse() != null && responseEvent.getResponse().getErrorStatus() == PDU.noError;
        } catch (IOException e) {
//            System.out.println("SNMP access error: " + e.getMessage());

            return false;
        }
    }

    @Override
    public VariableBinding performSnmpGet(String deviceIp, PDU pdu, Snmp snmp) {
        CompletableFuture<VariableBinding> futureResult = new CompletableFuture<>();
        VariableBinding result = new VariableBinding();

        try {
            CommunityTarget<UdpAddress> target = createTarget(deviceIp);
            VariableBinding finalResult = result;
            ResponseListener listener = new ResponseListener() {
                @Override
                public <A extends Address> void onResponse(ResponseEvent<A> responseEvent) {
                    ((Snmp) responseEvent.getSource()).cancel(responseEvent.getRequest(), this);
                    PDU response = responseEvent.getResponse();

                    if (response != null && response.getErrorStatus() == PDU.noError) {
                        Optional<? extends VariableBinding> firstBinding = response.getVariableBindings()
                                .stream().findFirst();
                        firstBinding.ifPresent(variableBinding ->
                                finalResult.setVariable(variableBinding.getVariable()));
                    }
                    futureResult.complete(finalResult);
                }
            };

            snmp.send(pdu, target, null, listener);
            result = futureResult.get(15, TimeUnit.SECONDS);

        } catch (IOException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
            eventDispatcher.dispatch(EventMessage.log(EventType.SYSTEM, MessageType.ERROR,
                    "SNMP: ошибка во время опроса оборудования " + deviceIp + ": " + e.getMessage()));
        } catch (TimeoutException e) {
            System.out.println("SNMP: таймаут оборудования " + deviceIp + " превышен");
            eventDispatcher.dispatch(EventMessage.log(EventType.SYSTEM, MessageType.ERROR,
                    "SNMP: таймаут оборудования " + deviceIp + " превышен"));

            return null; // Возвращаем null в случае таймаута
        }

        return result;
    }

    private CommunityTarget<UdpAddress> createTarget(String ipAddress) {
        CommunityTarget<UdpAddress> target = new CommunityTarget<>();
        target.setCommunity(new OctetString("public"));
        target.setAddress(new UdpAddress(ipAddress + "/161"));
        target.setVersion(SnmpConstants.version1);
        target.setRetries(3);
        target.setTimeout(10000);
        return target;
    }
}
