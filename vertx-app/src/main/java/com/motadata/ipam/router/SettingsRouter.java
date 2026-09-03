package com.motadata.ipam.router;

import com.motadata.ipam.service.AlertService;
import com.motadata.ipam.service.DiscoveryService;
import com.motadata.ipam.service.SettingsService;
import com.motadata.ipam.service.UserService;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

/**
 * Vert.x Web router for Users, Roles, Global Settings, Brand, Mail, Custom Columns,
 * Alert Configuration, Discovery, and Database Maintenance.
 * Architecture: Handler -> Service -> PgPool -> PostgreSQL
 */
public class SettingsRouter {

    private final UserService userService;
    private final SettingsService settingsService;
    private final AlertService alertService;
    private final DiscoveryService discoveryService;

    public SettingsRouter(UserService userService, SettingsService settingsService, AlertService alertService, DiscoveryService discoveryService) {
        this.userService = userService;
        this.settingsService = settingsService;
        this.alertService = alertService;
        this.discoveryService = discoveryService;
    }

    public void attachRoutes(Router router) {
        // User & Role Management endpoints
        router.get("/user/").handler(this::handleGetUsers);
        router.get("/user/:id").handler(this::handleGetUserById);
        router.post("/user/").handler(this::handleSaveUser);
        router.put("/user/:id").handler(this::handleSaveUser);
        router.delete("/user/:id").handler(this::handleDeleteUser);

        router.get("/role/").handler(this::handleGetRoles);
        router.get("/userRole/feature/").handler(this::handleGetRoleFeatures);
        router.get("/userRole/:id").handler(this::handleGetRoleById);
        router.get("/userRole/").handler(this::handleGetRoles);
        router.get("/userRole").handler(this::handleGetRoles);
        router.post("/userRole/").handler(this::handleSaveRole);
        router.put("/userRole/").handler(this::handleSaveRole);
        router.put("/userRole/:id").handler(this::handleSaveRole);
        router.delete("/userRole/:id").handler(this::handleDeleteRole);

        // Global Settings & Branding endpoints
        router.get("/globalSetting/").handler(this::handleGetGlobalSetting);
        router.put("/globalSetting/1").handler(this::handleSaveGlobalSetting);
        router.get("/brand/").handler(this::handleGetBrand);
        router.put("/brand/1").handler(this::handleSaveBrand);

        // Mail Server Configuration endpoints
        router.get("/mail/").handler(this::handleGetMailConfig);
        router.get("/mail/:id").handler(this::handleGetMailConfigById);
        router.post("/mail/").handler(this::handleSaveMailConfig);
        router.put("/mail/:id").handler(this::handleSaveMailConfig);

        // Alert Configuration & Custom Columns endpoints
        router.get("/configureAlert/").handler(this::handleGetConfigureAlert);
        router.post("/configureAlert/").handler(this::handleSaveConfigureAlert);
        router.put("/configureAlert/").handler(this::handleSaveConfigureAlert);
        router.get("/customColumn/").handler(this::handleGetCustomColumn);
        router.post("/customColumn/").handler(this::handleSaveCustomColumn);
        router.delete("/customColumn/:id").handler(this::handleDeleteCustomColumn);

        // Discovery endpoints
        router.get("/discovery/").handler(this::handleGetDiscovery);
        router.post("/discovery/").handler(this::handleSaveDiscovery);
        router.get("/discoveryScheduler/").handler(this::handleGetDiscoveryScheduler);
        router.post("/discoveryScheduler/").handler(this::handleSaveDiscoveryScheduler);

        // Database Maintenance endpoints
        router.get("/databaseMaintenance/1").handler(this::handleGetDatabaseMaintenance);
        router.put("/databaseMaintenance/1").handler(this::handleSaveDatabaseMaintenance);
        router.delete("/databaseMaintenance/1").handler(this::handleSaveDatabaseMaintenance);
        router.put("/databaseBackup/1").handler(this::handleSaveDatabaseMaintenance);
        router.put("/runDatabaseBackup/1").handler(this::handleSaveDatabaseMaintenance);
    }

