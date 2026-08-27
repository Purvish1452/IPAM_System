package com.motadata.traceorg.ipam.util;

import com.motadata.traceorg.ipam.entity.rogueDetection.TraceOrgRogueDetection;
import com.motadata.traceorg.ipam.entity.subnet.TraceOrgSubnetDetails;
import com.motadata.traceorg.ipam.entity.subnet.TraceOrgSubnetIpDetails;
import com.motadata.traceorg.ipam.repository.rogueDetection.TraceOrgRogueDetectionRepository;
import org.junit.Before;
import org.junit.Test;
import org.mockito.*;
import org.powermock.reflect.Whitebox;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;

public class UnitTestIPAM145TraceOrgSubnetUtil
{

    @InjectMocks
    private TraceOrgSubnetUtil traceOrgSubnetUtil;

    @Mock
    private TraceOrgSubnetDetails traceOrgSubnetDetails;

    @Mock
    private TraceOrgCommonUtil traceOrgCommonUtil;

    @Mock
    private TraceOrgRogueDetectionRepository traceOrgRogueDetectionRepository;

    @Before
    public void setUp()
    {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testSetAuthenticity_NewMacAddress() throws Exception
    {
        TraceOrgSubnetIpDetails traceOrgSubnetIpDetailsExisted = new TraceOrgSubnetIpDetails();

        traceOrgSubnetIpDetailsExisted.setPreviousMacAddress("00:11:22:33:44:55");

        traceOrgSubnetIpDetailsExisted.setMacAddress("00:11:22:33:44:66");

        traceOrgSubnetIpDetailsExisted.setIpAddress("192.168.1.1");

        TraceOrgSubnetIpDetails traceOrgSubnetIpDetails = new TraceOrgSubnetIpDetails();

        traceOrgSubnetIpDetails.setMacAddress("00:11:22:33:44:66");

        traceOrgSubnetIpDetails.setIpAddress("192.168.1.1");

        traceOrgSubnetIpDetails.setLastAliveTime(new java.util.Date());

        traceOrgSubnetIpDetails.setDeviceType("Device Type");

        List<String> rogueIps = new ArrayList<>();

        when(traceOrgRogueDetectionRepository.findByMacAddressAndIpAddress(anyString(), anyString())).thenReturn(null);

        Whitebox.invokeMethod(traceOrgSubnetUtil, "setAuthenticity",
                (TraceOrgRogueDetectionRepository) traceOrgRogueDetectionRepository,
                (TraceOrgSubnetIpDetails) traceOrgSubnetIpDetailsExisted,
                (TraceOrgSubnetIpDetails) traceOrgSubnetIpDetails,
                (ArrayList<String>) rogueIps,
                (TraceOrgCommonUtil) traceOrgCommonUtil,
                (TraceOrgSubnetDetails) traceOrgSubnetDetails);

        verify(traceOrgRogueDetectionRepository, times(1)).save(any(TraceOrgRogueDetection.class));

        assert(traceOrgSubnetIpDetails.getAuthenticity().equals("discovered"));

        assert(traceOrgSubnetIpDetailsExisted.getAuthenticity().equals("discovered"));
    }

    @Test
    public void testSetAuthenticity_ExistingRogueMacAddress() throws Exception
    {
        TraceOrgSubnetIpDetails traceOrgSubnetIpDetailsExisted = new TraceOrgSubnetIpDetails();

        traceOrgSubnetIpDetailsExisted.setMacAddress("00:11:22:33:44:55");

        traceOrgSubnetIpDetailsExisted.setIpAddress("192.168.1.1");

        TraceOrgSubnetIpDetails traceOrgSubnetIpDetails = new TraceOrgSubnetIpDetails();

        traceOrgSubnetIpDetails.setMacAddress("00:11:22:33:44:55");

        traceOrgSubnetIpDetails.setIpAddress("192.168.1.1");

        List<String> rogueIps = new ArrayList<>();

        TraceOrgRogueDetection traceOrgRogueDetection = new TraceOrgRogueDetection();

        traceOrgRogueDetection.setAuthenticity("rogue");

        when(traceOrgRogueDetectionRepository.findByMacAddressAndIpAddress(anyString(), anyString())).thenReturn(traceOrgRogueDetection);

        Whitebox.invokeMethod(traceOrgSubnetUtil, "setAuthenticity",
                (TraceOrgRogueDetectionRepository) traceOrgRogueDetectionRepository,
                (TraceOrgSubnetIpDetails) traceOrgSubnetIpDetailsExisted,
                (TraceOrgSubnetIpDetails) traceOrgSubnetIpDetails,
                (ArrayList<String>) rogueIps,
                (TraceOrgCommonUtil) traceOrgCommonUtil,
                (TraceOrgSubnetDetails) traceOrgSubnetDetails);

        assert(rogueIps.contains("192.168.1.1"));

        assert(traceOrgSubnetIpDetails.getAuthenticity().equals("rogue"));

        assert(traceOrgSubnetIpDetailsExisted.getAuthenticity().equals("rogue"));
    }

    @Test
    public void testSetAuthenticity_ExistingTrustedMacAddress() throws Exception
    {
        TraceOrgSubnetIpDetails traceOrgSubnetIpDetailsExisted = new TraceOrgSubnetIpDetails();

        traceOrgSubnetIpDetailsExisted.setMacAddress("00:11:22:33:44:55");

        traceOrgSubnetIpDetailsExisted.setIpAddress("192.168.1.1");

        TraceOrgSubnetIpDetails traceOrgSubnetIpDetails = new TraceOrgSubnetIpDetails();

        traceOrgSubnetIpDetails.setMacAddress("00:11:22:33:44:55");

        traceOrgSubnetIpDetails.setIpAddress("192.168.1.1");

        List<String> rogueIps = new ArrayList<>();

        TraceOrgRogueDetection traceOrgRogueDetection = new TraceOrgRogueDetection();

        traceOrgRogueDetection.setAuthenticity("trusted");

        when(traceOrgRogueDetectionRepository.findByMacAddressAndIpAddress(anyString(), anyString())).thenReturn(traceOrgRogueDetection);

        Whitebox.invokeMethod(traceOrgSubnetUtil, "setAuthenticity",
                (TraceOrgRogueDetectionRepository) traceOrgRogueDetectionRepository,
                (TraceOrgSubnetIpDetails) traceOrgSubnetIpDetailsExisted,
                (TraceOrgSubnetIpDetails) traceOrgSubnetIpDetails,
                (ArrayList<String>) rogueIps,
                (TraceOrgCommonUtil) traceOrgCommonUtil,
                (TraceOrgSubnetDetails) traceOrgSubnetDetails);

        assert(traceOrgSubnetIpDetails.getAuthenticity().equals("trusted"));

        assert(traceOrgSubnetIpDetailsExisted.getAuthenticity().equals("trusted"));
    }

    @Test
    public void testSetAuthenticity_NoMacAddress() throws Exception
    {
        TraceOrgSubnetIpDetails traceOrgSubnetIpDetailsExisted = new TraceOrgSubnetIpDetails();

        traceOrgSubnetIpDetailsExisted.setPreviousMacAddress("00:11:22:33:44:55");

        traceOrgSubnetIpDetailsExisted.setIpAddress("192.168.1.1");

        TraceOrgSubnetIpDetails traceOrgSubnetIpDetails = new TraceOrgSubnetIpDetails();

        traceOrgSubnetIpDetails.setIpAddress("192.168.1.1");

        List<String> rogueIps = new ArrayList<>();

        TraceOrgRogueDetection traceOrgRogueDetection = new TraceOrgRogueDetection();

        when(traceOrgRogueDetectionRepository.findByMacAddressAndIpAddress(anyString(), anyString())).thenReturn(traceOrgRogueDetection);

        Whitebox.invokeMethod(traceOrgSubnetUtil, "setAuthenticity",
                (TraceOrgRogueDetectionRepository) traceOrgRogueDetectionRepository,
                (TraceOrgSubnetIpDetails) traceOrgSubnetIpDetailsExisted,
                (TraceOrgSubnetIpDetails) traceOrgSubnetIpDetails,
                (ArrayList<String>) rogueIps,
                (TraceOrgCommonUtil) traceOrgCommonUtil,
                (TraceOrgSubnetDetails) traceOrgSubnetDetails);

        verify(traceOrgRogueDetectionRepository, times(1)).delete(anyLong());

        assert(traceOrgSubnetIpDetails.getAuthenticity().equals("-"));

        assert(traceOrgSubnetIpDetailsExisted.getAuthenticity().equals("-"));
    }
}
