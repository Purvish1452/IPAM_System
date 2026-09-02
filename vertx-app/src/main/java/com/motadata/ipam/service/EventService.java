package com.motadata.ipam.service;

import com.motadata.ipam.dao.EventDao;
import com.motadata.ipam.model.Event;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.util.List;

/**
 * Asynchronous Vert.x service for Event log operations.
 */
public class EventService {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventService.class);

    private final EventDao eventDao;

    public EventService(EventDao eventDao) {
        this.eventDao = eventDao;
    }

    public Future<JsonObject> getEvents(Integer page, Integer pageSize) {
        Promise<JsonObject> promise = Promise.promise();

        int pageNum = (page == null || page < 1) ? 1 : page;
        int size = (pageSize == null || pageSize < 1) ? 20 : pageSize;

        eventDao.countEvents().onComplete(countAr -> {
            if (countAr.succeeded()) {
                int totalCount = countAr.result();
                eventDao.findAllEvents(pageNum, size).onComplete(eventsAr -> {
                    if (eventsAr.succeeded()) {
                        List<Event> eventList = eventsAr.result();

                        JsonObject result = new JsonObject()
                                .put("data", new JsonArray(eventList))
                                .put("total", totalCount)
                                .put("success", true)
                                .put("message", (String) null);

                        promise.complete(result);
                    } else {
                        LOGGER.error("Error fetching event log: {}", eventsAr.cause().getMessage());
                        promise.complete(new JsonObject().put("success", false).put("data", new JsonArray()).put("message", "Something Went Wrong"));
                    }
                });
            } else {
                LOGGER.error("Error counting events: {}", countAr.cause().getMessage());
                promise.complete(new JsonObject().put("success", false).put("data", new JsonArray()).put("message", "Something Went Wrong"));
            }
        });

        return promise.future();
    }
}