    private void handleGetUsers(RoutingContext ctx) {
        userService.getAllUsers().onComplete(ar -> {
            JsonObject result = new JsonObject().put("data", ar.result()).put("success", true);
            ctx.response().putHeader("Content-Type", "application/json;charset=UTF-8").end(result.encode());
        });
    }

    private void handleGetUserById(RoutingContext ctx) {
        String idStr = ctx.pathParam("id");
        Long id = 1L;
        try { if (idStr != null) id = Long.parseLong(idStr); } catch (Exception ignored) {}

        userService.getUserById(id).onComplete(ar -> {
            JsonObject result = new JsonObject().put("data", ar.result()).put("success", true);
            ctx.response().putHeader("Content-Type", "application/json;charset=UTF-8").end(result.encode());
        });
    }

    private void handleSaveUser(RoutingContext ctx) {
        JsonObject body = null;
        try { body = ctx.body().asJsonObject(); } catch (Exception ignored) {}

        String userName = ctx.request().getParam("userName");
        String password = ctx.request().getParam("password");
        String email = ctx.request().getParam("email");
        String roleIdStr = ctx.request().getParam("roleId");

        if (body != null) {
            if (userName == null) userName = body.getString("userName");
            if (password == null) password = body.getString("password");
            if (email == null) email = body.getString("email");
            if (roleIdStr == null && body.getValue("roleId") != null) roleIdStr = String.valueOf(body.getValue("roleId"));
        }

        Long roleId = 2L;
        try { if (roleIdStr != null) roleId = Long.parseLong(roleIdStr); } catch (Exception ignored) {}

        JsonObject userObj = new JsonObject()
                .put("userName", userName != null ? userName : "new_user")
                .put("password", password != null ? password : "admin123")
                .put("email", email != null ? email : (userName + "@motadata.com"))
                .put("roleId", roleId);

        userService.saveUser(userObj).onComplete(ar -> {
            ctx.response().putHeader("Content-Type", "application/json;charset=UTF-8").end(ar.result().encode());
        });
    }

    private void handleDeleteUser(RoutingContext ctx) {
        String idStr = ctx.pathParam("id");
        Long id = 1L;
        try { if (idStr != null) id = Long.parseLong(idStr); } catch (Exception ignored) {}

        userService.deleteUser(id).onComplete(ar -> {
            ctx.response().putHeader("Content-Type", "application/json;charset=UTF-8").end(ar.result().encode());
        });
    }

    private void handleGetRoles(RoutingContext ctx) {
        userService.getAllRoles().onComplete(ar -> {
            JsonObject result = new JsonObject().put("data", ar.result()).put("success", true);
            ctx.response().putHeader("Content-Type", "application/json;charset=UTF-8").end(result.encode());
        });
    }

    private void handleGetRoleById(RoutingContext ctx) {
        String idStr = ctx.pathParam("id");
        if (idStr == null) idStr = ctx.request().getParam("id");
        if (idStr == null) idStr = ctx.request().getParam("userId");
        Long id = 1L;
        try { if (idStr != null) id = Long.parseLong(idStr); } catch (Exception ignored) {}

        userService.getRoleById(id).onComplete(ar -> {
            JsonObject result = new JsonObject().put("data", ar.result()).put("success", true);
            ctx.response().putHeader("Content-Type", "application/json;charset=UTF-8").end(result.encode());
        });
    }

    private void handleGetRoleFeatures(RoutingContext ctx) {
        userService.getRoleFeatures().onComplete(ar -> {
            JsonObject result = new JsonObject().put("data", ar.result()).put("success", true);
            ctx.response().putHeader("Content-Type", "application/json;charset=UTF-8").end(result.encode());
        });
    }

