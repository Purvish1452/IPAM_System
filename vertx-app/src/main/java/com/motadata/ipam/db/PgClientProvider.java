package com.motadata.ipam.db;

import com.motadata.ipam.config.AppConfig;
import io.vertx.core.Vertx;
import io.vertx.pgclient.PgBuilder;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.PoolOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Singleton / Lifecycle Provider for the Vert.x Reactive PostgreSQL Connection Pool.
 */
public class PgClientProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(PgClientProvider.class);

    // PostgreSQL connection pool used by the application
    private final Pool pool;

    // Initializes the reactive PostgreSQL connection pool using application configuration.
    public PgClientProvider(Vertx vertx, AppConfig config) {

        // Read database connection details from application configuration
        String host = config.getDbHost();
        if (host == null || "localhost".equalsIgnoreCase(host.trim())) {
            host = "127.0.0.1";
        }
        int port = config.getDbPort();
        String database = config.getDbName();
        String user = config.getDbUser();
        String password = config.getDbPassword();

        LOGGER.info("Initializing Vert.x PgPool for PostgreSQL database: {}:{}/{}", host, port, database);

        // Configure PostgreSQL connection settings
        PgConnectOptions connectOptions = new PgConnectOptions()
                .setHost(host)
                .setPort(port)
                .setDatabase(database)
                .setUser(user)
                .setPassword(password)
                .setReconnectAttempts(5)
                .setReconnectInterval(1000);

        // Configure connection pool size and waiting queue
        PoolOptions poolOptions = new PoolOptions()
                .setMaxSize(20)
                .setMaxWaitQueueSize(100);

        // Create the connection pool with all details
        this.pool = PgBuilder.pool()
                .with(poolOptions)
                .connectingTo(connectOptions)
                .using(vertx)
                .build();
        LOGGER.info("Vert.x Reactive PgPool successfully initialized.");
    }


    // Return the PostgreSQL connection pool to user for all files
    public Pool getPool() {
        return pool;
    }

    // Close the connection pool during application shutdown
    public void close() {
        if (pool != null) {
            pool.close();
            LOGGER.info("Vert.x Reactive PgPool closed.");
        }
    }
}
