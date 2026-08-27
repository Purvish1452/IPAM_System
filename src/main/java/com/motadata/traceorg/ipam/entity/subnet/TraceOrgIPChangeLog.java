package com.motadata.traceorg.ipam.entity.subnet;

import com.motadata.traceorg.ipam.util.TraceOrgCommonConstants;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "ip_change_log")
public class TraceOrgIPChangeLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user", nullable = false, length = 255)
    private String user;

    @Column(name = "ip_address_id", nullable = false)
    private Long ipAddressId;

    @Column(name = "subnet_id", nullable = false)
    private Long subnetId;

    @Column(name = "ip", nullable = false, length = 45)
    private String ip;

    @Column(name = "timestamp", nullable = false)
    private Date timestamp;

    @Column(name = "changelog", nullable = false, length = 255)
    private String changelog;

    public TraceOrgIPChangeLog() {
        // Default constructor
    }
    public TraceOrgIPChangeLog(String user, Long ipAddressId, Long subnetId, String ip, Date timestamp, String changelog) {
        this.user = user;
        this.ipAddressId = ipAddressId;
        this.subnetId = subnetId;
        this.ip = ip;
        this.timestamp = timestamp;
        this.changelog = changelog;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public Long getIpAddressId() {
        return ipAddressId;
    }

    public void setIpAddressId(Long ipaddressId) {
        this.ipAddressId = ipaddressId;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getTimestamp()
    {
        if(timestamp != null)
        {
            return TraceOrgCommonConstants.VISUAL_DATE_FORMAT.format(timestamp);
        }

        return null;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getChangelog() {
        return changelog;
    }

    public void setChangelog(String changelog) {
        this.changelog = changelog;
    }

    public Long getSubnetId() {
        return subnetId;
    }

    public void setSubnetId(Long subnetId) {
        this.subnetId = subnetId;
    }
}
