package com.motadata.traceorg.ipam.services.settings;

import com.motadata.traceorg.ipam.entity.settings.TraceOrgDatabaseMaintenance;

import java.util.HashMap;

public interface TraceOrgDatabaseMaintenanceService
{
    HashMap<String, Object> getDatabaseMaintenanceDetail(Long id);

    HashMap<String, Object> updateDatabaseMaintenanceDetail(Long id, TraceOrgDatabaseMaintenance traceOrgDatabaseMaintenance);

    HashMap<String, Object> updateDatabaseBackupDetail(Long id, TraceOrgDatabaseMaintenance traceOrgDatabaseBackup);

    HashMap<String, Object> runDatabaseBackup(Long id);

    HashMap<String, Object> runDatabaseMaintenanceDetail(Long id);
}
