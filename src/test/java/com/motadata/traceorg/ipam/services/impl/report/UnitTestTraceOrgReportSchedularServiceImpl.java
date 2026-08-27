package com.motadata.traceorg.ipam.services.impl.report;

import com.motadata.traceorg.ipam.entity.report.TraceOrgReportScheduler;
import com.motadata.traceorg.ipam.repository.report.TraceOrgReportScheduleRepository;
import com.motadata.traceorg.ipam.util.TraceOrgCommonConstants;
import com.motadata.traceorg.ipam.util.TraceOrgCommonUtil;
import com.motadata.traceorg.ipam.util.TraceOrgMessageConstants;
import org.junit.Before;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.*;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;

import static org.mockito.Mockito.times;

public class UnitTestTraceOrgReportSchedularServiceImpl
{
    @Mock
    private TraceOrgReportScheduleRepository traceOrgReportScheduleRepository;

    @Mock
    private TraceOrgReportScheduleRepository traceOrgReportSchedularRepository;

    @Mock
    TraceOrgReportScheduler exitstedTraceOrgReportScheduler;

    @Mock
    private TraceOrgCommonUtil traceOrgCommonUtil;

    @InjectMocks
    private TraceOrgReportSchedularServiceImpl traceOrgReportSchedularService;

    @Before
    public void setUp()
    {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testInsertReportScheduler_Success()
    {
        // Arrange
        TraceOrgReportScheduler scheduler = new TraceOrgReportScheduler();

        scheduler.setIpFilter("All IP");

        scheduler.setSchedulerName("TestScheduler");

        scheduler.setSchedulerTime(LocalTime.now().plusMinutes(10).format(DateTimeFormatter.ofPattern("HH:mm")));

        scheduler.setSubnetId("Subnet123");

        scheduler.setExportType("PDF");

        // Act
        HashMap<String, Object> result = traceOrgReportSchedularService.insertReportScheduler(scheduler);

        Assert.assertTrue((Boolean) result.get(TraceOrgCommonConstants.SUCCESS));
        // Assert
        Assert.assertEquals(TraceOrgMessageConstants.SCHEDULER_ADD_SUCCESS, result.get(TraceOrgCommonConstants.MESSAGE));

        traceOrgReportScheduleRepository.save(scheduler);

        // Verify that save method was called
        Mockito.verify(traceOrgReportScheduleRepository, times(1)).save(scheduler);
    }

    @Test
    public void testUpdateReportScheduler_Success()
    {
        // Arrange
        TraceOrgReportScheduler scheduler = new TraceOrgReportScheduler();

        scheduler.setIpFilter("All IP");

        scheduler.setSchedulerName("TestUpdateScheduler");

        scheduler.setSchedulerTime(LocalTime.now().plusMinutes(10).format(DateTimeFormatter.ofPattern("HH:mm")));

        scheduler.setSubnetId("Subnet12345");

        scheduler.setExportType("PDF");

        Mockito.when(traceOrgReportSchedularRepository.findOne((long) 1)).thenReturn(exitstedTraceOrgReportScheduler);

        Mockito.when(traceOrgReportSchedularRepository.exists((long) 1)).thenReturn(Boolean.TRUE);

        // Act
        HashMap<String, Object> result = traceOrgReportSchedularService.updateReportScheduler(scheduler, (long) 1);

        Assert.assertTrue((Boolean) result.get(TraceOrgCommonConstants.SUCCESS));
        // Assert
        Assert.assertEquals(TraceOrgMessageConstants.SCHEDULER_UPDATE_SUCCESS, result.get(TraceOrgCommonConstants.MESSAGE));

        traceOrgReportScheduleRepository.save(scheduler);

        // Verify that save method was called
        Mockito.verify(traceOrgReportScheduleRepository, times(1)).save(scheduler);
    }

    @Test
    public void testUpdateReportScheduler_PastTime()
    {
        // Arrange
        TraceOrgReportScheduler scheduler = new TraceOrgReportScheduler();

        scheduler.setIpFilter("All IP");

        scheduler.setSchedulerName("TestUpdateScheduler");

        scheduler.setSchedulerTime(LocalTime.now().minusMinutes(10).format(DateTimeFormatter.ofPattern("HH:mm")));

        scheduler.setSubnetId("Subnet12345");

        scheduler.setExportType("PDF");

        Mockito.when(traceOrgReportSchedularRepository.findOne((long) 1)).thenReturn(exitstedTraceOrgReportScheduler);

        // Act
        HashMap<String, Object> result = traceOrgReportSchedularService.updateReportScheduler(scheduler, (long) 1);

        Assert.assertFalse((Boolean) result.get(TraceOrgCommonConstants.SUCCESS));
        // Assert
        Assert.assertEquals(TraceOrgMessageConstants.ENTER_VALID_TIMELINE, result.get(TraceOrgCommonConstants.MESSAGE));
    }

    @Test
    public void testInsertReportScheduler_PastTime()
    {
        // Arrange
        TraceOrgReportScheduler scheduler = new TraceOrgReportScheduler();

        scheduler.setIpFilter("All IP");

        scheduler.setSchedulerName("TestScheduler");

        scheduler.setSchedulerTime(LocalTime.now().minusMinutes(10).format(DateTimeFormatter.ofPattern("HH:mm"))); // Past time

        scheduler.setSubnetId("Subnet123");

        scheduler.setExportType("PDF");
        // Act
        HashMap<String, Object> result = traceOrgReportSchedularService.insertReportScheduler(scheduler);

        // Assert
        Assert.assertFalse((Boolean) result.get(TraceOrgCommonConstants.SUCCESS));

        Assert.assertEquals(TraceOrgMessageConstants.ENTER_VALID_TIMELINE, result.get(TraceOrgCommonConstants.MESSAGE));
    }
}
