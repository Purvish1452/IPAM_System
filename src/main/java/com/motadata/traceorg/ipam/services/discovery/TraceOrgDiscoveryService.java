package com.motadata.traceorg.ipam.services.discovery;

import java.util.HashMap;

public interface TraceOrgDiscoveryService
{
    HashMap<String, Object> getDiscoveredSubnets();

    HashMap<String, Object> deleteDiscoveredSubnet(Integer id);

    HashMap<String, Object> getDiscoveredSubnet(Integer id);

    HashMap<String, Object> scanGateway(Long id);

    void autoDiscoverLocalSubnet();

    int getSubnetMaskLength(String subnet);

    HashMap<String, Object> statusScanGateway();
}
