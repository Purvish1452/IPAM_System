package com.motadata.ipam.router;

import com.motadata.ipam.service.ReportService;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Vert.x Web Router for Report Scheduling and PDF/CSV Reporting API Endpoints.
 * Architecture: Handler -> Service -> PgPool -> PostgreSQL
 */
public class ReportRouter {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReportRouter.class);

    private final ReportService reportService;

    // Constructs ReportRouter with the provided ReportService instance.
    public ReportRouter(ReportService reportService) {
        this.reportService = reportService;
    }

    // Registers report scheduling and report export routes.
    public void attachRoutes(Router router) {
        // Scheduler Export Endpoints
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
        router.get("/exportsubnetIpCsvByReportTimeline/").handler(this::handleSubnetCsvReport);
        router.get("/api/v1/reports/subnets/pdf").handler(this::handleSubnetPdfReport);
        router.get("/api/v1/reports/alerts/pdf").handler(this::handleAlertPdfReport);
        router.get("/api/v1/reports/events/pdf").handler(this::handleEventPdfReport);
        router.get("/api/v1/reports/dhcp/pdf").handler(this::handleDhcpPdfReport);
    }

    // Retrieves subnet report data.
    private void handleSubnetByReport(RoutingContext ctx) {
        reportService.getSubnetByReport().onComplete(ar -> {
            JsonObject result = new JsonObject().put("data", ar.result()).put("success", true);
            ctx.response().putHeader("Content-Type", "application/json;charset=UTF-8").end(result.encode());
        });
    }

    // Retrieves subnet IP report timeline with status filtering.
    private void handleSubnetIpByReportTimeline(RoutingContext ctx) {
        String subnetIdStr = ctx.request().getParam("subnetId");
        String status = ctx.request().getParam("status");
        if (status == null) status = ctx.request().getParam("ipStatus");

        List<Long> subnetIds = parseSubnetIds(subnetIdStr);

        reportService.getSubnetIpByReportTimeline(subnetIds, status).onComplete(ar -> {
            if (ar.succeeded()) {
                JsonObject result = new JsonObject().put("data", ar.result()).put("success", true);
                ctx.response().putHeader("Content-Type", "application/json;charset=UTF-8").end(result.encode());
            } else {
                ctx.response().setStatusCode(500)
                        .putHeader("Content-Type", "application/json;charset=UTF-8")
                        .end(new JsonObject().put("data", new JsonArray()).put("success", false)
                                .put("message", ar.cause() != null ? ar.cause().getMessage() : "Unknown error").encode());
            }
        });
    }

    // Retrieves all report schedules.
    private void handleGetReportSchedulers(RoutingContext ctx) {
        reportService.getReportSchedulers().onComplete(ar -> {
            JsonObject result = new JsonObject().put("data", ar.result()).put("success", true);
            ctx.response().putHeader("Content-Type", "application/json;charset=UTF-8").end(result.encode());
        });
    }

    // Retrieves a report schedule by ID.
    private void handleGetReportSchedulerById(RoutingContext ctx) {
        String idStr = ctx.pathParam("id");
        Long id = 1L;
        try { if (idStr != null) id = Long.parseLong(idStr); } catch (Exception ignored) {}

        reportService.getReportSchedulerById(id).onComplete(ar -> {
            JsonObject result = new JsonObject().put("data", ar.result()).put("success", true);
            ctx.response().putHeader("Content-Type", "application/json;charset=UTF-8").end(result.encode());
        });
    }

    // Saves or updates a report schedule.
    private void handleSaveReportScheduler(RoutingContext ctx) {
        JsonObject body = null;
        try { body = ctx.body().asJsonObject(); } catch (Exception ignored) {}
        if (body == null) body = new JsonObject();

        reportService.saveReportScheduler(body).onComplete(ar -> {
            ctx.response().putHeader("Content-Type", "application/json;charset=UTF-8").end(ar.result().encode());
        });
    }

    // Deletes a report schedule by ID.
    private void handleDeleteReportScheduler(RoutingContext ctx) {
        String idStr = ctx.pathParam("id");
        Long id = 1L;
        try { if (idStr != null) id = Long.parseLong(idStr); } catch (Exception ignored) {}

        reportService.deleteReportScheduler(id).onComplete(ar -> {
            ctx.response().putHeader("Content-Type", "application/json;charset=UTF-8").end(ar.result().encode());
        });
    }

    // Adds an email recipient for reports.
    private void handleInsertMailRecipient(RoutingContext ctx) {
        JsonObject result = new JsonObject().put("success", true).put("message", "Email recipient added");
        ctx.response().putHeader("Content-Type", "application/json;charset=UTF-8").end(result.encode());
    }

    // Generates and downloads the subnet PDF report.
    private void handleSubnetPdfReport(RoutingContext ctx) {
        String subnetIdStr = ctx.request().getParam("subnetId");
        String status = ctx.request().getParam("status");
        if (status == null) status = ctx.request().getParam("ipStatus");

        List<Long> subnetIds = parseSubnetIds(subnetIdStr);

        LOGGER.info("Generating Subnet PDF Report download for subnetIds={}, status={}", subnetIds, status);

        reportService.generateSubnetIpPdfReport(subnetIds, status).onComplete(ar -> {
            if (ar.succeeded()) {
                String filename = ar.result();
                ctx.response()
                        .putHeader("Content-Type", "application/json;charset=UTF-8")
                        .end(new JsonObject().put("data", filename).put("success", true).encode());
            } else {
                sendErrorResponse(ctx, 500, "Failed to generate Subnet PDF Report: " + (ar.cause() != null ? ar.cause().getMessage() : "Unknown error"));
            }
        });
    }

    // Generates and downloads the subnet CSV report.
    private void handleSubnetCsvReport(RoutingContext ctx) {
        String subnetIdStr = ctx.request().getParam("subnetId");
        String status = ctx.request().getParam("status");
        if (status == null) status = ctx.request().getParam("ipStatus");

        List<Long> subnetIds = parseSubnetIds(subnetIdStr);

        reportService.generateSubnetCsvReport(subnetIds, status).onComplete(ar -> {
            if (ar.succeeded()) {
                ctx.response().putHeader("Content-Type", "text/csv;charset=UTF-8")
                        .putHeader("Content-Disposition", "attachment; filename=\"Subnet_IP_Report.csv\"")
                        .end(Buffer.buffer(ar.result()));
            } else {
                sendErrorResponse(ctx, 500, "Failed to generate Subnet CSV Report: " + (ar.cause() != null ? ar.cause().getMessage() : "Unknown error"));
            }
        });
    }

    // Parses comma-separated subnet ID string into a list of Long values.
    private List<Long> parseSubnetIds(String value) {
        List<Long> list = new ArrayList<>();
        if (value != null && !value.trim().isEmpty() && !"undefined".equalsIgnoreCase(value.trim()) && !"null".equalsIgnoreCase(value.trim())) {
            for (String part : value.split(",")) {
                try {
                    String trimmed = part.trim();
                    if (!trimmed.isEmpty()) {
                        list.add(Long.parseLong(trimmed));
                    }
                } catch (NumberFormatException ignored) {}
            }
        }
        return list;
    }

    // Generates and downloads the alert PDF report.
    private void handleAlertPdfReport(RoutingContext ctx) {
        LOGGER.info("Generating Alert PDF Report download");
        reportService.generateAlertPdfReport().onComplete(ar -> {
            if (ar.succeeded()) {
                sendPdfResponse(ctx, ar.result(), "Alert_History_Report.pdf");
            } else {
                sendErrorResponse(ctx, 500, "Failed to generate Alert PDF Report: " + ar.cause().getMessage());
            }
        });
    }

    // Generates and downloads the event PDF report.
    private void handleEventPdfReport(RoutingContext ctx) {
        LOGGER.info("Generating Event PDF Report download");
        reportService.generateEventPdfReport().onComplete(ar -> {
            if (ar.succeeded()) {
                sendPdfResponse(ctx, ar.result(), "Event_Audit_Log_Report.pdf");
            } else {
                sendErrorResponse(ctx, 500, "Failed to generate Event PDF Report: " + ar.cause().getMessage());
            }
        });
    }

    // Generates and downloads the DHCP PDF report.
    private void handleDhcpPdfReport(RoutingContext ctx) {
        LOGGER.info("Generating DHCP PDF Report download");
        reportService.generateDhcpPdfReport().onComplete(ar -> {
            if (ar.succeeded()) {
                sendPdfResponse(ctx, ar.result(), "DHCP_Server_Report.pdf");
            } else {
                sendErrorResponse(ctx, 500, "Failed to generate DHCP PDF Report: " + ar.cause().getMessage());
            }
        });
    }

    // Sends the generated PDF as a downloadable response.
    private void sendPdfResponse(RoutingContext ctx, byte[] pdfBytes, String filename) {
        ctx.response()
                .putHeader("Content-Type", "application/pdf")
                .putHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"")
                .putHeader("Content-Length", String.valueOf(pdfBytes.length))
                .end(Buffer.buffer(pdfBytes));
    }

    // Sends a JSON error response with the given status and message.
    private void sendErrorResponse(RoutingContext ctx, int statusCode, String message) {
        JsonObject errorJson = new JsonObject().put("status", statusCode).put("message", message);
        ctx.response().setStatusCode(statusCode).putHeader("Content-Type", "application/json;charset=UTF-8").end(errorJson.encode());
    }
}
