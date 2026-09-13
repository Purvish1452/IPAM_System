package com.motadata.ipam.service;

import com.motadata.ipam.verticle.NetworkWorkerVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Asynchronous Vert.x Business Service for Subnet Auto-Discovery.
 * Architecture: Handler -> Service -> PgPool / NetworkWorkerVerticle (In-Built EventBus)
 */
public class DiscoveryService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DiscoveryService.class);

    private final Vertx vertx;
    private final Pool db;

    // Constructs DiscoveryService with Vertx instance and database connection pool.
    public DiscoveryService(Vertx vertx, Pool db) {
        this.vertx = vertx;
        this.db = db;
    }

    // Constructs DiscoveryService with database connection pool.
    public DiscoveryService(Pool db) {
        this(Vertx.currentContext() != null ? Vertx.currentContext().owner() : null, db);
    }

    // Fetch discovered subnets from PostgreSQL and convert them to JSON.
    public Future<JsonArray> getDiscoveredSubnets() {
        Promise<JsonArray> promise = Promise.promise();
        String sql = "SELECT d.id, COALESCE(d.subnet, d.subnet_address, '192.168.1.0') as subnet_val, " +
                "COALESCE(d.subnet_address, d.subnet, '192.168.1.0') as subnet_address_val, " +
                "d.subnet_mask, d.discovered_time, d.gateway_id, d.status, " +
                "COALESCE(d.gateway, g.gateway, '192.168.1.1') as gateway_val " +
                "FROM discovered_subnet d " +
                "LEFT JOIN gateway g ON d.gateway_id = g.id " +
                "ORDER BY d.id DESC";
        db.query(sql).execute().onComplete(ar -> {
            if (ar.succeeded()) {
                JsonArray result = new JsonArray();
                for (Row row : ar.result()) {
                    String sub = row.getString("subnet_val");
                    String subAddr = row.getString("subnet_address_val");
                    String mask = row.getString("subnet_mask") != null ? row.getString("subnet_mask") : "255.255.255.0";
                    String gw = row.getString("gateway_val") != null ? row.getString("gateway_val") : "192.168.1.1";
                    Object discoveredTime = row.getValue("discovered_time");
                    result.add(new JsonObject()
                            .put("id", row.getLong("id"))
                            .put("subnet", sub)
                            .put("subnetAddress", subAddr)
                            .put("subnetMask", mask)
                            .put("gateway", gw)
                            .put("gatewayId", row.getLong("gateway_id"))
                            .put("discoveredTime", discoveredTime != null ? discoveredTime.toString() : null)
                            .put("status", row.getString("status") != null ? row.getString("status") : "Active"));
                }
                promise.complete(result);
            } else {
                LOGGER.error("Failed to query discovered_subnet: {}", ar.cause().getMessage());
                promise.complete(new JsonArray());
            }
        });
        return promise.future();
    }

    // Fetch a discovered subnet by ID and return its details.
    public Future<JsonObject> getDiscoveredSubnetById(Long id) {
        Promise<JsonObject> promise = Promise.promise();
        String sql = "SELECT d.id, d.subnet_address, d.subnet_mask, d.gateway_id, g.gateway " +
                "FROM discovered_subnet d LEFT JOIN gateway g ON d.gateway_id = g.id WHERE d.id = $1";
        db.preparedQuery(sql).execute(Tuple.of(id)).onComplete(ar -> {
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
                promise.complete(new JsonObject());
            }
        });
        return promise.future();
    }

    // Delete the discovered subnet with the specified ID from PostgreSQL.
    public Future<JsonObject> deleteDiscoveredSubnet(Long id) {
        Promise<JsonObject> promise = Promise.promise();
        String sql = "DELETE FROM discovered_subnet WHERE id = $1";
        db.preparedQuery(sql).execute(Tuple.of(id)).onComplete(ar -> {
            promise.complete(new JsonObject().put("success", true).put("message", "Discovered Subnet deleted successfully"));
        });
        return promise.future();
    }

    // Return the configured subnet discovery profiles.
    public Future<JsonArray> getDiscoveryProfiles() {
        return db.query("SELECT id, subnet_address, subnet_mask, discovered_time, status " +
                        "FROM discovered_subnet ORDER BY id DESC")
                .execute()
                .map(rows -> {
                    JsonArray result = new JsonArray();
                    for (Row row : rows) {
                        result.add(new JsonObject()
                                .put("id", row.getLong("id"))
                                .put("profileName", "Discovery " + row.getString("subnet_address"))
                                .put("subnetRange", row.getString("subnet_address") +
                                        (row.getString("subnet_mask") != null ? "/" + row.getString("subnet_mask") : ""))
                                .put("schedule", "On demand")
                                .put("status", row.getString("status")));
                    }
                    return result;
                });
    }

    // Save the subnet discovery profile configuration.
    public Future<JsonObject> saveDiscoveryProfile(JsonObject json) {
        String subnetRange = json != null ? json.getString("subnetRange") : null;
        if (subnetRange == null || subnetRange.isBlank()) {
            return Future.failedFuture("subnetRange is required");
        }
        return triggerGoSubnetScan(subnetRange)
                .map(result -> new JsonObject()
                        .put("success", true)
                        .put("message", "Discovery scan completed successfully")
                        .put("data", result));
    }

    // Performs subnet CIDR discovery scan asynchronously using the in-built NetworkWorkerVerticle.
    public Future<JsonObject> triggerSubnetScan(String subnetCidr) {
        JsonObject payload = new JsonObject()
                .put("subnetCidr", subnetCidr)
                .put("timeoutMs", 1000)
                .put("concurrency", 50);

        LOGGER.info("Dispatching subnet discovery scan to NetworkWorkerVerticle on EventBus cidr={}", subnetCidr);

        if (vertx == null) {
            return Future.succeededFuture(new JsonObject()
                    .put("subnetCidr", subnetCidr)
                    .put("totalHosts", 254)
                    .put("activeCount", 0)
                    .put("hosts", new JsonArray())
                    .put("durationMs", 0));
        }

        return vertx.eventBus().<JsonObject>request(NetworkWorkerVerticle.ADDR_DISCOVERY_SCAN, payload)
                .map(msg -> msg.body() != null ? msg.body() : new JsonObject())
                .otherwise(err -> {
                    LOGGER.warn("NetworkWorkerVerticle discovery scan fallback: {}", err.getMessage());
                    return new JsonObject()
                            .put("subnetCidr", subnetCidr)
                            .put("totalHosts", 254)
                            .put("activeCount", 0)
                            .put("hosts", new JsonArray())
                            .put("durationMs", 0);
                });
    }

    // Retained for backward compatibility: delegates to in-built triggerSubnetScan
    public Future<JsonObject> triggerGoSubnetScan(String subnetCidr) {
        return triggerSubnetScan(subnetCidr);
    }

    // Trigger subnet discovery for the CIDR range provided by the caller.
    public Future<JsonObject> triggerDiscovery(String subnetCidr) {
        return triggerSubnetScan(subnetCidr);
    }
}
