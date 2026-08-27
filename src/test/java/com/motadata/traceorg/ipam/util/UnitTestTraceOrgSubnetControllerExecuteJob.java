package com.motadata.traceorg.ipam.util;

import com.motadata.traceorg.ipam.entity.alert.TraceOrgAlertStream;
import com.motadata.traceorg.ipam.entity.subnet.TraceOrgSubnetDetails;
import com.motadata.traceorg.ipam.repository.alert.TraceOrgAlertStreamRepository;
import com.motadata.traceorg.ipam.scheduler.subnet.TraceOrgSubnetControllerExecuteJob;
import com.motadata.traceorg.ipam.services.TraceOrgService;
import com.motadata.traceorg.ipam.services.alert.TraceOrgAlertService;
import com.motadata.traceorg.ipam.services.impl.alert.TraceOrgAlertServiceImpl;
import com.motadata.traceorg.ipam.services.settings.TraceOrgAlertConfigureService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.reflect.Whitebox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
@PrepareForTest({  TraceOrgFactoryUtil.class, Runtime.class, TraceOrgService.class})
@PowerMockIgnore({"javax.manageme3nt.*"})
public class UnitTestTraceOrgSubnetControllerExecuteJob
{
    @Mock
     TraceOrgCommonUtil traceOrgCommonUtil;

    @InjectMocks
    TraceOrgAlertServiceImpl traceOrgAlertService;

    @Mock
    TraceOrgAlertStreamRepository traceOrgAlertStreamRepository;

    @Mock
    TraceOrgAlertConfigureService traceOrgAlertConfigureService;

    @Test
    public void inspectAlertOnClearAlert() throws Exception
    {
        TraceOrgSubnetDetails traceOrgSubnetDetails = new TraceOrgSubnetDetails();

        List<String> rogueIps = new ArrayList<>();

        traceOrgSubnetDetails.setId(1L);

        traceOrgSubnetDetails.setSubnetAddress("10.20.41.0");

        traceOrgSubnetDetails.setUsedIpPercentage(12.54f);

        List<TraceOrgAlertStream> alertStreams = new ArrayList<>();

        TraceOrgAlertStream alertStream = new  TraceOrgAlertStream();

        alertStream.setSubnet("10.20.41.1");

        alertStream.setAlertType(TraceOrgCommonConstants.IP_UTILIZATION_ALERT_TYPE);

        alertStream.setMessage("Subnet 10.20.40.0 utilization has reached 53.91%, exceeding the threshold of 10% in the IP Address Manager.");

        alertStreams.add(alertStream);

        alertStream.setStatus(false);

        HashMap<String, String> map = new HashMap<>();

        map.put(TraceOrgCommonConstants.IP_UTILIZATION, "10");

        HashMap<String, Object> context = new HashMap<>();

        context.put("subnetAddress", "10.20.41.0");

        context.put("subnetId", 1L);

        context.put("usedIpPercentage", 1L);

        context.put("rogueIps",rogueIps );

        when(traceOrgAlertStreamRepository.findBySubnetIdAndAlertTypeAndStatus(any(),any(),any())).thenReturn((List) alertStreams);

        when(traceOrgAlertConfigureService.getAlertValue(any())).thenReturn("10");

        Whitebox.invokeMethod(traceOrgAlertService, "ipUtilization", context);

        ArgumentCaptor<String> argument1 = ArgumentCaptor.forClass(String.class);

        ArgumentCaptor<String> argument2 = ArgumentCaptor.forClass(String.class);

        Mockito.verify(traceOrgCommonUtil,atLeast(1)).sendMail(argument1.capture(),argument2.capture());

        String title =  argument1.getValue();

        String message =  argument2.getValue();

        Assert.assertEquals(title, "Subnet 10.20.41.0 Utilization Alert Cleared for Exceeded Threshold");

        Assert.assertEquals(message, "The alert is now cleared: Subnet 10.20.41.0 utilization has dropped below the threshold of 10% and is now at 1.0% in the IP Address Manager.");
    }

    @Test
    public void inspectAlert() throws Exception
    {
        TraceOrgSubnetDetails traceOrgSubnetDetails = new TraceOrgSubnetDetails();

        List<String> rogueIps = new ArrayList<>();

        traceOrgSubnetDetails.setId(1L);

        traceOrgSubnetDetails.setSubnetAddress("10.20.41.0");

        traceOrgSubnetDetails.setUsedIpPercentage(12.54f);

        traceOrgSubnetDetails.setTotalIp(240L);

        traceOrgSubnetDetails.setUsedIp(50L);

        List<TraceOrgAlertStream> alertStreams = new ArrayList<>();

        TraceOrgAlertStream alertStream = new  TraceOrgAlertStream();

        alertStream.setSubnet("10.20.41.1");

        alertStream.setAlertType(TraceOrgCommonConstants.IP_UTILIZATION_ALERT_TYPE);

        alertStream.setMessage("Subnet 10.20.40.0 utilization has reached 53.91%, exceeding the threshold of 10% in the IP Address Manager.");

        alertStreams.add(alertStream);

        HashMap<String, Object> context = new HashMap<>();

        context.put("subnetAddress", "10.20.41.1");

        context.put("subnetId", 1);

        context.put("usedIpPercentage", 12);

        context.put("rogueIps",rogueIps );

        HashMap<String, String> map = new HashMap<>();

        map.put(TraceOrgCommonConstants.IP_UTILIZATION, "10");


        Whitebox.invokeMethod(traceOrgAlertService, "ipUtilization", context);

        ArgumentCaptor<String> argument1 = ArgumentCaptor.forClass(String.class);

        ArgumentCaptor<String> argument2 = ArgumentCaptor.forClass(String.class);

        Mockito.verify(traceOrgCommonUtil,times(0)).sendMail( argument1.capture(),argument2.capture());
    }

    @Test
    public void inspectAlert_2() throws Exception
    {
        TraceOrgSubnetDetails traceOrgSubnetDetails = new TraceOrgSubnetDetails();

        List<String> rogueIps = new ArrayList<>();

        traceOrgSubnetDetails.setId(1L);

        traceOrgSubnetDetails.setSubnetAddress("10.20.41.0");

        traceOrgSubnetDetails.setUsedIpPercentage(12.54f);

        traceOrgSubnetDetails.setTotalIp(240L);

        traceOrgSubnetDetails.setUsedIp(50L);

        List<TraceOrgAlertStream> alertStreams = new ArrayList<>();

        HashMap<String, String> map = new HashMap<>();

        map.put(TraceOrgCommonConstants.IP_UTILIZATION, "10");

        when(traceOrgAlertConfigureService.getAlertValue(any())).thenReturn("10");

        HashMap<String, Object> context = new HashMap<>();

        context.put("subnetAddress", "10.20.41.0");

        context.put("subnetId", 1L);

        context.put("usedIpPercentage", 12);

        context.put("rogueIps",rogueIps );

        Whitebox.invokeMethod(traceOrgAlertService, "ipUtilization", context);

        ArgumentCaptor<String> argument1 = ArgumentCaptor.forClass(String.class);

        ArgumentCaptor<String> argument2 = ArgumentCaptor.forClass(String.class);

        Mockito.verify(traceOrgCommonUtil,atLeast(1)).sendMail(argument1.capture(),argument2.capture());

        String title =  argument1.getValue();

        String message =  argument2.getValue();

        Assert.assertEquals(title, "Subnet 10.20.41.0 Utilization Exceeded Threshold");

        Assert.assertEquals(message, "Subnet 10.20.41.0 utilization has reached 12.0%, exceeding the threshold of 10% in the IP Address Manager.");
    }
}
