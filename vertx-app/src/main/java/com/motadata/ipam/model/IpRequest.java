package com.motadata.ipam.model;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class IpRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Integer numberOfIps;
    private List<String> ips;
    private String status;
    private String subnetId;
    private String subnetAddress;
    private String deviceType;
    private String duration;
    private String remark;
    private Boolean preferredSubnet;
    private String purpose;
    private String createdBy;
    private Date createdDate;
    private String lastModifiedBy;
    private Date lastModifiedDate;

    // Default constructor for IpRequest
    public IpRequest() {
    }

    // Gets the IP request ID
    public Long getId() {
        return id;
    }

    // Sets the IP request ID
    public void setId(Long id) {
        this.id = id;
    }

    // Gets the requested number of IPs
    public Integer getNumberOfIps() {
        return numberOfIps;
    }

    // Sets the requested number of IPs
    public void setNumberOfIps(Integer numberOfIps) {
        this.numberOfIps = numberOfIps;
    }

    // Gets the allocated or requested list of IP addresses
    public List<String> getIps() {
        return ips;
    }

    // Sets the allocated or requested list of IP addresses
    public void setIps(List<String> ips) {
        this.ips = ips;
    }

    // Gets the request status (e.g., APPROVED, PENDING, REJECTED)
    public String getStatus() {
        return status;
    }

    // Sets the request status
    public void setStatus(String status) {
        this.status = status;
    }

    // Gets the associated subnet ID
    public String getSubnetId() {
        return subnetId;
    }

    // Sets the associated subnet ID
    public void setSubnetId(String subnetId) {
        this.subnetId = subnetId;
    }

    // Gets the associated subnet CIDR address
    public String getSubnetAddress() {
        return subnetAddress;
    }

    // Sets the associated subnet CIDR address
    public void setSubnetAddress(String subnetAddress) {
        this.subnetAddress = subnetAddress;
    }

    // Gets the target device type
    public String getDeviceType() {
        return deviceType;
    }

    // Sets the target device type
    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    // Gets the lease duration
    public String getDuration() {
        return duration;
    }

    // Sets the lease duration
    public void setDuration(String duration) {
        this.duration = duration;
    }

    // Gets the user remarks
    public String getRemark() {
        return remark;
    }

    // Sets the user remarks
    public void setRemark(String remark) {
        this.remark = remark;
    }

    // Checks whether a preferred subnet is indicated
    public Boolean getPreferredSubnet() {
        return preferredSubnet;
    }

    // Sets whether a preferred subnet is indicated
    public void setPreferredSubnet(Boolean preferredSubnet) {
        this.preferredSubnet = preferredSubnet;
    }

    // Gets the allocation purpose
    public String getPurpose() {
        return purpose;
    }

    // Sets the allocation purpose
    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    // Gets the creator username
    public String getCreatedBy() {
        return createdBy;
    }

    // Sets the creator username
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    // Gets the creation timestamp
    public Date getCreatedDate() {
        return createdDate;
    }

    // Sets the creation timestamp
    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    // Gets the username who last modified the request
    public String getLastModifiedBy() {
        return lastModifiedBy;
    }

    // Sets the username who last modified the request
    public void setLastModifiedBy(String lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }

    // Gets the timestamp when the request was last modified
    public Date getLastModifiedDate() {
        return lastModifiedDate;
    }

    // Sets the timestamp when the request was last modified
    public void setLastModifiedDate(Date lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }
}
