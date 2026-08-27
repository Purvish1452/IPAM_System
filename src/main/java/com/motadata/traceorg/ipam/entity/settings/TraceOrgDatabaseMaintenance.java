package com.motadata.traceorg.ipam.entity.settings;

import javax.persistence.*;
import java.io.Serializable;

/**
 * @author Krunal Thakkar
 *
 */

@Entity
@Table(name = "database_maintainence")
public class TraceOrgDatabaseMaintenance implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    private Long id;

    private Integer maintainedDays;

    private String status;

    private String backupPath;

    private String duration;

    private boolean scheduleStatus = false;

    private Integer scheduleHour = 0;

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getMaintainedDays() {
        return maintainedDays;
    }

    public void setMaintainedDays(Integer maintainedDays) {
        this.maintainedDays = maintainedDays;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getBackupPath() {
        return backupPath;
    }

    public void setBackupPath(String backupPath) {
        this.backupPath = backupPath;
    }

    public boolean getScheduleStatus() {
        return scheduleStatus;
    }

    public void setScheduleStatus(boolean scheduleStatus) {
        this.scheduleStatus = scheduleStatus;
    }

    public Integer getScheduleHour() {
        return scheduleHour;
    }

    public void setScheduleHour(Integer scheduleHour) {
        this.scheduleHour = scheduleHour;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }
}
