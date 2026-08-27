package com.motadata.traceorg.ipam.scheduler;

import com.motadata.traceorg.ipam.entity.report.TraceOrgReportScheduler;
import com.motadata.traceorg.ipam.scheduler.report.TraceOrgReportSchedulerJob;
import com.motadata.traceorg.ipam.services.TraceOrgService;
import com.motadata.traceorg.ipam.util.TraceOrgCommonConstants;
import com.motadata.traceorg.ipam.util.TraceOrgCommonUtil;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;

import java.util.HashMap;

import static org.mockito.Mockito.*;

public class UnitTestTraceOrgReportSchedulerJob
{

    @Mock
    private JobExecutionContext jobExecutionContext;

    @Mock
    private JobDataMap jobDataMap;

    @Mock
    private TraceOrgCommonUtil traceOrgCommonUtil;

    @Mock
    private TraceOrgService traceOrgService;

    @Mock
    private TraceOrgReportScheduler traceOrgReportScheduler;

    @InjectMocks
    private TraceOrgReportSchedulerJob traceOrgReportSchedulerJob;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testExecute_ExportAllEventReportPdfCalled() throws Exception
    {
        // Given
        String exportType = "PDF";

        String ipFilter = TraceOrgCommonConstants.EVENT_LOG_REPORT;

        Integer reportExportTimeline = new Integer(30);

        when(jobExecutionContext.getMergedJobDataMap()).thenReturn(jobDataMap);

        when(jobDataMap.get("traceOrgReportScheduler")).thenReturn(traceOrgReportScheduler);

        when(jobDataMap.get("traceOrgService")).thenReturn(traceOrgService);

        when(jobDataMap.get("traceOrgCommonUtil")).thenReturn(traceOrgCommonUtil);

        when(traceOrgReportScheduler.getExportType()).thenReturn(exportType);

        when(traceOrgReportScheduler.getIpFilter()).thenReturn(ipFilter);

        when(traceOrgReportScheduler.getReportExportTimeline()).thenReturn(reportExportTimeline);

        // When
        traceOrgReportSchedulerJob.execute(jobExecutionContext);

        // Then
        verify(traceOrgCommonUtil, times(1)).exportAllEventReportPdf(reportExportTimeline);
    }

    @Test
    public void testExecute_ExportOtherReportPdfCalled() throws Exception
    {
        HashMap<String, Object> result = new HashMap<>();
        // Given
        String exportType = "PDF";

        String ipFilter = TraceOrgCommonConstants.ALL_IP_REPORT;

        Integer reportExportTimeline = new Integer(30);

        when(jobExecutionContext.getMergedJobDataMap()).thenReturn(jobDataMap);

        when(jobDataMap.get("traceOrgReportScheduler")).thenReturn(traceOrgReportScheduler);

        when(jobDataMap.get("traceOrgService")).thenReturn(traceOrgService);

        when(jobDataMap.get("traceOrgCommonUtil")).thenReturn(traceOrgCommonUtil);

        when(traceOrgReportScheduler.getExportType()).thenReturn(exportType);

        when(traceOrgReportScheduler.getIpFilter()).thenReturn(ipFilter);

        when(traceOrgReportScheduler.getReportExportTimeline()).thenReturn(reportExportTimeline);

        // When
        traceOrgReportSchedulerJob.execute(jobExecutionContext);

        // Then
        verify(traceOrgCommonUtil, times(1)).exportSubnetIpReportByTimeline(result, traceOrgReportScheduler.getSubnetId(), traceOrgReportScheduler.getIpFilter(), traceOrgReportScheduler.getReportExportTimeline(), Boolean.TRUE);
    }

    @Test
    public void testExecute_ExportOtherReportCSVCalled() throws Exception
    {
        HashMap<String, Object> result = new HashMap<>();
        // Given
        String exportType = "CSV";

        String ipFilter = TraceOrgCommonConstants.ALL_IP_REPORT;

        Integer reportExportTimeline = new Integer(30);

        when(jobExecutionContext.getMergedJobDataMap()).thenReturn(jobDataMap);

        when(jobDataMap.get("traceOrgReportScheduler")).thenReturn(traceOrgReportScheduler);

        when(jobDataMap.get("traceOrgService")).thenReturn(traceOrgService);

        when(jobDataMap.get("traceOrgCommonUtil")).thenReturn(traceOrgCommonUtil);

        when(traceOrgReportScheduler.getExportType()).thenReturn(exportType);

        when(traceOrgReportScheduler.getIpFilter()).thenReturn(ipFilter);

        when(traceOrgReportScheduler.getReportExportTimeline()).thenReturn(reportExportTimeline);

        // When
        traceOrgReportSchedulerJob.execute(jobExecutionContext);

        // Then
        verify(traceOrgCommonUtil, times(1)).exportSubnetIpReportByTimeline(result, traceOrgReportScheduler.getSubnetId(), traceOrgReportScheduler.getIpFilter(), traceOrgReportScheduler.getReportExportTimeline(), Boolean.FALSE);
    }

