package com.motadata.ipam;

import com.motadata.ipam.core.verticle.ReportWorkerVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * JVM Process Entry Point & Bootstrap Launcher for the Motadata IPAM Application.
 * <p>
 * Responsibilities:
 * <ul>
 *   <li>Configures Vert.x Engine runtime options (event loops, blocked thread timeouts).</li>
 *   <li>Bootstraps the root {@link Vertx} instance.</li>
 *   <li>Registers JVM graceful shutdown hooks.</li>
 *   <li>Deploys the root orchestrator {@link MainVerticle}.</li>
 * </ul>
 */
public class IpamApplication {

    private static final Logger LOGGER = LoggerFactory.getLogger(IpamApplication.class);

    // Creates Vert.x instance with optimized engine options and deploys the MainVerticle orchestrator.
    public static void main(String[] args) {
        // 1. Silence third-party verbose JUL loggers from DynamicJasper & JasperReports
        ReportWorkerVerticle.silenceThirdPartyLoggers();

        // 2. Configure Vert.x Engine Options
        VertxOptions options = new VertxOptions()
                .setEventLoopPoolSize(Runtime.getRuntime().availableProcessors() * 2)
                .setBlockedThreadCheckInterval(5000)
                .setBlockedThreadCheckIntervalUnit(TimeUnit.MILLISECONDS)
                .setMaxEventLoopExecuteTime(2000)
                .setMaxEventLoopExecuteTimeUnit(TimeUnit.MILLISECONDS);

        // 3. Create root Vert.x instance
        Vertx vertx = Vertx.vertx(options);

        // 4. Register JVM Graceful Shutdown Hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOGGER.info("Graceful shutdown signal received. Closing Vert.x instance...");
            vertx.close().onComplete(ar -> LOGGER.info("Vert.x instance stopped cleanly."));
        }));

        // 5. Deploy Main Root Orchestrator Verticle
        LOGGER.info("Bootstrapping Vert.x 5 IPAM System...");
        vertx.deployVerticle(new MainVerticle()).onComplete(ar -> {
            if (ar.succeeded()) {
                LOGGER.info("Vert.x 5 IPAM Application successfully initialized and deployed (Deployment ID: {}).", ar.result());
            } else {
                LOGGER.error("Failed to start Vert.x 5 IPAM Application: {}", ar.cause().getMessage(), ar.cause());
                System.exit(1);
            }
        });
    }
}
