package com.motadata.ipam.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

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

    // Default constructor initializing baseline event timestamps and admin context.
    public Event() {
        this.doneBy = new HashMap<>();
        this.doneBy.put("userName", "admin");
        this.generatedTime = System.currentTimeMillis();
        this.eventTime = this.generatedTime;
    }

    // Parameterized constructor initializing event attributes and metadata.
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

    // Returns the event record ID.
    public Long getId() { return id; }

    // Sets the event record ID.
    public void setId(Long id) { this.id = id; }

    // Returns the event category name.
    public String getCategory() { return category; }

    // Sets the event category name.
    public void setCategory(String category) { 
        this.category = category; 
        if (this.eventType == null || this.eventType.isEmpty()) {
            this.eventType = category;
        }
        if (this.eventContext == null || this.eventContext.isEmpty()) {
            this.eventContext = category;
        }
    }

    // Returns the event message description.
    public String getMessage() { return message; }

    // Sets the event message description.
    public void setMessage(String message) { 
        this.message = message;
        this.eventLog = message;
    }

    // Returns the timestamp when the event occurred.
    public Long getTimestamp() { return timestamp; }

    // Sets the timestamp when the event occurred.
    public void setTimestamp(Long timestamp) { 
        this.timestamp = timestamp; 
        this.generatedTime = timestamp;
        this.eventTime = timestamp;
    }

    // Returns the generated timestamp of the event log.
    public Long getGeneratedTime() { return generatedTime; }

    // Sets the generated timestamp of the event log.
    public void setGeneratedTime(Long generatedTime) { this.generatedTime = generatedTime; }

    // Returns the recorded time of the event.
    public Long getEventTime() { return eventTime; }

    // Sets the recorded time of the event.
    public void setEventTime(Long eventTime) { this.eventTime = eventTime; }

    // Returns the formatted event log text.
    public String getEventLog() { return eventLog; }

    // Sets the formatted event log text.
    public void setEventLog(String eventLog) { this.eventLog = eventLog; }

    // Returns the associated IP address.
    public String getIpAddress() { return ipAddress; }

    // Sets the associated IP address.
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    // Returns the event severity level string.
    public String getSeverity() { return severity; }

    // Sets the event severity level string.
    public void setSeverity(String severity) { this.severity = severity; }

    // Returns the username associated with the event.
    public String getUser() { return user; }

    // Sets the username associated with the event.
    public void setUser(String user) { 
        this.user = user; 
        if (this.doneBy == null) this.doneBy = new HashMap<>();
        this.doneBy.put("userName", (user != null && !user.isEmpty()) ? user : "admin");
    }

    // Returns the functional context of the event.
    public String getEventContext() { return eventContext; }

    // Sets the functional context of the event.
    public void setEventContext(String eventContext) { this.eventContext = eventContext; }

    // Returns the event type classification.
    public String getEventType() { return eventType; }

    // Sets the event type classification.
    public void setEventType(String eventType) { this.eventType = eventType; }

    // Returns the actor name who triggered the event.
    public String getEventBy() { return eventBy; }

    // Sets the actor name who triggered the event.
    public void setEventBy(String eventBy) { this.eventBy = eventBy; }

    // Returns the actor details map for UI grid presentation.
    public Map<String, Object> getDoneBy() { return doneBy; }

    // Sets the actor details map for UI grid presentation.
    public void setDoneBy(Map<String, Object> doneBy) { this.doneBy = doneBy; }
}
