package com.motadata.traceorg.ipam.scheduler;

import com.motadata.traceorg.ipam.entity.alert.TraceOrgAlertStream;
import com.motadata.traceorg.ipam.entity.dhcp.TraceOrgDhcpCredentialDetails;
import com.motadata.traceorg.ipam.entity.event.TraceOrgEvent;
import com.motadata.traceorg.ipam.entity.settings.TraceOrgUser;
import com.motadata.traceorg.ipam.entity.subnet.TraceOrgSubnetDetails;
import com.motadata.traceorg.ipam.repository.alert.TraceOrgAlertStreamRepository;
import com.motadata.traceorg.ipam.scheduler.subnet.TraceOrgSubnetControllerExecuteJob;
import com.motadata.traceorg.ipam.services.TraceOrgService;
import com.motadata.traceorg.ipam.services.alert.TraceOrgAlertService;
import com.motadata.traceorg.ipam.services.impl.alert.TraceOrgAlertServiceImpl;
import com.motadata.traceorg.ipam.services.settings.TraceOrgAlertConfigureService;
import com.motadata.traceorg.ipam.util.TraceOrgCommonConstants;
import com.motadata.traceorg.ipam.util.TraceOrgCommonUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.*;
import org.powermock.reflect.Whitebox;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static junit.framework.TestCase.assertEquals;
import static org.mockito.Mockito.*;

public class UnitTestTraceOrgSubnetControllerExecuteJob
{

    @Mock
    private TraceOrgService traceOrgService;

    @Mock
    private TraceOrgCommonUtil traceOrgCommonUtil;

    @InjectMocks
    private TraceOrgAlertServiceImpl traceOrgAlertService;

    @Mock
    TraceOrgAlertStreamRepository traceOrgAlertStreamRepository;

    @InjectMocks
    private TraceOrgSubnetControllerExecuteJob traceOrgSubnetControllerExecuteJob;

    @Mock
    TraceOrgAlertConfigureService traceOrgAlertConfigureService;

    @Mock
    private JobExecutionContext jobExecutionContext;

    @Mock
    private JobDataMap jobDataMap;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testInspectAlert_IPUtilizationExceeded() throws Exception
    {
        TraceOrgSubnetDetails traceOrgSubnetDetails = new TraceOrgSubnetDetails();

        traceOrgSubnetDetails.setId(1L);

        traceOrgSubnetDetails.setSubnetAddress("10.20.41.0");

        HashMap<String, Object> context = new HashMap<>();

        context.put("subnetAddress", "10.20.41.0");

        context.put("subnetId", 1L);

        context.put("usedIpPercentage", 12);

        traceOrgSubnetDetails.setUsedIpPercentage(12.54f);

        TraceOrgSubnetDetails updatedTraceOrgSubnetDetails = new TraceOrgSubnetDetails();

        updatedTraceOrgSubnetDetails.setId(1L);

        updatedTraceOrgSubnetDetails.setSubnetAddress("10.20.41.0");

        updatedTraceOrgSubnetDetails.setUsedIpPercentage(20.83f);

        List<TraceOrgAlertStream> alertStreams = new ArrayList<>();

        when(traceOrgService.commonQuery(anyString())).thenReturn((List) alertStreams);

        HashMap<String, String> map = new HashMap<>();

        map.put(TraceOrgCommonConstants.IP_UTILIZATION, "10");

        when(traceOrgAlertConfigureService.getAlertValue(any())).thenReturn("10");

        String mailMessage = TraceOrgCommonConstants.IP_UTILIZATION_MAIL_ALERT_MESSAGE
                .replace(TraceOrgCommonConstants.SUBNET, traceOrgSubnetDetails.getSubnetAddress())
                .replace(TraceOrgCommonConstants.THRESHOLD, "10")
                .replace(TraceOrgCommonConstants.UTILIZATION, "20.83");

        String title = TraceOrgCommonConstants.IP_UTILIZATION_ALERT_TITLE.replace(TraceOrgCommonConstants.SUBNET, traceOrgSubnetDetails.getSubnetAddress());

        traceOrgCommonUtil.sendMail(title,mailMessage);

        Whitebox.invokeMethod(traceOrgAlertService, "ipUtilization", context);

        Mockito.verify(traceOrgCommonUtil, atLeast(1)).sendMail(title, mailMessage);

        Assert.assertEquals("Subnet 10.20.41.0 Utilization Exceeded Threshold", title);

        Assert.assertEquals("Subnet 10.20.41.0 utilization has reached 20.83%, exceeding the threshold of 10% in the IP Address Manager.", mailMessage);
    }

