package com.motadata.traceorg.ipam.util;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;


import com.motadata.traceorg.ipam.entity.subnet.TraceOrgIPChangeLog;
import com.motadata.traceorg.ipam.entity.subnet.TraceOrgSubnetDetails;
import com.motadata.traceorg.ipam.entity.subnet.TraceOrgSubnetIpDetails;
import com.motadata.traceorg.ipam.repository.rogueDetection.TraceOrgRogueDetectionRepository;
import com.motadata.traceorg.ipam.services.TraceOrgService;
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

@RunWith(MockitoJUnitRunner.class)
@PrepareForTest({  TraceOrgFactoryUtil.class, Runtime.class, TraceOrgService.class})
@PowerMockIgnore({"javax.manageme3nt.*"})
public class UnitTestTraceOrgSubnetUtil
{

    @Mock
    private TraceOrgService traceOrgService;

    @Mock
    TraceOrgCommonUtil traceOrgCommonUtil;

    @Mock
    private TraceOrgRogueDetectionRepository traceOrgRogueDetectionRepository;

    @InjectMocks
    private TraceOrgSubnetIpDetails traceOrgSubnetIpDetails;

    @InjectMocks
    TraceOrgSubnetUtil traceOrgSubnetUtil;

    @Test
    public void testInsertSubnetIp_1() {

        List<TraceOrgSubnetIpDetails> existingDetailsList = new ArrayList<>();
        List<String> rogueIps = new ArrayList<>();
        traceOrgSubnetIpDetails = new TraceOrgSubnetIpDetails();
        traceOrgSubnetIpDetails.setIpAddress("192.168.1.1");
        traceOrgSubnetIpDetails.setId(11L);
        TraceOrgSubnetDetails traceOrgSubnetDetails = new TraceOrgSubnetDetails();
        traceOrgSubnetDetails.setId(1L);
        traceOrgSubnetIpDetails.setSubnetId(traceOrgSubnetDetails);
        traceOrgSubnetIpDetails.setMacAddress("00:11:22:33:44:55");
        traceOrgSubnetIpDetails.setStatus(TraceOrgCommonConstants.USED);
        traceOrgSubnetIpDetails.setPreviousStatus(TraceOrgCommonConstants.AVAILABLE);
        traceOrgSubnetIpDetails.setLastAliveTime(new Date());
        existingDetailsList.add(traceOrgSubnetIpDetails);

        when(traceOrgService.commonQuery(anyString())).thenReturn((List) existingDetailsList);

        when(traceOrgService.insert(any(TraceOrgSubnetIpDetails.class))).thenReturn(true);

        TraceOrgSubnetIpDetails traceOrgSubnetIpDetails1 = new TraceOrgSubnetIpDetails();
        traceOrgSubnetIpDetails1.setIpAddress("192.168.1.1");
        traceOrgSubnetIpDetails1.setSubnetId(new TraceOrgSubnetDetails());
        traceOrgSubnetIpDetails1.setMacAddress("00:11:22:33:44:55");
        traceOrgSubnetIpDetails1.setStatus(TraceOrgCommonConstants.AVAILABLE);
        traceOrgSubnetIpDetails1.setDeviceType("Device Type");
        traceOrgSubnetIpDetails1.setLastAliveTime(new Date());

        boolean result = traceOrgSubnetUtil.insertSubnetIp(traceOrgSubnetIpDetails1, traceOrgService,traceOrgRogueDetectionRepository,rogueIps, traceOrgSubnetDetails, traceOrgCommonUtil);

        Assert.assertTrue(result);

        ArgumentCaptor<Object> argument = ArgumentCaptor.forClass(Object.class);

        Mockito.verify(traceOrgService,atLeast(1)).insert(argument.capture());

        TraceOrgIPChangeLog changeLog = (TraceOrgIPChangeLog) argument.getValue();

        Assert.assertEquals(changeLog.getChangelog(),"Status: Used -> Transient");

        Assert.assertEquals(changeLog.getUser(),"System");

        Assert.assertEquals(changeLog.getIp(), "192.168.1.1");

        Assert.assertNotNull(changeLog.getTimestamp());
    }

