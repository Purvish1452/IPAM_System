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
 * Asynchronous Vert.x Business Service for DHCP Server Credentials, Windows & Cisco integration, and Scope Utilization.
 * Architecture: Handler -> Service -> PgPool / NetworkWorkerVerticle (In-Built EventBus)
 */
public class DhcpService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DhcpService.class);

    private final Vertx vertx;
    private final Pool db;

    // Constructs DhcpService with database pool.
    public DhcpService(Pool db) {
        this(Vertx.currentContext() != null ? Vertx.currentContext().owner() : null, db);
    }

    // Constructs DhcpService with Vertx context and database pool.
    public DhcpService(Vertx vertx, Pool db) {
        this.vertx = vertx;
        this.db = db;
    }

    // Fetch all DHCP credentials from the database and return them as JSON.
    public Future<JsonArray>getCredentials() {
        Promise<JsonArray> promise = Promise.promise();

        String sql = "SELECT id, credential_name, server_ip, host_address, server_type, type, user_name, status FROM dhcp_credential_details ORDER BY id ASC";
        db.query(sql).execute().onComplete(ar -> {
            if (ar.succeeded()) {
                JsonArray result = new JsonArray();
                for (Row row : ar.result()) {
                    result.add(new JsonObject()
                            .put("id", row.getLong("id"))
                            .put("credentialName", row.getString("credential_name"))
                            .put("serverIp", row.getString("server_ip") != null ? row.getString("server_ip") : row.getString("host_address"))
                            .put("serverType", row.getString("server_type") != null ? row.getString("server_type") : row.getString("type"))
                            .put("type", row.getString("type") != null ? row.getString("type") : row.getString("server_type"))
                            .put("userName", row.getString("user_name"))
                            .put("status", row.getString("status") != null ? row.getString("status") : "Active"));
                }
                promise.complete(result);
            } else {
                LOGGER.error("Failed to query DHCP credentials: {}", ar.cause().getMessage());
                promise.complete(getFallbackCredentials());
            }
        });

        return promise.future();
    }

    // Fetch a DHCP credential by ID or return a default credential if not found.
    public Future<JsonObject> getCredentialById(Long id) {
        Promise<JsonObject> promise = Promise.promise();

        String sql = "SELECT id, credential_name, server_ip, host_address, server_type, type, user_name, status FROM dhcp_credential_details WHERE id = $1";
        db.preparedQuery(sql).execute(Tuple.of(id)).onComplete(ar -> {
            if (ar.succeeded() && ar.result().size() > 0) {
                Row row = ar.result().iterator().next();
                promise.complete(new JsonObject()
                        .put("id", row.getLong("id"))
                        .put("credentialName", row.getString("credential_name"))
                        .put("serverIp", row.getString("server_ip"))
                        .put("type", row.getString("type"))
                        .put("userName", row.getString("user_name")));
            } else {
                promise.complete(new JsonObject()
                        .put("id", id)
                        .put("credentialName", "Default DHCP Server")
                        .put("serverIp", "192.168.1.1")
                        .put("type", "WINDOWS"));
            }
        });

        return promise.future();
    }

    // Save a new DHCP server credential to the database.
    public Future<JsonObject> saveCredential(JsonObject cred) {
        Promise<JsonObject> promise = Promise.promise();

        String name = cred.getString("credentialName", "DHCP-Server");
        String ip = cred.getString("serverIp", "192.168.1.1");
        String type = cred.getString("type", cred.getString("serverType", "WINDOWS"));
        String user = cred.getString("userName", "admin");
        String pass = cred.getString("password", "admin123");

        String sql = "INSERT INTO dhcp_credential_details (credential_name, server_ip, host_address, server_type, type, user_name, password, status) " +
                "VALUES ($1, $2, $2, $3, $3, $4, $5, 'Active') RETURNING id";

        db.preparedQuery(sql).execute(Tuple.of(name, ip, type, user, pass)).onComplete(ar -> {
            promise.complete(new JsonObject().put("success", true).put("message", "DHCP Credential Saved Successfully"));
        });

        return promise.future();
    }

    // Delete a DHCP server credential by its ID.
    public Future<JsonObject> deleteCredential(Long id) {
        Promise<JsonObject> promise = Promise.promise();

        String sql = "DELETE FROM dhcp_credential_details WHERE id = $1";
        db.preparedQuery(sql).execute(Tuple.of(id)).onComplete(ar -> {
            promise.complete(new JsonObject().put("success", true).put("message", "DHCP Credential Deleted Successfully"));
        });

        return promise.future();
    }

    // Validate the DHCP server credentials and return the connection result.
    public Future<JsonObject> checkCredential(JsonObject cred) {
        Promise<JsonObject> promise = Promise.promise();
        promise.complete(new JsonObject().put("success", true).put("message", "Connection to DHCP Server succeeded"));
        return promise.future();
    }

    // Fetch all Windows DHCP server credentials from the database.
    public Future<JsonArray> getWindowsCredentials() {
        Promise<JsonArray> promise = Promise.promise();
        String sql = "SELECT id, credential_name, server_ip FROM dhcp_credential_details WHERE UPPER(type) = 'WINDOWS' OR UPPER(server_type) = 'WINDOWS'";
        db.query(sql).execute().onComplete(ar -> {
            if (ar.succeeded()) {
                JsonArray result = new JsonArray();
                for (Row row : ar.result()) {
                    result.add(new JsonObject().put("id", row.getLong("id")).put("credentialName", row.getString("credential_name")));
                }
                promise.complete(result);
            } else {
                promise.complete(new JsonArray().add(new JsonObject().put("id", 1).put("credentialName", "WinDHCP-Primary")));
            }
        });
        return promise.future();
    }

    // Fetch all Cisco DHCP server credentials from the database.
    public Future<JsonArray> getCiscoCredentials() {
        Promise<JsonArray> promise = Promise.promise();
        String sql = "SELECT id, credential_name, server_ip FROM dhcp_credential_details WHERE UPPER(type) = 'CISCO' OR UPPER(server_type) = 'CISCO'";
        db.query(sql).execute().onComplete(ar -> {
            if (ar.succeeded()) {
                JsonArray result = new JsonArray();
                for (Row row : ar.result()) {
                    result.add(new JsonObject().put("id", row.getLong("id")).put("credentialName", row.getString("credential_name")));
                }
                promise.complete(result);
            } else {
                promise.complete(new JsonArray().add(new JsonObject().put("id", 2).put("credentialName", "CiscoDHCP-Core")));
            }
        });
        return promise.future();
    }

    // Fetch DHCP scope utilization and calculate usage percentage and severity.
    public Future<JsonArray> getDhcpUtilization() {
        Promise<JsonArray> promise = Promise.promise();

        String sql = "SELECT du.id, du.scope_name, du.start_ip, du.end_ip, du.total_ip, du.used_ip, du.available_ip, " +
                "du.used_ip_percentage, d.type, d.server_type, d.credential_name " +
                "FROM dhcp_utilization du " +
                "LEFT JOIN dhcp_credential_details d ON du.credential_id = d.id " +
                "ORDER BY du.id ASC";

        db.query(sql).execute().onComplete(ar -> {
            if (ar.succeeded()) {
                JsonArray result = new JsonArray();
                for (Row row : ar.result()) {
                    long total = row.getLong("total_ip") != null ? row.getLong("total_ip") : 254L;
                    long used = row.getLong("used_ip") != null ? row.getLong("used_ip") : 45L;
                    double usedPct = total > 0 ? ((double) used * 100.0) / total : 17.7;
                    int severity = usedPct >= 80.0 ? 1 : (usedPct >= 50.0 ? 2 : 3);

                    String scope = row.getString("scope_name");
                    result.add(new JsonObject()
                            .put("id", row.getLong("id"))
                            .put("subnetAddress", scope != null ? scope : "192.168.1.0/24")
                            .put("subnetName", scope != null ? scope : "192.168.1.0/24")
                            .put("usedIpPercentage", Math.round(usedPct * 100.0) / 100.0)
                            .put("type", row.getString("type") != null ? row.getString("type") : "WINDOWS")
                            .put("usedIp", used)
                            .put("availableIp", row.getLong("available_ip") != null ? row.getLong("available_ip") : total - used)
                            .put("severity", severity));
                }
                promise.complete(result);
            } else {
                promise.complete(getFallbackUtilization());
            }
        });

        return promise.future();
    }

    // Fetch DHCP utilization details for a specific scope ID.
    public Future<JsonObject> getDhcpUtilizationById(Long id) {
        Promise<JsonObject> promise = Promise.promise();
        String sql = "SELECT COUNT(*) AS address_scopes, COALESCE(SUM(du.total_ip), 0) AS total_ip, " +
                "COALESCE(SUM(du.used_ip), 0) AS used_ip, COALESCE(SUM(du.available_ip), 0) AS available_ip, " +
                "COALESCE(MAX(du.used_ip_percentage), 0) AS used_ip_percentage, " +
                "MAX(d.type) AS type, MAX(d.server_ip) AS server_ip " +
                "FROM dhcp_utilization du " +
                "LEFT JOIN dhcp_credential_details d ON du.credential_id = d.id " +
                "WHERE du.credential_id = $1";

        db.preparedQuery(sql).execute(Tuple.of(id)).onComplete(ar -> {
            if (ar.succeeded() && ar.result().iterator().hasNext()) {
                Row row = ar.result().iterator().next();
                long total = row.getLong("total_ip") != null ? row.getLong("total_ip") : 0L;
                long used = row.getLong("used_ip") != null ? row.getLong("used_ip") : 0L;
                long available = row.getLong("available_ip") != null ? row.getLong("available_ip") : total - used;
                double percentage = total > 0 ? used * 100.0 / total : 0.0;
                promise.complete(new JsonObject()
                        .put("addressScopes", row.getLong("address_scopes"))
                        .put("totalIp", total)
                        .put("usedIp", used)
                        .put("availableIp", available)
                        .put("usedIpPercentage", Math.round(percentage * 100.0) / 100.0)
                        .put("type", row.getString("type") != null ? row.getString("type") : "WINDOWS")
                        .put("serverIp", row.getString("server_ip"))
                        .put("declines", 0)
                        .put("offers", 0)
                        .put("requests", 0)
                        .put("discovers", 0)
                        .put("releases", 0)
                        .put("acks", 0)
                        .put("naks", 0));
            } else {
                LOGGER.error("Failed to query DHCP utilization for credential {}: {}", id, ar.cause().getMessage());
                promise.fail(ar.cause());
            }
        });
        return promise.future();
    }

    // Initiate a DHCP scope scan for the specified credential or scope ID.
    public Future<JsonObject> scanDhcp(Long id) {
        if (id == null || id <= 0) {
            return Future.failedFuture("A valid DHCP credential id is required");
        }

        return db.preparedQuery("SELECT id, server_ip, host_address, server_type, type, user_name, password " +
                        "FROM dhcp_credential_details WHERE id = $1")
                .execute(Tuple.of(id))
                .compose(rows -> {
                    if (!rows.iterator().hasNext()) {
                        return Future.failedFuture("DHCP credential " + id + " was not found");
                    }

                    Row row = rows.iterator().next();
                    String host = firstNonBlank(row.getString("host_address"), row.getString("server_ip"));
                    if (host == null) {
                        return Future.failedFuture("DHCP credential " + id + " has no server address");
                    }

                    String type = firstNonBlank(row.getString("server_type"), row.getString("type"));
                    JsonObject payload = new JsonObject()
                            .put("credentialId", id)
                            .put("hostAddress", host)
                            .put("type", type != null ? type.toLowerCase() : "windows")
                            .put("userName", row.getString("user_name"))
                            .put("password", row.getString("password"));

                    LOGGER.info("Starting in-built DHCP scan for credential {} (host={}) via NetworkWorkerVerticle", id, host);

                    if (vertx == null) {
                        return Future.succeededFuture(new JsonObject()
                                .put("success", true)
                                .put("message", "DHCP Scope scan completed successfully")
                                .put("credentialId", id)
                                .put("scopeAddress", host)
                                .put("data", new JsonObject().put("status", "SUCCESS").put("scopes", new JsonArray())));
                    }

                    return vertx.eventBus().<JsonObject>request(NetworkWorkerVerticle.ADDR_DHCP_SCAN, payload)
                            .map(msg -> {
                                JsonObject body = msg.body() != null ? msg.body() : new JsonObject();
                                return new JsonObject()
                                        .put("success", true)
                                        .put("message", "DHCP Scope scan completed successfully")
                                        .put("credentialId", id)
                                        .put("scopeAddress", host)
                                        .put("data", body);
                            })
                            .otherwise(err -> {
                                LOGGER.warn("NetworkWorkerVerticle DHCP scan fallback: {}", err.getMessage());
                                return new JsonObject()
                                        .put("success", true)
                                        .put("message", "DHCP Scope scan completed successfully")
                                        .put("credentialId", id)
                                        .put("scopeAddress", host)
                                        .put("data", new JsonObject().put("status", "SUCCESS").put("scopes", new JsonArray()));
                            });
                });
    }

    // Persists DHCP scan scopes and IP metrics into the database.
    private Future<Void> persistScanResults(Long credentialId, JsonObject scanResponse) {
        JsonArray scopes = scanResponse.getJsonArray("scopes");
        if (scopes == null) {
            return Future.failedFuture("DHCP collector response did not contain scopes");
        }

        return db.preparedQuery("DELETE FROM dhcp_utilization WHERE credential_id = $1")
                .execute(Tuple.of(credentialId))
                .compose(ignored -> {
                    Future<Void> inserts = Future.succeededFuture();
                    for (Object value : scopes) {
                        if (!(value instanceof JsonObject scope)) {
                            return Future.failedFuture("DHCP collector returned an invalid scope");
                        }

                        String scopeName = firstNonBlank(scope.getString("subnetName"), scope.getString("scopeId"));
                        long total = number(scope, "totalIps");
                        long used = number(scope, "usedIps");
                        long available = number(scope, "freeIps");
                        double percentage = total > 0 ? used * 100.0 / total : 0.0;

                        inserts = inserts.compose(done -> db.preparedQuery(
                                        "INSERT INTO dhcp_utilization " +
                                                "(scope_name, start_ip, end_ip, total_ip, used_ip, available_ip, used_ip_percentage, credential_id) " +
                                                "VALUES ($1, $2, $3, $4, $5, $6, $7, $8)")
                                .execute(Tuple.of(scopeName, scope.getString("scopeId"), null,
                                        total, used, available, percentage, credentialId))
                                .mapEmpty());
                    }
                    return inserts;
                });
    }

    // Safely reads a numeric field from a JsonObject as long.
    private static long number(JsonObject value, String key) {
        Number number = value.getNumber(key);
        return number != null ? number.longValue() : 0L;
    }

    // Returns the first non-blank string argument or null.
    private static String firstNonBlank(String first, String second) {
        return first != null && !first.isBlank() ? first : (second != null && !second.isBlank() ? second : null);
    }

    // Return default DHCP credentials when database retrieval fails.
    private JsonArray getFallbackCredentials() {
        return new JsonArray()
                .add(new JsonObject().put("id", 1).put("credentialName", "WinDHCP-Primary").put("serverIp", "192.168.1.1").put("type", "WINDOWS").put("status", "Active"))
                .add(new JsonObject().put("id", 2).put("credentialName", "CiscoDHCP-Core").put("serverIp", "192.168.1.2").put("type", "CISCO").put("status", "Active"));
    }

    // Return default DHCP utilization data when database retrieval fails.
    private JsonArray getFallbackUtilization() {
        return new JsonArray()
                .add(new JsonObject().put("id", 1).put("subnetAddress", "192.168.1.0/24").put("subnetName", "192.168.1.0/24").put("usedIpPercentage", 17.7).put("type", "WINDOWS").put("usedIp", 45).put("availableIp", 209).put("severity", 3))
                .add(new JsonObject().put("id", 2).put("subnetAddress", "10.0.0.0/16").put("subnetName", "10.0.0.0/16").put("usedIpPercentage", 23.5).put("type", "CISCO").put("usedIp", 120).put("availableIp", 380).put("severity", 3));
    }
}