    private void handleSaveRole(RoutingContext ctx) {
        JsonObject body = null;
        try { body = ctx.body().asJsonObject(); } catch (Exception ignored) {}

        String idStr = ctx.pathParam("id");
        if (idStr == null) idStr = ctx.request().getParam("id");
        String role = ctx.request().getParam("role");
        if (role == null) role = ctx.request().getParam("roleName");
        String desc = ctx.request().getParam("description");

        if (body != null) {
            if (idStr == null && body.getValue("id") != null) idStr = String.valueOf(body.getValue("id"));
            if (role == null) role = body.getString("role", body.getString("roleName"));
            if (desc == null) desc = body.getString("description");
        }

        JsonObject roleObj = new JsonObject()
                .put("role", role != null ? role : "ROLE_CUSTOM")
                .put("description", desc != null ? desc : "Custom Role Description");
        if (idStr != null) {
            try { roleObj.put("id", Long.parseLong(idStr)); } catch (Exception ignored) {}
        }

        userService.saveRole(roleObj).onComplete(ar -> {
            ctx.response().putHeader("Content-Type", "application/json;charset=UTF-8").end(ar.result().encode());
        });
    }

    private void handleDeleteRole(RoutingContext ctx) {
        String idStr = ctx.pathParam("id");
        Long id = 1L;
        try { if (idStr != null) id = Long.parseLong(idStr); } catch (Exception ignored) {}

        userService.deleteRole(id).onComplete(ar -> {
            ctx.response().putHeader("Content-Type", "application/json;charset=UTF-8").end(ar.result().encode());
        });
    }

    private void handleGetGlobalSetting(RoutingContext ctx) {
        settingsService.getGlobalSetting().onComplete(ar -> {
            JsonObject result = new JsonObject().put("data", ar.result()).put("success", true);
            ctx.response().putHeader("Content-Type", "application/json;charset=UTF-8").end(result.encode());
        });
    }

    private void handleSaveGlobalSetting(RoutingContext ctx) {
        JsonObject body = null;
        try { body = ctx.body().asJsonObject(); } catch (Exception ignored) {}
        if (body == null) body = new JsonObject();

        settingsService.saveGlobalSetting(body).onComplete(ar -> {
            ctx.response().putHeader("Content-Type", "application/json;charset=UTF-8").end(ar.result().encode());
        });
    }

    private void handleGetBrand(RoutingContext ctx) {
        settingsService.getBrand().onComplete(ar -> {
            JsonObject result = new JsonObject().put("data", ar.result()).put("success", true);
            ctx.response().putHeader("Content-Type", "application/json;charset=UTF-8").end(result.encode());
        });
    }

    private void handleSaveBrand(RoutingContext ctx) {
        JsonObject body = null;
        try { body = ctx.body().asJsonObject(); } catch (Exception ignored) {}
        if (body == null) body = new JsonObject();

        settingsService.saveBrand(body).onComplete(ar -> {
            ctx.response().putHeader("Content-Type", "application/json;charset=UTF-8").end(ar.result().encode());
        });
    }

    private void handleGetMailConfig(RoutingContext ctx) {
        settingsService.getMailConfig().onComplete(ar -> {
            JsonObject result = new JsonObject().put("data", ar.result()).put("success", true);
            ctx.response().putHeader("Content-Type", "application/json;charset=UTF-8").end(result.encode());
        });
    }

    private void handleGetMailConfigById(RoutingContext ctx) {
        String idStr = ctx.pathParam("id");
        Long id = 1L;
        try { if (idStr != null) id = Long.parseLong(idStr); } catch (Exception ignored) {}

        settingsService.getMailConfigById(id).onComplete(ar -> {
            JsonObject result = new JsonObject().put("data", ar.result()).put("success", true);
            ctx.response().putHeader("Content-Type", "application/json;charset=UTF-8").end(result.encode());
        });
    }

    private void handleSaveMailConfig(RoutingContext ctx) {
        JsonObject body = null;
        try { body = ctx.body().asJsonObject(); } catch (Exception ignored) {}
        if (body == null) body = new JsonObject();

        settingsService.saveMailConfig(body).onComplete(ar -> {
            ctx.response().putHeader("Content-Type", "application/json;charset=UTF-8").end(ar.result().encode());
        });
    }

    private void handleGetConfigureAlert(RoutingContext ctx) {
        alertService.getAlertConfig().onComplete(ar -> {
            ctx.response().putHeader("Content-Type", "application/json;charset=UTF-8").end(ar.result().encode());
        });
    }

