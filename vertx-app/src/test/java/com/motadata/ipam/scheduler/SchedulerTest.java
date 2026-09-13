package com.motadata.ipam.scheduler;

import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(VertxExtension.class)
public class SchedulerTest {

    private JobScheduler jobScheduler;

    // Sets up and starts JobScheduler before each test.
    @BeforeEach
    public void setUp(Vertx vertx) {
        jobScheduler = new JobScheduler(vertx);
        jobScheduler.start();
    }

    // Stops JobScheduler after each test.
    @AfterEach
    public void tearDown() {
        if (jobScheduler != null) {
            jobScheduler.stop();
        }
    }

    // Tests that JobScheduler starts correctly and initializes default periodic timers.
    @Test
    public void testJobSchedulerInitialization() {
        assertTrue(jobScheduler.isStarted());
        assertEquals(2, jobScheduler.getPeriodicTimerCount());
    }

    // Tests scheduling a SubnetScanJob using a cron expression.
    @Test
    public void testScheduleSubnetScanCronJob() {
        Map<String, Object> jobData = new HashMap<>();
        jobData.put("subnetId", 101L);
        jobData.put("subnetAddress", "192.168.1.0/24");

        boolean scheduled = jobScheduler.scheduleCronJob("TestSubnetScanJob", "TEST_GROUP", SubnetScanJob.class, "0 0/5 * * * ?", jobData);
        assertTrue(scheduled, "SubnetScanJob should be successfully scheduled");
    }

    // Tests triggering an ad-hoc execution of a scheduled SubnetScanJob.
    @Test
    public void testTriggerSubnetScanJob() {
        Map<String, Object> jobData = new HashMap<>();
        jobData.put("subnetId", 102L);

        jobScheduler.scheduleCronJob("TestTriggerSubnetScanJob", "TEST_GROUP", SubnetScanJob.class, "0 0/10 * * * ?", jobData);
        boolean triggered = jobScheduler.triggerJob("TestTriggerSubnetScanJob", "TEST_GROUP");
        assertTrue(triggered, "TestTriggerSubnetScanJob should be triggered");
    }

    // Tests scheduling a DhcpScanJob with custom job data parameters.
    @Test
    public void testScheduleDhcpScanJob() {
        Map<String, Object> jobData = new HashMap<>();
        jobData.put("dhcpCredentialId", 1L);
        jobData.put("serverHost", "10.0.0.1");

        boolean scheduled = jobScheduler.scheduleCronJob("TestDhcpScanJob", "TEST_GROUP", DhcpScanJob.class, "0 0 12 * * ?", jobData);
        assertTrue(scheduled, "DhcpScanJob should be successfully scheduled");
    }

    // Tests deleting a scheduled job from the JobScheduler.
    @Test
    public void testDeleteJob() {
        jobScheduler.scheduleCronJob("TestDeleteJob", "TEST_GROUP", AlertCleanupJob.class, "0 0 0 * * ?", null);
        boolean deleted = jobScheduler.deleteJob("TestDeleteJob", "TEST_GROUP");
        assertTrue(deleted, "TestDeleteJob should be deleted from scheduler");
    }
}