    @Test
    public void testInsertSubnetIp_2() {

        List<TraceOrgSubnetIpDetails> existingDetailsList = new ArrayList<>();
        List<String> rogueIps = new ArrayList<>();
        traceOrgSubnetIpDetails = new TraceOrgSubnetIpDetails();
        traceOrgSubnetIpDetails.setIpAddress("192.168.1.1");
        traceOrgSubnetIpDetails.setId(11L);
        TraceOrgSubnetDetails traceOrgSubnetDetails = new TraceOrgSubnetDetails();
        traceOrgSubnetDetails.setId(1L);
        traceOrgSubnetIpDetails.setSubnetId(traceOrgSubnetDetails);
        traceOrgSubnetIpDetails.setMacAddress("00:11:22:33:44:55");
        traceOrgSubnetIpDetails.setStatus(TraceOrgCommonConstants.AVAILABLE);
        traceOrgSubnetIpDetails.setPreviousStatus(TraceOrgCommonConstants.AVAILABLE);
        traceOrgSubnetIpDetails.setLastAliveTime(new Date());
        existingDetailsList.add(traceOrgSubnetIpDetails);

        when(traceOrgService.commonQuery(anyString())).thenReturn((List) existingDetailsList);

        when(traceOrgService.insert(any(TraceOrgSubnetIpDetails.class))).thenReturn(true);

        TraceOrgSubnetIpDetails traceOrgSubnetIpDetails1 = new TraceOrgSubnetIpDetails();
        traceOrgSubnetIpDetails1.setIpAddress("192.168.1.1");
        traceOrgSubnetIpDetails1.setSubnetId(new TraceOrgSubnetDetails());
        traceOrgSubnetIpDetails1.setMacAddress("00:11:22:33:44:55");
        traceOrgSubnetIpDetails1.setStatus(TraceOrgCommonConstants.USED);
        traceOrgSubnetIpDetails1.setDeviceType("Device Type");
        traceOrgSubnetIpDetails1.setLastAliveTime(new Date());

        boolean result = traceOrgSubnetUtil.insertSubnetIp(traceOrgSubnetIpDetails1, traceOrgService,traceOrgRogueDetectionRepository,rogueIps, traceOrgSubnetDetails, traceOrgCommonUtil);

        Assert.assertTrue(result);

        ArgumentCaptor<Object> argument = ArgumentCaptor.forClass(Object.class);

        Mockito.verify(traceOrgService,atLeast(1)).insert(argument.capture());

        TraceOrgIPChangeLog changeLog = (TraceOrgIPChangeLog) argument.getValue();

        Assert.assertEquals(changeLog.getChangelog(),"Status: Available -> Used");

        Assert.assertEquals(changeLog.getUser(),"System");

        Assert.assertEquals(changeLog.getIp(), "192.168.1.1");

        Assert.assertNotNull(changeLog.getTimestamp());
    }

    @Test
    public void testInsertSubnetIp_3() {

        List<TraceOrgSubnetIpDetails> existingDetailsList = new ArrayList<>();
        List<String> rogueIps = new ArrayList<>();
        traceOrgSubnetIpDetails = new TraceOrgSubnetIpDetails();
        traceOrgSubnetIpDetails.setIpAddress("192.168.1.1");
        traceOrgSubnetIpDetails.setId(11L);
        TraceOrgSubnetDetails traceOrgSubnetDetails = new TraceOrgSubnetDetails();
        traceOrgSubnetDetails.setId(1L);
        traceOrgSubnetIpDetails.setSubnetId(traceOrgSubnetDetails);
        traceOrgSubnetIpDetails.setMacAddress("00:11:22:33:44:55");
        traceOrgSubnetIpDetails.setStatus(TraceOrgCommonConstants.RESERVED);
        traceOrgSubnetIpDetails.setPreviousStatus(TraceOrgCommonConstants.AVAILABLE);
        traceOrgSubnetIpDetails.setLastAliveTime(new Date());
        existingDetailsList.add(traceOrgSubnetIpDetails);

        when(traceOrgService.commonQuery(anyString())).thenReturn((List) existingDetailsList);

        when(traceOrgService.insert(any(TraceOrgSubnetIpDetails.class))).thenReturn(true);

        TraceOrgSubnetIpDetails traceOrgSubnetIpDetails1 = new TraceOrgSubnetIpDetails();
        traceOrgSubnetIpDetails1.setIpAddress("192.168.1.1");
        traceOrgSubnetIpDetails1.setSubnetId(new TraceOrgSubnetDetails());
        traceOrgSubnetIpDetails1.setMacAddress("00:11:22:33:44:55");
        traceOrgSubnetIpDetails1.setStatus(TraceOrgCommonConstants.USED);
        traceOrgSubnetIpDetails1.setDeviceType("Device Type");
        traceOrgSubnetIpDetails1.setLastAliveTime(new Date());

        boolean result = traceOrgSubnetUtil.insertSubnetIp(traceOrgSubnetIpDetails1, traceOrgService,traceOrgRogueDetectionRepository,rogueIps, traceOrgSubnetDetails, traceOrgCommonUtil);

        Assert.assertTrue(result);

        ArgumentCaptor<Object> argument = ArgumentCaptor.forClass(Object.class);

        Mockito.verify(traceOrgService,atLeast(1)).insert(argument.capture());

        TraceOrgIPChangeLog changeLog = (TraceOrgIPChangeLog) argument.getValue();

        Assert.assertEquals(changeLog.getChangelog(),"Status: Reserved -> Used");

        Assert.assertEquals(changeLog.getUser(),"System");

        Assert.assertEquals(changeLog.getIp(), "192.168.1.1");

        Assert.assertNotNull(changeLog.getTimestamp());
    }