    @Test
    public void testInspectAlert_IPUtilizationDropped() throws Exception
    {
        HashMap<String, Object> context = new HashMap<>();

        context.put("subnetAddress", "10.20.41.0");

        context.put("subnetId", 1L);

        context.put("usedIpPercentage", 12);

        TraceOrgSubnetDetails traceOrgSubnetDetails = new TraceOrgSubnetDetails();

        traceOrgSubnetDetails.setId(1L);

        traceOrgSubnetDetails.setSubnetAddress("10.20.41.0");

        traceOrgSubnetDetails.setUsedIpPercentage(12.54f);

        TraceOrgSubnetDetails updatedTraceOrgSubnetDetails = new TraceOrgSubnetDetails();

        updatedTraceOrgSubnetDetails.setId(1L);

        updatedTraceOrgSubnetDetails.setSubnetAddress("10.20.41.0");

        updatedTraceOrgSubnetDetails.setUsedIpPercentage(5.0f);

        List<String> rogueIps = new ArrayList<>();

        List<TraceOrgAlertStream> alertStreams = new ArrayList<>();

        TraceOrgAlertStream alertStream = new TraceOrgAlertStream();

        alertStream.setSubnet("10.20.41.0");

        alertStream.setAlertType(TraceOrgCommonConstants.IP_UTILIZATION_ALERT_TYPE);

        alertStream.setStatus(Boolean.TRUE);

        alertStreams.add(alertStream);

        when(traceOrgService.commonQuery(anyString())).thenReturn((List) alertStreams);

        HashMap<String, String> map = new HashMap<>();

        map.put(TraceOrgCommonConstants.IP_UTILIZATION, "10");

        when(traceOrgAlertConfigureService.getAlertValue(any())).thenReturn("10");

        String mailMessage = TraceOrgCommonConstants.IP_UTILIZATION_ALERT_CLEAR_MESSAGE
                .replace(TraceOrgCommonConstants.SUBNET, traceOrgSubnetDetails.getSubnetAddress())
                .replace(TraceOrgCommonConstants.THRESHOLD, "10")
                .replace(TraceOrgCommonConstants.UTILIZATION, "5.0");

        String title = TraceOrgCommonConstants.IP_UTILIZATION_ALERT_CLEAR_TITLE.replace(TraceOrgCommonConstants.SUBNET, traceOrgSubnetDetails.getSubnetAddress());

        traceOrgCommonUtil.sendMail(title, mailMessage);

        Whitebox.invokeMethod(traceOrgAlertService, "ipUtilization", context);

        Mockito.verify(traceOrgCommonUtil, atLeast(1)).sendMail(title, mailMessage);

        Assert.assertEquals("Subnet 10.20.41.0 Utilization Alert Cleared for Exceeded Threshold", title);

        Assert.assertEquals("Subnet 10.20.41.0 utilization has dropped below the threshold of 10% and is now at 5.0%.", mailMessage);
    }

