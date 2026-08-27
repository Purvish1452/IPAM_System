package com.motadata.ipam;

import com.motadata.ipam.config.AppConfig;
import com.motadata.ipam.dao.*;
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
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

/**
 * Entry point Verticle for the Vert.x IPAM Web Application.
 */
public class MainVerticle extends AbstractVerticle {

    private static final Logger LOGGER = LoggerFactory.getLogger(MainVerticle.class);

    private DatabasePool dbPool;
    private JobScheduler jobScheduler;

    @Override
    public void start(Promise<Void> startPromise) {
        LOGGER.info("Starting Vert.x IPAM MainVerticle...");

        AppConfig.load(vertx).onComplete(configAr -> {
            if (configAr.failed()) {
                LOGGER.error("Failed to load application configuration: {}", configAr.cause().getMessage());
                startPromise.fail(configAr.cause());
                return;
            }

            AppConfig config = configAr.result();

            // Run database Flyway migrations
            FlywayRunner.runMigrations(vertx, config).onComplete(flywayAr -> {
                if (flywayAr.failed()) {
                    LOGGER.warn("Flyway migration warning: {}", flywayAr.cause().getMessage());
                }

                // Initialize Database Pool & Security
                dbPool = new DatabasePool(vertx, config);
                JwtAuthProvider jwtAuthProvider = new JwtAuthProvider(vertx);

                // Initialize Background Job Scheduler
                jobScheduler = new JobScheduler(vertx);
                jobScheduler.start();

                // Initialize DAOs
                UserDao userDao = new UserDao(dbPool.getClient());
                SubnetDao subnetDao = new SubnetDao(dbPool.getClient());
                AlertDao alertDao = new AlertDao(dbPool.getClient());
                EventDao eventDao = new EventDao(dbPool.getClient());
                DhcpDao dhcpDao = new DhcpDao(dbPool.getClient());

                // Initialize Services
                UserService userService = new UserService(userDao, jwtAuthProvider);
                SubnetService subnetService = new SubnetService(subnetDao);
                AlertService alertService = new AlertService(alertDao);
                EventService eventService = new EventService(eventDao);
                ReportService reportService = new ReportService(vertx, subnetDao, alertDao, eventDao, dhcpDao);

                // Configure Router
                Router router = Router.router(vertx);

                // Body & Session handlers mounted first
                router.route().handler(BodyHandler.create());
                router.route().handler(SessionHandler.create(LocalSessionStore.create(vertx)));
                router.route().handler(new JwtAuthHandler(jwtAuthProvider));

                // Mount API Routers
                new AuthRouter(userService).attachRoutes(router);
                new SubnetRouter(subnetService).attachRoutes(router);
                new AlertRouter(alertService).attachRoutes(router);
                new EventRouter(eventService).attachRoutes(router);
                new SettingsRouter().attachRoutes(router);
                new DhcpRouter(dhcpDao).attachRoutes(router);
                new ReportRouter(reportService).attachRoutes(router);

                // Serve static web assets from webroot
                router.route("/*").handler(StaticHandler.create("webroot"));

                // Start HTTP Server
                int port = config.getServerPort();
                vertx.createHttpServer(new HttpServerOptions().setPort(port))
                        .requestHandler(router)
                        .listen(httpAr -> {
                            if (httpAr.succeeded()) {
                                LOGGER.info("Vert.x IPAM Server successfully started on http://localhost:{}", port);
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
        if (dbPool != null) {
            dbPool.close();
        }
        LOGGER.info("Stopped Vert.x IPAM MainVerticle.");
        stopPromise.complete();
    }
}