    @Test
    public void testInsertSubnetIp_4() {

        List<TraceOrgSubnetIpDetails> existingDetailsList = new ArrayList<>();
        List<String> rogueIps = new ArrayList<>();
        traceOrgSubnetIpDetails = new TraceOrgSubnetIpDetails();
        traceOrgSubnetIpDetails.setIpAddress("192.168.1.1");
        traceOrgSubnetIpDetails.setId(11L);
        TraceOrgSubnetDetails traceOrgSubnetDetails = new TraceOrgSubnetDetails();
        traceOrgSubnetDetails.setId(1L);
        traceOrgSubnetIpDetails.setSubnetId(traceOrgSubnetDetails);
        traceOrgSubnetIpDetails.setMacAddress("00:11:22:33:44:55");
        traceOrgSubnetIpDetails.setStatus(TraceOrgCommonConstants.AVAILABLE);
        traceOrgSubnetIpDetails.setPreviousStatus(TraceOrgCommonConstants.AVAILABLE);
        traceOrgSubnetIpDetails.setLastAliveTime(new Date());
        existingDetailsList.add(traceOrgSubnetIpDetails);

        when(traceOrgService.commonQuery(anyString())).thenReturn((List) existingDetailsList);

        when(traceOrgService.insert(any(TraceOrgSubnetIpDetails.class))).thenReturn(true);

        TraceOrgSubnetIpDetails traceOrgSubnetIpDetails1 = new TraceOrgSubnetIpDetails();
        traceOrgSubnetIpDetails1.setIpAddress("192.168.1.1");
        traceOrgSubnetIpDetails1.setSubnetId(new TraceOrgSubnetDetails());
        traceOrgSubnetIpDetails1.setMacAddress("00:11:22:33:44:55");
        traceOrgSubnetIpDetails1.setStatus(TraceOrgCommonConstants.RESERVED);
        traceOrgSubnetIpDetails1.setDeviceType("Device Type");
        traceOrgSubnetIpDetails1.setLastAliveTime(new Date());

        boolean result = traceOrgSubnetUtil.insertSubnetIp(traceOrgSubnetIpDetails1, traceOrgService, traceOrgRogueDetectionRepository, rogueIps, traceOrgSubnetDetails, traceOrgCommonUtil);

        Assert.assertTrue(result);

        ArgumentCaptor<Object> argument = ArgumentCaptor.forClass(Object.class);

        Mockito.verify(traceOrgService,atLeast(1)).insert(argument.capture());

        TraceOrgIPChangeLog changeLog = (TraceOrgIPChangeLog) argument.getValue();

        Assert.assertEquals(changeLog.getChangelog(),"Status: Available -> Reserved");

        Assert.assertEquals(changeLog.getUser(),"System");

        Assert.assertEquals(changeLog.getIp(), "192.168.1.1");

        Assert.assertNotNull(changeLog.getTimestamp());
    }

    @Test
    public void testUpdateStatusFromPing_1()
    {

        HashMap<String, Object> map = new HashMap<>();

        ArrayList<String> upIps =  new ArrayList<>();

        upIps.add("172.16.12.8");

        ArrayList<String> downIps =  new ArrayList<>();

        downIps.add("172.16.12.9");

        map.put("up", upIps);

        map.put("down", downIps);

        ArrayList<String> usedIps = new ArrayList<>();

        List<TraceOrgSubnetIpDetails> updateIpDetailList = new ArrayList<>();

        traceOrgSubnetUtil.updateStatusFromPing(map,usedIps, new TraceOrgSubnetDetails(),updateIpDetailList, traceOrgCommonUtil);

        Assert.assertEquals(usedIps.size(),1);

        Assert.assertEquals(updateIpDetailList.size(),1);

        Assert.assertEquals(updateIpDetailList.get(0).getIpAddress(),"172.16.12.8");
    }

    @Test
    public void testUpdateStatusFromPing_2()
    {

        HashMap<String, Object> map = new HashMap<>();

        ArrayList<String> upIps =  new ArrayList<>();

        ArrayList<String> downIps =  new ArrayList<>();

        downIps.add("172.16.12.9");

        map.put("up", upIps);

        map.put("down", downIps);

        ArrayList<String> usedIps = new ArrayList<>();

        List<TraceOrgSubnetIpDetails> updateIpDetailList = new ArrayList<>();

        traceOrgSubnetUtil.updateStatusFromPing(map,usedIps, new TraceOrgSubnetDetails(),updateIpDetailList, traceOrgCommonUtil);

        Assert.assertEquals(usedIps.size(),0);

        Assert.assertEquals(updateIpDetailList.size(),0);
    }
}
