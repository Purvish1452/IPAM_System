package com.motadata.ipam.service;

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

import java.io.*;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Asynchronous Vert.x Business Service for:
 *   - Subnet IP Scanning (ICMP Ping via executeBlocking)
 *   - Add Multiple IP Range
 *   - Select IP Range (Edit Status / Delete)
 *   - Import IPs from CSV
 *   - Export IPs to PDF / CSV
 *   - Download Sample CSV Template
 *
 * Direct Architecture: Handler -> Service -> PgPool -> PostgreSQL
 */
public class SubnetIPActionService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SubnetIPActionService.class);
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final String EXPORT_DIR = "file-uploads/exports/";

    private final Pool db;
    private final Vertx vertx;
    private final DiscoveryService discoveryService;

    // Track running scans to prevent duplicate scans
    private static final AtomicBoolean scanRunning = new AtomicBoolean(false);
    private static volatile String lastScanSubnetAddress = null;

    public SubnetIPActionService(Vertx vertx, Pool db) {
        this(vertx, db, null);
    }

    public SubnetIPActionService(Vertx vertx, Pool db, DiscoveryService discoveryService) {
        this.vertx = vertx;
        this.db = db;
        this.discoveryService = discoveryService;
        // Ensure export directory exists
        try {
            Files.createDirectories(Paths.get(EXPORT_DIR));
        } catch (Exception e) {
            LOGGER.warn("Could not create export directory: {}", e.getMessage());
        }
    }

    // ==========================================
    // 1. Scan Subnet (ICMP Ping every IP async)
    // ==========================================

    public Future<JsonObject> startScanSubnet(Long subnetId) {
        Promise<JsonObject> promise = Promise.promise();

        if (scanRunning.get()) {
            promise.complete(new JsonObject()
                    .put("success", false)
                    .put("message", "Please wait for some time, Import is running"));
            return promise.future();
        }

        // Fetch subnet details first
        String sql = "SELECT id, subnet_address, subnet_cidr FROM subnet_details WHERE id = $1";
        db.preparedQuery(sql).execute(Tuple.of(subnetId)).onComplete(ar -> {
            if (ar.failed() || ar.result().size() == 0) {
                promise.complete(new JsonObject()
                        .put("success", false)
                        .put("message", "Subnet not found"));
                return;
            }

            Row row = ar.result().iterator().next();
            String subnetAddress = row.getString("subnet_address");
            int cidr = row.getInteger("subnet_cidr") != null ? row.getInteger("subnet_cidr") : 24;

            scanRunning.set(true);
            lastScanSubnetAddress = subnetAddress + "/" + cidr;

            LOGGER.info("Starting subnet scan for {} (id={})", lastScanSubnetAddress, subnetId);

            promise.complete(new JsonObject()
                    .put("success", true)
                    .put("message", "Scan started for " + lastScanSubnetAddress));

            // Run the actual scan in a worker thread
            vertx.executeBlocking(() -> {
                try {
                    performScan(subnetId, subnetAddress, cidr);
                } catch (Exception e) {
                    LOGGER.error("Scan error: {}", e.getMessage());
                } finally {
                    scanRunning.set(false);
                    lastScanSubnetAddress = null;
                    LOGGER.info("Subnet scan completed for subnetId={}", subnetId);
                }
                return null;
            });
        });

        return promise.future();

    }

    private void performScan(Long subnetId, String networkAddress, int cidr) {
        try {
            List<String> ips = generateIPList(networkAddress, cidr);
            LOGGER.info("Scanning {} IPs in subnet {}", ips.size(), networkAddress);

            ExecutorService workers = Executors.newFixedThreadPool(16);
            try {
                List<CompletableFuture<Void>> tasks = new ArrayList<>();
                for (String ip : ips) {
                    tasks.add(CompletableFuture.runAsync(() -> scanAndStoreIp(ip, subnetId), workers));
                }
                CompletableFuture.allOf(tasks.toArray(new CompletableFuture[0])).get(120, TimeUnit.SECONDS);
            } finally {
                workers.shutdownNow();
            }

            // Update subnet stats after scan
            String updateStats = "UPDATE subnet_details SET " +
                    "used_ip = (SELECT count(*) FROM subnet_ip_details WHERE subnet_id = $1 AND status = 'USED'), " +
                    "available_ip = (SELECT count(*) FROM subnet_ip_details WHERE subnet_id = $1 AND status = 'AVAILABLE'), " +
                    "transient_ip = (SELECT count(*) FROM subnet_ip_details WHERE subnet_id = $1 AND status = 'TRANSIENT'), " +
                    "last_scan_time = CURRENT_TIMESTAMP " +
                    "WHERE id = $1";

            CompletableFuture<Void> finalCf = new CompletableFuture<>();
            db.preparedQuery(updateStats).execute(Tuple.of(subnetId)).onComplete(res -> finalCf.complete(null));
            finalCf.get(5, TimeUnit.SECONDS);

        } catch (Exception e) {
            LOGGER.error("Scan worker failed: {}", e.getMessage(), e);
        }
    }

    private void scanAndStoreIp(String ip, Long subnetId) {
        String status = "AVAILABLE";
        try {
            status = InetAddress.getByName(ip).isReachable(500) ? "USED" : "AVAILABLE";
        } catch (Exception e) {
            LOGGER.debug("Could not reach {}: {}", ip, e.getMessage());
        }

        String sql = "INSERT INTO subnet_ip_details " +
                "(ip_address, status, subnet_id, last_scan_time) " +
                "VALUES ($1, $2, $3, CURRENT_TIMESTAMP) " +
                "ON CONFLICT (ip_address) DO UPDATE " +
                "SET status = EXCLUDED.status, subnet_id = EXCLUDED.subnet_id, " +
                "last_scan_time = CURRENT_TIMESTAMP";
        CompletableFuture<Void> result = new CompletableFuture<>();
        db.preparedQuery(sql).execute(Tuple.of(ip, status, subnetId)).onComplete(ar -> {
            if (ar.failed()) {
                result.completeExceptionally(ar.cause());
            } else {
                result.complete(null);
            }
        });
        try {
            result.get(10, TimeUnit.SECONDS);
        } catch (Exception e) {
            LOGGER.warn("Failed to store scan result for {}: {}", ip, e.getMessage());
        }
    }

    /**
     * Generate all IPs in a CIDR block (max 1024 for safety)
     */
    private List<String> generateIPList(String networkAddr, int cidr) {
        List<String> ips = new ArrayList<>();
        try {
            String[] parts = networkAddr.split("\\.");
            long base = 0;
            for (String part : parts) {
                base = (base << 8) | Integer.parseInt(part.trim());
            }

            long hostBits = 32 - cidr;
            long totalHosts = (long) Math.pow(2, hostBits);
            long maxScan = Math.min(totalHosts - 2, 254); // skip network and broadcast, max 254

            for (long i = 1; i <= maxScan; i++) {
                long ipLong = base + i;
                String ip = ((ipLong >> 24) & 0xFF) + "." +
                        ((ipLong >> 16) & 0xFF) + "." +
                        ((ipLong >> 8) & 0xFF) + "." +
                        (ipLong & 0xFF);
                ips.add(ip);
            }
        } catch (Exception e) {
            LOGGER.error("IP list generation failed: {}", e.getMessage());
        }
        return ips;
    }

    public Future<JsonObject> getScanStatus() {
        Promise<JsonObject> promise = Promise.promise();
        if (scanRunning.get()) {
            promise.complete(new JsonObject()
                    .put("success", true)
                    .put("message", lastScanSubnetAddress));
        } else {
            promise.complete(new JsonObject()
                    .put("success", false)
                    .put("message", null));
        }
        return promise.future();
    }

    public Future<JsonObject> startScanGateway(Long gatewayId) {
        Promise<JsonObject> promise = Promise.promise();

        if (gatewayId == null) {
            promise.complete(new JsonObject().put("success", false).put("message", "Invalid gateway id"));
            return promise.future();
        }
        if (discoveryService == null) {
            promise.complete(new JsonObject().put("success", false)
                    .put("message", "Discovery service is not configured"));
            return promise.future();
        }
        if (scanRunning.get()) {
            promise.complete(new JsonObject().put("success", false)
                    .put("message", "Please wait for the current scan to complete"));
            return promise.future();
        }

        db.preparedQuery("SELECT id, gateway, description FROM gateway WHERE id = $1")
                .execute(Tuple.of(gatewayId))
                .onComplete(ar -> {
            if (ar.failed()) {
                LOGGER.error("Gateway lookup failed for id={}: {}", gatewayId, ar.cause().getMessage(), ar.cause());
                promise.complete(new JsonObject().put("success", false)
                        .put("message", "Gateway lookup failed: " + ar.cause().getMessage()));
                return;
            }
            if (ar.result().size() == 0) {
                promise.complete(new JsonObject().put("success", false).put("message", "Gateway not found"));
                return;
            }

            Row row = ar.result().iterator().next();
            String gatewayIp = row.getString("gateway");
            try {
                ipToLong(gatewayIp);
            } catch (RuntimeException e) {
                promise.complete(new JsonObject().put("success", false)
                        .put("message", "Gateway has an invalid IPv4 address"));
                return;
            }

            if (!scanRunning.compareAndSet(false, true)) {
                promise.complete(new JsonObject().put("success", false)
                        .put("message", "Please wait for the current scan to complete"));
                return;
            }

            String[] parts = gatewayIp.split("\\.");
            String subnetAddress = parts[0] + "." + parts[1] + "." + parts[2] + ".0";
            String subnetCidr = subnetAddress + "/24";
            String mask = "255.255.255.0";

            lastScanSubnetAddress = gatewayIp;
            promise.complete(new JsonObject().put("success", true)
                    .put("message", "Gateway scan started for " + gatewayIp));

            LOGGER.info("Starting gateway scan id={} ip={} via Go discovery cidr={}", gatewayId, gatewayIp, subnetCidr);

            discoveryService.triggerGoSubnetScan(subnetCidr).onComplete(scanAr -> {
                try {
                    if (scanAr.failed()) {
                        LOGGER.warn("Go gateway scan failed for {}: {}", gatewayIp, scanAr.cause().getMessage());
                        return;
                    }

                    JsonObject scanResult = scanAr.result();
                    JsonArray hosts = scanResult.getJsonArray("hosts", new JsonArray());
                    int activeCount = scanResult.getInteger("activeCount", 0);
                    boolean gatewayUp = false;
                    for (int i = 0; i < hosts.size(); i++) {
                        JsonObject host = hosts.getJsonObject(i);
                        if (gatewayIp.equals(host.getString("ip")) && "UP".equalsIgnoreCase(host.getString("status"))) {
                            gatewayUp = true;
                            break;
                        }
                    }

                    LOGGER.info("Go scan result for {}: totalHosts={} activeCount={} gatewayUp={}",
                            gatewayIp,
                            scanResult.getInteger("totalHosts"),
                            activeCount,
                            gatewayUp);

                    String status = (gatewayUp || activeCount > 0) ? "Active" : "Discovered";
                    String insertSubSql = "INSERT INTO discovered_subnet " +
                            "(subnet, subnet_address, subnet_mask, gateway, gateway_id, status, discovered_time) " +
                            "VALUES ($1, $2, $3, $4, $5, $6, CURRENT_TIMESTAMP)";
                    db.preparedQuery(insertSubSql)
                            .execute(Tuple.of(subnetCidr, subnetAddress, mask, gatewayIp, gatewayId, status))
                            .onComplete(subAr -> {
                                if (subAr.succeeded()) {
                                    LOGGER.info("Discovered subnet {} stored for gateway {}", subnetCidr, gatewayIp);
                                } else {
                                    LOGGER.warn("Failed to insert discovered subnet: {}", subAr.cause().getMessage());
                                }
                            });

                    db.preparedQuery("UPDATE gateway SET status = 'Active', previous_scan = CURRENT_TIMESTAMP WHERE id = $1")
                            .execute(Tuple.of(gatewayId))
                            .onComplete(gwAr -> {
                                if (gwAr.failed()) {
                                    LOGGER.warn("Failed to update gateway previous_scan: {}", gwAr.cause().getMessage());
                                }
                            });
                } finally {
                    scanRunning.set(false);
                    lastScanSubnetAddress = null;
                }
            });
        });

        return promise.future();
    }


    // ==========================================
    // 2. Add Multiple IP Range
    // ==========================================

    public Future<JsonObject> addMultipleIPRange(String startIp, String endIp, Long subnetId) {
        if (startIp == null || endIp == null || subnetId == null) {
            return Future.succeededFuture(new JsonObject().put("success", false).put("message", "Invalid parameters"));
        }

        return vertx.executeBlocking(() -> {
            try {
                List<String> ips = generateIPRange(startIp, endIp);
                if (ips.isEmpty()) {
                    return new JsonObject().put("success", false).put("message", "No IPs in specified range");
                }
                if (ips.size() > 500) {
                    return new JsonObject().put("success", false).put("message", "Range too large (max 500 IPs)");
                }

                AtomicInteger added = new AtomicInteger(0);
                for (String ip : ips) {
                    try {
                        String sql = "INSERT INTO subnet_ip_details (ip_address, status, subnet_id, last_scan_time) " +
                                "VALUES ($1, 'AVAILABLE', $2, CURRENT_TIMESTAMP) " +
                                "ON CONFLICT (ip_address) DO NOTHING";
                        CompletableFuture<Void> cf = new CompletableFuture<>();
                        db.preparedQuery(sql).execute(Tuple.of(ip, subnetId)).onComplete(res -> {
                            if (res.succeeded() && res.result().rowCount() > 0) added.incrementAndGet();
                            cf.complete(null);
                        });
                        cf.get(5, TimeUnit.SECONDS);
                    } catch (Exception e) {
                        LOGGER.warn("Failed to insert IP {}: {}", ip, e.getMessage());
                    }
                }

                // Update subnet used_ip count
                refreshSubnetStats(subnetId);

                return new JsonObject()
                        .put("success", true)
                        .put("message", added.get() + " IP(s) added successfully");

            } catch (Exception e) {
                LOGGER.error("Add Multiple IP range failed: {}", e.getMessage(), e);
                return new JsonObject().put("success", false).put("message", "Failed: " + e.getMessage());
            }
        });
    }

    // ==========================================
    // 3. Select IP Range - Update Status
    // ==========================================

    public Future<JsonObject> updateIPRangeStatus(String startIp, String endIp, String status, Long subnetId) {
        if (startIp == null || endIp == null || status == null) {
            return Future.succeededFuture(new JsonObject().put("success", false).put("message", "Invalid parameters"));
        }

        String dbStatus = status.toUpperCase().trim();

        return vertx.executeBlocking(() -> {
            try {
                List<String> ips = generateIPRange(startIp, endIp);
                if (ips.isEmpty()) {
                    return new JsonObject().put("success", false).put("message", "No IPs in range");
                }

                AtomicInteger updated = new AtomicInteger(0);
                for (String ip : ips) {
                    try {
                        String sql = "UPDATE subnet_ip_details SET status = $1, last_scan_time = CURRENT_TIMESTAMP " +
                                "WHERE ip_address = $2";
                        CompletableFuture<Void> cf = new CompletableFuture<>();
                        db.preparedQuery(sql).execute(Tuple.of(dbStatus, ip)).onComplete(res -> {
                            if (res.succeeded()) updated.incrementAndGet();
                            cf.complete(null);
                        });
                        cf.get(5, TimeUnit.SECONDS);
                    } catch (Exception e) {
                        LOGGER.warn("Failed to update IP status {}: {}", ip, e.getMessage());
                    }
                }

                refreshSubnetStats(subnetId);

                return new JsonObject()
                        .put("success", true)
                        .put("message", updated.get() + " IP(s) status updated to " + dbStatus);

            } catch (Exception e) {
                LOGGER.error("Update IP range status failed: {}", e.getMessage(), e);
                return new JsonObject().put("success", false).put("message", "Failed: " + e.getMessage());
            }
        });
    }

    // ==========================================
    // 4. Select IP Range - Delete
    // ==========================================

    public Future<JsonObject> deleteIPRange(String startIp, String endIp, Long subnetId) {
        if (startIp == null || endIp == null) {
            return Future.succeededFuture(new JsonObject().put("success", false).put("message", "Invalid parameters"));
        }

        return vertx.executeBlocking(() -> {
            try {
                List<String> ips = generateIPRange(startIp, endIp);
                if (ips.isEmpty()) {
                    return new JsonObject().put("success", false).put("message", "No IPs in range");
                }

                AtomicInteger deleted = new AtomicInteger(0);
                for (String ip : ips) {
                    try {
                        CompletableFuture<Void> cf = new CompletableFuture<>();
                        db.preparedQuery("DELETE FROM subnet_ip_details WHERE ip_address = $1 AND subnet_id = $2").execute(Tuple.of(ip, subnetId)).onComplete(res -> {
                                    if (res.succeeded() && res.result().rowCount() > 0) deleted.incrementAndGet();
                                    cf.complete(null);
                                });
                        cf.get(5, TimeUnit.SECONDS);
                    } catch (Exception e) {
                        LOGGER.warn("Failed to delete IP {}: {}", ip, e.getMessage());
                    }
                }

                refreshSubnetStats(subnetId);

                return new JsonObject()
                        .put("success", true)
                        .put("message", deleted.get() + " IP(s) deleted successfully");

            } catch (Exception e) {
                LOGGER.error("Delete IP range failed: {}", e.getMessage(), e);
                return new JsonObject().put("success", false).put("message", "Failed: " + e.getMessage());
            }
        });
    }

    // ==========================================
    // 5. Import IPs from CSV
    // ==========================================

    public Future<JsonObject> importIPsFromCSV(byte[] csvBytes, Long subnetId) {
        if (csvBytes == null || csvBytes.length == 0) {
            return Future.succeededFuture(new JsonObject().put("success", false).put("message", "CSV file is empty"));
        }

        return vertx.executeBlocking(() -> {
            try {
                List<String[]> rows = parseCSV(new String(csvBytes, "UTF-8"));
                if (rows.isEmpty()) {
                    return new JsonObject().put("success", false).put("message", "No valid data in CSV");
                }

                AtomicInteger imported = new AtomicInteger(0);
                AtomicInteger skipped = new AtomicInteger(0);

                // Skip header row
                int startRow = 0;
                if (!rows.isEmpty() && rows.get(0).length > 0 &&
                        (rows.get(0)[0].equalsIgnoreCase("IP Address") || rows.get(0)[0].equalsIgnoreCase("ip_address"))) {
                    startRow = 1;
                }

                for (int i = startRow; i < rows.size(); i++) {
                    String[] cols = rows.get(i);
                    if (cols.length == 0 || cols[0].trim().isEmpty()) continue;

                    String ipAddress = cols[0].trim();
                    String macAddress = cols.length > 1 ? cols[1].trim() : "";
                    String hostName = cols.length > 2 ? cols[2].trim() : "";
                    String status = cols.length > 3 ? cols[3].trim().toUpperCase() : "AVAILABLE";
                    String deviceType = cols.length > 4 ? cols[4].trim() : "";
                    String description = cols.length > 5 ? cols[5].trim() : "";

                    // Validate IP format
                    if (!ipAddress.matches("^\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}$")) {
                        skipped.incrementAndGet();
                        continue;
                    }

                    // Normalize status
                    if (!status.matches("USED|AVAILABLE|TRANSIENT|RESERVED")) {
                        status = "AVAILABLE";
                    }

                    try {
                        String sql = "INSERT INTO subnet_ip_details " +
                                "(ip_address, mac_address, host_name, status, device_type, system_description, subnet_id, last_scan_time) " +
                                "VALUES ($1, $2, $3, $4, $5, $6, $7, CURRENT_TIMESTAMP) " +
                                "ON CONFLICT (ip_address) DO UPDATE SET " +
                                "mac_address = EXCLUDED.mac_address, " +
                                "host_name = EXCLUDED.host_name, " +
                                "status = EXCLUDED.status, " +
                                "device_type = EXCLUDED.device_type, " +
                                "system_description = EXCLUDED.system_description, " +
                                "last_scan_time = CURRENT_TIMESTAMP";

                        final String finalIp = ipAddress;
                        final String finalMac = macAddress;
                        final String finalHost = hostName;
                        final String finalStatus = status;
                        final String finalDevice = deviceType;
                        final String finalDesc = description;

                        CompletableFuture<Void> cf = new CompletableFuture<>();
                        db.preparedQuery(sql).execute(Tuple.of(finalIp, finalMac, finalHost, finalStatus, finalDevice, finalDesc, subnetId)).onComplete(res -> {
                                    if (res.succeeded()) imported.incrementAndGet();
                                    else skipped.incrementAndGet();
                                    cf.complete(null);
                                });
                        cf.get(5, TimeUnit.SECONDS);
                    } catch (Exception e) {
                        skipped.incrementAndGet();
                        LOGGER.warn("Failed to import IP row {}: {}", i, e.getMessage());
                    }
                }

                refreshSubnetStats(subnetId);

                return new JsonObject()
                        .put("success", true)
                        .put("message", "Import complete: " + imported.get() + " imported, " + skipped.get() + " skipped");

            } catch (Exception e) {
                LOGGER.error("CSV import failed: {}", e.getMessage(), e);
                return new JsonObject().put("success", false).put("message", "Import failed: " + e.getMessage());
            }
        });
    }

    // ==========================================
    // 6. Export IPs to CSV
    // ==========================================

    public Future<JsonObject> exportSubnetIPsToCSV(Long subnetId, List<String> selectedIds) {
        Promise<JsonObject> promise = Promise.promise();

        String sql = buildExportSQL(subnetId, selectedIds);

        db.preparedQuery(sql).execute(Tuple.of(subnetId)).onComplete(ar -> {
            if (ar.failed()) {
                promise.complete(new JsonObject().put("success", false).put("message", "Export failed"));
                return;
            }

            StringBuilder sb = new StringBuilder();
            sb.append("IP Address,MAC Address,Host Name,Status,Device Type,DNS Status,Last Alive Time,Location,Description\n");

            for (Row row : ar.result()) {
                sb.append(safe(row.getString("ip_address"))).append(",")
                        .append(safe(row.getString("mac_address"))).append(",")
                        .append(safe(row.getString("host_name"))).append(",")
                        .append(safe(row.getString("status"))).append(",")
                        .append(safe(row.getString("device_type"))).append(",")
                        .append(safe(row.getString("dns_status"))).append(",")
                        .append(safe("N/A")).append(",")
                        .append(safe(row.getString("location"))).append(",")
                        .append(safe(row.getString("system_description"))).append("\n");
            }

            vertx.<JsonObject>executeBlocking(() -> {
                try {
                    String filename = "SubnetIP_Export_" + subnetId + "_" + System.currentTimeMillis() + ".csv";
                    String filePath = EXPORT_DIR + filename;
                    Files.write(Paths.get(filePath), sb.toString().getBytes("UTF-8"));

                    return new JsonObject().put("success", true).put("data", filename);
                } catch (Exception e) {
                    LOGGER.error("CSV export write failed: {}", e.getMessage(), e);
                    return new JsonObject().put("success", false).put("message", e.getMessage());
                }
            }).onComplete(res -> {
                promise.complete(res.succeeded() ? res.result() :
                        new JsonObject().put("success", false).put("message", "Export write error"));
            });
        });

        return promise.future();

    }

    // ==========================================
    // 7. Export IPs to PDF
    // ==========================================

    public Future<JsonObject> exportSubnetIPsToPDF(Long subnetId, List<String> selectedIds) {
        Promise<JsonObject> promise = Promise.promise();

        String sql = buildExportSQL(subnetId, selectedIds);

        db.preparedQuery(sql).execute(Tuple.of(subnetId)).onComplete(ar -> {
            if (ar.failed()) {
                promise.complete(new JsonObject().put("success", false).put("message", "Export failed"));
                return;
            }

            List<JsonObject> ipList = new ArrayList<>();
            for (Row row : ar.result()) {
                ipList.add(new JsonObject()
                        .put("ipAddress", safe(row.getString("ip_address")))
                        .put("macAddress", safe(row.getString("mac_address")))
                        .put("hostName", safe(row.getString("host_name")))
                        .put("status", safe(row.getString("status")))
                        .put("deviceType", safe(row.getString("device_type")))
                        .put("dnsStatus", safe(row.getString("dns_status")))
                        .put("location", safe(row.getString("location"))));
            }

            vertx.<JsonObject>executeBlocking(() -> {
                try {
                    String filename = "SubnetIP_Export_" + subnetId + "_" + System.currentTimeMillis() + ".pdf";
                    String filePath = EXPORT_DIR + filename;
                    byte[] pdfBytes = generateSimplePDF(ipList, subnetId);
                    Files.write(Paths.get(filePath), pdfBytes);
                    return new JsonObject().put("success", true).put("data", filename);
                } catch (Exception e) {
                    LOGGER.error("PDF export write failed: {}", e.getMessage(), e);
                    return new JsonObject().put("success", false).put("message", e.getMessage());
                }
            }).onComplete(res -> {
                promise.complete(res.succeeded() ? res.result() :
                        new JsonObject().put("success", false).put("message", "PDF export error"));
            });
        });

        return promise.future();
    }

    // ==========================================
    // 8. Download Sample CSV Template
    // ==========================================

    public Future<JsonObject> getSampleCSVTemplate(Long subnetId) {
        return vertx.executeBlocking(() -> {
            try {
                String csv = "IP Address,MAC Address,Host Name,Status,Device Type,Description\n" +
                        "192.168.10.10,00:11:22:33:44:55,server-01,USED,Server,Primary web server\n" +
                        "192.168.10.11,00:11:22:33:44:56,server-02,AVAILABLE,,Spare server\n" +
                        "192.168.10.12,00:11:22:33:44:57,printer-01,USED,Printer,Floor 2 printer\n";

                String filename = "SubnetIP_Sample_Template.csv";
                String filePath = EXPORT_DIR + filename;
                Files.write(Paths.get(filePath), csv.getBytes("UTF-8"));

                return new JsonObject().put("success", true).put("data", filename);
            } catch (Exception e) {
                return new JsonObject().put("success", false).put("message", e.getMessage());
            }
        });
    }

    // ==========================================
    // 9. Read exported file as bytes
    // ==========================================

    public Future<Buffer> readExportedFile(String filename) {
        Promise<Buffer> promise = Promise.promise();
        String filePath = EXPORT_DIR + filename;
        vertx.fileSystem().readFile(filePath).onComplete(ar -> {
            if (ar.succeeded()) {
                promise.complete(ar.result());
            } else {
                promise.fail("File not found: " + filename);
            }
        });
        return promise.future();
    }

    // ==========================================
    // Private Helpers
    // ==========================================

    private String buildExportSQL(Long subnetId, List<String> selectedIds) {
        StringBuilder sql = new StringBuilder(
                "SELECT ip_address, mac_address, host_name, status, device_type, dns_status, location, system_description " +
                "FROM subnet_ip_details WHERE subnet_id = $1");
        if (selectedIds != null && !selectedIds.isEmpty()) {
            List<String> ids = selectedIds.stream()
                    .map(String::trim)
                    .filter(id -> id.matches("\\d+"))
                    .collect(Collectors.toList());
            if (!ids.isEmpty()) {
                sql.append(" AND id IN (").append(String.join(",", ids)).append(")");
            }
        }
        return sql.append(" ORDER BY ip_address ASC").toString();
    }

    private List<String> generateIPRange(String startIp, String endIp) {
        List<String> ips = new ArrayList<>();
        try {
            long start = ipToLong(startIp);
            long end = ipToLong(endIp);
            if (start > end) { long tmp = start; start = end; end = tmp; }
            long range = end - start + 1;
            if (range > 500) range = 500;
            for (long i = 0; i < range; i++) {
                ips.add(longToIp(start + i));
            }
        } catch (Exception e) {
            LOGGER.error("IP range generation failed: {}", e.getMessage());
        }
        return ips;
    }

    private long ipToLong(String ip) {
        String[] parts = ip.trim().split("\\.");
        if (parts.length != 4) {
            throw new IllegalArgumentException("Invalid IPv4 address");
        }
        long result = 0;
        for (String part : parts) {
            int octet = Integer.parseInt(part.trim());
            if (octet < 0 || octet > 255) {
                throw new IllegalArgumentException("Invalid IPv4 octet");
            }
            result = (result << 8) | octet;
        }
        return result;
    }

    private String longToIp(long ip) {
        return ((ip >> 24) & 0xFF) + "." + ((ip >> 16) & 0xFF) + "." + ((ip >> 8) & 0xFF) + "." + (ip & 0xFF);
    }

    private List<String[]> parseCSV(String csvContent) {
        List<String[]> rows = new ArrayList<>();
        String[] lines = csvContent.split("\n");
        for (String line : lines) {
            line = line.trim().replace("\r", "");
            if (!line.isEmpty()) {
                rows.add(line.split(",", -1));
            }
        }
        return rows;
    }

    private String safe(String val) {
        if (val == null || val.isEmpty()) return "-";
        return "\"" + val.replace("\"", "\"\"") + "\"";
    }

    private void refreshSubnetStats(Long subnetId) {
        try {
            String updateSql = "UPDATE subnet_details SET " +
                    "used_ip = (SELECT count(*) FROM subnet_ip_details WHERE subnet_id = $1 AND status = 'USED'), " +
                    "available_ip = (SELECT count(*) FROM subnet_ip_details WHERE subnet_id = $1 AND status = 'AVAILABLE'), " +
                    "transient_ip = (SELECT count(*) FROM subnet_ip_details WHERE subnet_id = $1 AND UPPER(status) = 'TRANSIENT'), " +
                    "last_scan_time = CURRENT_TIMESTAMP WHERE id = $1";
            CompletableFuture<Void> cf = new CompletableFuture<>();
            db.preparedQuery(updateSql).execute(Tuple.of(subnetId)).onComplete(res -> cf.complete(null));
            cf.get(5, TimeUnit.SECONDS);
        } catch (Exception e) {
            LOGGER.warn("Failed to refresh subnet stats: {}", e.getMessage());
        }
    }

    /**
     * Generate a minimal plain-text PDF using raw PDF syntax (no external library needed)
     */
    private byte[] generateSimplePDF(List<JsonObject> ipList, Long subnetId) throws Exception {
        StringBuilder pdf = new StringBuilder();
        List<String> objects = new ArrayList<>();

        // Build table rows text
        StringBuilder content = new StringBuilder();
        content.append("BT\n");
        content.append("/F1 12 Tf\n");
        content.append("50 750 Td\n");
        content.append("(Subnet IP Address Export - Subnet ID: ").append(subnetId).append(") Tj\n");
        content.append("0 -20 Td\n");
        content.append("/F1 9 Tf\n");
        content.append("(Generated: ").append(DATE_FORMAT.format(new Date())).append(") Tj\n");
        content.append("0 -25 Td\n");
        content.append("/F1 10 Tf\n");
        content.append("(IP Address            Status     MAC Address        Host Name) Tj\n");
        content.append("0 -18 Td\n");
        content.append("(--------------------------------------------------------------) Tj\n");
        content.append("0 -5 Td\n");

        int yPos = 640;
        int page = 1;
        for (int idx = 0; idx < Math.min(ipList.size(), 60); idx++) {
            JsonObject ip = ipList.get(idx);
            String line = String.format("%-22s %-10s %-20s %-20s",
                    ip.getString("ipAddress", "-"),
                    ip.getString("status", "-"),
                    ip.getString("macAddress", "-"),
                    ip.getString("hostName", "-"));
            // Escape PDF special chars
            line = line.replace("\\", "\\\\").replace("(", "\\(").replace(")", "\\)");
            content.append("0 -18 Td\n");
            content.append("(").append(line).append(") Tj\n");
        }

        if (ipList.size() > 60) {
            content.append("0 -18 Td\n");
            content.append("(... and ").append(ipList.size() - 60).append(" more records) Tj\n");
        }

        content.append("ET\n");

        // Build raw PDF
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        List<Integer> offsets = new ArrayList<>();

        String header = "%PDF-1.4\n";
        out.write(header.getBytes());

        // Object 1 - Catalog
        offsets.add(out.size());
        String obj1 = "1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n";
        out.write(obj1.getBytes());

        // Object 2 - Pages
        offsets.add(out.size());
        String obj2 = "2 0 obj\n<< /Type /Pages /Kids [3 0 R] /Count 1 >>\nendobj\n";
        out.write(obj2.getBytes());

        // Object 3 - Page
        offsets.add(out.size());
        String obj3 = "3 0 obj\n<< /Type /Page /Parent 2 0 R /MediaBox [0 0 612 792] " +
                "/Contents 4 0 R /Resources << /Font << /F1 5 0 R >> >> >>\nendobj\n";
        out.write(obj3.getBytes());

        // Object 4 - Content stream
        byte[] contentBytes = content.toString().getBytes("ISO-8859-1");
        offsets.add(out.size());
        String obj4Header = "4 0 obj\n<< /Length " + contentBytes.length + " >>\nstream\n";
        out.write(obj4Header.getBytes());
        out.write(contentBytes);
        out.write("\nendstream\nendobj\n".getBytes());

        // Object 5 - Font
        offsets.add(out.size());
        String obj5 = "5 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Courier >>\nendobj\n";
        out.write(obj5.getBytes());

        // Cross-reference table
        int xrefOffset = out.size();
        out.write("xref\n".getBytes());
        out.write(("0 " + (offsets.size() + 1) + "\n").getBytes());
        out.write("0000000000 65535 f \n".getBytes());
        for (int offset : offsets) {
            out.write(String.format("%010d 00000 n \n", offset).getBytes());
        }

        // Trailer
        out.write("trailer\n".getBytes());
        out.write(("<< /Size " + (offsets.size() + 1) + " /Root 1 0 R >>\n").getBytes());
        out.write("startxref\n".getBytes());
        out.write((xrefOffset + "\n").getBytes());
        out.write("%%EOF\n".getBytes());

        return out.toByteArray();
    }
}
