package com.motadata.ipam.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;
import java.util.Date;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AlertStream implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long subnetId;
    private String alertType;
    private String message;
    private String subnet;
    private Date timestamp;
    private Boolean status;

    public AlertStream() {
    }

    public AlertStream(Long id, String alertType, String message, Date timestamp) {
        this.id = id;
        this.alertType = alertType;
        this.message = message;
        this.timestamp = timestamp;
    }

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

    public Date getTimestamp() {
        return timestamp;
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
