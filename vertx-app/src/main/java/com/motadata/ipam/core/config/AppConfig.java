package com.motadata.ipam.core.config;

import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.Future;
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
        return load(vertx, null);
    }

    // Loads configuration and merges runtime overrides (e.g. from verticle deployment options).
    public static Future<AppConfig> load(Vertx vertx, JsonObject overrides) {
        String configPath = findConfigFilePath();
        LOGGER.info("Loading configuration from path: {}", configPath);

        ConfigStoreOptions yamlStore = new ConfigStoreOptions()
                .setType("file")
                .setFormat("yaml")
                .setConfig(new JsonObject().put("path", configPath));

        ConfigRetrieverOptions options = new ConfigRetrieverOptions().addStore(yamlStore);
        ConfigRetriever retriever = ConfigRetriever.create(vertx, options);

        return retriever.getConfig()
                .map(json -> {
                    if (overrides != null && !overrides.isEmpty()) {
                        json.mergeIn(overrides, true);
                    }
                    LOGGER.info("Configuration loaded successfully: {}", json.encodePrettily());
                    return new AppConfig(json);
                })
                .recover(err -> {
                    LOGGER.warn("Failed to load configuration from {}, using defaults: {}", configPath, err.getMessage());
                    JsonObject defaultConfig = createDefaultConfig();
                    if (overrides != null && !overrides.isEmpty()) {
                        defaultConfig.mergeIn(overrides, true);
                    }
                    return Future.succeededFuture(new AppConfig(defaultConfig));
                });
    }

    // Locates the path to the YAML configuration file using dynamic discovery.
    private static String findConfigFilePath() {
        String customPath = System.getProperty("ipam.config", System.getenv("IPAM_CONFIG_PATH"));
        if (customPath != null && new File(customPath).exists()) {
            return customPath;
        }

        String userDir = System.getProperty("user.dir", ".");
        String[] possiblePaths = new String[]{
                "config/ipm-conf.yml",
                "../config/ipm-conf.yml",
                userDir + "/config/ipm-conf.yml",
                userDir + "/../config/ipm-conf.yml"
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

    // Returns the database host.
    public String getDbHost() {
        return config.getString("db-host", "127.0.0.1");
    }

    // Returns the database port.
    public int getDbPort() {
        return config.getInteger("db-port", 5432);
    }

    // Returns the database name.
    public String getDbName() {
        return config.getString("db-name", "ipam_db");
    }

    // Returns the database username.
    public String getDbUser() {
        return config.getString("db-user", "postgres");
    }

    // Returns the database password.
    public String getDbPassword() {
        return config.getString("db-password", "password");
    }

    // Returns the database pool maximum size.
    public int getDbPoolMaxSize() {
        return config.getInteger("db-pool-max-size", 60);
    }

    // Returns the ping check timeout in milliseconds.
    public int getMaxPingCheckTimeout() {
        return config.getInteger("max-ping-check-timeout", 10) * 100; // default 1000ms
    }

    // Returns the ping retry count.
    public int getMaxPingCheckRetryCount() {
        return config.getInteger("max-ping-check-retry-count", 2);
    }

    // Returns the max concurrent ping workers.
    public int getMaxConcurrentPing() {
        return config.getInteger("max-concurrent-ping", 500);
    }

    // Returns the process request timeout in seconds.
    public int getProcessRequestTimeout() {
        return config.getInteger("process-request-timeout", 1200);
    }

    // Returns the JWT secret key.
    public String getJwtSecret() {
        return config.getString("jwt-secret", "motadata-ipam-secure-jwt-secret-key-2026-production-super-strong-token");
    }

    // Returns the JWT token validity duration in milliseconds.
    public long getJwtExpiryMs() {
        return config.getLong("jwt-expiry-ms", 86400000L); // 24 hours default
    }

    // Returns the raw configuration JSON object.
    public JsonObject toJson() {
        return config.copy();
    }
}
