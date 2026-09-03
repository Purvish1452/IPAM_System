package com.motadata.ipam;

import com.motadata.ipam.config.AppConfig;
import com.motadata.ipam.db.DatabaseInit;
import com.motadata.ipam.db.PgClientProvider;
import com.motadata.ipam.router.*;
import com.motadata.ipam.scheduler.JobScheduler;
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
 * Entry point Verticle for the Vert.x IPAM Web Application.
 * Architecture: Handler -> Service -> PgPool -> PostgreSQL
 */
public class MainVerticle extends AbstractVerticle {

    private static final Logger LOGGER = LoggerFactory.getLogger(MainVerticle.class);

    private PgClientProvider pgClientProvider;
    private JobScheduler jobScheduler;

    @Override
    public void start(Promise<Void> startPromise) {
        LOGGER.info("Starting Vert.x IPAM MainVerticle (PostgreSQL Reactive Engine)...");

        AppConfig.load(vertx).onComplete(configAr -> {
            if (configAr.failed()) {
                LOGGER.error("Failed to load application configuration: {}", configAr.cause().getMessage());
                startPromise.fail(configAr.cause());
                return;
            }

            AppConfig config = configAr.result();

            // Initialize PostgreSQL Reactive Connection Pool
            pgClientProvider = new PgClientProvider(vertx, config);
            Pool db = pgClientProvider.getPool();

            // Initialize PostgreSQL Schema & Seed Data
            DatabaseInit.initSchema(vertx, db).onComplete(initAr -> {
                if (initAr.failed()) {
                    LOGGER.warn("Database initialization warning: {}", initAr.cause().getMessage());
                }

                // Initialize Security Provider
                JwtAuthProvider jwtAuthProvider = new JwtAuthProvider(vertx);

                // Initialize Background Job Scheduler
                jobScheduler = new JobScheduler(vertx);
                jobScheduler.start();

                // Initialize Direct Reactive Services (No DAO layer)
                UserService userService = new UserService(db, jwtAuthProvider);
                SubnetService subnetService = new SubnetService(db);
                DhcpService dhcpService = new DhcpService(db);
                AlertService alertService = new AlertService(db);
                EventService eventService = new EventService(db);
                SettingsService settingsService = new SettingsService(db);
                DiscoveryService discoveryService = new DiscoveryService(db);
                ReportService reportService = new ReportService(vertx, db);
                SubnetIPActionService subnetIPActionService = new SubnetIPActionService(vertx, db);

                // Configure Vert.x Web Router
                Router router = Router.router(vertx);

                // Body & Session handlers mounted first
                router.route().handler(BodyHandler.create());
                router.route().handler(SessionHandler.create(LocalSessionStore.create(vertx)));
                router.route().handler(new JwtAuthHandler(jwtAuthProvider));

                // Mount REST API Routers
                new AuthRouter(userService).attachRoutes(router);
                new SubnetRouter(subnetService, userService, subnetIPActionService).attachRoutes(router);
                new DhcpRouter(dhcpService).attachRoutes(router);
                new SettingsRouter(userService, settingsService, alertService, discoveryService).attachRoutes(router);
                new EventRouter(eventService).attachRoutes(router);
                new AlertRouter(alertService).attachRoutes(router);
                new ReportRouter(reportService).attachRoutes(router);

                // Serve static web assets from webroot (disable caching for development/live updates)
                router.route("/*").handler(StaticHandler.create("webroot")
                        .setCachingEnabled(false)
                        .setMaxAgeSeconds(0));

                // Start HTTP Server
                int port = config.getServerPort();
                vertx.createHttpServer(new HttpServerOptions().setPort(port))
                        .requestHandler(router)
                        .listen(httpAr -> {
                            if (httpAr.succeeded()) {
                                LOGGER.info("===============================================================");
                                LOGGER.info(" Vert.x IPAM Server running on http://localhost:{}", port);
                                LOGGER.info(" Architecture: Handler -> Service -> PgPool -> PostgreSQL");
                                LOGGER.info("===============================================================");
                                startPromise.complete();
                            } else {
                                LOGGER.error("Failed to start Vert.x HTTP server on port {}: {}", port, httpAr.cause().getMessage());
                                startPromise.fail(httpAr.cause());
                            }
                        });
            });
        });
    }

    @Override
    public void stop(Promise<Void> stopPromise) {
        if (jobScheduler != null) {
            jobScheduler.stop();
        }
        if (pgClientProvider != null) {
            pgClientProvider.close();
        }
        LOGGER.info("Stopped Vert.x IPAM MainVerticle.");
        stopPromise.complete();
    }
}
