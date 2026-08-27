package com.motadata.traceorg.ipam.services.impl.subnet;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import com.motadata.traceorg.ipam.entity.dashboard.TraceOrgCategory;
import com.motadata.traceorg.ipam.entity.rogueDetection.TraceOrgRogueDetection;
import com.motadata.traceorg.ipam.entity.subnet.TraceOrgSubnetDetails;
import com.motadata.traceorg.ipam.repository.dashboard.TraceOrgCategoryRepository;
import com.motadata.traceorg.ipam.repository.rogueDetection.TraceOrgRogueDetectionRepository;
import com.motadata.traceorg.ipam.repository.subnet.TraceOrgSubnetDetailsRepository;
import com.motadata.traceorg.ipam.repository.subnet.TraceOrgSubnetIpDetailsRepository;
import com.motadata.traceorg.ipam.services.impl.discovery.TraceOrgDiscoveryServiceIml;
import com.motadata.traceorg.ipam.services.impl.subnet.TraceOrgSubnetServiceImpl;
import com.motadata.traceorg.ipam.util.TraceOrgCommonConstants;
import org.junit.Test;

import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({TraceOrgDiscoveryServiceIml.class, ProcessBuilder.class})
public class UnitTestTraceOrgSubnetServiceImplTest {

    @InjectMocks
    private TraceOrgSubnetServiceImpl subnetService;

    @Mock
    private TraceOrgCategoryRepository categoryRepository;

    @Mock
    private TraceOrgSubnetDetailsRepository subnetDetailsRepository;

    @Mock
    private TraceOrgSubnetIpDetailsRepository subnetIpDetailsRepository;

    @Mock
    private TraceOrgRogueDetectionRepository rogueDetectionRepository;

    @Test
    public void testGetTop10SubnetUtilization_Success() {
        List<TraceOrgSubnetDetails> subnets = Arrays.asList(new TraceOrgSubnetDetails(), new TraceOrgSubnetDetails());
        when(subnetDetailsRepository.findTop10ByUtilization()).thenReturn(subnets);

        HashMap<String, Object> result = subnetService.getTop10SubnetUtilization();

        assertEquals(TraceOrgCommonConstants.TRUE, result.get(TraceOrgCommonConstants.SUCCESS));
        assertEquals(subnets, result.get(TraceOrgCommonConstants.DATA));
    }

    @Test
    public void testGetTop10SubnetUtilization_ExceptionHandling() {
        when(subnetDetailsRepository.findTop10ByUtilization()).thenThrow(new RuntimeException("DB Error"));

        HashMap<String, Object> result = subnetService.getTop10SubnetUtilization();
        assertNull(result.get(TraceOrgCommonConstants.SUCCESS));
    }

    @Test
    public void testGetTop10CategoryUtilization_Success() {
        List<TraceOrgCategory> categories = Arrays.asList(new TraceOrgCategory(), new TraceOrgCategory());
        List<TraceOrgSubnetDetails> subnets = Arrays.asList(new TraceOrgSubnetDetails(), new TraceOrgSubnetDetails());

        when(categoryRepository.findAll()).thenReturn(categories);
        when(subnetDetailsRepository.findAll()).thenReturn(subnets);

        HashMap<String, Object> result = subnetService.getTop10CategoryUtilization();
        assertEquals(TraceOrgCommonConstants.TRUE, result.get(TraceOrgCommonConstants.SUCCESS));
    }

    @Test
    public void testGetTop10CategoryUtilization_EmptyCategories() {
        when(categoryRepository.findAll()).thenReturn(Collections.emptyList());

        HashMap<String, Object> result = subnetService.getTop10CategoryUtilization();
        assertNull(result.get(TraceOrgCommonConstants.SUCCESS));
    }

    @Test
    public void testDnsStatusSummary_Success() {
        List<Object[]> statuses = Arrays.asList(new Object[]{"SUCCESS", 5L}, new Object[]{"FORWARD_DNS_FAILED", 3L});
        when(subnetIpDetailsRepository.findGroupedByDnsStatus()).thenReturn(statuses);

        HashMap<String, Object> result = subnetService.dnsStatusSummary();
        assertEquals(TraceOrgCommonConstants.TRUE, result.get(TraceOrgCommonConstants.SUCCESS));
    }


    @Test
    public void testDnsStatusSummary_ExceptionHandling() {
        when(subnetIpDetailsRepository.findGroupedByDnsStatus()).thenThrow(new RuntimeException("DB Error"));

        HashMap<String, Object> result = subnetService.dnsStatusSummary();
        assertNull(result.get(TraceOrgCommonConstants.SUCCESS));
    }

    @Test
    public void testRecentDiscovered_Success() {
        List<TraceOrgRogueDetection> detections = Arrays.asList(new TraceOrgRogueDetection(), new TraceOrgRogueDetection());
        when(rogueDetectionRepository.findTop20ByAuthenticityOrderByDiscoveredAtDesc("discovered")).thenReturn(detections);

        HashMap<String, Object> result = subnetService.recentDiscovered();
        assertEquals(TraceOrgCommonConstants.TRUE, result.get(TraceOrgCommonConstants.SUCCESS));
        assertEquals(detections, result.get(TraceOrgCommonConstants.DATA));
    }

    @Test
    public void testRecentDiscovered_NoData() {
        when(rogueDetectionRepository.findTop20ByAuthenticityOrderByDiscoveredAtDesc("discovered")).thenReturn(Collections.emptyList());

        HashMap<String, Object> result = subnetService.recentDiscovered();
        assertEquals(TraceOrgCommonConstants.TRUE, result.get(TraceOrgCommonConstants.SUCCESS));
        assertTrue(((List<?>) result.get(TraceOrgCommonConstants.DATA)).isEmpty());
    }

    @Test
    public void testRecentDiscovered_ExceptionHandling() {
        when(rogueDetectionRepository.findTop20ByAuthenticityOrderByDiscoveredAtDesc("discovered")).thenThrow(new RuntimeException("DB Error"));

        HashMap<String, Object> result = subnetService.recentDiscovered();
        assertNull(result.get(TraceOrgCommonConstants.SUCCESS));
    }
}
