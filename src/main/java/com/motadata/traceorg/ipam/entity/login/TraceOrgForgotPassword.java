package com.motadata.traceorg.ipam.entity.login;

import com.motadata.traceorg.ipam.entity.settings.TraceOrgUser;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * @author Krunal Thakkar
 *
 */

@Entity
@Table(name = "forgot_password")
public class TraceOrgForgotPassword implements Serializable
{
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private TraceOrgUser user;

    @Column(length = 20)
    private String uuid;

    private Date timestamp;

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TraceOrgUser getUser() {
        return user;
    }

    public void setUser(TraceOrgUser traceOrgUser) {
        this.user = traceOrgUser;
    }

    public String getUuid() {

        if(uuid!=null){
            return uuid.trim();
        }
        return uuid;
    }

    public void setUuid(String uuid)
    {
        if(uuid!=null && !uuid.isEmpty())
        {
            this.uuid = uuid.trim();
        }
        else
        {
            this.uuid = uuid;
        }
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}
