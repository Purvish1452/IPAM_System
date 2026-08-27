package com.motadata.traceorg.ipam.entity.supernet;

import com.motadata.traceorg.ipam.entity.dashboard.TraceOrgSupernetCategory;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Entity
@Table(name = "supernet_details")
public class TraceOrgSupernetDetails implements Serializable
{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "subnet_id")
    private String subnetId;

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private TraceOrgSupernetCategory traceOrgSupernetCategory;

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

    public TraceOrgSupernetCategory getTraceOrgSupernetCategory() {
        return traceOrgSupernetCategory;
    }

    public void setTraceOrgSupernetCategory(TraceOrgSupernetCategory traceOrgSupernetCategory) {
        this.traceOrgSupernetCategory = traceOrgSupernetCategory;
    }
}
