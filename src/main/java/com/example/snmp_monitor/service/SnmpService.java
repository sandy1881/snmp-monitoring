package com.example.snmp_monitor.service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
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

    private Snmp snmp;

    // Start SNMP transport once when app starts
    @PostConstruct
    public void init() throws IOException {
        TransportMapping<UdpAddress> transport = new DefaultUdpTransportMapping();
        snmp = new Snmp(transport);
        transport.listen();
        log.info("SNMP transport started");
    }

    // Close SNMP transport on shutdown
    @PreDestroy
    public void close() throws IOException {
        if (snmp != null) {
            snmp.close();
        }
    }

    private CommunityTarget<Address> createTarget(String ip, String community) {
        Address targetAddress = GenericAddress.parse("udp:" + ip + "/161");

        CommunityTarget<Address> target = new CommunityTarget<>();
        target.setCommunity(new OctetString(community));
        target.setAddress(targetAddress);
        target.setRetries(0);       // no retries = fast response
        target.setTimeout(800);     // 0.8 second timeout
        target.setVersion(SnmpConstants.version2c);

        return target;
    }

    // ---------- SNMP GET ----------
    public String snmpGet(String ip, String community, String oid) {
        try {
            CommunityTarget<Address> target = createTarget(ip, community);

            PDU pdu = new PDU();
            pdu.add(new VariableBinding(new OID(oid)));
            pdu.setType(PDU.GET);

            ResponseEvent<?> event = snmp.send(pdu, target);

            if (event == null || event.getResponse() == null) {
                return "No Response";
            }

            return event.getResponse().get(0).getVariable().toString();

        } catch (Exception e) {
            log.error("SNMP GET error for OID {}: {}", oid, e.getMessage());
            return "Error";
        }
    }

    // ---------- SNMP WALK (SAFE) ----------
    public List<String> snmpWalk(String ip, String community, String oid) {
        List<String> result = new ArrayList<>();

        try {
            CommunityTarget<Address> target = createTarget(ip, community);

            OID rootOid = new OID(oid);
            OID currentOid = rootOid;

            int maxIterations = 20; // 🔥 prevents long/hanging walks
            int count = 0;

            while (count < maxIterations) {
                PDU pdu = new PDU();
                pdu.add(new VariableBinding(currentOid));
                pdu.setType(PDU.GETNEXT);

                ResponseEvent<?> event = snmp.send(pdu, target);

                if (event == null || event.getResponse() == null) break;

                VariableBinding vb = event.getResponse().get(0);

                if (vb.getOid() == null || !vb.getOid().startsWith(rootOid)) break;

                result.add(vb.getOid() + " = " + vb.getVariable());
                currentOid = vb.getOid();
                count++;
            }

        } catch (Exception e) {
            log.error("SNMP WALK error for OID {}: {}", oid, e.getMessage());
            result.add("Walk Error");
        }

        return result;
    }
}
