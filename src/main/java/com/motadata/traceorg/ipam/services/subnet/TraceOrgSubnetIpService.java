package com.motadata.traceorg.ipam.services.subnet;

import java.util.HashMap;

public interface TraceOrgSubnetIpService
{
    HashMap<String, Object> exportPdfRecentlyDiscovered();

    HashMap<String, Object> exportPdfTop10CategoryUtilization();

    HashMap<String, Object> exportPdfTop10SubnetUtilization();
}
