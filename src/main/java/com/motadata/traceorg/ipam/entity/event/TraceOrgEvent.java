package com.motadata.traceorg.ipam.entity.event;

import com.motadata.traceorg.ipam.entity.settings.TraceOrgUser;
import com.motadata.traceorg.ipam.util.TraceOrgCommonConstants;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * @author Krunal Thakkar
 *
 */

@Entity
@Table(name = "event",indexes = {@Index(name = "event_type_index", columnList="event_type",unique = false)})
public class TraceOrgEvent implements Serializable
{
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Date timestamp;

    @ManyToOne
    private TraceOrgUser doneBy;

    @Column(name = "event_type",length = 50)
    private String eventType;


    @Column(length = 200)
    private String eventContext;

    private int severity;

    public int getSeverity() {
        return severity;
    }

    public void setSeverity(int severity) {
        this.severity = severity;
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

    public TraceOrgUser getDoneBy() {
        return doneBy;
    }

    public void setDoneBy(TraceOrgUser doneBy) {
        this.doneBy = doneBy;
    }

    public String getEventType() {

        if(eventType!=null){
            return eventType.trim();
        }
        return eventType;
    }

    public void setEventType(String eventType)
    {
        if(eventType!=null && !eventType.isEmpty())
        {
            this.eventType = eventType.trim();
        }
        else
        {
            this.eventType = eventType;
        }
    }

    public String getEventContext() {

        if(eventContext!=null && !eventContext.isEmpty())
        {
            return eventContext.trim();
        }
        return eventContext;
    }

    public void setEventContext(String eventContext)
    {
        if(eventContext!=null && !eventContext.isEmpty())
        {
            this.eventContext = eventContext.trim();
        }
        else
        {
            this.eventContext = eventContext;
        }
    }
}
