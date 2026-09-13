package com.motadata.ipam.config;

import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.io.File;

/**
 * Asynchronous Vert.x configuration loader for ipm-conf.yml
 */
public class AppConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(AppConfig.class);

    private final JsonObject config;

    // Constructs AppConfig with the provided JsonObject configuration.
    public AppConfig(JsonObject config) {
        this.config = config != null ? config : new JsonObject();
    }

    /**
     * Loads the ipm-conf.yml configuration asynchronously using Vert.x ConfigRetriever.
     */
    // Loads the application configuration asynchronously from the YAML file or defaults.
    public static Future<AppConfig> load(Vertx vertx) {
        Promise<AppConfig> promise = Promise.promise();

        String configPath = findConfigFilePath();
        LOGGER.info("Loading configuration from path: {}", configPath);

        ConfigStoreOptions yamlStore = new ConfigStoreOptions()
                .setType("file")
                .setFormat("yaml")
                .setConfig(new JsonObject().put("path", configPath));

        ConfigRetrieverOptions options = new ConfigRetrieverOptions().addStore(yamlStore);
        ConfigRetriever retriever = ConfigRetriever.create(vertx, options);

        retriever.getConfig().onComplete(ar -> {
            if (ar.succeeded()) {
                JsonObject json = ar.result();
                LOGGER.info("Configuration loaded successfully: {}", json.encodePrettily());
                promise.complete(new AppConfig(json));
            } else {
                LOGGER.warn("Failed to load configuration from {}, using defaults: {}", configPath, ar.cause().getMessage());
                promise.complete(new AppConfig(createDefaultConfig()));
            }
        });


        return promise.future();
    }

    // Locates the path to the YAML configuration file.
    private static String findConfigFilePath() {
        String[] possiblePaths = new String[]{
                "config/ipm-conf.yml",
                "../config/ipm-conf.yml",
                "/home/purvish/Documents/IPAM_Real/config/ipm-conf.yml"
        };
        for (String path : possiblePaths) {
            if (new File(path).exists()) {
                return path;
            }
        }
        return "config/ipm-conf.yml";
    }

    // Creates the fallback default configuration JsonObject.
    private static JsonObject createDefaultConfig() {
        return new JsonObject()
                .put("server-port", 8080)
                .put("server-host", "localhost")
                .put("db-host", "127.0.0.1")
                .put("db-port", 5432)
                .put("db-name", "ipam_db")
                .put("db-user", "postgres")
                .put("db-password", "password")
                .put("max-ping-check-timeout", 10)
                .put("max-ping-check-retry-count", 2)
                .put("max-concurrent-ping", 500)
                .put("process-request-timeout", 1200);
    }

    // Returns the configured HTTP server port.
    public int getServerPort() {
        return config.getInteger("server-port", 8080);
    }

    // Returns the configured HTTP server host.
    public String getServerHost() {
        return config.getString("server-host", "localhost");
    }

    // Returns the configured PostgreSQL database host.
    public String getDbHost() {
        return config.getString("db-host", "localhost");
    }

    // Returns the configured PostgreSQL database port.
    public int getDbPort() {
        return config.getInteger("db-port", 5432);
    }

    // Returns the configured PostgreSQL database name.
    public String getDbName() {
        return config.getString("db-name", "ipam_db");
    }

    // Returns the configured PostgreSQL database username.
    public String getDbUser() {
        return config.getString("db-user", "postgres");
    }

    // Returns the configured PostgreSQL database password.
    public String getDbPassword() {
        return config.getString("db-password", "password");
    }

    // Returns the maximum ping timeout in seconds.
    public int getMaxPingTimeout() {
        return config.getInteger("max-ping-check-timeout", 10);
    }

    // Returns the maximum ping retry count.
    public int getMaxPingRetryCount() {
        return config.getInteger("max-ping-check-retry-count", 2);
    }

    // Returns the maximum number of concurrent ping requests.
    public int getMaxConcurrentPing() {
        return config.getInteger("max-concurrent-ping", 500);
    }

    // Returns the process request timeout in seconds.
    public int getProcessRequestTimeout() {
        return config.getInteger("process-request-timeout", 1200);
    }

    // Returns the underlying JsonObject configuration.
    public JsonObject getJsonObject() {
        return config;
    }
}
