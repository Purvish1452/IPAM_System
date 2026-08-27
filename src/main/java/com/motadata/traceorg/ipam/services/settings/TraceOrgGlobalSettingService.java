package com.motadata.traceorg.ipam.services.settings;


import com.motadata.traceorg.ipam.entity.settings.TraceOrgGlobalSetting;

import java.util.HashMap;

public interface TraceOrgGlobalSettingService
{
    HashMap<String, Object> listAllGlobalSetting();

    HashMap<String, Object> updateGlobalSetting(Long id, TraceOrgGlobalSetting traceOrgGlobalSetting);
}
