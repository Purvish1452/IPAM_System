package com.motadata.ipam.scheduler;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Asynchronous background scheduler backed by Vert.x timers.
 */
public class JobScheduler {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobScheduler.class);
    private final Vertx vertx;
    private final List<Long> periodicTimerIds = new ArrayList<>();
    private final Map<String, Long> scheduledJobs = new HashMap<>();
    private final Map<String, ScheduledDefinition> scheduledDefinitions = new HashMap<>();
    private boolean started;

    // Constructs JobScheduler with the specified Vert.x instance.
    public JobScheduler(Vertx vertx) {
        this.vertx = vertx;
    }

    // Start the scheduler and register all predefined periodic Vert.x timers.
    public synchronized void start() {
        if (started) {
            return;
        }

        LOGGER.info("Initializing IPAM JobScheduler...");
        periodicTimerIds.add(vertx.setPeriodic(10_000L,
                timerId -> LOGGER.trace("Executing Vert.x subnet scan queue check (timerId={})", timerId)));
        periodicTimerIds.add(vertx.setPeriodic(3_600_000L, timerId -> {
            LOGGER.debug("Executing Vert.x alert cleanup task (timerId={})", timerId);
            new AlertCleanupJob().execute(new JsonObject());
        }));
        started = true;
        LOGGER.info("Vert.x JobScheduler successfully started with {} periodic timers.", periodicTimerIds.size());
    }

    // Stop the scheduler and cancel all registered Vert.x timers and jobs.
    public synchronized void stop() {
        LOGGER.info("Stopping IPAM JobScheduler...");
        periodicTimerIds.forEach(vertx::cancelTimer);
        scheduledJobs.values().forEach(vertx::cancelTimer);
        periodicTimerIds.clear();
        scheduledJobs.clear();
        scheduledDefinitions.clear();
        started = false;
        LOGGER.info("Vert.x JobScheduler stopped.");
    }

    // Schedule or replace a recurring job using a Vert.x periodic timer.
    public synchronized boolean scheduleCronJob(String jobName, String groupName,
                                                Class<? extends VertxScheduledJob> jobClass,
                                                String cronExpression, Map<String, Object> jobDataMap) {
        if (!started) {
            LOGGER.error("Cannot schedule job {}; Vert.x scheduler is not running", jobName);
            return false;
        }

        String key = groupName + ":" + jobName;
        Long previousTimer = scheduledJobs.remove(key);
        if (previousTimer != null) {
            vertx.cancelTimer(previousTimer);
        }

        JsonObject data = jobDataMap == null ? new JsonObject() : new JsonObject(jobDataMap);
        scheduledDefinitions.put(key, new ScheduledDefinition(jobClass, data));
        long timerId = vertx.setPeriodic(intervalForCron(cronExpression),
                ignored -> execute(jobClass, data));
        scheduledJobs.put(key, timerId);
        LOGGER.info("Successfully scheduled Vert.x job {} [{}] from cron '{}'",
                jobName, groupName, cronExpression);
        return true;
    }

    // Manually trigger a previously scheduled job immediately.
    public synchronized boolean triggerJob(String jobName, String groupName) {
        ScheduledDefinition definition = scheduledDefinitions.get(groupName + ":" + jobName);
        if (definition == null) {
            return false;
        }
        execute(definition.jobClass(), definition.data());
        return true;
    }

    // Remove a scheduled job and cancel its associated Vert.x timer.
    public synchronized boolean deleteJob(String jobName, String groupName) {
        String key = groupName + ":" + jobName;
        Long timerId = scheduledJobs.remove(key);
        scheduledDefinitions.remove(key);
        return timerId != null && vertx.cancelTimer(timerId);
    }

    // Check whether the scheduler is currently running.
    public synchronized boolean isStarted() {
        return started;
    }

    // Return the number of predefined periodic timers.
    public synchronized int getPeriodicTimerCount() {
        return periodicTimerIds.size();
    }

    // Create the scheduled job instance and execute it with the provided data.
    private void execute(Class<? extends VertxScheduledJob> jobClass, JsonObject data) {
        try {
            jobClass.getDeclaredConstructor().newInstance().execute(data);
        } catch (ReflectiveOperationException | RuntimeException e) {
            LOGGER.error("Scheduled job {} failed", jobClass.getName(), e);
        }
    }

    // Convert supported cron expressions into a recurring interval in milliseconds.
    private long intervalForCron(String cronExpression) {
        if (cronExpression != null && cronExpression.matches("0 0/\\d+ \\* \\* \\* \\?")) {
            int minutes = Integer.parseInt(cronExpression.split(" ")[1].substring(2));
            return minutes * 60_000L;
        }
        return 24 * 60 * 60_000L;
    }

    private record ScheduledDefinition(Class<? extends VertxScheduledJob> jobClass, JsonObject data) {
    }
}
