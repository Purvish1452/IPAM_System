package com.motadata.traceorg.ipam.services.discovery;

import com.motadata.traceorg.ipam.entity.discovery.TraceOrgGateway;

import java.util.HashMap;

public interface TraceOrgGatewayService
{
    HashMap<String, Object> listGateway();

    HashMap<String, Object> getGateways();

    HashMap<String, Object> getGateway(Long gatewayId);

    HashMap<String, Object> addGateway(TraceOrgGateway traceOrgGateway);

    HashMap<String, Object> updateGateway(Long gatewayId, TraceOrgGateway traceOrgGateway);

    HashMap<String, Object> removeGateway(Long gatewayId);
}
