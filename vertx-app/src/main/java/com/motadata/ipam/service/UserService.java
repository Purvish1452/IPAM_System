package com.motadata.ipam.service;

import com.motadata.ipam.dao.UserDao;
import com.motadata.ipam.model.RoleFeaturePermission;
import com.motadata.ipam.model.User;
import com.motadata.ipam.security.JwtAuthProvider;
import com.motadata.ipam.security.PasswordEncoder;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Asynchronous Business Service for User Authentication, Management, and Permissions.
 */
public class UserService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);

    private final UserDao userDao;
    private final JwtAuthProvider jwtAuthProvider;

    public UserService(UserDao userDao, JwtAuthProvider jwtAuthProvider) {
        this.userDao = userDao;
        this.jwtAuthProvider = jwtAuthProvider;
    }

    public Future<JsonObject> authenticate(String userName, String password) {
        Promise<JsonObject> promise = Promise.promise();

        if (userName == null || password == null) {
            promise.complete(new JsonObject().put("success", false).put("message", "Username and password required"));
            return promise.future();
        }

        userDao.findByUserName(userName).onComplete(ar -> {
            if (ar.succeeded()) {
                User user = ar.result();
                if (user == null) {
                    LOGGER.warn("Authentication failed: User {} not found", userName);
                    promise.complete(new JsonObject().put("success", false).put("message", "User not found"));
                    return;
                }

                if (PasswordEncoder.matches(password, user.getPassword())) {
                    String token = jwtAuthProvider.generateToken(user);
                    List<String> authorities = extractAuthorities(user);

                    JsonObject response = new JsonObject()
                            .put("success", true)
                            .put("token", token)
                            .put("userName", user.getUserName())
                            .put("username", user.getUserName())
                            .put("userId", user.getId())
                            .put("authorities", new JsonArray(authorities));

                    LOGGER.info("User {} successfully authenticated", userName);
                    promise.complete(response);
                } else {
                    LOGGER.warn("Authentication failed for user {}: Invalid password", userName);
                    promise.complete(new JsonObject().put("success", false).put("message", "Bad Credentials"));
                }
            } else {
                LOGGER.error("Database query failed during user authentication for {}: {}", userName, ar.cause().getMessage());
                promise.complete(new JsonObject().put("success", false).put("message", "Database Error"));
            }
        });

        return promise.future();
    }

    public Future<List<User>> getAllUsers() {
        return userDao.findAllUsers();
    }

    private List<String> extractAuthorities(User user) {
        List<String> authorities = new ArrayList<>();
        if (user.getUserRoleId() != null) {
            String roleName = user.getUserRoleId().getRole();
            if (roleName != null) {
                authorities.add("ROLE_" + roleName);
            }
            if (user.getUserRoleId().getRoleFeaturePermissions() != null) {
                for (RoleFeaturePermission perm : user.getUserRoleId().getRoleFeaturePermissions()) {
                    if (perm.getFeature() != null && perm.getFeature().getName() != null) {
                        if (Boolean.TRUE.equals(perm.isReadPermission())) {
                            authorities.add("PERM_READ_" + perm.getFeature().getName().toUpperCase());
                        }
                        if (Boolean.TRUE.equals(perm.isWritePermission())) {
                            authorities.add("PERM_WRITE_" + perm.getFeature().getName().toUpperCase());
                        }
                    }
                }
            }
        }
        return authorities;
    }
}
