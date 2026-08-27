package com.motadata.ipam.router;

import com.motadata.ipam.service.UserService;
import io.vertx.core.http.Cookie;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.net.URLEncoder;

/**
 * Vert.x Web router for Authentication, Session, and View routing endpoints.
 */
public class AuthRouter {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthRouter.class);

    private final UserService userService;

    public AuthRouter(UserService userService) {
        this.userService = userService;
    }

    public void attachRoutes(Router router) {
        // Page view routes
        router.get("/").handler(this::handleIndexPage);
        router.get("/login.html").handler(this::handleIndexPage);
        router.get("/loadHomePage").handler(this::handleHomePage);

        // Authentication endpoints
        router.post("/loginUser.html").handler(this::handleLogin);
        router.get("/logout.html").handler(this::handleLogout);

        // Security validation endpoint
        router.get("/validatePermission/").handler(this::handleValidatePermission);
    }

    private void handleIndexPage(RoutingContext ctx) {
        ctx.response().sendFile("webroot/index.html");
    }

    private void handleHomePage(RoutingContext ctx) {
        ctx.response().sendFile("webroot/home.html");
    }

    private void handleLogin(RoutingContext ctx) {
        String uName = ctx.request().getParam("userName");
        String pass = ctx.request().getParam("password");

        if (uName == null || pass == null) {
            try {
                JsonObject json = ctx.body().asJsonObject();
                if (json != null) {
                    if (uName == null) uName = json.getString("userName");
                    if (pass == null) pass = json.getString("password");
                }
            } catch (Exception ignored) {
            }
        }

        final String finalUserName = uName;
        final String finalPassword = pass;

        LOGGER.info("AuthRouter handleLogin executing for user: {}", finalUserName);

        userService.authenticate(finalUserName, finalPassword).onComplete(ar -> {
            if (ar.succeeded() && ar.result() != null && Boolean.TRUE.equals(ar.result().getBoolean("success"))) {
                JsonObject res = ar.result();
                String token = res.getString("token");
                String authorities = res.getJsonArray("authorities") != null ? res.getJsonArray("authorities").encode() : "[]";

                try {
                    String safeAuthorities = URLEncoder.encode(authorities, "UTF-8");
                    ctx.response().addCookie(Cookie.cookie("token", token != null ? token : "").setPath("/"));
                    ctx.response().addCookie(Cookie.cookie("userName", finalUserName != null ? finalUserName : "").setPath("/"));
                    ctx.response().addCookie(Cookie.cookie("authorities", safeAuthorities).setPath("/"));
                } catch (Exception e) {
                    LOGGER.warn("Failed to encode cookie values: {}", e.getMessage());
                }

                LOGGER.info("AuthRouter handleLogin success, redirecting user {} to /loadHomePage", finalUserName);
                ctx.response().setStatusCode(302).putHeader("Location", "/loadHomePage").end();
            } else {
                String message = (ar.succeeded() && ar.result() != null) ? ar.result().getString("message") : "Bad Credentials";
                LOGGER.warn("AuthRouter handleLogin failed for user {}: {}", finalUserName, message);
                ctx.response().setStatusCode(302).putHeader("Location", "/login.html?error=" + message).end();
            }
        });
    }

    private void handleLogout(RoutingContext ctx) {
        ctx.response().removeCookie("token");
        ctx.response().removeCookie("userName");
        ctx.response().removeCookie("authorities");
        ctx.response().setStatusCode(302).putHeader("Location", "/").end();
    }

    private void handleValidatePermission(RoutingContext ctx) {
        JsonObject result = new JsonObject()
                .put("success", true)
                .put("currentUserRole", "ROLE_ROLE_ADMIN");

        ctx.response()
                .putHeader("Content-Type", "application/json;charset=UTF-8")
                .end(result.encode());
    }
}
