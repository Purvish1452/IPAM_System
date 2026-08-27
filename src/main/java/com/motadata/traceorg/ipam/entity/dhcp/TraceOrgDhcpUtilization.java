package com.motadata.traceorg.ipam.entity.dhcp;

import javax.persistence.*;
import java.io.Serializable;

@SuppressWarnings("ALL")
@Entity
@Table(name = "dhcp_utilization")
public class TraceOrgDhcpUtilization implements Serializable
{

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private TraceOrgDhcpCredentialDetails dhcpCredentialDetailId;

    @Column(length = 20)
    private String addressScopes;

    @Column(length = 20)
    private String declines;

    @Column(length = 20)
    private String offers;

    @Column(length = 20)
    private String requests;

    @Column(length = 20)
    private String discovers;

    @Column(length = 20)
    private String releases;

    @Column(length = 20)
    private String acks;

    @Column(length = 20)
    private String naks;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TraceOrgDhcpCredentialDetails getDhcpCredentialDetailId() {
        return dhcpCredentialDetailId;
    }

    public void setDhcpCredentialDetailId(TraceOrgDhcpCredentialDetails dhcpCredentialDetailId) {
        this.dhcpCredentialDetailId = dhcpCredentialDetailId;
    }

    public String getAddressScopes() {
        return addressScopes;
    }

    public void setAddressScopes(String addressScopes) {
        this.addressScopes = addressScopes;
    }

    public String getDeclines() {
        return declines;
    }

    public void setDeclines(String declines) {
        this.declines = declines;
    }

    public String getOffers() {
        return offers;
    }

    public void setOffers(String offers) {
        this.offers = offers;
    }

    public String getRequests() {
        return requests;
    }

    public void setRequests(String requests) {
        this.requests = requests;
    }

    public String getDiscovers() {
        return discovers;
    }

    public void setDiscovers(String discovers) {
        this.discovers = discovers;
    }

    public String getReleases() {
        return releases;
    }

    public void setReleases(String releases) {
        this.releases = releases;
    }

    public String getAcks() {
        return acks;
    }

    public void setAcks(String acks) {
        this.acks = acks;
    }

    public String getNaks() {
        return naks;
    }

    public void setNaks(String naks) {
        this.naks = naks;
    }

    public static long getSerialVersionUID() {

        return serialVersionUID;
    }
}
