package com.motadata.ipam.router;

import com.motadata.ipam.security.PermissionHandler;
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

    // Registers Alert API routes with permission validation.
    public void attachRoutes(Router router) {
        router.get("/alerts").handler(PermissionHandler.require("PERM_ALERTS_READ")).handler(this::handleGetAlerts);
        router.get("/alerts/").handler(PermissionHandler.require("PERM_ALERTS_READ")).handler(this::handleGetAlerts);
    }

    // Handles GET alert requests with filtering and pagination.
    private void handleGetAlerts(RoutingContext ctx) {
        String alertFilter = ctx.request().getParam("alertFilter");

        Integer page = parsePositiveInteger(ctx, "page", 1);
        Integer pageSize = parsePositiveInteger(ctx, "pageSize", 20);
        if (page == null || pageSize == null) {
            return;
        }

        alertService.getAlerts(alertFilter, page, pageSize)
                .onSuccess(result -> sendJson(ctx, 200, result))
                .onFailure(error -> sendJson(ctx, 500, new JsonObject()
                        .put("success", false)
                        .put("message", error.getMessage() != null ? error.getMessage() : "Unable to load alerts")));
    }

    // Validates and parses positive integer query parameters.
    private Integer parsePositiveInteger(RoutingContext ctx, String parameterName, int defaultValue) {
        String value = ctx.request().getParam(parameterName);
        if (value == null || value.isBlank()) {
            return defaultValue;
        }

        try {
            int parsed = Integer.parseInt(value);
            if (parsed > 0) {
                return parsed;
            }
        } catch (NumberFormatException ignored) {
            // Return a consistent client error below for malformed query parameters.
        }

        sendJson(ctx, 400, new JsonObject()
                .put("success", false)
                .put("message", parameterName + " must be a positive integer"));
        return null;
    }

    // Sends a JSON response with the given HTTP status.
    private void sendJson(RoutingContext ctx, int statusCode, JsonObject body) {
        ctx.response()
                .setStatusCode(statusCode)
                .putHeader("Content-Type", "application/json;charset=UTF-8")
                .end(body.encode());
    }
}
