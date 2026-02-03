package com.example.snmp_monitor.controller;

import com.example.snmp_monitor.model.SnmpResponse;
import com.example.snmp_monitor.service.SnmpService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/snmp")
public class SnmpController {

    private final SnmpService snmpService;

    public SnmpController(SnmpService snmpService) {
        this.snmpService = snmpService;
    }

    @GetMapping("/collect")
    public SnmpResponse collectSnmp(
            @RequestParam(defaultValue = "snmp-simulator") String ip,
            @RequestParam(defaultValue = "public") String community
    ) {

        SnmpResponse response = new SnmpResponse();

        try {
            response.setSysObjectID(
                    snmpService.snmpGet(ip, community, "1.3.6.1.2.1.1.2.0"));

            response.setHostname(
                    snmpService.snmpGet(ip, community, "1.3.6.1.2.1.1.5.0"));

            response.setInterfaceCount(
                    snmpService.snmpGet(ip, community, "1.3.6.1.2.1.2.1.0"));

            response.setInterfaceTypes(
                    snmpService.snmpWalk(ip, community, "1.3.6.1.2.1.2.2.1.3"));

            response.setEquipments(
                    snmpService.snmpWalk(ip, community, "1.3.6.1.2.1.47.1.1.1.1.2"));

        } catch (Exception e) {
            response.setError("SNMP collection failed: " + e.getMessage());
        }

        return response;
    }
}
