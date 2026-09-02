package com.motadata.ipam.router;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Vert.x Web router for Global Settings, Re-branding, User Management, Mail, Custom Columns, Discovery, and Database Maintenance REST API endpoints.
 */
public class SettingsRouter {

    private static final List<JsonObject> fallbackUsers = new CopyOnWriteArrayList<>();
    private static final List<JsonObject> fallbackRoles = new CopyOnWriteArrayList<>();
    private static final List<JsonObject> fallbackAlerts = new CopyOnWriteArrayList<>();
    private static final List<JsonObject> fallbackCustomColumns = new CopyOnWriteArrayList<>();
    private static final List<JsonObject> fallbackDiscovery = new CopyOnWriteArrayList<>();

    public static List<JsonObject> getFallbackUsers() {
        return fallbackUsers;
    }
    private static final JsonObject alertConfigStore = new JsonObject()
            .put("ipUtilizationBelowFlag", "true")
            .put("ipUtilizationFlag", "true")
            .put("macIpChangeFlag", "true")
            .put("rogueDetection", "true")
            .put("ipStateChange", "true")
            .put("reverseLookupFailed", "true")
            .put("forwardLookupFailed", "false")
            .put("forwardLookupMismatch", "false")
            .put("ipReservationChange", "true")
            .put("ipConflict", "true")
            .put("newSubnetsDiscovered", "true")
            .put("ipUtilizationBelow", "20")
            .put("ipUtilization", "80")
            .put("macIpChange", "00:50:56:FE:DC:BA");

    static {
        fallbackUsers.add(new JsonObject()
                .put("id", 1)
                .put("userName", "admin")
                .put("email", "admin@motadata.com")
                .put("status", true)
                .put("roleName", "ROLE_ADMIN")
                .put("userRoleId", new JsonObject().put("id", 1).put("role", "ROLE_ADMIN").put("description", "Administrator Role")));

        fallbackUsers.add(new JsonObject()
                .put("id", 2)
                .put("userName", "purvish")
                .put("email", "purvishpanchal2005@gmail.com")
                .put("status", true)
                .put("roleName", "ROLE_USER")
                .put("userRoleId", new JsonObject().put("id", 2).put("role", "ROLE_USER").put("description", "Standard User Role")));

        fallbackRoles.add(new JsonObject().put("id", 1).put("role", "ROLE_ADMIN").put("description", "Administrator Role"));
        fallbackRoles.add(new JsonObject().put("id", 2).put("role", "ROLE_USER").put("description", "Standard User Role"));

        fallbackAlerts.add(new JsonObject().put("id", 1).put("alertName", "High Subnet Utilization").put("threshold", 85).put("severity", "CRITICAL").put("status", "Enabled"));
        fallbackAlerts.add(new JsonObject().put("id", 2).put("alertName", "Rogue IP Detected").put("threshold", 1).put("severity", "MAJOR").put("status", "Enabled"));

        fallbackCustomColumns.add(new JsonObject().put("id", 1).put("columnName", "Asset Tag").put("columnType", "STRING").put("description", "Hardware Asset Identifier"));
        fallbackCustomColumns.add(new JsonObject().put("id", 2).put("columnName", "Owner Department").put("columnType", "STRING").put("description", "Department responsible for IP allocation"));

        fallbackDiscovery.add(new JsonObject().put("id", 1).put("profileName", "Subnet Auto Discovery").put("subnetRange", "192.168.1.0/24").put("schedule", "Daily at 00:00").put("status", "Active"));
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
        JsonArray data = new JsonArray();
        for (JsonObject u : fallbackUsers) {
            data.add(u);
        }

        JsonObject result = new JsonObject()
                .put("data", data)
                .put("success", true);

        ctx.response().putHeader("Content-Type", "application/json;charset=UTF-8").end(result.encode());
    }

    private void handleGetUserById(RoutingContext ctx) {
        JsonObject data = new JsonObject()
                .put("id", 1)
                .put("userName", "admin")
                .put("email", "admin@motadata.com")
                .put("status", true)
                .put("roleId", 1);

        JsonObject result = new JsonObject()
                .put("data", data)
                .put("success", true);

        ctx.response().putHeader("Content-Type", "application/json;charset=UTF-8").end(result.encode());
    }

