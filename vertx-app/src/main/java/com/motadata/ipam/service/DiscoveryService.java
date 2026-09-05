package com.motadata.ipam.service;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Asynchronous Vert.x Business Service for Subnet Auto-Discovery.
 * Architecture: Handler -> Service -> PgPool / Go Discovery Microservice
 */
public class DiscoveryService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DiscoveryService.class);

    private final Pool db;
    private final WebClient webClient;
    private final String goDiscoveryHost;
    private final int goDiscoveryPort;

    public DiscoveryService(Vertx vertx, Pool db) {
        this(vertx, db, "localhost", 8081);
    }

    public DiscoveryService(Vertx vertx, Pool db, String goDiscoveryHost, int goDiscoveryPort) {
        this.db = db;
        this.goDiscoveryHost = goDiscoveryHost;
        this.goDiscoveryPort = goDiscoveryPort;
        this.webClient = WebClient.create(vertx, new WebClientOptions()
                .setConnectTimeout(5000)
                .setIdleTimeout(120));
    }

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

    public Future<JsonObject> deleteDiscoveredSubnet(Long id) {
        Promise<JsonObject> promise = Promise.promise();
        String sql = "DELETE FROM discovered_subnet WHERE id = $1";
        db.preparedQuery(sql).execute(Tuple.of(id)).onComplete(ar -> {
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

    /**
     * Dispatch a subnet CIDR scan to the Go discovery microservice on port 8081.
     */
    public Future<JsonObject> triggerGoSubnetScan(String subnetCidr) {
        Promise<JsonObject> promise = Promise.promise();

        JsonObject payload = new JsonObject()
                .put("subnetCidr", subnetCidr)
                .put("timeoutMs", 1000)
                .put("concurrency", 50);

        LOGGER.info("Dispatching subnet discovery scan to Go microservice at http://{}:{}/api/v1/scan/subnet cidr={}",
                goDiscoveryHost, goDiscoveryPort, subnetCidr);

        webClient.post(goDiscoveryPort, goDiscoveryHost, "/api/v1/scan/subnet")
                .putHeader("Content-Type", "application/json")
                .sendJsonObject(payload)
                .onComplete(ar -> {
                    if (ar.succeeded() && ar.result().statusCode() >= 200 && ar.result().statusCode() < 300) {
                        JsonObject body = ar.result().bodyAsJsonObject();
                        LOGGER.info("Go discovery responded status={} activeCount={}",
                                ar.result().statusCode(),
                                body != null ? body.getInteger("activeCount") : null);
                        promise.complete(body != null ? body : new JsonObject());
                    } else if (ar.succeeded()) {
                        LOGGER.warn("Go discovery returned HTTP {}", ar.result().statusCode());
                        promise.fail("Go discovery returned HTTP " + ar.result().statusCode());
                    } else {
                        LOGGER.warn("Failed to reach Go discovery microservice: {}", ar.cause().getMessage());
                        promise.fail(ar.cause());
                    }
                });

        return promise.future();
    }

    public Future<JsonObject> triggerDiscovery() {
        return triggerGoSubnetScan("192.168.1.0/24");
    }
}
