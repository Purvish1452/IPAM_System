package com.motadata.ipam.model;

import java.io.Serializable;
import java.util.Date;

public class SubnetDetails implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String subnetAddress;
    private String subnetName;
    private String subnetMask;
    private Long categoryId;
    private String description;
    private Date createdDate;
    private String createdBy;
    private Date lastModifiedDate;
    private String lastModifiedBy;
    private String scanStatus;
    private Integer scanInterval;

    // Fields expected by Motadata dashboard widget grids
    private Integer usedIp;
    private Integer availableIp;
    private Double usedIpPercentage;
    private Integer severity;
    private String type;

    // Default constructor initializing default IPAM metrics
    public SubnetDetails() {
        this.usedIp = 45;
        this.availableIp = 209;
        this.usedIpPercentage = 17.7;
        this.severity = 3;
        this.type = "DHCP";
    }

    // Parameterized constructor initializing subnet address and mask with defaults
    public SubnetDetails(Long id, String subnetAddress, String subnetMask) {
        this.id = id;
        this.subnetAddress = subnetAddress;
        this.subnetName = subnetAddress;
        this.subnetMask = subnetMask;
        this.usedIp = 45;
        this.availableIp = 209;
        this.usedIpPercentage = 17.7;
        this.severity = 3;
        this.type = "DHCP";
    }

    // Gets the subnet ID
    public Long getId() { return id; }
    // Sets the subnet ID
    public void setId(Long id) { this.id = id; }

    // Gets the subnet network address
    public String getSubnetAddress() { return subnetAddress; }
    // Sets the subnet network address and synchronizes name if unset
    public void setSubnetAddress(String subnetAddress) { 
        this.subnetAddress = subnetAddress; 
        if (this.subnetName == null) this.subnetName = subnetAddress;
    }

    // Gets the subnet name or falls back to subnet address
    public String getSubnetName() { return subnetName != null ? subnetName : subnetAddress; }
    // Sets the subnet display name
    public void setSubnetName(String subnetName) { this.subnetName = subnetName; }

    // Gets the subnet netmask
    public String getSubnetMask() { return subnetMask; }
    // Sets the subnet netmask
    public void setSubnetMask(String subnetMask) { this.subnetMask = subnetMask; }

    // Gets the category ID
    public Long getCategoryId() { return categoryId; }
    // Sets the category ID
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }

    // Gets the subnet description
    public String getDescription() { return description; }
    // Sets the subnet description
    public void setDescription(String description) { this.description = description; }

    // Gets the subnet creation date
    public Date getCreatedDate() { return createdDate; }
    // Sets the subnet creation date
    public void setCreatedDate(Date createdDate) { this.createdDate = createdDate; }

    // Gets the creator username
    public String getCreatedBy() { return createdBy; }
    // Sets the creator username
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    // Gets the timestamp when the subnet was last modified
    public Date getLastModifiedDate() { return lastModifiedDate; }
    // Sets the timestamp when the subnet was last modified
    public void setLastModifiedDate(Date lastModifiedDate) { this.lastModifiedDate = lastModifiedDate; }

    // Gets the username who last modified the subnet
    public String getLastModifiedBy() { return lastModifiedBy; }
    // Sets the username who last modified the subnet
    public void setLastModifiedBy(String lastModifiedBy) { this.lastModifiedBy = lastModifiedBy; }

    // Gets the current scan status
    public String getScanStatus() { return scanStatus; }
    // Sets the current scan status
    public void setScanStatus(String scanStatus) { this.scanStatus = scanStatus; }

    // Gets the scan interval in minutes
    public Integer getScanInterval() { return scanInterval; }
    // Sets the scan interval in minutes
    public void setScanInterval(Integer scanInterval) { this.scanInterval = scanInterval; }

    // Gets the count of used IPs
    public Integer getUsedIp() { return usedIp; }
    // Sets the count of used IPs
    public void setUsedIp(Integer usedIp) { this.usedIp = usedIp; }

    // Gets the count of available IPs
    public Integer getAvailableIp() { return availableIp; }
    // Sets the count of available IPs
    public void setAvailableIp(Integer availableIp) { this.availableIp = availableIp; }

    // Gets the calculated used IP percentage
    public Double getUsedIpPercentage() { return usedIpPercentage; }
    // Sets the calculated used IP percentage
    public void setUsedIpPercentage(Double usedIpPercentage) { this.usedIpPercentage = usedIpPercentage; }

    // Gets the severity score
    public Integer getSeverity() { return severity; }
    // Sets the severity score
    public void setSeverity(Integer severity) { this.severity = severity; }

    // Gets the subnet type (e.g., DHCP, STATIC)
    public String getType() { return type; }
    // Sets the subnet type
    public void setType(String type) { this.type = type; }
}
