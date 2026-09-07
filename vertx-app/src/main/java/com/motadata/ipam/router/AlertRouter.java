package com.motadata.ipam.router;

import com.motadata.ipam.service.AlertService;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Vert.x Web router for Alert Stream REST API endpoints.
 */
public class AlertRouter {

    private static final Logger LOGGER = LoggerFactory.getLogger(AlertRouter.class);

    private final AlertService alertService;

    public AlertRouter(AlertService alertService) {
        this.alertService = alertService;
    }

    // Registers Alert API routes.
    public void attachRoutes(Router router) {
        router.get("/alerts").handler(this::handleGetAlerts);
        router.get("/alerts/").handler(this::handleGetAlerts);
        router.post("/alerts").handler(this::handleCreateAlert);
        router.post("/alerts/").handler(this::handleCreateAlert);
        router.put("/alerts/:id/clear").handler(this::handleClearAlert);
        router.post("/clearAlert/:id").handler(this::handleClearAlert);
        router.post("/clearAlert/").handler(this::handleClearAllAlerts);
        router.delete("/alerts/:id").handler(this::handleDeleteAlert);
    }

    // Handles GET alert requests with filtering, searching, and pagination.
    private void handleGetAlerts(RoutingContext ctx) {
        String alertFilter = ctx.request().getParam("alertFilter");
        String search = ctx.request().getParam("search");
        if (search == null) search = ctx.request().getParam("searchFilter");

        Integer page = parsePositiveInteger(ctx, "page", 1);
        Integer pageSize = parsePositiveInteger(ctx, "pageSize", 20);
        if (page == null || pageSize == null) {
            return;
        }

        alertService.getAlerts(alertFilter, search, page, pageSize)
                .onSuccess(result -> sendJson(ctx, 200, result))
                .onFailure(error -> sendJson(ctx, 500, new JsonObject()
                        .put("success", false)
                        .put("message", error.getMessage() != null ? error.getMessage() : "Unable to load alerts")));
    }

    // Handles manual creation of an alert.
    private void handleCreateAlert(RoutingContext ctx) {
        JsonObject body = ctx.body().asJsonObject();
        if (body == null) {
            sendJson(ctx, 400, new JsonObject().put("success", false).put("message", "Request body is required"));
            return;
        }

        Long subnetId = body.getLong("subnetId", 1L);
        String alertType = body.getString("alertType", "INFO");
        String message = body.getString("message", "Manual alert");
        String subnet = body.getString("subnet", "General");
        boolean status = body.getBoolean("status", true);

        alertService.createAlert(subnetId, alertType, message, subnet, status)
                .onSuccess(result -> sendJson(ctx, 201, result))
                .onFailure(error -> sendJson(ctx, 500, new JsonObject().put("success", false).put("message", error.getMessage())));
    }

    // Handles clearing an alert by ID.
    private void handleClearAlert(RoutingContext ctx) {
        String idStr = ctx.pathParam("id");
        Long id = 1L;
        try { if (idStr != null) id = Long.parseLong(idStr); } catch (Exception ignored) {}

        alertService.clearAlert(id)
                .onSuccess(result -> sendJson(ctx, 200, result))
                .onFailure(error -> sendJson(ctx, 500, new JsonObject().put("success", false).put("message", error.getMessage())));
    }

    // Handles clearing all active alerts.
    private void handleClearAllAlerts(RoutingContext ctx) {
        alertService.clearAllLiveAlerts()
                .onSuccess(result -> sendJson(ctx, 200, result))
                .onFailure(error -> sendJson(ctx, 500, new JsonObject().put("success", false).put("message", error.getMessage())));
    }

    // Handles deleting an alert by ID.
    private void handleDeleteAlert(RoutingContext ctx) {
        String idStr = ctx.pathParam("id");
        Long id = 1L;
        try { if (idStr != null) id = Long.parseLong(idStr); } catch (Exception ignored) {}

        alertService.deleteAlert(id)
                .onSuccess(result -> sendJson(ctx, 200, result))
                .onFailure(error -> sendJson(ctx, 500, new JsonObject().put("success", false).put("message", error.getMessage())));
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
