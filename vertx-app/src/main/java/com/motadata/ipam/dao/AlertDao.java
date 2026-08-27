package com.motadata.ipam.dao;

import com.motadata.ipam.model.AlertStream;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.mysqlclient.MySQLPool;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.Tuple;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Reactive non-blocking Data Access Object for Alert operations using Vert.x MySQLPool.
 */
public class AlertDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(AlertDao.class);

    private final MySQLPool client;

    public AlertDao(MySQLPool client) {
        this.client = client;
    }

    public Future<Integer> countByStatus(Boolean status) {
        Promise<Integer> promise = Promise.promise();

        String sql = "SELECT COUNT(*) FROM alert_stream WHERE status = ?";

        client.preparedQuery(sql).execute(Tuple.of(status), ar -> {
            if (ar.succeeded()) {
                Row row = ar.result().iterator().next();
                Long count = row.getLong(0);
                promise.complete(count != null ? count.intValue() : 0);
            } else {
                LOGGER.error("Failed to count alerts by status {}: {}", status, ar.cause().getMessage());
                promise.complete(0);
            }
        });

        return promise.future();
    }

    public Future<List<AlertStream>> findByStatusOrderByTimestampDesc(Boolean status, int page, int pageSize) {
        Promise<List<AlertStream>> promise = Promise.promise();

        int offset = (page - 1) * pageSize;
        String sql = "SELECT id, subnet_id, alert_type, message, subnet, timestamp, status " +
                "FROM alert_stream WHERE status = ? ORDER BY timestamp DESC LIMIT ? OFFSET ?";

        client.preparedQuery(sql).execute(Tuple.of(status, pageSize, offset), ar -> {
            if (ar.succeeded()) {
                List<AlertStream> alerts = new ArrayList<>();
                for (Row row : ar.result()) {
                    AlertStream alert = new AlertStream();
                    alert.setId(row.getLong("id"));
                    alert.setSubnetId(row.getLong("subnet_id"));
                    alert.setAlertType(row.getString("alert_type"));
                    alert.setMessage(row.getString("message"));
                    alert.setSubnet(row.getString("subnet"));
                    alert.setStatus(row.getBoolean("status"));

                    LocalDateTime ldt = row.getLocalDateTime("timestamp");
                    if (ldt != null) {
                        alert.setTimestamp(Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant()));
                    }
                    alerts.add(alert);
                }
                promise.complete(alerts);
            } else {
                LOGGER.error("Failed to fetch alerts by status {}: {}", status, ar.cause().getMessage());
                promise.fail(ar.cause());
            }
        });

        return promise.future();
    }
}
