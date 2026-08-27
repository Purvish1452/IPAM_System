package com.motadata.traceorg.ipam.entity.dhcp;

import com.motadata.traceorg.ipam.util.TraceOrgCommonConstants;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;

/**
 * @author Krunal Thakkar
 *
 */

@Entity
@Table(name = "dhcp_credential_details",indexes = {@Index(name = "host_address_index", columnList="host_address",unique = false),
                                                    @Index(name = "credential_name_index", columnList="credential_name",unique = true)})
public class TraceOrgDhcpCredentialDetails implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "credential_name",length = 50)
    private String credentialName;

    @NotNull
    @Column(length = 50)
    private String userName;

    @Column(length = 10)
    private String duration;

    @NotNull
    @Column(name = "host_address",length = 50)
    private String hostAddress;

    @NotNull
    @Column(length = 20)
    private String password;

    @Column(length = 20)
    private String type;

    private boolean scheduleStatus = false;

    private Integer scheduleHour = 0;

    private String subnetDuration;

    private Integer subnetScheduleHour = 0;

    private Integer port;

    private Date lastScanTime;

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

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
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

    public String getLastScanTime() {
        if(lastScanTime !=null)
        {
            return TraceOrgCommonConstants.VISUAL_DATE_FORMAT.format(lastScanTime).trim();
        }
        return null;
    }

    public void setLastScanTime(Date lastScanTime) {
        this.lastScanTime = lastScanTime;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public Integer getScheduleHour() {
        return scheduleHour;
    }

    public void setScheduleHour(Integer scheduleHour) {
        this.scheduleHour = scheduleHour;
    }

    public String getSubnetDuration() {

        if(subnetDuration!=null && !subnetDuration.isEmpty()){
            return subnetDuration.trim();
        }
        return subnetDuration;
    }

    public void setSubnetDuration(String subnetDuration)
    {
        if(subnetDuration!=null && !subnetDuration.isEmpty())
        {
            this.subnetDuration = subnetDuration.trim();
        }
        else
        {
            this.subnetDuration = subnetDuration;
        }
    }

    public Integer getSubnetScheduleHour() {
        return subnetScheduleHour;
    }

    public void setSubnetScheduleHour(Integer subnetScheduleHour) {
        this.subnetScheduleHour = subnetScheduleHour;
    }

    public String getDuration() {

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

    public String getHostAddress() {

        if(hostAddress!=null && !hostAddress.isEmpty()){
            return hostAddress.trim();
        }
        return hostAddress;
    }

    public void setHostAddress(String hostAddress)
    {
        if(hostAddress!=null && !hostAddress.isEmpty())
        {
            this.hostAddress = hostAddress.trim();
        }
        else
        {
            this.hostAddress = hostAddress;
        }
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

    public String getCredentialName() {

        if(credentialName!=null){
            return credentialName.trim();
        }
        return credentialName;
    }

    public void setCredentialName(String credentialName)
    {
        if(credentialName!=null && !credentialName.isEmpty())
        {
            this.credentialName = credentialName.trim();
        }
        else
        {
            this.credentialName = credentialName;
        }
    }

    public String getUserName() {

        if(userName!=null && !userName.isEmpty()){
            return userName.trim();
        }
        return userName;
    }

    public void setUserName(String userName)
    {
        if(userName!=null && !userName.isEmpty())
        {
            this.userName = userName.trim();
        }
        else
        {
            this.userName = userName;
        }
    }

    public String getPassword() {

        if(password!=null && !password.isEmpty()){
            return password.trim();
        }
        return password;
    }

    public void setPassword(String password)
    {
        if(password!=null && !password.isEmpty())
        {
            this.password = password.trim();
        }
        else
        {
            this.password = password;
        }
    }

    public String getType() {

        if(type!=null && !type.isEmpty()){
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
}
