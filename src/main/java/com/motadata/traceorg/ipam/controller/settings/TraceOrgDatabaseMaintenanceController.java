package com.motadata.traceorg.ipam.controller.settings;

import com.motadata.traceorg.ipam.entity.Response;
import com.motadata.traceorg.ipam.entity.settings.TraceOrgDatabaseMaintenance;
import com.motadata.traceorg.ipam.services.settings.TraceOrgDatabaseMaintenanceService;
import com.motadata.traceorg.ipam.util.TraceOrgCommonConstants;
import com.motadata.traceorg.ipam.util.TraceOrgCommonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@SuppressWarnings("ALL")
@RestController
public class TraceOrgDatabaseMaintenanceController
{
    @Autowired
    private TraceOrgCommonUtil traceOrgCommonUtil;

    @Autowired
    private TraceOrgDatabaseMaintenanceService traceOrgDatabaseMaintenanceService;

    @RequestMapping(value = TraceOrgCommonConstants.DATABASE_MAINTENANCE + "{id}", method = RequestMethod.GET)
    @PreAuthorize("hasAuthority('PERM_SETTINGS_READ')")
    public ResponseEntity<?> getDatabaseMaintenanceDetail(@PathVariable(TraceOrgCommonConstants.ID) Long id, HttpServletRequest request)
    {
        Response response = new Response();

        traceOrgCommonUtil.buildResponse(traceOrgDatabaseMaintenanceService.getDatabaseMaintenanceDetail(id), response);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @RequestMapping(value = TraceOrgCommonConstants.DATABASE_MAINTENANCE + "{id}", method = RequestMethod.PUT)
    @PreAuthorize("hasAuthority('PERM_SETTINGS_WRITE')")
    public ResponseEntity<?> updateDatabaseMaintenanceDetail(@PathVariable Long id, HttpServletRequest request, @RequestBody TraceOrgDatabaseMaintenance traceOrgDatabaseMaintenance)
    {
        Response response = new Response();

        traceOrgCommonUtil.buildResponse(traceOrgDatabaseMaintenanceService.updateDatabaseMaintenanceDetail(id,traceOrgDatabaseMaintenance), response);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @RequestMapping(value = TraceOrgCommonConstants.DATABASE_BACKUP + "{id}", method = RequestMethod.PUT)
    @PreAuthorize("hasAuthority('PERM_SETTINGS_WRITE')")
    public ResponseEntity<?> updateDatabaseBackupDetail(@PathVariable Long id, HttpServletRequest request, @RequestBody TraceOrgDatabaseMaintenance traceOrgDatabaseBackup)
    {
        Response response = new Response();

        traceOrgCommonUtil.buildResponse(traceOrgDatabaseMaintenanceService.updateDatabaseBackupDetail(id, traceOrgDatabaseBackup), response);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @RequestMapping(value = TraceOrgCommonConstants.RUN_DATABASE_BACKUP + "{id}", method = RequestMethod.PUT)
    @PreAuthorize("hasAuthority('PERM_SETTINGS_WRITE')")
    public ResponseEntity<?> runDatabaseBackup(@PathVariable Long id, HttpServletRequest request, @RequestBody TraceOrgDatabaseMaintenance traceOrgDatabaseBackup)
    {
        Response response = new Response();

        traceOrgCommonUtil.buildResponse(traceOrgDatabaseMaintenanceService.runDatabaseBackup(id), response);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @RequestMapping(value = TraceOrgCommonConstants.DATABASE_MAINTENANCE + "{id}", method = RequestMethod.DELETE)
    @PreAuthorize("hasAuthority('PERM_SETTINGS_WRITE')")
    public ResponseEntity<?> runDatabaseMaintenanceDetail(@PathVariable Long id,HttpServletRequest request)
    {
        Response response = new Response();

        traceOrgCommonUtil.buildResponse(traceOrgDatabaseMaintenanceService.runDatabaseMaintenanceDetail(id), response);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
