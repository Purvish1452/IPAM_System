package com.motadata.ipam.dao;

import com.motadata.ipam.model.Feature;
import com.motadata.ipam.model.RoleFeaturePermission;
import com.motadata.ipam.model.User;
import com.motadata.ipam.model.UserRole;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.mysqlclient.MySQLPool;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.Tuple;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Reactive non-blocking Data Access Object for User and UserRole operations using Vert.x MySQLPool.
 */
public class UserDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserDao.class);

    private final MySQLPool client;

    public UserDao(MySQLPool client) {
        this.client = client;
    }

    public Future<User> findByUserName(String userName) {
        Promise<User> promise = Promise.promise();

        String sql = "SELECT u.id as id, u.user_name as userName, u.password as password, u.email as email, " +
                "u.status as status, u.description as description, " +
                "u.user_role_id_id as roleId, ur.role as roleName, ur.description as roleDesc " +
                "FROM user u LEFT JOIN user_role ur ON u.user_role_id_id = ur.id " +
                "WHERE u.user_name = ?";

        client.preparedQuery(sql).execute(Tuple.of(userName), ar -> {
            if (ar.succeeded()) {
                RowSet<Row> rows = ar.result();
                if (rows.size() > 0) {
                    Row row = rows.iterator().next();
                    User user = mapRowToUser(row);
                    if (user.getUserRoleId() != null && user.getUserRoleId().getId() != null) {
                        getRolePermissions(user.getUserRoleId().getId()).onComplete(permAr -> {
                            if (permAr.succeeded()) {
                                user.getUserRoleId().setRoleFeaturePermissions(permAr.result());
                            } else {
                                LOGGER.warn("Failed to retrieve role permissions for role {}: {}", user.getUserRoleId().getId(), permAr.cause().getMessage());
                            }
                            promise.complete(user);
                        });
                    } else {
                        promise.complete(user);
                    }
                } else {
                    promise.complete(null);
                }
            } else {
                LOGGER.error("Failed to fetch user by username {}: {}", userName, ar.cause().getMessage());
                promise.fail(ar.cause());
            }
        });

        return promise.future();
    }

    public Future<List<RoleFeaturePermission>> getRolePermissions(Long roleId) {
        Promise<List<RoleFeaturePermission>> promise = Promise.promise();

        if (roleId == null) {
            promise.complete(new ArrayList<>());
            return promise.future();
        }

        String sql = "SELECT rfp.id as id, rfp.role_id as role_id, rfp.feature_id as feature_id, " +
                "rfp.read_permission as read_permission, rfp.write_permission as write_permission, " +
                "f.name as feature_name " +
                "FROM role_feature_permission rfp " +
                "JOIN feature f ON rfp.feature_id = f.id " +
                "WHERE rfp.role_id = ?";

        client.preparedQuery(sql).execute(Tuple.of(roleId), ar -> {
            if (ar.succeeded()) {
                List<RoleFeaturePermission> permissions = new ArrayList<>();
                for (Row row : ar.result()) {
                    RoleFeaturePermission rfp = new RoleFeaturePermission();
                    rfp.setId(row.getLong("id"));
                    rfp.setRoleId(row.getLong("role_id"));
                    rfp.setFeatureId(row.getLong("feature_id"));
                    rfp.setReadPermission(row.getBoolean("read_permission"));
                    rfp.setWritePermission(row.getBoolean("write_permission"));
                    rfp.setFeature(new Feature(row.getLong("feature_id"), row.getString("feature_name")));
                    permissions.add(rfp);
                }
                promise.complete(permissions);
            } else {
                LOGGER.error("Failed to fetch role permissions for roleId {}: {}", roleId, ar.cause().getMessage());
                promise.complete(new ArrayList<>());
            }
        });

        return promise.future();
    }

    public Future<List<User>> findAllUsers() {
        Promise<List<User>> promise = Promise.promise();

        String sql = "SELECT u.id as id, u.user_name as userName, u.password as password, u.email as email, " +
                "u.status as status, u.description as description, " +
                "u.user_role_id_id as roleId, ur.role as roleName, ur.description as roleDesc " +
                "FROM user u LEFT JOIN user_role ur ON u.user_role_id_id = ur.id";

        client.query(sql).execute(ar -> {
            if (ar.succeeded()) {
                List<User> users = new ArrayList<>();
                for (Row row : ar.result()) {
                    users.add(mapRowToUser(row));
                }
                promise.complete(users);
            } else {
                LOGGER.error("Failed to fetch all users: {}", ar.cause().getMessage());
                promise.fail(ar.cause());
            }
        });

        return promise.future();
    }

    private User mapRowToUser(Row row) {
        User user = new User();
        user.setId(row.getLong("id"));
        user.setUserName(row.getString("userName"));
        user.setPassword(row.getString("password"));
        user.setEmail(row.getString("email"));

        Boolean status = row.getBoolean("status");
        user.setStatus(status != null && status);
        user.setDescription(row.getString("description"));

        Long roleId = row.getLong("roleId");
        if (roleId != null) {
            String roleName = row.getString("roleName");
            String roleDesc = row.getString("roleDesc");
            UserRole role = new UserRole(roleId, roleName, roleDesc);
            user.setUserRoleId(role);
            user.setRoleId(roleId);
        }
        return user;
    }
}
