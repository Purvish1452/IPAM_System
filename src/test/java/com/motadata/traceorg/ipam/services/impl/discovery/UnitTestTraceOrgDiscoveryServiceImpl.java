package com.motadata.traceorg.ipam.services.impl.discovery;


import com.motadata.traceorg.ipam.entity.discovery.TraceOrgDiscoveredSubnet;
import com.motadata.traceorg.ipam.entity.discovery.TraceOrgGateway;
import com.motadata.traceorg.ipam.entity.subnet.TraceOrgSubnetDetails;
import com.motadata.traceorg.ipam.repository.dashboard.TraceOrgCategoryRepository;
import com.motadata.traceorg.ipam.repository.discovery.TraceOrgDiscoveredSubnetRepository;
import com.motadata.traceorg.ipam.repository.subnet.TraceOrgSubnetDetailsRepository;
import com.motadata.traceorg.ipam.util.TraceOrgCommonConstants;
import com.motadata.traceorg.ipam.util.TraceOrgCommonUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;


import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;


@RunWith(PowerMockRunner.class)
@PrepareForTest({TraceOrgDiscoveryServiceIml.class, ProcessBuilder.class})
public class UnitTestTraceOrgDiscoveryServiceImpl {

    @InjectMocks
    private TraceOrgDiscoveryServiceIml discoveryService;

    @Mock
    private TraceOrgCategoryRepository traceOrgCategoryRepository;

    @Mock
    private TraceOrgSubnetDetailsRepository traceOrgSubnetDetailsRepository;

    @Mock
    private TraceOrgCommonUtil traceOrgCommonUtil;

    @Mock
    private TraceOrgDiscoveredSubnetRepository traceOrgDiscoveredSubnetRepository;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testGetSubnetMaskLength_ValidSubnetMasks() {
        assertEquals(24, discoveryService.getSubnetMaskLength("255.255.255.0"));
        assertEquals(16, discoveryService.getSubnetMaskLength("255.255.0.0"));
        assertEquals(8, discoveryService.getSubnetMaskLength("255.0.0.0"));
    }


    @Test
    public void testGetSubnetMaskLength_EdgeCases() {
        assertEquals(0, discoveryService.getSubnetMaskLength("0.0.0.0"));
        assertEquals(32, discoveryService.getSubnetMaskLength("255.255.255.255"));
    }

    @Test
    public void testGetSubnets_ValidIpAndSubnetMaskLength() {
        ArrayList<String> subnets = discoveryService.getSubnets("192.168.1.1", 24);
        assertEquals(1, subnets.size());
        assertEquals("192.168.1.0", subnets.get(0));

        subnets = discoveryService.getSubnets("10.0.0.1", 16);
        assertEquals(256, subnets.size());
        assertTrue(subnets.contains("10.0.0.0"));
        assertTrue(subnets.contains("10.0.255.0"));
    }

    @Test
    public void testGetSubnets_InvalidIp() {
        ArrayList<String> subnets = discoveryService.getSubnets("invalid.ip", 24);
        assertTrue(subnets.isEmpty());
    }

    @Test
    public void testGetSubnets_InvalidSubnetMaskLength() {
        ArrayList<String> subnets = discoveryService.getSubnets("192.168.1.1", 33);
        assertTrue(subnets.isEmpty());
    }

    @Test
    public void testGetSubnets_EdgeCases() {
        ArrayList<String> subnets = discoveryService.getSubnets("192.168.1.1", 0);
        assertTrue(subnets.isEmpty());

        subnets = discoveryService.getSubnets("192.168.1.1", 32);
        assertEquals(1, subnets.size());
        assertEquals("192.168.1.1", subnets.get(0));
    }

    @Test
    public void testFilterSubnetsWithIPs_ValidSubnetsAndIPs() {
        List<String> subnets = Arrays.asList("192.168.1.0", "10.0.0.0");
        HashSet<String> ips = new HashSet<>(Arrays.asList("192.168.1.1", "10.0.0.1"));

        ArrayList<String> result = discoveryService.filterSubnetsWithIPs(subnets, ips);
        assertEquals(2, result.size());
        assertTrue(result.contains("192.168.1.0"));
        assertTrue(result.contains("10.0.0.0"));
    }

    @Test
    public void testFilterSubnetsWithIPs_ValidSubnetsAndIPs_2() {
        List<String> subnets = Arrays.asList("192.168.1.0", "11.0.0.0");
        HashSet<String> ips = new HashSet<>(Arrays.asList("192.168.1.1", "10.0.0.1"));

        ArrayList<String> result = discoveryService.filterSubnetsWithIPs(subnets, ips);
        assertEquals(1, result.size());
        assertTrue(result.contains("192.168.1.0"));
        assertFalse(result.contains("10.0.0.0"));
    }


