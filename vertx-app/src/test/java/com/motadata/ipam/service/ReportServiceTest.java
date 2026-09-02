package com.motadata.ipam.service;

import com.motadata.ipam.config.AppConfig;
import com.motadata.ipam.dao.*;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(VertxExtension.class)
public class ReportServiceTest {

    private DatabasePool dbPool;
    private ReportService reportService;

    @BeforeEach
    public void setUp(Vertx vertx, VertxTestContext testContext) {
        AppConfig.load(vertx).onComplete(configAr -> {
            if (configAr.succeeded()) {
                AppConfig config = configAr.result();
                FlywayRunner.runMigrations(vertx, config).onComplete(flywayAr -> {
                    dbPool = new DatabasePool(vertx, config);
                    SubnetDao subnetDao = new SubnetDao(dbPool.getClient());
                    AlertDao alertDao = new AlertDao(dbPool.getClient());
                    EventDao eventDao = new EventDao(dbPool.getClient());
                    DhcpDao dhcpDao = new DhcpDao(dbPool.getClient());

                    reportService = new ReportService(vertx, subnetDao, alertDao, eventDao, dhcpDao);
                    testContext.completeNow();
                });
            } else {
                testContext.failNow(configAr.cause());
            }
        });
    }

    @AfterEach
    public void tearDown() {
        if (dbPool != null) {
            dbPool.close();
        }
    }

    @Test
    public void testGenerateSubnetPdfReport(VertxTestContext testContext) {
        reportService.generateSubnetPdfReport().onComplete(ar -> {
            if (ar.succeeded()) {
                try {
                    byte[] pdfBytes = ar.result();
                    assertNotNull(pdfBytes);
                    assertTrue(pdfBytes.length > 0);
                    // Verify PDF header magic bytes %PDF
                    String pdfHeader = new String(pdfBytes, 0, Math.min(4, pdfBytes.length));
                    assertEquals("%PDF", pdfHeader);
                    testContext.completeNow();
                } catch (Throwable t) {
                    testContext.failNow(t);
                }
            } else {
                testContext.failNow(ar.cause());
            }
        });
    }

    @Test
    public void testGenerateAlertPdfReport(VertxTestContext testContext) {
        reportService.generateAlertPdfReport().onComplete(ar -> {
            if (ar.succeeded()) {
                try {
                    byte[] pdfBytes = ar.result();
                    assertNotNull(pdfBytes);
                    assertTrue(pdfBytes.length > 0);
                    String pdfHeader = new String(pdfBytes, 0, Math.min(4, pdfBytes.length));
                    assertEquals("%PDF", pdfHeader);
                    testContext.completeNow();
                } catch (Throwable t) {
                    testContext.failNow(t);
                }
            } else {
                testContext.failNow(ar.cause());
            }
        });
    }

    @Test
    public void testGenerateEventPdfReport(VertxTestContext testContext) {
        reportService.generateEventPdfReport().onComplete(ar -> {
            if (ar.succeeded()) {
                try {
                    byte[] pdfBytes = ar.result();
                    assertNotNull(pdfBytes);
                    assertTrue(pdfBytes.length > 0);
                    String pdfHeader = new String(pdfBytes, 0, Math.min(4, pdfBytes.length));
                    assertEquals("%PDF", pdfHeader);
                    testContext.completeNow();
                } catch (Throwable t) {
                    testContext.failNow(t);
                }
            } else {
                testContext.failNow(ar.cause());
            }
        });
    }

    @Test
    public void testGenerateDhcpPdfReport(VertxTestContext testContext) {
        reportService.generateDhcpPdfReport().onComplete(ar -> {
            if (ar.succeeded()) {
                try {
                    byte[] pdfBytes = ar.result();
                    assertNotNull(pdfBytes);
                    assertTrue(pdfBytes.length > 0);
                    String pdfHeader = new String(pdfBytes, 0, Math.min(4, pdfBytes.length));
                    assertEquals("%PDF", pdfHeader);
                    testContext.completeNow();
                } catch (Throwable t) {
                    testContext.failNow(t);
                }
            } else {
                testContext.failNow(ar.cause());
            }
        });
    }
}
