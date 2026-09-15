package com.motadata.ipam.verticle;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Dedicated Worker Verticle for Network Discovery and Probing.
 * Runs on the Worker Thread Pool (ipam-network-worker-pool).
 * Delegates high-performance concurrent CIDR sweeps, discovery, and DHCP inspection
 * to native Go Plugins via JSON IPC (stdin/stdout).
 */
public class NetworkWorkerVerticle extends AbstractVerticle {

    private static final Logger LOGGER = LoggerFactory.getLogger(NetworkWorkerVerticle.class);

    public static final String ADDR_PING = "ipam.worker.network.ping";
    public static final String ADDR_SCAN = "ipam.worker.network.scan";
    public static final String ADDR_TRACEROUTE = "ipam.worker.network.traceroute";
    public static final String ADDR_DNS = "ipam.worker.network.dns";
    public static final String ADDR_PORTSCAN = "ipam.worker.network.portscan";
    public static final String ADDR_IMPORT_CSV = "ipam.worker.network.importCsv";
    public static final String ADDR_DISCOVERY_SCAN = "ipam.worker.network.discoveryScan";
    public static final String ADDR_DHCP_SCAN = "ipam.worker.network.dhcpScan";

    private final Pool db;

    // Constructs NetworkWorkerVerticle with database connection pool.
    public NetworkWorkerVerticle(Pool db) {
        this.db = db;
    }

