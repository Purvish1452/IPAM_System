package com.motadata.ipam.dao;

import com.motadata.ipam.config.AppConfig;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.mysqlclient.MySQLConnectOptions;
import io.vertx.mysqlclient.MySQLPool;
import io.vertx.sqlclient.PoolOptions;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

/**
 * Manages the Vert.x MySQLPool reactive non-blocking database connection pool.
 */
public class DatabasePool {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatabasePool.class);

    private final MySQLPool client;

    public DatabasePool(Vertx vertx, AppConfig config) {
        MySQLConnectOptions connectOptions = new MySQLConnectOptions()
                .setHost(config.getDbHost())
                .setPort(config.getDbPort())
                .setDatabase(config.getDbName())
                .setUser(config.getDbUser())
                .setPassword(config.getDbPassword());

        PoolOptions poolOptions = new PoolOptions()
                .setMaxSize(10);

        this.client = MySQLPool.pool(vertx, connectOptions, poolOptions);
        LOGGER.info("Initialized Vert.x MySQLPool for database {}@{}:{}", config.getDbName(), config.getDbHost(), config.getDbPort());
    }

    public MySQLPool getClient() {
        return client;
    }

    public Future<Void> testConnection() {
        Promise<Void> promise = Promise.promise();
        client.query("SELECT 1").execute(ar -> {
            if (ar.succeeded()) {
                LOGGER.info("Vert.x MySQLPool connection test succeeded.");
                promise.complete();
            } else {
                LOGGER.error("Vert.x MySQLPool connection test failed: {}", ar.cause().getMessage());
                promise.fail(ar.cause());
            }
        });
        return promise.future();
    }

    public void close() {
        if (client != null) {
            client.close();
            LOGGER.info("Closed Vert.x MySQLPool.");
        }
    }
}
