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
    }

    private void handleGetEvents(RoutingContext ctx) {
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
}
