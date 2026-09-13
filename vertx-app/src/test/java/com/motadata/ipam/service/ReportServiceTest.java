package com.motadata.ipam.service;

import com.motadata.ipam.config.AppConfig;
import com.motadata.ipam.db.DatabaseInit;
import com.motadata.ipam.db.PgClientProvider;
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
public class ReportServiceTest {

    private PgClientProvider pgClientProvider;
    private ReportService reportService;

    // Sets up Database schema and ReportService before each test.
    @BeforeEach
    public void setUp(Vertx vertx, VertxTestContext testContext) {
        AppConfig.load(vertx).onComplete(configAr -> {
            if (configAr.succeeded()) {
                AppConfig config = configAr.result();
                pgClientProvider = new PgClientProvider(vertx, config);
                Pool db = pgClientProvider.getPool();

                DatabaseInit.initSchema(vertx, db).onComplete(initAr -> {
                    reportService = new ReportService(vertx, db);
                    testContext.completeNow();
                });
            } else {
                testContext.failNow(configAr.cause());
            }
        });
    }

    // Cleans up the database pool connection after each test.
    @AfterEach
    public void tearDown() {
        if (pgClientProvider != null) {
            pgClientProvider.close();
        }
    }

    // Tests generating Subnet PDF report bytes with valid PDF magic headers.
    @Test
    public void testGenerateSubnetPdfReport(VertxTestContext testContext) {
        reportService.generateSubnetPdfReport().onComplete(ar -> {
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

    // Tests generating Alert PDF report bytes with valid PDF magic headers.
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

    // Tests generating Event PDF report bytes with valid PDF magic headers.
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

    // Tests generating DHCP PDF report bytes with valid PDF magic headers.
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
