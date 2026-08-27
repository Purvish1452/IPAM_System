package com.motadata.traceorg.ipam.entity.subnet;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.motadata.traceorg.ipam.converter.TraceOrgJsonNodeConverter;
import com.motadata.traceorg.ipam.util.TraceOrgCommonConstants;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;

/**
 * @author Krunal Thakkar
 * IPAM-145 : System should have rogue device detection capability
 * Change The column rogue to authenticity in subnet ip details.
 */

@Entity
@Table(name = "subnet_ip_details",indexes = {@Index(name = "ip_address_index", columnList="ip_address",unique = false)})
/*
,indexes = {@Index(name = "ip_address_index", columnList="ip_address",unique = false),
@Index(name = "status_index", columnList="status",unique = false),
@Index(name = "device_type_index", columnList="device_type",unique = false),
@Index(name = "modified_date_index", columnList="modified_date",unique = false),
@Index(name = "conflict_mac_index", columnList="conflict_mac",unique = false),
@Index(name = "rogue_status_index", columnList="rogue_status",unique = false)}*/
public class TraceOrgSubnetIpDetails implements Serializable
{
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne
    private TraceOrgSubnetDetails subnetId;

    @Column(name = "ip_address",length = 40)
    private String ipAddress;

    @Column(length = 20)
    private String macAddress;

    @Column(length = 20)
    private String previousMacAddress;

    @Column(length = 50)
    private String dnsStatus;

    /*@Column(length = 50)
    private String ipAddressState;*/

    @Column(name = "status",length = 20)
    private String status;

    @Column(length = 20)
    private String previousStatus;

    private Date leaseExpireDate;

    @Column(length = 50)
    private String hostName;

    @Column(length = 100)
    private String description;

    @Column(name = "device_type",length = 100)
    private String deviceType;

    @Column(name = "authenticity")
    private String authenticity;

    @Column(name = "last_alive_time")
    private Date lastAliveTime;

    @Column(length = 50)
    private String ipToDns;

    @Column(length = 50)
    private String dnsToIp;

    private boolean deactiveStatus = false;

    @Column(name = "conflict_mac",length = 50)
    private String conflictMac;

    private Date createdDate;

    @Column(name = "modified_date")
    private Date modifiedDate;

    @Transient
    private String subnetName;

