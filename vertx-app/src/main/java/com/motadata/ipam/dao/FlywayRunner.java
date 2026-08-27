package com.motadata.ipam.dao;

import com.motadata.ipam.config.AppConfig;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.flywaydb.core.Flyway;

/**
 * Executes Flyway database schema migrations off the Vert.x event loop.
 */
public class FlywayRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(FlywayRunner.class);

    public static Future<Void> runMigrations(Vertx vertx, AppConfig config) {
        Promise<Void> promise = Promise.promise();

        vertx.executeBlocking(blockingPromise -> {
            try {
                String jdbcUrl = "jdbc:mysql://" + config.getDbHost() + ":" + config.getDbPort() +
                        "/" + config.getDbName() + "?useSSL=false&allowPublicKeyRetrieval=true";
                LOGGER.info("Starting Flyway database migrations for JDBC URL: {}", jdbcUrl);

                Flyway flyway = Flyway.configure()
                        .dataSource(jdbcUrl, config.getDbUser(), config.getDbPassword())
                        .locations("classpath:db/migration")
                        .baselineOnMigrate(true)
                        .load();

                try {
                    flyway.repair();
                } catch (Exception e) {
                    LOGGER.debug("Flyway repair note: {}", e.getMessage());
                }

                flyway.migrate();
                LOGGER.info("Flyway database migrations completed successfully.");
                blockingPromise.complete();
            } catch (Exception e) {
                LOGGER.error("Error executing Flyway migrations: {}", e.getMessage(), e);
                blockingPromise.fail(e);
            }
        }, ar -> {
            if (ar.succeeded()) {
                promise.complete();
            } else {
                promise.fail(ar.cause());
            }
        });

        return promise.future();
    }
}
