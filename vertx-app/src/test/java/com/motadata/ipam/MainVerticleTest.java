package com.motadata.ipam;

import com.motadata.ipam.model.User;
import com.motadata.ipam.model.UserRole;
import com.motadata.ipam.security.JwtAuthProvider;
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

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(VertxExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class MainVerticleTest {

    private WebClient webClient;
    private JwtAuthProvider jwtAuthProvider;
    private String deploymentId;

    @BeforeAll
    public void setUpAll(Vertx vertx, VertxTestContext testContext) {
        webClient = WebClient.create(vertx);
        jwtAuthProvider = new JwtAuthProvider(vertx);

        vertx.deployVerticle(new MainVerticle(), ar -> {
            if (ar.succeeded()) {
                deploymentId = ar.result();
                testContext.completeNow();
            } else {
                testContext.failNow(ar.cause());
            }
        });
    }

    @AfterAll
    public void tearDownAll(Vertx vertx, VertxTestContext testContext) {
        if (webClient != null) {
            webClient.close();
        }
        if (deploymentId != null) {
            vertx.undeploy(deploymentId, ar -> testContext.completeNow());
        } else {
            testContext.completeNow();
        }
    }

    @Test
    public void testGetIndexPage(VertxTestContext testContext) {
        webClient.get(8080, "localhost", "/")
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

    @Test
    public void testValidatePermissionEndpoint(VertxTestContext testContext) {
        User user = new User(1L, "admin", "admin@motadata.com", true);
        user.setUserRoleId(new UserRole(1L, "ROLE_ADMIN", "Administrator"));
        String token = jwtAuthProvider.generateToken(user);

        webClient.get(8080, "localhost", "/validatePermission/")
                .putHeader("accessToken", token)
                .send()
                .onComplete(ar -> {
                    if (ar.succeeded()) {
                        try {
                            assertEquals(200, ar.result().statusCode());
                            assertTrue(ar.result().bodyAsJsonObject().getBoolean("success"));
                            assertEquals("ROLE_ROLE_ADMIN", ar.result().bodyAsJsonObject().getString("currentUserRole"));
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
    public void testLoginFlow(VertxTestContext testContext) {
        JsonObject body = new JsonObject()
                .put("userName", "admin")
                .put("password", "admin");

        webClient.post(8080, "localhost", "/loginUser.html")
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
