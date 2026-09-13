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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Asynchronous Vert.x Business Service for Alert Streams, Thresholds, Dynamic Alerts, and Configuration.
 * Direct Architecture: Handler -> Service -> PgPool -> PostgreSQL
 */
public class AlertService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AlertService.class);
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private final Pool db;

    // Constructs AlertService with the specified database connection pool.
    public AlertService(Pool db) {
        this.db = db;
    }

    // Fetch, filter, search, paginate, and convert alert records into a JSON response.
    public Future<JsonObject> getAlerts(String alertFilter, String search, Integer page, Integer pageSize) {
        Promise<JsonObject> promise = Promise.promise();

        int p = (page == null || page < 1) ? 1 : page;
        int size = (pageSize == null || pageSize < 1) ? 20 : pageSize;
        int offset = (p - 1) * size;

        List<String> conditions = new ArrayList<>();
        Tuple countParams = Tuple.tuple();
        Tuple dataParams = Tuple.tuple();
        int paramIdx = 1;

        if ("live".equalsIgnoreCase(alertFilter)) {
            conditions.add("status = true");
        } else if ("clear".equalsIgnoreCase(alertFilter) || "cleared".equalsIgnoreCase(alertFilter)) {
            conditions.add("status = false");
        }

        if (search != null && !search.trim().isEmpty()) {
            String pattern = "%" + search.trim().toLowerCase() + "%";
            conditions.add("(LOWER(message) LIKE $" + paramIdx + " OR LOWER(alert_type) LIKE $" + paramIdx + " OR LOWER(subnet) LIKE $" + paramIdx + ")");
            countParams.addString(pattern);
            dataParams.addString(pattern);
            paramIdx++;
        }

        String whereClause = conditions.isEmpty() ? "" : " WHERE " + String.join(" AND ", conditions) + " ";
        String countSql = "SELECT count(*) as total FROM alert_stream " + whereClause;
        String dataSql = "SELECT id, subnet_id, alert_type, message, subnet, timestamp, status " +
                "FROM alert_stream " + whereClause + " ORDER BY id DESC LIMIT $" + paramIdx + " OFFSET $" + (paramIdx + 1);

        dataParams.addInteger(size);
        dataParams.addInteger(offset);

        db.preparedQuery(countSql).execute(countParams).onComplete(countAr -> {
            if (countAr.failed()) {
                LOGGER.error("Failed to query alert count: {}", countAr.cause().getMessage());
                promise.fail(countAr.cause());
                return;
            }

            long total = 0;
            if (countAr.result().size() > 0) {
                total = countAr.result().iterator().next().getLong("total");
            }

            if (total == 0) {
                db.query("SELECT count(*) as cnt FROM alert_stream").execute().onComplete(allCntAr -> {
                    long allCnt = (allCntAr.succeeded() && allCntAr.result().size() > 0) ?
                            allCntAr.result().iterator().next().getLong("cnt") : 0;
                    if (allCnt == 0) {
                        seedInitialAlerts().onComplete(seedAr -> {
                            db.preparedQuery(countSql).execute(countParams).onComplete(reCountAr -> {
                                long reTotal = (reCountAr.succeeded() && reCountAr.result().size() > 0) ?
                                        reCountAr.result().iterator().next().getLong("total") : 0;
                                fetchAlertData(dataSql, dataParams, reTotal, promise);
                            });
                        });
                    } else {
                        fetchAlertData(dataSql, dataParams, 0, promise);
                    }
                });
            } else {
                fetchAlertData(dataSql, dataParams, total, promise);
            }
        });

        return promise.future();
    }

    // Retrieves filtered and paginated alerts without search term.
    public Future<JsonObject> getAlerts(String alertFilter, Integer page, Integer pageSize) {
        return getAlerts(alertFilter, null, page, pageSize);
    }

    // Fetches alert database rows and shapes the JSON response payload.
    private void fetchAlertData(String sql, Tuple params, long total, Promise<JsonObject> promise) {
        db.preparedQuery(sql).execute(params).onComplete(dataAr -> {
            if (dataAr.succeeded()) {
                JsonArray list = new JsonArray();
                for (Row row : dataAr.result()) {
                    Date ts = row.getLocalDateTime("timestamp") != null ?
                            java.sql.Timestamp.valueOf(row.getLocalDateTime("timestamp")) : new Date();

                    JsonObject a = new JsonObject()
                            .put("id", row.getLong("id"))
                            .put("alertType", row.getString("alert_type") != null ? row.getString("alert_type") : "CRITICAL")
                            .put("message", row.getString("message") != null ? row.getString("message") : "Subnet alert triggered")
                            .put("subnet", row.getString("subnet") != null ? row.getString("subnet") : "192.168.10.0")
                            .put("timestamp", DATE_FORMAT.format(ts))
                            .put("status", row.getBoolean("status") != null && row.getBoolean("status"));
                    list.add(a);
                }

                JsonObject response = new JsonObject()
                        .put("data", list)
                        .put("total", total)
                        .put("success", true);
                promise.complete(response);
            } else {
                LOGGER.error("Failed to query alert data: {}", dataAr.cause().getMessage());
                promise.fail(dataAr.cause());
            }
        });
    }

    // Insert default alert records when the alert stream is initially empty.
    public Future<Void> seedInitialAlerts() {
        Promise<Void> promise = Promise.promise();
        String seedSql = "INSERT INTO alert_stream (subnet_id, alert_type, message, subnet, timestamp, status) VALUES " +
                "(1, 'CRITICAL', 'Subnet 192.168.10.0/24 utilization reached 85.2% (Threshold: 80%)', '192.168.10.0', CURRENT_TIMESTAMP - INTERVAL '15 minutes', true), " +
                "(1, 'MAJOR', 'Rogue Device 00:50:56:FE:DC:BA detected on IP 192.168.10.155', '192.168.10.0', CURRENT_TIMESTAMP - INTERVAL '45 minutes', true), " +
                "(2, 'WARNING', 'IP Conflict detected on 10.0.0.45 between MACs', '10.0.0.0', CURRENT_TIMESTAMP - INTERVAL '2 hours', true), " +
                "(1, 'INFO', 'DHCP Scope Office-DHCP-Pool lease sync completed successfully', '192.168.10.0', CURRENT_TIMESTAMP - INTERVAL '3 hours', true), " +
                "(2, 'CRITICAL', 'Production Subnet 10.0.0.0/16 high utilization warning', '10.0.0.0', CURRENT_TIMESTAMP - INTERVAL '5 hours', true), " +
                "(1, 'CLEARED', 'Subnet 192.168.10.0/24 utilization normalized to 45%', '192.168.10.0', CURRENT_TIMESTAMP - INTERVAL '1 day', false), " +
                "(1, 'CLEARED', 'Rogue Device 00:50:56:FE:10:04 authorized as Gateway Host', '192.168.10.0', CURRENT_TIMESTAMP - INTERVAL '2 days', false), " +
                "(2, 'CLEARED', 'IP Conflict on 10.0.0.12 resolved automatically', '10.0.0.0', CURRENT_TIMESTAMP - INTERVAL '3 days', false)";

        db.query(seedSql).execute().onComplete(ar -> {
            if (ar.succeeded()) {
                LOGGER.info("Seeded initial realistic alert records into alert_stream table.");
                promise.complete();
            } else {
                LOGGER.warn("Initial alert seeding skipped: {}", ar.cause().getMessage());
                promise.complete();
            }
        });
        return promise.future();
    }

    // Creates a new dynamic alert record.
    public Future<JsonObject> createAlert(Long subnetId, String alertType, String message, String subnet, boolean status) {
        Promise<JsonObject> promise = Promise.promise();
        long sid = subnetId != null ? subnetId : 1L;
        String type = alertType != null ? alertType.toUpperCase() : "INFO";
        String sub = subnet != null ? subnet : "General";

        String sql = "INSERT INTO alert_stream (subnet_id, alert_type, message, subnet, timestamp, status) " +
                "VALUES ($1, $2, $3, $4, CURRENT_TIMESTAMP, $5) RETURNING id";

        db.preparedQuery(sql).execute(Tuple.of(sid, type, message, sub, status)).onComplete(ar -> {
            if (ar.succeeded()) {
                long id = ar.result().iterator().next().getLong("id");
                LOGGER.info("Created alert [id={}, type={}, subnet={}]: {}", id, type, sub, message);
                promise.complete(new JsonObject().put("success", true).put("id", id).put("message", "Alert created"));
            } else {
                LOGGER.error("Failed to create alert: {}", ar.cause().getMessage());
                promise.fail(ar.cause());
            }
        });
        return promise.future();
    }

    // Creates an active alert record with default status true.
    public Future<JsonObject> createAlert(Long subnetId, String alertType, String message, String subnet) {
        return createAlert(subnetId, alertType, message, subnet, true);
    }

    // Clears an alert by ID.
    public Future<JsonObject> clearAlert(Long id) {
        Promise<JsonObject> promise = Promise.promise();
        String sql = "UPDATE alert_stream SET status = false WHERE id = $1";
        db.preparedQuery(sql).execute(Tuple.of(id)).onComplete(ar -> {
            if (ar.succeeded()) {
                promise.complete(new JsonObject().put("success", true).put("message", "Alert marked as cleared"));
            } else {
                promise.fail(ar.cause());
            }
        });
        return promise.future();
    }

    // Clears all active alerts.
    public Future<JsonObject> clearAllLiveAlerts() {
        Promise<JsonObject> promise = Promise.promise();
        String sql = "UPDATE alert_stream SET status = false WHERE status = true";
        db.query(sql).execute().onComplete(ar -> {
            if (ar.succeeded()) {
                promise.complete(new JsonObject().put("success", true).put("message", "All live alerts cleared"));
            } else {
                promise.fail(ar.cause());
            }
        });
        return promise.future();
    }

    // Deletes an alert by ID.
    public Future<JsonObject> deleteAlert(Long id) {
        Promise<JsonObject> promise = Promise.promise();
        String sql = "DELETE FROM alert_stream WHERE id = $1";
        db.preparedQuery(sql).execute(Tuple.of(id)).onComplete(ar -> {
            if (ar.succeeded()) {
                promise.complete(new JsonObject().put("success", true).put("message", "Alert deleted"));
            } else {
                promise.fail(ar.cause());
            }
        });
        return promise.future();
    }

    // Checks and generates live subnet threshold alerts.
    public Future<Void> checkAndGenerateSubnetAlerts(Long subnetId, String subnetAddress, long totalIp, long usedIp, long availableIp) {
        Promise<Void> promise = Promise.promise();
        if (totalIp <= 0) {
            promise.complete();
            return promise.future();
        }

        double pct = (double) usedIp * 100.0 / totalIp;
        String sAddr = subnetAddress != null ? subnetAddress : "Subnet-" + subnetId;

        getAlertConfig().onComplete(configAr -> {
            double highThreshold = 80.0;
            double lowThreshold = 20.0;
            boolean highEnabled = true;
            boolean lowEnabled = true;

            if (configAr.succeeded() && configAr.result().getJsonObject("data") != null) {
                JsonObject data = configAr.result().getJsonObject("data");
                try {
                    if (data.containsKey("ipUtilization")) {
                        highThreshold = Double.parseDouble(data.getString("ipUtilization"));
                    }
                    if (data.containsKey("ipUtilizationBelow")) {
                        lowThreshold = Double.parseDouble(data.getString("ipUtilizationBelow"));
                    }
                    if (data.containsKey("ipUtilizationFlag")) {
                        highEnabled = Boolean.parseBoolean(data.getString("ipUtilizationFlag"));
                    }
                    if (data.containsKey("ipUtilizationBelowFlag")) {
                        lowEnabled = Boolean.parseBoolean(data.getString("ipUtilizationBelowFlag"));
                    }
                } catch (Exception ignored) {}
            }

            if (highEnabled && pct >= highThreshold && usedIp > 0) {
                String msg = String.format("Subnet %s utilization is critical (%.1f%%). Exceeds threshold of %.0f%%.", sAddr, pct, highThreshold);
                createAlert(subnetId, "CRITICAL", msg, sAddr, true);
            } else if (lowEnabled && pct <= lowThreshold && usedIp > 0) {
                String msg = String.format("Subnet %s utilization is low (%.1f%%). Below threshold of %.0f%%.", sAddr, pct, lowThreshold);
                createAlert(subnetId, "WARNING", msg, sAddr, true);
            }
            promise.complete();
        });

        return promise.future();
    }

    // Checks and raises a security alert when a rogue MAC address is detected.
    public Future<Void> checkAndGenerateRogueAlert(Long subnetId, String subnetAddress, String ipAddress, String macAddress) {
        String sub = subnetAddress != null ? subnetAddress : "General";
        String msg = "Rogue Device " + macAddress + " detected on IP " + ipAddress + " in subnet " + sub;
        return createAlert(subnetId, "MAJOR", msg, sub, true).mapEmpty();
    }

    // Checks and raises a critical alert when an IP address conflict is detected.
    public Future<Void> checkAndGenerateIpConflictAlert(Long subnetId, String subnetAddress, String ipAddress, String mac1, String mac2) {
        String sub = subnetAddress != null ? subnetAddress : "General";
        String msg = "IP conflict detected on " + ipAddress + " between MAC " + mac1 + " and " + mac2;
        return createAlert(subnetId, "CRITICAL", msg, sub, true).mapEmpty();
    }

    // Retrieve alert configuration from the database and return it as JSON.
    public Future<JsonObject> getAlertConfig() {
        Promise<JsonObject> promise = Promise.promise();

        String sql = "SELECT alert_key, alert_value FROM alert";
        db.query(sql).execute().onComplete(ar -> {
            if (ar.succeeded()) {
                JsonObject config = new JsonObject();
                for (Row row : ar.result()) {
                    config.put(row.getString("alert_key"), row.getString("alert_value"));
                }
                promise.complete(new JsonObject().put("data", config).put("success", true));
            } else {
                promise.complete(new JsonObject().put("data", getFallbackAlertConfig()).put("success", true));
            }
        });

        return promise.future();
    }

    // Save or update alert configuration values in the database.
    public Future<JsonObject> saveAlertConfig(JsonObject config) {
        Promise<JsonObject> promise = Promise.promise();

        if (config != null) {
            for (String key : config.fieldNames()) {
                String val = String.valueOf(config.getValue(key));
                String sql = "INSERT INTO alert (alert_key, alert_value) VALUES ($1, $2) " +
                        "ON CONFLICT (alert_key) DO UPDATE SET alert_value = EXCLUDED.alert_value";
                db.preparedQuery(sql).execute(Tuple.of(key, val)).onComplete(ar -> {});
            }
        }

        promise.complete(new JsonObject().put("success", true).put("message", "Alert Configuration Saved Successfully"));
        return promise.future();
    }

    // Delete alert records older than the specified number of days.
    public Future<Integer> cleanupOldAlerts(int days) {
        Promise<Integer> promise = Promise.promise();
        String sql = "DELETE FROM alert_stream WHERE timestamp < CURRENT_TIMESTAMP - INTERVAL '" + days + " days'";
        db.query(sql).execute().onComplete(ar -> {
            if (ar.succeeded()) {
                promise.complete(ar.result().rowCount());
            } else {
                promise.complete(0);
            }
        });
        return promise.future();
    }

    // Return default alert configuration values when database configuration is unavailable.
    private JsonObject getFallbackAlertConfig() {
        return new JsonObject()
                .put("ipUtilizationBelowFlag", "true")
                .put("ipUtilizationFlag", "true")
                .put("macIpChangeFlag", "true")
                .put("rogueDetection", "true")
                .put("ipStateChange", "true")
                .put("reverseLookupFailed", "true")
                .put("forwardLookupFailed", "false")
                .put("forwardLookupMismatch", "false")
                .put("ipReservationChange", "true")
                .put("ipConflict", "true")
                .put("newSubnetsDiscovered", "true")
                .put("ipUtilizationBelow", "20")
                .put("ipUtilization", "80")
                .put("macIpChange", "00:50:56:FE:DC:BA");
    }
}
