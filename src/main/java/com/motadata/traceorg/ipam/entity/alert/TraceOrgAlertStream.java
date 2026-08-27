package com.motadata.traceorg.ipam.entity.alert;

import com.motadata.traceorg.ipam.util.TraceOrgCommonConstants;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "alert_stream")
public class TraceOrgAlertStream {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "subnet_id", nullable = false)
    private Long subnetId;

    @Column(name = "alert_type", length = 100, nullable = false)
    private String alertType;

    @Column(name = "message")
    private String message;

    @Column(name = "subnet", length = 45)
    private String subnet;

    @Column(name = "timestamp", nullable = false)
    private Date timestamp;

    @Column(name = "status")
    private Boolean status;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSubnetId() {
        return subnetId;
    }

    public void setSubnetId(Long subnetId) {
        this.subnetId = subnetId;
    }

    public String getAlertType() {
        return alertType;
    }

    public void setAlertType(String alertType) {
        this.alertType = alertType;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSubnet() {
        return subnet;
    }

    public void setSubnet(String subnet) {
        this.subnet = subnet;
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

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }
}
