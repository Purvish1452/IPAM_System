package com.motadata.traceorg.ipam.services.subnet;

import java.util.HashMap;

public interface TraceOrgSubnetService
{
    HashMap<String, Object> getTop10SubnetUtilization();

    HashMap<String, Object> getTop10CategoryUtilization();

    HashMap<String, Object> dnsStatusSummary();

    HashMap<String, Object> recentDiscovered();

}
