package com.motadata.traceorg.ipam.services.impl.ipRequests;


import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import com.motadata.traceorg.ipam.dto.ipRequests.TraceOrgApproveIpRequestDTO;
import com.motadata.traceorg.ipam.dto.ipRequests.TraceOrgRejectIpRequestDTO;
import com.motadata.traceorg.ipam.entity.ipRequests.TraceOrgIpRequests;
import com.motadata.traceorg.ipam.entity.subnet.TraceOrgSubnetDetails;
import com.motadata.traceorg.ipam.entity.subnet.TraceOrgSubnetIpDetails;
import com.motadata.traceorg.ipam.enumeration.TraceOrgIpRequestsStatus;
import com.motadata.traceorg.ipam.repository.ipRequests.TraceOrgIpRequestsRepository;
import com.motadata.traceorg.ipam.repository.subnet.TraceOrgIpChangeLogRepository;
import com.motadata.traceorg.ipam.repository.subnet.TraceOrgSubnetDetailsRepository;
import com.motadata.traceorg.ipam.repository.subnet.TraceOrgSubnetIpDetailsRepository;
import com.motadata.traceorg.ipam.services.TraceOrgService;
import com.motadata.traceorg.ipam.util.TraceOrgMessageConstants;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.*;

@RunWith(MockitoJUnitRunner.class)
public class TraceOrgIpRequestsServiceImplTest {

    @InjectMocks
    private TraceOrgIpRequestsServiceImpl ipRequestsService;

    @Mock
    private TraceOrgIpRequestsRepository ipRequestsRepository;

    @Mock
    private TraceOrgSubnetDetailsRepository subnetDetailsRepository;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    private TraceOrgIpRequests ipRequest;

    @Mock
    TraceOrgSubnetIpDetailsRepository traceOrgSubnetIpRepository;

    @Mock
    private TraceOrgSubnetIpDetailsRepository traceOrgSubnetIpDetailsRepository;

    @Mock
    private TraceOrgService traceOrgServiceMock;

    @Mock
    private TraceOrgIpChangeLogRepository traceOrgIpChangeLogRepository;

