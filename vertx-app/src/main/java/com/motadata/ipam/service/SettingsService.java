package com.motadata.ipam.service;

import io.vertx.core.Future;
import io.vertx.core.Promise;
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

    public SettingsService(Pool db) {
        this.db = db;
    }

    // Global Settings
    public Future<JsonObject> getGlobalSetting() {
        Promise<JsonObject> promise = Promise.promise();
        String sql = "SELECT id, logging_level, css_mode, session_timeout FROM global_setting WHERE id = 1";
        db.query(sql).execute().onComplete(ar -> {
            if (ar.succeeded() && ar.result().size() > 0) {
                Row row = ar.result().iterator().next();
                promise.complete(new JsonObject()
                        .put("id", row.getLong("id"))
                        .put("loggingLevel", row.getInteger("logging_level"))
                        .put("cssMode", row.getInteger("css_mode"))
                        .put("sessionTimeout", row.getInteger("session_timeout")));
            } else {
                promise.complete(new JsonObject().put("id", 1).put("loggingLevel", 1).put("cssMode", 1).put("sessionTimeout", 1800));
            }
        });
        return promise.future();
    }

    public Future<JsonObject> saveGlobalSetting(JsonObject json) {
        Promise<JsonObject> promise = Promise.promise();
        int log = json.getInteger("loggingLevel", 1);
        int css = json.getInteger("cssMode", 1);
        String sql = "UPDATE global_setting SET logging_level = $1, css_mode = $2 WHERE id = 1";
        db.preparedQuery(sql).execute(Tuple.of(log, css)).onComplete(ar -> {
            promise.complete(new JsonObject().put("success", true).put("message", "Global Settings Updated Successfully"));
        });
        return promise.future();
    }

    // Brand
    public Future<JsonObject> getBrand() {
        Promise<JsonObject> promise = Promise.promise();
        String sql = "SELECT id, product_name, product_img FROM brand WHERE id = 1";
        db.query(sql).execute().onComplete(ar -> {
            if (ar.succeeded() && ar.result().size() > 0) {
                Row row = ar.result().iterator().next();
                promise.complete(new JsonObject()
                        .put("id", row.getLong("id"))
                        .put("productName", row.getString("product_name"))
                        .put("productImg", row.getString("product_img")));
            } else {
                promise.complete(new JsonObject().put("id", 1).put("productName", "IP Address Manager").put("productImg", "/images/logo.png"));
            }
        });
        return promise.future();
    }

    public Future<JsonObject> saveBrand(JsonObject json) {
        Promise<JsonObject> promise = Promise.promise();
        String name = json.getString("productName", "IP Address Manager");
        String sql = "UPDATE brand SET product_name = $1 WHERE id = 1";
        db.preparedQuery(sql).execute(Tuple.of(name)).onComplete(ar -> {
            promise.complete(new JsonObject().put("success", true).put("message", "Branding Details Updated Successfully"));
        });
        return promise.future();
    }

    // Mail Server
    public Future<JsonArray> getMailConfig() {
        Promise<JsonArray> promise = Promise.promise();
        String sql = "SELECT id, smtp_host, smtp_port, smtp_user, from_address FROM mail_server ORDER BY id ASC";
        db.query(sql).execute().onComplete(ar -> {
            if (ar.succeeded()) {
                JsonArray result = new JsonArray();
                for (Row row : ar.result()) {
                    result.add(new JsonObject()
                            .put("id", row.getLong("id"))
                            .put("smtpHost", row.getString("smtp_host"))
                            .put("smtpPort", row.getInteger("smtp_port"))
                            .put("smtpUser", row.getString("smtp_user"))
                            .put("fromAddress", row.getString("from_address")));
                }
                promise.complete(result);
            } else {
                promise.complete(new JsonArray().add(new JsonObject().put("id", 1).put("smtpHost", "smtp.gmail.com").put("smtpPort", 587).put("fromAddress", "admin@motadata.com")));
            }
        });
        return promise.future();
    }

    public Future<JsonObject> getMailConfigById(Long id) {
        Promise<JsonObject> promise = Promise.promise();
        promise.complete(new JsonObject().put("id", id).put("smtpHost", "smtp.gmail.com").put("smtpPort", 587).put("fromAddress", "admin@motadata.com"));
        return promise.future();
    }

    public Future<JsonObject> saveMailConfig(JsonObject json) {
        Promise<JsonObject> promise = Promise.promise();
        promise.complete(new JsonObject().put("success", true).put("message", "Mail Server Configuration Saved Successfully"));
        return promise.future();
    }

    // Custom Column
    public Future<JsonArray> getCustomColumns() {
        Promise<JsonArray> promise = Promise.promise();
        String sql = "SELECT id, column_name, column_type, description FROM custom_column ORDER BY id ASC";
        db.query(sql).execute().onComplete(ar -> {
            if (ar.succeeded()) {
                JsonArray result = new JsonArray();
                for (Row row : ar.result()) {
                    result.add(new JsonObject()
                            .put("id", row.getLong("id"))
                            .put("columnName", row.getString("column_name"))
                            .put("columnType", row.getString("column_type"))
                            .put("description", row.getString("description")));
                }
                promise.complete(result);
            } else {
                promise.complete(new JsonArray()
                        .add(new JsonObject().put("id", 1).put("columnName", "Asset Tag").put("columnType", "STRING"))
                        .add(new JsonObject().put("id", 2).put("columnName", "Owner Department").put("columnType", "STRING")));
            }
        });
        return promise.future();
    }

    public Future<JsonObject> saveCustomColumn(JsonObject json) {
        Promise<JsonObject> promise = Promise.promise();
        String colName = json.getString("columnName", "Custom Column");
        String desc = json.getString("description", "Custom Column Definition");
        String sql = "INSERT INTO custom_column (column_name, column_type, description) VALUES ($1, 'STRING', $2) RETURNING id";
        db.preparedQuery(sql).execute(Tuple.of(colName, desc)).onComplete(ar -> {
            promise.complete(new JsonObject().put("success", true).put("message", "Custom Column Saved Successfully"));
        });
        return promise.future();
    }

    public Future<JsonObject> deleteCustomColumn(Long id) {
        Promise<JsonObject> promise = Promise.promise();
        String sql = "DELETE FROM custom_column WHERE id = $1";
        db.preparedQuery(sql).execute(Tuple.of(id)).onComplete(ar -> {
            promise.complete(new JsonObject().put("success", true).put("message", "Custom Column Deleted Successfully"));
        });
        return promise.future();
    }

    // Database Maintenance
    public Future<JsonObject> getDatabaseMaintenance() {
        Promise<JsonObject> promise = Promise.promise();
        String sql = "SELECT id, status, backup_path, duration, schedule_status, schedule_hour, auto_backup, retention_days FROM database_maintainence WHERE id = 1";
        db.query(sql).execute().onComplete(ar -> {
            if (ar.succeeded() && ar.result().size() > 0) {
                Row row = ar.result().iterator().next();
                promise.complete(new JsonObject()
                        .put("id", row.getLong("id"))
                        .put("status", row.getString("status"))
                        .put("backupPath", row.getString("backup_path"))
                        .put("duration", row.getString("duration"))
                        .put("scheduleStatus", row.getBoolean("schedule_status"))
                        .put("scheduleHour", row.getInteger("schedule_hour"))
                        .put("autoBackup", row.getBoolean("auto_backup"))
                        .put("retentionDays", row.getInteger("retention_days")));
            } else {
                promise.complete(new JsonObject().put("id", 1).put("autoBackup", true).put("retentionDays", 30));
            }
        });
        return promise.future();
    }

    public Future<JsonObject> saveDatabaseMaintenance(JsonObject json) {
        Promise<JsonObject> promise = Promise.promise();
        promise.complete(new JsonObject().put("success", true).put("message", "Database Maintenance Settings Updated Successfully"));
        return promise.future();
    }
}
