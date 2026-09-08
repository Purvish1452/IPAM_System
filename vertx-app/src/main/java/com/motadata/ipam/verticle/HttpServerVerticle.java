package com.motadata.ipam.verticle;

import com.motadata.ipam.config.AppConfig;
import com.motadata.ipam.router.*;
import com.motadata.ipam.security.JwtAuthHandler;
import com.motadata.ipam.security.JwtAuthProvider;
import com.motadata.ipam.service.*;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.sstore.LocalSessionStore;
import io.vertx.sqlclient.Pool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Dedicated Event Loop Verticle for HTTP Web Server & REST Routing.
 * Runs strictly on the Netty Event Loop.
 * Zero blocking operations: all database queries are reactive (PgPool)
 * and all heavy/blocking jobs are dispatched asynchronously to the EventBus.
 */
public class HttpServerVerticle extends AbstractVerticle {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpServerVerticle.class);

    private final Pool db;
    private final AppConfig config;
    private final JwtAuthProvider jwtAuthProvider;

    public HttpServerVerticle(Pool db, AppConfig config, JwtAuthProvider jwtAuthProvider) {
        this.db = db;
        this.config = config;
        this.jwtAuthProvider = jwtAuthProvider;
    }

    @Override
    public void start(Promise<Void> startPromise) {
        LOGGER.info("Starting HttpServerVerticle on Event Loop Thread: {}", Thread.currentThread().getName());

        // Initialize Direct Reactive Services (No DAO layer)
        UserService userService = new UserService(db, jwtAuthProvider);
        SubnetService subnetService = new SubnetService(db);
        DhcpService dhcpService = new DhcpService(vertx, db);
        AlertService alertService = new AlertService(db);
        EventService eventService = new EventService(db);
        SettingsService settingsService = new SettingsService(db);
        DiscoveryService discoveryService = new DiscoveryService(vertx, db);
        ReportService reportService = new ReportService(vertx, db);
        SubnetIPActionService subnetIPActionService = new SubnetIPActionService(vertx, db, discoveryService, alertService);

        // Configure Vert.x Web Router
        Router router = Router.router(vertx);

        // Body & Session handlers mounted first
        router.route().handler(BodyHandler.create());
        router.route().handler(SessionHandler.create(LocalSessionStore.create(vertx)));
        router.route().handler(new JwtAuthHandler(jwtAuthProvider));

        // Mount REST API Routers
        new AuthRouter(userService).attachRoutes(router);
        new SubnetRouter(subnetService, userService, subnetIPActionService, discoveryService).attachRoutes(router);
        new DhcpRouter(dhcpService).attachRoutes(router);
        new SettingsRouter(userService, settingsService, alertService, discoveryService).attachRoutes(router);
        new EventRouter(eventService, reportService).attachRoutes(router);
        new AlertRouter(alertService).attachRoutes(router);
        new ReportRouter(reportService).attachRoutes(router);

        // Serve static web assets from webroot
        router.route("/*").handler(StaticHandler.create("webroot")
                .setCachingEnabled(false)
                .setMaxAgeSeconds(0));

        // Determine port
        int port = config().getInteger("server-port", config != null ? config.getServerPort() : 8080);

        vertx.createHttpServer(new HttpServerOptions().setPort(port))
                .requestHandler(router)
                .listen()
                .onComplete(httpAr -> {
                    if (httpAr.succeeded()) {
                        LOGGER.info("===============================================================");
                        LOGGER.info(" HttpServerVerticle running on http://localhost:{}", port);
                        LOGGER.info(" Mode: Non-blocking Event Loop [Netty]");
                        LOGGER.info("===============================================================");
                        startPromise.complete();
                    } else {
                        LOGGER.error("Failed to start HTTP server on port {}: {}", port, httpAr.cause().getMessage());
                        startPromise.fail(httpAr.cause());
                    }
                });
    }
}
