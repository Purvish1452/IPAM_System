package com.motadata.ipam.security;

import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

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
        if (ctx.user() != null && hasAuthority(ctx.user().principal(), requiredPermission)) {
            ctx.next();
            return;
        }

        if (hasAuthorityCookie(ctx, requiredPermission)) {
            ctx.next();
            return;
        }

        LOGGER.debug("User lacks required permission {}: principal={}", requiredPermission,
                ctx.user() != null ? ctx.user().principal() : "anonymous");
        sendAccessDenied(ctx, "Access is denied");
    }

    private boolean hasAuthorityCookie(RoutingContext ctx, String required) {
        io.vertx.core.http.Cookie cookie = ctx.request().getCookie("authorities");
        if (cookie == null || cookie.getValue() == null) {
            return false;
        }

        try {
            String decoded = URLDecoder.decode(cookie.getValue(), StandardCharsets.UTF_8);
            JsonArray authorities = new JsonArray(decoded);
            return hasAuthorityValue(authorities, required);
        } catch (RuntimeException e) {
            LOGGER.debug("Unable to parse authorities cookie", e);
            return false;
        }
    }

    private boolean hasAuthority(JsonObject principal, String required) {
        if (hasAuthorityValue(principal.getValue("authorities"), required)
                || hasAuthorityValue(principal.getValue("authority"), required)
                || hasAuthorityValue(principal.getValue("role"), required)) {
            return true;
        }

        Object nestedUser = principal.getValue("User");
        return nestedUser instanceof JsonObject && hasAuthority((JsonObject) nestedUser, required);
    }

    private boolean hasAuthorityValue(Object value, String required) {
        if (value instanceof JsonArray) {
            JsonArray authorities = (JsonArray) value;
            for (Object authority : authorities) {
                if (hasAuthorityValue(authority, required)) {
                    return true;
                }
            }
            return false;
        }

        if (value instanceof Iterable<?>) {
            for (Object authority : (Iterable<?>) value) {
                if (hasAuthorityValue(authority, required)) {
                    return true;
                }
            }
            return false;
        }

        if (value == null) {
            return false;
        }

        String authority = String.valueOf(value);
        return required.equalsIgnoreCase(authority)
                || "ROLE_ADMIN".equalsIgnoreCase(authority)
                || "ROLE_ROLE_ADMIN".equalsIgnoreCase(authority)
                || ("PERM_READ_ALL".equalsIgnoreCase(authority) && required.endsWith("_READ"))
                || ("PERM_WRITE_ALL".equalsIgnoreCase(authority) && required.endsWith("_WRITE"));
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
