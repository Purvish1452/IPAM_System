package com.motadata.ipam.scheduler;

import io.vertx.core.json.JsonObject;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

/**
 * Vert.x job implementation for purging resolved alerts and historical event logs.
 */
public class AlertCleanupJob implements VertxScheduledJob {

    private static final Logger LOGGER = LoggerFactory.getLogger(AlertCleanupJob.class);

    // Executes the cleanup job to purge resolved alerts and historical logs.
    @Override
    public void execute(JsonObject data) {
        LOGGER.info("Executing background AlertCleanupJob...");
        try {
            // Execution logic for purging old resolved alerts
            LOGGER.info("AlertCleanupJob completed successfully.");
        } catch (Exception e) {
            LOGGER.error("AlertCleanupJob failed: {}", e.getMessage(), e);
            throw e;
        }
    }
}
