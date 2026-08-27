package com.motadata.traceorg.ipam.entity.settings;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "alert")
public class TraceOrgAlertConfigure implements Serializable {

    @Id
    @Column(name = "alert_key", nullable = false)
    private String alertKey;

    @Column(name = "alert_value")
    private String alertValue;

    public String getAlertKey() {
        return alertKey;
    }

    public void setAlertKey(String alertKey) {
        this.alertKey = alertKey;
    }

    public String getAlertValue() {
        return alertValue;
    }

    public void setAlertValue(String alertValue) {
        this.alertValue = alertValue;
    }
}