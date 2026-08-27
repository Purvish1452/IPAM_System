package com.motadata.traceorg.ipam.entity.ipRequests;
import com.motadata.traceorg.ipam.converter.TraceOrgListToJsonConverter;
import com.motadata.traceorg.ipam.entity.TraceOrgAuditable;
import com.motadata.traceorg.ipam.enumeration.TraceOrgIpRequestsStatus;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "ip_requests")
public class TraceOrgIpRequests extends TraceOrgAuditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int numberOfIps;

    @Convert(converter = TraceOrgListToJsonConverter.class)
    private List<String> ips;


    private TraceOrgIpRequestsStatus status;

    private String subnetId;

    private Boolean preferredSubnet;

    private String purpose;

    private String remark;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getNumberOfIps() {
        return numberOfIps;
    }

    public void setNumberOfIps(int numberOfIps) {
        this.numberOfIps = numberOfIps;
    }

    public TraceOrgIpRequestsStatus getStatus() {
        return status;
    }

    public void setStatus(TraceOrgIpRequestsStatus status) {
        this.status = status;
    }

    public String getSubnetId() {
        return subnetId;
    }

    public void setSubnetId(String subnetId) {
        this.subnetId = subnetId;
    }

    public List<String> getIps() {
        return ips;
    }

    public void setIps(List<String> ips) {
        this.ips = ips;
    }

    public Boolean getPreferredSubnet() {
        return preferredSubnet;
    }

    public void setPreferredSubnet(Boolean preferredSubnet) {
        this.preferredSubnet = preferredSubnet;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