    @Test
    public void testInspectAlert_RogueDetection() throws Exception
    {
        TraceOrgSubnetDetails traceOrgSubnetDetails = new TraceOrgSubnetDetails();

        traceOrgSubnetDetails.setId(1L);

        traceOrgSubnetDetails.setSubnetAddress("10.20.41.0");

        traceOrgSubnetDetails.setUsedIpPercentage(12.54f);

        TraceOrgSubnetDetails updatedTraceOrgSubnetDetails = new TraceOrgSubnetDetails();

        updatedTraceOrgSubnetDetails.setId(1L);

        updatedTraceOrgSubnetDetails.setSubnetAddress("10.20.41.0");

        updatedTraceOrgSubnetDetails.setUsedIpPercentage(12.54f);

        List<String> rogueIps = new ArrayList<>();

        rogueIps.add("10.20.41.5");

        HashMap<String, Object> context = new HashMap<>();

        context.put("subnetAddress", "10.20.41.0");

        context.put("subnetId", 1);

        context.put("usedIpPercentage", 12);

        context.put("rogueIps",rogueIps );

        List<TraceOrgAlertStream> alertStreams = new ArrayList<>();

        when(traceOrgAlertStreamRepository.findBySubnetIdAndAlertTypeAndStatus(any(),any(),any())).thenReturn((List) alertStreams);

        HashMap<String, String> map = new HashMap<>();

        map.put(TraceOrgCommonConstants.ROGUE_DETECTION, "true");

        when(traceOrgService.insert(any())).thenReturn(true);

        String mailMessage = TraceOrgCommonConstants.ROGUE_DETECTION_MAIL_ALERT_MESSAGE
                .replace(TraceOrgCommonConstants.SUBNET, traceOrgSubnetDetails.getSubnetAddress());

        String mailBody = null;

        if(mailMessage != null)
        {
            mailBody  = mailMessage + ".<br><br> <table style =\"border: 1px solid\" > <tr> <th style =\"border: 1px solid\">Rogue Ips</th> </tr>";

            for(String rogueIp : rogueIps)
            {
                mailBody = mailBody + "<tr> <td style =\"border: 1px solid\">" + rogueIp + "</td> </tr>";
            }

            mailBody = mailBody + "</table>";
        }

        String title = TraceOrgCommonConstants.ROGUE_DETECTION_ALERT_TITLE.replace(TraceOrgCommonConstants.SUBNET, traceOrgSubnetDetails.getSubnetAddress());

        if(mailBody != null)
        {
            traceOrgCommonUtil.sendMail(title, mailBody);
        }

        Whitebox.invokeMethod(traceOrgAlertService, "rougeDetection", context);

        Mockito.verify(traceOrgCommonUtil, atLeast(1)).sendMail(title, mailBody);

        Assert.assertEquals("Subnet 10.20.41.0 Detected Rogue IP", title);

        Assert.assertEquals("Subnet 10.20.41.0 has following rogue ip detected in the IP Address Manager,.<br><br> <table style =\"border: 1px solid\" > <tr> <th style =\"border: 1px solid\">Rogue Ips</th> </tr><tr> <td style =\"border: 1px solid\">10.20.41.5</td> </tr></table>", mailBody);
    }

    @Test
    public void testInspectAlert_ClearRogueDetection() throws Exception
    {
        TraceOrgSubnetDetails traceOrgSubnetDetails = new TraceOrgSubnetDetails();

        traceOrgSubnetDetails.setId(1L);

        traceOrgSubnetDetails.setSubnetAddress("10.20.41.0");

        traceOrgSubnetDetails.setUsedIpPercentage(12.54f);

        TraceOrgSubnetDetails updatedTraceOrgSubnetDetails = new TraceOrgSubnetDetails();

        updatedTraceOrgSubnetDetails.setId(1L);

        updatedTraceOrgSubnetDetails.setSubnetAddress("10.20.41.0");

        updatedTraceOrgSubnetDetails.setUsedIpPercentage(12.54f);

        List<String> rogueIps = new ArrayList<>();

        HashMap<String, Object> context = new HashMap<>();

        context.put("subnetAddress", "10.20.41.0");

        context.put("subnetId", 1);

        context.put("usedIpPercentage", 12);

        context.put("rogueIps",rogueIps );

        List<TraceOrgAlertStream> alertStreams = new ArrayList<>();

        when(traceOrgService.commonQuery(anyString())).thenReturn((List) alertStreams);

        HashMap<String, String> map = new HashMap<>();

        map.put(TraceOrgCommonConstants.ROGUE_DETECTION, "true");

        when(traceOrgService.insert(any())).thenReturn(true);

        String mailMessage = TraceOrgCommonConstants.ROGUE_DETECTION_ALERT_CLEAR_MESSAGE
                .replace(TraceOrgCommonConstants.SUBNET, traceOrgSubnetDetails.getSubnetAddress());

        String title = TraceOrgCommonConstants.ROGUE_DETECTION_ALERT_CLEAR_TITLE.replace(TraceOrgCommonConstants.SUBNET, traceOrgSubnetDetails.getSubnetAddress());

        traceOrgCommonUtil.sendMail(title, mailMessage);

        Whitebox.invokeMethod(traceOrgAlertService, "rougeDetection", context);

        Mockito.verify(traceOrgCommonUtil, atLeast(1)).sendMail(title, mailMessage);

        Assert.assertEquals("Subnet 10.20.41.0 Rogue IP Alert Cleared", title);

        Assert.assertEquals("Subnet 10.20.41.0 has no rogue ip detected in the IP Address Manager.", mailMessage);
    }