    @Before
    public void setUp()
    {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testAddIpRequest()
    {
        ipRequest = new TraceOrgIpRequests();

        ipRequest.setId(1L);

        ipRequest.setNumberOfIps(5);

        ipRequest.setStatus(TraceOrgIpRequestsStatus.PENDING);

        ipRequest.setSubnetId("subnet-123");

        ipRequest.setPreferredSubnet(true);

        ipRequest.setPurpose("Testing IP allocation");

        ipRequest.setIps(Arrays.asList("192.168.1.1", "192.168.1.2", "192.168.1.3", "192.168.1.4", "192.168.1.5"));

        when(ipRequestsRepository.save(any(TraceOrgIpRequests.class))).thenReturn(ipRequest);

        HashMap<String, Object> response = ipRequestsService.addIpRequests(ipRequest);

        assertEquals(true, response.get("success"));

        assertEquals("Ip request added successfully", response.get("message"));
    }

    @Test
    public void testAddIpRequestPreferredSubnetTrue()
    {
        TraceOrgIpRequests ipRequest = new TraceOrgIpRequests();

        ipRequest.setId(1L);

        ipRequest.setNumberOfIps(5);

        ipRequest.setStatus(TraceOrgIpRequestsStatus.APPROVED);

        ipRequest.setSubnetId("subnet-123");

        ipRequest.setPreferredSubnet(true);

        ipRequest.setPurpose("Testing IP allocation");

        ipRequest.setIps(Arrays.asList("192.168.1.1", "192.168.1.2", "192.168.1.3", "192.168.1.4", "192.168.1.5"));

        when(ipRequestsRepository.save(any(TraceOrgIpRequests.class))).thenReturn(ipRequest);

        HashMap<String, Object> response = ipRequestsService.addIpRequests(ipRequest);

        assertEquals(true, response.get("success"));

        assertEquals("Ip request added successfully", response.get("message"));
    }

    @Test
    public void testAddIpRequestPreferredSubnetTrueFailed()
    {
        TraceOrgIpRequests ipRequest = new TraceOrgIpRequests();

        ipRequest.setId(1L);

        ipRequest.setNumberOfIps(0);

        ipRequest.setStatus(TraceOrgIpRequestsStatus.APPROVED);

        ipRequest.setPreferredSubnet(true);

        ipRequest.setPurpose("Testing IP allocation");

        ipRequest.setIps(Collections.emptyList());

        HashMap<String, Object> response = ipRequestsService.addIpRequests(ipRequest);

        assertEquals(false, response.get("success"));

        assertEquals(TraceOrgMessageConstants.ALLOCATE_IP, response.get("message"));
    }

    @Test
    public void testGetIpRequest()
    {
        ipRequest = new TraceOrgIpRequests();

        ipRequest.setId(1L);

        ipRequest.setPreferredSubnet(false);

        ipRequest.setStatus(TraceOrgIpRequestsStatus.PENDING);

        when(ipRequestsRepository.findOne(1L)).thenReturn(ipRequest);

        HashMap<String, Object> response = ipRequestsService.getIpRequest(1L);

        assertEquals(true, response.get("success"));

        assertNotNull(response.get("data"));
    }

    @Test
    public void testIpRequestApprovedPreferredSubnetTrue()
    {
        TraceOrgIpRequests ipRequest = new TraceOrgIpRequests();

        ipRequest.setId(1L);

        ipRequest.setNumberOfIps(2);

        ipRequest.setStatus(TraceOrgIpRequestsStatus.APPROVED);

        ipRequest.setSubnetId("subnet-123");

        ipRequest.setPreferredSubnet(true);

        ipRequest.setPurpose("Testing IP allocation");

        ipRequest.setIps(Arrays.asList("192.168.1.1", "192.168.1.2"));

        TraceOrgApproveIpRequestDTO approveRequestDTO = new TraceOrgApproveIpRequestDTO();

        approveRequestDTO.setId(1L);

        approveRequestDTO.setIps(Arrays.asList("192.168.1.1", "192.168.1.2"));

        when(ipRequestsRepository.findOne(1L)).thenReturn(ipRequest);

        when(ipRequestsRepository.save(any(TraceOrgIpRequests.class))).thenReturn(ipRequest);

        when(traceOrgSubnetIpRepository.existsAvailableStatus(Mockito.any())).thenReturn(Boolean.TRUE);

        List<String> ipList = Arrays.asList("192.168.1.1", "192.168.1.2");

        TraceOrgSubnetIpDetails ip1 = mock(TraceOrgSubnetIpDetails.class);

        TraceOrgSubnetIpDetails ip2 = mock(TraceOrgSubnetIpDetails.class);

        TraceOrgSubnetDetails subnet = mock(TraceOrgSubnetDetails.class);

        when(ip1.getId()).thenReturn(1L);

        when(ip2.getId()).thenReturn(2L);

        when(ip1.getSubnetId()).thenReturn(subnet);

        when(ip2.getSubnetId()).thenReturn(subnet);

        when(subnet.getId()).thenReturn(10L);

        when(ip1.getIpAddress()).thenReturn("192.168.1.1");

        when(ip2.getIpAddress()).thenReturn("192.168.1.2");

        when(traceOrgSubnetIpDetailsRepository.findByIpAddressIn(ipList)).thenReturn(Arrays.asList(ip1, ip2));

        SecurityContextHolder.setContext(securityContext);

        when(securityContext.getAuthentication()).thenReturn(authentication);

        when(authentication.getName()).thenReturn("testUser");

        HashMap<String, Object> response = ipRequestsService.ipRequestApproved(approveRequestDTO);

        assertEquals(true, response.get("success"));

        assertEquals("Ip request approved successfully", response.get("message"));
    }

    @Test
    public void testIpRequestApprovedPreferredSubnetFalse()
    {
        TraceOrgIpRequests ipRequest = new TraceOrgIpRequests();

        ipRequest.setId(1L);

        ipRequest.setNumberOfIps(2);

        ipRequest.setStatus(TraceOrgIpRequestsStatus.APPROVED);

        ipRequest.setSubnetId("subnet-123");

        ipRequest.setPreferredSubnet(false);

        ipRequest.setPurpose("Testing IP allocation");

        ipRequest.setIps(Arrays.asList("192.168.1.1", "192.168.1.2"));

        TraceOrgApproveIpRequestDTO approveRequestDTO = new TraceOrgApproveIpRequestDTO();

        approveRequestDTO.setId(1L);

        approveRequestDTO.setSubnetId("subnet-123");

        approveRequestDTO.setIps(Arrays.asList("192.168.1.1", "192.168.1.2"));

        when(ipRequestsRepository.findOne(1L)).thenReturn(ipRequest);

        when(ipRequestsRepository.save(any(TraceOrgIpRequests.class))).thenReturn(ipRequest);

        when(traceOrgSubnetIpRepository.existsAvailableStatus(Mockito.any())).thenReturn(Boolean.TRUE);

        List<String> ipList = Arrays.asList("192.168.1.1", "192.168.1.2");

        TraceOrgSubnetIpDetails ip1 = mock(TraceOrgSubnetIpDetails.class);

        TraceOrgSubnetIpDetails ip2 = mock(TraceOrgSubnetIpDetails.class);

        TraceOrgSubnetDetails subnet = mock(TraceOrgSubnetDetails.class);

        when(ip1.getId()).thenReturn(1L);

        when(ip2.getId()).thenReturn(2L);

        when(ip1.getSubnetId()).thenReturn(subnet);

        when(ip2.getSubnetId()).thenReturn(subnet);

        when(subnet.getId()).thenReturn(10L);

        when(ip1.getIpAddress()).thenReturn("192.168.1.1");

        when(ip2.getIpAddress()).thenReturn("192.168.1.2");

        when(traceOrgSubnetIpDetailsRepository.findByIpAddressIn(ipList)).thenReturn(Arrays.asList(ip1, ip2));

        SecurityContextHolder.setContext(securityContext);

        when(securityContext.getAuthentication()).thenReturn(authentication);

        when(authentication.getName()).thenReturn("testUser");


        HashMap<String, Object> response = ipRequestsService.ipRequestApproved(approveRequestDTO);

        assertEquals(true, response.get("success"));

        assertEquals("Ip request approved successfully", response.get("message"));
    }

    @Test
    public void testIpRequestApprovedPreferredSubnetFail()
    {
        TraceOrgIpRequests ipRequest = new TraceOrgIpRequests();

        ipRequest.setId(1L);

        ipRequest.setNumberOfIps(5);

        ipRequest.setStatus(TraceOrgIpRequestsStatus.APPROVED);

        ipRequest.setSubnetId("subnet-123");

        ipRequest.setPreferredSubnet(false);

        ipRequest.setPurpose("Testing IP allocation");

        ipRequest.setIps(Arrays.asList("192.168.1.1", "192.168.1.2", "192.168.1.3", "192.168.1.4", "192.168.1.5"));

        TraceOrgApproveIpRequestDTO approveRequestDTO = new TraceOrgApproveIpRequestDTO();

        approveRequestDTO.setId(1L);

        approveRequestDTO.setIps(Arrays.asList("192.168.1.1", "192.168.1.2", "192.168.1.3", "192.168.1.4", "192.168.1.5"));

        when(ipRequestsRepository.findOne(1L)).thenReturn(ipRequest);

        HashMap<String, Object> response = ipRequestsService.ipRequestApproved(approveRequestDTO);

        assertEquals(false, response.get("success"));

        assertEquals(TraceOrgMessageConstants.ALLOCATE_IP, response.get("message"));
    }

    @Test
    public void testIpRequestRejected()
    {
        TraceOrgIpRequests ipRequest = new TraceOrgIpRequests();

        ipRequest.setId(1L);

        ipRequest.setNumberOfIps(5);

        ipRequest.setStatus(TraceOrgIpRequestsStatus.APPROVED);

        ipRequest.setSubnetId("subnet-123");

        ipRequest.setPreferredSubnet(false);

        ipRequest.setPurpose("Testing IP allocation");

        ipRequest.setIps(Arrays.asList("192.168.1.1", "192.168.1.2", "192.168.1.3", "192.168.1.4", "192.168.1.5"));

        when(ipRequestsRepository.findOne(1L)).thenReturn(ipRequest);

        when(ipRequestsRepository.save(any(TraceOrgIpRequests.class))).thenReturn(ipRequest);

        TraceOrgRejectIpRequestDTO traceOrgRejectIpRequestDTO = new TraceOrgRejectIpRequestDTO();

        traceOrgRejectIpRequestDTO.setId(1L);

        HashMap<String, Object> response = ipRequestsService.ipRequestRejected(traceOrgRejectIpRequestDTO);

        assertEquals(true, response.get("success"));

        assertEquals(TraceOrgMessageConstants.IP_REQUEST_REJECTED, response.get("message"));
    }
    @Test
    public void testListAllIpRequestsAdminUser()
    {

        SecurityContextHolder.setContext(securityContext);

        when(securityContext.getAuthentication()).thenReturn(authentication);

        Collection<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"));

        when(authentication.getAuthorities()).thenAnswer(invocation -> authorities);

        TraceOrgIpRequests ipRequest = new TraceOrgIpRequests();

        when(ipRequestsRepository.findAll()).thenReturn(Collections.singletonList(ipRequest));

        HashMap<String, Object> response = ipRequestsService.listAllIpRequests();

        assertEquals(true, response.get("success"));

        assertNotNull(response.get("data"));

        assertEquals(1, ((List<?>) response.get("data")).size());
    }

    @Test
    public void testListAllIpRequestsNonAdminUser()
    {
        SecurityContextHolder.setContext(securityContext);

        when(securityContext.getAuthentication()).thenReturn(authentication);

        when(authentication.getName()).thenReturn("User1");

        TraceOrgIpRequests ipRequest = new TraceOrgIpRequests();

        when(ipRequestsRepository.findByCreatedBy("User1")).thenReturn(Collections.singletonList(ipRequest));

        HashMap<String, Object> response = ipRequestsService.listAllIpRequests();

        assertEquals(true, response.get("success"));

        assertNotNull(response.get("data"));

        assertEquals(1, ((List<?>) response.get("data")).size());
    }

    @Test
    public void testIpRequestApprovedPreferredSubnetTrueRequestedIPNotAvailable()
    {
        TraceOrgIpRequests ipRequest = new TraceOrgIpRequests();

        ipRequest.setId(1L);

        ipRequest.setNumberOfIps(5);

        ipRequest.setStatus(TraceOrgIpRequestsStatus.APPROVED);

        ipRequest.setSubnetId("subnet-123");

        ipRequest.setPreferredSubnet(true);

        ipRequest.setPurpose("Testing IP allocation");

        ipRequest.setIps(Arrays.asList("192.168.1.1", "192.168.1.2", "192.168.1.3", "192.168.1.4", "192.168.1.5"));

        TraceOrgApproveIpRequestDTO approveRequestDTO = new TraceOrgApproveIpRequestDTO();

        approveRequestDTO.setId(1L);

        approveRequestDTO.setIps(Arrays.asList("192.168.1.1", "192.168.1.2", "192.168.1.3", "192.168.1.4", "192.168.1.5"));

        when(ipRequestsRepository.findOne(1L)).thenReturn(ipRequest);

        when(traceOrgSubnetIpRepository.existsAvailableStatus(Mockito.any())).thenReturn(Boolean.FALSE);

        HashMap<String, Object> response = ipRequestsService.ipRequestApproved(approveRequestDTO);

        assertEquals(false, response.get("success"));

        assertEquals(TraceOrgMessageConstants.IPS_NOT_AVAILABLE, response.get("message"));
    }

    @Test
    public void testIpRequestApprovedPreferredSubnetFalseRequestedIPNotAvailable()
    {
        TraceOrgIpRequests ipRequest = new TraceOrgIpRequests();

        ipRequest.setId(1L);

        ipRequest.setNumberOfIps(5);

        ipRequest.setStatus(TraceOrgIpRequestsStatus.APPROVED);

        ipRequest.setSubnetId("subnet-123");

        ipRequest.setPreferredSubnet(false);

        ipRequest.setPurpose("Testing IP allocation");

        ipRequest.setIps(Arrays.asList("192.168.1.1", "192.168.1.2", "192.168.1.3", "192.168.1.4", "192.168.1.5"));

        TraceOrgApproveIpRequestDTO approveRequestDTO = new TraceOrgApproveIpRequestDTO();

        approveRequestDTO.setId(1L);

        approveRequestDTO.setSubnetId("subnet-123");

        approveRequestDTO.setIps(Arrays.asList("192.168.1.1", "192.168.1.2", "192.168.1.3", "192.168.1.4", "192.168.1.5"));

        when(ipRequestsRepository.findOne(1L)).thenReturn(ipRequest);

        when(traceOrgSubnetIpRepository.existsAvailableStatus(Mockito.any())).thenReturn(Boolean.FALSE);

        HashMap<String, Object> response = ipRequestsService.ipRequestApproved(approveRequestDTO);

        assertEquals(false, response.get("success"));

        assertEquals(TraceOrgMessageConstants.IPS_NOT_AVAILABLE, response.get("message"));
    }
}
