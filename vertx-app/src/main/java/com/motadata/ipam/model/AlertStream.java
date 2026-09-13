package com.motadata.ipam.model;

import java.io.Serializable;
import java.util.Date;


//Serializable is used for object data to network , file convert .
public class AlertStream implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long subnetId;
    private String alertType;
    private String message;
    private String subnet;
    private Date timestamp;
    private Boolean status;

    // Default constructor for serialization.
    public AlertStream() {
    }

    // Parameterized constructor initializing alert fields.
    public AlertStream(Long id, String alertType, String message, Date timestamp) {
        this.id = id;
        this.alertType = alertType;
        this.message = message;
        this.timestamp = timestamp;
    }

    // Returns the alert record ID.
    public Long getId() {
        return id;
    }

    // Sets the alert record ID.
    public void setId(Long id) {
        this.id = id;
    }

    // Returns the associated subnet ID.
    public Long getSubnetId() {
        return subnetId;
    }

    // Sets the associated subnet ID.
    public void setSubnetId(Long subnetId) {
        this.subnetId = subnetId;
    }

    // Returns the alert severity type.
    public String getAlertType() {
        return alertType;
    }

    // Sets the alert severity type.
    public void setAlertType(String alertType) {
        this.alertType = alertType;
    }

    // Returns the alert message description.
    public String getMessage() {
        return message;
    }

    // Sets the alert message description.
    public void setMessage(String message) {
        this.message = message;
    }

    // Returns the subnet address label.
    public String getSubnet() {
        return subnet;
    }

    // Sets the subnet address label.
    public void setSubnet(String subnet) {
        this.subnet = subnet;
    }

    // Returns the timestamp when the alert occurred.
    public Date getTimestamp() {
        return timestamp;
    }

    // Sets the timestamp when the alert occurred.
    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    // Returns the active or resolved status of the alert.
    public Boolean getStatus() {
        return status;
    }

    // Sets the active or resolved status of the alert.
    public void setStatus(Boolean status) {
        this.status = status;
    }
}
