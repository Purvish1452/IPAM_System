package com.motadata.ipam.core.verticle;

import com.motadata.ipam.core.config.AppConfig;
import com.motadata.ipam.feature.alert.AlertRouter;
import com.motadata.ipam.feature.alert.AlertService;
import com.motadata.ipam.feature.auth.AuthRouter;
import com.motadata.ipam.feature.auth.JwtAuthHandler;
import com.motadata.ipam.feature.auth.JwtAuthProvider;
import com.motadata.ipam.feature.auth.UserService;
import com.motadata.ipam.feature.dhcp.DhcpRouter;
import com.motadata.ipam.feature.dhcp.DhcpService;
import com.motadata.ipam.feature.discovery.DiscoveryService;
import com.motadata.ipam.feature.event.EventRouter;
import com.motadata.ipam.feature.event.EventService;
import com.motadata.ipam.feature.report.ReportRouter;
import com.motadata.ipam.feature.report.ReportService;
import com.motadata.ipam.feature.settings.SettingsRouter;
import com.motadata.ipam.feature.settings.SettingsService;
import com.motadata.ipam.feature.subnet.SubnetIPActionService;
import com.motadata.ipam.feature.subnet.SubnetRouter;
import com.motadata.ipam.feature.subnet.SubnetService;
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
 * Standard Verticle serving the REST API and UI routes non-blockingly on the Netty Event Loop.
 * Direct Architecture: Event Loop Handler -> Service -> PgPool / EventBus
 * Zero blocking operations on the Event Loop.
 */
public class HttpServerVerticle extends AbstractVerticle {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpServerVerticle.class);

    private final Pool db;
    private final AppConfig config;
    private final JwtAuthProvider jwtAuthProvider;

    // Constructs HttpServerVerticle with database pool, application config, and JWT provider.
    public HttpServerVerticle(Pool db, AppConfig config, JwtAuthProvider jwtAuthProvider) {
        this.db = db;
        this.config = config;
        this.jwtAuthProvider = jwtAuthProvider;
    }

    // Initializes services, configures routes, and starts the reactive HTTP server on the Event Loop.
    @Override
    public void start(Promise<Void> startPromise) {
        LOGGER.info("Starting HttpServerVerticle on Event Loop Thread: {}", Thread.currentThread().getName());

        // Initialize Direct Reactive Services (No DAO layer)
        UserService userService = new UserService(db, jwtAuthProvider);
        SubnetService subnetService = new SubnetService(db);
        DhcpService dhcpService = new DhcpService(vertx, db);
        AlertService alertService = new AlertService(db);
        EventService eventService = new EventService(vertx, db);
        SettingsService settingsService = new SettingsService(db);
        DiscoveryService discoveryService = new DiscoveryService(vertx, db);
        ReportService reportService = new ReportService(vertx, db);
        SubnetIPActionService subnetIPActionService = new SubnetIPActionService(vertx, db, discoveryService, alertService);

        // Configure Vert.x Web Router
        Router router = Router.router(vertx);

        // CORS and Options handler mounted before routing
        router.route().handler(ctx -> {
            ctx.response().putHeader("Access-Control-Allow-Origin", "*");
            ctx.response().putHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
            ctx.response().putHeader("Access-Control-Allow-Headers", "Content-Type, Authorization, X-Requested-With");
            if (ctx.request().method() == io.vertx.core.http.HttpMethod.OPTIONS) {
                ctx.response().setStatusCode(204).end();
            } else {
                ctx.next();
            }
        });

        // Body & Session handlers mounted
        router.route().handler(BodyHandler.create());
        router.route().handler(SessionHandler.create(LocalSessionStore.create(vertx)));
        router.route().handler(new JwtAuthHandler(jwtAuthProvider));

        // Global Failure Handler for clean JSON error responses
        router.route().failureHandler(ctx -> {
            int statusCode = ctx.statusCode() > 0 ? ctx.statusCode() : 500;
            Throwable failure = ctx.failure();
            String errorMessage = failure != null ? failure.getMessage() : "Internal Server Error";
            LOGGER.error("HTTP Request [{}] {} failed with status {}: {}",
                    ctx.request().method(), ctx.request().uri(), statusCode, errorMessage);

            if (!ctx.response().ended()) {
                ctx.response()
                        .setStatusCode(statusCode)
                        .putHeader("Content-Type", "application/json; charset=utf-8")
                        .end(new io.vertx.core.json.JsonObject()
                                .put("success", false)
                                .put("status", statusCode)
                                .put("message", errorMessage)
                                .encode());
            }
        });

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

        // Determine host and port
        int port = config().getInteger("server-port", config != null ? config.getServerPort() : 8080);
        String host = config().getString("server-host", config != null ? config.getServerHost() : "0.0.0.0");

        HttpServerOptions serverOptions = new HttpServerOptions()
                .setHost(host)
                .setPort(port)
                .setCompressionSupported(true)
                .setDecompressionSupported(true);

        vertx.createHttpServer(serverOptions)
                .requestHandler(router)
                .listen(port, host)
                .mapEmpty()
                .onSuccess(v -> {
                    LOGGER.info("===============================================================");
                    LOGGER.info(" HttpServerVerticle running on http://{}:{}", host, port);
                    LOGGER.info(" Mode: Non-blocking Event Loop [Netty]");
                    LOGGER.info("===============================================================");
                    startPromise.complete();
                })
                .onFailure(err -> {
                    LOGGER.error("Failed to start HTTP server on {}:{}: {}", host, port, err.getMessage());
                    startPromise.fail(err);
                });
    }
}
