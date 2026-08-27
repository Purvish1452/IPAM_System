package com.motadata.traceorg.ipam.entity.settings;

import javax.persistence.*;
import java.io.Serializable;

/**
 * @author Krunal Thakkar
 *
 */

@Entity
@Table(name = "mail_server")
public class TraceOrgMailServer implements Serializable
{
    private static final long serialVersionUID = 1L;

    @Id
    private Long id;

    @Column(length = 20)
    private String mailHost;

    private Integer mailPort;

    @Column(length = 100)
    private String mailFromEmail;

    @Column(length = 100)
    private String mailToEmail;

    @Column(length = 50)
    private String mailUserName;

    @Column(length = 320)
    private String mailUserId;

    @Column(length = 20)
    private String mailPassword;

    @Column(length = 20)
    private String mailProtocol;

    private int mailTimeout;

    @Column(length = 20)
    private String mailType;

    public String getMailType() {
        if(mailType!=null && !mailType.isEmpty()){
            return mailType.trim();
        }
        return mailType;
    }

    public void setMailType(String mailType)
    {
        if(mailType!=null && !mailType.isEmpty())
        {
            this.mailType = mailType.trim();
        }
        else
        {
            this.mailType = mailType;
        }
    }

    public int getMailTimeout() {
        return mailTimeout;
    }

    public void setMailTimeout(int mailTimeout) {
        this.mailTimeout = mailTimeout;
    }

    public String getMailProtocol() {

        if(mailProtocol!=null && !mailProtocol.isEmpty()){
            return mailProtocol.trim();
        }
        return mailProtocol;
    }

    public void setMailProtocol(String mailProtocol)
    {
        if(mailProtocol!=null && !mailProtocol.isEmpty())
        {
            this.mailProtocol = mailProtocol.trim();
        }
        else
        {
            this.mailProtocol = mailProtocol;
        }
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

    public String getMailHost() {

        if(mailHost!=null){
            return mailHost.trim();
        }
        return mailHost;
    }

    public void setMailHost(String mailHost)
    {
        if(mailHost!=null && !mailHost.isEmpty())
        {
            this.mailHost = mailHost.trim();
        }
        else
        {
            this.mailHost = mailHost;
        }
    }

    public Integer getMailPort() {
        return mailPort;
    }

    public void setMailPort(Integer mailPort) {
        this.mailPort = mailPort;
    }

    public String getMailFromEmail() {

        if(mailFromEmail!=null && !mailFromEmail.isEmpty()){
            return mailFromEmail.trim();
        }
        return mailFromEmail;
    }

    public void setMailFromEmail(String mailFromEmail)
    {
        if(mailFromEmail!=null && !mailFromEmail.isEmpty())
        {
            this.mailFromEmail = mailFromEmail.trim();
        }
        else
        {
            this.mailFromEmail = mailFromEmail;
        }
    }

    public String getMailToEmail() {
        return mailToEmail;
    }

    public void setMailToEmail(String mailToEmail) {
        this.mailToEmail = mailToEmail;
    }

    public String getMailUserName() {

        if(mailUserName!=null){
            return mailUserName.trim();
        }
        return mailUserName;
    }

    public void setMailUserName(String mailUserName)
    {
        if(mailUserName!=null && !mailUserName.isEmpty())
        {
            this.mailUserName = mailUserName.trim();
        }
        else
        {
            this.mailUserName = mailUserName;
        }
    }

    public String getMailPassword()
    {
        if(mailPassword!=null && !mailPassword.isEmpty())
        {
            return mailPassword.trim();
        }
        return mailPassword;
    }

    public void setMailPassword(String mailPassword) {

        if(mailPassword!=null && !mailPassword.isEmpty())
        {
            this.mailPassword = mailPassword.trim();
        }
        else
        {
            this.mailPassword = mailPassword;
        }
    }

    public String getMailUserId()
    {
        return mailUserId;
    }

    public void setMailUserId(String mailUserId)
    {
        this.mailUserId = mailUserId;
    }

}