    @Test
    public void testExecute_ExportAllConflictIpReportPdfCalled() throws Exception
    {
        // Given
        String exportType = "PDF";

        String ipFilter = TraceOrgCommonConstants.CONFLICT_IP_REPORT;

        Integer reportExportTimeline = new Integer(30);

        when(jobExecutionContext.getMergedJobDataMap()).thenReturn(jobDataMap);

        when(jobDataMap.get("traceOrgReportScheduler")).thenReturn(traceOrgReportScheduler);

        when(jobDataMap.get("traceOrgService")).thenReturn(traceOrgService);

        when(jobDataMap.get("traceOrgCommonUtil")).thenReturn(traceOrgCommonUtil);

        when(traceOrgReportScheduler.getExportType()).thenReturn(exportType);

        when(traceOrgReportScheduler.getIpFilter()).thenReturn(ipFilter);

        when(traceOrgReportScheduler.getReportExportTimeline()).thenReturn(reportExportTimeline);

        // When
        traceOrgReportSchedulerJob.execute(jobExecutionContext);

        // Then
        verify(traceOrgCommonUtil, times(1)).exportAllConflictIpReportPdf(reportExportTimeline);
    }

    @Test
    public void testExecute_ExportSubnetUtilizationReportPdfCalled() throws Exception
    {
        // Given
        String exportType = "PDF";

        String ipFilter = TraceOrgCommonConstants.SUBNET_UTILIZATION_REPORT;

        Integer reportExportTimeline = new Integer(30);

        when(jobExecutionContext.getMergedJobDataMap()).thenReturn(jobDataMap);

        when(jobDataMap.get("traceOrgReportScheduler")).thenReturn(traceOrgReportScheduler);

        when(jobDataMap.get("traceOrgService")).thenReturn(traceOrgService);

        when(jobDataMap.get("traceOrgCommonUtil")).thenReturn(traceOrgCommonUtil);

        when(traceOrgReportScheduler.getExportType()).thenReturn(exportType);

        when(traceOrgReportScheduler.getIpFilter()).thenReturn(ipFilter);

        when(traceOrgReportScheduler.getReportExportTimeline()).thenReturn(reportExportTimeline);

        // When
        traceOrgReportSchedulerJob.execute(jobExecutionContext);

        // Then
        verify(traceOrgCommonUtil, times(1)).exportSubnetUtilizationReportPdf(reportExportTimeline);
    }

    @Test
    public void testExecute_ExportDHCPUtilizationReportPdfCalled() throws Exception
    {
        // Given
        String exportType = "PDF";

        String ipFilter = TraceOrgCommonConstants.DHCP_UTILIZATION_REPORT;

        Integer reportExportTimeline = new Integer(30);

        when(jobExecutionContext.getMergedJobDataMap()).thenReturn(jobDataMap);

        when(jobDataMap.get("traceOrgReportScheduler")).thenReturn(traceOrgReportScheduler);

        when(jobDataMap.get("traceOrgService")).thenReturn(traceOrgService);

        when(jobDataMap.get("traceOrgCommonUtil")).thenReturn(traceOrgCommonUtil);

        when(traceOrgReportScheduler.getExportType()).thenReturn(exportType);

        when(traceOrgReportScheduler.getIpFilter()).thenReturn(ipFilter);

        when(traceOrgReportScheduler.getReportExportTimeline()).thenReturn(reportExportTimeline);

        // When
        traceOrgReportSchedulerJob.execute(jobExecutionContext);

        // Then
        verify(traceOrgCommonUtil, times(1)).exportDHCPUtilizationReportPdf(reportExportTimeline);
    }

