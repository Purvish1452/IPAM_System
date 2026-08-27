package com.motadata.traceorg.ipam.services.alert;

import java.util.HashMap;

public interface TraceOrgAlertService
{
    HashMap<String, Object> getAlerts(String alertFilter, Integer page, Integer pageSize);

    void inspectAlert(HashMap<String, Object> message);
}