    @Test
    public void testExecuteForDHCPSubnetByAdmin() throws Exception
    {
        TraceOrgDhcpCredentialDetails traceOrgDhcpCredentialDetails = new TraceOrgDhcpCredentialDetails();

        TraceOrgSubnetDetails traceOrgSubnetDetails = new TraceOrgSubnetDetails();
        traceOrgSubnetDetails.setId(1L);
        traceOrgSubnetDetails.setSubnetAddress("192.168.1.0");
        traceOrgSubnetDetails.setCreatedBy(null);
        traceOrgSubnetDetails.setType("Normal");
        traceOrgSubnetDetails.setSubnetName("192.168.1.0 Subnet");
        traceOrgSubnetDetails.setSubnetCidr(24);
        traceOrgSubnetDetails.setTraceOrgDhcpCredentialDetailsId(traceOrgDhcpCredentialDetails);

        TraceOrgUser traceOrgUser = new TraceOrgUser();
        traceOrgUser.setUserName("Admin");

        when(jobExecutionContext.getMergedJobDataMap()).thenReturn(jobDataMap);
        when(jobDataMap.get("subnetDetails")).thenReturn(traceOrgSubnetDetails);
        when(jobDataMap.get(TraceOrgCommonConstants.TRACE_ORG_SERVICE)).thenReturn(traceOrgService);
        when(jobDataMap.get(TraceOrgCommonConstants.TRACE_ORG_COMMON_UTIL)).thenReturn(traceOrgCommonUtil);
        when(jobDataMap.get(TraceOrgCommonConstants.MANUAL_SUBNET_SCAN)).thenReturn(true);
        when(jobDataMap.get(TraceOrgCommonConstants.USER_NAME)).thenReturn("Admin");
        when(traceOrgService.isExist(anyString(), anyString(), anyString())).thenReturn(true);
        when(traceOrgService.findByUserName("Admin")).thenReturn(traceOrgUser);

        traceOrgSubnetControllerExecuteJob.execute(jobExecutionContext);

        ArgumentCaptor<TraceOrgEvent> eventCaptor = ArgumentCaptor.forClass(TraceOrgEvent.class);
        verify(traceOrgService, times(1)).insert(eventCaptor.capture());

        TraceOrgEvent capturedEvent = eventCaptor.getValue();
        assertEquals("Subnet 192.168.1.0 is scanned in IP Address Manager by Admin", capturedEvent.getEventContext());
        assertEquals(traceOrgUser, capturedEvent.getDoneBy());
        assertEquals("Scan Subnet", capturedEvent.getEventType());
        assertEquals(2, capturedEvent.getSeverity());
    }

