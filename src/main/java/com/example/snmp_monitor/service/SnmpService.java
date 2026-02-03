package com.example.snmp_monitor.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snmp4j.*;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.*;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class SnmpService {

    private static final Logger log = LoggerFactory.getLogger(SnmpService.class);

    private CommunityTarget<Address> createTarget(String ip, String community) {
        String address = ip.contains(":") ? ip : ip + ":161";

        String[] parts = address.split(":");
        String host = parts[0];
        String port = parts[1];

        Address targetAddress = GenericAddress.parse("udp:" + host + "/" + port);

        CommunityTarget<Address> target = new CommunityTarget<>();
        target.setCommunity(new OctetString(community));
        target.setAddress(targetAddress);
        target.setRetries(2);
        target.setTimeout(3000);
        target.setVersion(SnmpConstants.version2c);

        log.info("SNMP Target Created: {}", targetAddress);
        return target;
    }

    public String snmpGet(String ip, String community, String oid) throws IOException {
        try (TransportMapping<?> transport = new DefaultUdpTransportMapping();
             Snmp snmp = new Snmp(transport)) {

            transport.listen();
            CommunityTarget<Address> target = createTarget(ip, community);

            PDU pdu = new PDU();
            pdu.add(new VariableBinding(new OID(oid)));
            pdu.setType(PDU.GET);

            ResponseEvent<?> event = snmp.send(pdu, target);

            if (event == null) return "Timeout: No SNMP response";
            if (event.getResponse() == null) return "Error: Null SNMP response";

            if (event.getResponse().getErrorStatus() != PDU.noError) {
                return "SNMP Error: " + event.getResponse().getErrorStatusText();
            }

            return event.getResponse().get(0).getVariable().toString();
        }
    }

    public List<String> snmpWalk(String ip, String community, String oid) throws IOException {
        List<String> result = new ArrayList<>();

        try (TransportMapping<?> transport = new DefaultUdpTransportMapping();
             Snmp snmp = new Snmp(transport)) {

            transport.listen();
            CommunityTarget<Address> target = createTarget(ip, community);

            OID rootOid = new OID(oid);
            OID currentOid = rootOid;

            while (true) {
                PDU pdu = new PDU();
                pdu.add(new VariableBinding(currentOid));
                pdu.setType(PDU.GETNEXT);

                ResponseEvent<?> event = snmp.send(pdu, target);

                if (event == null || event.getResponse() == null) break;

                VariableBinding vb = event.getResponse().get(0);

                if (vb.getOid() == null || !vb.getOid().startsWith(rootOid)) break;

                result.add(vb.getOid() + " = " + vb.getVariable().toString());
                currentOid = vb.getOid();
            }
        }

        return result;
    }
}
