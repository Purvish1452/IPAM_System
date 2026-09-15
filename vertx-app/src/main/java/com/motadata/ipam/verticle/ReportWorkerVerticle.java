package com.motadata.ipam.verticle;

import ar.com.fdvs.dj.core.DynamicJasperHelper;
import ar.com.fdvs.dj.core.layout.ClassicLayoutManager;
import ar.com.fdvs.dj.domain.DynamicReport;
import ar.com.fdvs.dj.domain.Style;
import ar.com.fdvs.dj.domain.builders.ColumnBuilder;
import ar.com.fdvs.dj.domain.builders.FastReportBuilder;
import ar.com.fdvs.dj.domain.constants.Font;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.Pool;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Dedicated Worker Verticle for Document Generation and Heavy Reporting.
 * Runs on the Worker Thread Pool (report-worker-pool).
 * Completely isolates CPU/heap-intensive PDF and spreadsheet rendering from the HTTP Event Loop.
 */
public class ReportWorkerVerticle extends AbstractVerticle {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReportWorkerVerticle.class);
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final String EXPORT_DIR = "file-uploads/exports/";

    public static final String ADDR_GENERATE_SUBNET_PDF = "ipam.worker.report.subnet.pdf";
    public static final String ADDR_GENERATE_SUBNET_CSV = "ipam.worker.report.subnet.csv";
    public static final String ADDR_GENERATE_VENDOR_PDF = "ipam.worker.report.vendor.pdf";
    public static final String ADDR_DYNAMIC_JASPER_PDF = "ipam.worker.report.dynamic.pdf";
    public static final String ADDR_GENERATE_CSV = "ipam.worker.report.generic.csv";

    static {
        silenceThirdPartyLoggers();
    }

    /**
     * Programmatically silences verbose JUL (java.util.logging) loggers from DynamicJasper,
     * JasperReports, and Commons-Logging to ensure clean terminal and log output.
     */
    public static void silenceThirdPartyLoggers() {
        try {
            System.setProperty("net.sf.jasperreports.awt.ignore.missing.font", "true");
            String[] silencedLoggers = {
                    "",
                    "ar.com.fdvs.dj",
                    "ar.com.fdvs.dj.core",
                    "ar.com.fdvs.dj.core.DynamicJasperHelper",
                    "ar.com.fdvs.dj.core.DJJRDesignHelper",
                    "ar.com.fdvs.dj.core.layout.ClassicLayoutManager",
                    "net.sf.jasperreports",
                    "net.sf.jasperreports.engine",
                    "org.apache.commons.logging"
            };
            for (String loggerName : silencedLoggers) {
                java.util.logging.Logger jul = java.util.logging.Logger.getLogger(loggerName);
                if (jul != null) {
                    jul.setLevel(java.util.logging.Level.WARNING);
                    for (java.util.logging.Handler h : jul.getHandlers()) {
                        h.setLevel(java.util.logging.Level.WARNING);
                    }
                }
            }
        } catch (Exception ignored) {}
    }

    private final Pool db;

    // Constructs ReportWorkerVerticle and ensures export directories exist.
    public ReportWorkerVerticle(Pool db) {
        this.db = db;
        silenceThirdPartyLoggers();
        try {
            Files.createDirectories(Paths.get(EXPORT_DIR));
        } catch (Exception e) {
            LOGGER.warn("Could not create exports directory: {}", e.getMessage());
        }
    }

    // Registers EventBus consumers for subnet PDF/CSV, vendor summary PDF, and DynamicJasper PDF rendering.
    @Override
    public void start(Promise<Void> startPromise) {
        silenceThirdPartyLoggers();
        LOGGER.info("Starting ReportWorkerVerticle on Worker Thread Pool: {}", Thread.currentThread().getName());

        // 1. Subnet IP PDF Generation (File-backed)
        vertx.eventBus().<JsonObject>consumer(ADDR_GENERATE_SUBNET_PDF, message -> {
            long startTime = System.currentTimeMillis();
            try {
                JsonObject body = message.body();
                JsonArray data = body.getJsonArray("data", new JsonArray());
                String subLabel = body.getString("subLabel", "All");

                LOGGER.info("ReportWorkerVerticle [{}] generating Subnet IP PDF report for subLabel={}, records={}",
                        Thread.currentThread().getName(), subLabel, data.size());

                List<JsonObject> list = new ArrayList<>();
                for (int i = 0; i < data.size(); i++) {
                    list.add(data.getJsonObject(i));
                }

                String filename = "SubnetIP_Export_" + subLabel + "_" + System.currentTimeMillis() + ".pdf";
                String filePath = EXPORT_DIR + filename;
                byte[] pdfBytes = generateSimplePdf(list, subLabel);
                Files.write(Paths.get(filePath), pdfBytes);

                long duration = System.currentTimeMillis() - startTime;
                LOGGER.info("ReportWorkerVerticle [{}] successfully generated Subnet IP PDF report '{}' ({} bytes, {} records) in {} ms",
                        Thread.currentThread().getName(), filename, pdfBytes.length, list.size(), duration);

                message.reply(new JsonObject()
                        .put("success", true)
                        .put("filename", filename)
                        .put("filePath", filePath)
                        .put("size", pdfBytes.length));
            } catch (Exception e) {
                LOGGER.error("ReportWorkerVerticle [{}] error generating Subnet IP PDF report: {}",
                        Thread.currentThread().getName(), e.getMessage(), e);
                message.fail(500, e.getMessage());
            }
        });

        // 2. Subnet IP CSV Generation (File-backed)
        vertx.eventBus().<JsonObject>consumer(ADDR_GENERATE_SUBNET_CSV, message -> {
            long startTime = System.currentTimeMillis();
            try {
                JsonObject body = message.body();
                JsonArray data = body.getJsonArray("data", new JsonArray());
                String subLabel = body.getString("subLabel", "All");

                LOGGER.info("ReportWorkerVerticle [{}] generating Subnet IP CSV report for subLabel={}, records={}",
                        Thread.currentThread().getName(), subLabel, data.size());

                StringBuilder sb = new StringBuilder();
                sb.append("IP Address,MAC Address,Host Name,Status,Device Type,DNS Status,Last Alive Time,Location,Description\n");

                for (int i = 0; i < data.size(); i++) {
                    JsonObject row = data.getJsonObject(i);
                    sb.append(csvEscape(row.getString("ipAddress"))).append(",")
                            .append(csvEscape(row.getString("macAddress"))).append(",")
                            .append(csvEscape(row.getString("hostName"))).append(",")
                            .append(csvEscape(row.getString("status"))).append(",")
                            .append(csvEscape(row.getString("deviceType"))).append(",")
                            .append(csvEscape(row.getString("dnsStatus"))).append(",")
                            .append(csvEscape(row.getString("lastAliveTime", "N/A"))).append(",")
                            .append(csvEscape(row.getString("location"))).append(",")
                            .append(csvEscape(row.getString("description"))).append("\n");
                }

                String filename = "SubnetIP_Export_" + subLabel + "_" + System.currentTimeMillis() + ".csv";
                String filePath = EXPORT_DIR + filename;
                byte[] csvBytes = sb.toString().getBytes(StandardCharsets.UTF_8);
                Files.write(Paths.get(filePath), csvBytes);

                long duration = System.currentTimeMillis() - startTime;
                LOGGER.info("ReportWorkerVerticle [{}] successfully generated Subnet IP CSV report '{}' ({} bytes, {} records) in {} ms",
                        Thread.currentThread().getName(), filename, csvBytes.length, data.size(), duration);

                message.reply(new JsonObject()
                        .put("success", true)
                        .put("filename", filename)
                        .put("filePath", filePath)
                        .put("size", csvBytes.length));
            } catch (Exception e) {
                LOGGER.error("ReportWorkerVerticle [{}] error generating Subnet IP CSV report: {}",
                        Thread.currentThread().getName(), e.getMessage(), e);
                message.fail(500, e.getMessage());
            }
        });

        // 3. Vendor Summary PDF Generation (File-backed)
        vertx.eventBus().<JsonObject>consumer(ADDR_GENERATE_VENDOR_PDF, message -> {
            long startTime = System.currentTimeMillis();
            try {
                JsonObject body = message.body();
                JsonArray data = body.getJsonArray("data", new JsonArray());
                String subLabel = body.getString("subLabel", "All");

                LOGGER.info("ReportWorkerVerticle [{}] generating Vendor Summary PDF report for subLabel={}, records={}",
                        Thread.currentThread().getName(), subLabel, data.size());

                String filename = "Vendor_Summary_Export_" + subLabel + "_" + System.currentTimeMillis() + ".pdf";
                String filePath = EXPORT_DIR + filename;
                byte[] pdfBytes = generateVendorSummaryPdf(data, subLabel);
                Files.write(Paths.get(filePath), pdfBytes);

                long duration = System.currentTimeMillis() - startTime;
                LOGGER.info("ReportWorkerVerticle [{}] successfully generated Vendor Summary PDF report '{}' ({} bytes, {} records) in {} ms",
                        Thread.currentThread().getName(), filename, pdfBytes.length, data.size(), duration);

                message.reply(new JsonObject()
                        .put("success", true)
                        .put("filename", filename)
                        .put("filePath", filePath)
                        .put("size", pdfBytes.length));
            } catch (Exception e) {
                LOGGER.error("ReportWorkerVerticle [{}] error generating Vendor Summary PDF report: {}",
                        Thread.currentThread().getName(), e.getMessage(), e);
                message.fail(500, e.getMessage());
            }
        });

        // 4. DynamicJasper PDF Generation (In-Memory Buffer)
        vertx.eventBus().<JsonObject>consumer(ADDR_DYNAMIC_JASPER_PDF, message -> {
            long startTime = System.currentTimeMillis();
            try {
                JsonObject body = message.body();
                String title = body.getString("title", "Report");
                JsonArray data = body.getJsonArray("data", new JsonArray());
                JsonArray columns = body.getJsonArray("columns", new JsonArray());

                LOGGER.info("ReportWorkerVerticle [{}] compiling DynamicJasper PDF report '{}' (records={}, columns={})",
                        Thread.currentThread().getName(), title, data.size(), columns.size());

                FastReportBuilder frb = new FastReportBuilder();
                frb.setTitle(title);
                frb.setSubtitle("Generated by Motadata IPAM System");
                frb.setUseFullPageWidth(true);

                Style defaultStyle = new Style();
                defaultStyle.setFont(new Font(9, "SansSerif", false));
                frb.setDefaultStyles(defaultStyle, defaultStyle, defaultStyle, defaultStyle);

                for (int i = 0; i < columns.size(); i++) {
                    JsonObject col = columns.getJsonObject(i);
                    frb.addColumn(ColumnBuilder.getNew()
                            .setColumnProperty(col.getString("property"), String.class.getName())
                            .setTitle(col.getString("title"))
                            .setWidth(col.getInteger("width", 100))
                            .setStyle(defaultStyle)
                            .build());
                }

                DynamicReport dynamicReport = frb.build();
                List<Map<String, Object>> mapList = new ArrayList<>();
                for (int i = 0; i < data.size(); i++) {
                    mapList.add(data.getJsonObject(i).getMap());
                }

                JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(mapList);
                JasperPrint jasperPrint = DynamicJasperHelper.generateJasperPrint(dynamicReport, new ClassicLayoutManager(), dataSource);

                ByteArrayOutputStream pdfOutputStream = new ByteArrayOutputStream();
                net.sf.jasperreports.engine.JasperExportManager.exportReportToPdfStream(jasperPrint, pdfOutputStream);

                byte[] pdfBytes = pdfOutputStream.toByteArray();
                long duration = System.currentTimeMillis() - startTime;
                LOGGER.info("ReportWorkerVerticle [{}] successfully compiled DynamicJasper PDF report '{}' ({} bytes, {} records) in {} ms",
                        Thread.currentThread().getName(), title, pdfBytes.length, data.size(), duration);

                Buffer buffer = Buffer.buffer(pdfBytes);
                message.reply(buffer);
            } catch (Exception e) {
                LOGGER.error("ReportWorkerVerticle [{}] error generating DynamicJasper PDF report: {}",
                        Thread.currentThread().getName(), e.getMessage(), e);
                message.fail(500, e.getMessage());
            }
        });

        // 5. Generic CSV Generation (In-Memory Buffer)
        vertx.eventBus().<JsonObject>consumer(ADDR_GENERATE_CSV, message -> {
            long startTime = System.currentTimeMillis();
            try {
                JsonObject body = message.body();
                String title = body.getString("title", "Report");
                String subLabel = body.getString("subLabel", "All");
                JsonArray data = body.getJsonArray("data", new JsonArray());
                JsonArray columns = body.getJsonArray("columns", new JsonArray());

                LOGGER.info("ReportWorkerVerticle [{}] generating CSV report '{}' for subLabel={}, records={}, columns={}",
                        Thread.currentThread().getName(), title, subLabel, data.size(), columns.size());

                StringBuilder csv = new StringBuilder();
                for (int i = 0; i < columns.size(); i++) {
                    if (i > 0) csv.append(",");
                    csv.append(csvEscape(columns.getJsonObject(i).getString("title")));
                }
                csv.append("\n");

                for (int i = 0; i < data.size(); i++) {
                    JsonObject row = data.getJsonObject(i);
                    for (int j = 0; j < columns.size(); j++) {
                        if (j > 0) csv.append(",");
                        String prop = columns.getJsonObject(j).getString("property");
                        csv.append(csvEscape(String.valueOf(row.getValue(prop, ""))));
                    }
                    csv.append("\n");
                }

                byte[] csvBytes = csv.toString().getBytes(StandardCharsets.UTF_8);
                long duration = System.currentTimeMillis() - startTime;
                LOGGER.info("ReportWorkerVerticle [{}] successfully generated CSV report '{}' for subLabel={} ({} bytes, {} records) in {} ms",
                        Thread.currentThread().getName(), title, subLabel, csvBytes.length, data.size(), duration);

                message.reply(Buffer.buffer(csvBytes));
            } catch (Exception e) {
                LOGGER.error("ReportWorkerVerticle [{}] error generating CSV report: {}",
                        Thread.currentThread().getName(), e.getMessage(), e);
                message.fail(500, e.getMessage());
            }
        });

        LOGGER.info("ReportWorkerVerticle consumers successfully initialized on EventBus.");
        startPromise.complete();
    }

    // Generates raw PDF bytes containing formatted subnet IP address tabular records.
    private byte[] generateSimplePdf(List<JsonObject> ipList, String subnetLabel) throws Exception {
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

    // Generates raw PDF bytes containing device vendor distribution summaries.
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

    // Pads or truncates a string to a fixed length for text alignment.
    private static String pad(String s, int len) {
        if (s == null) s = "-";
        if (s.length() >= len) return s.substring(0, len);
        return String.format("%-" + len + "s", s);
    }

    // Escapes special characters for PDF text streams.
    private static String sanitize(String s) {
        return s.replace("\\", "\\\\").replace("(", "\\(").replace(")", "\\)");
    }

    // Escapes special characters for safe inclusion in CSV fields.
    private static String csvEscape(String val) {
        if (val == null || "null".equals(val) || val.isEmpty()) return "-";
        return "\"" + val.replace("\"", "\"\"") + "\"";
    }
}