    @Test
    public void testExecute_ExportAllEventReportCsvCalled() throws Exception
    {
        // Given
        String exportType = "CSV";

        String ipFilter = TraceOrgCommonConstants.EVENT_LOG_REPORT;

        Integer reportExportTimeline = new Integer(30);

        when(jobExecutionContext.getMergedJobDataMap()).thenReturn(jobDataMap);

        when(jobDataMap.get("traceOrgReportScheduler")).thenReturn(traceOrgReportScheduler);

        when(jobDataMap.get("traceOrgService")).thenReturn(traceOrgService);

        when(jobDataMap.get("traceOrgCommonUtil")).thenReturn(traceOrgCommonUtil);

        when(traceOrgReportScheduler.getExportType()).thenReturn(exportType);

        when(traceOrgReportScheduler.getIpFilter()).thenReturn(ipFilter);

        when(traceOrgReportScheduler.getReportExportTimeline()).thenReturn(reportExportTimeline);

        // When
        traceOrgReportSchedulerJob.execute(jobExecutionContext);

        // Then
        verify(traceOrgCommonUtil, times(1)).exportAllEventReportCsv(reportExportTimeline);
    }

    @Test
    public void testExecute_ExportAllConflictIpReportCsvCalled() throws Exception
    {
        // Given
        String exportType = "CSV";

        String ipFilter = TraceOrgCommonConstants.CONFLICT_IP_REPORT;

        Integer reportExportTimeline = new Integer(30);

        when(jobExecutionContext.getMergedJobDataMap()).thenReturn(jobDataMap);

        when(jobDataMap.get("traceOrgReportScheduler")).thenReturn(traceOrgReportScheduler);

        when(jobDataMap.get("traceOrgService")).thenReturn(traceOrgService);

        when(jobDataMap.get("traceOrgCommonUtil")).thenReturn(traceOrgCommonUtil);

        when(traceOrgReportScheduler.getExportType()).thenReturn(exportType);

        when(traceOrgReportScheduler.getIpFilter()).thenReturn(ipFilter);

        when(traceOrgReportScheduler.getReportExportTimeline()).thenReturn(reportExportTimeline);

        // When
        traceOrgReportSchedulerJob.execute(jobExecutionContext);

        // Then
        verify(traceOrgCommonUtil, times(1)).exportAllConflictIpReportCsv(reportExportTimeline);
    }

    @Test
    public void testExecute_ExportSubnetUtilizationReportCsvCalled() throws Exception
    {
        // Given
        String exportType = "CSV";

        String ipFilter = TraceOrgCommonConstants.SUBNET_UTILIZATION_REPORT;

        Integer reportExportTimeline = new Integer(30);

        when(jobExecutionContext.getMergedJobDataMap()).thenReturn(jobDataMap);

        when(jobDataMap.get("traceOrgReportScheduler")).thenReturn(traceOrgReportScheduler);

        when(jobDataMap.get("traceOrgService")).thenReturn(traceOrgService);

        when(jobDataMap.get("traceOrgCommonUtil")).thenReturn(traceOrgCommonUtil);

        when(traceOrgReportScheduler.getExportType()).thenReturn(exportType);

        when(traceOrgReportScheduler.getIpFilter()).thenReturn(ipFilter);

        when(traceOrgReportScheduler.getReportExportTimeline()).thenReturn(reportExportTimeline);

        // When
        traceOrgReportSchedulerJob.execute(jobExecutionContext);

        // Then
        verify(traceOrgCommonUtil, times(1)).exportSubnetUtilizationReportCsv(reportExportTimeline);
    }

    @Test
    public void testExecute_ExportDHCPUtilizationReportCsvCalled() throws Exception
    {
        // Given
        String exportType = "CSV";

        String ipFilter = TraceOrgCommonConstants.DHCP_UTILIZATION_REPORT;

        Integer reportExportTimeline = new Integer(30);

        when(jobExecutionContext.getMergedJobDataMap()).thenReturn(jobDataMap);

        when(jobDataMap.get("traceOrgReportScheduler")).thenReturn(traceOrgReportScheduler);

        when(jobDataMap.get("traceOrgService")).thenReturn(traceOrgService);

        when(jobDataMap.get("traceOrgCommonUtil")).thenReturn(traceOrgCommonUtil);

        when(traceOrgReportScheduler.getExportType()).thenReturn(exportType);

        when(traceOrgReportScheduler.getIpFilter()).thenReturn(ipFilter);

        when(traceOrgReportScheduler.getReportExportTimeline()).thenReturn(reportExportTimeline);

        // When
        traceOrgReportSchedulerJob.execute(jobExecutionContext);

        // Then
        verify(traceOrgCommonUtil, times(1)).exportDHCPUtilizationReportCsv(reportExportTimeline);
    }
}