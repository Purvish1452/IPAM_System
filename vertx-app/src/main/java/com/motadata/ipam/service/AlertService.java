package com.motadata.ipam.service;

import com.motadata.ipam.dao.AlertDao;
import com.motadata.ipam.model.AlertStream;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.util.List;

/**
 * Asynchronous Vert.x service for Alert management operations.
 */
public class AlertService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AlertService.class);

    private final AlertDao alertDao;

    public AlertService(AlertDao alertDao) {
        this.alertDao = alertDao;
    }

    public Future<JsonObject> getAlerts(String alertFilter, Integer page, Integer pageSize) {
        Promise<JsonObject> promise = Promise.promise();

        int pageNum = (page == null || page < 1) ? 1 : page;
        int size = (pageSize == null || pageSize < 1) ? 20 : pageSize;

        Boolean status = !(alertFilter != null && alertFilter.equalsIgnoreCase("ALERT_CLEAR"));

        alertDao.countByStatus(status).onComplete(countAr -> {
            if (countAr.succeeded()) {
                int totalCount = countAr.result();
                alertDao.findByStatusOrderByTimestampDesc(status, pageNum, size).onComplete(alertsAr -> {
                    if (alertsAr.succeeded()) {
                        List<AlertStream> alertList = alertsAr.result();

                        JsonObject dataJson = new JsonObject()
                                .put("total", totalCount)
                                .put("data", new JsonArray(alertList));

                        JsonObject result = new JsonObject()
                                .put("data", dataJson)
                                .put("success", true)
                                .put("message", (String) null);

                        promise.complete(result);
                    } else {
                        LOGGER.error("Error fetching alert streams: {}", alertsAr.cause().getMessage());
                        promise.complete(new JsonObject().put("success", false).put("message", "Something Went Wrong"));
                    }
                });
            } else {
                LOGGER.error("Error counting alert streams: {}", countAr.cause().getMessage());
                promise.complete(new JsonObject().put("success", false).put("message", "Something Went Wrong"));
            }
        });

        return promise.future();
    }
}
