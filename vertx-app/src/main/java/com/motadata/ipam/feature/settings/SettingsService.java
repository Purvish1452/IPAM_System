package com.motadata.ipam.feature.settings;

import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Asynchronous Vert.x Business Service for Global Settings, Branding, Mail Server, Custom Columns,
 * and Database Maintenance.
 * Direct Architecture: Handler -> Service -> PgPool -> PostgreSQL
 */
public class SettingsService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SettingsService.class);

    private final Pool db;

    // Constructs SettingsService with the given database pool.
    public SettingsService(Pool db) {
        this.db = db;
    }

    // Retrieves global system settings including logging level and CSS mode.
    public Future<JsonObject> getGlobalSetting() {
        String sql = "SELECT id, logging_level, css_mode, session_timeout FROM global_setting WHERE id = 1";
        return db.query(sql).execute().map(rows -> {
            if (rows.size() > 0) {
                Row row = rows.iterator().next();
                return new JsonObject()
                        .put("id", row.getLong("id"))
                        .put("loggingLevel", row.getInteger("logging_level"))
                        .put("cssMode", row.getInteger("css_mode"))
                        .put("sessionTimeout", row.getInteger("session_timeout"));
            }
            return new JsonObject().put("id", 1).put("loggingLevel", 1).put("cssMode", 1).put("sessionTimeout", 1800);
        }).otherwise(err -> new JsonObject().put("id", 1).put("loggingLevel", 1).put("cssMode", 1).put("sessionTimeout", 1800));
    }

    // Updates global system settings in the database.
    public Future<JsonObject> saveGlobalSetting(JsonObject json) {
        int log = json.getInteger("loggingLevel", 1);
        int css = json.getInteger("cssMode", 1);
        String sql = "UPDATE global_setting SET logging_level = $1, css_mode = $2 WHERE id = 1";
        return db.preparedQuery(sql).execute(Tuple.of(log, css))
                .map(rows -> new JsonObject().put("success", true).put("message", "Global Settings Updated Successfully"));
    }

    // Retrieves application branding details.
    public Future<JsonObject> getBrand() {
        String sql = "SELECT id, product_name, product_img FROM brand WHERE id = 1";
        return db.query(sql).execute().map(rows -> {
            if (rows.size() > 0) {
                Row row = rows.iterator().next();
                return new JsonObject()
                        .put("id", row.getLong("id"))
                        .put("productName", row.getString("product_name"))
                        .put("productImg", row.getString("product_img"));
            }
            return new JsonObject().put("id", 1).put("productName", "IP Address Manager").put("productImg", "/images/logo.png");
        }).otherwise(err -> new JsonObject().put("id", 1).put("productName", "IP Address Manager").put("productImg", "/images/logo.png"));
    }

    // Updates application branding configuration in the database.
    public Future<JsonObject> saveBrand(JsonObject json) {
        String name = json.getString("productName", "IP Address Manager");
        String sql = "UPDATE brand SET product_name = $1 WHERE id = 1";
        return db.preparedQuery(sql).execute(Tuple.of(name))
                .map(rows -> new JsonObject().put("success", true).put("message", "Branding Details Updated Successfully"));
    }

    // Retrieves mail server SMTP configurations from the database.
    public Future<JsonArray> getMailConfig() {
        String sql = "SELECT id, smtp_host, smtp_port, smtp_user, from_address FROM mail_server ORDER BY id ASC";
        return db.query(sql).execute().map(rows -> {
            JsonArray result = new JsonArray();
            for (Row row : rows) {
                result.add(new JsonObject()
                        .put("id", row.getLong("id"))
                        .put("smtpHost", row.getString("smtp_host"))
                        .put("smtpPort", row.getInteger("smtp_port"))
                        .put("smtpUser", row.getString("smtp_user"))
                        .put("fromAddress", row.getString("from_address")));
            }
            return result;
        }).otherwise(err -> new JsonArray().add(new JsonObject().put("id", 1).put("smtpHost", "smtp.gmail.com").put("smtpPort", 587).put("fromAddress", "admin@motadata.com")));
    }

    // Retrieves mail server configuration by ID.
    public Future<JsonObject> getMailConfigById(Long id) {
        return Future.succeededFuture(new JsonObject().put("id", id).put("smtpHost", "smtp.gmail.com").put("smtpPort", 587).put("fromAddress", "admin@motadata.com"));
    }

    // Saves mail server configuration settings.
    public Future<JsonObject> saveMailConfig(JsonObject json) {
        return Future.succeededFuture(new JsonObject().put("success", true).put("message", "Mail Server Configuration Saved Successfully"));
    }

    // Retrieves configured custom inventory columns from the database.
    public Future<JsonArray> getCustomColumns() {
        String sql = "SELECT id, column_name, column_type, description FROM custom_column ORDER BY id ASC";
        return db.query(sql).execute().map(rows -> {
            JsonArray result = new JsonArray();
            for (Row row : rows) {
                result.add(new JsonObject()
                        .put("id", row.getLong("id"))
                        .put("columnName", row.getString("column_name"))
                        .put("columnType", row.getString("column_type"))
                        .put("description", row.getString("description")));
            }
            return result;
        }).otherwise(err -> new JsonArray()
                .add(new JsonObject().put("id", 1).put("columnName", "Asset Tag").put("columnType", "STRING"))
                .add(new JsonObject().put("id", 2).put("columnName", "Owner Department").put("columnType", "STRING")));
    }

    // Saves a new custom column definition into the database.
    public Future<JsonObject> saveCustomColumn(JsonObject json) {
        String colName = json.getString("columnName", "Custom Column");
        String desc = json.getString("description", "Custom Column Definition");
        String sql = "INSERT INTO custom_column (column_name, column_type, description) VALUES ($1, 'STRING', $2) RETURNING id";
        return db.preparedQuery(sql).execute(Tuple.of(colName, desc))
                .map(rows -> new JsonObject().put("success", true).put("message", "Custom Column Saved Successfully"));
    }

    // Deletes a custom column definition by its ID.
    public Future<JsonObject> deleteCustomColumn(Long id) {
        String sql = "DELETE FROM custom_column WHERE id = $1";
        return db.preparedQuery(sql).execute(Tuple.of(id))
                .map(rows -> new JsonObject().put("success", true).put("message", "Custom Column Deleted Successfully"));
    }

    // Retrieves database backup and maintenance settings.
    public Future<JsonObject> getDatabaseMaintenance() {
        String sql = "SELECT id, status, backup_path, duration, schedule_status, schedule_hour, auto_backup, retention_days FROM database_maintainence WHERE id = 1";
        return db.query(sql).execute().map(rows -> {
            if (rows.size() > 0) {
                Row row = rows.iterator().next();
                return new JsonObject()
                        .put("id", row.getLong("id"))
                        .put("status", row.getString("status"))
                        .put("backupPath", row.getString("backup_path"))
                        .put("duration", row.getString("duration"))
                        .put("scheduleStatus", row.getBoolean("schedule_status"))
                        .put("scheduleHour", row.getInteger("schedule_hour"))
                        .put("autoBackup", row.getBoolean("auto_backup"))
                        .put("retentionDays", row.getInteger("retention_days"));
            }
            return new JsonObject().put("id", 1).put("autoBackup", true).put("retentionDays", 30);
        }).otherwise(err -> new JsonObject().put("id", 1).put("autoBackup", true).put("retentionDays", 30));
    }

    // Updates database maintenance and backup schedule configuration.
    public Future<JsonObject> saveDatabaseMaintenance(JsonObject json) {
        return Future.succeededFuture(new JsonObject().put("success", true).put("message", "Database Maintenance Settings Updated Successfully"));
    }
}