    @Column(name = "custom_columns", columnDefinition = "JSON")
    @Convert(converter = TraceOrgJsonNodeConverter.class) // Convert JSON automatically
    private JsonNode customColumns;

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 31). // two randomly chosen prime numbers
                        append(ipAddress).
                        append(id).
                        toHashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof TraceOrgSubnetIpDetails))
            return false;
        if (obj == this)
            return true;

        TraceOrgSubnetIpDetails traceOrgSubnetIpDetails = (TraceOrgSubnetIpDetails) obj;
        return new EqualsBuilder().
                        append(ipAddress, traceOrgSubnetIpDetails.ipAddress).
                        append(id, traceOrgSubnetIpDetails.id).
                        isEquals();
    }


    public String getSubnetName() {
        return subnetName;
    }

    public void setSubnetName(String subnetName) {
        this.subnetName = subnetName;
    }

    public String getModifiedDate()
    {
        if(modifiedDate !=null)
        {
            return TraceOrgCommonConstants.VISUAL_DATE_FORMAT.format(modifiedDate).trim();
        }
        return null;
    }

    public void setModifiedDate(Date modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    public String getCreatedDate()
    {
        if(createdDate !=null)
        {
            return TraceOrgCommonConstants.VISUAL_DATE_FORMAT.format(createdDate).trim();
        }
        return null;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public String getConflictMac() {
        return conflictMac;
    }

    public void setConflictMac(String conflictMac) {
        this.conflictMac = conflictMac;
    }

    public String getPreviousMacAddress() {
        return previousMacAddress;
    }

    public void setPreviousMacAddress(String previousMacAddress) {
        this.previousMacAddress = previousMacAddress;
    }

    public String getPreviousStatus()
    {
        if(previousStatus!=null && !previousStatus.isEmpty())
        {
            return previousStatus.trim();
        }
        return previousStatus;
    }

    public void setPreviousStatus(String previousStatus) {
        this.previousStatus = previousStatus;
    }

    public boolean isDeactiveStatus() {
        return deactiveStatus;
    }

    public void setDeactiveStatus(boolean deactiveStatus) {
        this.deactiveStatus = deactiveStatus;
    }

    public String getIpToDns()
    {
        if(ipToDns!=null && !ipToDns.isEmpty())
        {
            return ipToDns.trim();
        }
        return ipToDns;
    }

    public void setIpToDns(String ipToDns)
    {
        if(ipToDns!=null && !ipToDns.isEmpty())
        {
            this.ipToDns = ipToDns.trim();
        }
        else
        {
            this.ipToDns = ipToDns;
        }
    }

    public String getDnsToIp() {

        if(dnsToIp!=null){
            return dnsToIp.trim();
        }
        return dnsToIp;
    }

    public void setDnsToIp(String dnsToIp)
    {
        if(dnsToIp!=null && !dnsToIp.isEmpty())
        {
            this.dnsToIp = dnsToIp.trim();
        }
        else
        {
            this.dnsToIp = dnsToIp;
        }
    }

    public String getLastAliveTime()
    {
        if(lastAliveTime != null)
        {
            return TraceOrgCommonConstants.VISUAL_DATE_FORMAT.format(lastAliveTime);
        }
        return null;
    }

    public void setLastAliveTime(Date lastAliveTime) {
        this.lastAliveTime = lastAliveTime;
    }

    public String getAuthenticity() {
        return authenticity;
    }

    public void setAuthenticity(String authenticity) {
        this.authenticity = authenticity;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TraceOrgSubnetDetails getSubnetId() {
        return subnetId;
    }

    public void setSubnetId(TraceOrgSubnetDetails subnetId) {
        this.subnetId = subnetId;
    }

    public String getIpAddress() {

        if(ipAddress!=null && !ipAddress.isEmpty()){
            return ipAddress.trim();
        }
        return ipAddress;
    }

    public void setIpAddress(String ipAddress)
    {
        if(ipAddress!=null && !ipAddress.isEmpty())
        {
            this.ipAddress = ipAddress.trim();
        }
        else
        {
            this.ipAddress = ipAddress;
        }
    }

    public String getMacAddress()
    {
        if(macAddress!=null && !macAddress.isEmpty()){
            return macAddress.trim();
        }
        return macAddress;
    }

    public void setMacAddress(String macAddress)
    {
        if(macAddress!=null && !macAddress.isEmpty())
        {
            this.macAddress = macAddress.trim();
        }
        else
        {
            this.macAddress = macAddress;
        }
    }

    public String getDnsStatus() {

        if(dnsStatus!=null){
            return dnsStatus.trim();
        }
        return dnsStatus;
    }

    public void setDnsStatus(String dnsStatus)
    {
        if(dnsStatus!=null && !dnsStatus.isEmpty())
        {
            this.dnsStatus = dnsStatus.trim();
        }
        else
        {
            this.dnsStatus = dnsStatus;
        }
    }

   /* public String getIpAddressState() {

        if(ipAddressState!=null){
            return ipAddressState.trim();
        }
        return ipAddressState;
    }

    public void setIpAddressState(String ipAddressState)
    {
        if(ipAddressState!=null && !ipAddressState.isEmpty())
        {
            this.ipAddressState = ipAddressState.trim();
        }
        else
        {
            this.ipAddressState = ipAddressState;
        }
    }*/

    public String getStatus() {

        if(status!=null){
            return status.trim();
        }
        return status;
    }

    public void setStatus(String status)
    {
        if(status!=null && !status.isEmpty())
        {
            this.status = status.trim();
        }
        else
        {
            this.status = status;
        }
    }

    public String getLeaseExpireDate()
    {
        if(leaseExpireDate != null)
        {
            return TraceOrgCommonConstants.VISUAL_DATE_FORMAT.format(leaseExpireDate);
        }
        return null;
    }

    public void setLeaseExpireDate(Date leaseExpireDate) {
        this.leaseExpireDate = leaseExpireDate;
    }

    public String getHostName() {

        if(hostName!=null && !hostName.isEmpty()){
            return hostName.trim();
        }
        return hostName;
    }

    public void setHostName(String hostName)
    {
        if(hostName!=null && !hostName.isEmpty())
        {
            this.hostName = hostName.trim();
        }
        else
        {
            this.hostName = hostName;
        }
    }

    public String getDescription() {

        if(description!=null){
            return description.trim();
        }
        return description;
    }

    public void setDescription(String description)
    {
        if(description!=null && !description.isEmpty())
        {
            this.description = description.trim();
        }
        else
        {
            this.description = description;
        }
    }

    public String getDeviceType() {

        if(deviceType!=null && !deviceType.isEmpty())
        {
            return deviceType.trim();
        }
        return deviceType;
    }

    public void setDeviceType(String deviceType)
    {
        if(deviceType!=null && !deviceType.isEmpty())
        {
            this.deviceType = deviceType.trim();
        }
        else
        {
            this.deviceType = deviceType;
        }
    }

    public JsonNode getCustomColumns() {
        if (customColumns.size()==0) {

            return new ObjectMapper().createObjectNode();
        }
        return customColumns;
    }


    public void setCustomColumns(JsonNode customColumns) {
        this.customColumns = customColumns;
    }

}