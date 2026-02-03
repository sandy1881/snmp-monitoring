package com.example.snmp_monitor.controller;

import com.example.snmp_monitor.service.SnmpService;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/snmp")
public class SnmpController {

    private final SnmpService snmpService;

    public SnmpController(SnmpService snmpService) {
        this.snmpService = snmpService;
    }

    @GetMapping("/collect")
    public Map<String, Object> collectSnmp(
            @RequestParam String ip,
            @RequestParam(defaultValue = "public") String community
    ) throws IOException {

        Map<String, Object> result = new HashMap<>();

        result.put("sysObjectID", snmpService.snmpGet(ip, community, "1.3.6.1.2.1.1.2.0"));
        result.put("hostname", snmpService.snmpGet(ip, community, "1.3.6.1.2.1.1.5.0"));
        result.put("interfaceCount", snmpService.snmpGet(ip, community, "1.3.6.1.2.1.2.1.0"));

        List<String> ifTypes = snmpService.snmpWalk(ip, community, "1.3.6.1.2.1.2.2.1.3");
        result.put("interfaceTypes", ifTypes);

        List<String> equipment = snmpService.snmpWalk(ip, community, "1.3.6.1.2.1.47.1.1.1.1.2");
        result.put("equipmentDetails", equipment);

        return result;
    }
}
