package com.motadata.ipam.service;

import com.motadata.ipam.config.AppConfig;
import com.motadata.ipam.dao.*;
import com.motadata.ipam.security.JwtAuthProvider;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(VertxExtension.class)
public class ServiceTest {

    private DatabasePool dbPool;
    private UserService userService;
    private SubnetService subnetService;
    private AlertService alertService;
    private EventService eventService;

    @BeforeEach
    public void setUp(Vertx vertx, VertxTestContext testContext) {
        AppConfig.load(vertx).onComplete(testContext.succeeding(config -> {
            FlywayRunner.runMigrations(vertx, config).onComplete(testContext.succeeding(v -> {
                dbPool = new DatabasePool(vertx, config);
                JwtAuthProvider jwtAuthProvider = new JwtAuthProvider(vertx);

                UserDao userDao = new UserDao(dbPool.getClient());
                SubnetDao subnetDao = new SubnetDao(dbPool.getClient());
                AlertDao alertDao = new AlertDao(dbPool.getClient());
                EventDao eventDao = new EventDao(dbPool.getClient());

                userService = new UserService(userDao, jwtAuthProvider);
                subnetService = new SubnetService(subnetDao);
                alertService = new AlertService(alertDao);
                eventService = new EventService(eventDao);

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
    public void testUserAuthentication(VertxTestContext testContext) {
        userService.authenticate("admin", "admin").onComplete(testContext.succeeding(result -> {
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
                assertNotNull(result.getJsonObject("data"));
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
                assertNotNull(result.getJsonObject("data"));
                testContext.completeNow();
            });
        }));
    }

    @Test
    public void testSubnetService(VertxTestContext testContext) {
        subnetService.getAllSubnets().onComplete(testContext.succeeding(subnets -> {
            testContext.verify(() -> {
                assertNotNull(subnets);
                testContext.completeNow();
            });
        }));
    }
}
