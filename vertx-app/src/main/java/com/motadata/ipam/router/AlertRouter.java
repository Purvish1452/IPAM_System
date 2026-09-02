package com.motadata.ipam.router;

import com.motadata.ipam.service.AlertService;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

/**
 * Vert.x Web router for Alert Stream REST API endpoints.
 */
public class AlertRouter {

    private final AlertService alertService;

    public AlertRouter(AlertService alertService) {
        this.alertService = alertService;
    }

    public void attachRoutes(Router router) {
        router.get("/alerts/").handler(this::handleGetAlerts);
    }

    private void handleGetAlerts(RoutingContext ctx) {
        String alertFilter = ctx.request().getParam("alertFilter");
        String pageStr = ctx.request().getParam("page");
        String pageSizeStr = ctx.request().getParam("pageSize");

        Integer page = (pageStr != null) ? Integer.parseInt(pageStr) : 1;
        Integer pageSize = (pageSizeStr != null) ? Integer.parseInt(pageSizeStr) : 20;

        alertService.getAlerts(alertFilter, page, pageSize).onComplete(ar -> {
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
}
