package com.motadata.ipam.db;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.sqlclient.Pool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Ensures PostgreSQL tables and seed data are initialized if they do not exist.
 */
public class DatabaseInit {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseInit.class);

    public static Future<Void> initSchema(Vertx vertx, Pool pool) {
        Promise<Void> promise = Promise.promise();

        // Check if users table exists
        pool.query("SELECT 1 FROM information_schema.tables WHERE table_name = 'users'").execute(ar -> {
            if (ar.succeeded() && ar.result().size() > 0) {
                LOGGER.info("PostgreSQL database tables already present. Schema check passed.");
                pool.query("CREATE UNIQUE INDEX IF NOT EXISTS subnet_ip_details_ip_address_uq " +
                        "ON subnet_ip_details (ip_address)").execute(indexAr -> {
                    if (indexAr.failed()) {
                        LOGGER.warn("Could not ensure unique IP address index: {}", indexAr.cause().getMessage());
                    }
                    promise.complete();
                });
            } else {
                LOGGER.info("Initializing PostgreSQL schema and seed data from init_ipam_postgres.sql...");
                vertx.fileSystem().readFile("db/init_ipam_postgres.sql", fileAr -> {
                    if (fileAr.succeeded()) {
                        String sql = fileAr.result().toString();
                        pool.query(sql).execute(execAr -> {
                            if (execAr.succeeded()) {
                                LOGGER.info("PostgreSQL schema initialized successfully.");
                                promise.complete();
                            } else {
                                LOGGER.warn("Failed executing init_ipam_postgres.sql (tables may already exist): {}", execAr.cause().getMessage());
                                promise.complete();
                            }
                        });
                    } else {
                        LOGGER.warn("Could not read db/init_ipam_postgres.sql file: {}", fileAr.cause().getMessage());
                        promise.complete();
                    }
                });
            }
        });

        return promise.future();
    }
}
