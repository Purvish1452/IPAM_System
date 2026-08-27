package com.motadata.traceorg.ipam.entity.settings;

import com.motadata.traceorg.ipam.util.TraceOrgCommonConstants;
import org.apache.commons.lang3.StringUtils;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 * @author Krunal Thakkar
 *
 */

@Entity
@Table(name = "user")
public class TraceOrgUser
{

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(length = 50)
    private String userName;

    @Column(length = 150)
    private String password;

    @Transient
    private String activeStatus;

    @NotNull
    @Column(length = 50)
    private String email;

    @ManyToOne
    private TraceOrgUserRole userRoleId;

    @Transient
    private Long roleId;

    private Date currentLoginStatus;

    private Date previousLoginStatus;

    private boolean status = true;

    @Column(length = 100)
    private String description;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUserName() {

        if(userName!=null && !userName.isEmpty()){
            return userName.trim();
        }
        return userName;
    }

    public void setUserName(String userName)
    {

        if(userName!=null && !userName.isEmpty())
        {
            this.userName = userName.trim();
        }
        else
        {
            this.userName = userName;
        }
    }

    public String getPassword() {

        if(password!=null && !StringUtils.isEmpty(password.trim())){
                return password.trim();
        }
        return null;
    }

    public void setPassword(String password)
    {
        if(password!=null && !password.isEmpty())
        {
            this.password = password.trim();
        }
        else{
            this.password = password;
        }
    }

    public String getEmail() {

        if(email!=null && !email.isEmpty()){
            return email.trim();
        }
        return email;
    }

    public void setEmail(String email) {

        if(email!=null && !email.isEmpty())
        {
            this.email = email.trim();
        }
        else
        {
            this.email = email;
        }
    }

    public TraceOrgUserRole getUserRoleId() {
        return userRoleId;
    }

    public void setUserRoleId(TraceOrgUserRole traceOrgUserRoleId) {
        this.userRoleId = traceOrgUserRoleId;
    }

    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public String getDescription() {

        if(description!=null && !description.isEmpty()){
            return description.trim();
        }
        return description;
    }

    public void setDescription(String description)
    {
        if(description!=null && !description.isEmpty())
        {
            this.description = description.trim();
        }
        else
        {
            this.description = description;
        }
    }

    public static long getSerialversionuid() {
        return serialVersionUID;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public String getCurrentLoginStatus()
    {
        if(currentLoginStatus != null)
        {
            return TraceOrgCommonConstants.VISUAL_DATE_FORMAT.format(currentLoginStatus);
        }
        return null;
    }

    public void setCurrentLoginStatus(Date currentLoginStatus) {
        this.currentLoginStatus = currentLoginStatus;
    }

    public String getPreviousLoginStatus()
    {
        if(previousLoginStatus != null)
        {
            return TraceOrgCommonConstants.VISUAL_DATE_FORMAT.format(previousLoginStatus);
        }
        return null;
    }

    public void setPreviousLoginStatus(Date previousLoginStatus) {
        this.previousLoginStatus = previousLoginStatus;
    }

    public String getActiveStatus() {

        if(activeStatus!=null && !activeStatus.isEmpty()){
            return activeStatus.trim();
        }
        return activeStatus;
    }

    public void setActiveStatus(String activeStatus) {

        if(activeStatus!=null && !activeStatus.isEmpty())
        {
            this.activeStatus = activeStatus.trim();
        }
        else
        {
            this.activeStatus =  activeStatus;
        }
    }



    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", userName='" + userName + '\'' +
                ", password='" + password + '\'' +
                ", activeStatus='" + activeStatus + '\'' +
                ", email='" + email + '\'' +
                ", userRoleId=" + userRoleId +
                ", status=" + status +
                '}';
    }
}
