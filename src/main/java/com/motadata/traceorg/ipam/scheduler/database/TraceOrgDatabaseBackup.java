package com.motadata.traceorg.ipam.scheduler.database;

import com.google.common.base.Strings;
import com.motadata.traceorg.ipam.logger.TraceOrgLogger;
import com.motadata.traceorg.ipam.util.TraceOrgCommonConstants;
import com.motadata.traceorg.ipam.util.TraceOrgCommonUtil;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class TraceOrgDatabaseBackup implements Job {

    private final TraceOrgLogger _logger = new TraceOrgLogger(TraceOrgDatabaseBackup.class, "Database Backup");

    /**
     * IPAM-145 : System should have rogue device detection capability
     * Schedule Database backup not working because of autowired object so change the logic.
     * @param jobExecutionContext
     * @throws JobExecutionException
     */
    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException
    {
        try
        {
            JobDataMap dataMap = jobExecutionContext.getMergedJobDataMap();

            String backupPath = TraceOrgCommonUtil.getStringValue(dataMap.get("path"));

            TraceOrgCommonUtil traceOrgCommonUtil = (TraceOrgCommonUtil) dataMap.get(TraceOrgCommonConstants.TRACE_ORG_COMMON_UTIL);

            if(!Strings.isNullOrEmpty(backupPath))
            {
                traceOrgCommonUtil.backup(backupPath);
            }

        }
        catch (Exception exception)
        {
            _logger.error(exception);
        }
    }
}
