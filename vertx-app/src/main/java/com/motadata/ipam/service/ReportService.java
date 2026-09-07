package com.motadata.ipam.service;

import ar.com.fdvs.dj.core.DynamicJasperHelper;
import ar.com.fdvs.dj.core.layout.ClassicLayoutManager;
import ar.com.fdvs.dj.domain.DynamicReport;
import ar.com.fdvs.dj.domain.Style;
import ar.com.fdvs.dj.domain.builders.ColumnBuilder;
import ar.com.fdvs.dj.domain.builders.FastReportBuilder;
import ar.com.fdvs.dj.domain.constants.Font;
import com.motadata.ipam.model.AlertStream;
import com.motadata.ipam.model.Event;
import com.motadata.ipam.model.SubnetDetails;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.Tuple;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Asynchronous Vert.x Business Service for Report Scheduling and PDF/CSV Generation.
 * Direct Architecture: Handler -> Service -> PgPool -> PostgreSQL
 */
public class ReportService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReportService.class);
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    static {
        System.setProperty("net.sf.jasperreports.awt.ignore.missing.font", "true");
    }

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
            if (ar.succeeded()) promise.complete(result); else promise.fail(ar.cause());
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
                            .put("systemName", deviceType)
                            .put("dnsStatus", row.getString("dns_status") != null ? row.getString("dns_status") : "Forward & Reverse OK")
                            .put("dnsForwardName", "")
                            .put("ipToDns", "Forward OK")
                            .put("dnsToIp", "Reverse OK")
                            .put("authenticity", auth)
                            .put("lastSeen", DATE_FORMAT.format(dt))
                            .put("lastAliveTime", DATE_FORMAT.format(dt)));
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
                "COUNT(*)::bigint AS vendor_count " +
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
        sql.append("GROUP BY COALESCE(NULLIF(TRIM(ip.device_type), ''), NULLIF(TRIM(ip.vendor), ''), 'Unknown') ORDER BY vendor_count DESC, vendor_name ASC");

        db.preparedQuery(sql.toString()).execute(tuple).onComplete(ar -> {
            if (ar.succeeded()) {
                JsonArray list = new JsonArray();
                long total = 0;
                for (Row row : ar.result()) {
                    total += row.getLong("vendor_count");
                }
                for (Row row : ar.result()) {
                    long count = row.getLong("vendor_count");
                    double pct = total > 0 ? Math.round(((double) count / total * 100.0) * 100.0) / 100.0 : 0.0;
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

        if ("VENDOR SUMMARY".equals(normalizedStatus)) {
            getVendorSummaryReport(subnetIds).onComplete(ar -> {
                if (ar.failed()) {
                    promise.fail(ar.cause());
                    return;
                }
                JsonArray data = ar.result();
                vertx.<String>executeBlocking(() -> {
                    String subLabel = (subnetIds != null && !subnetIds.isEmpty()) ? String.join("_", subnetIds.stream().map(Object::toString).toList()) : "All";
                    String filename = "Vendor_Summary_Export_" + subLabel + "_" + System.currentTimeMillis() + ".pdf";
                    String exportDir = "file-uploads/exports/";
                    java.nio.file.Files.createDirectories(java.nio.file.Paths.get(exportDir));
                    String filePath = exportDir + filename;
                    byte[] pdfBytes = generateVendorSummaryPdf(data, subLabel);
                    java.nio.file.Files.write(java.nio.file.Paths.get(filePath), pdfBytes);
                    return filename;
                }).onComplete(promise);
            });
            return promise.future();
        }

        getSubnetIpByReportTimeline(subnetIds, status).onComplete(ar -> {
            if (ar.failed()) {
                promise.fail(ar.cause());
                return;
            }
            JsonArray data = ar.result();
            List<JsonObject> list = new ArrayList<>();
            for (int i = 0; i < data.size(); i++) {
                list.add(data.getJsonObject(i));
            }

            vertx.<String>executeBlocking(() -> {
                String subLabel = (subnetIds != null && !subnetIds.isEmpty()) ? String.join("_", subnetIds.stream().map(Object::toString).toList()) : "All";
                String filename = "SubnetIP_Export_" + subLabel + "_" + System.currentTimeMillis() + ".pdf";
                String exportDir = "file-uploads/exports/";
                java.nio.file.Files.createDirectories(java.nio.file.Paths.get(exportDir));
                String filePath = exportDir + filename;
                byte[] pdfBytes = generateSimplePdf(list, subLabel);
                java.nio.file.Files.write(java.nio.file.Paths.get(filePath), pdfBytes);
                return filename;
            }).onComplete(promise);
        });
        return promise.future();
    }

    public Future<byte[]> generateSubnetPdfReport() {
        Promise<byte[]> promise = Promise.promise();

        String sql = "SELECT id, subnet_name, subnet_address, subnet_mask, description, created_by FROM subnet_details ORDER BY id ASC";
        db.query(sql).execute().onComplete(ar -> {
            List<SubnetDetails> subnets = new ArrayList<>();
            if (ar.succeeded()) {
                for (Row row : ar.result()) {
                    SubnetDetails s = new SubnetDetails();
                    s.setId(row.getLong("id"));
                    s.setSubnetName(row.getString("subnet_name"));
                    s.setSubnetAddress(row.getString("subnet_address"));
                    s.setSubnetMask(row.getString("subnet_mask"));
                    s.setDescription(row.getString("description"));
                    s.setCreatedBy(row.getString("created_by") != null ? row.getString("created_by") : "admin");
                    subnets.add(s);
                }

            }
            if (ar.failed()) {
                promise.fail(ar.cause());
            } else {
                executeBlockingReportGeneration("Subnet Utilization Report", subnets, createSubnetReportColumns()).onComplete(promise);
            }
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

    private byte[] generateSimplePdf(List<JsonObject> ipList, String subnetLabel) throws Exception {
        StringBuilder pdf = new StringBuilder();
        List<String> objects = new ArrayList<>();

        StringBuilder content = new StringBuilder();
        content.append("BT\n");
        content.append("/F1 12 Tf\n");
        content.append("50 750 Td\n");
        content.append("(Subnet IP Address Report - Subnets: ").append(sanitize(subnetLabel)).append(") Tj\n");
        content.append("0 -20 Td\n");
        content.append("/F1 9 Tf\n");
        content.append("(Generated: ").append(DATE_FORMAT.format(new Date())).append(") Tj\n");
        content.append("0 -25 Td\n");
        content.append("/F1 10 Tf\n");
        content.append("(IP Address            Status     Scope              MAC Address        Host Name) Tj\n");
        content.append("0 -18 Td\n");
        content.append("(--------------------------------------------------------------------------------) Tj\n");
        content.append("0 -5 Td\n");

        int lineCount = 0;
        for (JsonObject ip : ipList) {
            if (lineCount++ >= 45) break;
            String ipAddr = pad(ip.getString("ipAddress", "-"), 22);
            String st = pad(ip.getString("status", "-"), 11);
            String sc = pad(ip.getString("subnetName", "-"), 19);
            String mac = pad(ip.getString("macAddress", "-"), 19);
            String host = pad(ip.getString("hostName", "-"), 15);

            content.append("0 -14 Td\n");
            content.append("/F1 9 Tf\n");
            content.append("(").append(sanitize(ipAddr + st + sc + mac + host)).append(") Tj\n");
        }
        content.append("ET\n");

        byte[] streamBytes = content.toString().getBytes(StandardCharsets.ISO_8859_1);

        objects.add("1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n");
        objects.add("2 0 obj\n<< /Type /Pages /Kids [3 0 R] /Count 1 >>\nendobj\n");
        objects.add("3 0 obj\n<< /Type /Page /Parent 2 0 R /MediaBox [0 0 612 792] /Contents 4 0 R /Resources << /Font << /F1 5 0 R >> >> >>\nendobj\n");
        objects.add("4 0 obj\n<< /Length " + streamBytes.length + " >>\nstream\n" + content + "\nendstream\nendobj\n");
        objects.add("5 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>\nendobj\n");

        pdf.append("%PDF-1.4\n");
        List<Integer> offsets = new ArrayList<>();
        int currentOffset = pdf.length();

        for (String obj : objects) {
            offsets.add(currentOffset);
            pdf.append(obj);
            currentOffset = pdf.length();
        }

        int xrefOffset = pdf.length();
        pdf.append("xref\n0 ").append(objects.size() + 1).append("\n");
        pdf.append("0000000000 65535 f \n");
        for (int offset : offsets) {
            pdf.append(String.format("%010d 00000 n \n", offset));
        }

        pdf.append("trailer\n<< /Size ").append(objects.size() + 1).append(" /Root 1 0 R >>\n");
        pdf.append("startxref\n").append(xrefOffset).append("\n%%EOF\n");

        return pdf.toString().getBytes(StandardCharsets.ISO_8859_1);
    }

    private byte[] generateVendorSummaryPdf(JsonArray data, String subnetLabel) throws Exception {
        StringBuilder content = new StringBuilder();
        content.append("BT\n");
        content.append("/F1 12 Tf\n");
        content.append("50 750 Td\n");
        content.append("(Vendor Summary Report - Subnets: ").append(sanitize(subnetLabel)).append(") Tj\n");
        content.append("0 -20 Td\n");
        content.append("/F1 9 Tf\n");
        content.append("(Generated: ").append(DATE_FORMAT.format(new Date())).append(") Tj\n");
        content.append("0 -25 Td\n");
        content.append("/F1 10 Tf\n");
        content.append("(Vendor Name                         Count          Percentage) Tj\n");
        content.append("0 -18 Td\n");
        content.append("(----------------------------------------------------------------) Tj\n");
        content.append("0 -5 Td\n");

        for (int i = 0; i < data.size(); i++) {
            JsonObject row = data.getJsonObject(i);
            String vName = pad(row.getString("VendorName", "Unknown"), 36);
            String vCount = pad(String.valueOf(row.getValue("VendorCount", "0")), 15);
            String vPct = String.valueOf(row.getValue("VendorPercentage", "0")) + " %";

            content.append("0 -15 Td\n");
            content.append("/F1 9 Tf\n");
            content.append("(").append(sanitize(vName + vCount + vPct)).append(") Tj\n");
        }
        content.append("ET\n");

        byte[] streamBytes = content.toString().getBytes(StandardCharsets.ISO_8859_1);
        List<String> objects = new ArrayList<>();
        objects.add("1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n");
        objects.add("2 0 obj\n<< /Type /Pages /Kids [3 0 R] /Count 1 >>\nendobj\n");
        objects.add("3 0 obj\n<< /Type /Page /Parent 2 0 R /MediaBox [0 0 612 792] /Contents 4 0 R /Resources << /Font << /F1 5 0 R >> >> >>\nendobj\n");
        objects.add("4 0 obj\n<< /Length " + streamBytes.length + " >>\nstream\n" + content + "\nendstream\nendobj\n");
        objects.add("5 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>\nendobj\n");

        StringBuilder pdf = new StringBuilder();
        pdf.append("%PDF-1.4\n");
        List<Integer> offsets = new ArrayList<>();
        int currentOffset = pdf.length();

        for (String obj : objects) {
            offsets.add(currentOffset);
            pdf.append(obj);
            currentOffset = pdf.length();
        }

        int xrefOffset = pdf.length();
        pdf.append("xref\n0 ").append(objects.size() + 1).append("\n");
        pdf.append("0000000000 65535 f \n");
        for (int offset : offsets) {
            pdf.append(String.format("%010d 00000 n \n", offset));
        }

        pdf.append("trailer\n<< /Size ").append(objects.size() + 1).append(" /Root 1 0 R >>\n");
        pdf.append("startxref\n").append(xrefOffset).append("\n%%EOF\n");

        return pdf.toString().getBytes(StandardCharsets.ISO_8859_1);
    }

    private static String pad(String s, int len) {
        if (s == null) s = "-";
        if (s.length() >= len) return s.substring(0, len);
        return String.format("%-" + len + "s", s);
    }

    private static String sanitize(String s) {
        return s.replace("\\", "\\\\").replace("(", "\\(").replace(")", "\\)");
    }

    public Future<byte[]> generateAlertPdfReport() {
        Promise<byte[]> promise = Promise.promise();

        String sql = "SELECT id, alert_type, message, subnet, timestamp, status FROM alert_stream ORDER BY id DESC LIMIT 100";
        db.query(sql).execute().onComplete(ar -> {
            List<AlertStream> alerts = new ArrayList<>();
            if (ar.succeeded()) {
                for (Row row : ar.result()) {
                    AlertStream a = new AlertStream();
                    a.setId(row.getLong("id"));
                    a.setAlertType(row.getString("alert_type"));
                    a.setMessage(row.getString("message"));
                    a.setSubnet(row.getString("subnet"));
                    a.setStatus(row.getBoolean("status"));
                    alerts.add(a);
                }
            }
            if (ar.failed()) promise.fail(ar.cause());
            else executeBlockingReportGeneration("Alert History Report", alerts, createAlertReportColumns()).onComplete(promise);
        });

        return promise.future();
    }

    public Future<byte[]> generateEventPdfReport() {
        Promise<byte[]> promise = Promise.promise();

        String sql = "SELECT id, event_type, event_context, message, user_name, timestamp FROM event ORDER BY id DESC LIMIT 100";
        db.query(sql).execute().onComplete(ar -> {
            List<Event> events = new ArrayList<>();
            if (ar.succeeded()) {
                for (Row row : ar.result()) {
                    Event e = new Event();
                    e.setId(row.getLong("id"));
                    e.setEventType(row.getString("event_type"));
                    e.setEventContext(row.getString("event_context"));
                    e.setMessage(row.getString("message"));
                    e.setUser(row.getString("user_name"));
                    events.add(e);
                }
            }
            if (ar.failed()) promise.fail(ar.cause());
            else executeBlockingReportGeneration("Event Audit Log Report", events, createEventReportColumns()).onComplete(promise);
        });

        return promise.future();
    }

    public Future<byte[]> generateDhcpPdfReport() {
        Promise<byte[]> promise = Promise.promise();

        String sql = "SELECT id, credential_name, server_ip, host_address, type, user_name FROM dhcp_credential_details ORDER BY id ASC";
        db.query(sql).execute().onComplete(ar -> {
            List<DhcpReportItem> dhcpServers = new ArrayList<>();
            if (ar.succeeded()) {
                for (Row row : ar.result()) {
                    dhcpServers.add(new DhcpReportItem(
                            row.getString("credential_name"),
                            row.getString("server_ip") != null ? row.getString("server_ip") : row.getString("host_address"),
                            row.getString("type"),
                            row.getString("user_name") != null ? row.getString("user_name") : "admin"
                    ));
                }
            }
            if (ar.failed()) promise.fail(ar.cause());
            else executeBlockingReportGeneration("DHCP Server Statistics Report", dhcpServers, createDhcpReportColumns()).onComplete(promise);
        });

        return promise.future();
    }

    public static class DhcpReportItem {
        private String credentialName;
        private String hostAddress;
        private String type;
        private String createdBy;

        public DhcpReportItem(String credentialName, String hostAddress, String type, String createdBy) {
            this.credentialName = credentialName;
            this.hostAddress = hostAddress;
            this.type = type;
            this.createdBy = createdBy;
        }

        public String getCredentialName() { return credentialName; }
        public String getHostAddress() { return hostAddress; }
        public String getType() { return type; }
        public String getCreatedBy() { return createdBy; }
    }

    private <T> Future<byte[]> executeBlockingReportGeneration(String title, List<T> data, List<ReportColumnDef> columns) {
        return vertx.executeBlocking(() -> {
            try {
                FastReportBuilder frb = new FastReportBuilder();
                frb.setTitle(title);
                frb.setSubtitle("Generated by Motadata IPAM System");
                frb.setUseFullPageWidth(true);

                Style defaultStyle = new Style();
                defaultStyle.setFont(new Font(9, "SansSerif", false));
                frb.setDefaultStyles(defaultStyle, defaultStyle, defaultStyle, defaultStyle);

                for (ReportColumnDef col : columns) {
                    frb.addColumn(ColumnBuilder.getNew()
                            .setColumnProperty(col.property, String.class.getName())
                            .setTitle(col.title)
                            .setWidth(col.width)
                            .setStyle(defaultStyle)
                            .build());
                }

                DynamicReport dynamicReport = frb.build();
                JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(data != null ? data : new ArrayList<>());

                JasperPrint jasperPrint = DynamicJasperHelper.generateJasperPrint(dynamicReport, new ClassicLayoutManager(), dataSource);

                ByteArrayOutputStream pdfOutputStream = new ByteArrayOutputStream();
                net.sf.jasperreports.engine.JasperExportManager.exportReportToPdfStream(jasperPrint, pdfOutputStream);

                return pdfOutputStream.toByteArray();
            } catch (Exception e) {
                LOGGER.error("Error building DynamicJasper PDF report '{}': {}", title, e.getMessage(), e);
                throw e;
            }
        });
    }

    private List<ReportColumnDef> createSubnetReportColumns() {
        List<ReportColumnDef> cols = new ArrayList<>();
        cols.add(new ReportColumnDef("subnetAddress", "Subnet Address", 120));
        cols.add(new ReportColumnDef("subnetMask", "Subnet Mask", 100));
        cols.add(new ReportColumnDef("description", "Description", 150));
        cols.add(new ReportColumnDef("createdBy", "Created By", 90));
        return cols;
    }

    private List<ReportColumnDef> createAlertReportColumns() {
        List<ReportColumnDef> cols = new ArrayList<>();
        cols.add(new ReportColumnDef("message", "Alert Message", 180));
        cols.add(new ReportColumnDef("alertType", "Alert Type", 90));
        cols.add(new ReportColumnDef("subnet", "Subnet", 100));
        return cols;
    }

    private List<ReportColumnDef> createEventReportColumns() {
        List<ReportColumnDef> cols = new ArrayList<>();
        cols.add(new ReportColumnDef("eventType", "Event Type", 100));
        cols.add(new ReportColumnDef("eventContext", "Details / Context", 200));
        return cols;
    }

    private List<ReportColumnDef> createDhcpReportColumns() {
        List<ReportColumnDef> cols = new ArrayList<>();
        cols.add(new ReportColumnDef("credentialName", "Credential Name", 100));
        cols.add(new ReportColumnDef("hostAddress", "Host Address", 110));
        cols.add(new ReportColumnDef("type", "Type", 80));
        cols.add(new ReportColumnDef("createdBy", "Created By", 90));
        return cols;
    }

    private static class ReportColumnDef {
        final String property;
        final String title;
        final Integer width;

        ReportColumnDef(String property, String title, Integer width) {
            this.property = property;
            this.title = title;
            this.width = width;
        }
    }
}
