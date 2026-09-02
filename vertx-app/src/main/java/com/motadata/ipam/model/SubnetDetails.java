package com.motadata.ipam.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;
import java.util.Date;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
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

    public SubnetDetails() {
        this.usedIp = 45;
        this.availableIp = 209;
        this.usedIpPercentage = 17.7;
        this.severity = 3;
        this.type = "DHCP";
    }

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

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getSubnetAddress() { return subnetAddress; }
    public void setSubnetAddress(String subnetAddress) { 
        this.subnetAddress = subnetAddress; 
        if (this.subnetName == null) this.subnetName = subnetAddress;
    }

    public String getSubnetName() { return subnetName != null ? subnetName : subnetAddress; }
    public void setSubnetName(String subnetName) { this.subnetName = subnetName; }

    public String getSubnetMask() { return subnetMask; }
    public void setSubnetMask(String subnetMask) { this.subnetMask = subnetMask; }

    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Date getCreatedDate() { return createdDate; }
    public void setCreatedDate(Date createdDate) { this.createdDate = createdDate; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public Date getLastModifiedDate() { return lastModifiedDate; }
    public void setLastModifiedDate(Date lastModifiedDate) { this.lastModifiedDate = lastModifiedDate; }

    public String getLastModifiedBy() { return lastModifiedBy; }
    public void setLastModifiedBy(String lastModifiedBy) { this.lastModifiedBy = lastModifiedBy; }

    public String getScanStatus() { return scanStatus; }
    public void setScanStatus(String scanStatus) { this.scanStatus = scanStatus; }

    public Integer getScanInterval() { return scanInterval; }
    public void setScanInterval(Integer scanInterval) { this.scanInterval = scanInterval; }

    public Integer getUsedIp() { return usedIp; }
    public void setUsedIp(Integer usedIp) { this.usedIp = usedIp; }

    public Integer getAvailableIp() { return availableIp; }
    public void setAvailableIp(Integer availableIp) { this.availableIp = availableIp; }

    public Double getUsedIpPercentage() { return usedIpPercentage; }
    public void setUsedIpPercentage(Double usedIpPercentage) { this.usedIpPercentage = usedIpPercentage; }

    public Integer getSeverity() { return severity; }
    public void setSeverity(Integer severity) { this.severity = severity; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
}
