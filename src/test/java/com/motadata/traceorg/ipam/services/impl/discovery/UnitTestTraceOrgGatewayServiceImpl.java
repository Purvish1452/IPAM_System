package com.motadata.traceorg.ipam.services.impl.discovery;



import com.motadata.traceorg.ipam.entity.subnet.TraceOrgSubnetDetails;
import com.motadata.traceorg.ipam.repository.discovery.TraceOrgGatewayRepository;
import com.motadata.traceorg.ipam.repository.subnet.TraceOrgSubnetDetailsRepository;
import com.motadata.traceorg.ipam.util.TraceOrgCommonConstants;
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
import static org.mockito.Mockito.*;


@RunWith(PowerMockRunner.class)
@PrepareForTest({TraceOrgDiscoveryServiceIml.class, ProcessBuilder.class})
public class UnitTestTraceOrgGatewayServiceImpl {


    @InjectMocks
    private TraceOrgGatewayServiceImpl gatewayService;

    @Mock
    private TraceOrgGatewayRepository traceOrgGatewayRepository;

    @Mock
    private TraceOrgSubnetDetailsRepository traceOrgSubnetDetailsRepository;


    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testRemoveGateway_GatewayInUse() {
        Long gatewayId = 1L;
        List<TraceOrgSubnetDetails> subnetDetails = new ArrayList<>();
        subnetDetails.add(new TraceOrgSubnetDetails());
        when(traceOrgSubnetDetailsRepository.findByGatewayId(gatewayId)).thenReturn(subnetDetails);

        HashMap<String, Object> result = gatewayService.removeGateway(gatewayId);
        assertFalse((Boolean) result.get(TraceOrgCommonConstants.SUCCESS));
        assertEquals("Gateway is in use and cannot be deleted !", result.get(TraceOrgCommonConstants.MESSAGE));
    }

    @Test
    public void testRemoveGateway_Success() {
        Long gatewayId = 1L;
        when(traceOrgSubnetDetailsRepository.findByGatewayId(gatewayId)).thenReturn(new ArrayList<>());

        HashMap<String, Object> result = gatewayService.removeGateway(gatewayId);
        assertTrue((Boolean) result.get(TraceOrgCommonConstants.SUCCESS));
        assertEquals("Gateway deleted Successfully!", result.get(TraceOrgCommonConstants.MESSAGE));
        verify(traceOrgGatewayRepository, times(1)).delete(gatewayId);
    }
}