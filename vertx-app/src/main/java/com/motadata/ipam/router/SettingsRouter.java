package com.motadata.ipam.router;

import com.motadata.ipam.security.PermissionHandler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

/**
 * Vert.x Web router for Global Settings, Re-branding, and User Management REST API endpoints.
 */
public class SettingsRouter {

    public void attachRoutes(Router router) {
        router.get("/user/").handler(PermissionHandler.require("PERM_SETTINGS_READ")).handler(this::handleGetUsers);
        router.get("/role/").handler(PermissionHandler.require("PERM_SETTINGS_READ")).handler(this::handleGetRoles);
        router.get("/globalSetting/").handler(PermissionHandler.require("PERM_SETTINGS_READ")).handler(this::handleGetGlobalSetting);
        router.get("/brand/").handler(this::handleGetBrand);
    }

    private void handleGetUsers(RoutingContext ctx) {
        JsonObject result = new JsonObject()
                .put("data", new JsonArray())
                .put("success", true);

        ctx.response().putHeader("Content-Type", "application/json;charset=UTF-8").end(result.encode());
    }

    private void handleGetRoles(RoutingContext ctx) {
        JsonObject result = new JsonObject()
                .put("data", new JsonArray())
                .put("success", true);

        ctx.response().putHeader("Content-Type", "application/json;charset=UTF-8").end(result.encode());
    }

    private void handleGetGlobalSetting(RoutingContext ctx) {
        JsonObject data = new JsonObject()
                .put("id", 1)
                .put("loggingLevel", 1)
                .put("cssMode", 1);

        JsonObject result = new JsonObject()
                .put("data", data)
                .put("success", true);

        ctx.response().putHeader("Content-Type", "application/json;charset=UTF-8").end(result.encode());
    }

    private void handleGetBrand(RoutingContext ctx) {
        JsonObject data = new JsonObject()
                .put("id", 1)
                .put("productName", "IP Address Manager")
                .put("productImg", "/images/logo.png");

        JsonObject result = new JsonObject()
                .put("data", data)
                .put("success", true);

        ctx.response().putHeader("Content-Type", "application/json;charset=UTF-8").end(result.encode());
    }
}
