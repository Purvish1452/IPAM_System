package com.motadata.ipam;

import com.motadata.ipam.core.config.AppConfig;
import com.motadata.ipam.core.db.DatabaseInit;
import com.motadata.ipam.core.db.PgClientProvider;
import com.motadata.ipam.core.scheduler.JobScheduler;
import com.motadata.ipam.core.verticle.HttpServerVerticle;
import com.motadata.ipam.core.verticle.NetworkWorkerVerticle;
import com.motadata.ipam.core.verticle.ReportWorkerVerticle;
import com.motadata.ipam.feature.auth.JwtAuthProvider;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.ThreadingModel;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.Pool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * JVM Process Entry Point & Central Bootstrap Launcher for the Motadata IPAM Application.
 * <p>
 * Responsibilities:
 * <ul>
 *   <li>Configures Vert.x Engine runtime options (event loops, blocked thread timeouts).</li>
 *   <li>Initializes PostgreSQL connection pool, schema migrations, and JWT auth.</li>
 *   <li>Deploys all 3 Verticles directly (HttpServerVerticle, NetworkWorkerVerticle, ReportWorkerVerticle) via Vert.x Futures.</li>
 *   <li>Initializes background JobScheduler and registers JVM graceful shutdown hooks.</li>
 * </ul>
 */
public class IpamApplication {

    private static final Logger LOGGER = LoggerFactory.getLogger(IpamApplication.class);

    /**
     * Application runtime context holding references to shared resources and deployment IDs.
     */
    public static class AppContext {
        private final PgClientProvider pgClientProvider;
        private final JobScheduler jobScheduler;
        private final List<String> deploymentIds;

        public AppContext(PgClientProvider pgClientProvider, JobScheduler jobScheduler, List<String> deploymentIds) {
            this.pgClientProvider = pgClientProvider;
            this.jobScheduler = jobScheduler;
            this.deploymentIds = deploymentIds != null ? deploymentIds : new ArrayList<>();
        }

        public PgClientProvider getPgClientProvider() {
            return pgClientProvider;
        }

        public JobScheduler getJobScheduler() {
            return jobScheduler;
        }

        public List<String> getDeploymentIds() {
            return deploymentIds;
        }

        public void close() {
            if (jobScheduler != null) {
                jobScheduler.stop();
            }
            if (pgClientProvider != null) {
                pgClientProvider.close();
            }
        }
    }

    /**
     * Bootstraps application configuration, database pool, security provider, schema,
     * and deploys all 3 functional verticles directly using Vert.x Futures.
     */
    public static Future<AppContext> bootstrap(Vertx vertx, JsonObject configOverrides) {
        ReportWorkerVerticle.silenceThirdPartyLoggers();
        LOGGER.info("Starting Vert.x IPAM Bootstrap Engine (Option A: 3 Independent Verticles)...");

        return AppConfig.load(vertx, configOverrides).compose(config -> {
            // 1. Initialize PostgreSQL Reactive Connection Pool
            PgClientProvider pgClientProvider = new PgClientProvider(vertx, config);
            Pool db = pgClientProvider.getPool();

            // 2. Initialize Security Provider
            JwtAuthProvider jwtAuthProvider = new JwtAuthProvider(vertx);

            // 3. Initialize PostgreSQL Schema & Seed Data
            return DatabaseInit.initSchema(vertx, db).compose(v -> {
                // 4. Configure Deployment Options for each Verticle
                DeploymentOptions networkWorkerOpts = new DeploymentOptions()
                        .setThreadingModel(ThreadingModel.WORKER)
                        .setWorkerPoolName("ipam-network-worker-pool")
                        .setWorkerPoolSize(30)
                        .setInstances(Runtime.getRuntime().availableProcessors() * 2);

                DeploymentOptions reportWorkerOpts = new DeploymentOptions()
                        .setThreadingModel(ThreadingModel.WORKER)
                        .setWorkerPoolName("ipam-report-worker-pool")
                        .setWorkerPoolSize(5)
                        .setInstances(Runtime.getRuntime().availableProcessors() * 2);

                DeploymentOptions httpOptions = new DeploymentOptions()
                        .setConfig(configOverrides != null ? configOverrides : new JsonObject())
                        .setInstances(Math.min(5,Runtime.getRuntime().availableProcessors()));

                // 5. Deploy all 3 Verticles in parallel using Vert.x Futures
                Future<String> deployNetworkWorker = vertx.deployVerticle(() -> new NetworkWorkerVerticle(db), networkWorkerOpts);
                Future<String> deployReportWorker = vertx.deployVerticle(() -> new ReportWorkerVerticle(db), reportWorkerOpts);
                Future<String> deployHttpServer = vertx.deployVerticle(() -> new HttpServerVerticle(db, config, jwtAuthProvider), httpOptions);

                return Future.all(deployNetworkWorker, deployReportWorker, deployHttpServer)
                        .map(composite -> {
                            // 6. Start background job scheduler after all verticles are running
                            JobScheduler jobScheduler = new JobScheduler(vertx);
                            jobScheduler.start();

                            List<String> deploymentIds = new ArrayList<>();
                            deploymentIds.add(composite.resultAt(0));
                            deploymentIds.add(composite.resultAt(1));
                            deploymentIds.add(composite.resultAt(2));

                            LOGGER.info("===============================================================");
                            LOGGER.info(" Vert.x IPAM System Fully Initialized & Deployed");
                            LOGGER.info(" [1] Event Loop Layer: HttpServerVerticle (Deployment ID: {})", deploymentIds.get(2));
                            LOGGER.info(" [2] Network Worker Pool: 30 Threads (Deployment ID: {})", deploymentIds.get(0));
                            LOGGER.info(" [3] Report Worker Pool: 5 Threads (Deployment ID: {})", deploymentIds.get(1));
                            LOGGER.info(" [4] Messaging Backbone: Vert.x EventBus");
                            LOGGER.info(" [5] Background Scheduler: Active (JobScheduler)");
                            LOGGER.info("===============================================================");

                            return new AppContext(pgClientProvider, jobScheduler, deploymentIds);
                        });
            });
        });
    }

     /**
     * JVM Process entry point. Creates Vertx runtime, boots all verticles, and handles graceful shutdown.
     */
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

        // 4. Bootstrap and deploy all verticles directly
        LOGGER.info("Bootstrapping Vert.x 5 IPAM System directly from IpamApplication...");
        bootstrap(vertx, null).onComplete(ar -> {
            if (ar.succeeded()) {
                AppContext appContext = ar.result();
                LOGGER.info("Vert.x 5 IPAM Application successfully booted with all verticles deployed.");

                // Register JVM Graceful Shutdown Hook
                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                    LOGGER.info("Graceful shutdown signal received. Closing resources and Vert.x instance...");
                    if (appContext != null) {
                        appContext.close();
                    }
                    vertx.close().onComplete(closeAr -> LOGGER.info("Vert.x instance stopped cleanly."));
                }));
            } else {
                LOGGER.error("Failed to start Vert.x 5 IPAM Application: {}", ar.cause().getMessage(), ar.cause());
                System.exit(1);
            }
        });
    }
}
