package com.motadata.traceorg.ipam.services.settings;

import com.motadata.traceorg.ipam.entity.settings.TraceOrgCustomColumn;

import java.util.HashMap;

/**
 * IPAM-160 IPAM Roadmap : The solution must be flexible to allow the creation of custom fields for objects in IPAM. This must be configurable via the Web GUI.
 */
public interface TraceOrgCustomColumnService
{
    HashMap<String, Object> createCustomColumn(TraceOrgCustomColumn customColumn, String header);

    HashMap<String, Object> listAllCustomColumn();

    HashMap<String, Object> removeCustomColumn(Long id);

    String generateCsv();
}
