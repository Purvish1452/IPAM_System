package com.motadata.ipam.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Event implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id = 1L;
    private String category = "Add Subnet";
    private String message = "Subnet activity logged";
    private Long timestamp = System.currentTimeMillis();
    private String severity = "1";
    private String user = "admin";

    // Fields expected by Motadata frontend event-log.js Kendo UI Grid & templates
    private String eventContext = "Subnet Management";
    private String eventType = "Information";
    private String eventBy = "admin";
    private Long eventTime = System.currentTimeMillis();
    private Long generatedTime = System.currentTimeMillis();
    private String eventLog = "Subnet activity logged";
    private String ipAddress = "192.168.10.0";
    private Map<String, Object> doneBy;

    public Event() {
        this.doneBy = new HashMap<>();
        this.doneBy.put("userName", "admin");
        this.generatedTime = System.currentTimeMillis();
        this.eventTime = this.generatedTime;
    }

    public Event(Long id, String category, String message, Long timestamp, String severity) {
        this.id = id;
        this.category = (category != null) ? category : "Add Subnet";
        this.message = (message != null) ? message : "Subnet activity logged";
        this.timestamp = (timestamp != null) ? timestamp : System.currentTimeMillis();
        this.generatedTime = this.timestamp;
        this.eventTime = this.timestamp;
        this.eventLog = this.message;
        this.severity = (severity != null) ? severity : "1";
        this.eventType = (category != null) ? category : "Information";
        this.eventContext = (category != null) ? category : "Subnet Management";
        this.eventBy = "admin";
        this.ipAddress = "192.168.10.0";
        this.doneBy = new HashMap<>();
        this.doneBy.put("userName", "admin");
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCategory() { return category; }
    public void setCategory(String category) { 
        this.category = category; 
        if (this.eventType == null || this.eventType.isEmpty()) {
            this.eventType = category;
        }
        if (this.eventContext == null || this.eventContext.isEmpty()) {
            this.eventContext = category;
        }
    }

    public String getMessage() { return message; }
    public void setMessage(String message) { 
        this.message = message;
        this.eventLog = message;
    }

    public Long getTimestamp() { return timestamp; }
    public void setTimestamp(Long timestamp) { 
        this.timestamp = timestamp; 
        this.generatedTime = timestamp;
        this.eventTime = timestamp;
    }

    public Long getGeneratedTime() { return generatedTime; }
    public void setGeneratedTime(Long generatedTime) { this.generatedTime = generatedTime; }

    public Long getEventTime() { return eventTime; }
    public void setEventTime(Long eventTime) { this.eventTime = eventTime; }

    public String getEventLog() { return eventLog; }
    public void setEventLog(String eventLog) { this.eventLog = eventLog; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }

    public String getUser() { return user; }
    public void setUser(String user) { 
        this.user = user; 
        if (this.doneBy == null) this.doneBy = new HashMap<>();
        this.doneBy.put("userName", (user != null && !user.isEmpty()) ? user : "admin");
    }

    public String getEventContext() { return eventContext; }
    public void setEventContext(String eventContext) { this.eventContext = eventContext; }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public String getEventBy() { return eventBy; }
    public void setEventBy(String eventBy) { this.eventBy = eventBy; }

    public Map<String, Object> getDoneBy() { return doneBy; }
    public void setDoneBy(Map<String, Object> doneBy) { this.doneBy = doneBy; }
}
