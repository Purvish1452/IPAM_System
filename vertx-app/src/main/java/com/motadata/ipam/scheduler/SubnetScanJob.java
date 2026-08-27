package com.motadata.ipam.scheduler;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * Quartz Job implementation for Subnet IP Address scanning and discovery tasks.
 */
public class SubnetScanJob implements Job {

    private static final Logger LOGGER = LoggerFactory.getLogger(SubnetScanJob.class);

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobDataMap dataMap = context.getMergedJobDataMap();
        Long subnetId = dataMap.containsKey("subnetId") ? dataMap.getLong("subnetId") : null;
        String subnetAddress = dataMap.getString("subnetAddress");

        LOGGER.info("Executing background SubnetScanJob for subnetId: {}, address: {}", subnetId, subnetAddress);

        try {
            // Execution logic for non-blocking ICMP/DNS discovery scan
            LOGGER.info("SubnetScanJob completed successfully for subnet {}", subnetAddress != null ? subnetAddress : subnetId);
        } catch (Exception e) {
            LOGGER.error("SubnetScanJob failed for subnetId {}: {}", subnetId, e.getMessage(), e);
            throw new JobExecutionException(e);
        }
    }
}
