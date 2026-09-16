package com.motadata.ipam.feature.report;

import com.motadata.ipam.core.verticle.ReportWorkerVerticle;
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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Reactive Business Service for Report Scheduling and Document Generation.
 * Architecture: Event Loop Handler -> Service -> PgPool / EventBus ReportWorkerVerticle.
 * Zero blocking operations on the Event Loop.
 */
public class ReportService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReportService.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final Vertx vertx;
    private final Pool db;

    // Constructs ReportService with Vert.x instance and database pool.
    public ReportService(Vertx vertx, Pool db) {
        this.vertx = vertx;
        this.db = db;
    }

    // Retrieves all scheduled report configurations from the database.
    public Future<JsonArray> getReportSchedulers() {
        String sql = "SELECT id, schedule_name, report_type, schedule_time, schedule_status, recipients FROM report ORDER BY id ASC";
        return db.query(sql).execute().map(rows -> {
            JsonArray result = new JsonArray();
            for (Row row : rows) {
                result.add(new JsonObject()
                        .put("id", row.getLong("id"))
                        .put("scheduleName", row.getString("schedule_name"))
                        .put("reportType", row.getString("report_type"))
                        .put("scheduleTime", row.getString("schedule_time"))
                        .put("scheduleStatus", row.getBoolean("schedule_status"))
                        .put("recipients", row.getString("recipients")));
            }
            return result;
        });
    }

    // Retrieves a specific scheduled report configuration by ID.
    public Future<JsonObject> getReportSchedulerById(Long id) {
        return db.preparedQuery("SELECT id, schedule_name, report_type, schedule_time, schedule_status, recipients " +
                "FROM report WHERE id = $1").execute(Tuple.of(id)).compose(rows -> {
            if (rows.iterator().hasNext()) {
                Row row = rows.iterator().next();
                return Future.succeededFuture(new JsonObject()
                        .put("id", row.getLong("id"))
                        .put("scheduleName", row.getString("schedule_name"))
                        .put("reportType", row.getString("report_type"))
                        .put("scheduleTime", row.getString("schedule_time"))
                        .put("scheduleStatus", row.getBoolean("schedule_status"))
                        .put("recipients", row.getString("recipients")));
            } else {
                return Future.failedFuture("Report schedule " + id + " was not found");
            }
        });
    }

    // Saves or updates a scheduled report definition in the database.
    public Future<JsonObject> saveReportScheduler(JsonObject json) {
        String name = json.getString("scheduleName", "Report Schedule");
        String type = json.getString("reportType", "PDF");
        String time = json.getString("scheduleTime", "09:00");
        Long id = json.getLong("id");
        String sql = id == null
                ? "INSERT INTO report (schedule_name, report_type, schedule_time, schedule_status, recipients) VALUES ($1, $2, $3, true, $4)"
                : "UPDATE report SET schedule_name = $1, report_type = $2, schedule_time = $3, recipients = $4 WHERE id = $5";
        String recipients = json.getString("recipients", "");
        Tuple params = id == null ? Tuple.of(name, type, time, recipients) : Tuple.of(name, type, time, recipients, id);

        return db.preparedQuery(sql).execute(params)
                .map(rows -> new JsonObject().put("success", true).put("message", "Report Schedule Saved Successfully"));
    }

    // Deletes a scheduled report entry by its ID.
    public Future<JsonObject> deleteReportScheduler(Long id) {
        String sql = "DELETE FROM report WHERE id = $1";
        return db.preparedQuery(sql).execute(Tuple.of(id))
                .map(rows -> new JsonObject().put("success", true).put("message", "Report Schedule Deleted"));
    }

    // Retrieves report tree options and subnets with filtering child categories.
    public Future<JsonArray> getSubnetByReport() {
        String sql = "SELECT id, subnet_name, subnet_address FROM subnet_details ORDER BY id ASC";
        return db.query(sql).execute().map(rows -> {
            JsonArray result = new JsonArray();
            for (Row row : rows) {
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
            return result;
        });
    }

    // Retrieves report timeline data for a single subnet ID and status.
    public Future<JsonArray> getSubnetIpByReportTimeline(Long subnetId, String status) {
        return getSubnetIpByReportTimeline(subnetId != null ? List.of(subnetId) : List.of(), status);
    }

    // Retrieves report timeline data for multiple subnet IDs with status filtering.
    public Future<JsonArray> getSubnetIpByReportTimeline(List<Long> subnetIds, String status) {
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

        return db.preparedQuery(sql.toString()).execute(tuple).map(rows -> {
            JsonArray list = new JsonArray();
            for (Row row : rows) {
                String ipStatus = row.getString("status") != null ? row.getString("status").toUpperCase() : "AVAILABLE";
                LocalDateTime dt = row.getLocalDateTime("last_scan_time");
                String timeFormatted = dt != null ? dt.format(DATE_FORMATTER) : LocalDateTime.now().format(DATE_FORMATTER);
                long sid = row.getLong("subnet_id") != null ? row.getLong("subnet_id") : 1L;
                String sName = row.getString("subnet_name") != null ? row.getString("subnet_name") :
                        (row.getString("subnet_address") != null ? row.getString("subnet_address") : "Subnet-" + sid);
                String subnetAddress = row.getString("subnet_address") != null
                        ? row.getString("subnet_address") : sName;
                String deviceType = row.getString("device_type") != null
                        ? row.getString("device_type") : "Unknown";
                String dnsStat = row.getString("dns_status") != null ? row.getString("dns_status") : "Forward & Reverse OK";
                String ipToDnsVal = dnsStat.contains("Forward") ? "Forward OK" : (dnsStat.contains("Reverse") ? "Forward Failed" : "-");
                String dnsToIpVal = dnsStat.contains("Reverse") ? "Reverse OK" : (dnsStat.contains("Forward") ? "Reverse Failed" : "-");
                String authenticity = row.getString("authenticity") != null ? row.getString("authenticity") : "TRUSTED";

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
                        .put("dnsStatus", dnsStat)
                        .put("ipToDns", ipToDnsVal)
                        .put("dnsToIp", dnsToIpVal)
                        .put("authenticity", authenticity)
                        .put("lastAliveTime", timeFormatted)
                        .put("lastScanTime", timeFormatted)
                        .put("lastSeen", timeFormatted));
            }
            return list;
        });
    }

    // Generates a grouped vendor device summary report for the given subnets.
    public Future<JsonArray> getVendorSummaryReport(List<Long> subnetIds) {
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

        return db.preparedQuery(sql.toString()).execute(tuple).map(rows -> {
            JsonArray list = new JsonArray();
            long totalCount = 0;
            List<Row> rowList = new ArrayList<>();
            for (Row r : rows) {
                rowList.add(r);
                totalCount += r.getLong("vendor_count");
            }

            for (Row row : rowList) {
                long count = row.getLong("vendor_count");
                double pct = totalCount > 0 ? Math.round(((double) count / totalCount * 100.0) * 10.0) / 10.0 : 0.0;
                list.add(new JsonObject()
                        .put("VendorName", row.getString("vendor_name"))
                        .put("VendorCount", count)
                        .put("VendorPercentage", pct));
            }
            return list;
        });
    }

    // Generates a subnet IP PDF report for a single subnet.
    public Future<String> generateSubnetIpPdfReport(Long subnetId, String status) {
        return generateSubnetIpPdfReport(subnetId != null ? List.of(subnetId) : List.of(), status);
    }

    // Generates a subnet IP PDF report file via ReportWorkerVerticle and returns the filename.
    public Future<String> generateSubnetIpPdfReport(List<Long> subnetIds, String status) {
        String normalizedStatus = normalizeStatus(status);
        String subLabel = (subnetIds != null && !subnetIds.isEmpty()) ? String.join("_", subnetIds.stream().map(Object::toString).toList()) : "All";

        if ("VENDOR SUMMARY".equals(normalizedStatus)) {
            return getVendorSummaryReport(subnetIds).compose(data -> {
                JsonObject payload = new JsonObject()
                        .put("data", data)
                        .put("subLabel", subLabel);
                return vertx.eventBus().<JsonObject>request(ReportWorkerVerticle.ADDR_GENERATE_VENDOR_PDF, payload)
                        .map(reply -> reply.body().getString("filename"));
            });
        }

        return getSubnetIpByReportTimeline(subnetIds, status).compose(data -> {
            JsonObject payload = new JsonObject()
                    .put("data", data)
                    .put("subLabel", subLabel);
            return vertx.eventBus().<JsonObject>request(ReportWorkerVerticle.ADDR_GENERATE_SUBNET_PDF, payload)
                    .map(reply -> reply.body().getString("filename"));
        });
    }

    // Generates a PDF report summarizing all subnets using dynamic Jasper reports.
    public Future<byte[]> generateSubnetPdfReport() {
        String sql = "SELECT id, subnet_name, subnet_address, subnet_mask, description, created_by FROM subnet_details ORDER BY id ASC";
        return db.query(sql).execute().compose(rows -> {
            JsonArray data = new JsonArray();
            for (Row row : rows) {
                data.add(new JsonObject()
                        .put("subnetAddress", row.getString("subnet_address"))
                        .put("subnetMask", row.getString("subnet_mask"))
                        .put("description", row.getString("description"))
                        .put("createdBy", row.getString("created_by") != null ? row.getString("created_by") : "admin"));
            }
            return dispatchDynamicJasper("Subnet Utilization Report", data, createSubnetReportColumns());
        });
    }

    // Generates a CSV report for a single subnet.
    public Future<byte[]> generateSubnetCsvReport(Long subnetId, String status) {
        return generateSubnetCsvReport(subnetId != null ? List.of(subnetId) : List.of(), status);
    }

    // Generates a CSV report for multiple subnets with status filtering.
    public Future<byte[]> generateSubnetCsvReport(List<Long> subnetIds, String status) {
        String normalizedStatus = normalizeStatus(status);
        String subLabel = (subnetIds != null && !subnetIds.isEmpty()) ? String.join("_", subnetIds.stream().map(Object::toString).toList()) : "All";

        if ("VENDOR SUMMARY".equals(normalizedStatus)) {
            return getVendorSummaryReport(subnetIds).compose(data -> {
                JsonArray columns = new JsonArray()
                        .add(new JsonObject().put("property", "VendorName").put("title", "Vendor Name"))
                        .add(new JsonObject().put("property", "VendorCount").put("title", "Vendor Count"))
                        .add(new JsonObject().put("property", "VendorPercentage").put("title", "Percentage (%)"));
                return exportReportToCsv("Vendor Summary CSV Report", subLabel, data, columns);
            });
        }

        return getSubnetIpByReportTimeline(subnetIds, status).compose(data -> {
            JsonArray columns = new JsonArray()
                    .add(new JsonObject().put("property", "id").put("title", "ID"))
                    .add(new JsonObject().put("property", "ipAddress").put("title", "IP Address"))
                    .add(new JsonObject().put("property", "subnetName").put("title", "Scope"))
                    .add(new JsonObject().put("property", "status").put("title", "Status"))
                    .add(new JsonObject().put("property", "macAddress").put("title", "MAC Address"))
                    .add(new JsonObject().put("property", "deviceType").put("title", "Vendor"))
                    .add(new JsonObject().put("property", "hostName").put("title", "Host Name"))
                    .add(new JsonObject().put("property", "dnsStatus").put("title", "DNS Status"))
                    .add(new JsonObject().put("property", "authenticity").put("title", "Authenticity"))
                    .add(new JsonObject().put("property", "lastSeen").put("title", "Last Seen"));
            return exportReportToCsv("Subnet IP Timeline CSV Report", subLabel, data, columns);
        });
    }

    // Generates an alert history PDF report using dynamic Jasper layout.
    public Future<byte[]> generateAlertPdfReport() {
        String sql = "SELECT a.id, a.message, a.alert_type, s.subnet_address FROM alert_stream a LEFT JOIN subnet_details s ON s.id = a.subnet_id ORDER BY a.id DESC LIMIT 100";
        return db.query(sql).execute().compose(rows -> {
            JsonArray data = new JsonArray();
            for (Row row : rows) {
                data.add(new JsonObject()
                        .put("message", row.getString("message"))
                        .put("alertType", row.getString("alert_type"))
                        .put("subnet", row.getString("subnet_address") != null ? row.getString("subnet_address") : "General"));
            }
            return dispatchDynamicJasper("Alert History Report", data, createAlertReportColumns());
        });
    }

    // Generates an audit event log PDF report using dynamic Jasper layout.
    public Future<byte[]> generateEventPdfReport() {
        String sql = "SELECT id, event_type, event_context, timestamp FROM event ORDER BY id DESC LIMIT 100";
        return db.query(sql).execute().compose(rows -> {
            JsonArray data = new JsonArray();
            for (Row row : rows) {
                data.add(new JsonObject()
                        .put("eventType", row.getString("event_type"))
                        .put("eventContext", row.getString("event_context")));
            }
            return dispatchDynamicJasper("Event Audit Log Report", data, createEventReportColumns());
        });
    }

    // Generates a DHCP server statistics PDF report using dynamic Jasper layout.
    public Future<byte[]> generateDhcpPdfReport() {
        String sql = "SELECT id, credential_name, host_address, type, user_name FROM dhcp_credential_details ORDER BY id ASC";
        return db.query(sql).execute().compose(rows -> {
            JsonArray data = new JsonArray();
            for (Row row : rows) {
                data.add(new JsonObject()
                        .put("credentialName", row.getString("credential_name"))
                        .put("hostAddress", row.getString("host_address"))
                        .put("type", row.getString("type"))
                        .put("createdBy", row.getString("user_name") != null ? row.getString("user_name") : "admin"));
            }
            return dispatchDynamicJasper("DHCP Server Statistics Report", data, createDhcpReportColumns());
        });
    }

    // Serializes arbitrary tabular dataset and columns definition to CSV bytes via ReportWorkerVerticle.
    public Future<byte[]> exportReportToCsv(String title, JsonArray data, JsonArray columns) {
        return exportReportToCsv(title, "All", data, columns);
    }

    // Serializes arbitrary tabular dataset and columns definition to CSV bytes via ReportWorkerVerticle with subLabel.
    public Future<byte[]> exportReportToCsv(String title, String subLabel, JsonArray data, JsonArray columns) {
        JsonObject payload = new JsonObject()
                .put("title", title)
                .put("subLabel", subLabel != null ? subLabel : "All")
                .put("data", data)
                .put("columns", columns);

        LOGGER.info("Dispatching CSV report generation to ReportWorkerVerticle: '{}' for subLabel={} (records={})", title, subLabel, data.size());

        return vertx.eventBus().<Buffer>request(ReportWorkerVerticle.ADDR_GENERATE_CSV, payload)
                .map(msg -> msg.body().getBytes());
    }

    // Dispatches dynamic Jasper PDF compilation request to ReportWorkerVerticle.
    private Future<byte[]> dispatchDynamicJasper(String title, JsonArray data, JsonArray columns) {
        JsonObject payload = new JsonObject()
                .put("title", title)
                .put("data", data)
                .put("columns", columns);

        LOGGER.info("Dispatching DynamicJasper PDF report generation to ReportWorkerVerticle: '{}' (records={})", title, data.size());

        return vertx.eventBus().<Buffer>request(ReportWorkerVerticle.ADDR_DYNAMIC_JASPER_PDF, payload)
                .map(msg -> msg.body().getBytes());
    }

    // Normalizes status query parameter into a standard IPAM status string.
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

    // Defines column metadata for subnet reports.
    private JsonArray createSubnetReportColumns() {
        return new JsonArray()
                .add(new JsonObject().put("property", "subnetAddress").put("title", "Subnet Address").put("width", 120))
                .add(new JsonObject().put("property", "subnetMask").put("title", "Subnet Mask").put("width", 100))
                .add(new JsonObject().put("property", "description").put("title", "Description").put("width", 150))
                .add(new JsonObject().put("property", "createdBy").put("title", "Created By").put("width", 90));
    }

    // Defines column metadata for alert history reports.
    private JsonArray createAlertReportColumns() {
        return new JsonArray()
                .add(new JsonObject().put("property", "message").put("title", "Alert Message").put("width", 180))
                .add(new JsonObject().put("property", "alertType").put("title", "Alert Type").put("width", 90))
                .add(new JsonObject().put("property", "subnet").put("title", "Subnet").put("width", 100));
    }

    // Defines column metadata for event audit reports.
    private JsonArray createEventReportColumns() {
        return new JsonArray()
                .add(new JsonObject().put("property", "eventType").put("title", "Event Type").put("width", 100))
                .add(new JsonObject().put("property", "eventContext").put("title", "Details / Context").put("width", 200));
    }

    // Defines column metadata for DHCP server reports.
    private JsonArray createDhcpReportColumns() {
        return new JsonArray()
                .add(new JsonObject().put("property", "credentialName").put("title", "Credential Name").put("width", 100))
                .add(new JsonObject().put("property", "hostAddress").put("title", "Host Address").put("width", 110))
                .add(new JsonObject().put("property", "type").put("title", "Type").put("width", 80))
                .add(new JsonObject().put("property", "createdBy").put("title", "Created By").put("width", 90));
    }
}
