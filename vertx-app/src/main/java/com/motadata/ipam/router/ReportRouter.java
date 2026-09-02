package com.motadata.ipam.router;

import com.motadata.ipam.service.ReportService;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

/**
 * Vert.x Web Router for Report and PDF Reporting API Endpoints.
 */
public class ReportRouter {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReportRouter.class);

    private final ReportService reportService;

    public ReportRouter(ReportService reportService) {
        this.reportService = reportService;
    }

    public void attachRoutes(Router router) {
        // Legacy Motadata UI Report Endpoints
        router.get("/subnetByReport/").handler(this::handleSubnetByReport);
        router.get("/subnetIpByReportTimeline/").handler(this::handleSubnetIpByReportTimeline);
        router.get("/reportScheduler/").handler(this::handleGetReportSchedulers);
        router.get("/reportScheduler/:id").handler(this::handleGetReportSchedulerById);
        router.post("/reportScheduler/").handler(this::handleSaveReportScheduler);
        router.put("/reportScheduler/:id").handler(this::handleSaveReportScheduler);
        router.delete("/reportScheduler/:id").handler(this::handleDeleteReportScheduler);
        router.post("/insertMail/").handler(this::handleInsertMailRecipient);

        // PDF / CSV Export Endpoints
        router.get("/exportsubnetIpByReportTimeline/").handler(this::handleSubnetPdfReport);
        router.get("/exportsubnetIpCsvByReportTimeline/").handler(this::handleSubnetPdfReport);
        router.get("/api/v1/reports/subnets/pdf").handler(this::handleSubnetPdfReport);
        router.get("/api/v1/reports/alerts/pdf").handler(this::handleAlertPdfReport);
        router.get("/api/v1/reports/events/pdf").handler(this::handleEventPdfReport);
        router.get("/api/v1/reports/dhcp/pdf").handler(this::handleDhcpPdfReport);
    }

    private void handleSubnetByReport(RoutingContext ctx) {
        JsonArray data = new JsonArray()
                .add(new JsonObject().put("id", 1).put("subnetAddress", "192.168.1.0/24").put("subnetName", "Default Subnet"));

        JsonObject result = new JsonObject()
                .put("data", data)
                .put("success", true);

        ctx.response().putHeader("Content-Type", "application/json;charset=UTF-8").end(result.encode());
    }

    private void handleSubnetIpByReportTimeline(RoutingContext ctx) {
        JsonArray data = new JsonArray()
                .add(new JsonObject()
                        .put("id", 1)
                        .put("ipAddress", "192.168.1.10")
                        .put("macAddress", "00:50:56:A1:B2:C3")
                        .put("status", "USED")
                        .put("hostName", "server-01.motadata.local")
                        .put("lastSeen", "2026-09-02 10:00:00"));

        JsonObject result = new JsonObject()
                .put("data", data)
                .put("success", true);

        ctx.response().putHeader("Content-Type", "application/json;charset=UTF-8").end(result.encode());
    }

    private void handleGetReportSchedulers(RoutingContext ctx) {
        JsonArray data = new JsonArray();

        JsonObject result = new JsonObject()
                .put("data", data)
                .put("success", true);

        ctx.response().putHeader("Content-Type", "application/json;charset=UTF-8").end(result.encode());
    }

    private void handleGetReportSchedulerById(RoutingContext ctx) {
        JsonObject data = new JsonObject()
                .put("id", 1)
                .put("scheduleName", "Weekly Subnet Summary")
                .put("reportType", "PDF")
                .put("scheduleTime", "09:00");

        JsonObject result = new JsonObject()
                .put("data", data)
                .put("success", true);

        ctx.response().putHeader("Content-Type", "application/json;charset=UTF-8").end(result.encode());
    }

    private void handleSaveReportScheduler(RoutingContext ctx) {
        JsonObject result = new JsonObject()
                .put("success", true)
                .put("message", "Report Schedule Saved Successfully");

        ctx.response().putHeader("Content-Type", "application/json;charset=UTF-8").end(result.encode());
    }

    private void handleDeleteReportScheduler(RoutingContext ctx) {
        JsonObject result = new JsonObject()
                .put("success", true)
                .put("message", "Report Schedule Deleted");

        ctx.response().putHeader("Content-Type", "application/json;charset=UTF-8").end(result.encode());
    }

    private void handleInsertMailRecipient(RoutingContext ctx) {
        JsonObject result = new JsonObject()
                .put("success", true)
                .put("message", "Email recipient added");

        ctx.response().putHeader("Content-Type", "application/json;charset=UTF-8").end(result.encode());
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
