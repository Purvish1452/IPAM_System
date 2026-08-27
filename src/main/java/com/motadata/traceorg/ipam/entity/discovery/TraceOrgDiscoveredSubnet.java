package com.motadata.traceorg.ipam.entity.discovery;

import javax.persistence.*;

@Entity
@Table(name = "discovered_subnet")
public class TraceOrgDiscoveredSubnet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "subnet", nullable = false, length = 45)
    private String subnet;

    @Column(name = "subnet_mask", nullable = false, length = 100)
    private String subnetMask;

    @Column(name = "gateway", nullable = false, length = 45)
    private String gateway;

    private Long gatewayId;

    public String getSubnet() {
        return subnet;
    }

    public String getSubnetMask() {
        return subnetMask;
    }

    public void setSubnetMask(String subnetMask) {
        this.subnetMask = subnetMask;
    }

    public String getGateway() {
        return gateway;
    }

    public void setGateway(String gateway) {
        this.gateway = gateway;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Long getGatewayId() {
        return gatewayId;
    }

    public void setGatewayId(Long gatewayId) {
        this.gatewayId = gatewayId;
    }

    public void setSubnet(String subnet) {
        this.subnet = subnet;
    }
}