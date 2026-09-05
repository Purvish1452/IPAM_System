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

    private final Pool pool;

    public PgClientProvider(Vertx vertx, AppConfig config) {
        String host = config.getDbHost();
        int port = config.getDbPort();
        String database = config.getDbName();
        String user = config.getDbUser();
        String password = config.getDbPassword();

        LOGGER.info("Initializing Vert.x PgPool for PostgreSQL database: {}:{}/{}", host, port, database);

        PgConnectOptions connectOptions = new PgConnectOptions()
                .setHost(host)
                .setPort(port)
                .setDatabase(database)
                .setUser(user)
                .setPassword(password)
                .setReconnectAttempts(5)
                .setReconnectInterval(1000);

        PoolOptions poolOptions = new PoolOptions()
                .setMaxSize(20)
                .setMaxWaitQueueSize(100);

        this.pool = PgBuilder.pool()
                .with(poolOptions)
                .connectingTo(connectOptions)
                .using(vertx)
                .build();
        LOGGER.info("Vert.x Reactive PgPool successfully initialized.");
    }


    public Pool getPool() {
        return pool;
    }

    public void close() {
        if (pool != null) {
            pool.close();
            LOGGER.info("Vert.x Reactive PgPool closed.");
        }
    }
}
