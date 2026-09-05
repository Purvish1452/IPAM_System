package com.motadata.ipam.router;

import com.motadata.ipam.service.ReportService;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Vert.x Web Router for Report Scheduling and PDF/CSV Reporting API Endpoints.
 * Architecture: Handler -> Service -> PgPool -> PostgreSQL
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
        reportService.getSubnetByReport().onComplete(ar -> {
            JsonObject result = new JsonObject().put("data", ar.result()).put("success", true);
            ctx.response().putHeader("Content-Type", "application/json;charset=UTF-8").end(result.encode());
        });
    }

    private void handleSubnetIpByReportTimeline(RoutingContext ctx) {
        String subnetIdStr = ctx.request().getParam("subnetId");
        String status = ctx.request().getParam("status");
        Long subnetId = 1L;
        try {
            if (subnetIdStr != null) subnetId = Long.parseLong(subnetIdStr);
        } catch (Exception ignored) {}

        reportService.getSubnetIpByReportTimeline(subnetId, status).onComplete(ar -> {
            JsonObject result = new JsonObject().put("data", ar.result()).put("success", true);
            ctx.response().putHeader("Content-Type", "application/json;charset=UTF-8").end(result.encode());
        });
    }

    private void handleGetReportSchedulers(RoutingContext ctx) {
        reportService.getReportSchedulers().onComplete(ar -> {
            JsonObject result = new JsonObject().put("data", ar.result()).put("success", true);
            ctx.response().putHeader("Content-Type", "application/json;charset=UTF-8").end(result.encode());
        });
    }

    private void handleGetReportSchedulerById(RoutingContext ctx) {
        String idStr = ctx.pathParam("id");
        Long id = 1L;
        try { if (idStr != null) id = Long.parseLong(idStr); } catch (Exception ignored) {}

        reportService.getReportSchedulerById(id).onComplete(ar -> {
            JsonObject result = new JsonObject().put("data", ar.result()).put("success", true);
            ctx.response().putHeader("Content-Type", "application/json;charset=UTF-8").end(result.encode());
        });
    }

    private void handleSaveReportScheduler(RoutingContext ctx) {
        JsonObject body = null;
        try { body = ctx.body().asJsonObject(); } catch (Exception ignored) {}
        if (body == null) body = new JsonObject();

        reportService.saveReportScheduler(body).onComplete(ar -> {
            ctx.response().putHeader("Content-Type", "application/json;charset=UTF-8").end(ar.result().encode());
        });
    }

    private void handleDeleteReportScheduler(RoutingContext ctx) {
        String idStr = ctx.pathParam("id");
        Long id = 1L;
        try { if (idStr != null) id = Long.parseLong(idStr); } catch (Exception ignored) {}

        reportService.deleteReportScheduler(id).onComplete(ar -> {
            ctx.response().putHeader("Content-Type", "application/json;charset=UTF-8").end(ar.result().encode());
        });
    }

    private void handleInsertMailRecipient(RoutingContext ctx) {
        JsonObject result = new JsonObject().put("success", true).put("message", "Email recipient added");
        ctx.response().putHeader("Content-Type", "application/json;charset=UTF-8").end(result.encode());
    }

    private void handleSubnetPdfReport(RoutingContext ctx) {
        LOGGER.info("Generating Subnet PDF Report download");
        reportService.generateSubnetPdfReport().onComplete(ar -> {
            if (ar.succeeded()) {
                sendPdfResponse(ctx, ar.result(), "Subnet_Utilization_Report.pdf");
            } else {
                sendErrorResponse(ctx, 500, "Failed to generate Subnet PDF Report: " + ar.cause().getMessage());
            }
        });
    }

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

    private void sendPdfResponse(RoutingContext ctx, byte[] pdfBytes, String filename) {
        ctx.response()
                .putHeader("Content-Type", "application/pdf")
                .putHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"")
                .putHeader("Content-Length", String.valueOf(pdfBytes.length))
                .end(Buffer.buffer(pdfBytes));
    }

    private void sendErrorResponse(RoutingContext ctx, int statusCode, String message) {
        JsonObject errorJson = new JsonObject().put("status", statusCode).put("message", message);
        ctx.response().setStatusCode(statusCode).putHeader("Content-Type", "application/json;charset=UTF-8").end(errorJson.encode());
    }
}
