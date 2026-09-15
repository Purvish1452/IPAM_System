package com.motadata.ipam.service;

import com.motadata.ipam.verticle.ReportWorkerVerticle;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
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

    private final Vertx vertx;
    private final Pool db;

    // Constructs EventService with the specified database connection pool.
    public EventService(Pool db) {
        this(null, db);
    }

    // Constructs EventService with Vertx instance and database connection pool.
    public EventService(Vertx vertx, Pool db) {
        this.vertx = vertx;
        this.db = db;
    }

    // Fetch paginated event logs from PostgreSQL and return them as JSON.
    public Future<JsonObject> getEvents(Integer page, Integer pageSize) {
        return getEvents(page, pageSize, null);
    }

    // Retrieves event logs with pagination and timeline filtering.
    public Future<JsonObject> getEvents(Integer page, Integer pageSize, String exportTimeline) {
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

        return db.query(countSql).execute().compose(countRows -> {
            long total = (countRows.size() > 0) ? countRows.iterator().next().getLong("total") : 0L;

            return db.preparedQuery(dataSql).execute(Tuple.of(size, offset)).map(dataRows -> {
                JsonArray list = new JsonArray();
                for (Row row : dataRows) {
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

                return new JsonObject()
                        .put("data", list)
                        .put("total", total)
                        .put("success", true);
            });
        });
    }

    // Generates a CSV audit report byte array for the specified timeline via ReportWorkerVerticle.
    public Future<byte[]> generateEventCsvReport(String exportTimeline) {
        String whereClause = "";
        if ("0".equals(exportTimeline)) {
            whereClause = " WHERE timestamp >= CURRENT_DATE ";
        } else if ("7".equals(exportTimeline)) {
            whereClause = " WHERE timestamp >= CURRENT_DATE - INTERVAL '7 days' ";
        } else if ("30".equals(exportTimeline)) {
            whereClause = " WHERE timestamp >= CURRENT_DATE - INTERVAL '30 days' ";
        }
        String sql = "SELECT id, event_type, event_context, message, user_name, timestamp FROM event" + whereClause + " ORDER BY id DESC";

        return db.query(sql).execute().compose(rows -> {
            JsonArray data = new JsonArray();
            for (Row row : rows) {
                Date ts = row.getLocalDateTime("timestamp") != null ?
                        java.sql.Timestamp.valueOf(row.getLocalDateTime("timestamp")) : new Date();
                data.add(new JsonObject()
                        .put("id", row.getLong("id"))
                        .put("eventType", row.getString("event_type") != null ? row.getString("event_type") : "")
                        .put("context", row.getString("event_context") != null ? row.getString("event_context") : "")
                        .put("description", row.getString("message") != null ? row.getString("message") : "")
                        .put("user", row.getString("user_name") != null ? row.getString("user_name") : "")
                        .put("timestamp", DATE_FORMAT.format(ts)));
            }

            JsonArray columns = new JsonArray()
                    .add(new JsonObject().put("property", "id").put("title", "ID"))
                    .add(new JsonObject().put("property", "eventType").put("title", "Event Type"))
                    .add(new JsonObject().put("property", "context").put("title", "Context"))
                    .add(new JsonObject().put("property", "description").put("title", "Description"))
                    .add(new JsonObject().put("property", "user").put("title", "User"))
                    .add(new JsonObject().put("property", "timestamp").put("title", "Timestamp"));

            String subLabel = "Timeline_" + (exportTimeline != null ? exportTimeline : "all");
            JsonObject payload = new JsonObject()
                    .put("title", "Event Audit Log Report")
                    .put("subLabel", subLabel)
                    .put("data", data)
                    .put("columns", columns);

            if (vertx != null) {
                LOGGER.info("Dispatching Event CSV report to ReportWorkerVerticle (records={})", data.size());
                return vertx.eventBus().<Buffer>request(ReportWorkerVerticle.ADDR_GENERATE_CSV, payload)
                        .map(reply -> reply.body().getBytes());
            } else {
                StringBuilder csv = new StringBuilder("ID,Event Type,Context,Description,User,Timestamp\n");
                for (int i = 0; i < data.size(); i++) {
                    JsonObject r = data.getJsonObject(i);
                    csv.append(r.getLong("id")).append(',')
                            .append(csvValue(r.getString("eventType"))).append(',')
                            .append(csvValue(r.getString("context"))).append(',')
                            .append(csvValue(r.getString("description"))).append(',')
                            .append(csvValue(r.getString("user"))).append(',')
                            .append(csvValue(r.getString("timestamp"))).append('\n');
                }
                return Future.succeededFuture(csv.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8));
            }
        });
    }

    // Escapes special characters for safe inclusion in CSV fields.
    private static String csvValue(String value) {
        if (value == null || "null".equals(value)) return "";
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }

    // Seeds initial event audit records into the database.
    public Future<Void> seedInitialEvents() {
        String seedSql = "INSERT INTO event (event_type, event_context, message, user_name, timestamp) VALUES " +
                "('Information', 'Subnet Management', 'Subnet 192.168.10.0/24 created in IP Address Manager by admin', 'admin', CURRENT_TIMESTAMP - INTERVAL '15 minutes'), " +
                "('Information', 'Discovery', 'Gateway scan initiated for gateway 172.16.14.7 by admin', 'admin', CURRENT_TIMESTAMP - INTERVAL '30 minutes'), " +
                "('Information', 'DHCP Management', 'DHCP Scope Office-Pool lease synchronized successfully', 'admin', CURRENT_TIMESTAMP - INTERVAL '1 hour'), " +
                "('Warning', 'IP Conflict', 'IP conflict alert triggered on IP 10.0.0.45', 'system', CURRENT_TIMESTAMP - INTERVAL '2 hours'), " +
                "('Information', 'Authentication', 'User admin logged in successfully from 127.0.0.1', 'admin', CURRENT_TIMESTAMP - INTERVAL '3 hours') " +
                "ON CONFLICT DO NOTHING";
        return db.query(seedSql).execute()
                .onSuccess(v -> LOGGER.info("Seeded initial event logs into event table."))
                .onFailure(err -> LOGGER.warn("Initial event seeding skipped: {}", err.getMessage()))
                .mapEmpty();
    }

    // Return the monthly event count summary.
    public Future<JsonArray> getEventSummary() {
        String sql = "SELECT TO_CHAR(timestamp, 'Mon') AS month, COUNT(*) AS count, " +
                "EXTRACT(MONTH FROM timestamp) AS month_number " +
                "FROM event WHERE timestamp >= CURRENT_DATE - INTERVAL '12 months' " +
                "GROUP BY month, month_number ORDER BY month_number";
        return db.query(sql).execute().map(rows -> {
            JsonArray result = new JsonArray();
            for (Row row : rows) {
                result.add(new JsonObject()
                        .put("month", row.getString("month"))
                        .put("count", row.getLong("count")));
            }
            return result;
        });
    }

    // Return the most frequent event types.
    public Future<JsonArray> getTopEvents() {
        String sql = "SELECT event_type AS \"eventType\", COUNT(*) AS count " +
                "FROM event GROUP BY event_type ORDER BY count DESC, event_type ASC LIMIT 10";
        return db.query(sql).execute().map(rows -> {
            JsonArray result = new JsonArray();
            for (Row row : rows) {
                String eventType = row.getString("eventType") != null ? row.getString("eventType") : row.getString(0);
                Long count = row.getLong("count") != null ? row.getLong("count") : row.getLong(1);
                result.add(new JsonObject()
                        .put("eventType", eventType)
                        .put("count", count));
            }
            return result;
        });
    }

    // Asynchronously insert a new system event into PostgreSQL.
    public Future<Void> logEvent(String eventType, String context, String message, String userName) {
        String sql = "INSERT INTO event (event_type, event_context, message, user_name, timestamp) VALUES ($1, $2, $3, $4, CURRENT_TIMESTAMP)";
        return db.preparedQuery(sql).execute(Tuple.of(eventType, context, message, userName != null ? userName : "system")).mapEmpty();
    }
}