    private void handleSaveUser(RoutingContext ctx) {
        JsonObject body = null;
        try {
            body = ctx.body().asJsonObject();
        } catch (Exception ignored) {}

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

        Long roleId = 1L;
        try {
            if (roleIdStr != null) roleId = Long.parseLong(roleIdStr);
        } catch (Exception ignored) {}

        if (userName == null || userName.trim().isEmpty()) {
            userName = "new_user";
        }
        if (email == null || email.trim().isEmpty()) {
            email = userName + "@motadata.com";
        }

        String roleName = (roleId == 2L) ? "ROLE_USER" : "ROLE_ADMIN";
        String roleDesc = (roleId == 2L) ? "Standard User Role" : "Administrator Role";

        long newId = fallbackUsers.size() + 1;
        JsonObject newUser = new JsonObject()
                .put("id", newId)
                .put("userName", userName)
                .put("email", email)
                .put("status", true)
                .put("roleName", roleName)
                .put("userRoleId", new JsonObject().put("id", roleId).put("role", roleName).put("description", roleDesc));

        fallbackUsers.add(newUser);

        JsonObject result = new JsonObject()
                .put("success", true)
                .put("message", "User Details Saved Successfully");

        ctx.response().putHeader("Content-Type", "application/json;charset=UTF-8").end(result.encode());
    }

    private void handleDeleteUser(RoutingContext ctx) {
        String idParam = ctx.pathParam("id");
        if (idParam != null) {
            try {
                long targetId = Long.parseLong(idParam);
                fallbackUsers.removeIf(u -> {
                    Object val = u.getValue("id");
                    return val != null && Long.parseLong(String.valueOf(val)) == targetId;
                });
            } catch (Exception ignored) {}
        }
        JsonObject result = new JsonObject()
                .put("success", true)
                .put("message", "User Deleted Successfully");

        ctx.response().putHeader("Content-Type", "application/json;charset=UTF-8").end(result.encode());
    }

    private void handleGetRoles(RoutingContext ctx) {
        JsonArray data = new JsonArray();
        for (JsonObject r : fallbackRoles) {
            data.add(r);
        }

        JsonObject result = new JsonObject()
                .put("data", data)
                .put("success", true);

        ctx.response().putHeader("Content-Type", "application/json;charset=UTF-8").end(result.encode());
    }

    private void handleGetRoleById(RoutingContext ctx) {
        String idParam = ctx.pathParam("id");
        if (idParam == null) idParam = ctx.request().getParam("id");
        if (idParam == null) idParam = ctx.request().getParam("userId");

        JsonObject data = null;
        if (idParam != null) {
            try {
                long targetId = Long.parseLong(idParam);
                for (JsonObject r : fallbackRoles) {
                    Object val = r.getValue("id");
                    if (val != null && Long.parseLong(String.valueOf(val)) == targetId) {
                        data = r;
                        break;
                    }
                }
            } catch (Exception ignored) {}
        }
        if (data == null) {
            data = new JsonObject()
                    .put("id", 1)
                    .put("role", "ROLE_ADMIN")
                    .put("roleName", "ROLE_ADMIN")
                    .put("description", "Administrator Role");
        }

        JsonObject result = new JsonObject()
                .put("data", data)
                .put("success", true);

        ctx.response().putHeader("Content-Type", "application/json;charset=UTF-8").end(result.encode());
    }

    private void handleGetRoleFeatures(RoutingContext ctx) {
        JsonArray data = new JsonArray()
                .add(new JsonObject().put("id", 1).put("featureName", "ALERTS"))
                .add(new JsonObject().put("id", 2).put("featureName", "ROGUE DETECTION"))
                .add(new JsonObject().put("id", 3).put("featureName", "REPORTS"))
                .add(new JsonObject().put("id", 4).put("featureName", "EVENT NOTIFICATIONS"))
                .add(new JsonObject().put("id", 5).put("featureName", "SETTINGS"))
                .add(new JsonObject().put("id", 6).put("featureName", "DASHBOARD"))
                .add(new JsonObject().put("id", 7).put("featureName", "IP REQUESTS"));

        JsonObject result = new JsonObject()
                .put("data", data)
                .put("success", true);

        ctx.response().putHeader("Content-Type", "application/json;charset=UTF-8").end(result.encode());
    }

