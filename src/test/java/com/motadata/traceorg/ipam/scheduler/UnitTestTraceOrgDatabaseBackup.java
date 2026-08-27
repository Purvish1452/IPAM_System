package com.motadata.traceorg.ipam.scheduler;

import com.motadata.traceorg.ipam.scheduler.database.TraceOrgDatabaseBackup;
import com.motadata.traceorg.ipam.util.*;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;

import static org.mockito.Mockito.*;

public class UnitTestTraceOrgDatabaseBackup
{
    @Mock
    private JobExecutionContext jobExecutionContext;

    @Mock
    private JobDataMap jobDataMap;

    @Mock
    private TraceOrgCommonUtil traceOrgCommonUtil;

    @InjectMocks
    private TraceOrgDatabaseBackup traceOrgDatabaseBackup;

    @Before
    public void setUp()
    {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testExecute_BackupCalledOnce_WhenBackupPathIsProvided() throws Exception
    {
        // Given
        String backupPath = "/path/to/backup";

        when(jobExecutionContext.getMergedJobDataMap()).thenReturn(jobDataMap);

        when(jobDataMap.get("path")).thenReturn(backupPath);

        when(jobDataMap.get(TraceOrgCommonConstants.TRACE_ORG_COMMON_UTIL)).thenReturn(traceOrgCommonUtil);

        // When
        traceOrgDatabaseBackup.execute(jobExecutionContext);

        // Then
        Mockito.verify(traceOrgCommonUtil, times(1)).backup(backupPath);
    }

    @Test
    public void testExecute_BackupNotCalled_WhenBackupPathIsEmpty() throws Exception
    {
        // Given
        String emptyBackupPath = "";

        when(jobExecutionContext.getMergedJobDataMap()).thenReturn(jobDataMap);

        when(jobDataMap.get("path")).thenReturn(emptyBackupPath);

        when(jobDataMap.get(TraceOrgCommonConstants.TRACE_ORG_COMMON_UTIL)).thenReturn(traceOrgCommonUtil);

        // When
        traceOrgDatabaseBackup.execute(jobExecutionContext);

        // Then
        Mockito.verify(traceOrgCommonUtil, never()).backup(anyString());
    }
}
