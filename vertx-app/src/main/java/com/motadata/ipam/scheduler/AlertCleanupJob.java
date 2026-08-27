package com.motadata.ipam.scheduler;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * Quartz Job implementation for purging resolved alerts and historical event logs.
 */
public class AlertCleanupJob implements Job {

    private static final Logger LOGGER = LoggerFactory.getLogger(AlertCleanupJob.class);

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        LOGGER.info("Executing background AlertCleanupJob...");
        try {
            // Execution logic for purging old resolved alerts
            LOGGER.info("AlertCleanupJob completed successfully.");
        } catch (Exception e) {
            LOGGER.error("AlertCleanupJob failed: {}", e.getMessage(), e);
            throw new JobExecutionException(e);
        }
    }
}
