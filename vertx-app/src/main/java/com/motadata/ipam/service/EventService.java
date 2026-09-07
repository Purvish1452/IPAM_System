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
import java.util.Date;

/**
 * Asynchronous Vert.x Business Service for System & Audit Event Logging.
 * Direct Architecture: Handler -> Service -> PgPool -> PostgreSQL
 */
public class EventService {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventService.class);
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private final Pool db;

    public EventService(Pool db) {
        this.db = db;
    }

    // Fetch paginated event logs from PostgreSQL and return them as JSON.
    public Future<JsonObject> getEvents(Integer page, Integer pageSize) {
        return getEvents(page, pageSize, null);
    }

    public Future<JsonObject> getEvents(Integer page, Integer pageSize, String exportTimeline) {
        Promise<JsonObject> promise = Promise.promise();

        int p = (page == null || page < 1) ? 1 : page;
        int size = (pageSize == null || pageSize < 1) ? 20 : pageSize;
        int offset = (p - 1) * size;

        String whereClause = "";
        if ("0".equals(exportTimeline)) {
            whereClause = " WHERE timestamp >= CURRENT_DATE ";
        } else if ("7".equals(exportTimeline)) {
            whereClause = " WHERE timestamp >= CURRENT_DATE - INTERVAL '7 days' ";
        } else if ("30".equals(exportTimeline)) {
            whereClause = " WHERE timestamp >= CURRENT_DATE - INTERVAL '30 days' ";
        }

        String countSql = "SELECT count(*) as total FROM event" + whereClause;
        String dataSql = "SELECT id, event_type, event_context, message, user_name, timestamp " +
                "FROM event" + whereClause + " ORDER BY id DESC LIMIT $1 OFFSET $2";

        db.query(countSql).execute().onComplete(countAr -> {
            long total = 0;
            if (countAr.succeeded() && countAr.result().size() > 0) {
                total = countAr.result().iterator().next().getLong("total");
            }

            final long finalTotal = total;
            db.preparedQuery(dataSql).execute(Tuple.of(size, offset)).onComplete(dataAr -> {
                if (dataAr.succeeded()) {
                    JsonArray list = new JsonArray();
                    for (Row row : dataAr.result()) {
                        Date ts = row.getLocalDateTime("timestamp") != null ?
                                java.sql.Timestamp.valueOf(row.getLocalDateTime("timestamp")) : new Date();

                        String user = row.getString("user_name") != null ? row.getString("user_name") : "admin";
                        String msg = row.getString("message") != null ? row.getString("message") : "Subnet operation completed";

                        JsonObject e = new JsonObject()
                                .put("id", row.getLong("id"))
                                .put("generatedTime", ts.getTime())
                                .put("eventLog", msg)
                                .put("message", msg)
                                .put("eventType", row.getString("event_type") != null ? row.getString("event_type") : "Information")
                                .put("eventContext", row.getString("event_context") != null ? row.getString("event_context") : "Subnet Management")
                                .put("ipAddress", "")
                                .put("userName", user)
                                .put("username", user)
                                .put("doneBy", new JsonObject().put("id", 1).put("userName", user))
                                .put("timestamp", DATE_FORMAT.format(ts));
                        list.add(e);
                    }

                    JsonObject response = new JsonObject()
                            .put("data", list)
                            .put("total", finalTotal)
                            .put("success", true);
                    promise.complete(response);
                } else {
                    promise.fail(dataAr.cause());
                }
            });
        });

        return promise.future();
    }

    public Future<byte[]> generateEventCsvReport(String exportTimeline) {
        StringBuilder csv = new StringBuilder("ID,Event Type,Context,Description,User,Timestamp\n");
        String whereClause = "";
        if ("0".equals(exportTimeline)) {
            whereClause = " WHERE timestamp >= CURRENT_DATE ";
        } else if ("7".equals(exportTimeline)) {
            whereClause = " WHERE timestamp >= CURRENT_DATE - INTERVAL '7 days' ";
        } else if ("30".equals(exportTimeline)) {
            whereClause = " WHERE timestamp >= CURRENT_DATE - INTERVAL '30 days' ";
        }
        String sql = "SELECT id, event_type, event_context, message, user_name, timestamp FROM event" + whereClause + " ORDER BY id DESC";
        return db.query(sql).execute().map(rows -> {
            for (Row row : rows) {
                Date ts = row.getLocalDateTime("timestamp") != null ?
                        java.sql.Timestamp.valueOf(row.getLocalDateTime("timestamp")) : new Date();
                csv.append(row.getLong("id")).append(',')
                        .append(csvValue(row.getString("event_type"))).append(',')
                        .append(csvValue(row.getString("event_context"))).append(',')
                        .append(csvValue(row.getString("message"))).append(',')
                        .append(csvValue(row.getString("user_name"))).append(',')
                        .append(csvValue(DATE_FORMAT.format(ts))).append('\n');
            }
            return csv.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
        });
    }

    private static String csvValue(String value) {
        if (value == null || "null".equals(value)) return "";
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }


    private void seedInitialEvents() {
        String seedSql = "INSERT INTO event (event_type, event_context, message, user_name, timestamp) VALUES " +
                "('Information', 'Subnet Management', 'Subnet 192.168.10.0/24 created in IP Address Manager by admin', 'admin', CURRENT_TIMESTAMP - INTERVAL '15 minutes'), " +
                "('Information', 'Discovery', 'Gateway scan initiated for gateway 172.16.14.7 by admin', 'admin', CURRENT_TIMESTAMP - INTERVAL '30 minutes'), " +
                "('Information', 'DHCP Management', 'DHCP Scope Office-Pool lease synchronized successfully', 'admin', CURRENT_TIMESTAMP - INTERVAL '1 hour'), " +
                "('Warning', 'IP Conflict', 'IP conflict alert triggered on IP 10.0.0.45', 'system', CURRENT_TIMESTAMP - INTERVAL '2 hours'), " +
                "('Information', 'Authentication', 'User admin logged in successfully from 127.0.0.1', 'admin', CURRENT_TIMESTAMP - INTERVAL '3 hours') " +
                "ON CONFLICT DO NOTHING";
        db.query(seedSql).execute().onComplete(ar -> {
            if (ar.succeeded()) {
                LOGGER.info("Seeded initial event logs into event table.");
            }
        });
    }

    // Return the monthly event count summary.
    public Future<JsonArray> getEventSummary() {
        Promise<JsonArray> promise = Promise.promise();
        String sql = "SELECT TO_CHAR(timestamp, 'Mon') AS month, COUNT(*) AS count, " +
                "EXTRACT(MONTH FROM timestamp) AS month_number " +
                "FROM event WHERE timestamp >= CURRENT_DATE - INTERVAL '12 months' " +
                "GROUP BY month, month_number ORDER BY month_number";
        db.query(sql).execute().onComplete(ar -> {
            if (ar.succeeded()) {
                JsonArray result = new JsonArray();
                for (Row row : ar.result()) {
                    result.add(new JsonObject()
                            .put("month", row.getString("month"))
                            .put("count", row.getLong("count")));
                }
                promise.complete(result);
            } else {
                promise.fail(ar.cause());
            }
        });
        return promise.future();
    }

    // Return the most frequent event types.
    public Future<JsonArray> getTopEvents() {
        Promise<JsonArray> promise = Promise.promise();
        String sql = "SELECT event_type AS eventType, COUNT(*) AS count " +
                "FROM event GROUP BY event_type ORDER BY count DESC, event_type ASC LIMIT 10";
        db.query(sql).execute().onComplete(ar -> {
            if (ar.succeeded()) {
                JsonArray result = new JsonArray();
                for (Row row : ar.result()) {
                    result.add(new JsonObject()
                            .put("eventType", row.getString("eventType"))
                            .put("count", row.getLong("count")));
                }
                promise.complete(result);
            } else {
                promise.fail(ar.cause());
            }
        });
        return promise.future();
    }

    // Asynchronously insert a new system event into PostgreSQL.
    public Future<Void> logEvent(String eventType, String context, String message, String userName) {
        Promise<Void> promise = Promise.promise();
        String sql = "INSERT INTO event (event_type, event_context, message, user_name, timestamp) VALUES ($1, $2, $3, $4, CURRENT_TIMESTAMP)";
        db.preparedQuery(sql).execute(Tuple.of(eventType, context, message, userName != null ? userName : "system")).onComplete(ar -> {
            if (ar.succeeded()) {
                promise.complete();
            } else {
                promise.fail(ar.cause());
            }
        });
        return promise.future();
    }
}
