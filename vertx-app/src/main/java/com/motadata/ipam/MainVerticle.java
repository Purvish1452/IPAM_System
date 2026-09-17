package com.motadata.ipam;

import com.motadata.ipam.core.config.AppConfig;
import com.motadata.ipam.core.db.DatabaseInit;
import com.motadata.ipam.core.db.PgClientProvider;
import com.motadata.ipam.core.scheduler.JobScheduler;
import com.motadata.ipam.core.verticle.HttpServerVerticle;
import com.motadata.ipam.core.verticle.NetworkWorkerVerticle;
import com.motadata.ipam.core.verticle.ReportWorkerVerticle;
import com.motadata.ipam.feature.auth.JwtAuthProvider;
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


    // Initializes configurations, database pool, scheduler, and deploys all application verticles.
    @Override
    public void start(Promise<Void> startPromise) {
        ReportWorkerVerticle.silenceThirdPartyLoggers();
        LOGGER.info("Starting Vert.x IPAM Main Deployer (Reactive EventBus Engine)...");

        AppConfig.load(vertx, config()).compose(config -> {
            // Initialize PostgreSQL Reactive Connection Pool
            pgClientProvider = new PgClientProvider(vertx, config);
            Pool db = pgClientProvider.getPool();

            // Initialize Security Provider
            JwtAuthProvider jwtAuthProvider = new JwtAuthProvider(vertx);


            // Initialize PostgreSQL Schema & Seed Data
            return DatabaseInit.initSchema(vertx, db).compose(v -> {
                // 1. Deploy Network Discovery Verticle (2 Instances on EventLoop, dispatches to dedicated 30-thread WorkerExecutor)
                DeploymentOptions networkWorkerOpts = new DeploymentOptions()
                        .setInstances(Runtime.getRuntime().availableProcessors() * 2);

                Future<String> deployNetworkWorker = vertx.deployVerticle(() -> new NetworkWorkerVerticle(db), networkWorkerOpts);

                // 2. Deploy Report Verticle (1 Instance on EventLoop, dispatches to dedicated 5-thread WorkerExecutor)
                DeploymentOptions reportWorkerOpts = new DeploymentOptions()
                        .setInstances(Runtime.getRuntime().availableProcessors() * 2);

                Future<String> deployReportWorker = vertx.deployVerticle(() -> new ReportWorkerVerticle(db), reportWorkerOpts);

                // 3. Deploy HTTP Server Verticle on the Event Loop
                DeploymentOptions httpOptions = new DeploymentOptions()
                        .setConfig(config())
                        .setInstances(Runtime.getRuntime().availableProcessors() * 2);

                Future<String> deployHttpServer = vertx.deployVerticle(() -> new HttpServerVerticle(db, config, jwtAuthProvider), httpOptions);

                // Wait for all verticles to deploy successfully
                return Future.all(deployNetworkWorker, deployReportWorker, deployHttpServer)
                        .mapEmpty()
                        .onSuccess(x -> {
                            // Start background job scheduler after all verticles are running
                            jobScheduler = new JobScheduler(vertx);
                            jobScheduler.start();

                            LOGGER.info("===============================================================");
                            LOGGER.info(" Vert.x IPAM System Fully Initialized & Deployed");
                            LOGGER.info(" [1] Event Loop Layer: HttpServerVerticle");
                            LOGGER.info(" [2] Network Worker Pool: 30 Threads (NetworkWorkerVerticle)");
                            LOGGER.info(" [3] Report Worker Pool: 5 Threads (ReportWorkerVerticle)");
                            LOGGER.info(" [4] Messaging Backbone: Vert.x EventBus");
                            LOGGER.info(" [5] Background Scheduler: Active (JobScheduler)");
                            LOGGER.info("===============================================================");
                        });
            });
        }).onSuccess(v -> startPromise.complete())
          .onFailure(startPromise::fail);
    }

    // Stops background scheduler jobs and closes the database connection pool on shutdown.
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
