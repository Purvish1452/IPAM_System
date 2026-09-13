package com.motadata.ipam.model;

import java.io.Serializable;
import java.util.Date;

public class SubnetIpDetails implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long subnetId;
    private String ipAddress;
    private String macAddress;
    private String status;
    private String dnsName;
    private String systemName;
    private String vendor;
    private String deviceType;
    private Date lastSeen;
    private boolean reserved = false;
    private String description;

    // Default constructor for SubnetIpDetails
    public SubnetIpDetails() {
    }

    // Parameterized constructor initializing ID, subnet ID, IP address, and status
    public SubnetIpDetails(Long id, Long subnetId, String ipAddress, String status) {
        this.id = id;
        this.subnetId = subnetId;
        this.ipAddress = ipAddress;
        this.status = status;
    }

    // Gets the IP details record ID
    public Long getId() {
        return id;
    }

    // Sets the IP details record ID
    public void setId(Long id) {
        this.id = id;
    }

    // Gets the parent subnet ID
    public Long getSubnetId() {
        return subnetId;
    }

    // Sets the parent subnet ID
    public void setSubnetId(Long subnetId) {
        this.subnetId = subnetId;
    }

    // Gets the IP address
    public String getIpAddress() {
        return ipAddress;
    }

    // Sets the IP address
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    // Gets the MAC address
    public String getMacAddress() {
        return macAddress;
    }

    // Sets the MAC address
    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    // Gets the IP status (e.g., Used, Transient, Available)
    public String getStatus() {
        return status;
    }

    // Sets the IP status
    public void setStatus(String status) {
        this.status = status;
    }

    // Gets the DNS hostname
    public String getDnsName() {
        return dnsName;
    }

    // Sets the DNS hostname
    public void setDnsName(String dnsName) {
        this.dnsName = dnsName;
    }

    // Gets the system name
    public String getSystemName() {
        return systemName;
    }

    // Sets the system name
    public void setSystemName(String systemName) {
        this.systemName = systemName;
    }

    // Gets the hardware vendor name
    public String getVendor() {
        return vendor;
    }

    // Sets the hardware vendor name
    public void setVendor(String vendor) {
        this.vendor = vendor;
    }

    // Gets the detected device type
    public String getDeviceType() {
        return deviceType;
    }

    // Sets the detected device type
    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    // Gets the timestamp when the IP was last seen active
    public Date getLastSeen() {
        return lastSeen;
    }

    // Sets the timestamp when the IP was last seen active
    public void setLastSeen(Date lastSeen) {
        this.lastSeen = lastSeen;
    }

    // Checks whether the IP address is reserved
    public boolean isReserved() {
        return reserved;
    }

    // Sets the reserved status of the IP address
    public void setReserved(boolean reserved) {
        this.reserved = reserved;
    }

    // Gets the IP description / note
    public String getDescription() {
        return description;
    }

    // Sets the IP description / note
    public void setDescription(String description) {
        this.description = description;
    }
}