    @Test
    public void testFilterSubnetsWithIPs_EmptySubnets() {
        List<String> subnets = new ArrayList<>();
        HashSet<String> ips = new HashSet<>(Arrays.asList("192.168.1.1", "10.0.0.1"));

        ArrayList<String> result = discoveryService.filterSubnetsWithIPs(subnets, ips);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testFilterSubnetsWithIPs_EmptyIPs() {
        List<String> subnets = Arrays.asList("192.168.1.0", "10.0.0.0");
        HashSet<String> ips = new HashSet<>();

        ArrayList<String> result = discoveryService.filterSubnetsWithIPs(subnets, ips);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testGetDiscoveredSubnets() {
        List<TraceOrgDiscoveredSubnet> subnets = new ArrayList<>();
        when(traceOrgDiscoveredSubnetRepository.findAll()).thenReturn(subnets);

        HashMap<String, Object> result = discoveryService.getDiscoveredSubnets();
        assertTrue((Boolean) result.get(TraceOrgCommonConstants.SUCCESS));
        assertEquals(subnets, result.get(TraceOrgCommonConstants.DATA));
    }

    @Test
    public void testDeleteDiscoveredSubnet() {
        doNothing().when(traceOrgDiscoveredSubnetRepository).delete(anyInt());

        HashMap<String, Object> result = discoveryService.deleteDiscoveredSubnet(1);
        assertTrue((Boolean) result.get(TraceOrgCommonConstants.SUCCESS));
    }

    @Test
    public void testGetDiscoveredSubnet() {
        TraceOrgDiscoveredSubnet subnet = new TraceOrgDiscoveredSubnet();
        subnet.setSubnet("192.168.1.0");
        subnet.setSubnetMask("255.255.255.0");
        when(traceOrgDiscoveredSubnetRepository.findOne(anyInt())).thenReturn(subnet);

        HashMap<String, Object> result = discoveryService.getDiscoveredSubnet(1);
        assertTrue((Boolean) result.get(TraceOrgCommonConstants.SUCCESS));
        assertNotNull(result.get(TraceOrgCommonConstants.DATA));
    }


    @Test
    public void testStatusScanGateway() {
        TraceOrgDiscoveryServiceIml.setIsScanRunning(true);

        HashMap<String, Object> result = discoveryService.statusScanGateway();
        assertTrue((Boolean) result.get(TraceOrgCommonConstants.SUCCESS));
    }


    @Test
    public void testUpdateGatewayStatus() {
        TraceOrgGateway gateway = new TraceOrgGateway();
        gateway.setStatus("oldStatus");

        discoveryService.updateGatewayStatus(gateway, TraceOrgCommonConstants.GATEWAY_RUNNING_STATUS);
        assertEquals(TraceOrgCommonConstants.GATEWAY_RUNNING_STATUS, gateway.getStatus());

        discoveryService.updateGatewayStatus(gateway, TraceOrgCommonConstants.GATEWAY_SUCCESS_STATUS);
        assertEquals(TraceOrgCommonConstants.GATEWAY_SUCCESS_STATUS, gateway.getStatus());
        assertNotNull(gateway.getPreviousScan());
    }

    @Test
    public void testAddDiscoveredSubnets() {
        TraceOrgGateway gateway = new TraceOrgGateway();
        HashMap<String, String> subnets = new HashMap<>();
        subnets.put("192.168.1.0", "255.255.255.0");

        List<TraceOrgDiscoveredSubnet> emptyList = new ArrayList<>();
        when(traceOrgDiscoveredSubnetRepository.findBySubnetAndSubnetMask(anyString(), anyString())).thenReturn(emptyList);

        discoveryService.addDiscoveredSubnets(subnets, gateway);
        verify(traceOrgDiscoveredSubnetRepository, times(1)).save(any(TraceOrgDiscoveredSubnet.class));
    }

    @Test
    public void testAddSubnet() {
        ArrayList<String> subnets = new ArrayList<>();
        subnets.add("192.168.1.0");
        String ipAndSubnetMask = "192.168.1.1" + TraceOrgCommonConstants.VALUE_SEPARATOR + "255.255.255.0";

        TraceOrgSubnetDetails subnetDetails = new TraceOrgSubnetDetails();
        when(traceOrgCategoryRepository.findOne(anyLong())).thenReturn(null);

        discoveryService.addSubnet(subnets, ipAndSubnetMask);
        verify(traceOrgSubnetDetailsRepository, times(1)).save(any(TraceOrgSubnetDetails.class));
    }
}