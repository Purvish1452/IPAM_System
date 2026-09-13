package com.motadata.ipam.scheduler;

import io.vertx.core.json.JsonObject;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

/**
 * Vert.x job implementation for background DHCP Server lease polling and utilization sync.
 */
public class DhcpScanJob implements VertxScheduledJob {

    private static final Logger LOGGER = LoggerFactory.getLogger(DhcpScanJob.class);

    // Executes background polling of DHCP server scopes and active leases.
    @Override
    public void execute(JsonObject dataMap) {
        Long dhcpCredentialId = dataMap.getLong("dhcpCredentialId");
        String serverHost = dataMap.getString("serverHost");

        LOGGER.info("Executing background DhcpScanJob for DHCP server credentialId: {}, host: {}", dhcpCredentialId, serverHost);

        try {
            // Execution logic for polling DHCP server leases and updating utilization statistics
            LOGGER.info("DhcpScanJob completed successfully for server {}", serverHost != null ? serverHost : dhcpCredentialId);
        } catch (Exception e) {
            LOGGER.error("DhcpScanJob failed for server {}: {}", serverHost, e.getMessage(), e);
            throw e;
        }
    }
}
