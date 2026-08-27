package com.motadata.ipam.router;

import com.motadata.ipam.service.SubnetService;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

/**
 * Vert.x Web router for Subnet, Category, Supernet, and Dashboard Statistics REST API endpoints.
 */
public class SubnetRouter {

    private final SubnetService subnetService;

    public SubnetRouter(SubnetService subnetService) {
        this.subnetService = subnetService;
    }

    public void attachRoutes(Router router) {
        router.get("/validatePermission/").handler(this::handleValidatePermission);
        router.get("/subnet/").handler(this::handleGetAllSubnets);
        router.get("/subnetIp/").handler(this::handleGetIpDetails);
        router.get("/ipSummary/").handler(this::handleGetIpSummary);
        router.get("/pingIpSummary/").handler(this::handleGetPingIpSummary);
        router.get("/rogueSubnetIp/").handler(this::handleGetRogueSubnetIp);
        router.get("/dnsStatusSummary/").handler(this::handleGetDnsStatusSummary);
        router.get("/vendor/").handler(this::handleGetVendorSummary);
        router.get("/top10Subnet/").handler(this::handleGetTop10Subnet);
        router.get("/top10Category/").handler(this::handleGetTop10Category);
        router.get("/recentDiscovery/").handler(this::handleGetRecentDiscovery);
        router.get("/conflictedIp/").handler(this::handleGetConflictedIp);
        router.get("/category/").handler(this::handleGetCategories);
        router.get("/gateway/").handler(this::handleGetGateways);
        router.get("/supernet/").handler(this::handleGetSupernets);
    }

    private void handleValidatePermission(RoutingContext ctx) {
        JsonObject result = new JsonObject()
                .put("success", true)
                .put("message", "Permission granted");
        ctx.response()
                .putHeader("Content-Type", "application/json;charset=UTF-8")
                .end(result.encode());
    }

    private void handleGetAllSubnets(RoutingContext ctx) {
        subnetService.getAllSubnets().onComplete(ar -> {
            if (ar.succeeded()) {
                JsonObject result = new JsonObject()
                        .put("data", new JsonArray(ar.result()))
                        .put("success", true);

                ctx.response()
                        .putHeader("Content-Type", "application/json;charset=UTF-8")
                        .end(result.encode());
            } else {
                ctx.response()
                        .setStatusCode(500)
                        .putHeader("Content-Type", "application/json;charset=UTF-8")
                        .end(new JsonObject().put("success", false).put("message", ar.cause().getMessage()).encode());
            }
        });
    }

    private void handleGetIpDetails(RoutingContext ctx) {
        String subnetIdStr = ctx.request().getParam("subnetId");
        String pageStr = ctx.request().getParam("page");
        String pageSizeStr = ctx.request().getParam("pageSize");

        Long subnetId = (subnetIdStr != null) ? Long.parseLong(subnetIdStr) : 1L;
        Integer page = (pageStr != null) ? Integer.parseInt(pageStr) : 1;
        Integer pageSize = (pageSizeStr != null) ? Integer.parseInt(pageSizeStr) : 20;

        subnetService.getIpDetails(subnetId, page, pageSize).onComplete(ar -> {
            if (ar.succeeded()) {
                JsonObject result = new JsonObject()
                        .put("data", new JsonArray(ar.result()))
                        .put("success", true);

                ctx.response()
                        .putHeader("Content-Type", "application/json;charset=UTF-8")
                        .end(result.encode());
            } else {
                ctx.response()
                        .setStatusCode(500)
                        .putHeader("Content-Type", "application/json;charset=UTF-8")
                        .end(new JsonObject().put("success", false).put("message", ar.cause().getMessage()).encode());
            }
        });
    }

    private void handleGetIpSummary(RoutingContext ctx) {
        JsonObject data = new JsonObject()
                .put("used", 45)
                .put("available", 209)
                .put("transient", 5);

        JsonObject result = new JsonObject()
                .put("data", data)
                .put("success", true);

        ctx.response()
                .putHeader("Content-Type", "application/json;charset=UTF-8")
                .end(result.encode());
    }

    private void handleGetPingIpSummary(RoutingContext ctx) {
        JsonObject data = new JsonObject()
                .put("total", 259)
                .put("failure", 12);

        JsonObject result = new JsonObject()
                .put("data", data)
                .put("success", true);

        ctx.response()
                .putHeader("Content-Type", "application/json;charset=UTF-8")
                .end(result.encode());
    }

