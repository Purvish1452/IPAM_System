package com.motadata.ipam.db;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.sqlclient.Pool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Ensures PostgreSQL tables and seed data are initialized if they do not exist.
 */
public class DatabaseInit {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseInit.class);

    // Verifies schema existence, applies migrations, or executes SQL initialization script.
    public static Future<Void> initSchema(Vertx vertx, Pool pool) {
        return pool.query("SELECT 1 FROM information_schema.tables WHERE table_name = 'users'").execute()
                .compose(rows -> {
                    if (rows.size() > 0) {
                        LOGGER.info("PostgreSQL database tables already present. Schema check passed.");
                        String migrationSql = "ALTER TABLE gateway ADD COLUMN IF NOT EXISTS name VARCHAR(100); " +
                                "ALTER TABLE gateway ADD COLUMN IF NOT EXISTS previous_scan TIMESTAMP; " +
                                "ALTER TABLE gateway ADD COLUMN IF NOT EXISTS status VARCHAR(50) DEFAULT 'Active'; " +
                                "ALTER TABLE discovered_subnet ADD COLUMN IF NOT EXISTS subnet VARCHAR(100); " +
                                "ALTER TABLE discovered_subnet ADD COLUMN IF NOT EXISTS gateway VARCHAR(100); " +
                                "ALTER TABLE ip_requests ADD COLUMN IF NOT EXISTS device_type VARCHAR(100) DEFAULT 'Server'; " +
                                "ALTER TABLE ip_requests ADD COLUMN IF NOT EXISTS duration VARCHAR(100) DEFAULT 'Permanent'; " +
                                "ALTER TABLE ip_requests ADD COLUMN IF NOT EXISTS ips TEXT; " +
                                "ALTER TABLE ip_requests ADD COLUMN IF NOT EXISTS preferred_subnet BOOLEAN DEFAULT FALSE; " +
                                "ALTER TABLE ip_requests ADD COLUMN IF NOT EXISTS last_modified_by VARCHAR(100); " +
                                "ALTER TABLE ip_requests ADD COLUMN IF NOT EXISTS last_modified_date TIMESTAMP; " +
                                "UPDATE gateway SET name = COALESCE(name, description, 'Core Gateway Router'), status = COALESCE(status, 'Active'), previous_scan = COALESCE(previous_scan, CURRENT_TIMESTAMP); " +
                                "UPDATE discovered_subnet SET subnet = COALESCE(subnet, subnet_address), gateway = COALESCE(gateway, '192.168.1.1'); " +
                                "UPDATE ip_requests SET device_type = COALESCE(device_type, 'Server'), duration = COALESCE(duration, 'Permanent') WHERE device_type IS NULL; " +
                                "CREATE UNIQUE INDEX IF NOT EXISTS subnet_ip_details_ip_address_uq ON subnet_ip_details (ip_address);";

                        return pool.query(migrationSql).execute()
                                .onSuccess(v -> LOGGER.info("Schema migration for gateway and discovered_subnet applied successfully."))
                                .onFailure(err -> LOGGER.warn("Schema migration warning: {}", err.getMessage()))
                                .mapEmpty();
                    } else {
                        LOGGER.info("Initializing PostgreSQL schema and seed data from init_ipam_postgres.sql...");
                        return vertx.fileSystem().readFile("db/init_ipam_postgres.sql")
                                .compose(buffer -> {
                                    String sql = buffer.toString();
                                    return pool.query(sql).execute()
                                            .onSuccess(v -> LOGGER.info("PostgreSQL schema initialized successfully."))
                                            .onFailure(execErr -> LOGGER.warn("Failed executing init_ipam_postgres.sql (tables may already exist): {}", execErr.getMessage()))
                                            .mapEmpty();
                                })
                                .onFailure(fileErr -> LOGGER.warn("Could not read db/init_ipam_postgres.sql file: {}", fileErr.getMessage()))
                                .mapEmpty();
                    }
                });
    }
}
