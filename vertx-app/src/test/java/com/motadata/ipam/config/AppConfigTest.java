package com.motadata.ipam.config;

import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(VertxExtension.class)
public class AppConfigTest {

    @Test
    public void testLoadAppConfig(Vertx vertx, VertxTestContext testContext) {
        AppConfig.load(vertx).onComplete(testContext.succeeding(config -> {
            testContext.verify(() -> {
                assertNotNull(config);
                assertEquals(8080, config.getServerPort());
                assertEquals("localhost", config.getServerHost());
                assertEquals("localhost", config.getDbHost());
                assertEquals(3306, config.getDbPort());
                testContext.completeNow();
            });
        }));
    }
}
