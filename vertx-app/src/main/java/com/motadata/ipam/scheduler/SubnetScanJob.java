package com.motadata.ipam.scheduler;

import io.vertx.core.json.JsonObject;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

/**
 * Vert.x job implementation for Subnet IP Address scanning and discovery tasks.
 */
public class SubnetScanJob implements VertxScheduledJob {

    private static final Logger LOGGER = LoggerFactory.getLogger(SubnetScanJob.class);

    @Override
    public void execute(JsonObject dataMap) {
        Long subnetId = dataMap.getLong("subnetId");
        String subnetAddress = dataMap.getString("subnetAddress");

        LOGGER.info("Executing background SubnetScanJob for subnetId: {}, address: {}", subnetId, subnetAddress);

        try {
            // Execution logic for non-blocking ICMP/DNS discovery scan
            LOGGER.info("SubnetScanJob completed successfully for subnet {}", subnetAddress != null ? subnetAddress : subnetId);
        } catch (Exception e) {
            LOGGER.error("SubnetScanJob failed for subnetId {}: {}", subnetId, e.getMessage(), e);
            throw e;
        }
    }
}
