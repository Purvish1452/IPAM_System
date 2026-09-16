package com.motadata.ipam.feature.subnet;

import com.motadata.ipam.core.verticle.NetworkWorkerVerticle;
import com.motadata.ipam.core.verticle.ReportWorkerVerticle;
import com.motadata.ipam.feature.alert.AlertService;
import com.motadata.ipam.feature.discovery.DiscoveryService;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * Reactive Business Service for Subnet IP Actions.
 * Architecture: Event Loop Handler -> Service -> PgPool / EventBus Worker Verticles.
 * Zero blocking operations on the Event Loop.
 */
public class SubnetIPActionService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SubnetIPActionService.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String EXPORT_DIR = "file-uploads/exports/";

    private final Pool db;
    private final Vertx vertx;
    private final DiscoveryService discoveryService;
    private final AlertService alertService;

    // Track running scans to prevent duplicate scans (10 min max timeout)
    private static final AtomicBoolean scanRunning = new AtomicBoolean(false);
    private static volatile String lastScanSubnetAddress = null;
    private static volatile long lastScanStartTime = 0L;
    private static final long MAX_SCAN_DURATION_MS = 600_000L; // 10 minutes

    // Constructs SubnetIPActionService with default discovery and alert dependencies.
    public SubnetIPActionService(Vertx vertx, Pool db) {
        this(vertx, db, null, null);
    }

    // Constructs SubnetIPActionService with a DiscoveryService instance.
    public SubnetIPActionService(Vertx vertx, Pool db, DiscoveryService discoveryService) {
        this(vertx, db, discoveryService, null);
    }

    // Constructs SubnetIPActionService with explicit discovery and alert service dependencies.
    public SubnetIPActionService(Vertx vertx, Pool db, DiscoveryService discoveryService, AlertService alertService) {
        this.vertx = vertx;
        this.db = db;
        this.discoveryService = discoveryService != null ? discoveryService : new DiscoveryService(vertx, db);
        this.alertService = alertService;
        try {
            Files.createDirectories(Paths.get(EXPORT_DIR));
        } catch (Exception e) {
            LOGGER.warn("Could not create export directory: {}", e.getMessage());
        }
    }

    // ==========================================
    // 1. Scan Subnet (Dispatched to NetworkWorkerVerticle via EventBus)
    // ==========================================

    // Dispatches a subnet scanning job to NetworkWorkerVerticle via EventBus with CIDR-aware timeout.
    public Future<JsonObject> startScanSubnet(Long subnetId) {
        long now = System.currentTimeMillis();
        if (scanRunning.get()) {
            if (now - lastScanStartTime > MAX_SCAN_DURATION_MS) {
                LOGGER.warn("Forcing reset of stuck scan state in startScanSubnet (exceeded 10 minutes)");
                scanRunning.set(false);
                lastScanSubnetAddress = null;
            } else {
                return Future.succeededFuture(new JsonObject()
                        .put("success", false)
                        .put("message", "Please wait for some time, Scan is running"));
            }
        }

        // Fetch subnet details first
        String sql = "SELECT id, subnet_address, subnet_cidr FROM subnet_details WHERE id = $1";
        return db.preparedQuery(sql).execute(Tuple.of(subnetId)).map(rows -> {
            if (rows.size() == 0) {
                return new JsonObject()
                        .put("success", false)
                        .put("message", "Subnet not found");
            }

            Row row = rows.iterator().next();
            String subnetAddress = row.getString("subnet_address");
            int cidr = row.getInteger("subnet_cidr") != null ? row.getInteger("subnet_cidr") : 24;

            if (!scanRunning.compareAndSet(false, true)) {
                return new JsonObject()
                        .put("success", false)
                        .put("message", "Please wait for some time, Scan is running");
            }

            lastScanStartTime = System.currentTimeMillis();
            lastScanSubnetAddress = subnetAddress + "/" + cidr;

            LOGGER.info("Dispatching subnet scan for {} (id={}) to NetworkWorkerVerticle", lastScanSubnetAddress, subnetId);

            JsonObject scanPayload = new JsonObject()
                    .put("subnetId", subnetId)
                    .put("subnetAddress", subnetAddress)
                    .put("cidr", cidr);

            // Compute dynamic timeout based on CIDR size (e.g. /16 has 65k IPs -> 10 min timeout)
            long scanTimeoutMs = calculateScanTimeout(cidr);
            DeliveryOptions deliveryOptions = new DeliveryOptions().setSendTimeout(scanTimeoutMs);

            // Send non-blocking request to NetworkWorkerVerticle via EventBus in background
            vertx.eventBus().<JsonObject>request(NetworkWorkerVerticle.ADDR_SCAN, scanPayload, deliveryOptions)
                    .onComplete(replyAr -> {
                        scanRunning.set(false);
                        lastScanSubnetAddress = null;
                        if (replyAr.succeeded()) {
                            LOGGER.info("Worker subnet scan completed successfully for subnet {} (id={})", subnetAddress, subnetId);
                        } else {
                            LOGGER.error("Worker subnet scan failed for subnet {}: {}", subnetAddress, replyAr.cause().getMessage());
                        }
                    });

            return new JsonObject()
                    .put("success", true)
                    .put("message", "Scan started for " + lastScanSubnetAddress)
                    .put("scopeAddress", lastScanSubnetAddress)
                    .put("subnetId", subnetId);
        });
    }

    // Calculates appropriate EventBus send timeout based on subnet CIDR size.
    private long calculateScanTimeout(int cidr) {
        if (cidr <= 16) {
            return 600_000L; // 10 minutes for /16 (65,536 hosts)
        } else if (cidr <= 20) {
            return 300_000L; // 5 minutes for /20 (4,096 hosts)
        } else if (cidr <= 22) {
            return 180_000L; // 3 minutes for /22 (1,024 hosts)
        } else {
            return 120_000L; // 2 minutes for /24 or smaller
        }
    }

    // Returns current running status and target address of active network scans.
    public Future<JsonObject> getScanStatus() {
        long now = System.currentTimeMillis();
        if (scanRunning.get()) {
            if (now - lastScanStartTime > MAX_SCAN_DURATION_MS) {
                LOGGER.warn("Auto-clearing stuck scan status lock (exceeded 10 minutes)");
                scanRunning.set(false);
                lastScanSubnetAddress = null;
                return Future.succeededFuture(new JsonObject()
                        .put("success", false)
                        .put("message", (Object) null));
            } else {
                return Future.succeededFuture(new JsonObject()
                        .put("success", true)
                        .put("message", lastScanSubnetAddress));
            }
        } else {
            return Future.succeededFuture(new JsonObject()
                    .put("success", false)
                    .put("message", (Object) null));
        }
    }

    // Dispatches gateway IP scan job to NetworkWorkerVerticle via EventBus.
    public Future<JsonObject> startScanGateway(Long gatewayId) {
        if (gatewayId == null) {
            return Future.succeededFuture(new JsonObject().put("success", false).put("message", "Invalid gateway id"));
        }

        long now = System.currentTimeMillis();
        if (scanRunning.get()) {
            if (now - lastScanStartTime > 60000) {
                LOGGER.warn("Forcing reset of stuck scan state in startScanGateway");
                scanRunning.set(false);
                lastScanSubnetAddress = null;
            } else {
                return Future.succeededFuture(new JsonObject().put("success", false)
                        .put("message", "Please wait for the current scan to complete"));
            }
        }

        return db.preparedQuery("SELECT id, gateway, description, COALESCE(name, 'Core Gateway') as name FROM gateway WHERE id = $1")
                .execute(Tuple.of(gatewayId))
                .map(rows -> {
                    if (rows.size() == 0) {
                        return new JsonObject().put("success", false).put("message", "Gateway not found");
                    }

                    Row row = rows.iterator().next();
                    String rawGateway = row.getString("gateway");
                    final String gatewayIp = (rawGateway != null && rawGateway.contains(".")) ? rawGateway : "192.168.1.1";

                    if (!scanRunning.compareAndSet(false, true)) {
                        return new JsonObject().put("success", false)
                                .put("message", "Please wait for the current scan to complete");
                    }

                    lastScanStartTime = System.currentTimeMillis();
                    lastScanSubnetAddress = gatewayIp;

                    String[] parts = gatewayIp.split("\\.");
                    String subnetAddress = parts[0] + "." + parts[1] + "." + parts[2] + ".0";
                    String subnetCidr = subnetAddress + "/24";

                    LOGGER.info("Dispatching gateway discovery scan for gateway id={}, ip={}, subnetCidr={}",
                            gatewayId, gatewayIp, subnetCidr);

                    JsonObject scanPayload = new JsonObject()
                            .put("subnetCidr", subnetCidr)
                            .put("gatewayId", gatewayId)
                            .put("gatewayIp", gatewayIp)
                            .put("timeoutMs", 1000)
                            .put("concurrency", 64);

                    // Send non-blocking request to NetworkWorkerVerticle via EventBus in background
                    vertx.eventBus().<JsonObject>request(NetworkWorkerVerticle.ADDR_DISCOVERY_SCAN, scanPayload, new DeliveryOptions().setSendTimeout(180_000L))
                            .onComplete(res -> {
                                scanRunning.set(false);
                                lastScanSubnetAddress = null;
                                if (res.succeeded()) {
                                    LOGGER.info("Gateway discovery scan completed successfully for gateway id={}, ip={}", gatewayId, gatewayIp);
                                } else {
                                    LOGGER.error("Worker discovery scan failed: {}", res.cause().getMessage());
                                    db.preparedQuery("UPDATE gateway SET previous_scan = CURRENT_TIMESTAMP WHERE id = $1")
                                            .execute(Tuple.of(gatewayId));
                                }
                            });

                    return new JsonObject()
                            .put("success", true)
                            .put("message", "Gateway discovery scan started for " + gatewayIp)
                            .put("scopeAddress", gatewayIp)
                            .put("gatewayIp", gatewayIp);
                })
                .recover(err -> {
                    LOGGER.error("Gateway lookup failed for id={}: {}", gatewayId, err.getMessage(), err);
                    return Future.succeededFuture(new JsonObject().put("success", false)
                            .put("message", "Gateway lookup failed: " + err.getMessage()));
                });
    }

    // ==========================================
    // 2. Add Multiple IP Range (Reactive Batching)
    // ==========================================

    // Adds a batch range of IP addresses to the subnet in the database.
    public Future<JsonObject> addMultipleIPRange(String startIp, String endIp, Long subnetId) {
        if (startIp == null || endIp == null || subnetId == null) {
            return Future.succeededFuture(new JsonObject().put("success", false).put("message", "Invalid parameters"));
        }

        List<String> ips = generateIPRange(startIp, endIp);
        if (ips.isEmpty()) {
            return Future.succeededFuture(new JsonObject().put("success", false).put("message", "No IPs in specified range"));
        }
        if (ips.size() > 500) {
            return Future.succeededFuture(new JsonObject().put("success", false).put("message", "Range too large (max 500 IPs)"));
        }

        List<Tuple> batch = new ArrayList<>();
        for (String ip : ips) {
            batch.add(Tuple.of(ip, subnetId));
        }

        String sql = "INSERT INTO subnet_ip_details (ip_address, status, subnet_id, last_scan_time) " +
                "VALUES ($1, 'AVAILABLE', $2, CURRENT_TIMESTAMP) " +
                "ON CONFLICT (ip_address) DO NOTHING";

        return db.preparedQuery(sql).executeBatch(batch)
                .compose(rows -> refreshSubnetStats(subnetId))
                .map(v -> new JsonObject()
                        .put("success", true)
                        .put("message", ips.size() + " IP(s) processed successfully"))
                .recover(err -> {
                    LOGGER.error("Add Multiple IP range failed: {}", err.getMessage());
                    return Future.succeededFuture(new JsonObject().put("success", false).put("message", "Failed: " + err.getMessage()));
                });
    }

    // ==========================================
    // 3. Select IP Range - Update Status (Reactive Batching)
    // ==========================================

    // Updates the status of an IP range in a subnet using reactive batching.
    public Future<JsonObject> updateIPRangeStatus(String startIp, String endIp, String status, Long subnetId) {
        if (startIp == null || endIp == null || status == null) {
            return Future.succeededFuture(new JsonObject().put("success", false).put("message", "Invalid parameters"));
        }

        String dbStatus = status.toUpperCase().trim();
        List<String> ips = generateIPRange(startIp, endIp);
        if (ips.isEmpty()) {
            return Future.succeededFuture(new JsonObject().put("success", false).put("message", "No IPs in range"));
        }

        List<Tuple> batch = new ArrayList<>();
        for (String ip : ips) {
            batch.add(Tuple.of(dbStatus, ip));
        }

        String sql = "UPDATE subnet_ip_details SET status = $1, last_scan_time = CURRENT_TIMESTAMP WHERE ip_address = $2";

        return db.preparedQuery(sql).executeBatch(batch)
                .compose(rows -> refreshSubnetStats(subnetId))
                .map(v -> new JsonObject()
                        .put("success", true)
                        .put("message", ips.size() + " IP(s) status updated to " + dbStatus))
                .recover(err -> {
                    LOGGER.error("Update IP range status failed: {}", err.getMessage());
                    return Future.succeededFuture(new JsonObject().put("success", false).put("message", "Failed: " + err.getMessage()));
                });
    }

    // ==========================================
    // 4. Select IP Range - Delete (Reactive Batching)
    // ==========================================

    // Deletes an IP range from a subnet using reactive batching.
    public Future<JsonObject> deleteIPRange(String startIp, String endIp, Long subnetId) {
        if (startIp == null || endIp == null) {
            return Future.succeededFuture(new JsonObject().put("success", false).put("message", "Invalid parameters"));
        }

        List<String> ips = generateIPRange(startIp, endIp);
        if (ips.isEmpty()) {
            return Future.succeededFuture(new JsonObject().put("success", false).put("message", "No IPs in range"));
        }

        List<Tuple> batch = new ArrayList<>();
        for (String ip : ips) {
            batch.add(Tuple.of(ip, subnetId));
        }

        String sql = "DELETE FROM subnet_ip_details WHERE ip_address = $1 AND subnet_id = $2";

        return db.preparedQuery(sql).executeBatch(batch)
                .compose(rows -> refreshSubnetStats(subnetId))
                .map(v -> new JsonObject()
                        .put("success", true)
                        .put("message", ips.size() + " IP(s) deleted successfully"))
                .recover(err -> {
                    LOGGER.error("Delete IP range failed: {}", err.getMessage());
                    return Future.succeededFuture(new JsonObject().put("success", false).put("message", "Failed: " + err.getMessage()));
                });
    }

    // ==========================================
    // 5. Import IPs from CSV (EventBus Worker)
    // ==========================================

    // Imports IP addresses and metadata from CSV bytes for a subnet.
    public Future<JsonObject> importIPsFromCSV(byte[] csvBytes, Long subnetId) {
        if (csvBytes == null || csvBytes.length == 0) {
            return Future.succeededFuture(new JsonObject().put("success", false).put("message", "CSV file is empty"));
        }

        String csvText = new String(csvBytes, StandardCharsets.UTF_8);
        JsonObject payload = new JsonObject()
                .put("csvText", csvText)
                .put("subnetId", subnetId);

        return vertx.eventBus().<JsonObject>request(NetworkWorkerVerticle.ADDR_IMPORT_CSV, payload)
                .compose(reply -> refreshSubnetStats(subnetId).map(v -> reply.body()))
                .recover(err -> Future.succeededFuture(new JsonObject().put("success", false).put("message", "Import failed: " + err.getMessage())));
    }

    // ==========================================
    // 6. Export IPs to CSV (EventBus Report Worker)
    // ==========================================

    // Exports subnet IP details to a CSV file via the report worker.
    public Future<JsonObject> exportSubnetIPsToCSV(Long subnetId, List<String> selectedIds) {
        String sql = buildExportSQL(subnetId, selectedIds);

        return db.preparedQuery(sql).execute(Tuple.of(subnetId))
                .compose(rows -> {
                    JsonArray ipList = new JsonArray();
                    for (Row row : rows) {
                        ipList.add(new JsonObject()
                                .put("ipAddress", safe(row.getString("ip_address")))
                                .put("macAddress", safe(row.getString("mac_address")))
                                .put("hostName", safe(row.getString("host_name")))
                                .put("status", safe(row.getString("status")))
                                .put("deviceType", safe(row.getString("device_type")))
                                .put("dnsStatus", safe(row.getString("dns_status")))
                                .put("lastAliveTime", "N/A")
                                .put("location", safe(row.getString("location")))
                                .put("description", safe(row.getString("system_description"))));
                    }

                    JsonObject payload = new JsonObject()
                            .put("data", ipList)
                            .put("subLabel", String.valueOf(subnetId));

                    LOGGER.info("Dispatching Subnet IP CSV export to ReportWorkerVerticle (subnetId={}, records={})", subnetId, ipList.size());

                    return vertx.eventBus().<JsonObject>request(ReportWorkerVerticle.ADDR_GENERATE_SUBNET_CSV, payload)
                            .map(reply -> {
                                JsonObject res = reply.body();
                                LOGGER.info("Subnet IP CSV exported successfully via ReportWorkerVerticle: {}", res.getString("filename"));
                                return new JsonObject().put("success", true).put("data", res.getString("filename"));
                            });
                })
                .recover(err -> {
                    LOGGER.error("Export Subnet IP CSV failed for subnetId={}: {}", subnetId, err.getMessage());
                    return Future.succeededFuture(new JsonObject().put("success", false).put("message", err.getMessage()));
                });
    }

    // ==========================================
    // 7. Export IPs to PDF (EventBus Report Worker)
    // ==========================================

    // Exports subnet IP details to a PDF file via the report worker.
    public Future<JsonObject> exportSubnetIPsToPDF(Long subnetId, List<String> selectedIds) {
        String sql = buildExportSQL(subnetId, selectedIds);

        return db.preparedQuery(sql).execute(Tuple.of(subnetId))
                .compose(rows -> {
                    JsonArray ipList = new JsonArray();
                    for (Row row : rows) {
                        ipList.add(new JsonObject()
                                .put("ipAddress", safe(row.getString("ip_address")))
                                .put("macAddress", safe(row.getString("mac_address")))
                                .put("hostName", safe(row.getString("host_name")))
                                .put("status", safe(row.getString("status")))
                                .put("deviceType", safe(row.getString("device_type")))
                                .put("dnsStatus", safe(row.getString("dns_status")))
                                .put("location", safe(row.getString("location"))));
                    }

                    JsonObject payload = new JsonObject()
                            .put("data", ipList)
                            .put("subLabel", String.valueOf(subnetId));

                    LOGGER.info("Dispatching Subnet IP PDF export to ReportWorkerVerticle (subnetId={}, records={})", subnetId, ipList.size());

                    return vertx.eventBus().<JsonObject>request(ReportWorkerVerticle.ADDR_GENERATE_SUBNET_PDF, payload)
                            .map(reply -> {
                                JsonObject res = reply.body();
                                LOGGER.info("Subnet IP PDF exported successfully via ReportWorkerVerticle: {}", res.getString("filename"));
                                return new JsonObject().put("success", true).put("data", res.getString("filename"));
                            });
                })
                .recover(err -> {
                    LOGGER.error("Export Subnet IP PDF failed for subnetId={}: {}", subnetId, err.getMessage());
                    return Future.succeededFuture(new JsonObject().put("success", false).put("message", err.getMessage()));
                });
    }

    // ==========================================
    // Export Rogue Detection to PDF (EventBus Report Worker)
    // ==========================================

    // Exports rogue detection events to a PDF report file.
    public Future<JsonObject> exportRogueDetectionToPDF(List<String> selectedIds) {
        String sql = buildRogueExportSQL(selectedIds);

        return db.query(sql).execute()
                .compose(rows -> {
                    JsonArray rogueList = new JsonArray();
                    for (Row row : rows) {
                        LocalDateTime discTs = row.getLocalDateTime("discovered_at");
                        String discTsStr = discTs != null ? discTs.format(DATE_FORMATTER) : "2026-09-02 10:00:00";
                        rogueList.add(new JsonObject()
                                .put("id", row.getLong("id"))
                                .put("macAddress", safe(row.getString("mac_address")))
                                .put("ipAddress", safe(row.getString("ip_address")))
                                .put("discoveredAt", discTsStr)
                                .put("nicType", safe(row.getString("nic_type") != null ? row.getString("nic_type") : "Virtual NIC"))
                                .put("authenticity", safe(row.getString("authenticity") != null ? row.getString("authenticity") : "UNAUTHORIZED"))
                                .put("hostName", safe(row.getString("host_name"))));
                    }

                    JsonObject payload = new JsonObject().put("data", rogueList);

                    LOGGER.info("Dispatching Rogue Detection PDF export to ReportWorkerVerticle (records={})", rogueList.size());

                    return vertx.eventBus().<JsonObject>request(ReportWorkerVerticle.ADDR_GENERATE_ROGUE_PDF, payload)
                            .map(reply -> {
                                JsonObject res = reply.body();
                                LOGGER.info("Rogue Detection PDF exported successfully via ReportWorkerVerticle: {}", res.getString("filename"));
                                return new JsonObject().put("success", true).put("data", res.getString("filename"));
                            });
                })
                .recover(err -> {
                    LOGGER.error("Export Rogue Detection PDF failed: {}", err.getMessage());
                    return Future.succeededFuture(new JsonObject().put("success", false).put("message", err.getMessage()));
                });
    }

    // ==========================================
    // Export Rogue Detection to CSV (EventBus Report Worker)
    // ==========================================

    // Exports rogue detection events to a CSV report file.
    public Future<JsonObject> exportRogueDetectionToCSV(List<String> selectedIds) {
        String sql = buildRogueExportSQL(selectedIds);

        return db.query(sql).execute()
                .compose(rows -> {
                    JsonArray rogueList = new JsonArray();
                    for (Row row : rows) {
                        LocalDateTime discTs = row.getLocalDateTime("discovered_at");
                        String discTsStr = discTs != null ? discTs.format(DATE_FORMATTER) : "2026-09-02 10:00:00";
                        rogueList.add(new JsonObject()
                                .put("id", row.getLong("id"))
                                .put("macAddress", safe(row.getString("mac_address")))
                                .put("ipAddress", safe(row.getString("ip_address")))
                                .put("discoveredAt", discTsStr)
                                .put("nicType", safe(row.getString("nic_type") != null ? row.getString("nic_type") : "Virtual NIC"))
                                .put("authenticity", safe(row.getString("authenticity") != null ? row.getString("authenticity") : "UNAUTHORIZED"))
                                .put("hostName", safe(row.getString("host_name"))));
                    }

                    JsonObject payload = new JsonObject().put("data", rogueList);

                    LOGGER.info("Dispatching Rogue Detection CSV export to ReportWorkerVerticle (records={})", rogueList.size());

                    return vertx.eventBus().<JsonObject>request(ReportWorkerVerticle.ADDR_GENERATE_ROGUE_CSV, payload)
                            .map(reply -> {
                                JsonObject res = reply.body();
                                LOGGER.info("Rogue Detection CSV exported successfully via ReportWorkerVerticle: {}", res.getString("filename"));
                                return new JsonObject().put("success", true).put("data", res.getString("filename"));
                            });
                })
                .recover(err -> {
                    LOGGER.error("Export Rogue Detection CSV failed: {}", err.getMessage());
                    return Future.succeededFuture(new JsonObject().put("success", false).put("message", err.getMessage()));
                });
    }

    // ==========================================
    // 8. Download Sample CSV Template
    // ==========================================

    // Generates and provides a sample CSV import template.
    public Future<JsonObject> getSampleCSVTemplate(Long subnetId) {
        String csv = "IP Address,MAC Address,Host Name,Status,Device Type,Description\n" +
                "192.168.10.10,00:11:22:33:44:55,server-01,USED,Server,Primary web server\n" +
                "192.168.10.11,00:11:22:33:44:56,server-02,AVAILABLE,,Spare server\n" +
                "192.168.10.12,00:11:22:33:44:57,printer-01,USED,Printer,Floor 2 printer\n";

        String filename = "SubnetIP_Sample_Template.csv";
        String filePath = EXPORT_DIR + filename;

        return vertx.fileSystem().writeFile(filePath, Buffer.buffer(csv.getBytes(StandardCharsets.UTF_8)))
                .map(v -> new JsonObject().put("success", true).put("data", filename))
                .recover(err -> Future.succeededFuture(new JsonObject().put("success", false).put("message", err.getMessage())));
    }

    // ==========================================
    // 9. Read exported file as bytes
    // ==========================================

    // Reads an exported file from disk into a buffer.
    public Future<Buffer> readExportedFile(String filename) {
        String filePath = EXPORT_DIR + filename;
        return vertx.fileSystem().readFile(filePath);
    }

    // ==========================================
    // Network Probing Actions via EventBus
    // ==========================================

    // Dispatches an ICMP ping request for an IP address over the EventBus.
    public Future<JsonObject> pingHost(String ip) {
        return vertx.eventBus().<JsonObject>request(NetworkWorkerVerticle.ADDR_PING, new JsonObject().put("ip", ip))
                .map(msg -> msg.body());
    }

    // Dispatches a DNS reverse lookup request for an IP address over the EventBus.
    public Future<JsonObject> lookupDns(String ip) {
        return vertx.eventBus().<JsonObject>request(NetworkWorkerVerticle.ADDR_DNS, new JsonObject().put("ip", ip))
                .map(msg -> msg.body());
    }

    // Dispatches a port scanning request for an IP address and ports over the EventBus.
    public Future<JsonObject> probePorts(String ip, JsonArray ports) {
        return vertx.eventBus().<JsonObject>request(NetworkWorkerVerticle.ADDR_PORTSCAN, new JsonObject().put("ip", ip).put("ports", ports))
                .map(msg -> msg.body());
    }

    // Dispatches a network traceroute request for an IP address over the EventBus.
    public Future<JsonObject> traceroute(String ip) {
        return vertx.eventBus().<JsonObject>request(NetworkWorkerVerticle.ADDR_TRACEROUTE, new JsonObject().put("ip", ip))
                .map(msg -> msg.body());
    }

    // ==========================================
    // Private Helpers
    // ==========================================

    // Builds the SQL query for exporting subnet IP records.
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
        return sql.append(" ORDER BY ip_address::inet ASC").toString();
    }

    // Builds the SQL query for exporting rogue detection records.
    private String buildRogueExportSQL(List<String> selectedIds) {
        StringBuilder sql = new StringBuilder(
                "SELECT id, mac_address, ip_address, discovered_at, nic_type, authenticity, host_name " +
                        "FROM rogue_detection_details");
        if (selectedIds != null && !selectedIds.isEmpty()) {
            List<String> ids = selectedIds.stream()
                    .map(String::trim)
                    .filter(id -> id.matches("\\d+"))
                    .collect(Collectors.toList());
            if (!ids.isEmpty()) {
                sql.append(" WHERE id IN (").append(String.join(",", ids)).append(")");
            }
        }
        return sql.append(" ORDER BY id ASC").toString();
    }

    // Generates a list of IP addresses between start and end IPs.
    private List<String> generateIPRange(String startIp, String endIp) {
        List<String> ips = new ArrayList<>();
        try {
            long start = ipToLong(startIp);
            long end = ipToLong(endIp);
            if (start > end) {
                long tmp = start;
                start = end;
                end = tmp;
            }
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

    // Converts an IPv4 dotted string into a 32-bit long integer.
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

    // Converts a 32-bit long integer into an IPv4 dotted string.
    private String longToIp(long ip) {
        return ((ip >> 24) & 0xFF) + "." + ((ip >> 16) & 0xFF) + "." + ((ip >> 8) & 0xFF) + "." + (ip & 0xFF);
    }

    // Sanitizes and quotes a string value for CSV export safety.
    private String safe(String val) {
        if (val == null || val.isEmpty()) return "-";
        return "\"" + val.replace("\"", "\"\"") + "\"";
    }

    // Recalculates and updates IP usage statistics for a subnet.
    private Future<Void> refreshSubnetStats(Long subnetId) {
        String updateSql = "UPDATE subnet_details SET " +
                "used_ip = (SELECT count(*) FROM subnet_ip_details WHERE subnet_id = $1 AND status = 'USED'), " +
                "available_ip = (SELECT count(*) FROM subnet_ip_details WHERE subnet_id = $1 AND status = 'AVAILABLE'), " +
                "transient_ip = (SELECT count(*) FROM subnet_ip_details WHERE subnet_id = $1 AND UPPER(status) = 'TRANSIENT'), " +
                "last_scan_time = CURRENT_TIMESTAMP WHERE id = $1";
        return db.preparedQuery(updateSql).execute(Tuple.of(subnetId)).mapEmpty();
    }
}