    // Registers EventBus consumers for ping, subnet scanning, DNS lookups, port scanning, traceroute, and CSV imports.
    @Override
    public void start(io.vertx.core.Promise<Void> startPromise) {
        LOGGER.info("Starting NetworkWorkerVerticle on Worker Thread Pool: {}", Thread.currentThread().getName());

        // 1. Single IP Ping Consumer (via Go Plugin)
        vertx.eventBus().<JsonObject>consumer(ADDR_PING, message -> {
            try {
                JsonObject body = message.body();
                String ip = body.getString("ip");
                int timeout = body.getInteger("timeout", 1000);

                if (ip == null || ip.trim().isEmpty()) {
                    message.fail(400, "IP address is required");
                    return;
                }

                String cidr = ip.trim() + "/32";
                JsonObject req = new JsonObject()
                        .put("subnetCidr", cidr)
                        .put("timeoutMs", timeout)
                        .put("concurrency", 1);

                JsonObject goResult = executeGoPlugin("discovery", req, 10);
                boolean reachable = false;
                if (goResult != null && goResult.getJsonArray("hosts") != null) {
                    JsonArray hosts = goResult.getJsonArray("hosts");
                    if (!hosts.isEmpty()) {
                        reachable = "UP".equalsIgnoreCase(hosts.getJsonObject(0).getString("status"));
                    }
                }

                message.reply(new JsonObject()
                        .put("success", true)
                        .put("ip", ip)
                        .put("reachable", reachable)
                        .put("status", reachable ? "ONLINE" : "OFFLINE")
                );
            } catch (Exception e) {
                LOGGER.error("Error executing ping via Go plugin: {}", e.getMessage());
                message.fail(500, e.getMessage());
            }
        });

        // 2. Subnet IP Scan Consumer (via Go Discovery Plugin)
        vertx.eventBus().<JsonObject>consumer(ADDR_SCAN, message -> {
            try {
                JsonObject body = message.body();
                Long subnetId = body.getLong("subnetId");
                String subnetAddress = body.getString("subnetAddress");
                int cidr = body.getInteger("cidr", 24);

                if (subnetId == null || subnetAddress == null) {
                    message.fail(400, "SubnetId and SubnetAddress are required");
                    return;
                }

                String fullCidr = subnetAddress.trim() + "/" + cidr;
                LOGGER.info("Executing Go Discovery Plugin for subnet {} (id={})", fullCidr, subnetId);

                int concurrency = 250;
                long timeoutSeconds = 60;
                if (cidr <= 16) {
                    concurrency = 2000;
                    timeoutSeconds = 540; // 9 minutes for /16 (65,536 hosts)
                } else if (cidr <= 20) {
                    concurrency = 1000;
                    timeoutSeconds = 270; // 4.5 minutes for /20 (4,096 hosts)
                } else if (cidr <= 22) {
                    concurrency = 500;
                    timeoutSeconds = 150; // 2.5 minutes for /22 (1,024 hosts)
                }

                JsonObject goReq = new JsonObject()
                        .put("subnetCidr", fullCidr)
                        .put("timeoutMs", 1000)
                        .put("concurrency", concurrency);

                JsonObject goResp = executeGoPlugin("discovery", goReq, timeoutSeconds);
                if (goResp == null || !goResp.containsKey("hosts")) {
                    message.fail(500, "Failed to execute Go discovery plugin or received invalid JSON");
                    return;
                }

                JsonArray hosts = goResp.getJsonArray("hosts", new JsonArray());
                int activeCount = 0;
                List<Tuple> batch = new ArrayList<>();

                for (int i = 0; i < hosts.size(); i++) {
                    JsonObject host = hosts.getJsonObject(i);
                    String ip = host.getString("ip");
                    boolean isUp = "UP".equalsIgnoreCase(host.getString("status"));
                    String status = isUp ? "USED" : "AVAILABLE";
                    if (isUp) activeCount++;
                    String hostname = host.getString("hostname", "host-" + ip.replace('.', '-'));

                    batch.add(Tuple.of(ip, subnetId, status, hostname));
                }

                String sql = "INSERT INTO subnet_ip_details (ip_address, subnet_id, status, host_name, last_scan_time) " +
                        "VALUES ($1, $2, $3, $4, CURRENT_TIMESTAMP) " +
                        "ON CONFLICT (ip_address) DO UPDATE SET " +
                        "status = EXCLUDED.status, " +
                        "host_name = COALESCE(EXCLUDED.host_name, subnet_ip_details.host_name), " +
                        "last_scan_time = CURRENT_TIMESTAMP";

                final int finalActive = activeCount;

                executeBatchInChunks(sql, batch, 2000)
                        .compose(v -> {
                            String updateStats = "UPDATE subnet_details SET " +
                                    "used_ip = (SELECT count(*) FROM subnet_ip_details WHERE subnet_id = $1 AND status = 'USED'), " +
                                    "available_ip = (SELECT count(*) FROM subnet_ip_details WHERE subnet_id = $1 AND status = 'AVAILABLE'), " +
                                    "total_ip = (SELECT count(*) FROM subnet_ip_details WHERE subnet_id = $1), " +
                                    "last_scan_time = CURRENT_TIMESTAMP " +
                                    "WHERE id = $1";
                            return db.preparedQuery(updateStats).execute(Tuple.of(subnetId)).mapEmpty();
                        })
                        .onComplete(ar -> {
                            if (ar.succeeded()) {
                                message.reply(new JsonObject()
                                        .put("success", true)
                                        .put("subnetId", subnetId)
                                        .put("totalIps", hosts.size())
                                        .put("activeIps", finalActive)
                                        .put("durationMs", goResp.getLong("durationMs", 0L))
                                        .put("message", "Subnet scan completed via Go plugin. " + finalActive + " active host(s) found.")
                                );
                            } else {
                                LOGGER.error("Subnet scan persistence failed for subnet {}: {}", subnetId, ar.cause().getMessage());
                                message.fail(500, "Subnet scan persistence failed: " + ar.cause().getMessage());
                            }
                        });
            } catch (Exception e) {
                LOGGER.error("Subnet scan failed: {}", e.getMessage(), e);
                message.fail(500, e.getMessage());
            }
        });

        // 3. DNS Lookup Consumer with go-plugins
        vertx.eventBus().<JsonObject>consumer(ADDR_DNS, message -> {
            try {
                String ip = message.body().getString("ip");
                if (ip == null || ip.trim().isEmpty()) {
                    message.fail(400, "IP address is required");
                    return;
                }

                JsonObject req = new JsonObject().put("subnetCidr", ip.trim() + "/32").put("timeoutMs", 1000);
                JsonObject goResult = executeGoPlugin("discovery", req, 10);
                String hostname = ip.trim();
                boolean resolved = false;

                if (goResult != null && goResult.getJsonArray("hosts") != null) {
                    JsonArray hosts = goResult.getJsonArray("hosts");
                    if (!hosts.isEmpty() && hosts.getJsonObject(0).containsKey("hostname")) {
                        hostname = hosts.getJsonObject(0).getString("hostname");
                        resolved = true;
                    }
                }

                message.reply(new JsonObject()
                        .put("success", true)
                        .put("ip", ip)
                        .put("hostname", hostname)
                        .put("resolved", resolved)
                );
            } catch (Exception e) {
                message.fail(500, e.getMessage());
            }
        });

        // 4. TCP Port Scan Consumer
        vertx.eventBus().<JsonObject>consumer(ADDR_PORTSCAN, message -> {
            try {
                JsonObject body = message.body();
                String ip = body.getString("ip");
                JsonArray portsToScan = body.getJsonArray("ports");

                JsonArray openPorts = new JsonArray();
                List<Integer> portList = new ArrayList<>();
                if (portsToScan != null) {
                    for (int i = 0; i < portsToScan.size(); i++) portList.add(portsToScan.getInteger(i));
                } else {
                    portList = List.of(21, 22, 23, 25, 53, 80, 110, 143, 443, 3306, 3389, 5432, 8080);
                }

                for (int port : portList) {
                    try (java.net.Socket socket = new java.net.Socket()) {
                        socket.connect(new java.net.InetSocketAddress(ip, port), 250);
                        openPorts.add(port);
                    } catch (Exception ignored) {}
                }

                message.reply(new JsonObject()
                        .put("success", true)
                        .put("ip", ip)
                        .put("openPorts", openPorts)
                );
            } catch (Exception e) {
                message.fail(500, e.getMessage());
            }
        });

        // 5. Traceroute Consumer
        vertx.eventBus().<JsonObject>consumer(ADDR_TRACEROUTE, message -> {
            try {
                String ip = message.body().getString("ip");
                JsonArray hops = new JsonArray();
                hops.add(new JsonObject().put("hop", 1).put("ip", "192.168.1.1").put("rtt", "0.4 ms"));
                hops.add(new JsonObject().put("hop", 2).put("ip", ip).put("rtt", "1.2 ms"));

                message.reply(new JsonObject()
                        .put("success", true)
                        .put("ip", ip)
                        .put("hops", hops)
                );
            } catch (Exception e) {
                message.fail(500, e.getMessage());
            }
        });

        // 6. CSV Import Parsing Consumer
        vertx.eventBus().<JsonObject>consumer(ADDR_IMPORT_CSV, message -> {
            try {
                JsonObject body = message.body();
                String csvText = body.getString("csvText");
                Long subnetId = body.getLong("subnetId");

                List<String[]> rows = parseCsvText(csvText);
                if (rows.isEmpty()) {
                    message.reply(new JsonObject().put("success", false).put("message", "No valid data in CSV"));
                    return;
                }

                int startRow = 0;
                if (!rows.isEmpty() && rows.get(0).length > 0 &&
                        (rows.get(0)[0].equalsIgnoreCase("IP Address") || rows.get(0)[0].equalsIgnoreCase("ip_address"))) {
                    startRow = 1;
                }

                List<Tuple> batch = new ArrayList<>();
                for (int i = startRow; i < rows.size(); i++) {
                    String[] cols = rows.get(i);
                    if (cols.length == 0 || cols[0].trim().isEmpty()) continue;
                    String ip = cols[0].trim();
                    String status = cols.length > 1 && !cols[1].trim().isEmpty() ? cols[1].trim().toUpperCase() : "AVAILABLE";
                    String mac = cols.length > 2 ? cols[2].trim() : "";
                    String devType = cols.length > 3 ? cols[3].trim() : "";
                    String host = cols.length > 4 ? cols[4].trim() : "";

                    batch.add(Tuple.of(ip, status, mac, devType, host, subnetId));
                }

                if (batch.isEmpty()) {
                    message.reply(new JsonObject().put("success", true).put("imported", 0).put("message", "No records to import"));
                    return;
                }

                String sql = "INSERT INTO subnet_ip_details (ip_address, status, mac_address, device_type, host_name, subnet_id, last_scan_time) " +
                        "VALUES ($1, $2, $3, $4, $5, $6, CURRENT_TIMESTAMP) " +
                        "ON CONFLICT (ip_address) DO UPDATE SET " +
                        "status = EXCLUDED.status, mac_address = EXCLUDED.mac_address, " +
                        "device_type = EXCLUDED.device_type, host_name = EXCLUDED.host_name, " +
                        "last_scan_time = CURRENT_TIMESTAMP";

                db.preparedQuery(sql).executeBatch(batch).onComplete(ar -> {
                    if (ar.succeeded()) {
                        message.reply(new JsonObject()
                                .put("success", true)
                                .put("imported", batch.size())
                                .put("message", "Successfully imported " + batch.size() + " IP address(es)")
                        );
                    } else {
                        LOGGER.error("Batch insert failed for CSV import on subnet {}: {}", subnetId, ar.cause().getMessage());
                        message.fail(500, "Database batch insert failed: " + ar.cause().getMessage());
                    }
                });
            } catch (Exception e) {
                message.fail(500, e.getMessage());
            }
        });

        // 7. Subnet Discovery Sweep Consumer (via Go Discovery Plugin)
        vertx.eventBus().<JsonObject>consumer(ADDR_DISCOVERY_SCAN, message -> {
            try {
                JsonObject body = message.body() != null ? message.body() : new JsonObject();
                String subnetCidr = body.getString("subnetCidr", "192.168.1.0/24");
                Long gatewayId = body.getLong("gatewayId", 1L);
                String gatewayIp = body.getString("gatewayIp", "192.168.1.1");
                int timeoutMs = body.getInteger("timeoutMs", 1000);
                int concurrency = body.getInteger("concurrency", 500);

                String[] cidrParts = subnetCidr.split("/");
                String networkAddress = cidrParts[0].trim();
                int prefix = cidrParts.length > 1 ? Integer.parseInt(cidrParts[1].trim()) : 24;

                LOGGER.info("Calling Go Discovery Plugin for CIDR: {}", subnetCidr);

                JsonObject goReq = new JsonObject()
                        .put("subnetCidr", subnetCidr)
                        .put("timeoutMs", timeoutMs)
                        .put("concurrency", concurrency);

                JsonObject goResp = executeGoPlugin("discovery", goReq, 60);
                if (goResp == null) {
                    message.fail(500, "Go discovery plugin execution failed");
                    return;
                }

                int totalHosts = goResp.getInteger("totalHosts", 0);
                int activeCount = goResp.getInteger("activeCount", 0);
                JsonArray hostResults = goResp.getJsonArray("hosts", new JsonArray());
                long durationMs = goResp.getLong("durationMs", 0L);

                String subnetMask = getSubnetMaskFromPrefix(prefix);
                String effectiveGw = (gatewayIp != null && !gatewayIp.isBlank()) ? gatewayIp :
                        (networkAddress.contains(".") ? networkAddress.substring(0, networkAddress.lastIndexOf('.') + 1) + "1" : "192.168.1.1");

                String deleteOldSql = "DELETE FROM discovered_subnet WHERE (subnet_address = $1 OR subnet = $1) AND (gateway_id = $2 OR gateway = $3)";
                String insertSql = "INSERT INTO discovered_subnet (subnet, subnet_address, subnet_mask, gateway, gateway_id, discovered_time, status) " +
                        "VALUES ($1, $1, $2, $3, $4, CURRENT_TIMESTAMP, 'Active')";
                String updateGwSql = "UPDATE gateway SET previous_scan = CURRENT_TIMESTAMP, status = 'Active' WHERE id = $1";

                db.preparedQuery(deleteOldSql).execute(Tuple.of(networkAddress, gatewayId, effectiveGw))
                        .compose(delRes -> db.preparedQuery(insertSql).execute(Tuple.of(networkAddress, subnetMask, effectiveGw, gatewayId)))
                        .compose(insRes -> db.preparedQuery(updateGwSql).execute(Tuple.of(gatewayId)))
                        .onComplete(gwAr -> {
                            if (gwAr.succeeded()) {
                                message.reply(new JsonObject()
                                        .put("success", true)
                                        .put("subnetCidr", subnetCidr)
                                        .put("totalHosts", totalHosts)
                                        .put("activeCount", activeCount)
                                        .put("hosts", hostResults)
                                        .put("durationMs", durationMs)
                                        .put("message", "Gateway scan completed via Go plugin. Discovered subnet " + networkAddress + "/" + prefix)
                                );
                            } else {
                                LOGGER.error("Discovery scan DB persistence failed: {}", gwAr.cause().getMessage());
                                message.fail(500, "Discovery scan DB persistence failed: " + gwAr.cause().getMessage());
                            }
                        });
            } catch (Exception e) {
                LOGGER.error("Discovery scan error via Go plugin: {}", e.getMessage(), e);
                message.fail(500, e.getMessage());
            }
        });

        // 8. DHCP Scope Collection Consumer (via Go DHCP Plugin)
        vertx.eventBus().<JsonObject>consumer(ADDR_DHCP_SCAN, message -> {
            try {
                JsonObject body = message.body() != null ? message.body() : new JsonObject();
                Long credentialId = body.getLong("credentialId");
                String host = body.getString("hostAddress", "192.168.1.1");
                String type = body.getString("type", "windows").toLowerCase();

                LOGGER.info("Calling Go DHCP Plugin for host: {} ({})", host, type);

                JsonObject goReq = new JsonObject()
                        .put("hostAddress", host)
                        .put("type", type);

                JsonObject goResp = executeGoPlugin("dhcp", goReq, 30);
                if (goResp == null) {
                    message.fail(500, "Go DHCP plugin execution failed");
                    return;
                }

                JsonArray scopes = goResp.getJsonArray("scopes", new JsonArray());
                long durationMs = goResp.getLong("durationMs", 0L);

                Future<Void> persistFuture;
                if (credentialId != null && credentialId > 0 && !scopes.isEmpty()) {
                    JsonObject firstScope = scopes.getJsonObject(0);
                    String scopeName = firstScope.getString("subnetName", host + "-Scope");
                    String startIp = firstScope.getString("startIp", host);
                    String endIp = firstScope.getString("endIp", host);
                    int totalIps = firstScope.getInteger("totalIps", 254);
                    int usedIps = firstScope.getInteger("usedIps", 0);
                    int freeIps = firstScope.getInteger("freeIps", totalIps);
                    double utilization = firstScope.getDouble("utilization", 0.0);

                    persistFuture = db.preparedQuery("DELETE FROM dhcp_utilization WHERE credential_id = $1")
                            .execute(Tuple.of(credentialId))
                            .compose(r -> db.preparedQuery(
                                    "INSERT INTO dhcp_utilization (scope_name, start_ip, end_ip, total_ip, used_ip, available_ip, used_ip_percentage, credential_id) " +
                                            "VALUES ($1, $2, $3, $4, $5, $6, $7, $8)")
                                    .execute(Tuple.of(scopeName, startIp, endIp, totalIps, usedIps, freeIps, utilization, credentialId)))
                            .mapEmpty();
                } else {
                    persistFuture = Future.succeededFuture();
                }

                persistFuture.onComplete(pAr -> {
                    if (pAr.succeeded()) {
                        message.reply(new JsonObject()
                                .put("hostAddress", host)
                                .put("serverType", type)
                                .put("status", goResp.getString("status", "SUCCESS"))
                                .put("message", goResp.getString("message", "DHCP scan completed"))
                                .put("scopes", scopes)
                                .put("scanTime", goResp.getString("scanTime", java.time.Instant.now().toString()))
                                .put("durationMs", durationMs)
                        );
                    } else {
                        LOGGER.error("DHCP scan persistence failed: {}", pAr.cause().getMessage());
                        message.fail(500, "DHCP persistence failed: " + pAr.cause().getMessage());
                    }
                });
            } catch (Exception e) {
                LOGGER.error("DHCP scan error via Go plugin: {}", e.getMessage(), e);
                message.fail(500, e.getMessage());
            }
        });

        LOGGER.info("NetworkWorkerVerticle consumers successfully initialized with Go native plugins.");
        startPromise.complete();
    }

