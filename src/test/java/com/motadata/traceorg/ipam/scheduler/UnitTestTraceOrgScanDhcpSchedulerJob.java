package com.motadata.traceorg.ipam.scheduler;

import com.motadata.traceorg.ipam.entity.dashboard.TraceOrgCategory;
import com.motadata.traceorg.ipam.entity.dhcp.TraceOrgDhcpCredentialDetails;
import com.motadata.traceorg.ipam.entity.event.TraceOrgEvent;
import com.motadata.traceorg.ipam.entity.settings.TraceOrgUser;
import com.motadata.traceorg.ipam.entity.subnet.TraceOrgSubnetDetails;
import com.motadata.traceorg.ipam.scheduler.dhcp.TraceOrgScanDhcpSchedulerJob;
import com.motadata.traceorg.ipam.services.TraceOrgService;
import com.motadata.traceorg.ipam.services.supernet.TraceOrgSupernetService;
import com.motadata.traceorg.ipam.util.TraceOrgCiscoDHCPServerUtil;
import com.motadata.traceorg.ipam.util.TraceOrgCommonConstants;
import com.motadata.traceorg.ipam.util.TraceOrgCommonUtil;
import com.motadata.traceorg.ipam.util.TraceOrgWindowsDhcpServerUtil;
import org.junit.Before;
import org.junit.Test;
import org.mockito.*;
import org.powermock.reflect.Whitebox;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static junit.framework.TestCase.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class UnitTestTraceOrgScanDhcpSchedulerJob
{
    @Mock
    private JobExecutionContext jobExecutionContext;

    @Mock
    private JobDataMap jobDataMap;

    @Mock
    private TraceOrgDhcpCredentialDetails traceOrgDhcpCredentialDetails;

    @Mock
    private TraceOrgCiscoDHCPServerUtil traceOrgCiscoDHCPServerUtil;

    @Mock
    private TraceOrgWindowsDhcpServerUtil traceOrgWindowsDhcpServerUtil;

    @Mock
    private TraceOrgSupernetService traceOrgSupernetService;

    @Mock
    private TraceOrgService traceOrgService;

    @Mock
    private TraceOrgCommonUtil traceOrgCommonUtil;

    @Mock
    ConcurrentHashMap<String,String> m_scanSubnet = new ConcurrentHashMap<>();

    @Mock
    private TraceOrgEvent traceOrgEvent;

    @InjectMocks
    private TraceOrgScanDhcpSchedulerJob traceOrgScanDhcpSchedulerJob;

    @Before
    public void setUp()
    {
        MockitoAnnotations.initMocks(this);
        when(jobExecutionContext.getMergedJobDataMap()).thenReturn(jobDataMap);
        when(jobDataMap.get("traceOrgDhcpCredentialDetails")).thenReturn(traceOrgDhcpCredentialDetails);
        when(jobDataMap.get("traceOrgCiscoDHCPServerUtil")).thenReturn(traceOrgCiscoDHCPServerUtil);
        when(jobDataMap.get("traceOrgWindowsDhcpServerUtil")).thenReturn(traceOrgWindowsDhcpServerUtil);
        when(jobDataMap.get(TraceOrgCommonConstants.TRACE_ORG_SUPERNET_SERVICE)).thenReturn(traceOrgSupernetService);
        when(jobDataMap.get(TraceOrgCommonConstants.TRACE_ORG_SERVICE)).thenReturn(traceOrgService);
        when(jobDataMap.get(TraceOrgCommonConstants.TRACE_ORG_COMMON_UTIL)).thenReturn(traceOrgCommonUtil);

        Whitebox.setInternalState(TraceOrgCommonUtil.class,"m_scanSubnet",m_scanSubnet);
    }

    @Test
    public void testExecute() throws Exception
    {
        List<TraceOrgSubnetDetails> traceOrgSubnetDetails = new ArrayList<>();
        TraceOrgSubnetDetails subnetDetail = new TraceOrgSubnetDetails();
        subnetDetail.setSubnetAddress("192.168.1.0");
        subnetDetail.setSubnetCidr(24);
        subnetDetail.setCreatedBy("Admin");
        traceOrgSubnetDetails.add(subnetDetail);

        when(traceOrgDhcpCredentialDetails.getType()).thenReturn("CISCO");
        when(traceOrgCiscoDHCPServerUtil.discoveryForSubnet(traceOrgDhcpCredentialDetails, traceOrgService)).thenReturn(traceOrgSubnetDetails);
        when(traceOrgService.isExist(anyString(), anyString(), anyString())).thenReturn(false);
        when(traceOrgService.getById(TraceOrgCommonConstants.TRACE_ORG_CATEGORY,1L)).thenReturn(new TraceOrgCategory());

        TraceOrgUser traceOrgUser = new TraceOrgUser();
        traceOrgUser.setUserName("Admin");

        when(traceOrgDhcpCredentialDetails.getCreatedBy()).thenReturn("Admin");

        when(traceOrgService.findByUserName("Admin")).thenReturn(traceOrgUser);

        traceOrgScanDhcpSchedulerJob.execute(jobExecutionContext);

        verify(traceOrgService, times(1)).insert(any(TraceOrgSubnetDetails.class));

        verify(traceOrgSupernetService, times(1)).insertSubnetInSupernetCategory(
                eq("192.168.1.0"),
                eq(24),
                isNull(),
                eq(traceOrgUser),
                eq("by Scanning DHCP Server by Admin")
        );
    }

    @Test
    public void testEventContextBasedOnScanTypeByAdmin() throws Exception
    {
        TraceOrgUser traceOrgUser = new TraceOrgUser();
        traceOrgUser.setUserName("Admin");

        List<TraceOrgSubnetDetails> traceOrgSubnetDetails = new ArrayList<>();
        TraceOrgSubnetDetails subnetDetail = new TraceOrgSubnetDetails();
        subnetDetail.setSubnetAddress("192.168.1.0");
        subnetDetail.setSubnetCidr(24);
        subnetDetail.setCreatedBy("Admin");
        traceOrgSubnetDetails.add(subnetDetail);

        when(traceOrgDhcpCredentialDetails.getCreatedBy()).thenReturn("Admin");
        when(traceOrgService.findByUserName("Admin")).thenReturn(traceOrgUser);
        when(jobDataMap.get(TraceOrgCommonConstants.MANUAL_DHCP_SCAN)).thenReturn(true);
        when(jobDataMap.get(TraceOrgCommonConstants.USER_NAME)).thenReturn("Admin");
        when(jobExecutionContext.getMergedJobDataMap()).thenReturn(jobDataMap);

        when(traceOrgDhcpCredentialDetails.getType()).thenReturn("CISCO");
        when(traceOrgDhcpCredentialDetails.getHostAddress()).thenReturn("192.168.1.0");
        when(traceOrgCiscoDHCPServerUtil.discoveryForSubnet(traceOrgDhcpCredentialDetails, traceOrgService)).thenReturn(traceOrgSubnetDetails);
        when(traceOrgService.isExist(anyString(), anyString(), anyString())).thenReturn(false);
        when(traceOrgService.getById(TraceOrgCommonConstants.TRACE_ORG_CATEGORY,1L)).thenReturn(new TraceOrgCategory());

        traceOrgScanDhcpSchedulerJob.execute(jobExecutionContext);

        ArgumentCaptor<TraceOrgEvent> eventCaptor = ArgumentCaptor.forClass(TraceOrgEvent.class);
        verify(traceOrgService, times(4)).insert(eventCaptor.capture());

        List<TraceOrgEvent> capturedEvents = eventCaptor.getAllValues();
        TraceOrgEvent event = capturedEvents.get(3);

        assertEquals("DHCP Server  192.168.1.0 is scanned in IP Address Manager by Admin", event.getEventContext());
        assertEquals(traceOrgUser, event.getDoneBy());
    }

    @Test
    public void testEventContextBasedOnScanTypeByScheduler() throws Exception
    {
        TraceOrgUser traceOrgUser = new TraceOrgUser();
        traceOrgUser.setUserName("Admin");

        List<TraceOrgSubnetDetails> traceOrgSubnetDetails = new ArrayList<>();
        TraceOrgSubnetDetails subnetDetail = new TraceOrgSubnetDetails();
        subnetDetail.setSubnetAddress("192.168.1.0");
        subnetDetail.setSubnetCidr(24);
        subnetDetail.setCreatedBy("Admin");
        traceOrgSubnetDetails.add(subnetDetail);

        when(traceOrgDhcpCredentialDetails.getCreatedBy()).thenReturn("Admin");
        when(traceOrgService.findByUserName("Admin")).thenReturn(traceOrgUser);
        when(jobDataMap.get(TraceOrgCommonConstants.MANUAL_DHCP_SCAN)).thenReturn(false);
        when(jobExecutionContext.getMergedJobDataMap()).thenReturn(jobDataMap);

        when(traceOrgDhcpCredentialDetails.getType()).thenReturn("CISCO");
        when(traceOrgDhcpCredentialDetails.getHostAddress()).thenReturn("192.168.1.0");
        when(traceOrgCiscoDHCPServerUtil.discoveryForSubnet(traceOrgDhcpCredentialDetails, traceOrgService)).thenReturn(traceOrgSubnetDetails);
        when(traceOrgService.isExist(anyString(), anyString(), anyString())).thenReturn(false);
        when(traceOrgService.getById(TraceOrgCommonConstants.TRACE_ORG_CATEGORY,1L)).thenReturn(new TraceOrgCategory());

        traceOrgScanDhcpSchedulerJob.execute(jobExecutionContext);

        ArgumentCaptor<TraceOrgEvent> eventCaptor = ArgumentCaptor.forClass(TraceOrgEvent.class);
        verify(traceOrgService, times(4)).insert(eventCaptor.capture());

        List<TraceOrgEvent> capturedEvents = eventCaptor.getAllValues();
        TraceOrgEvent event = capturedEvents.get(3);

        assertEquals("DHCP Server  192.168.1.0 is scanned in IP Address Manager by Scheduler", event.getEventContext());
    }
}
