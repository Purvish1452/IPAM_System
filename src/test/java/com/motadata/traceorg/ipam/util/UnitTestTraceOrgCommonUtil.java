package com.motadata.traceorg.ipam.util;

import com.motadata.traceorg.ipam.entity.subnet.TraceOrgSubnetDetails;
import com.motadata.traceorg.ipam.controller.TestCasesApplicationTests;
import com.motadata.traceorg.ipam.services.TraceOrgService;
import org.junit.Assert;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.reflect.Whitebox;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.*;

@RunWith(MockitoJUnitRunner.class)
@PrepareForTest({  TraceOrgFactoryUtil.class, Runtime.class, TraceOrgService.class})
@PowerMockIgnore({"javax.manageme3nt.*"})
public class UnitTestTraceOrgCommonUtil extends TestCasesApplicationTests
{
    TraceOrgCommonUtil traceOrgCommonUtil = new TraceOrgCommonUtil();

    @Mock
    private Runtime mockRuntime;

    @Mock
    Process process;

    @Mock
    TraceOrgService traceOrgService;

    @Mock
    TraceOrgFactoryUtil traceOrgFactoryUtil;

    @InjectMocks
    private TraceOrgCommonUtil util;

    @Mock
    BufferedReader bufferedInputStream;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @Mock
    private UserDetails userDetails;

    @Before
    public void setUp()
    {
        mockStatic(Runtime.class);

        MockitoAnnotations.initMocks(this);

        SecurityContextHolder.setContext(securityContext);
    }


    @Test
    public void checkSubnet_ipv4_fail()
    {
        Assert.assertFalse(traceOrgCommonUtil.checkSubnet("172.16.12.0",16));
    }

    @Test
    public void checkSubnet_ipv4_success()
    {
        Assert.assertTrue(traceOrgCommonUtil.checkSubnet("172.16.12.0",24));
    }

    @Test
    public void checkSubnet_ipv6_success()
    {
        Assert.assertTrue(traceOrgCommonUtil.checkSubnet("ff02::",16));
    }

    @Test
    public void checkSubnet_ipv6_fail()
    {
        Assert.assertFalse(traceOrgCommonUtil.checkSubnet("ff02::",8));
    }

    @Test
    public void countTotalIp_ipv4_success()
    {
        Assert.assertEquals(256L, (long) traceOrgCommonUtil.countTotalIp("172.16.12.0",24));
    }

    @Test
    public void countTotalIp_ipv4_fail()
    {
        Assert.assertNotEquals(256L, (long) traceOrgCommonUtil.countTotalIp("172.16.12.0", 16));
    }

    @Test
    public void countTotalIp_ipv6()
    {
        Assert.assertEquals(0L, (long) traceOrgCommonUtil.countTotalIp("ff02::", 16));
    }

    @Test
    public void isValidIp_ipv4_success()
    {
        TraceOrgSubnetDetails traceOrgSubnetDetails = new TraceOrgSubnetDetails();

        traceOrgSubnetDetails.setSubnetAddress("172.16.12.0");

        traceOrgSubnetDetails.setSubnetCidr(24);

        Assert.assertTrue(TraceOrgCommonUtil.isValidIp(traceOrgSubnetDetails,"172.16.12.1"));
    }

    @Test
    public void isValidIp_ipv4_fail()
    {
        TraceOrgSubnetDetails traceOrgSubnetDetails = new TraceOrgSubnetDetails();

        traceOrgSubnetDetails.setSubnetAddress("172.16.12.0");

        traceOrgSubnetDetails.setSubnetCidr(24);

        Assert.assertFalse(TraceOrgCommonUtil.isValidIp(traceOrgSubnetDetails,"172.16.13.1"));
    }

    @Test
    public void isValidIp_ipv6()
    {
        TraceOrgSubnetDetails traceOrgSubnetDetails = new TraceOrgSubnetDetails();

        traceOrgSubnetDetails.setSubnetAddress("ff02::");

        traceOrgSubnetDetails.setSubnetCidr(16);

        Assert.assertFalse(TraceOrgCommonUtil.isValidIp(traceOrgSubnetDetails,"ff02::1"));
    }

    @Test
    public void getIPV6Addresses_success() throws Exception
    {
        TraceOrgCommonUtil traceOrgCommonUtil1 = new TraceOrgCommonUtil();

        Whitebox.setInternalState(traceOrgCommonUtil1, "traceOrgFactoryUtil", traceOrgFactoryUtil);

        when(traceOrgFactoryUtil.getBufferedReader(any())).thenReturn(bufferedInputStream);

        when(traceOrgFactoryUtil.getRuntime()).thenReturn(mockRuntime);

        when(mockRuntime.exec(TraceOrgCommonConstants.IPV6_NETSH_COMMAND)).thenReturn(process);

        when(bufferedInputStream.readLine()).thenReturn("ff02::1:ff33:6cd3                             33-33-ff-33-6c-d3  Permanent ").thenReturn("fe80::70c9:96ff:fe5d:a34                      Unreachable        Unreachable (Router)").thenReturn("fe80::aeca:ca9e:2908:395                      b8-1e-a4-8e-6c-99  Stale ").thenReturn("fe80::b786:a278:ec43:18ff                     b8-1e-a4-4d-cc-89  Stale ").thenReturn(null);

        List<String> result =  traceOrgCommonUtil1.getIPV6Addresses("fe80::/16");

        Assert.assertEquals(result.size(), 3);
    }

