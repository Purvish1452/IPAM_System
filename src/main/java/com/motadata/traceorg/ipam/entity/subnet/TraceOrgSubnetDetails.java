package com.motadata.traceorg.ipam.entity.subnet;

import com.motadata.traceorg.ipam.entity.dashboard.TraceOrgCategory;
import com.motadata.traceorg.ipam.entity.dhcp.TraceOrgDhcpCredentialDetails;
import com.motadata.traceorg.ipam.util.TraceOrgCommonConstants;
import org.apache.commons.lang3.StringUtils;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.Date;

/**
 * @author Krunal Thakkar
 *
 */

@Entity
@Table(name = "subnet_details")
public class TraceOrgSubnetDetails implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private boolean isIpv6 = false;

    @Column(length = 50)
    private String subnetName;

    @Transient
    @Column(length = 50)
    private String maskInfo;

    @NotNull
    @Column(length = 40)
    private String subnetAddress;

    private Integer subnetCidr;

    @Column(length = 20)
    private String subnetMask;

    @Column(length = 100)
    private String description;

    @Column(length = 50)
    private String location;

    @Column(name = "is_local_subnet", columnDefinition = "boolean default true")
    private boolean isLocalSubnet;

    @Column(name = "snmp_community", length = 50)
    private String snmpCommunity;

    @Column(name = "gateway_ip", length = 50)
    private String gatewayIp;

    private Long gatewayId = TraceOrgCommonConstants.NONE_GATEWAY_ID;

    private boolean scheduleStatus = false;

    private Integer scheduleHour = 0;

    private Long totalIp = 0L;

    private Long usedIp = 0L;

    @Transient
    private float usedIpPercentage;

    private Long availableIp = 0L;

    private Long transientIp = 0L;

    private Date lastScanTime;

    @Column(length = 50)
    private String vlanName;

    @Column(length = 50)
    private String dnsAddress;

    private boolean allowIcmp;

    private boolean allowDns;

    @Transient
    private Long categoryId;

    @ManyToOne
    private TraceOrgCategory traceOrgCategory;

    @Transient
    private int severity;

    private String duration;

    @Column(length = 50)
    private String type;

    @Transient
    private String networkInterface;

    @ManyToOne
    private TraceOrgDhcpCredentialDetails traceOrgDhcpCredentialDetailsId;

    @Column(length = 50)
    private String createdBy;

    private Date createdDate;

    private Date modifiedDate;


    public boolean isScheduleStatus() {
        return scheduleStatus;
    }

    public void setScheduleStatus(boolean scheduleStatus) {
        this.scheduleStatus = scheduleStatus;
    }

    public Date getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(Date modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
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

    public TraceOrgDhcpCredentialDetails getTraceOrgDhcpCredentialDetailsId() {
        return traceOrgDhcpCredentialDetailsId;
    }

    public void setTraceOrgDhcpCredentialDetailsId(TraceOrgDhcpCredentialDetails traceOrgDhcpCredentialDetailsId) {
        this.traceOrgDhcpCredentialDetailsId = traceOrgDhcpCredentialDetailsId;
    }

    public String getNetworkInterface() {
        return networkInterface;
    }

    public void setNetworkInterface(String networkInterface) {
        this.networkInterface = networkInterface;
    }

    public String getType() {

        if(type!=null && !StringUtils.isEmpty(type))
        {
            return type.trim();
        }
        return type;
    }

    public void setType(String type)
    {
        if(type!=null && !type.isEmpty())
        {
            this.type = type.trim();
        }
        else
        {
            this.type = type;
        }
    }

    public String getDuration()
    {
        if(duration!=null && !duration.isEmpty())
        {
            return duration.trim();
        }
        return duration;
    }

    public void setDuration(String duration)
    {
        if(duration!=null && !duration.isEmpty())
        {
            this.duration = duration.trim();
        }
        else
        {
            this.duration = duration;
        }
    }

    public int getSeverity() {

        if(totalIp>0)
        {
            if((usedIp *100)/totalIp < 50)
            {
                severity = 3;
            }
            else if((usedIp *100)/totalIp >= 50 && (usedIp *100)/totalIp <80)
            {
                severity = 2;
            }
            else if((usedIp *100)/totalIp >= 80)
            {
                severity = 1;
            }
        }
        else
        {
            severity = 3;
        }
        return severity;
    }

    public void setSeverity(int severity) {
        this.severity = severity;
    }

    public float getUsedIpPercentage()
    {
        if(totalIp > 0)
        {
            DecimalFormat decimalFormat = new DecimalFormat();

            decimalFormat.setMaximumFractionDigits(2);

            return Float.parseFloat(decimalFormat.format((float)(usedIp *100)/totalIp));
        }
        else
        {
            return 0;
        }

    }

    public void setUsedIpPercentage(float usedIpPercentage) {
        this.usedIpPercentage = usedIpPercentage;
    }

    public boolean isAllowIcmp() {
        return allowIcmp;
    }

    public void setAllowIcmp(boolean allowIcmp) {
        this.allowIcmp = allowIcmp;
    }

    public boolean isAllowDns() {
        return allowDns;
    }

    public void setAllowDns(boolean allowDns) {
        this.allowDns = allowDns;
    }

    public String getDnsAddress() {

        if(dnsAddress!=null)
        {
            return dnsAddress.trim();
        }
        return dnsAddress;
    }

    public void setDnsAddress(String dnsAddress)
    {
        if(dnsAddress!=null && !dnsAddress.isEmpty())
        {
            this.dnsAddress = dnsAddress.trim();
        }
        else
        {
            this.dnsAddress = dnsAddress;
        }
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public String getVlanName() {

        if(vlanName!=null && !vlanName.isEmpty())
        {
            return vlanName.trim();
        }
        return vlanName;
    }

    public void setVlanName(String vlanName)
    {
        if(vlanName!=null && !vlanName.isEmpty())
        {
            this.vlanName = vlanName.trim();
        }
        else
        {
            this.vlanName = vlanName;
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSubnetName() {

        if(subnetName!=null){
            return subnetName.trim();
        }
        return subnetName;
    }

    public void setSubnetName(String subnetName)
    {
        if(subnetName!=null && !subnetName.isEmpty()){
            this.subnetName = subnetName.trim();
        }
        else
        {
            this.subnetName = subnetName;
        }
    }

    public String getSubnetAddress()
    {
        if(subnetAddress!=null && !subnetAddress.isEmpty())
        {
            return subnetAddress.trim();
        }
        return subnetAddress;
    }

    public void setSubnetAddress(String subnetAddress)
    {
        if(subnetAddress!=null && !subnetAddress.isEmpty())
        {
            this.subnetAddress = subnetAddress.trim();
        }
        else
        {
            this.subnetAddress = subnetAddress;
        }
    }

    public Integer getSubnetCidr() {
        return subnetCidr;
    }

    public void setSubnetCidr(Integer subnetCidr) {
        this.subnetCidr = subnetCidr;
    }

    public String getSubnetMask() {
        if(subnetMask!=null){
            return subnetMask.trim();
        }
        return subnetMask;
    }

    public void setSubnetMask(String subnetMask)
    {
        if(subnetMask!=null && !subnetMask.isEmpty())
        {
            this.subnetMask = subnetMask.trim();
        }
        else
        {
            this.subnetMask = subnetMask;
        }
    }

    public String getDescription() {
        if(description!=null && !description.isEmpty())
        {
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

    public String getLocation() {

        if(location!=null){
            return location.trim();
        }
        return location;
    }

    public void setLocation(String location)
    {
        if(location!=null && !location.isEmpty())
        {
            this.location = location.trim();
        }
        else
        {
            this.location = location;
        }
    }

    public Integer getScheduleHour() {
        return scheduleHour;
    }

    public void setScheduleHour(Integer scheduleHour) {
        this.scheduleHour = scheduleHour;
    }

    public Long getTotalIp() {
        return totalIp;
    }

    public void setTotalIp(Long totalIp) {
        this.totalIp = totalIp;
    }

    public Long getUsedIp() {
        return usedIp;
    }

    public void setUsedIp(Long usedIp) {
        this.usedIp = usedIp;
    }

    public Long getAvailableIp() {
        return availableIp;
    }

    public void setAvailableIp(Long availableIp) {
        this.availableIp = availableIp;
    }

    public Long getTransientIp() {
        return transientIp;
    }

    public void setTransientIp(Long transientIp) {
        this.transientIp = transientIp;
    }

    public String getLastScanTime()
    {
        if(lastScanTime !=null)
        {
            return TraceOrgCommonConstants.VISUAL_DATE_FORMAT.format(lastScanTime).trim();
        }
        return null;
    }

    public void setLastScanTime(Date lastScanTime) {
        this.lastScanTime = lastScanTime;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public TraceOrgCategory getTraceOrgCategory() {
        return traceOrgCategory;
    }

    public void setTraceOrgCategory(TraceOrgCategory traceOrgCategory) {
        this.traceOrgCategory = traceOrgCategory;
    }

    public String getMaskInfo()
    {
        if(maskInfo!=null && !maskInfo.isEmpty())
        {
            return maskInfo.trim();
        }
        return maskInfo;
    }

    public void setMaskInfo(String maskInfo)
    {
        if(maskInfo!=null && !maskInfo.isEmpty())
        {
            this.maskInfo = maskInfo.trim();
        }
        else
        {
            this.maskInfo = maskInfo;
        }
    }

    public boolean isLocalSubnet() {
        return isLocalSubnet;
    }

    public void setIsLocalSubnet(boolean localSubnet) {
        isLocalSubnet = localSubnet;
    }

    public boolean isIpv6() {
        return isIpv6;
    }

    public void setIpv6(boolean ipv6) {
        isIpv6 = ipv6;
    }

    public Long getGatewayId() {
        return gatewayId;
    }

    public void setGatewayId(Long gatewayId) {
        this.gatewayId = gatewayId;
    }

    public String getSnmpCommunity() {
        return snmpCommunity;
    }

    public void setSnmpCommunity(String snmpCommunity) {
        this.snmpCommunity = snmpCommunity;
    }

    public String getGatewayIp() {
        return gatewayIp;
    }

    public void setGatewayIp(String gatewayIp) {
        this.gatewayIp = gatewayIp;
    }
}
