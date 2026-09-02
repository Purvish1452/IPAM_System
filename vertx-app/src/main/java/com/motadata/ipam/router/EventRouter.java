package com.motadata.ipam.router;

import com.motadata.ipam.service.EventService;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

/**
 * Vert.x Web router for Event log REST API endpoints.
 */
public class EventRouter {

    private final EventService eventService;

    public EventRouter(EventService eventService) {
        this.eventService = eventService;
    }

    public void attachRoutes(Router router) {
        router.get("/event/").handler(this::handleGetEvents);
        router.get("/eventSummary/").handler(this::handleGetEventSummary);
        router.get("/topEvent/").handler(this::handleGetTopEvents);
    }

    private void handleGetEvents(RoutingContext ctx) {
        String pdfParam = ctx.request().getParam("pdf");
        String csvParam = ctx.request().getParam("csv");

        if ("true".equalsIgnoreCase(csvParam)) {
            String csvData = "ID,Event Type,Event Context,Message,Username,Timestamp\n" +
                    "1,Information,Subnet Management,\"Subnet 192.168.10.0 is added in IP Address Manager by admin\",admin,2026-09-02 10:00:00\n" +
                    "2,Information,DHCP Management,\"DHCP Server WinDHCP-Primary synced\",admin,2026-09-02 10:15:00\n";
            ctx.response()
                    .putHeader("Content-Type", "text/csv")
                    .putHeader("Content-Disposition", "attachment; filename=\"Event_Logs.csv\"")
                    .end(csvData);
            return;
        }

        if ("true".equalsIgnoreCase(pdfParam)) {
            String csvData = "ID,Event Type,Event Context,Message,Username,Timestamp\n" +
                    "1,Information,Subnet Management,\"Subnet 192.168.10.0 is added in IP Address Manager by admin\",admin,2026-09-02 10:00:00\n" +
                    "2,Information,DHCP Management,\"DHCP Server WinDHCP-Primary synced\",admin,2026-09-02 10:15:00\n";
            ctx.response()
                    .putHeader("Content-Type", "text/csv")
                    .putHeader("Content-Disposition", "attachment; filename=\"Event_Logs.pdf\"")
                    .end(csvData);
            return;
        }

        String pageStr = ctx.request().getParam("page");
        String pageSizeStr = ctx.request().getParam("pageSize");

        Integer page = (pageStr != null) ? Integer.parseInt(pageStr) : 1;
        Integer pageSize = (pageSizeStr != null) ? Integer.parseInt(pageSizeStr) : 20;

        eventService.getEvents(page, pageSize).onComplete(ar -> {
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

    private void handleGetEventSummary(RoutingContext ctx) {
        JsonArray data = new JsonArray()
                .add(new JsonObject().put("month", "Jan").put("count", 12))
                .add(new JsonObject().put("month", "Feb").put("count", 18))
                .add(new JsonObject().put("month", "Mar").put("count", 25));

        JsonObject result = new JsonObject()
                .put("data", data)
                .put("success", true);

        ctx.response()
                .putHeader("Content-Type", "application/json;charset=UTF-8")
                .end(result.encode());
    }

    private void handleGetTopEvents(RoutingContext ctx) {
        JsonArray data = new JsonArray();

        JsonObject result = new JsonObject()
                .put("data", data)
                .put("success", true);

        ctx.response()
                .putHeader("Content-Type", "application/json;charset=UTF-8")
                .end(result.encode());
    }
}
