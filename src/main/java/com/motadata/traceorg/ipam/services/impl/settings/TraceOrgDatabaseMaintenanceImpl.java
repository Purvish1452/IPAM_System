package com.motadata.traceorg.ipam.services.impl.settings;

import com.google.common.base.Strings;
import com.motadata.traceorg.ipam.controller.settings.TraceOrgGlobalSettingController;
import com.motadata.traceorg.ipam.entity.settings.TraceOrgDatabaseMaintenance;
import com.motadata.traceorg.ipam.logger.TraceOrgLogger;
import com.motadata.traceorg.ipam.repository.settings.TraceOrgDatabaseMaintenanceRepository;
import com.motadata.traceorg.ipam.repository.event.TraceOrgEventRepository;
import com.motadata.traceorg.ipam.repository.subnet.TraceOrgIpChangeLogRepository;
import com.motadata.traceorg.ipam.services.settings.TraceOrgDatabaseMaintenanceService;
import com.motadata.traceorg.ipam.util.TraceOrgCommonConstants;
import com.motadata.traceorg.ipam.util.TraceOrgCommonUtil;
import com.motadata.traceorg.ipam.util.TraceOrgMessageConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

@Service
public class TraceOrgDatabaseMaintenanceImpl implements TraceOrgDatabaseMaintenanceService
{

    private static final TraceOrgLogger _logger = new TraceOrgLogger(TraceOrgGlobalSettingController.class, "Database Maintenance Service");

    @Autowired
    private TraceOrgCommonUtil traceOrgCommonUtil;

    @Autowired
    private TraceOrgEventRepository traceOrgEventRepository;

    @Autowired
    private TraceOrgIpChangeLogRepository traceOrgIpChangeLogRepository;

    @Autowired
    private TraceOrgDatabaseMaintenanceRepository traceOrgDatabaseMaintenanceRepository;

    @Override
    public HashMap<String, Object> getDatabaseMaintenanceDetail(Long id)
    {
        HashMap<String, Object> result = new HashMap<>();

        try
        {
            TraceOrgDatabaseMaintenance traceOrgDatabaseMaintenance = traceOrgDatabaseMaintenanceRepository.findOne(id);

            if (traceOrgDatabaseMaintenance != null)
            {
                if(Strings.isNullOrEmpty(traceOrgDatabaseMaintenance.getStatus()))
                {
                    traceOrgDatabaseMaintenance.setStatus(TraceOrgCommonConstants.ENABLE);
                }

                result.put(TraceOrgCommonConstants.SUCCESS, TraceOrgCommonConstants.TRUE);

                result.put(TraceOrgCommonConstants.DATA, traceOrgDatabaseMaintenance);
            }
            else
            {
                result.put(TraceOrgCommonConstants.SUCCESS, TraceOrgCommonConstants.FALSE);

                result.put(TraceOrgCommonConstants.MESSAGE, TraceOrgMessageConstants.ENTER_VALID_DETAILS);
            }
        }
        catch (Exception exception)
        {
            _logger.error(exception);
        }

        return result;
    }

    @Override
    public HashMap<String, Object> updateDatabaseMaintenanceDetail(Long id, TraceOrgDatabaseMaintenance traceOrgDatabaseMaintenance)
    {
        HashMap<String, Object> result = new HashMap<>();

        try
        {
            if(id != null && traceOrgDatabaseMaintenance.getMaintainedDays() != null && traceOrgDatabaseMaintenance.getMaintainedDays() > 0)
            {
                TraceOrgDatabaseMaintenance traceOrgDatabase = traceOrgDatabaseMaintenanceRepository.findOne(id);

                if(traceOrgDatabase == null)
                {
                    traceOrgDatabase = new TraceOrgDatabaseMaintenance();
                }

                traceOrgDatabase.setId(id);

                traceOrgDatabase.setStatus(traceOrgDatabaseMaintenance.getStatus());

                traceOrgDatabase.setMaintainedDays(traceOrgDatabaseMaintenance.getMaintainedDays());

                traceOrgDatabaseMaintenanceRepository.save(traceOrgDatabase);

                result.put(TraceOrgCommonConstants.SUCCESS, TraceOrgCommonConstants.TRUE);

                result.put(TraceOrgCommonConstants.MESSAGE, TraceOrgMessageConstants.DATABASE_MAINTENANCE_UPDATE_SUCCESS);
            }
            else
            {
                result.put(TraceOrgCommonConstants.SUCCESS, TraceOrgCommonConstants.FALSE);

                result.put(TraceOrgCommonConstants.MESSAGE, TraceOrgMessageConstants.ENTER_VALID_DETAILS);
            }
        }
        catch (Exception exception)
        {
            _logger.error(exception);
        }

        return result;
    }

