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

    public DhcpRouter(DhcpService dhcpService) {
        this.dhcpService = dhcpService;
    }

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

    private void handleGetDhcpCredentials(RoutingContext ctx) {
        dhcpService.getCredentials().onComplete(ar -> {
            JsonObject result = new JsonObject().put("data", ar.result()).put("success", true);
            ctx.response().putHeader("Content-Type", "application/json;charset=UTF-8").end(result.encode());
        });
    }

    private void handleGetDhcpCredentialById(RoutingContext ctx) {
        String idStr = ctx.pathParam("id");
        Long id = 1L;
        try { if (idStr != null) id = Long.parseLong(idStr); } catch (Exception ignored) {}

        dhcpService.getCredentialById(id).onComplete(ar -> {
            JsonObject result = new JsonObject().put("data", ar.result()).put("success", true);
            ctx.response().putHeader("Content-Type", "application/json;charset=UTF-8").end(result.encode());
        });
    }

    private void handleSaveDhcpCredential(RoutingContext ctx) {
        JsonObject body = null;
        try { body = ctx.body().asJsonObject(); } catch (Exception ignored) {}
        if (body == null) body = new JsonObject();

        dhcpService.saveCredential(body).onComplete(ar -> {
            ctx.response().putHeader("Content-Type", "application/json;charset=UTF-8").end(ar.result().encode());
        });
    }

    private void handleDeleteDhcpCredential(RoutingContext ctx) {
        String idStr = ctx.pathParam("id");
        Long id = 1L;
        try { if (idStr != null) id = Long.parseLong(idStr); } catch (Exception ignored) {}

        dhcpService.deleteCredential(id).onComplete(ar -> {
            ctx.response().putHeader("Content-Type", "application/json;charset=UTF-8").end(ar.result().encode());
        });
    }

    private void handleGetWindowsDhcpCredentials(RoutingContext ctx) {
        dhcpService.getWindowsCredentials().onComplete(ar -> {
            JsonObject result = new JsonObject().put("data", ar.result()).put("success", true);
            ctx.response().putHeader("Content-Type", "application/json;charset=UTF-8").end(result.encode());
        });
    }

    private void handleGetCiscoDhcpCredentials(RoutingContext ctx) {
        dhcpService.getCiscoCredentials().onComplete(ar -> {
            JsonObject result = new JsonObject().put("data", ar.result()).put("success", true);
            ctx.response().putHeader("Content-Type", "application/json;charset=UTF-8").end(result.encode());
        });
    }

    private void handleCheckDhcpCredential(RoutingContext ctx) {
        dhcpService.checkCredential(new JsonObject()).onComplete(ar -> {
            ctx.response().putHeader("Content-Type", "application/json;charset=UTF-8").end(ar.result().encode());
        });
    }

    private void handleGetDhcpUtilization(RoutingContext ctx) {
        dhcpService.getDhcpUtilization().onComplete(ar -> {
            JsonObject result = new JsonObject().put("data", ar.result()).put("success", true);
            ctx.response().putHeader("Content-Type", "application/json;charset=UTF-8").end(result.encode());
        });
    }

    private void handleGetDhcpUtilizationById(RoutingContext ctx) {
        String idStr = ctx.pathParam("id");
        Long id = 1L;
        try { if (idStr != null) id = Long.parseLong(idStr); } catch (Exception ignored) {}

        dhcpService.getDhcpUtilizationById(id).onComplete(ar -> {
            JsonObject result = new JsonObject().put("data", ar.result()).put("success", true);
            ctx.response().putHeader("Content-Type", "application/json;charset=UTF-8").end(result.encode());
        });
    }

    private void handleScanDhcp(RoutingContext ctx) {
        String idStr = ctx.pathParam("id");
        Long id = 1L;
        try { if (idStr != null) id = Long.parseLong(idStr); } catch (Exception ignored) {}

        dhcpService.scanDhcp(id).onComplete(ar -> {
            ctx.response().putHeader("Content-Type", "application/json;charset=UTF-8").end(ar.result().encode());
        });
    }
}
