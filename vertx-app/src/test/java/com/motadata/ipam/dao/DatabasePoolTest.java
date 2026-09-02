package com.motadata.ipam.dao;

import com.motadata.ipam.config.AppConfig;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(VertxExtension.class)
public class DatabasePoolTest {

    private DatabasePool dbPool;

    @BeforeEach
    public void setUp(Vertx vertx, VertxTestContext testContext) {
        AppConfig.load(vertx).onComplete(testContext.succeeding(config -> {
            FlywayRunner.runMigrations(vertx, config).onComplete(testContext.succeeding(v -> {
                dbPool = new DatabasePool(vertx, config);
                testContext.completeNow();
            }));
        }));
    }

    @AfterEach
    public void tearDown() {
        if (dbPool != null) {
            dbPool.close();
        }
    }

    @Test
    public void testDatabaseConnection(VertxTestContext testContext) {
        assertNotNull(dbPool);
        dbPool.testConnection().onComplete(testContext.succeeding(v -> {
            testContext.completeNow();
        }));
    }
}