    @Override
    public HashMap<String, Object> updateDatabaseBackupDetail(Long id, TraceOrgDatabaseMaintenance traceOrgDatabaseBackup)
    {
        HashMap<String, Object> result = new HashMap<>();

        try
        {
            if(id != null)
            {
                File file = new File(traceOrgDatabaseBackup.getBackupPath());

                if(file.exists())
                {
                    TraceOrgDatabaseMaintenance traceOrgDatabase = traceOrgDatabaseMaintenanceRepository.findOne(id);

                    if(traceOrgDatabase == null)
                    {
                        traceOrgDatabase = new TraceOrgDatabaseMaintenance();
                    }

                    traceOrgDatabase.setId(id);

                    traceOrgDatabase.setBackupPath(traceOrgDatabaseBackup.getBackupPath());

                    traceOrgDatabase.setScheduleStatus(traceOrgDatabaseBackup.getScheduleStatus());

                    traceOrgDatabase.setScheduleHour(traceOrgDatabaseBackup.getScheduleHour());

                    traceOrgDatabase.setDuration(traceOrgDatabaseBackup.getDuration());

                    traceOrgDatabaseMaintenanceRepository.save(traceOrgDatabase);

                    if(traceOrgDatabase.getScheduleStatus())
                    {
                        traceOrgCommonUtil.scheduleBackupJob();
                    }
                    else
                    {
                        traceOrgCommonUtil.removeBackupJob();
                    }

                    result.put(TraceOrgCommonConstants.SUCCESS, TraceOrgCommonConstants.TRUE);

                    result.put(TraceOrgCommonConstants.MESSAGE, "Database backup updated successfully");                }
                else
                {
                    result.put(TraceOrgCommonConstants.SUCCESS, TraceOrgCommonConstants.FALSE);

                    result.put(TraceOrgCommonConstants.MESSAGE, "Invalid path for database backup!");
                }
            }
            else
            {
                result.put(TraceOrgCommonConstants.SUCCESS, TraceOrgCommonConstants.FALSE);

                result.put(TraceOrgCommonConstants.MESSAGE, TraceOrgMessageConstants.ENTER_VALID_DETAILS);
            }
        }
        catch (Exception exception)
        {
            _logger.error(exception);
        }

        return result;
    }

    @Override
    public HashMap<String, Object> runDatabaseBackup(Long id)
    {
        HashMap<String, Object> result = new HashMap<>();

        try
        {
            if(id != null)
            {
                TraceOrgDatabaseMaintenance traceOrgDatabase = traceOrgDatabaseMaintenanceRepository.findOne(id);

                if(traceOrgDatabase != null && !Strings.isNullOrEmpty(traceOrgDatabase.getBackupPath()))
                {
                    if(traceOrgCommonUtil.backup(traceOrgDatabase.getBackupPath()))
                    {
                        result.put(TraceOrgCommonConstants.SUCCESS, TraceOrgCommonConstants.TRUE);

                        result.put(TraceOrgCommonConstants.MESSAGE, "The database backup completed successfully.");
                    }
                    else
                    {
                        result.put(TraceOrgCommonConstants.SUCCESS, TraceOrgCommonConstants.FALSE);

                        result.put(TraceOrgCommonConstants.MESSAGE, "The database backup failed!");
                    }
                }
                else
                {
                    result.put(TraceOrgCommonConstants.SUCCESS, TraceOrgCommonConstants.FALSE);

                    result.put(TraceOrgCommonConstants.MESSAGE, "Invalid path for database backup!");
                }
            }
            else
            {
                result.put(TraceOrgCommonConstants.SUCCESS, TraceOrgCommonConstants.FALSE);

                result.put(TraceOrgCommonConstants.MESSAGE, TraceOrgMessageConstants.ENTER_VALID_DETAILS);
            }
        }
        catch (Exception exception)
        {
            _logger.error(exception);
        }

        return result;
    }

    @Override
    public HashMap<String, Object> runDatabaseMaintenanceDetail(Long id)
    {
        HashMap<String, Object> result = new HashMap<>();

        try
        {
            if(id != null && id == 1)
            {
                TraceOrgDatabaseMaintenance traceOrgDatabaseMaintenance =  traceOrgDatabaseMaintenanceRepository.findOne(id);

                if(Strings.isNullOrEmpty(traceOrgDatabaseMaintenance.getStatus()) || traceOrgDatabaseMaintenance.getStatus().equals(TraceOrgCommonConstants.ENABLE))
                {
                    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

                    Date dataFrom = new Date();

                    String currentDate = dateFormat.format(dataFrom);

                    traceOrgEventRepository.deleteEvents(currentDate, traceOrgDatabaseMaintenance.getMaintainedDays());

                    traceOrgIpChangeLogRepository.deleteIpChangeLogs(currentDate, traceOrgDatabaseMaintenance.getMaintainedDays());

                    result.put(TraceOrgCommonConstants.SUCCESS, TraceOrgCommonConstants.TRUE);

                    result.put(TraceOrgCommonConstants.MESSAGE, "Data Retention Applied Successfully");
                }
                else
                {

                    result.put(TraceOrgCommonConstants.SUCCESS, TraceOrgCommonConstants.FALSE);

                    result.put(TraceOrgCommonConstants.MESSAGE, "Data Retention is Disabled");
                }

            }
            else
            {
                result.put(TraceOrgCommonConstants.SUCCESS, TraceOrgCommonConstants.FALSE);

                result.put(TraceOrgCommonConstants.MESSAGE, TraceOrgMessageConstants.ENTER_VALID_DETAILS);
            }
        }
        catch (Exception exception)
        {
            _logger.error(exception);
        }

        return result;
    }
}