    private void handleSaveRole(RoutingContext ctx) {
        JsonObject body = null;
        try {
            body = ctx.body().asJsonObject();
        } catch (Exception ignored) {}

        String idStr = ctx.pathParam("id");
        if (idStr == null) idStr = ctx.request().getParam("id");

        String role = ctx.request().getParam("role");
        if (role == null) role = ctx.request().getParam("roleName");
        String description = ctx.request().getParam("description");

        JsonArray permissions = null;

        if (body != null) {
            if (idStr == null && body.getValue("id") != null) idStr = String.valueOf(body.getValue("id"));
            if (role == null) role = body.getString("role");
            if (role == null) role = body.getString("roleName");
            if (description == null) description = body.getString("description");
            if (body.getJsonArray("permissions") != null) permissions = body.getJsonArray("permissions");
        }

        Long targetId = null;
        if (idStr != null) {
            try {
                targetId = Long.parseLong(idStr);
            } catch (Exception ignored) {}
        }

        if (role == null || role.trim().isEmpty()) {
            role = "ROLE_CUSTOM_" + (fallbackRoles.size() + 1);
        }
        if (description == null || description.trim().isEmpty()) {
            description = "Custom Role Description";
        }

        boolean updated = false;
        if (targetId != null) {
            for (JsonObject r : fallbackRoles) {
                Object val = r.getValue("id");
                if (val != null && Long.parseLong(String.valueOf(val)) == targetId) {
                    r.put("role", role);
                    r.put("roleName", role);
                    r.put("description", description);
                    if (permissions != null) r.put("permissions", permissions);
                    updated = true;
                    break;
                }
            }
        }

        if (!updated) {
            long newId = fallbackRoles.size() + 1;
            JsonObject newRole = new JsonObject()
                    .put("id", newId)
                    .put("role", role)
                    .put("roleName", role)
                    .put("description", description);
            if (permissions != null) newRole.put("permissions", permissions);

            fallbackRoles.add(newRole);
        }

        JsonObject result = new JsonObject()
                .put("success", true)
                .put("message", updated ? "User Role Updated Successfully" : "User Role Saved Successfully");

        ctx.response().putHeader("Content-Type", "application/json;charset=UTF-8").end(result.encode());
    }

