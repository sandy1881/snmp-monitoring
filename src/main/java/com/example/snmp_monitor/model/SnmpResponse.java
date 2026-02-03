package com.example.snmp_monitor.model;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.List;

@JsonPropertyOrder({
        "sysObjectID",
        "hostname",
        "interfaceCount",
        "interfaceTypes",
        "equipments",
        "error"
})
public class SnmpResponse {

    private String sysObjectID;
    private String hostname;
    private String interfaceCount;
    private List<String> interfaceTypes;
    private List<String> equipments;
    private String error;

    public String getSysObjectID() {
        return sysObjectID;
    }

    public void setSysObjectID(String sysObjectID) {
        this.sysObjectID = sysObjectID;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public String getInterfaceCount() {
        return interfaceCount;
    }

    public void setInterfaceCount(String interfaceCount) {
        this.interfaceCount = interfaceCount;
    }

    public List<String> getInterfaceTypes() {
        return interfaceTypes;
    }

    public void setInterfaceTypes(List<String> interfaceTypes) {
        this.interfaceTypes = interfaceTypes;
    }

    public List<String> getEquipments() {
        return equipments;
    }

    public void setEquipments(List<String> equipments) {
        this.equipments = equipments;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
