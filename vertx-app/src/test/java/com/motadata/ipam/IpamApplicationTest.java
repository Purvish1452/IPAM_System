package com.motadata.ipam;

import com.motadata.ipam.feature.auth.JwtAuthProvider;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(VertxExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class IpamApplicationTest {

    private static final int TEST_PORT = 8089;
    private WebClient webClient;
    private JwtAuthProvider jwtAuthProvider;
    private IpamApplication.AppContext appContext;

    // Bootstraps IpamApplication and initializes the test HTTP WebClient and JWT provider.
    @BeforeAll
    public void setUpAll(Vertx vertx, VertxTestContext testContext) {
        webClient = WebClient.create(vertx);
        jwtAuthProvider = new JwtAuthProvider(vertx);

        JsonObject configOverrides = new JsonObject().put("server-port", TEST_PORT);

        IpamApplication.bootstrap(vertx, configOverrides).onComplete(ar -> {
            if (ar.succeeded()) {
                appContext = ar.result();
                testContext.completeNow();
            } else {
                testContext.failNow(ar.cause());
            }
        });
    }

    // Closes the WebClient and stops the application context after all tests complete.
    @AfterAll
    public void tearDownAll(Vertx vertx, VertxTestContext testContext) {
        if (webClient != null) {
            webClient.close();
        }
        if (appContext != null) {
            appContext.close();
        }
        testContext.completeNow();
    }

    // Tests accessing the root index page and verifying the HTML title content.
    @Test
    public void testGetIndexPage(VertxTestContext testContext) {
        webClient.get(TEST_PORT, "localhost", "/")
                .send()
                .onComplete(ar -> {
                    if (ar.succeeded()) {
                        try {
                            assertEquals(200, ar.result().statusCode());
                            assertTrue(ar.result().bodyAsString().contains("IP Address Manager"));
                            testContext.completeNow();
                        } catch (Throwable t) {
                            testContext.failNow(t);
                        }
                    } else {
                        testContext.failNow(ar.cause());
                    }
                });
    }

    // Tests permission validation endpoint using a signed JWT token.
    @Test
    public void testValidatePermissionEndpoint(VertxTestContext testContext) {
        String token = jwtAuthProvider.generateToken("admin", List.of("ROLE_ADMIN"));

        webClient.get(TEST_PORT, "localhost", "/validatePermission/")
                .putHeader("accessToken", token)
                .send()
                .onComplete(ar -> {
                    if (ar.succeeded()) {
                        try {
                            assertEquals(200, ar.result().statusCode());
                            assertTrue(ar.result().bodyAsJsonObject().getBoolean("success"));
                            assertEquals("ROLE_ADMIN", ar.result().bodyAsJsonObject().getString("currentUserRole"));
                            testContext.completeNow();
                        } catch (Throwable t) {
                            testContext.failNow(t);
                        }
                    } else {
                        testContext.failNow(ar.cause());
                    }
                });
    }

    // Tests user login authentication flow, redirect headers, and session cookie generation.
    @Test
    public void testLoginFlow(VertxTestContext testContext) {
        JsonObject body = new JsonObject()
                .put("userName", "admin")
                .put("password", "admin");

        webClient.post(TEST_PORT, "localhost", "/loginUser.html")
                .followRedirects(false)
                .sendJsonObject(body)
                .onComplete(ar -> {
                    if (ar.succeeded()) {
                        try {
                            assertEquals(302, ar.result().statusCode(), "Unexpected status code: " + ar.result().statusCode() + " Body: " + ar.result().bodyAsString());
                            assertEquals("/loadHomePage", ar.result().getHeader("Location"));
                            assertNotNull(ar.result().cookies());
                            assertFalse(ar.result().cookies().isEmpty());
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
