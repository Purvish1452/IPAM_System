package com.motadata.ipam.service;

import com.motadata.ipam.config.AppConfig;
import com.motadata.ipam.db.DatabaseInit;
import com.motadata.ipam.db.PgClientProvider;
import com.motadata.ipam.security.JwtAuthProvider;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import io.vertx.sqlclient.Pool;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(VertxExtension.class)
public class ServiceTest {

    private PgClientProvider pgClientProvider;
    private UserService userService;
    private SubnetService subnetService;
    private AlertService alertService;
    private EventService eventService;

    @BeforeEach
    public void setUp(Vertx vertx, VertxTestContext testContext) {
        AppConfig.load(vertx).onComplete(testContext.succeeding(config -> {
            pgClientProvider = new PgClientProvider(vertx, config);
            Pool db = pgClientProvider.getPool();

            DatabaseInit.initSchema(vertx, db).onComplete(testContext.succeeding(v -> {
                JwtAuthProvider jwtAuthProvider = new JwtAuthProvider(vertx);

                userService = new UserService(db, jwtAuthProvider);
                subnetService = new SubnetService(db);
                alertService = new AlertService(db);
                eventService = new EventService(db);

                testContext.completeNow();
            }));
        }));
    }

    @AfterEach
    public void tearDown() {
        if (pgClientProvider != null) {
            pgClientProvider.close();
        }
    }

    @Test
    public void testUserAuthentication(VertxTestContext testContext) {
        userService.authenticate("admin", "admin123").onComplete(testContext.succeeding(result -> {
            testContext.verify(() -> {
                assertNotNull(result);
                assertTrue(result.getBoolean("success"));
                assertNotNull(result.getString("token"));
                assertEquals("admin", result.getString("username"));
                testContext.completeNow();
            });
        }));
    }

    @Test
    public void testAlertService(VertxTestContext testContext) {
        alertService.getAlerts(null, 1, 20).onComplete(testContext.succeeding(result -> {
            testContext.verify(() -> {
                assertNotNull(result);
                assertTrue(result.getBoolean("success"));
                assertNotNull(result.getJsonArray("data"));
                testContext.completeNow();
            });
        }));
    }

    @Test
    public void testEventService(VertxTestContext testContext) {
        eventService.getEvents(1, 20).onComplete(testContext.succeeding(result -> {
            testContext.verify(() -> {
                assertNotNull(result);
                assertTrue(result.getBoolean("success"));
                assertNotNull(result.getJsonArray("data"));
                testContext.completeNow();
            });
        }));
    }

    @Test
    public void testSubnetService(VertxTestContext testContext) {
        subnetService.getAllSubnets().onComplete(testContext.succeeding(subnets -> {
            testContext.verify(() -> {
                assertNotNull(subnets);
                assertTrue(subnets.size() > 0);
                testContext.completeNow();
            });
        }));
    }
}
