package com.motadata.ipam.dao;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.mysqlclient.MySQLPool;
import io.vertx.sqlclient.Row;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Reactive non-blocking Data Access Object for DHCP Credentials using Vert.x MySQLPool.
 */
public class DhcpDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(DhcpDao.class);

    private final MySQLPool client;

    public DhcpDao(MySQLPool client) {
        this.client = client;
    }

    public Future<List<Map<String, Object>>> findAllCredentials() {
        Promise<List<Map<String, Object>>> promise = Promise.promise();

        String sql = "SELECT id, credential_name, host_address, type, user_name, port, created_by " +
                "FROM dhcp_credential_details";

        client.query(sql).execute(ar -> {
            if (ar.succeeded()) {
                List<Map<String, Object>> list = new ArrayList<>();
                for (Row row : ar.result()) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", row.getLong("id"));
                    map.put("credentialName", row.getString("credential_name"));
                    map.put("hostAddress", row.getString("host_address"));
                    map.put("type", row.getString("type"));
                    map.put("userName", row.getString("user_name"));
                    map.put("port", row.getInteger("port"));
                    map.put("createdBy", row.getString("created_by"));
                    list.add(map);
                }
                promise.complete(list);
            } else {
                LOGGER.error("Failed to fetch DHCP credentials: {}", ar.cause().getMessage());
                promise.fail(ar.cause());
            }
        });

        return promise.future();
    }
}
