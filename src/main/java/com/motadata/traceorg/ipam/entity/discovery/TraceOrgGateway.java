package com.motadata.traceorg.ipam.entity.discovery;

import com.motadata.traceorg.ipam.util.TraceOrgCommonConstants;
import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;

@Entity
@Table(name = "gateway")
public class TraceOrgGateway implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "gateway", nullable = false, length = 45)
    private String gateway;

    @Column(name = "authentication_password")
    private String authenticationPassword;

    @Column(name = "authentication_protocol", length = 50)
    private String authenticationProtocol;

    @Column(name = "community")
    private String community;

    @Column(name = "privacy_protocol", length = 50)
    private String privacyProtocol;

    @Column(name = "private_password")
    private String privatePassword;

    @Column(name = "security_level", length = 50)
    private String securityLevel;

    @Column(name = "security_user_name")
    private String securityUserName;

    @Column(name = "version", length = 10)
    private String version;

    @Column(name = "name")
    private String name;

    @Column(name = "previous_scan")
    private Date previousScan;

    @Column(name = "status")
    private String status;

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getGateway() {
        return gateway;
    }

    public void setGateway(String gateway) {
        this.gateway = gateway;
    }

    public String getAuthenticationPassword() {
        return authenticationPassword;
    }

    public void setAuthenticationPassword(String authenticationPassword) {
        this.authenticationPassword = authenticationPassword;
    }

    public String getAuthenticationProtocol() {
        return authenticationProtocol;
    }

    public void setAuthenticationProtocol(String authenticationProtocol) {
        this.authenticationProtocol = authenticationProtocol;
    }

    public String getCommunity() {
        return community;
    }

    public void setCommunity(String community) {
        this.community = community;
    }

    public String getPrivacyProtocol() {
        return privacyProtocol;
    }

    public void setPrivacyProtocol(String privacyProtocol) {
        this.privacyProtocol = privacyProtocol;
    }

    public String getPrivatePassword() {
        return privatePassword;
    }

    public void setPrivatePassword(String privatePassword) {
        this.privatePassword = privatePassword;
    }

    public String getSecurityLevel() {
        return securityLevel;
    }

    public void setSecurityLevel(String securityLevel) {
        this.securityLevel = securityLevel;
    }

    public String getSecurityUserName() {
        return securityUserName;
    }

    public void setSecurityUserName(String securityUserName) {
        this.securityUserName = securityUserName;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public HashMap<String, String> getCredentialContext()
    {
        HashMap<String, String> context = new HashMap<>();

        context.put("gateway", this.getGateway());

        context.put("community", this.getCommunity());

        context.put("security-level", this.getSecurityLevel());

        context.put("privacy-protocol", this.getPrivacyProtocol());

        context.put("auth-protocol", this.getAuthenticationProtocol());

        context.put("private-password", this.getPrivatePassword());

        context.put("auth-password", this.getAuthenticationPassword());

        context.put("user-name", this.getSecurityUserName());

        context.put("version", this.getVersion());

        return context;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPreviousScan()
    {
        if(previousScan != null)
        {
            return TraceOrgCommonConstants.VISUAL_DATE_FORMAT.format(previousScan);
        }

        return null;
    }

    public void setPreviousScan(Date previousScan) {
        this.previousScan = previousScan;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
