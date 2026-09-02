package com.motadata.ipam.security;

import com.motadata.ipam.model.Feature;
import com.motadata.ipam.model.RoleFeaturePermission;
import com.motadata.ipam.model.User;
import com.motadata.ipam.model.UserRole;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(VertxExtension.class)
public class SecurityTest {

    @Test
    public void testPasswordEncoder() {
        String raw = "admin";
        String encoded = PasswordEncoder.encode(raw);

        assertNotNull(encoded);
        assertTrue(PasswordEncoder.matches("admin", encoded));
        assertFalse(PasswordEncoder.matches("wrong_password", encoded));
    }

    @Test
    public void testJwtAuthProvider(Vertx vertx, VertxTestContext testContext) {
        JwtAuthProvider jwtAuthProvider = new JwtAuthProvider(vertx);

        User user = new User(1L, "admin", "admin@motadata.com", true);
        UserRole role = new UserRole(1L, "ROLE_ADMIN", "Administrator");

        List<RoleFeaturePermission> rfps = new ArrayList<>();
        rfps.add(new RoleFeaturePermission(1L, 1L, new Feature(1L, "ALERTS"), true, true));
        rfps.add(new RoleFeaturePermission(2L, 1L, new Feature(5L, "SETTINGS"), true, true));
        role.setRoleFeaturePermissions(rfps);

        user.setUserRoleId(role);

        String token = jwtAuthProvider.generateToken(user);
        assertNotNull(token);
        assertFalse(token.isEmpty());

        JsonObject credentials = new JsonObject().put("token", token);
        jwtAuthProvider.getJwtAuth().authenticate(credentials, ar -> {
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
}