    private void handleSaveConfigureAlert(RoutingContext ctx) {
        JsonObject alertMap = new JsonObject();
        try {
            JsonArray bodyArray = ctx.body().asJsonArray();
            if (bodyArray != null) {
                for (int i = 0; i < bodyArray.size(); i++) {
                    JsonObject item = bodyArray.getJsonObject(i);
                    if (item != null && item.getString("alertKey") != null) {
                        alertMap.put(item.getString("alertKey"), item.getString("alertValue"));
                    }
                }
            }
        } catch (Exception ignored) {
            try {
                JsonObject bodyObj = ctx.body().asJsonObject();
                if (bodyObj != null) {
                    alertMap.mergeIn(bodyObj);
                }
            } catch (Exception ignored2) {}
        }

        alertService.saveAlertConfig(alertMap).onComplete(ar -> {
            ctx.response().putHeader("Content-Type", "application/json;charset=UTF-8").end(ar.result().encode());
        });
    }

    private void handleGetCustomColumn(RoutingContext ctx) {
        settingsService.getCustomColumns().onComplete(ar -> {
            JsonObject result = new JsonObject().put("data", ar.result()).put("success", true);
            ctx.response().putHeader("Content-Type", "application/json;charset=UTF-8").end(result.encode());
        });
    }

    private void handleSaveCustomColumn(RoutingContext ctx) {
        JsonObject body = null;
        try { body = ctx.body().asJsonObject(); } catch (Exception ignored) {}
        if (body == null) body = new JsonObject();

        settingsService.saveCustomColumn(body).onComplete(ar -> {
            ctx.response().putHeader("Content-Type", "application/json;charset=UTF-8").end(ar.result().encode());
        });
    }

    private void handleDeleteCustomColumn(RoutingContext ctx) {
        String idStr = ctx.pathParam("id");
        Long id = 1L;
        try { if (idStr != null) id = Long.parseLong(idStr); } catch (Exception ignored) {}

        settingsService.deleteCustomColumn(id).onComplete(ar -> {
            ctx.response().putHeader("Content-Type", "application/json;charset=UTF-8").end(ar.result().encode());
        });
    }

    private void handleGetDiscovery(RoutingContext ctx) {
        discoveryService.getDiscoveryProfiles().onComplete(ar -> {
            JsonObject result = new JsonObject().put("data", ar.result()).put("success", true);
            ctx.response().putHeader("Content-Type", "application/json;charset=UTF-8").end(result.encode());
        });
    }

    private void handleSaveDiscovery(RoutingContext ctx) {
        discoveryService.saveDiscoveryProfile(new JsonObject()).onComplete(ar -> {
            ctx.response().putHeader("Content-Type", "application/json;charset=UTF-8").end(ar.result().encode());
        });
    }

    private void handleGetDiscoveryScheduler(RoutingContext ctx) {
        discoveryService.getDiscoveryProfiles().onComplete(ar -> {
            JsonObject result = new JsonObject().put("data", ar.result()).put("success", true);
            ctx.response().putHeader("Content-Type", "application/json;charset=UTF-8").end(result.encode());
        });
    }

    private void handleSaveDiscoveryScheduler(RoutingContext ctx) {
        discoveryService.saveDiscoveryProfile(new JsonObject()).onComplete(ar -> {
            ctx.response().putHeader("Content-Type", "application/json;charset=UTF-8").end(ar.result().encode());
        });
    }

    private void handleGetDatabaseMaintenance(RoutingContext ctx) {
        settingsService.getDatabaseMaintenance().onComplete(ar -> {
            JsonObject result = new JsonObject().put("data", ar.result()).put("success", true);
            ctx.response().putHeader("Content-Type", "application/json;charset=UTF-8").end(result.encode());
        });
    }

    private void handleSaveDatabaseMaintenance(RoutingContext ctx) {
        settingsService.saveDatabaseMaintenance(new JsonObject()).onComplete(ar -> {
            ctx.response().putHeader("Content-Type", "application/json;charset=UTF-8").end(ar.result().encode());
        });
    }
}
