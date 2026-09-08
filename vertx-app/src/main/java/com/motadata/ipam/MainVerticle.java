package com.motadata.ipam;

import com.motadata.ipam.config.AppConfig;
import com.motadata.ipam.db.DatabaseInit;
import com.motadata.ipam.db.PgClientProvider;
import com.motadata.ipam.scheduler.JobScheduler;
import com.motadata.ipam.security.JwtAuthProvider;
import com.motadata.ipam.verticle.HttpServerVerticle;
import com.motadata.ipam.verticle.NetworkWorkerVerticle;
import com.motadata.ipam.verticle.ReportWorkerVerticle;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.sqlclient.Pool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main Deployer & Orchestrator Verticle for the Vert.x IPAM Application.
 * Architecture:
 *   - Event Loop Layer: HttpServerVerticle (Standard Verticle)
 *   - Worker Pool Layer: NetworkWorkerVerticle (30 threads), ReportWorkerVerticle (5 threads)
 *   - Messaging: Vert.x EventBus
 */
public class MainVerticle extends AbstractVerticle {

    private static final Logger LOGGER = LoggerFactory.getLogger(MainVerticle.class);

    private PgClientProvider pgClientProvider;
    private JobScheduler jobScheduler;

    // Create Vert.x and deploy the main application verticle.
    public static void main(String[] args) {
        io.vertx.core.Vertx vertx = io.vertx.core.Vertx.vertx();
        vertx.deployVerticle(new MainVerticle()).onComplete(ar -> {
            if (ar.succeeded()) {
                LOGGER.info("Vert.x 5 IPAM Application successfully initialized and deployed.");
            } else {
                LOGGER.error("Failed to start Vert.x 5 IPAM Application: {}", ar.cause().getMessage(), ar.cause());
                System.exit(1);
            }
        });
    }

    @Override
    public void start(Promise<Void> startPromise) {
        LOGGER.info("Starting Vert.x IPAM Main Deployer (Reactive EventBus Engine)...");

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

                // 1. Deploy Network Discovery Worker Verticle (Worker Pool: 30 Threads)
                DeploymentOptions networkWorkerOpts = new DeploymentOptions()
                        .setThreadingModel(io.vertx.core.ThreadingModel.WORKER)
                        .setWorkerPoolName("ipam-network-worker-pool")
                        .setWorkerPoolSize(30)
                        .setInstances(2);

                Future<String> deployNetworkWorker = vertx.deployVerticle(() -> new NetworkWorkerVerticle(db), networkWorkerOpts);

                // 2. Deploy Report Worker Verticle (Worker Pool: 5 Threads, Capped for Heap Safety)
                DeploymentOptions reportWorkerOpts = new DeploymentOptions()
                        .setThreadingModel(io.vertx.core.ThreadingModel.WORKER)
                        .setWorkerPoolName("ipam-report-worker-pool")
                        .setWorkerPoolSize(5)
                        .setInstances(1);

                Future<String> deployReportWorker = vertx.deployVerticle(() -> new ReportWorkerVerticle(db), reportWorkerOpts);

                // 3. Deploy HTTP Server Verticle on the Event Loop
                DeploymentOptions httpOptions = new DeploymentOptions()
                        .setConfig(config());

                Future<String> deployHttpServer = vertx.deployVerticle(() -> new HttpServerVerticle(db, config, jwtAuthProvider), httpOptions);

                // Wait for all verticles to deploy successfully
                Future.all(deployNetworkWorker, deployReportWorker, deployHttpServer).onComplete(deployAr -> {
                    if (deployAr.succeeded()) {
                        LOGGER.info("===============================================================");
                        LOGGER.info(" Vert.x IPAM System Fully Initialized & Deployed");
                        LOGGER.info(" [1] Event Loop Layer: HttpServerVerticle");
                        LOGGER.info(" [2] Network Worker Pool: 30 Threads (NetworkWorkerVerticle)");
                        LOGGER.info(" [3] Report Worker Pool: 5 Threads (ReportWorkerVerticle)");
                        LOGGER.info(" [4] Messaging Backbone: Vert.x EventBus");
                        LOGGER.info("===============================================================");
                        startPromise.complete();
                    } else {
                        LOGGER.error("Failed to deploy all verticles: {}", deployAr.cause().getMessage(), deployAr.cause());
                        startPromise.fail(deployAr.cause());
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
