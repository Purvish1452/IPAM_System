package com.motadata.ipam.dao;

import com.motadata.ipam.config.AppConfig;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(VertxExtension.class)
public class DaoTest {

    private DatabasePool dbPool;
    private UserDao userDao;
    private SubnetDao subnetDao;
    private AlertDao alertDao;
    private EventDao eventDao;

    @BeforeEach
    public void setUp(Vertx vertx, VertxTestContext testContext) {
        AppConfig.load(vertx).onComplete(testContext.succeeding(config -> {
            FlywayRunner.runMigrations(vertx, config).onComplete(testContext.succeeding(v -> {
                dbPool = new DatabasePool(vertx, config);
                userDao = new UserDao(dbPool.getClient());
                subnetDao = new SubnetDao(dbPool.getClient());
                alertDao = new AlertDao(dbPool.getClient());
                eventDao = new EventDao(dbPool.getClient());
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
    public void testFindUserByAdmin(VertxTestContext testContext) {
        userDao.findByUserName("admin").onComplete(testContext.succeeding(user -> {
            testContext.verify(() -> {
                assertNotNull(user);
                assertEquals("admin", user.getUserName());
                assertNotNull(user.getUserRoleId());
                assertEquals("ROLE_ADMIN", user.getUserRoleId().getRole());
                assertFalse(user.getUserRoleId().getRoleFeaturePermissions().isEmpty());
                testContext.completeNow();
            });
        }));
    }

    @Test
    public void testFindAllSubnets(VertxTestContext testContext) {
        subnetDao.findAllSubnets().onComplete(testContext.succeeding(subnets -> {
            testContext.verify(() -> {
                assertNotNull(subnets);
                testContext.completeNow();
            });
        }));
    }

    @Test
    public void testCountAlerts(VertxTestContext testContext) {
        alertDao.countByStatus(true).onComplete(testContext.succeeding(count -> {
            testContext.verify(() -> {
                assertTrue(count >= 0);
                testContext.completeNow();
            });
        }));
    }

    @Test
    public void testFindAllEvents(VertxTestContext testContext) {
        eventDao.findAllEvents(1, 20).onComplete(testContext.succeeding(events -> {
            testContext.verify(() -> {
                assertNotNull(events);
                testContext.completeNow();
            });
        }));
    }
}
