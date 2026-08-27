package com.motadata.traceorg.ipam.dto.ipRequests;

import com.motadata.traceorg.ipam.converter.TraceOrgListToJsonConverter;

import javax.persistence.Convert;
import java.util.List;

public class TraceOrgApproveIpRequestDTO {

    private Long id;

    private String subnetId;

    @Convert(converter = TraceOrgListToJsonConverter.class)
    private List<String> ips;

    private String remark;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