    private void handleGetRogueSubnetIp(RoutingContext ctx) {
        JsonObject data = new JsonObject()
                .put("discover", 8)
                .put("rogue", 2)
                .put("trusted", 249);

        JsonObject result = new JsonObject()
                .put("data", data)
                .put("success", true);

        ctx.response()
                .putHeader("Content-Type", "application/json;charset=UTF-8")
                .end(result.encode());
    }

    private void handleGetDnsStatusSummary(RoutingContext ctx) {
        JsonArray data = new JsonArray()
                .add(new JsonObject().put("category", "Forward & Reverse OK").put("value", 85))
                .add(new JsonObject().put("category", "Forward Only").put("value", 10))
                .add(new JsonObject().put("category", "Failed DNS").put("value", 5));

        JsonObject result = new JsonObject()
                .put("data", data)
                .put("success", true);

        ctx.response()
                .putHeader("Content-Type", "application/json;charset=UTF-8")
                .end(result.encode());
    }

    private void handleGetVendorSummary(RoutingContext ctx) {
        JsonArray data = new JsonArray()
                .add(new JsonObject().put("vendor", "Cisco Systems").put("count", 120))
                .add(new JsonObject().put("vendor", "VMware Inc").put("count", 45))
                .add(new JsonObject().put("vendor", "Intel Corp").put("count", 30))
                .add(new JsonObject().put("vendor", "Dell Inc").put("count", 25));

        JsonObject result = new JsonObject()
                .put("data", data)
                .put("success", true);

        ctx.response()
                .putHeader("Content-Type", "application/json;charset=UTF-8")
                .end(result.encode());
    }

    private void handleGetTop10Subnet(RoutingContext ctx) {
        JsonArray data = new JsonArray()
                .add(new JsonObject().put("subnetAddress", "192.168.1.0/24").put("utilization", 78.5))
                .add(new JsonObject().put("subnetAddress", "10.0.0.0/16").put("utilization", 45.2));

        JsonObject result = new JsonObject()
                .put("data", data)
                .put("success", true);

        ctx.response()
                .putHeader("Content-Type", "application/json;charset=UTF-8")
                .end(result.encode());
    }

    private void handleGetTop10Category(RoutingContext ctx) {
        JsonArray data = new JsonArray()
                .add(new JsonObject().put("categoryName", "Default").put("utilization", 65.0))
                .add(new JsonObject().put("categoryName", "Servers").put("utilization", 42.0));

        JsonObject result = new JsonObject()
                .put("data", data)
                .put("success", true);

        ctx.response()
                .putHeader("Content-Type", "application/json;charset=UTF-8")
                .end(result.encode());
    }

    private void handleGetRecentDiscovery(RoutingContext ctx) {
        JsonArray data = new JsonArray()
                .add(new JsonObject().put("macAddress", "00:50:56:A1:B2:C3").put("ipAddress", "192.168.1.50").put("discoveredTime", "2026-08-27 10:00:00"))
                .add(new JsonObject().put("macAddress", "00:50:56:D4:E5:F6").put("ipAddress", "192.168.1.51").put("discoveredTime", "2026-08-27 10:05:00"));

        JsonObject result = new JsonObject()
                .put("data", data)
                .put("success", true);

        ctx.response()
                .putHeader("Content-Type", "application/json;charset=UTF-8")
                .end(result.encode());
    }

    private void handleGetConflictedIp(RoutingContext ctx) {
        JsonArray data = new JsonArray();

        JsonObject result = new JsonObject()
                .put("data", data)
                .put("success", true);

        ctx.response()
                .putHeader("Content-Type", "application/json;charset=UTF-8")
                .end(result.encode());
    }

    private void handleGetCategories(RoutingContext ctx) {
        JsonArray data = new JsonArray()
                .add(new JsonObject().put("id", 1).put("categoryName", "Default Category"))
                .add(new JsonObject().put("id", 2).put("categoryName", "Production Subnets"));

        JsonObject result = new JsonObject()
                .put("data", data)
                .put("success", true);

        ctx.response()
                .putHeader("Content-Type", "application/json;charset=UTF-8")
                .end(result.encode());
    }

    private void handleGetGateways(RoutingContext ctx) {
        JsonArray data = new JsonArray()
                .add(new JsonObject().put("id", 1).put("gateway", "192.168.1.1"));

        JsonObject result = new JsonObject()
                .put("data", data)
                .put("success", true);

        ctx.response()
                .putHeader("Content-Type", "application/json;charset=UTF-8")
                .end(result.encode());
    }

    private void handleGetSupernets(RoutingContext ctx) {
        JsonArray data = new JsonArray();

        JsonObject result = new JsonObject()
                .put("data", data)
                .put("success", true);

        ctx.response()
                .putHeader("Content-Type", "application/json;charset=UTF-8")
                .end(result.encode());
    }
}
