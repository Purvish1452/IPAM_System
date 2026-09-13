package com.motadata.ipam.security;

import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.User;
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

    // Constructs PermissionHandler requiring the specified permission string.
    public PermissionHandler(String requiredPermission) {
        this.requiredPermission = requiredPermission;
    }

    // Static factory creating a PermissionHandler for the required permission.
    public static Handler<RoutingContext> require(String requiredPermission) {
        return new PermissionHandler(requiredPermission);
    }

    // Evaluates context token, cookie, and session authorities against required permission.
    @Override
    public void handle(RoutingContext ctx) {
        User authenticatedUser = ctx.user();
        if (authenticatedUser == null) {
            authenticatedUser = ctx.get("user");
        }

        if (authenticatedUser != null && hasAuthority(authenticatedUser.principal(), requiredPermission)) {
            ctx.next();
            return;
        }

        if (hasAuthorityCookie(ctx, requiredPermission)) {
            ctx.next();
            return;
        }

        // Fallback: check authorities stored in the Vert.x session during login.
        // This handles the case where the browser loses the authorities cookie
        // but the vertx-web.session cookie (used by SessionHandler) is still active.
        if (hasAuthoritySession(ctx, requiredPermission)) {
            ctx.next();
            return;
        }

        LOGGER.debug("User lacks required permission {}: principal={}", requiredPermission,
                authenticatedUser != null ? authenticatedUser.principal() : "anonymous");
        sendAccessDenied(ctx, "Access is denied");
    }

    // Checks whether the authorities cookie contains the required permission.
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

    // Checks the session store for user authorities matching the required permission.
    private boolean hasAuthoritySession(RoutingContext ctx, String required) {
        if (ctx.session() == null) {
            return false;
        }

        String authoritiesJson = ctx.session().get("authorities");
        if (authoritiesJson == null || authoritiesJson.isEmpty()) {
            return false;
        }

        try {
            JsonArray authorities = new JsonArray(authoritiesJson);
            return hasAuthorityValue(authorities, required);
        } catch (RuntimeException e) {
            LOGGER.debug("Unable to parse session authorities", e);
            return false;
        }
    }

    // Checks whether the principal payload contains the required permission.
    private boolean hasAuthority(JsonObject principal, String required) {
        if (hasAuthorityValue(principal.getValue("authorities"), required)
                || hasAuthorityValue(principal.getValue("authority"), required)
                || hasAuthorityValue(principal.getValue("role"), required)) {
            return true;
        }

        Object nestedUser = principal.getValue("User");
        return nestedUser instanceof JsonObject && hasAuthority((JsonObject) nestedUser, required);
    }

    // Checks if the authority object or array satisfies the required permission.
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
        return matchesPermission(authority, required)
                || "ROLE_ADMIN".equalsIgnoreCase(authority)
                || "ROLE_ROLE_ADMIN".equalsIgnoreCase(authority)
                || ("PERM_READ_ALL".equalsIgnoreCase(authority) && required.endsWith("_READ"))
                || ("PERM_WRITE_ALL".equalsIgnoreCase(authority) && required.endsWith("_WRITE"));
    }

    // Matches permission strings accounting for exact matches and legacy formats.
    private boolean matchesPermission(String authority, String required) {
        if (required.equalsIgnoreCase(authority)) {
            return true;
        }

        // Accept the legacy PERM_READ_FEATURE form while sessions issued before
        // the authority naming fix are still active.
        String legacyRequired = required.startsWith("PERM_") && required.endsWith("_READ")
                ? "PERM_READ_" + required.substring("PERM_".length(), required.length() - "_READ".length())
                : required.startsWith("PERM_") && required.endsWith("_WRITE")
                ? "PERM_WRITE_" + required.substring("PERM_".length(), required.length() - "_WRITE".length())
                : required;
        return legacyRequired.equalsIgnoreCase(authority);
    }

    // Sends a 403 Forbidden response when authorization check fails.
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
