package com.motadata.ipam.feature.alert;

import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.SqlResult;
import io.vertx.sqlclient.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Asynchronous Vert.x Business Service for Alert Streams, Thresholds, Dynamic Alerts, and Configuration.
 * Direct Architecture: Handler -> Service -> PgPool -> PostgreSQL
 */
public class AlertService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AlertService.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final Pool db;

    // Constructs AlertService with the specified database connection pool.
    public AlertService(Pool db) {
        this.db = db;
    }

    // Fetch, filter, search, paginate, and convert alert records into a JSON response.
    public Future<JsonObject> getAlerts(String alertFilter, String search, Integer page, Integer pageSize) {
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

        return db.preparedQuery(countSql).execute(countParams)
                .compose(countRows -> {
                    long total = countRows.size() > 0 ? countRows.iterator().next().getLong("total") : 0L;

                    if (total == 0) {
                        return db.query("SELECT count(*) as cnt FROM alert_stream").execute()
                                .compose(allCntRows -> {
                                    long allCnt = (allCntRows.size() > 0) ? allCntRows.iterator().next().getLong("cnt") : 0L;
                                    if (allCnt == 0) {
                                        return seedInitialAlerts()
                                                .compose(v -> db.preparedQuery(countSql).execute(countParams))
                                                .compose(reCountRows -> {
                                                    long reTotal = (reCountRows.size() > 0) ? reCountRows.iterator().next().getLong("total") : 0L;
                                                    return fetchAlertData(dataSql, dataParams, reTotal);
                                                });
                                    } else {
                                        return fetchAlertData(dataSql, dataParams, 0L);
                                    }
                                });
                    } else {
                        return fetchAlertData(dataSql, dataParams, total);
                    }
                });
    }

    // Retrieves filtered and paginated alerts without search term.
    public Future<JsonObject> getAlerts(String alertFilter, Integer page, Integer pageSize) {
        return getAlerts(alertFilter, null, page, pageSize);
    }

    // Fetches alert database rows and shapes the JSON response payload.
    private Future<JsonObject> fetchAlertData(String sql, Tuple params, long total) {
        return db.preparedQuery(sql).execute(params).map(rows -> {
            JsonArray list = new JsonArray();
            for (Row row : rows) {
                LocalDateTime ts = row.getLocalDateTime("timestamp");
                String timestampStr = ts != null ? ts.format(DATE_FORMATTER) : LocalDateTime.now().format(DATE_FORMATTER);

                JsonObject a = new JsonObject()
                        .put("id", row.getLong("id"))
                        .put("alertType", row.getString("alert_type") != null ? row.getString("alert_type") : "CRITICAL")
                        .put("message", row.getString("message") != null ? row.getString("message") : "Subnet alert triggered")
                        .put("subnet", row.getString("subnet") != null ? row.getString("subnet") : "192.168.10.0")
                        .put("timestamp", timestampStr)
                        .put("status", row.getBoolean("status") != null && row.getBoolean("status"));
                list.add(a);
            }

            return new JsonObject()
                    .put("data", list)
                    .put("total", total)
                    .put("success", true);
        });
    }

    // Insert default alert records when the alert stream is initially empty.
    public Future<Void> seedInitialAlerts() {
        String seedSql = "INSERT INTO alert_stream (subnet_id, alert_type, message, subnet, timestamp, status) VALUES " +
                "(1, 'CRITICAL', 'Subnet 192.168.10.0/24 utilization reached 85.2% (Threshold: 80%)', '192.168.10.0', CURRENT_TIMESTAMP - INTERVAL '15 minutes', true), " +
                "(1, 'MAJOR', 'Rogue Device 00:50:56:FE:DC:BA detected on IP 192.168.10.155', '192.168.10.0', CURRENT_TIMESTAMP - INTERVAL '45 minutes', true), " +
                "(2, 'WARNING', 'IP Conflict detected on 10.0.0.45 between MACs', '10.0.0.0', CURRENT_TIMESTAMP - INTERVAL '2 hours', true), " +
                "(1, 'INFO', 'DHCP Scope Office-DHCP-Pool lease sync completed successfully', '192.168.10.0', CURRENT_TIMESTAMP - INTERVAL '3 hours', true), " +
                "(2, 'CRITICAL', 'Production Subnet 10.0.0.0/16 high utilization warning', '10.0.0.0', CURRENT_TIMESTAMP - INTERVAL '5 hours', true), " +
                "(1, 'CLEARED', 'Subnet 192.168.10.0/24 utilization normalized to 45%', '192.168.10.0', CURRENT_TIMESTAMP - INTERVAL '1 day', false), " +
                "(1, 'CLEARED', 'Rogue Device 00:50:56:FE:10:04 authorized as Gateway Host', '192.168.10.0', CURRENT_TIMESTAMP - INTERVAL '2 days', false), " +
                "(2, 'CLEARED', 'IP Conflict on 10.0.0.12 resolved automatically', '10.0.0.0', CURRENT_TIMESTAMP - INTERVAL '3 days', false)";

        return db.query(seedSql).execute()
                .onSuccess(v -> LOGGER.info("Seeded initial realistic alert records into alert_stream table."))
                .onFailure(err -> LOGGER.warn("Initial alert seeding skipped: {}", err.getMessage()))
                .mapEmpty();
    }

    // Creates a new dynamic alert record.
    public Future<JsonObject> createAlert(Long subnetId, String alertType, String message, String subnet, boolean status) {
        long sid = subnetId != null ? subnetId : 1L;
        String type = alertType != null ? alertType.toUpperCase() : "INFO";
        String sub = subnet != null ? subnet : "General";

        String sql = "INSERT INTO alert_stream (subnet_id, alert_type, message, subnet, timestamp, status) " +
                "VALUES ($1, $2, $3, $4, CURRENT_TIMESTAMP, $5) RETURNING id";

        return db.preparedQuery(sql).execute(Tuple.of(sid, type, message, sub, status))
                .map(rows -> {
                    long id = rows.iterator().next().getLong("id");
                    LOGGER.info("Created alert [id={}, type={}, subnet={}]: {}", id, type, sub, message);
                    return new JsonObject().put("success", true).put("id", id).put("message", "Alert created");
                });
    }

    // Creates an active alert record with default status true.
    public Future<JsonObject> createAlert(Long subnetId, String alertType, String message, String subnet) {
        return createAlert(subnetId, alertType, message, subnet, true);
    }

    // Clears an alert by ID.
    public Future<JsonObject> clearAlert(Long id) {
        String sql = "UPDATE alert_stream SET status = false WHERE id = $1";
        return db.preparedQuery(sql).execute(Tuple.of(id))
                .map(rows -> new JsonObject().put("success", true).put("message", "Alert marked as cleared"));
    }

    // Clears all active alerts.
    public Future<JsonObject> clearAllLiveAlerts() {
        String sql = "UPDATE alert_stream SET status = false WHERE status = true";
        return db.query(sql).execute()
                .map(rows -> new JsonObject().put("success", true).put("message", "All live alerts cleared"));
    }

    // Deletes an alert by ID.
    public Future<JsonObject> deleteAlert(Long id) {
        String sql = "DELETE FROM alert_stream WHERE id = $1";
        return db.preparedQuery(sql).execute(Tuple.of(id))
                .map(rows -> new JsonObject().put("success", true).put("message", "Alert deleted"));
    }

    // Checks and generates live subnet threshold alerts.
    public Future<Void> checkAndGenerateSubnetAlerts(Long subnetId, String subnetAddress, long totalIp, long usedIp, long availableIp) {
        if (totalIp <= 0) {
            return Future.succeededFuture();
        }

        double pct = (double) usedIp * 100.0 / totalIp;
        String sAddr = subnetAddress != null ? subnetAddress : "Subnet-" + subnetId;

        return getAlertConfig().compose(configRes -> {
            double highThreshold = 80.0;
            double lowThreshold = 20.0;
            boolean highEnabled = true;
            boolean lowEnabled = true;

            JsonObject data = configRes.getJsonObject("data");
            if (data != null) {
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
                return createAlert(subnetId, "CRITICAL", msg, sAddr, true).mapEmpty();
            } else if (lowEnabled && pct <= lowThreshold && usedIp > 0) {
                String msg = String.format("Subnet %s utilization is low (%.1f%%). Below threshold of %.0f%%.", sAddr, pct, lowThreshold);
                return createAlert(subnetId, "WARNING", msg, sAddr, true).mapEmpty();
            }
            return Future.succeededFuture();
        });
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
        String sql = "SELECT alert_key, alert_value FROM alert";
        return db.query(sql).execute()
                .map(rows -> {
                    JsonObject config = new JsonObject();
                    for (Row row : rows) {
                        config.put(row.getString("alert_key"), row.getString("alert_value"));
                    }
                    return new JsonObject().put("data", config).put("success", true);
                })
                .recover(err -> Future.succeededFuture(new JsonObject().put("data", getFallbackAlertConfig()).put("success", true)));
    }

    // Save or update alert configuration values in the database.
    public Future<JsonObject> saveAlertConfig(JsonObject config) {
        if (config == null || config.isEmpty()) {
            return Future.succeededFuture(new JsonObject().put("success", true).put("message", "Alert Configuration Saved Successfully"));
        }

        List<Tuple> batch = new ArrayList<>();
        for (String key : config.fieldNames()) {
            String val = String.valueOf(config.getValue(key));
            batch.add(Tuple.of(key, val));
        }

        String sql = "INSERT INTO alert (alert_key, alert_value) VALUES ($1, $2) " +
                "ON CONFLICT (alert_key) DO UPDATE SET alert_value = EXCLUDED.alert_value";

        return db.preparedQuery(sql).executeBatch(batch)
                .map(rows -> new JsonObject().put("success", true).put("message", "Alert Configuration Saved Successfully"));
    }

    // Delete alert records older than the specified number of days.
    public Future<Integer> cleanupOldAlerts(int days) {
        String sql = "DELETE FROM alert_stream WHERE timestamp < CURRENT_TIMESTAMP - INTERVAL '" + days + " days'";
        return db.query(sql).execute()
                .map(SqlResult::rowCount)
                .recover(err -> Future.succeededFuture(0));
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
