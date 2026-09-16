package com.motadata.ipam.feature.auth;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(VertxExtension.class)
public class SecurityTest {

    // Tests BCrypt password encoding and hash verification.
    @Test
    public void testPasswordEncoder() {
        String raw = "admin";
        String encoded = PasswordEncoder.encode(raw);

        assertNotNull(encoded);
        assertTrue(PasswordEncoder.matches("admin", encoded));
        assertFalse(PasswordEncoder.matches("wrong_password", encoded));
    }

    // Tests generating and validating JWT tokens with embedded user permissions using Vert.x JSON.
    @Test
    public void testJwtAuthProvider(Vertx vertx, VertxTestContext testContext) {
        JwtAuthProvider jwtAuthProvider = new JwtAuthProvider(vertx);

        List<String> authorities = List.of("ROLE_ADMIN", "PERM_ALERTS_READ", "PERM_SETTINGS_READ");
        String token = jwtAuthProvider.generateToken("admin", authorities);
        assertNotNull(token);
        assertFalse(token.isEmpty());

        jwtAuthProvider.getJwtAuth().authenticate(new io.vertx.ext.auth.authentication.TokenCredentials(token)).onComplete(ar -> {
            testContext.verify(() -> {
                assertTrue(ar.succeeded());
                io.vertx.ext.auth.User authUser = ar.result();
                assertNotNull(authUser);

                JsonObject principal = authUser.principal();
                assertEquals("admin", principal.getString("user_name"));
                assertTrue(principal.getJsonArray("authorities").contains("PERM_ALERTS_READ"));
                assertTrue(principal.getJsonArray("authorities").contains("PERM_SETTINGS_READ"));

                testContext.completeNow();
            });
        });
    }

    // Tests extracting authorities from a pure JsonObject role definition.
    @Test
    public void testExtractAuthoritiesFromJson(Vertx vertx) {
        JwtAuthProvider jwtAuthProvider = new JwtAuthProvider(vertx);

        JsonObject roleJson = new JsonObject()
                .put("role", "ADMIN")
                .put("permissions", new JsonArray()
                        .add(new JsonObject().put("feature", "SUBNET").put("readPermission", true).put("writePermission", true))
                        .add(new JsonObject().put("feature", "DHCP").put("readPermission", true).put("writePermission", false)));

        List<String> authorities = jwtAuthProvider.extractAuthorities(roleJson);
        assertTrue(authorities.contains("ROLE_ADMIN"));
        assertTrue(authorities.contains("PERM_SUBNET_READ"));
        assertTrue(authorities.contains("PERM_SUBNET_WRITE"));
        assertTrue(authorities.contains("PERM_DHCP_READ"));
        assertFalse(authorities.contains("PERM_DHCP_WRITE"));
    }
}
