package com.motadata.ipam.security;

import com.motadata.ipam.model.RoleFeaturePermission;
import com.motadata.ipam.model.User;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.JWTOptions;
import io.vertx.ext.auth.PubSecKeyOptions;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTAuthOptions;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Vert.x JWT authentication provider and token issuing utility.
 */
public class JwtAuthProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(JwtAuthProvider.class);
    private static final String SECRET_KEY = "motadata_secret";

    private final JWTAuth jwtAuth;

    public JwtAuthProvider(Vertx vertx) {
        JWTAuthOptions config = new JWTAuthOptions()
                .addPubSecKey(new PubSecKeyOptions()
                        .setAlgorithm("HS256")
                        .setBuffer(SECRET_KEY));

        this.jwtAuth = JWTAuth.create(vertx, config);
        LOGGER.info("Initialized Vert.x JWTAuth provider.");
    }

    public JWTAuth getJwtAuth() {
        return jwtAuth;
    }

    public String generateToken(User user) {
        List<String> authoritiesList = extractAuthorities(user);

        JsonArray authoritiesJson = new JsonArray();
        for (String auth : authoritiesList) {
            authoritiesJson.add(auth);
        }

        JsonObject userDetails = new JsonObject()
                .put("username", user.getUserName())
                .put("enabled", user.isStatus())
                .put("authorities", authoritiesJson);

        JsonObject claims = new JsonObject()
                .put("User", userDetails)
                .put("user_name", user.getUserName())
                .put("authorities", authoritiesJson)
                .put("scope", new JsonArray().add("read").add("write"))
                .put("client_id", "motadata_client")
                .put("jti", UUID.randomUUID().toString());

        JWTOptions jwtOptions = new JWTOptions()
                .setAlgorithm("HS256")
                .setExpiresInMinutes(60 * 24 * 30); // 30 days

        return jwtAuth.generateToken(claims, jwtOptions);
    }

    public List<String> extractAuthorities(User user) {
        List<String> list = new ArrayList<>();
        if (user != null && user.getUserRoleId() != null) {
            String roleName = user.getUserRoleId().getRole();
            if (roleName != null) {
                if (!roleName.startsWith("ROLE_")) {
                    list.add("ROLE_" + roleName.toUpperCase());
                } else {
                    list.add(roleName.toUpperCase());
                }
            }
            if (user.getUserRoleId().getRoleFeaturePermissions() != null) {
                for (RoleFeaturePermission rfp : user.getUserRoleId().getRoleFeaturePermissions()) {
                    if (rfp.getFeature() != null && rfp.getFeature().getName() != null) {
                        String featureName = rfp.getFeature().getName();
                        if (rfp.isReadPermission()) {
                            list.add("PERM_" + featureName + "_READ");
                        }
                        if (rfp.isWritePermission()) {
                            list.add("PERM_" + featureName + "_WRITE");
                        }
                    }
                }
            }
        }
        return list;
    }
}
