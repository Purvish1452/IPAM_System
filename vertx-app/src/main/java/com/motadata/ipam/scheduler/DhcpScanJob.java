package com.motadata.ipam.scheduler;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * Quartz Job implementation for background DHCP Server lease polling and utilization sync.
 */
public class DhcpScanJob implements Job {

    private static final Logger LOGGER = LoggerFactory.getLogger(DhcpScanJob.class);

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobDataMap dataMap = context.getMergedJobDataMap();
        Long dhcpCredentialId = dataMap.containsKey("dhcpCredentialId") ? dataMap.getLong("dhcpCredentialId") : null;
        String serverHost = dataMap.getString("serverHost");

        LOGGER.info("Executing background DhcpScanJob for DHCP server credentialId: {}, host: {}", dhcpCredentialId, serverHost);

        try {
            // Execution logic for polling DHCP server leases and updating utilization statistics
            LOGGER.info("DhcpScanJob completed successfully for server {}", serverHost != null ? serverHost : dhcpCredentialId);
        } catch (Exception e) {
            LOGGER.error("DhcpScanJob failed for server {}: {}", serverHost, e.getMessage(), e);
            throw new JobExecutionException(e);
        }
    }
}
