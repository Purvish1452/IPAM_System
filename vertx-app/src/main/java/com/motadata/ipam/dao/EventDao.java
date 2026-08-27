package com.motadata.ipam.dao;

import com.motadata.ipam.model.Event;
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
 * Reactive non-blocking Data Access Object for Event log operations using Vert.x MySQLPool.
 */
public class EventDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventDao.class);

    private final MySQLPool client;

    public EventDao(MySQLPool client) {
        this.client = client;
    }

    public Future<Integer> countEvents() {
        Promise<Integer> promise = Promise.promise();

        String sql = "SELECT COUNT(*) FROM event";

        client.query(sql).execute(ar -> {
            if (ar.succeeded()) {
                Row row = ar.result().iterator().next();
                Long count = row.getLong(0);
                promise.complete(count != null ? count.intValue() : 0);
            } else {
                LOGGER.error("Failed to count events: {}", ar.cause().getMessage());
                promise.complete(0);
            }
        });

        return promise.future();
    }

    public Future<List<Event>> findAllEvents(int page, int pageSize) {
        Promise<List<Event>> promise = Promise.promise();

        int offset = (page - 1) * pageSize;
        String sql = "SELECT id, event_type as category, event_context as message, timestamp, " +
                "severity, done_by_id as user " +
                "FROM event ORDER BY timestamp DESC LIMIT ? OFFSET ?";

        client.preparedQuery(sql).execute(Tuple.of(pageSize, offset), ar -> {
            if (ar.succeeded()) {
                List<Event> events = new ArrayList<>();
                for (Row row : ar.result()) {
                    Event event = new Event();
                    event.setId(row.getLong("id"));
                    event.setCategory(row.getString("category"));
                    event.setMessage(row.getString("message"));
                    event.setSeverity(String.valueOf(row.getInteger("severity")));
                    event.setUser(String.valueOf(row.getLong("user")));

                    LocalDateTime ldt = row.getLocalDateTime("timestamp");
                    if (ldt != null) {
                        event.setTimestamp(Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant()));
                    }
                    events.add(event);
                }
                promise.complete(events);
            } else {
                LOGGER.error("Failed to fetch events: {}", ar.cause().getMessage());
                promise.fail(ar.cause());
            }
        });

        return promise.future();
    }
}
