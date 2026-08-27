package com.motadata.ipam.service;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

/**
 * Asynchronous Vert.x service delegating Subnet Auto-Discovery to Golang Microservices.
 */
public class DiscoveryService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DiscoveryService.class);

    private final WebClient webClient;
    private final String goDiscoveryHost;
    private final int goDiscoveryPort;

    public DiscoveryService(Vertx vertx) {
        this(vertx, "localhost", 8081);
    }

    public DiscoveryService(Vertx vertx, String goDiscoveryHost, int goDiscoveryPort) {
        this.webClient = WebClient.create(vertx);
        this.goDiscoveryHost = goDiscoveryHost;
        this.goDiscoveryPort = goDiscoveryPort;
    }

    public Future<JsonObject> triggerGoSubnetScan(String subnetCidr) {
        Promise<JsonObject> promise = Promise.promise();

        JsonObject payload = new JsonObject()
                .put("subnetCidr", subnetCidr)
                .put("timeoutMs", 1000)
                .put("concurrency", 50);

        LOGGER.info("Dispatching subnet discovery scan to Golang microservice at http://{}:{}/api/v1/scan/subnet", goDiscoveryHost, goDiscoveryPort);

        webClient.post(goDiscoveryPort, goDiscoveryHost, "/api/v1/scan/subnet")
                .sendJsonObject(payload, ar -> {
                    if (ar.succeeded()) {
                        LOGGER.info("Golang discovery microservice responded with status: {}", ar.result().statusCode());
                        promise.complete(ar.result().bodyAsJsonObject());
                    } else {
                        LOGGER.warn("Failed to reach Golang discovery microservice: {}", ar.cause().getMessage());
                        // Fallback response if microservice is offline
                        promise.complete(new JsonObject()
                                .put("status", "DEGRADED")
                                .put("subnetCidr", subnetCidr)
                                .put("message", "Microservice offline fallback"));
                    }
                });

        return promise.future();
    }

    public Future<JsonObject> autoDiscoverLocalSubnet() {
        return triggerGoSubnetScan("192.168.1.0/24");
    }
}
