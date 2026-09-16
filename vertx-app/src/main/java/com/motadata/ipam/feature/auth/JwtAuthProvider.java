package com.motadata.ipam.feature.auth;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.JWTOptions;
import io.vertx.ext.auth.PubSecKeyOptions;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTAuthOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Vert.x 5 JWT authentication provider and token issuing utility using pure JSON structures.
 */
public class JwtAuthProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(JwtAuthProvider.class);
    private static final String SECRET_KEY = "motadata_secret";

    private final JWTAuth jwtAuth;

    // Initialize Vert.x JWT authentication using HS256 and the configured secret key.
    public JwtAuthProvider(Vertx vertx) {
        JWTAuthOptions config = new JWTAuthOptions()
                .addPubSecKey(new PubSecKeyOptions()
                        .setAlgorithm("HS256")
                        .setBuffer(SECRET_KEY));

        this.jwtAuth = JWTAuth.create(vertx, config);
        LOGGER.info("Initialized Vert.x 5 JWTAuth provider.");
    }

    // Return the configured JWTAuth provider for token authentication and validation.
    public JWTAuth getJwtAuth() {
        return jwtAuth;
    }

    // Generate a default JWT token for a username with default authorities.
    public String generateToken(String username) {
        List<String> defaultAuthorities = new ArrayList<>();
        defaultAuthorities.add("admin".equalsIgnoreCase(username) ? "ROLE_ADMIN" : "ROLE_USER");
        defaultAuthorities.add("PERM_READ_ALL");
        if ("admin".equalsIgnoreCase(username)) {
            defaultAuthorities.add("PERM_WRITE_ALL");
        }
        return generateToken(username, true, defaultAuthorities);
    }

    // Generate a JWT token containing the username and list of authorities.
    public String generateToken(String username, List<String> authoritiesList) {
        return generateToken(username, true, authoritiesList);
    }

    // Generates a JWT token embedding user details and authorities into token claims.
    public String generateToken(String username, boolean enabled, List<String> authoritiesList) {
        JsonArray authoritiesJson = new JsonArray();
        if (authoritiesList != null) {
            for (String auth : authoritiesList) {
                authoritiesJson.add(auth);
            }
        }

        JsonObject userDetails = new JsonObject()
                .put("username", username)
                .put("enabled", enabled)
                .put("authorities", authoritiesJson);

        JsonObject claims = new JsonObject()
                .put("User", userDetails)
                .put("user_name", username)
                .put("authorities", authoritiesJson)
                .put("scope", new JsonArray().add("read").add("write"))
                .put("client_id", "motadata_client")
                .put("jti", UUID.randomUUID().toString());

        JWTOptions jwtOptions = new JWTOptions()
                .setAlgorithm("HS256")
                .setExpiresInMinutes(60 * 24 * 30); // 30 days

        return jwtAuth.generateToken(claims, jwtOptions);
    }

    // Generates a JWT token from a JsonObject representing user entity.
    public String generateToken(JsonObject userJson, List<String> authoritiesList) {
        String username = userJson.getString("userName", userJson.getString("username", "admin"));
        boolean enabled = userJson.getBoolean("status", userJson.getBoolean("enabled", true));
        return generateToken(username, enabled, authoritiesList);
    }

    // Extracts normalized authority strings from a JsonObject user role representation.
    public List<String> extractAuthorities(JsonObject roleJson) {
        List<String> list = new ArrayList<>();
        if (roleJson != null) {
            String roleName = roleJson.getString("role");
            if (roleName != null) {
                if (!roleName.startsWith("ROLE_")) {
                    list.add("ROLE_" + roleName.toUpperCase());
                } else {
                    list.add(roleName.toUpperCase());
                }
            }

            JsonArray permissions = roleJson.getJsonArray("permissions");
            if (permissions != null) {
                for (int i = 0; i < permissions.size(); i++) {
                    JsonObject p = permissions.getJsonObject(i);
                    String feature = p.getString("feature");
                    if (feature != null) {
                        if (p.getBoolean("readPermission", false)) {
                            list.add("PERM_" + feature.toUpperCase() + "_READ");
                        }
                        if (p.getBoolean("writePermission", false)) {
                            list.add("PERM_" + feature.toUpperCase() + "_WRITE");
                        }
                    }
                }
            }
        }
        return list;
    }
}
