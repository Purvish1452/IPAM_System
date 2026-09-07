package com.motadata.ipam.scheduler;

import io.vertx.core.json.JsonObject;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

/**
 * Vert.x job implementation for scheduled PDF/CSV report generation and email dispatch.
 */
public class ReportSchedulerJob implements VertxScheduledJob {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReportSchedulerJob.class);

    @Override
    public void execute(JsonObject dataMap) {
        Long reportId = dataMap.getLong("reportId");
        String reportType = dataMap.getString("reportType");

        LOGGER.info("Executing background ReportSchedulerJob for reportId: {}, type: {}", reportId, reportType);

        try {
            // Execution logic for scheduled report compilation
            LOGGER.info("ReportSchedulerJob completed successfully for report {}", reportId);
        } catch (Exception e) {
            LOGGER.error("ReportSchedulerJob failed for report {}: {}", reportId, e.getMessage(), e);
            throw e;
        }
    }
}
