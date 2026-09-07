package com.motadata.ipam.router;

import com.motadata.ipam.service.EventService;
import com.motadata.ipam.service.ReportService;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

/**
 * Vert.x Web router for Event log REST API endpoints.
 */
public class EventRouter {

    private final EventService eventService;
    private final ReportService reportService;

    public EventRouter(EventService eventService) {
        this(eventService, null);
    }

    public EventRouter(EventService eventService, ReportService reportService) {
        this.eventService = eventService;
        this.reportService = reportService;
    }

    // Registers event log, summary, and top-event routes.
    public void attachRoutes(Router router) {
        router.get("/event/").handler(this::handleGetEvents);
        router.get("/event").handler(this::handleGetEvents);
        router.get("/events/").handler(this::handleGetEvents);
        router.get("/events").handler(this::handleGetEvents);
        router.get("/eventSummary/").handler(this::handleGetEventSummary);
        router.get("/eventSummary").handler(this::handleGetEventSummary);
        router.get("/topEvent/").handler(this::handleGetTopEvents);
        router.get("/topEvent").handler(this::handleGetTopEvents);
    }

    // Retrieves event logs and handles CSV/PDF export requests.
    private void handleGetEvents(RoutingContext ctx) {
        String pdfParam = ctx.request().getParam("pdf");
        String csvParam = ctx.request().getParam("csv");
        String timeline = ctx.request().getParam("exportTimeline");

        if (pdfParam != null && ("true".equalsIgnoreCase(pdfParam) || "1".equals(pdfParam))) {
            if (reportService != null) {
                reportService.generateEventPdfReport().onComplete(ar -> {
                    if (ar.succeeded()) {
                        ctx.response()
                                .putHeader("Content-Type", "application/pdf")
                                .putHeader("Content-Disposition", "attachment; filename=\"Event_Audit_Log_Report.pdf\"")
                                .putHeader("Content-Length", String.valueOf(ar.result().length))
                                .end(Buffer.buffer(ar.result()));
                    } else {
                        ctx.response().setStatusCode(500).putHeader("Content-Type", "application/json;charset=UTF-8")
                                .end(new JsonObject().put("success", false).put("message", ar.cause().getMessage()).encode());
                    }
                });
            } else {
                ctx.response().setStatusCode(500).putHeader("Content-Type", "application/json;charset=UTF-8")
                        .end(new JsonObject().put("success", false).put("message", "ReportService not configured").encode());
            }
            return;
        }

        if (csvParam != null && ("true".equalsIgnoreCase(csvParam) || "1".equals(csvParam))) {
            eventService.generateEventCsvReport(timeline).onComplete(ar -> {
                if (ar.succeeded()) {
                    ctx.response()
                            .putHeader("Content-Type", "text/csv")
                            .putHeader("Content-Disposition", "attachment; filename=\"Event_Audit_Log_Report.csv\"")
                            .end(Buffer.buffer(ar.result()));
                } else {
                    ctx.response().setStatusCode(500).putHeader("Content-Type", "application/json;charset=UTF-8")
                            .end(new JsonObject().put("success", false).put("message", ar.cause().getMessage()).encode());
                }
            });
            return;
        }

        String pageStr = ctx.request().getParam("page");
        String pageSizeStr = ctx.request().getParam("pageSize");

        Integer page = (pageStr != null) ? Integer.parseInt(pageStr) : 1;
        Integer pageSize = (pageSizeStr != null) ? Integer.parseInt(pageSizeStr) : 20;

        eventService.getEvents(page, pageSize, timeline).onComplete(ar -> {
            if (ar.succeeded()) {
                ctx.response()
                        .putHeader("Content-Type", "application/json;charset=UTF-8")
                        .end(ar.result().encode());
            } else {
                ctx.response()
                        .setStatusCode(500)
                        .putHeader("Content-Type", "application/json;charset=UTF-8")
                        .end(new JsonObject().put("success", false).put("message", ar.cause().getMessage()).encode());
            }
        });
    }

    // Returns monthly event summary data.
    private void handleGetEventSummary(RoutingContext ctx) {
        eventService.getEventSummary().onComplete(ar -> sendResult(ctx, ar));
    }

    // Returns top event data.
    private void handleGetTopEvents(RoutingContext ctx) {
        eventService.getTopEvents().onComplete(ar -> sendResult(ctx, ar));
    }

    private void sendResult(RoutingContext ctx, io.vertx.core.AsyncResult<JsonArray> ar) {
        if (ar.succeeded()) {
            ctx.response().putHeader("Content-Type", "application/json;charset=UTF-8")
                    .end(new JsonObject().put("data", ar.result()).put("success", true).encode());
        } else {
            ctx.response().setStatusCode(500).putHeader("Content-Type", "application/json;charset=UTF-8")
                    .end(new JsonObject().put("data", new JsonArray()).put("success", false)
                            .put("message", ar.cause().getMessage()).encode());
        }
    }
}