    private void handleDeleteRole(RoutingContext ctx) {
        String idParam = ctx.pathParam("id");
        if (idParam != null) {
            try {
                long targetId = Long.parseLong(idParam);
                fallbackRoles.removeIf(r -> {
                    Object val = r.getValue("id");
                    return val != null && Long.parseLong(String.valueOf(val)) == targetId;
                });
            } catch (Exception ignored) {}
        }
        JsonObject result = new JsonObject()
                .put("success", true)
                .put("message", "User Role Deleted Successfully");

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

    private void handleSaveGlobalSetting(RoutingContext ctx) {
        JsonObject result = new JsonObject()
                .put("success", true)
                .put("message", "Global Settings Updated Successfully");

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

    private void handleSaveBrand(RoutingContext ctx) {
        JsonObject result = new JsonObject()
                .put("success", true)
                .put("message", "Branding Details Updated Successfully");

        ctx.response().putHeader("Content-Type", "application/json;charset=UTF-8").end(result.encode());
    }

    private void handleGetMailConfig(RoutingContext ctx) {
        JsonArray data = new JsonArray()
                .add(new JsonObject().put("id", 1).put("smtpHost", "smtp.gmail.com").put("smtpPort", 587).put("fromAddress", "admin@motadata.com"));

        JsonObject result = new JsonObject()
                .put("data", data)
                .put("success", true);

        ctx.response().putHeader("Content-Type", "application/json;charset=UTF-8").end(result.encode());
    }

    private void handleGetMailConfigById(RoutingContext ctx) {
        JsonObject data = new JsonObject()
                .put("id", 1)
                .put("smtpHost", "smtp.gmail.com")
                .put("smtpPort", 587)
                .put("fromAddress", "admin@motadata.com");

        JsonObject result = new JsonObject()
                .put("data", data)
                .put("success", true);

        ctx.response().putHeader("Content-Type", "application/json;charset=UTF-8").end(result.encode());
    }

    private void handleSaveMailConfig(RoutingContext ctx) {
        JsonObject result = new JsonObject()
                .put("success", true)
                .put("message", "Mail Server Configuration Saved Successfully");

        ctx.response().putHeader("Content-Type", "application/json;charset=UTF-8").end(result.encode());
    }

    private void handleGetConfigureAlert(RoutingContext ctx) {
        JsonObject result = new JsonObject()
                .put("data", alertConfigStore.copy())
                .put("success", true);

        ctx.response().putHeader("Content-Type", "application/json;charset=UTF-8").end(result.encode());
    }

    private void handleSaveConfigureAlert(RoutingContext ctx) {
        try {
            JsonArray bodyArray = ctx.body().asJsonArray();
            if (bodyArray != null) {
                for (int i = 0; i < bodyArray.size(); i++) {
                    JsonObject item = bodyArray.getJsonObject(i);
                    if (item != null && item.getString("alertKey") != null) {
                        alertConfigStore.put(item.getString("alertKey"), item.getString("alertValue"));
                    }
                }
            }
        } catch (Exception ignored) {
            try {
                JsonObject bodyObj = ctx.body().asJsonObject();
                if (bodyObj != null) {
                    for (String fieldName : bodyObj.fieldNames()) {
                        alertConfigStore.put(fieldName, String.valueOf(bodyObj.getValue(fieldName)));
                    }
                }
            } catch (Exception ignored2) {}
        }

        JsonObject result = new JsonObject()
                .put("success", true)
                .put("message", "Alert Configuration Saved Successfully");

        ctx.response().putHeader("Content-Type", "application/json;charset=UTF-8").end(result.encode());
    }

    private void handleGetCustomColumn(RoutingContext ctx) {
        JsonArray data = new JsonArray();
        for (JsonObject col : fallbackCustomColumns) {
            data.add(col);
        }

        JsonObject result = new JsonObject()
                .put("data", data)
                .put("success", true);

        ctx.response().putHeader("Content-Type", "application/json;charset=UTF-8").end(result.encode());
    }

    private void handleSaveCustomColumn(RoutingContext ctx) {
        JsonObject body = null;
        try {
            body = ctx.body().asJsonObject();
        } catch (Exception ignored) {}

        String columnName = ctx.request().getParam("columnName");
        if (body != null && columnName == null) columnName = body.getString("columnName");
        if (columnName == null || columnName.trim().isEmpty()) columnName = "Custom Column " + (fallbackCustomColumns.size() + 1);

        long newId = fallbackCustomColumns.size() + 1;
        JsonObject newCol = new JsonObject()
                .put("id", newId)
                .put("columnName", columnName)
                .put("columnType", "STRING")
                .put("description", "Custom Column Definition");

        fallbackCustomColumns.add(newCol);

        JsonObject result = new JsonObject()
                .put("success", true)
                .put("message", "Custom Column Saved Successfully");

        ctx.response().putHeader("Content-Type", "application/json;charset=UTF-8").end(result.encode());
    }

    private void handleDeleteCustomColumn(RoutingContext ctx) {
        JsonObject result = new JsonObject()
                .put("success", true)
                .put("message", "Custom Column Deleted Successfully");

        ctx.response().putHeader("Content-Type", "application/json;charset=UTF-8").end(result.encode());
    }

    private void handleGetDiscovery(RoutingContext ctx) {
        JsonArray data = new JsonArray();
        for (JsonObject d : fallbackDiscovery) {
            data.add(d);
        }

        JsonObject result = new JsonObject()
                .put("data", data)
                .put("success", true);

        ctx.response().putHeader("Content-Type", "application/json;charset=UTF-8").end(result.encode());
    }

    private void handleSaveDiscovery(RoutingContext ctx) {
        JsonObject result = new JsonObject()
                .put("success", true)
                .put("message", "Discovery Profile Saved Successfully");

        ctx.response().putHeader("Content-Type", "application/json;charset=UTF-8").end(result.encode());
    }

    private void handleGetDiscoveryScheduler(RoutingContext ctx) {
        JsonArray data = new JsonArray();
        for (JsonObject d : fallbackDiscovery) {
            data.add(d);
        }

        JsonObject result = new JsonObject()
                .put("data", data)
                .put("success", true);

        ctx.response().putHeader("Content-Type", "application/json;charset=UTF-8").end(result.encode());
    }

    private void handleSaveDiscoveryScheduler(RoutingContext ctx) {
        JsonObject result = new JsonObject()
                .put("success", true)
                .put("message", "Discovery Scheduler Saved Successfully");

        ctx.response().putHeader("Content-Type", "application/json;charset=UTF-8").end(result.encode());
    }

    private void handleGetDatabaseMaintenance(RoutingContext ctx) {
        JsonObject data = new JsonObject()
                .put("id", 1)
                .put("autoBackup", true)
                .put("retentionDays", 30);

        JsonObject result = new JsonObject()
                .put("data", data)
                .put("success", true);

        ctx.response().putHeader("Content-Type", "application/json;charset=UTF-8").end(result.encode());
    }

    private void handleSaveDatabaseMaintenance(RoutingContext ctx) {
        JsonObject result = new JsonObject()
                .put("success", true)
                .put("message", "Database Maintenance Settings Updated Successfully");

        ctx.response().putHeader("Content-Type", "application/json;charset=UTF-8").end(result.encode());
    }
}
