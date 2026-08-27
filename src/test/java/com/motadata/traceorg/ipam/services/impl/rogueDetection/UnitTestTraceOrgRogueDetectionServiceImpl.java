package com.motadata.traceorg.ipam.services.impl.rogueDetection;

import com.motadata.traceorg.ipam.entity.rogueDetection.TraceOrgRogueDetection;
import com.motadata.traceorg.ipam.entity.settings.TraceOrgUser;
import com.motadata.traceorg.ipam.entity.subnet.TraceOrgSubnetIpDetails;
import com.motadata.traceorg.ipam.repository.rogueDetection.TraceOrgRogueDetectionRepository;
import com.motadata.traceorg.ipam.repository.subnet.TraceOrgSubnetIpDetailsRepository;
import com.motadata.traceorg.ipam.services.TraceOrgService;
import com.motadata.traceorg.ipam.util.TraceOrgCommonConstants;
import com.motadata.traceorg.ipam.util.TraceOrgCommonUtil;
import com.motadata.traceorg.ipam.util.TraceOrgMessageConstants;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.*;
import java.util.*;

import static org.mockito.Mockito.*;

public class UnitTestTraceOrgRogueDetectionServiceImpl
{

    @Mock
    private TraceOrgRogueDetectionRepository traceOrgRogueDetectionRepository;

    @Mock
    private TraceOrgSubnetIpDetailsRepository traceOrgSubnetIpDetailsRepository;

    @Mock
    private TraceOrgService traceOrgService;

    @Mock
    private TraceOrgCommonUtil traceOrgCommonUtil;

    @Mock
    private TraceOrgUser traceOrgUser;

    @Mock
    private TraceOrgRogueDetection traceOrgRogueDetection;

    @Mock
    private TraceOrgSubnetIpDetails traceOrgSubnetIpDetails;

    @InjectMocks
    private TraceOrgRogueDetectionServiceImpl traceOrgRogueDetectionService;

    @Before
    public void setUp()
    {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testLoadRogueDetectionDetails()
    {
        List<TraceOrgRogueDetection> rogueDetections = new ArrayList<>();

        rogueDetections.add(new TraceOrgRogueDetection());

        when(traceOrgRogueDetectionRepository.findAll()).thenReturn(rogueDetections);

        HashMap<String, Object> result = traceOrgRogueDetectionService.loadRogueDetectionDetails();

        Assert.assertEquals(TraceOrgCommonConstants.TRUE, result.get(TraceOrgCommonConstants.SUCCESS));

        Assert.assertEquals(rogueDetections, result.get(TraceOrgCommonConstants.DATA));
    }

    @Test
    public void testLoadIndividualRogueDetectionDetails()
    {
        List<TraceOrgRogueDetection> rogueDetections = new ArrayList<>();

        rogueDetections.add(new TraceOrgRogueDetection());

        when(traceOrgRogueDetectionRepository.findByAuthenticity(anyString())).thenReturn(rogueDetections);

        HashMap<String, Object> result = traceOrgRogueDetectionService.loadIndividualRogueDetectionDetails("trusted");

        Assert.assertEquals(TraceOrgCommonConstants.TRUE, result.get(TraceOrgCommonConstants.SUCCESS));

        Assert.assertEquals(rogueDetections, result.get(TraceOrgCommonConstants.DATA));
    }

    @Test
    public void testMarkedAuthenticityOfMAC()
    {
        when(traceOrgRogueDetectionRepository.findOne(anyLong())).thenReturn(new TraceOrgRogueDetection());

        when(traceOrgSubnetIpDetailsRepository.findByMacAddressAndIpAddress(anyString(), anyString())).thenReturn(traceOrgSubnetIpDetails);

        when(traceOrgService.insert(any())).thenReturn(true);

        when(traceOrgCommonUtil.currentUser("token")).thenReturn(traceOrgUser);

        HashMap<String, Object> result = traceOrgRogueDetectionService.markedAuthenticityOfMAC("1", true, "token");

        Assert.assertEquals(TraceOrgCommonConstants.TRUE, result.get(TraceOrgCommonConstants.SUCCESS));

        Assert.assertEquals(TraceOrgMessageConstants.SUBNET_IP_ROGUE_SUCCESS, result.get(TraceOrgCommonConstants.MESSAGE));
    }

    @Test
    public void testDeleteMACAddressesWhenAuthenticityNonDiscovered()
    {
        when(traceOrgRogueDetectionRepository.findOne(anyLong())).thenReturn(traceOrgRogueDetection);

        when(traceOrgRogueDetection.getAuthenticity()).thenReturn("rogue");

        when(traceOrgSubnetIpDetailsRepository.findByMacAddressAndIpAddress(anyString(), anyString())).thenReturn(traceOrgSubnetIpDetails);

        doNothing().when(traceOrgRogueDetectionRepository).delete(anyLong());

        HashMap<String, Object> result = traceOrgRogueDetectionService.deleteMACAddresses("1");

        Assert.assertEquals(TraceOrgCommonConstants.TRUE, result.get(TraceOrgCommonConstants.SUCCESS));

        Assert.assertEquals(TraceOrgMessageConstants.DELETE_MAC_ADDRESSES_NON_DISCOVERED_AUTHENTICITY_SUCCESS, result.get(TraceOrgCommonConstants.MESSAGE));
    }

    @Test
    public void testDeleteMACAddressesWhenAuthenticityDiscovered()
    {
        when(traceOrgRogueDetectionRepository.findOne(anyLong())).thenReturn(traceOrgRogueDetection);

        when(traceOrgRogueDetection.getAuthenticity()).thenReturn("discovered");

        when(traceOrgSubnetIpDetailsRepository.findByMacAddressAndIpAddress(anyString(), anyString())).thenReturn(traceOrgSubnetIpDetails);

        doNothing().when(traceOrgRogueDetectionRepository).delete(anyLong());

        HashMap<String, Object> result = traceOrgRogueDetectionService.deleteMACAddresses("1");

        Assert.assertEquals(TraceOrgCommonConstants.TRUE, result.get(TraceOrgCommonConstants.SUCCESS));

        Assert.assertEquals(TraceOrgMessageConstants.DELETE_MAC_ADDRESSES_SUCCESS, result.get(TraceOrgCommonConstants.MESSAGE));
    }

    @Test
    public void testExportRogueDetectionDetails()
    {
        List<TraceOrgRogueDetection> rogueDetections = new ArrayList<>();

        rogueDetections.add(traceOrgRogueDetection);

        when(traceOrgRogueDetectionRepository.findAll()).thenReturn(rogueDetections);

        when(traceOrgRogueDetection.getAuthenticity()).thenReturn("discovered");

        HashMap<String, Object> result = traceOrgRogueDetectionService.exportRogueDetectionDetails(TraceOrgCommonConstants.ALL_ROGUE_DETECTION_DETAILS_EXPORT, TraceOrgCommonConstants.EXPORT_PDF, null, TraceOrgCommonConstants.ROGUE_DETECTION_DETAILS);

        Assert.assertEquals(TraceOrgCommonConstants.TRUE, result.get(TraceOrgCommonConstants.SUCCESS));

        Assert.assertNotNull(result.get(TraceOrgCommonConstants.DATA));

        Assert.assertEquals(TraceOrgMessageConstants.EXPORT_ROGUE_DETECTION_SUCCESS, result.get(TraceOrgCommonConstants.MESSAGE));
    }
}