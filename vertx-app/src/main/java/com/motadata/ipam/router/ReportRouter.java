package com.motadata.ipam.router;

import com.motadata.ipam.service.ReportService;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

/**
 * Vert.x Web Router for PDF Reporting API Endpoints.
 */
public class ReportRouter {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReportRouter.class);

    private final ReportService reportService;

    public ReportRouter(ReportService reportService) {
        this.reportService = reportService;
    }

    public void attachRoutes(Router router) {
        router.get("/api/v1/reports/subnets/pdf").handler(this::handleSubnetPdfReport);
        router.get("/api/v1/reports/alerts/pdf").handler(this::handleAlertPdfReport);
        router.get("/api/v1/reports/events/pdf").handler(this::handleEventPdfReport);
        router.get("/api/v1/reports/dhcp/pdf").handler(this::handleDhcpPdfReport);
    }

    private void handleSubnetPdfReport(RoutingContext ctx) {
        LOGGER.info("Received request for Subnet PDF Report download");
        reportService.generateSubnetPdfReport().onComplete(ar -> {
            if (ar.succeeded()) {
                sendPdfResponse(ctx, ar.result(), "Subnet_Utilization_Report.pdf");
            } else {
                sendErrorResponse(ctx, 500, "Failed to generate Subnet PDF Report: " + ar.cause().getMessage());
            }
        });
    }

    private void handleAlertPdfReport(RoutingContext ctx) {
        LOGGER.info("Received request for Alert PDF Report download");
        reportService.generateAlertPdfReport().onComplete(ar -> {
            if (ar.succeeded()) {
                sendPdfResponse(ctx, ar.result(), "Alert_History_Report.pdf");
            } else {
                sendErrorResponse(ctx, 500, "Failed to generate Alert PDF Report: " + ar.cause().getMessage());
            }
        });
    }

    private void handleEventPdfReport(RoutingContext ctx) {
        LOGGER.info("Received request for Event PDF Report download");
        reportService.generateEventPdfReport().onComplete(ar -> {
            if (ar.succeeded()) {
                sendPdfResponse(ctx, ar.result(), "Event_Audit_Log_Report.pdf");
            } else {
                sendErrorResponse(ctx, 500, "Failed to generate Event PDF Report: " + ar.cause().getMessage());
            }
        });
    }

    private void handleDhcpPdfReport(RoutingContext ctx) {
        LOGGER.info("Received request for DHCP PDF Report download");
        reportService.generateDhcpPdfReport().onComplete(ar -> {
            if (ar.succeeded()) {
                sendPdfResponse(ctx, ar.result(), "DHCP_Server_Report.pdf");
            } else {
                sendErrorResponse(ctx, 500, "Failed to generate DHCP PDF Report: " + ar.cause().getMessage());
            }
        });
    }

    private void sendPdfResponse(RoutingContext ctx, byte[] pdfBytes, String filename) {
        ctx.response()
                .putHeader("Content-Type", "application/pdf")
                .putHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"")
                .putHeader("Content-Length", String.valueOf(pdfBytes.length))
                .end(Buffer.buffer(pdfBytes));
    }

    private void sendErrorResponse(RoutingContext ctx, int statusCode, String message) {
        JsonObject errorJson = new JsonObject()
                .put("status", statusCode)
                .put("message", message);

        ctx.response()
                .setStatusCode(statusCode)
                .putHeader("Content-Type", "application/json;charset=UTF-8")
                .end(errorJson.encode());
    }
}