    @Test
    public void testExecuteForSubnetByAdmin() throws Exception
    {
        TraceOrgSubnetDetails traceOrgSubnetDetails = new TraceOrgSubnetDetails();
        traceOrgSubnetDetails.setId(1L);
        traceOrgSubnetDetails.setSubnetAddress("192.168.1.0");
        traceOrgSubnetDetails.setCreatedBy("Admin");
        traceOrgSubnetDetails.setType("Normal");
        traceOrgSubnetDetails.setSubnetName("192.168.1.0 Subnet");
        traceOrgSubnetDetails.setSubnetCidr(24);

        TraceOrgUser traceOrgUser = new TraceOrgUser();
        traceOrgUser.setUserName("Admin");

        when(jobExecutionContext.getMergedJobDataMap()).thenReturn(jobDataMap);
        when(jobDataMap.get("subnetDetails")).thenReturn(traceOrgSubnetDetails);
        when(jobDataMap.get(TraceOrgCommonConstants.TRACE_ORG_SERVICE)).thenReturn(traceOrgService);
        when(jobDataMap.get(TraceOrgCommonConstants.TRACE_ORG_COMMON_UTIL)).thenReturn(traceOrgCommonUtil);
        when(jobDataMap.get(TraceOrgCommonConstants.MANUAL_SUBNET_SCAN)).thenReturn(true);
        when(jobDataMap.get(TraceOrgCommonConstants.USER_NAME)).thenReturn("Admin");
        when(traceOrgService.isExist(anyString(), anyString(), anyString())).thenReturn(true);
        when(traceOrgService.findByUserName("Admin")).thenReturn(traceOrgUser);

        traceOrgSubnetControllerExecuteJob.execute(jobExecutionContext);

        ArgumentCaptor<TraceOrgEvent> eventCaptor = ArgumentCaptor.forClass(TraceOrgEvent.class);
        verify(traceOrgService, times(1)).insert(eventCaptor.capture());

        TraceOrgEvent capturedEvent = eventCaptor.getValue();
        assertEquals("Subnet 192.168.1.0 is scanned in IP Address Manager by Admin", capturedEvent.getEventContext());
        assertEquals(traceOrgUser, capturedEvent.getDoneBy());
        assertEquals("Scan Subnet", capturedEvent.getEventType());
        assertEquals(2, capturedEvent.getSeverity());
    }

    @Test
    public void testExecuteByScheduler() throws Exception
    {
        TraceOrgSubnetDetails traceOrgSubnetDetails = new TraceOrgSubnetDetails();
        traceOrgSubnetDetails.setId(1L);
        traceOrgSubnetDetails.setSubnetAddress("192.168.1.0");
        traceOrgSubnetDetails.setCreatedBy("Admin");
        traceOrgSubnetDetails.setType("Normal");
        traceOrgSubnetDetails.setSubnetName("192.168.1.0 Subnet");
        traceOrgSubnetDetails.setSubnetCidr(24);

        TraceOrgUser traceOrgUser = new TraceOrgUser();
        traceOrgUser.setUserName("Admin");

        when(jobExecutionContext.getMergedJobDataMap()).thenReturn(jobDataMap);
        when(jobDataMap.get("subnetDetails")).thenReturn(traceOrgSubnetDetails);
        when(jobDataMap.get(TraceOrgCommonConstants.TRACE_ORG_SERVICE)).thenReturn(traceOrgService);
        when(jobDataMap.get(TraceOrgCommonConstants.TRACE_ORG_COMMON_UTIL)).thenReturn(traceOrgCommonUtil);
        when(jobDataMap.get(TraceOrgCommonConstants.MANUAL_SUBNET_SCAN)).thenReturn(false);
        when(traceOrgService.isExist(anyString(), anyString(), anyString())).thenReturn(true);
        when(traceOrgService.findByUserName("Admin")).thenReturn(traceOrgUser);

        traceOrgSubnetControllerExecuteJob.execute(jobExecutionContext);

        ArgumentCaptor<TraceOrgEvent> eventCaptor = ArgumentCaptor.forClass(TraceOrgEvent.class);
        verify(traceOrgService, times(1)).insert(eventCaptor.capture());

        TraceOrgEvent capturedEvent = eventCaptor.getValue();
        assertEquals("Subnet 192.168.1.0 is scanned in IP Address Manager by Scheduler", capturedEvent.getEventContext());
        assertEquals("Scan Subnet", capturedEvent.getEventType());
        assertEquals(2, capturedEvent.getSeverity());
    }
}