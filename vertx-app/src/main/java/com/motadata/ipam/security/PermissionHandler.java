package com.motadata.ipam.security;

import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

/**
 * Vert.x Route Handler to enforce RBAC permissions (e.g., PERM_ALERTS_READ, PERM_SETTINGS_READ).
 */
public class PermissionHandler implements Handler<RoutingContext> {

    private static final Logger LOGGER = LoggerFactory.getLogger(PermissionHandler.class);

    private final String requiredPermission;

    public PermissionHandler(String requiredPermission) {
        this.requiredPermission = requiredPermission;
    }

    public static Handler<RoutingContext> require(String requiredPermission) {
        return new PermissionHandler(requiredPermission);
    }

    @Override
    public void handle(RoutingContext ctx) {
        if (ctx.user() == null) {
            sendAccessDenied(ctx, "Access is denied");
            return;
        }

        JsonObject principal = ctx.user().principal();
        JsonArray authorities = principal.getJsonArray("authorities");

        if (authorities != null) {
            for (int i = 0; i < authorities.size(); i++) {
                String auth = authorities.getString(i);
                if (requiredPermission.equalsIgnoreCase(auth) || "ROLE_ADMIN".equalsIgnoreCase(auth) || "ROLE_ROLE_ADMIN".equalsIgnoreCase(auth)) {
                    ctx.next();
                    return;
                }
            }
        }

        LOGGER.debug("User lacks required permission {}: principal={}", requiredPermission, principal);
        sendAccessDenied(ctx, "Access is denied");
    }

    private void sendAccessDenied(RoutingContext ctx, String message) {
        JsonObject errorResponse = new JsonObject()
                .put("message", message)
                .put("status", 403)
                .put("error", "Forbidden");

        ctx.response()
                .setStatusCode(403)
                .putHeader("Content-Type", "application/json;charset=UTF-8")
                .end(errorResponse.encode());
    }
}
