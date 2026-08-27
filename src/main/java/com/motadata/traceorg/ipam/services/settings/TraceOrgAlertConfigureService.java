package com.motadata.traceorg.ipam.services.settings;

import com.motadata.traceorg.ipam.entity.settings.TraceOrgAlertConfigure;

import java.util.HashMap;
import java.util.List;

public interface TraceOrgAlertConfigureService
{
    HashMap<String, Object> updateAlertConfiguration(List<TraceOrgAlertConfigure> traceOrgAlertConfigures);

    HashMap<String, Object> getAlertConfiguration();

    String getAlertValue(String alertKey);
}
