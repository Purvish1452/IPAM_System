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
        router.get("/dhcp/").handler(this::handleGetDhcpUtilization);
    }

    private void handleGetDhcpCredentials(RoutingContext ctx) {
        dhcpDao.findAllCredentials().onComplete(ar -> {
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

    private void handleGetDhcpUtilization(RoutingContext ctx) {
        JsonArray data = new JsonArray()
                .add(new JsonObject().put("scopeName", "Scope-192.168.1.0").put("utilization", 17.7))
                .add(new JsonObject().put("scopeName", "Scope-10.0.0.0").put("utilization", 23.5));

        JsonObject result = new JsonObject()
                .put("data", data)
                .put("success", true);

        ctx.response()
                .putHeader("Content-Type", "application/json;charset=UTF-8")
                .end(result.encode());
    }
}
