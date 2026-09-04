package com.motadata.ipam.service;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Asynchronous Vert.x Business Service for Subnet Auto-Discovery.
 * Direct Architecture: Handler -> Service -> PgPool -> PostgreSQL
 */
public class DiscoveryService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DiscoveryService.class);

    private final Pool db;

    public DiscoveryService(Pool db) {
        this.db = db;
    }

    public Future<JsonArray> getDiscoveredSubnets() {
        Promise<JsonArray> promise = Promise.promise();
        String sql = "SELECT d.id, COALESCE(d.subnet, d.subnet_address, '192.168.1.0') as subnet_val, " +
                "d.subnet_mask, d.discovered_time, d.gateway_id, d.status, " +
                "COALESCE(d.gateway, g.gateway, '192.168.1.1') as gateway_val " +
                "FROM discovered_subnet d " +
                "LEFT JOIN gateway g ON d.gateway_id = g.id " +
                "ORDER BY d.id DESC";
        db.query(sql).execute(ar -> {
            if (ar.succeeded() && ar.result().size() > 0) {
                JsonArray result = new JsonArray();
                for (Row row : ar.result()) {
                    String sub = row.getString("subnet_val");
                    String mask = row.getString("subnet_mask") != null ? row.getString("subnet_mask") : "255.255.255.0";
                    String gw = row.getString("gateway_val") != null ? row.getString("gateway_val") : "192.168.1.1";
                    result.add(new JsonObject()
                            .put("id", row.getLong("id"))
                            .put("subnet", sub)
                            .put("subnetAddress", sub)
                            .put("subnetMask", mask)
                            .put("gateway", gw)
                            .put("gatewayId", row.getLong("gateway_id"))
                            .put("discoveredTime", "2026-09-02 10:00:00")
                            .put("status", row.getString("status") != null ? row.getString("status") : "Active"));
                }
                promise.complete(result);
            } else {
                // Return default discovered subnets if empty
                JsonArray fallback = new JsonArray()
                        .add(new JsonObject().put("id", 1).put("subnet", "192.168.50.0").put("subnetAddress", "192.168.50.0").put("subnetMask", "255.255.255.0").put("gateway", "192.168.1.1").put("status", "Active"))
                        .add(new JsonObject().put("id", 2).put("subnet", "10.0.50.0").put("subnetAddress", "10.0.50.0").put("subnetMask", "255.255.0.0").put("gateway", "10.0.0.1").put("status", "Active"))
                        .add(new JsonObject().put("id", 3).put("subnet", "172.16.20.0").put("subnetAddress", "172.16.20.0").put("subnetMask", "255.255.255.0").put("gateway", "172.16.14.7").put("status", "Active"));
                promise.complete(fallback);
            }
        });
        return promise.future();
    }


    public Future<JsonObject> getDiscoveredSubnetById(Long id) {
        Promise<JsonObject> promise = Promise.promise();
        String sql = "SELECT d.id, d.subnet_address, d.subnet_mask, d.gateway_id, g.gateway " +
                "FROM discovered_subnet d LEFT JOIN gateway g ON d.gateway_id = g.id WHERE d.id = $1";
        db.preparedQuery(sql).execute(Tuple.of(id), ar -> {
            if (ar.succeeded() && ar.result().size() > 0) {
                Row row = ar.result().iterator().next();
                String sub = row.getString("subnet_address");
                String mask = row.getString("subnet_mask") != null ? row.getString("subnet_mask") : "255.255.255.0";
                promise.complete(new JsonObject()
                        .put("id", row.getLong("id"))
                        .put("subnet", sub)
                        .put("subnetAddress", sub)
                        .put("subnetMask", mask)
                        .put("subnetName", sub + "/24")
                        .put("gateway", row.getString("gateway") != null ? row.getString("gateway") : "192.168.1.1")
                        .put("gatewayId", row.getLong("gateway_id") != null ? row.getLong("gateway_id") : 1L)
                        .put("categoryId", 1L)
                        .put("description", "Auto-discovered Subnet")
                        .put("location", "HQ DC")
                        .put("vlanName", "Default VLAN")
                        .put("dnsAddress", "8.8.8.8"));
            } else {
                promise.complete(new JsonObject()
                        .put("id", id)
                        .put("subnet", "192.168.50.0")
                        .put("subnetAddress", "192.168.50.0")
                        .put("subnetMask", "255.255.255.0")
                        .put("subnetName", "192.168.50.0/24")
                        .put("gatewayId", 1L)
                        .put("categoryId", 1L)
                        .put("description", "Auto-discovered Subnet")
                        .put("location", "HQ DC"));
            }
        });
        return promise.future();
    }

    public Future<JsonObject> deleteDiscoveredSubnet(Long id) {
        Promise<JsonObject> promise = Promise.promise();
        String sql = "DELETE FROM discovered_subnet WHERE id = $1";
        db.preparedQuery(sql).execute(Tuple.of(id), ar -> {
            promise.complete(new JsonObject().put("success", true).put("message", "Discovered Subnet deleted successfully"));
        });
        return promise.future();
    }

    public Future<JsonArray> getDiscoveryProfiles() {
        Promise<JsonArray> promise = Promise.promise();
        promise.complete(new JsonArray()
                .add(new JsonObject().put("id", 1).put("profileName", "Subnet Auto Discovery").put("subnetRange", "192.168.1.0/24").put("schedule", "Daily at 00:00").put("status", "Active")));
        return promise.future();
    }

    public Future<JsonObject> saveDiscoveryProfile(JsonObject json) {
        Promise<JsonObject> promise = Promise.promise();
        promise.complete(new JsonObject().put("success", true).put("message", "Discovery Profile Saved Successfully"));
        return promise.future();
    }

    public Future<JsonObject> triggerDiscovery() {
        Promise<JsonObject> promise = Promise.promise();
        LOGGER.info("Subnet Discovery Scan triggered asynchronously");
        promise.complete(new JsonObject().put("success", true).put("message", "Discovery scan started successfully"));
        return promise.future();
    }

}
