package com.motadata.ipam.scheduler;

import io.vertx.core.json.JsonObject;

@FunctionalInterface
public interface VertxScheduledJob {

    void execute(JsonObject data);
}
