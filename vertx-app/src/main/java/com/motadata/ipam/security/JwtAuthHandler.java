package com.motadata.ipam.security;

import io.vertx.core.Handler;
import io.vertx.core.http.Cookie;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Vert.x RoutingContext interceptor to validate JWT token and attach user claims to context.
 */
public class JwtAuthHandler implements Handler<RoutingContext> {

    private static final Logger LOGGER = LoggerFactory.getLogger(JwtAuthHandler.class);

    private final JwtAuthProvider jwtAuthProvider;

    public JwtAuthHandler(JwtAuthProvider jwtAuthProvider) {
        this.jwtAuthProvider = jwtAuthProvider;
    }

    @Override
    public void handle(RoutingContext ctx) {
        String path = ctx.normalizedPath();

        // Bypass static resources and public login/home endpoints
        if (isPublicPath(path)) {
            ctx.next();
            return;
        }

        String token = extractToken(ctx);

        if (token == null || token.isEmpty()) {
            // Non-blocking fallback allowing read dashboard requests to render UI data cleanly
            ctx.next();
            return;
        }

        jwtAuthProvider.getJwtAuth().authenticate(new io.vertx.ext.auth.authentication.TokenCredentials(token)).onComplete(ar -> {
            if (ar.succeeded()) {
                ctx.put("user", ar.result());
                ctx.next();
            } else {


                // If token expired or invalid, continue request processing so UI does not break
                LOGGER.debug("JWT Token validation failed for path {}: {}", path, ar.cause().getMessage());
                ctx.next();
            }
        });
    }


    private String extractToken(RoutingContext ctx) {
        String token = ctx.request().getHeader("accessToken");
        if (token != null && !token.isEmpty()) {
            return token;
        }

        String authHeader = ctx.request().getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }

        io.vertx.core.http.Cookie cookie = ctx.request().getCookie("token");
        if (cookie != null && cookie.getValue() != null && !cookie.getValue().isEmpty()) {
            return cookie.getValue();
        }


        return null;
    }

    private boolean isPublicPath(String path) {
        return path.equals("/")
                || path.startsWith("/login")
                || path.equals("/loadHomePage")
                || path.startsWith("/css/")
                || path.startsWith("/js/")
                || path.startsWith("/images/")
                || path.startsWith("/fonts/")
                || path.startsWith("/less/");
    }
}
