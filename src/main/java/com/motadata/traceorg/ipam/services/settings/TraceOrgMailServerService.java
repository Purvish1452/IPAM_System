package com.motadata.traceorg.ipam.services.settings;

import com.motadata.traceorg.ipam.entity.settings.TraceOrgMailServer;

import java.util.HashMap;

public interface TraceOrgMailServerService
{
    HashMap<String, Object> listAllMailServer();

    HashMap<String, Object> getMailServer(Long id);

    HashMap<String, Object> updateMailServer(Long id, TraceOrgMailServer traceOrgMailServer);

    HashMap<String, Object> insertMailServer(String mailToEmail);

    HashMap<String, Object> testMailServer(TraceOrgMailServer traceOrgMailServer);
}