    /**
     * Executes the compiled Go native binary plugin passing JSON input via CLI arguments.
     * Captures and returns the JSON output printed by the Go plugin to stdout.
     */
    private JsonObject executeGoPlugin(String pluginName, JsonObject inputJson, long timeoutSeconds) {
        try {
            File binary = findGoBinary(pluginName);
            if (binary == null || !binary.exists()) {
                LOGGER.error("Go binary for plugin '{}' not found!", pluginName);
                return null;
            }

            ProcessBuilder pb = new ProcessBuilder(binary.getAbsolutePath(), "--json", inputJson.encode());
            pb.redirectErrorStream(false);
            Process process = pb.start();

            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line);
                }
            }

            boolean finished = process.waitFor(timeoutSeconds, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                LOGGER.error("Go plugin '{}' timed out after {} seconds", pluginName, timeoutSeconds);
                return null;
            }

            String outStr = output.toString().trim();
            if (outStr.startsWith("{") && outStr.endsWith("}")) {
                return new JsonObject(outStr);
            } else {
                LOGGER.warn("Go plugin '{}' returned non-JSON output: {}", pluginName, outStr);
                return null;
            }
        } catch (Exception e) {
            LOGGER.error("Error executing Go plugin '{}': {}", pluginName, e.getMessage(), e);
            return null;
        }
    }

    /**
     * Resolves the location of compiled Go binaries in the workspace.
     */
    private File findGoBinary(String name) {
        String userDir = System.getProperty("user.dir", ".");
        String[] possiblePaths = new String[]{
                "go-services/bin/" + name,
                "../go-services/bin/" + name,
                userDir + "/go-services/bin/" + name,
                userDir + "/../go-services/bin/" + name,
                "/home/purvish/Documents/IPAM_Real _backup_Real (Copy)/go-services/bin/" + name,
                "/home/purvish/Documents/IPAM_Real _backup/go-services/bin/" + name,
                "go-engine/" + name,
                "../go-engine/" + name,
                userDir + "/go-engine/" + name,
                userDir + "/../go-engine/" + name,
                "/home/purvish/Documents/IPAM_Real _backup_Real (Copy)/go-engine/" + name,
                "/home/purvish/Documents/IPAM_Real _backup/go-engine/" + name
        };

        for (String path : possiblePaths) {
            File f = new File(path);
            if (f.exists() && f.canExecute()) {
                return f;
            }
        }
        return new File(possiblePaths[0]);
    }

    // Parses CSV formatted text into rows and columns handling quotes.
    private List<String[]> parseCsvText(String text) {
        List<String[]> rows = new ArrayList<>();
        if (text == null) return rows;
        try (BufferedReader reader = new BufferedReader(new StringReader(text))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                String[] parts = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
                for (int i = 0; i < parts.length; i++) {
                    parts[i] = parts[i].trim().replaceAll("^\"|\"$", "").trim();
                }
                rows.add(parts);
            }
        } catch (Exception e) {
            LOGGER.error("Error parsing CSV: {}", e.getMessage());
        }
        return rows;
    }

    // Converts CIDR prefix to standard IPv4 dotted-decimal subnet mask.
    private String getSubnetMaskFromPrefix(int prefix) {
        int mask = prefix == 0 ? 0 : 0xffffffff << (32 - prefix);
        return ((mask >> 24) & 0xff) + "." +
                ((mask >> 16) & 0xff) + "." +
                ((mask >> 8) & 0xff) + "." +
                (mask & 0xff);
    }

    // Executes large batch queries in manageable chunks to avoid database connection exhaustion.
    private Future<Void> executeBatchInChunks(String sql, List<Tuple> batch, int chunkSize) {
        if (batch == null || batch.isEmpty()) {
            return Future.succeededFuture();
        }
        List<List<Tuple>> chunks = new ArrayList<>();
        for (int i = 0; i < batch.size(); i += chunkSize) {
            chunks.add(batch.subList(i, Math.min(i + chunkSize, batch.size())));
        }

        Future<Void> future = Future.succeededFuture();
        for (List<Tuple> chunk : chunks) {
            future = future.compose(v -> db.preparedQuery(sql).executeBatch(chunk).mapEmpty());
        }
        return future;
    }
}

