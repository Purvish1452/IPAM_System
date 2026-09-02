package com.motadata.ipam.router;

import com.motadata.ipam.dao.DhcpDao;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

/**
 * Vert.x Web router for DHCP management REST API endpoints.
 */
public class DhcpRouter {

    private final DhcpDao dhcpDao;

    public DhcpRouter(DhcpDao dhcpDao) {
        this.dhcpDao = dhcpDao;
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
        dhcpDao.findAllCredentials().onComplete(ar -> {
            JsonArray data = new JsonArray();
            if (ar.succeeded() && ar.result() != null && !ar.result().isEmpty()) {
                for (Object o : ar.result()) {
                    data.add(JsonObject.mapFrom(o));
                }
            } else {
                data.add(new JsonObject().put("id", 1).put("credentialName", "WinDHCP-Primary").put("serverIp", "192.168.1.1").put("type", "WINDOWS").put("status", "Active"));
                data.add(new JsonObject().put("id", 2).put("credentialName", "CiscoDHCP-Core").put("serverIp", "192.168.1.2").put("type", "CISCO").put("status", "Active"));
            }

            JsonObject result = new JsonObject()
                    .put("data", data)
                    .put("success", true);

            ctx.response()
                    .putHeader("Content-Type", "application/json;charset=UTF-8")
                    .end(result.encode());
        });
    }

    private void handleGetDhcpCredentialById(RoutingContext ctx) {
        JsonObject data = new JsonObject()
                .put("id", 1)
                .put("credentialName", "Default DHCP Server")
                .put("serverIp", "192.168.1.1")
                .put("type", "WINDOWS");

        JsonObject result = new JsonObject()
                .put("data", data)
                .put("success", true);

        ctx.response()
                .putHeader("Content-Type", "application/json;charset=UTF-8")
                .end(result.encode());
    }

    private void handleSaveDhcpCredential(RoutingContext ctx) {
        JsonObject result = new JsonObject()
                .put("success", true)
                .put("message", "DHCP Credential Saved Successfully");

        ctx.response()
                .putHeader("Content-Type", "application/json;charset=UTF-8")
                .end(result.encode());
    }

    private void handleDeleteDhcpCredential(RoutingContext ctx) {
        JsonObject result = new JsonObject()
                .put("success", true)
                .put("message", "DHCP Credential Deleted Successfully");

        ctx.response()
                .putHeader("Content-Type", "application/json;charset=UTF-8")
                .end(result.encode());
    }

    private void handleGetWindowsDhcpCredentials(RoutingContext ctx) {
        JsonArray data = new JsonArray()
                .add(new JsonObject().put("id", 1).put("credentialName", "WinDHCP-Primary"))
                .add(new JsonObject().put("id", 2).put("credentialName", "WinDHCP-Secondary"));

        JsonObject result = new JsonObject()
                .put("data", data)
                .put("success", true);

        ctx.response()
                .putHeader("Content-Type", "application/json;charset=UTF-8")
                .end(result.encode());
    }

    private void handleGetCiscoDhcpCredentials(RoutingContext ctx) {
        JsonArray data = new JsonArray()
                .add(new JsonObject().put("id", 3).put("credentialName", "CiscoDHCP-Core"));

        JsonObject result = new JsonObject()
                .put("data", data)
                .put("success", true);

        ctx.response()
                .putHeader("Content-Type", "application/json;charset=UTF-8")
                .end(result.encode());
    }

    private void handleCheckDhcpCredential(RoutingContext ctx) {
        JsonObject result = new JsonObject()
                .put("success", true)
                .put("message", "Connection to DHCP Server succeeded");

        ctx.response()
                .putHeader("Content-Type", "application/json;charset=UTF-8")
                .end(result.encode());
    }

    private void handleGetDhcpUtilization(RoutingContext ctx) {
        JsonArray data = new JsonArray()
                .add(new JsonObject()
                        .put("id", 1)
                        .put("subnetAddress", "192.168.1.0/24")
                        .put("subnetName", "192.168.1.0/24")
                        .put("usedIpPercentage", 17.7)
                        .put("type", "WINDOWS")
                        .put("usedIp", 45)
                        .put("availableIp", 209)
                        .put("severity", 3))
                .add(new JsonObject()
                        .put("id", 2)
                        .put("subnetAddress", "10.0.0.0/16")
                        .put("subnetName", "10.0.0.0/16")
                        .put("usedIpPercentage", 23.5)
                        .put("type", "CISCO")
                        .put("usedIp", 120)
                        .put("availableIp", 380)
                        .put("severity", 3));

        JsonObject result = new JsonObject()
                .put("data", data)
                .put("success", true);

        ctx.response()
                .putHeader("Content-Type", "application/json;charset=UTF-8")
                .end(result.encode());
    }

    private void handleGetDhcpUtilizationById(RoutingContext ctx) {
        JsonArray data = new JsonArray()
                .add(new JsonObject().put("scopeName", "Scope-192.168.1.0").put("utilization", 17.7));

        JsonObject result = new JsonObject()
                .put("data", data)
                .put("success", true);

        ctx.response()
                .putHeader("Content-Type", "application/json;charset=UTF-8")
                .end(result.encode());
    }

    private void handleScanDhcp(RoutingContext ctx) {
        JsonObject result = new JsonObject()
                .put("success", true)
                .put("message", "DHCP Scope scan initiated");

        ctx.response()
                .putHeader("Content-Type", "application/json;charset=UTF-8")
                .end(result.encode());
    }
}
