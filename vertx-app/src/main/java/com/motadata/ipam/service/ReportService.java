package com.motadata.ipam.service;

import com.motadata.ipam.model.AlertStream;
import com.motadata.ipam.model.Event;
import com.motadata.ipam.model.SubnetDetails;
import com.motadata.ipam.verticle.ReportWorkerVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Reactive Business Service for Report Scheduling and Document Generation.
 * Architecture: Event Loop Handler -> Service -> PgPool / EventBus ReportWorkerVerticle.
 * Zero blocking operations on the Event Loop.
 */
public class ReportService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReportService.class);
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private final Vertx vertx;
    private final Pool db;

    public ReportService(Vertx vertx, Pool db) {
        this.vertx = vertx;
        this.db = db;
    }

    public Future<JsonArray> getReportSchedulers() {
        Promise<JsonArray> promise = Promise.promise();
        String sql = "SELECT id, schedule_name, report_type, schedule_time, schedule_status, recipients FROM report ORDER BY id ASC";
        db.query(sql).execute().onComplete(ar -> {
            if (ar.succeeded()) {
                JsonArray result = new JsonArray();
                for (Row row : ar.result()) {
                    result.add(new JsonObject()
                            .put("id", row.getLong("id"))
                            .put("scheduleName", row.getString("schedule_name"))
                            .put("reportType", row.getString("report_type"))
                            .put("scheduleTime", row.getString("schedule_time"))
                            .put("scheduleStatus", row.getBoolean("schedule_status"))
                            .put("recipients", row.getString("recipients")));
                }
                promise.complete(result);
            } else {
                promise.fail(ar.cause());
            }
        });
        return promise.future();
    }

    public Future<JsonObject> getReportSchedulerById(Long id) {
        Promise<JsonObject> promise = Promise.promise();
        db.preparedQuery("SELECT id, schedule_name, report_type, schedule_time, schedule_status, recipients " +
                "FROM report WHERE id = $1").execute(Tuple.of(id)).onComplete(ar -> {
            if (ar.succeeded() && ar.result().iterator().hasNext()) {
                Row row = ar.result().iterator().next();
                promise.complete(new JsonObject()
                        .put("id", row.getLong("id"))
                        .put("scheduleName", row.getString("schedule_name"))
                        .put("reportType", row.getString("report_type"))
                        .put("scheduleTime", row.getString("schedule_time"))
                        .put("scheduleStatus", row.getBoolean("schedule_status"))
                        .put("recipients", row.getString("recipients")));
            } else if (ar.succeeded()) {
                promise.fail("Report schedule " + id + " was not found");
            } else {
                promise.fail(ar.cause());
            }
        });
        return promise.future();
    }

    public Future<JsonObject> saveReportScheduler(JsonObject json) {
        Promise<JsonObject> promise = Promise.promise();
        String name = json.getString("scheduleName", "Report Schedule");
        String type = json.getString("reportType", "PDF");
        String time = json.getString("scheduleTime", "09:00");
        Long id = json.getLong("id");
        String sql = id == null
                ? "INSERT INTO report (schedule_name, report_type, schedule_time, schedule_status, recipients) VALUES ($1, $2, $3, true, $4)"
                : "UPDATE report SET schedule_name = $1, report_type = $2, schedule_time = $3, recipients = $4 WHERE id = $5";
        String recipients = json.getString("recipients", "");
        Tuple params = id == null ? Tuple.of(name, type, time, recipients) : Tuple.of(name, type, time, recipients, id);
        db.preparedQuery(sql).execute(params).onComplete(ar -> {
            if (ar.succeeded()) {
                promise.complete(new JsonObject().put("success", true).put("message", "Report Schedule Saved Successfully"));
            } else {
                promise.fail(ar.cause());
            }
        });
        return promise.future();
    }

    public Future<JsonObject> deleteReportScheduler(Long id) {
        Promise<JsonObject> promise = Promise.promise();
        String sql = "DELETE FROM report WHERE id = $1";
        db.preparedQuery(sql).execute(Tuple.of(id)).onComplete(ar -> {
            if (ar.succeeded()) {
                promise.complete(new JsonObject().put("success", true).put("message", "Report Schedule Deleted"));
            } else {
                promise.fail(ar.cause());
            }
        });
        return promise.future();
    }

    public Future<JsonArray> getSubnetByReport() {
        Promise<JsonArray> promise = Promise.promise();
        String sql = "SELECT id, subnet_name, subnet_address FROM subnet_details ORDER BY id ASC";
        db.query(sql).execute().onComplete(ar -> {
            JsonArray result = new JsonArray();
            if (ar.succeeded() && ar.result().size() > 0) {
                for (Row row : ar.result()) {
                    long id = row.getLong("id");
                    String addr = row.getString("subnet_address");
                    String name = row.getString("subnet_name") != null ? row.getString("subnet_name") : addr;

                    JsonArray children = new JsonArray()
                            .add(new JsonObject().put("id", id).put("subnetName", "All IP").put("networkInterface", "ALL"))
                            .add(new JsonObject().put("id", id).put("subnetName", "Used IP").put("networkInterface", "USED"))
                            .add(new JsonObject().put("id", id).put("subnetName", "Available IP").put("networkInterface", "AVAILABLE"))
                            .add(new JsonObject().put("id", id).put("subnetName", "Reserved IP").put("networkInterface", "RESERVED"))
                            .add(new JsonObject().put("id", id).put("subnetName", "Transient IP").put("networkInterface", "TRANSIENT"))
                            .add(new JsonObject().put("id", id).put("subnetName", "Rogue IP").put("networkInterface", "ROGUE"))
                            .add(new JsonObject().put("id", id).put("subnetName", "Trusted IP").put("networkInterface", "TRUSTED"))
                            .add(new JsonObject().put("id", id).put("subnetName", "Vendor Summary").put("networkInterface", "VENDOR SUMMARY"));

                    result.add(new JsonObject()
                            .put("id", id)
                            .put("subnetAddress", name)
                            .put("subnets", children));
                }
            }
            if (ar.succeeded()) promise.complete(result);
            else promise.fail(ar.cause());
        });
        return promise.future();
    }

    public Future<JsonArray> getSubnetIpByReportTimeline(Long subnetId, String status) {
        return getSubnetIpByReportTimeline(subnetId != null ? List.of(subnetId) : List.of(), status);
    }

    public Future<JsonArray> getSubnetIpByReportTimeline(List<Long> subnetIds, String status) {
        Promise<JsonArray> promise = Promise.promise();
        String normalizedStatus = normalizeStatus(status);

        if ("VENDOR SUMMARY".equals(normalizedStatus)) {
            return getVendorSummaryReport(subnetIds);
        }

        StringBuilder sql = new StringBuilder(
                "SELECT ip.id, ip.ip_address, ip.mac_address, ip.status, ip.host_name, " +
                        "ip.dns_status, COALESCE(NULLIF(TRIM(ip.device_type), ''), NULLIF(TRIM(ip.vendor), ''), 'Unknown') AS device_type, " +
                        "ip.last_scan_time, ip.subnet_id, s.subnet_address, s.subnet_name, " +
                        "COALESCE(r.authenticity, CASE WHEN UPPER(ip.status) = 'ROGUE' THEN 'UNAUTHORIZED' ELSE 'TRUSTED' END) AS authenticity " +
                        "FROM subnet_ip_details ip " +
                        "LEFT JOIN subnet_details s ON s.id = ip.subnet_id " +
                        "LEFT JOIN (SELECT ip_address, MAX(authenticity) AS authenticity FROM rogue_detection_details GROUP BY ip_address) r " +
                        "  ON r.ip_address = ip.ip_address "
        );

        Tuple tuple = Tuple.tuple();
        List<String> whereClauses = new ArrayList<>();
        int paramIndex = 1;

        if (subnetIds != null && !subnetIds.isEmpty()) {
            StringBuilder inClause = new StringBuilder("ip.subnet_id IN (");
            for (int i = 0; i < subnetIds.size(); i++) {
                if (i > 0) inClause.append(", ");
                inClause.append("$").append(paramIndex++);
                tuple.addLong(subnetIds.get(i));
            }
            inClause.append(")");
            whereClauses.add(inClause.toString());
        }

        if ("USED".equals(normalizedStatus)) {
            whereClauses.add("UPPER(ip.status) = 'USED'");
        } else if ("AVAILABLE".equals(normalizedStatus)) {
            whereClauses.add("UPPER(ip.status) = 'AVAILABLE'");
        } else if ("RESERVED".equals(normalizedStatus)) {
            whereClauses.add("(UPPER(ip.status) = 'RESERVED' OR ip.ip_reserved = true)");
        } else if ("TRANSIENT".equals(normalizedStatus)) {
            whereClauses.add("UPPER(ip.status) = 'TRANSIENT'");
        } else if ("ROGUE".equals(normalizedStatus)) {
            whereClauses.add("(UPPER(ip.status) = 'ROGUE' OR UPPER(COALESCE(r.authenticity, '')) = 'UNAUTHORIZED' OR UPPER(COALESCE(r.authenticity, '')) = 'ROGUE')");
        } else if ("TRUSTED".equals(normalizedStatus)) {
            whereClauses.add("(UPPER(ip.status) != 'ROGUE' AND UPPER(COALESCE(r.authenticity, 'TRUSTED')) != 'UNAUTHORIZED' AND UPPER(COALESCE(r.authenticity, 'TRUSTED')) != 'ROGUE')");
        }

        if (!whereClauses.isEmpty()) {
            sql.append("WHERE ").append(String.join(" AND ", whereClauses)).append(" ");
        }

        sql.append("ORDER BY ip.subnet_id ASC, ip.id ASC");

        db.preparedQuery(sql.toString()).execute(tuple).onComplete(ar -> {
            if (ar.succeeded()) {
                JsonArray list = new JsonArray();
                for (Row row : ar.result()) {
                    String ipStatus = row.getString("status") != null ? row.getString("status").toUpperCase() : "AVAILABLE";
                    Date dt = row.getLocalDateTime("last_scan_time") != null ?
                            java.sql.Timestamp.valueOf(row.getLocalDateTime("last_scan_time")) : new Date();
                    long sid = row.getLong("subnet_id") != null ? row.getLong("subnet_id") : 1L;
                    String sName = row.getString("subnet_name") != null ? row.getString("subnet_name") :
                            (row.getString("subnet_address") != null ? row.getString("subnet_address") : "Subnet-" + sid);
                    String subnetAddress = row.getString("subnet_address") != null
                            ? row.getString("subnet_address") : sName;
                    String deviceType = row.getString("device_type") != null
                            ? row.getString("device_type") : "Unknown";
                    String auth = row.getString("authenticity") != null
                            ? row.getString("authenticity") : ("ROGUE".equals(ipStatus) ? "UNAUTHORIZED" : "TRUSTED");

                    list.add(new JsonObject()
                            .put("id", row.getLong("id"))
                            .put("ipAddress", row.getString("ip_address"))
                            .put("subnetId", new JsonObject()
                                    .put("id", sid)
                                    .put("subnetAddress", subnetAddress))
                            .put("subnetName", sName)
                            .put("macAddress", row.getString("mac_address") != null ? row.getString("mac_address") : "-")
                            .put("status", ipStatus)
                            .put("hostName", row.getString("host_name") != null ? row.getString("host_name") : "host-" + row.getLong("id"))
                            .put("deviceType", deviceType)
                            .put("dnsStatus", row.getString("dns_status") != null ? row.getString("dns_status") : "Resolved")
                            .put("authenticity", auth)
                            .put("lastSeen", DATE_FORMAT.format(dt)));
                }
                promise.complete(list);
            } else {
                LOGGER.error("Failed to query subnet IP report: {}", ar.cause().getMessage());
                promise.fail(ar.cause());
            }
        });

        return promise.future();
    }

    public Future<JsonArray> getVendorSummaryReport(List<Long> subnetIds) {
        Promise<JsonArray> promise = Promise.promise();

        StringBuilder sql = new StringBuilder(
                "SELECT COALESCE(NULLIF(TRIM(ip.device_type), ''), NULLIF(TRIM(ip.vendor), ''), 'Unknown') AS vendor_name, " +
                        "COUNT(*) as vendor_count " +
                        "FROM subnet_ip_details ip "
        );

        Tuple tuple = Tuple.tuple();
        if (subnetIds != null && !subnetIds.isEmpty()) {
            sql.append("WHERE ip.subnet_id IN (");
            for (int i = 0; i < subnetIds.size(); i++) {
                if (i > 0) sql.append(", ");
                sql.append("$").append(i + 1);
                tuple.addLong(subnetIds.get(i));
            }
            sql.append(") ");
        }

        sql.append("GROUP BY vendor_name ORDER BY vendor_count DESC");

        db.preparedQuery(sql.toString()).execute(tuple).onComplete(ar -> {
            if (ar.succeeded()) {
                JsonArray list = new JsonArray();
                long totalCount = 0;
                List<Row> rows = new ArrayList<>();
                for (Row r : ar.result()) {
                    rows.add(r);
                    totalCount += r.getLong("vendor_count");
                }

                for (Row row : rows) {
                    long count = row.getLong("vendor_count");
                    double pct = totalCount > 0 ? Math.round(((double) count / totalCount * 100.0) * 10.0) / 10.0 : 0.0;
                    list.add(new JsonObject()
                            .put("VendorName", row.getString("vendor_name"))
                            .put("VendorCount", count)
                            .put("VendorPercentage", pct));
                }
                promise.complete(list);
            } else {
                LOGGER.error("Failed to query vendor summary report: {}", ar.cause().getMessage());
                promise.fail(ar.cause());
            }
        });

        return promise.future();
    }

    public Future<String> generateSubnetIpPdfReport(Long subnetId, String status) {
        return generateSubnetIpPdfReport(subnetId != null ? List.of(subnetId) : List.of(), status);
    }

    public Future<String> generateSubnetIpPdfReport(List<Long> subnetIds, String status) {
        Promise<String> promise = Promise.promise();
        String normalizedStatus = normalizeStatus(status);
        String subLabel = (subnetIds != null && !subnetIds.isEmpty()) ? String.join("_", subnetIds.stream().map(Object::toString).toList()) : "All";

        if ("VENDOR SUMMARY".equals(normalizedStatus)) {
            getVendorSummaryReport(subnetIds).onComplete(ar -> {
                if (ar.failed()) {
                    promise.fail(ar.cause());
                    return;
                }
                JsonObject payload = new JsonObject()
                        .put("data", ar.result())
                        .put("subLabel", subLabel);
                vertx.eventBus().<JsonObject>request(ReportWorkerVerticle.ADDR_GENERATE_VENDOR_PDF, payload)
                        .onComplete(replyAr -> {
                            if (replyAr.succeeded()) {
                                promise.complete(replyAr.result().body().getString("filename"));
                            } else {
                                promise.fail(replyAr.cause());
                            }
                        });
            });
            return promise.future();
        }

        getSubnetIpByReportTimeline(subnetIds, status).onComplete(ar -> {
            if (ar.failed()) {
                promise.fail(ar.cause());
                return;
            }
            JsonObject payload = new JsonObject()
                    .put("data", ar.result())
                    .put("subLabel", subLabel);
            vertx.eventBus().<JsonObject>request(ReportWorkerVerticle.ADDR_GENERATE_SUBNET_PDF, payload)
                    .onComplete(replyAr -> {
                        if (replyAr.succeeded()) {
                            promise.complete(replyAr.result().body().getString("filename"));
                        } else {
                            promise.fail(replyAr.cause());
                        }
                    });
        });
        return promise.future();
    }

    public Future<byte[]> generateSubnetPdfReport() {
        Promise<byte[]> promise = Promise.promise();
        String sql = "SELECT id, subnet_name, subnet_address, subnet_mask, description, created_by FROM subnet_details ORDER BY id ASC";
        db.query(sql).execute().onComplete(ar -> {
            if (ar.failed()) {
                promise.fail(ar.cause());
                return;
            }
            JsonArray data = new JsonArray();
            for (Row row : ar.result()) {
                data.add(new JsonObject()
                        .put("subnetAddress", row.getString("subnet_address"))
                        .put("subnetMask", row.getString("subnet_mask"))
                        .put("description", row.getString("description"))
                        .put("createdBy", row.getString("created_by") != null ? row.getString("created_by") : "admin"));
            }
            dispatchDynamicJasper("Subnet Utilization Report", data, createSubnetReportColumns()).onComplete(promise);
        });
        return promise.future();
    }

    public Future<byte[]> generateSubnetCsvReport(Long subnetId, String status) {
        return generateSubnetCsvReport(subnetId != null ? List.of(subnetId) : List.of(), status);
    }

    public Future<byte[]> generateSubnetCsvReport(List<Long> subnetIds, String status) {
        String normalizedStatus = normalizeStatus(status);
        if ("VENDOR SUMMARY".equals(normalizedStatus)) {
            return getVendorSummaryReport(subnetIds).map(data -> {
                StringBuilder csv = new StringBuilder("Vendor Name,Vendor Count,Percentage (%)\n");
                for (int i = 0; i < data.size(); i++) {
                    JsonObject row = data.getJsonObject(i);
                    csv.append(csvValue(row.getString("VendorName"))).append(',')
                            .append(row.getLong("VendorCount")).append(',')
                            .append(row.getDouble("VendorPercentage")).append('\n');
                }
                return csv.toString().getBytes(StandardCharsets.UTF_8);
            });
        }

        return getSubnetIpByReportTimeline(subnetIds, status).map(data -> {
            StringBuilder csv = new StringBuilder("ID,IP Address,Scope,Status,MAC Address,Vendor,Host Name,DNS Status,Authenticity,Last Seen\n");
            for (int i = 0; i < data.size(); i++) {
                JsonObject row = data.getJsonObject(i);
                csv.append(row.getLong("id")).append(',')
                        .append(csvValue(row.getString("ipAddress"))).append(',')
                        .append(csvValue(row.getString("subnetName"))).append(',')
                        .append(csvValue(row.getString("status"))).append(',')
                        .append(csvValue(row.getString("macAddress"))).append(',')
                        .append(csvValue(row.getString("deviceType"))).append(',')
                        .append(csvValue(row.getString("hostName"))).append(',')
                        .append(csvValue(row.getString("dnsStatus"))).append(',')
                        .append(csvValue(row.getString("authenticity"))).append(',')
                        .append(csvValue(row.getString("lastSeen"))).append('\n');
            }
            return csv.toString().getBytes(StandardCharsets.UTF_8);
        });
    }

    public Future<byte[]> generateAlertPdfReport() {
        Promise<byte[]> promise = Promise.promise();
        String sql = "SELECT id, message, alert_type, subnet_address, created_date FROM alert_stream ORDER BY id DESC LIMIT 100";
        db.query(sql).execute().onComplete(ar -> {
            if (ar.failed()) {
                promise.fail(ar.cause());
                return;
            }
            JsonArray data = new JsonArray();
            for (Row row : ar.result()) {
                data.add(new JsonObject()
                        .put("message", row.getString("message"))
                        .put("alertType", row.getString("alert_type"))
                        .put("subnet", row.getString("subnet_address")));
            }
            dispatchDynamicJasper("Alert History Report", data, createAlertReportColumns()).onComplete(promise);
        });
        return promise.future();
    }

    public Future<byte[]> generateEventPdfReport() {
        Promise<byte[]> promise = Promise.promise();
        String sql = "SELECT id, event_type, event_context, created_date FROM event ORDER BY id DESC LIMIT 100";
        db.query(sql).execute().onComplete(ar -> {
            if (ar.failed()) {
                promise.fail(ar.cause());
                return;
            }
            JsonArray data = new JsonArray();
            for (Row row : ar.result()) {
                data.add(new JsonObject()
                        .put("eventType", row.getString("event_type"))
                        .put("eventContext", row.getString("event_context")));
            }
            dispatchDynamicJasper("Event Audit Log Report", data, createEventReportColumns()).onComplete(promise);
        });
        return promise.future();
    }

    public Future<byte[]> generateDhcpPdfReport() {
        Promise<byte[]> promise = Promise.promise();
        String sql = "SELECT id, credential_name, host_address, type, created_by FROM dhcp_server ORDER BY id ASC";
        db.query(sql).execute().onComplete(ar -> {
            if (ar.failed()) {
                promise.fail(ar.cause());
                return;
            }
            JsonArray data = new JsonArray();
            for (Row row : ar.result()) {
                data.add(new JsonObject()
                        .put("credentialName", row.getString("credential_name"))
                        .put("hostAddress", row.getString("host_address"))
                        .put("type", row.getString("type"))
                        .put("createdBy", row.getString("created_by") != null ? row.getString("created_by") : "admin"));
            }
            dispatchDynamicJasper("DHCP Server Statistics Report", data, createDhcpReportColumns()).onComplete(promise);
        });
        return promise.future();
    }

    public Future<byte[]> exportReportToCsv(String title, JsonArray data, JsonArray columns) {
        StringBuilder csv = new StringBuilder();
        for (int i = 0; i < columns.size(); i++) {
            if (i > 0) csv.append(",");
            csv.append(csvValue(columns.getJsonObject(i).getString("title")));
        }
        csv.append("\n");

        for (int i = 0; i < data.size(); i++) {
            JsonObject row = data.getJsonObject(i);
            for (int j = 0; j < columns.size(); j++) {
                if (j > 0) csv.append(",");
                String prop = columns.getJsonObject(j).getString("property");
                csv.append(csvValue(String.valueOf(row.getValue(prop, ""))));
            }
            csv.append("\n");
        }
        return Future.succeededFuture(csv.toString().getBytes(StandardCharsets.UTF_8));
    }

    private Future<byte[]> dispatchDynamicJasper(String title, JsonArray data, JsonArray columns) {
        JsonObject payload = new JsonObject()
                .put("title", title)
                .put("data", data)
                .put("columns", columns);

        return vertx.eventBus().<Buffer>request(ReportWorkerVerticle.ADDR_DYNAMIC_JASPER_PDF, payload)
                .map(msg -> msg.body().getBytes());
    }

    private static String csvValue(String value) {
        if (value == null || "null".equals(value)) return "";
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }

    private String normalizeStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            return "ALL";
        }
        String s = status.trim().toUpperCase();
        if (s.contains("VENDOR")) {
            return "VENDOR SUMMARY";
        }
        if (s.endsWith(" IP")) {
            s = s.substring(0, s.length() - 3).trim();
        }
        return switch (s) {
            case "USED" -> "USED";
            case "AVAILABLE" -> "AVAILABLE";
            case "RESERVED" -> "RESERVED";
            case "TRANSIENT" -> "TRANSIENT";
            case "ROGUE" -> "ROGUE";
            case "TRUSTED" -> "TRUSTED";
            default -> "ALL";
        };
    }

    private JsonArray createSubnetReportColumns() {
        return new JsonArray()
                .add(new JsonObject().put("property", "subnetAddress").put("title", "Subnet Address").put("width", 120))
                .add(new JsonObject().put("property", "subnetMask").put("title", "Subnet Mask").put("width", 100))
                .add(new JsonObject().put("property", "description").put("title", "Description").put("width", 150))
                .add(new JsonObject().put("property", "createdBy").put("title", "Created By").put("width", 90));
    }

    private JsonArray createAlertReportColumns() {
        return new JsonArray()
                .add(new JsonObject().put("property", "message").put("title", "Alert Message").put("width", 180))
                .add(new JsonObject().put("property", "alertType").put("title", "Alert Type").put("width", 90))
                .add(new JsonObject().put("property", "subnet").put("title", "Subnet").put("width", 100));
    }

    private JsonArray createEventReportColumns() {
        return new JsonArray()
                .add(new JsonObject().put("property", "eventType").put("title", "Event Type").put("width", 100))
                .add(new JsonObject().put("property", "eventContext").put("title", "Details / Context").put("width", 200));
    }

    private JsonArray createDhcpReportColumns() {
        return new JsonArray()
                .add(new JsonObject().put("property", "credentialName").put("title", "Credential Name").put("width", 100))
                .add(new JsonObject().put("property", "hostAddress").put("title", "Host Address").put("width", 110))
                .add(new JsonObject().put("property", "type").put("title", "Type").put("width", 80))
                .add(new JsonObject().put("property", "createdBy").put("title", "Created By").put("width", 90));
    }
}
