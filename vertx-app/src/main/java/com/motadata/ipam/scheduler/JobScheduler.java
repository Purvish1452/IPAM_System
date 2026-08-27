package com.motadata.ipam.scheduler;

import io.vertx.core.Vertx;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Asynchronous Background Job Scheduler managing Vert.x Periodic Timers and Quartz Cron Jobs.
 */
public class JobScheduler {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobScheduler.class);

    private final Vertx vertx;
    private Scheduler quartzScheduler;
    private final List<Long> periodicTimerIds = new ArrayList<>();

    public JobScheduler(Vertx vertx) {
        this.vertx = vertx;
    }

    public synchronized void start() {
        LOGGER.info("Initializing IPAM JobScheduler...");
        try {
            SchedulerFactory schedulerFactory = new StdSchedulerFactory();
            quartzScheduler = schedulerFactory.getScheduler();
            quartzScheduler.start();
            LOGGER.info("Quartz Scheduler successfully started.");
        } catch (SchedulerException e) {
            LOGGER.error("Failed to start Quartz Scheduler: {}", e.getMessage(), e);
        }

        // Register Vert.x Periodic Timer for Lightweight Queue Checks (Every 10 Seconds)
        long queueCheckTimerId = vertx.setPeriodic(10000, timerId -> {
            LOGGER.trace("Executing Vert.x periodic subnet scan queue check (timerId={})", timerId);
            // Periodic lightweight health and queue check logic
        });
        periodicTimerIds.add(queueCheckTimerId);

        // Register Vert.x Periodic Timer for Alert Cleanup (Every 1 Hour)
        long alertCleanupTimerId = vertx.setPeriodic(3600000, timerId -> {
            LOGGER.debug("Executing Vert.x periodic alert cleanup task (timerId={})", timerId);
            triggerJobQuietly("AlertCleanupJob", "DEFAULT");
        });
        periodicTimerIds.add(alertCleanupTimerId);
    }

    public synchronized void stop() {
        LOGGER.info("Stopping IPAM JobScheduler...");
        for (Long timerId : periodicTimerIds) {
            vertx.cancelTimer(timerId);
        }
        periodicTimerIds.clear();

        if (quartzScheduler != null) {
            try {
                if (!quartzScheduler.isShutdown()) {
                    quartzScheduler.shutdown(true);
                    LOGGER.info("Quartz Scheduler gracefully shutdown.");
                }
            } catch (SchedulerException e) {
                LOGGER.error("Error shutting down Quartz Scheduler: {}", e.getMessage(), e);
            }
        }
    }

    public boolean scheduleCronJob(String jobName, String groupName, Class<? extends Job> jobClass, String cronExpression, Map<String, Object> jobDataMap) {
        if (quartzScheduler == null) {
            LOGGER.error("Cannot schedule job {}; Quartz Scheduler is not running", jobName);
            return false;
        }

        try {
            JobKey jobKey = JobKey.jobKey(jobName, groupName);
            TriggerKey triggerKey = TriggerKey.triggerKey(jobName + "Trigger", groupName);

            if (quartzScheduler.checkExists(jobKey)) {
                quartzScheduler.deleteJob(jobKey);
            }

            JobBuilder jobBuilder = JobBuilder.newJob(jobClass)
                    .withIdentity(jobKey)
                    .storeDurably();

            if (jobDataMap != null && !jobDataMap.isEmpty()) {
                jobBuilder.usingJobData(new JobDataMap(jobDataMap));
            }

            JobDetail jobDetail = jobBuilder.build();

            CronTrigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity(triggerKey)
                    .withSchedule(CronScheduleBuilder.cronSchedule(cronExpression))
                    .build();

            quartzScheduler.scheduleJob(jobDetail, trigger);
            LOGGER.info("Successfully scheduled Quartz job {} [{}] with cron '{}'", jobName, groupName, cronExpression);
            return true;
        } catch (Exception e) {
            LOGGER.error("Failed to schedule cron job {} [{}]: {}", jobName, groupName, e.getMessage(), e);
            return false;
        }
    }

    public boolean triggerJob(String jobName, String groupName) {
        if (quartzScheduler == null) {
            return false;
        }
        try {
            JobKey jobKey = JobKey.jobKey(jobName, groupName);
            if (quartzScheduler.checkExists(jobKey)) {
                quartzScheduler.triggerJob(jobKey);
                LOGGER.info("Manually triggered Quartz job {} [{}]", jobName, groupName);
                return true;
            } else {
                LOGGER.warn("Job {} [{}] does not exist in Quartz scheduler", jobName, groupName);
                return false;
            }
        } catch (SchedulerException e) {
            LOGGER.error("Failed to trigger job {} [{}]: {}", jobName, groupName, e.getMessage(), e);
            return false;
        }
    }

    private void triggerJobQuietly(String jobName, String groupName) {
        try {
            triggerJob(jobName, groupName);
        } catch (Exception ignored) {
        }
    }

    public boolean deleteJob(String jobName, String groupName) {
        if (quartzScheduler == null) {
            return false;
        }
        try {
            JobKey jobKey = JobKey.jobKey(jobName, groupName);
            if (quartzScheduler.checkExists(jobKey)) {
                boolean deleted = quartzScheduler.deleteJob(jobKey);
                LOGGER.info("Deleted Quartz job {} [{}]: {}", jobName, groupName, deleted);
                return deleted;
            }
            return false;
        } catch (SchedulerException e) {
            LOGGER.error("Failed to delete job {} [{}]: {}", jobName, groupName, e.getMessage(), e);
            return false;
        }
    }

    public Scheduler getQuartzScheduler() {
        return quartzScheduler;
    }
}
