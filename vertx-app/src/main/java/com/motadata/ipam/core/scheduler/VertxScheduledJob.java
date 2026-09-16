package com.motadata.ipam.core.scheduler;

import io.vertx.core.json.JsonObject;

@FunctionalInterface
public interface VertxScheduledJob {

    // Executes the scheduled task with given JSON configuration data.
    void execute(JsonObject data);
}