    @Test
    public void getIPV6Addresses_fail() throws Exception
    {
        TraceOrgCommonUtil traceOrgCommonUtil1 = new TraceOrgCommonUtil();

        Whitebox.setInternalState(traceOrgCommonUtil1, "traceOrgFactoryUtil", traceOrgFactoryUtil);

        when(traceOrgFactoryUtil.getBufferedReader(any())).thenReturn(bufferedInputStream);

        when(traceOrgFactoryUtil.getRuntime()).thenReturn(mockRuntime);

        when(mockRuntime.exec(TraceOrgCommonConstants.IPV6_NETSH_COMMAND)).thenReturn(process);

        when(bufferedInputStream.readLine()).thenReturn("ff02::1:ff33:6cd3                             33-33-ff-33-6c-d3  Permanent ").thenReturn("fe80::70c9:96ff:fe5d:a34                      Unreachable        Unreachable (Router)").thenReturn("fe80::aeca:ca9e:2908:395                      b8-1e-a4-8e-6c-99  Stale ").thenReturn("fe80::b786:a278:ec43:18ff                     b8-1e-a4-4d-cc-89  Stale ").thenReturn(null);

        List<String> result =  traceOrgCommonUtil1.getIPV6Addresses("fb80::/16");

        Assert.assertEquals(result.size(), 0);
    }


    @Test
    public void getIPV6Addresses_Success_with_1_IP() throws Exception
    {
        Whitebox.setInternalState(util, "traceOrgFactoryUtil", traceOrgFactoryUtil);

        when(traceOrgFactoryUtil.getBufferedReader(any())).thenReturn(bufferedInputStream);

        when(traceOrgFactoryUtil.getRuntime()).thenReturn(mockRuntime);

        when(mockRuntime.exec(TraceOrgCommonConstants.IPV6_NETSH_COMMAND)).thenReturn(process);

        when(bufferedInputStream.readLine()).thenReturn("ff52::1:ff33:6cd3                             33-33-ff-33-6c-d3  Permanent ").thenReturn("fe80::70c9:96ff:fe5d:a34                      Unreachable        Unreachable (Router)").thenReturn("fe80::aeca:ca9e:2908:395                      b8-1e-a4-8e-6c-99  Stale ").thenReturn("fe80::b786:a278:ec43:18ff                     b8-1e-a4-4d-cc-89  Stale ").thenReturn(null);

        TraceOrgSubnetDetails traceOrgSubnetDetails = new TraceOrgSubnetDetails();

        traceOrgSubnetDetails.setSubnetAddress("ff52::");

        traceOrgSubnetDetails.setSubnetCidr(16);

        List<TraceOrgSubnetDetails> traceOrgSubnetDetailsList = new ArrayList<>();

        traceOrgSubnetDetailsList.add(traceOrgSubnetDetails);

        util.setTraceOrgService(traceOrgService);

        Mockito.when(traceOrgService.commonQuery(any(),any())).thenReturn((List) traceOrgSubnetDetailsList);

        util.ipList(traceOrgSubnetDetails);

        ArgumentCaptor<Object> argument = ArgumentCaptor.forClass(Object.class);

        Mockito.verify(traceOrgService).insertAll((List<?>) argument.capture());

        ArrayList result = (ArrayList) argument.getAllValues().get(0);

        Assert.assertEquals(result.size(),1);
    }

    @Test
    public void getIPV6Addresses_Success_with_2_IP() throws Exception
    {
        Whitebox.setInternalState(util, "traceOrgFactoryUtil", traceOrgFactoryUtil);

        when(traceOrgFactoryUtil.getBufferedReader(any())).thenReturn(bufferedInputStream);

        when(traceOrgFactoryUtil.getRuntime()).thenReturn(mockRuntime);

        when(mockRuntime.exec(TraceOrgCommonConstants.IPV6_NETSH_COMMAND)).thenReturn(process);

        when(bufferedInputStream.readLine()).thenReturn("ff52::1:ff33:6cd3                             33-33-ff-33-6c-d3  Permanent ").thenReturn("ff52::70c9:96ff:fe5d:a34                      Unreachable        Unreachable (Router)").thenReturn("fe80::aeca:ca9e:2908:395                      b8-1e-a4-8e-6c-99  Stale ").thenReturn("fe80::b786:a278:ec43:18ff                     b8-1e-a4-4d-cc-89  Stale ").thenReturn(null);

        TraceOrgSubnetDetails traceOrgSubnetDetails = new TraceOrgSubnetDetails();

        traceOrgSubnetDetails.setSubnetAddress("ff52::");

        traceOrgSubnetDetails.setSubnetCidr(16);

        List<TraceOrgSubnetDetails> traceOrgSubnetDetailsList = new ArrayList<>();

        traceOrgSubnetDetailsList.add(traceOrgSubnetDetails);

        util.setTraceOrgService(traceOrgService);

        Mockito.when(traceOrgService.commonQuery(any(),any())).thenReturn((List) traceOrgSubnetDetailsList);

        util.ipList(traceOrgSubnetDetails);

        ArgumentCaptor<Object> argument = ArgumentCaptor.forClass(Object.class);

        Mockito.verify(traceOrgService).insertAll((List<?>) argument.capture());

        ArrayList result = (ArrayList) argument.getAllValues().get(0);

        Assert.assertEquals(result.size(),2);
    }

    @Test
    public void testGetCurrentUserName()
    {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn("Admin");

        String currentUserName = util.getCurrentUserName();
        assertEquals("Admin", currentUserName);
    }

    @Test
    public void testGetCurrentUserNameWithPrincipalAsString()
    {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn("Admin");

        String currentUserName = util.getCurrentUserName();
        assertEquals("Admin", currentUserName);
    }
}
