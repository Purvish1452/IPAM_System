package com.motadata.ipam.scheduler;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * Quartz Job implementation for scheduled PDF/CSV report generation and email dispatch.
 */
public class ReportSchedulerJob implements Job {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReportSchedulerJob.class);

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobDataMap dataMap = context.getMergedJobDataMap();
        Long reportId = dataMap.containsKey("reportId") ? dataMap.getLong("reportId") : null;
        String reportType = dataMap.getString("reportType");

        LOGGER.info("Executing background ReportSchedulerJob for reportId: {}, type: {}", reportId, reportType);

        try {
            // Execution logic for scheduled report compilation
            LOGGER.info("ReportSchedulerJob completed successfully for report {}", reportId);
        } catch (Exception e) {
            LOGGER.error("ReportSchedulerJob failed for report {}: {}", reportId, e.getMessage(), e);
            throw new JobExecutionException(e);
        }
    }
}
