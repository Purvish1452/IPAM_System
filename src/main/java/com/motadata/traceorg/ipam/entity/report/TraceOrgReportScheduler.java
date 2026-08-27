package com.motadata.traceorg.ipam.entity.report;

import javax.persistence.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

@Entity
@Table(name = "report")
public class TraceOrgReportScheduler
{

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 50)
    private String ipFilter;

    private String subnetId;

    @Column(length = 500)
    private String emailTo;

    private Date  schedulerDate;

    @Column(length = 100)
    private String schedulerTime;

    @Column(length = 50)
    private String schedulerName;

    private boolean repeatFlag ;

    @Column(length = 100)
    private String repeatHourTime;

    @Column(length = 100)
    private String repeatDay;

    @Column(length = 100)
    private String repeatDate;

    @Column(length = 100)
    private String repeatMonth;

    @Column(length = 100)
    private String exportType;

    private Integer schedulerTimeLine;

    private Integer reportExportTimeline;


    public String getSubnetId() {
        return subnetId;
    }

    public void setSubnetId(String subnetId) {
        this.subnetId = subnetId;
    }

    public boolean isRepeatFlag() {
        return repeatFlag;
    }

    public void setRepeatFlag(boolean repeatFlag) {
        this.repeatFlag = repeatFlag;
    }

    public Integer getReportExportTimeline() {
        return reportExportTimeline;
    }

    public void setReportExportTimeline(Integer reportExportTimeline) {
        this.reportExportTimeline = reportExportTimeline;
    }

    public Integer getSchedulerTimeLine() {
        return schedulerTimeLine;
    }

    public void setSchedulerTimeLine(Integer schedulerTimeLine) {
        this.schedulerTimeLine = schedulerTimeLine;
    }

    public String getExportType() {
        return exportType;
    }

    public void setExportType(String exportType) {
        this.exportType = exportType;
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

    public String getIpFilter() {
        return ipFilter;
    }

    public void setIpFilter(String ipFilter) {
        this.ipFilter = ipFilter;
    }

    public String getEmailTo() {
        return emailTo;
    }

    public void setEmailTo(String emailTo) {
        this.emailTo = emailTo;
    }

    public String getSchedulerDate()
    {
        if(schedulerDate!=null)
        {
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

            return dateFormat.format(schedulerDate);
        }
        return null;
    }

    public void setSchedulerDate(Date schedulerDate) {
        this.schedulerDate = schedulerDate;
    }

    public String getSchedulerTime() {
        return schedulerTime;
    }

    public void setSchedulerTime(String schedulerTime) {
        this.schedulerTime = schedulerTime;
    }

    public String getSchedulerName() {
        return schedulerName;
    }

    public void setSchedulerName(String schedulerName) {
        this.schedulerName = schedulerName;
    }

    public String getRepeatHourTime() {
        return repeatHourTime;
    }

    public void setRepeatHourTime(String repeatHourTime) {
        this.repeatHourTime = repeatHourTime;
    }

    public String getRepeatDay() {
        return repeatDay;
    }

    public void setRepeatDay(String repeatDay) {
        this.repeatDay = repeatDay;
    }

    public String getRepeatMonth() {
        return repeatMonth;
    }

    public void setRepeatMonth(String repeatMonth) {
        this.repeatMonth = repeatMonth;
    }

    public String getRepeatDate() {
        return repeatDate;
    }

    public void setRepeatDate(String repeatDate) {
        this.repeatDate = repeatDate;
    }
}
