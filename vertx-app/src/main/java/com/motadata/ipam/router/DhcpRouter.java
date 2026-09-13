package com.motadata.ipam.router;

import com.motadata.ipam.service.DhcpService;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

/**
 * Vert.x Web router for DHCP Server management & Scope Utilization REST API endpoints.
 * Architecture: Handler -> Service -> PgPool -> PostgreSQL
 */
public class DhcpRouter {

    private final DhcpService dhcpService;

    // Constructs DhcpRouter with the provided DhcpService instance.
    public DhcpRouter(DhcpService dhcpService) {
        this.dhcpService = dhcpService;
    }

    // Registers DHCP credential, utilization, and scan routes
    public void attachRoutes(Router router) {
        router.get("/dhcpCredential/").handler(this::handleGetDhcpCredentials);
        router.get("/dhcpCredential/:id").handler(this::handleGetDhcpCredentialById);
        router.post("/dhcpCredential/").handler(this::handleSaveDhcpCredential);
        router.put("/dhcpCredential/:id").handler(this::handleSaveDhcpCredential);
        router.delete("/dhcpCredential/:id").handler(this::handleDeleteDhcpCredential);

        router.get("/windowsDhcpCredential/").handler(this::handleGetWindowsDhcpCredentials);
        router.get("/ciscoDhcpCredential/").handler(this::handleGetCiscoDhcpCredentials);
        router.post("/checkDhcpCredential/").handler(this::handleCheckDhcpCredential);

        router.get("/dhcp/").handler(this::handleGetDhcpUtilization);
        router.get("/dhcpSubnet/").handler(this::handleGetDhcpUtilization);
        router.get("/dhcpUtilization/:id").handler(this::handleGetDhcpUtilizationById);
        router.get("/scanDhcp/:id").handler(this::handleScanDhcp);
    }

    // Retrieves all DHCP credentials.
    private void handleGetDhcpCredentials(RoutingContext ctx) {
        dhcpService.getCredentials().onComplete(ar -> {
            JsonObject result = new JsonObject().put("data", ar.result()).put("success", true);
            ctx.response().putHeader("Content-Type", "application/json;charset=UTF-8").end(result.encode());
        });
    }

    // Retrieves a DHCP credential by ID.
    private void handleGetDhcpCredentialById(RoutingContext ctx) {
        String idStr = ctx.pathParam("id");
        Long id = 1L;
        try { if (idStr != null) id = Long.parseLong(idStr); } catch (Exception ignored) {}

        dhcpService.getCredentialById(id).onComplete(ar -> {
            JsonObject result = new JsonObject().put("data", ar.result()).put("success", true);
            ctx.response().putHeader("Content-Type", "application/json;charset=UTF-8").end(result.encode());
        });
    }

    // Saves or updates a DHCP credential.
    private void handleSaveDhcpCredential(RoutingContext ctx) {
        JsonObject body = null;
        try { body = ctx.body().asJsonObject(); } catch (Exception ignored) {}
        if (body == null) body = new JsonObject();

        dhcpService.saveCredential(body).onComplete(ar -> {
            ctx.response().putHeader("Content-Type", "application/json;charset=UTF-8").end(ar.result().encode());
        });
    }

    // Deletes a DHCP credential by ID.
    private void handleDeleteDhcpCredential(RoutingContext ctx) {
        String idStr = ctx.pathParam("id");
        Long id = 1L;
        try { if (idStr != null) id = Long.parseLong(idStr); } catch (Exception ignored) {}

        dhcpService.deleteCredential(id).onComplete(ar -> {
            ctx.response().putHeader("Content-Type", "application/json;charset=UTF-8").end(ar.result().encode());
        });
    }

    // Retrieves Windows DHCP credentials.
    private void handleGetWindowsDhcpCredentials(RoutingContext ctx) {
        dhcpService.getWindowsCredentials().onComplete(ar -> {
            JsonObject result = new JsonObject().put("data", ar.result()).put("success", true);
            ctx.response().putHeader("Content-Type", "application/json;charset=UTF-8").end(result.encode());
        });
    }

    // Retrieves Cisco DHCP credentials.
    private void handleGetCiscoDhcpCredentials(RoutingContext ctx) {
        dhcpService.getCiscoCredentials().onComplete(ar -> {
            JsonObject result = new JsonObject().put("data", ar.result()).put("success", true);
            ctx.response().putHeader("Content-Type", "application/json;charset=UTF-8").end(result.encode());
        });
    }

    // Validates the DHCP credentials
    private void handleCheckDhcpCredential(RoutingContext ctx) {
        dhcpService.checkCredential(new JsonObject()).onComplete(ar -> {
            ctx.response().putHeader("Content-Type", "application/json;charset=UTF-8").end(ar.result().encode());
        });
    }

    // Retrieves DHCP utilization information
    private void handleGetDhcpUtilization(RoutingContext ctx) {
        dhcpService.getDhcpUtilization().onComplete(ar -> {
            JsonObject result = new JsonObject().put("data", ar.result()).put("success", true);
            ctx.response().putHeader("Content-Type", "application/json;charset=UTF-8").end(result.encode());
        });
    }

    // Retrieves DHCP utilization by ID
    private void handleGetDhcpUtilizationById(RoutingContext ctx) {
        String idStr = ctx.pathParam("id");
        Long id = 1L;
        try { if (idStr != null) id = Long.parseLong(idStr); } catch (Exception ignored) {}

        dhcpService.getDhcpUtilizationById(id).onComplete(ar -> {
            if (ar.succeeded()) {
                JsonObject result = new JsonObject().put("data", ar.result()).put("success", true);
                ctx.response().putHeader("Content-Type", "application/json;charset=UTF-8").end(result.encode());
            } else {
                sendError(ctx, 500, ar.cause() != null ? ar.cause().getMessage() : "Unable to load DHCP utilization");
            }
        });
    }

    // Starts a DHCP scan for the specified ID.
    private void handleScanDhcp(RoutingContext ctx) {
        String idStr = ctx.pathParam("id");
        final Long id;
        try {
            id = Long.parseLong(idStr);
        } catch (Exception e) {
            sendError(ctx, 400, "A valid DHCP credential id is required");
            return;
        }

        dhcpService.scanDhcp(id).onComplete(ar -> {
            if (ar.succeeded()) {
                ctx.response().putHeader("Content-Type", "application/json;charset=UTF-8").end(ar.result().encode());
            } else {
                sendError(ctx, 502, ar.cause() != null ? ar.cause().getMessage() : "DHCP scan failed");
            }
        });
    }

    // Sends a formatted JSON error response with the specified status code.
    private void sendError(RoutingContext ctx, int statusCode, String message) {
        ctx.response().setStatusCode(statusCode)
                .putHeader("Content-Type", "application/json;charset=UTF-8")
                .end(new JsonObject().put("success", false).put("message", message).encode());
    }
}
