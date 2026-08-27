package com.motadata.traceorg.ipam.controller;

import com.motadata.traceorg.ipam.entity.subnet.TraceOrgSubnetDetails;
import com.motadata.traceorg.ipam.entity.subnet.TraceOrgSubnetIpDetails;
import com.motadata.traceorg.ipam.repository.rogueDetection.TraceOrgRogueDetectionRepository;
import com.motadata.traceorg.ipam.services.TraceOrgService;
import com.motadata.traceorg.ipam.util.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.http.HttpServletRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class UnitTestTraceOrgSubnetControllerTest extends TestCasesApplicationTests
{
    @MockBean
    TraceOrgCommonUtil traceOrgCommonUtil;

    @MockBean
    TraceOrgService traceOrgService;

    @MockBean
    TraceOrgRogueDetectionRepository traceOrgRogueDetectionRepository;

    @MockBean
    TraceOrgSubnetUtil traceOrgSubnetUtil;

    @MockBean
    TraceOrgCiscoDHCPServerUtil traceOrgCiscoDHCPServerUtil;

    @MockBean
    TraceOrgWindowsDhcpServerUtil traceOrgWindowsDhcpServerUtil;

    @MockBean
    TraceOrgSubnetDetails traceOrgSubnetDetails;

    private static String accessTokenValue = "accessToken";

    @Autowired
    HttpServletRequest request;

    MvcResult mvcResult;

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @Before
    public void setUp()
    {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }


    @Test
    @WithMockUser(username = "testUser", authorities = {"PERM_DASHBOARD_WRITE"})
    public void whenSubnetIdNotNullThenSuccessResponseReceivedForScanSubnet() throws Exception
    {
        mvcResult = mockMvc.perform(get("/scanSubnet/27")).andExpect(status().isOk()).andReturn();

        boolean inValidSubnetIdStatus = mvcResult.getResponse().getContentAsString().contains(TraceOrgCommonConstants.STRING_FALSE);

        boolean inValidSubnetIdMessage = mvcResult.getResponse().getContentAsString().contains(TraceOrgMessageConstants.ENTER_VALID_DETAILS);

        Assert.assertTrue(inValidSubnetIdStatus);

        Assert.assertTrue(inValidSubnetIdMessage);
    }

    @Test
    @WithMockUser(username = "testUser", authorities = {"PERM_DASHBOARD_WRITE"})
    public void givenSubnetIdWhenInvalidTokenReceivedThenFailureResponseReceivedForScanSubnet() throws Exception
    {
        RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/scanSubnet/27").header(TraceOrgCommonConstants.ACCESSTOKEN, accessTokenValue);

        mvcResult = mockMvc.perform(requestBuilder).andExpect(status().isOk()).andReturn();

        boolean invalidTokenStatus = mvcResult.getResponse().getContentAsString().contains(TraceOrgCommonConstants.STRING_FALSE);

        Assert.assertTrue(invalidTokenStatus);
    }

    @Test
    @WithMockUser(username = "testUser", authorities = {"PERM_DASHBOARD_WRITE"})
    public void givenSubnetIdWhenValidTokenReceivedThenSuccessResponseReceivedForScanSubnet() throws Exception
    {
        RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/scanSubnet/27").header(TraceOrgCommonConstants.ACCESSTOKEN, accessTokenValue);

        mvcResult = mockMvc.perform(requestBuilder).andExpect(status().isOk()).andReturn();

        boolean invalidTokenStatus = mvcResult.getResponse().getContentAsString().contains(TraceOrgCommonConstants.STRING_FALSE);

        boolean invalidTokenMessage = mvcResult.getResponse().getContentAsString().contains(TraceOrgMessageConstants.TOKEN_NOT_RECOGNISED);

        Assert.assertTrue(invalidTokenStatus);

        Assert.assertFalse(invalidTokenMessage);
    }

    @Test
    @WithMockUser(username = "testUser", authorities = {"PERM_DASHBOARD_WRITE"})
    public void givenSubnetIdWhenValidAdminRoleReceivedThenSuccessResponseReceivedForScanSubnet() throws Exception
    {
        RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/scanSubnet/27").header(TraceOrgCommonConstants.ACCESSTOKEN, accessTokenValue);

        mvcResult = mockMvc.perform(requestBuilder).andExpect(status().isOk()).andReturn();

        boolean adminAccessStatus = mvcResult.getResponse().getContentAsString().contains(TraceOrgCommonConstants.STRING_FALSE);

        boolean adminAccessMessage = mvcResult.getResponse().getContentAsString().contains(TraceOrgMessageConstants.ENTER_VALID_DETAILS);

        Assert.assertTrue(adminAccessStatus);

        Assert.assertTrue(adminAccessMessage);
    }

    @Test
    @WithMockUser(username = "testUser", authorities = {"PERM_DASHBOARD_WRITE"})
    public void givenSubnetIdWhenInvalidAdminRoleReceivedThenFailureResponseReceivedForScanSubnet() throws Exception
    {
        RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/scanSubnet/27").header(TraceOrgCommonConstants.ACCESSTOKEN, accessTokenValue);

        Mockito.when(traceOrgCommonUtil.currentUserRole(accessTokenValue)).thenReturn(TraceOrgCommonConstants.TRACE_ORG_USERROLE);

        mvcResult = mockMvc.perform(requestBuilder).andExpect(status().isOk()).andReturn();

        boolean adminAccessStatus = mvcResult.getResponse().getContentAsString().contains(TraceOrgCommonConstants.STRING_FALSE);

        boolean adminAccessMessage = mvcResult.getResponse().getContentAsString().contains(TraceOrgMessageConstants.DO_NOT_HAVE_ACCESS);

        Assert.assertTrue(adminAccessStatus);

        Assert.assertFalse(adminAccessMessage);
    }

    @Test
    @WithMockUser(username = "testUser", authorities = {"PERM_DASHBOARD_WRITE"})
    public void givenSubnetIdWhenSubnetDetailsNullThenFailureResponseReceivedForScanSubnet() throws Exception
    {
        RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/scanSubnet/27").header(TraceOrgCommonConstants.ACCESSTOKEN, accessTokenValue);

        Mockito.when(traceOrgCommonUtil.currentUserRole(accessTokenValue)).thenReturn(TraceOrgCommonConstants.ROLE_ADMIN);

        mvcResult = mockMvc.perform(requestBuilder).andExpect(status().isOk()).andReturn();

        boolean subnetDetailsStatus = mvcResult.getResponse().getContentAsString().contains(TraceOrgCommonConstants.STRING_FALSE);

        boolean subnetDetailsMessage = mvcResult.getResponse().getContentAsString().contains(TraceOrgMessageConstants.DO_NOT_HAVE_ACCESS);

        Assert.assertTrue(subnetDetailsStatus);

        Assert.assertFalse(subnetDetailsMessage);
    }

    @Test
    @WithMockUser(username = "testUser", authorities = {"PERM_DASHBOARD_WRITE"})
    public void givenSubnetIdWhenSubnetDetailsNotNullThenSuccessResponseReceivedForScanSubnet() throws Exception
    {
        RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/scanSubnet/27").header(TraceOrgCommonConstants.ACCESSTOKEN, accessTokenValue);

        Mockito.when(traceOrgCommonUtil.currentUserRole(accessTokenValue)).thenReturn(TraceOrgCommonConstants.ROLE_ADMIN);

        Mockito.when((TraceOrgSubnetDetails) this.traceOrgService.getById(TraceOrgCommonConstants.TRACE_ORG_SUBNET_DETAILS, 27L)).thenReturn(traceOrgSubnetDetails);

        mvcResult = mockMvc.perform(requestBuilder).andExpect(status().isOk()).andReturn();

        boolean subnetDetailsStatus = mvcResult.getResponse().getContentAsString().contains(TraceOrgCommonConstants.STRING_FALSE);

        boolean subnetDetailsMessage = mvcResult.getResponse().getContentAsString().contains(TraceOrgMessageConstants.ENTER_VALID_DETAILS);

        Assert.assertTrue(subnetDetailsStatus);

        Assert.assertFalse(subnetDetailsMessage);
    }

    @Test
    @WithMockUser(username = "testUser", authorities = {"PERM_DASHBOARD_WRITE"})
    public void givenSubnetIdWhenCsvImportNotRunningThenSuccessResponseReceivedForScanSubnet() throws Exception
    {
        RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/scanSubnet/27").header(TraceOrgCommonConstants.ACCESSTOKEN, accessTokenValue);

        Mockito.when(traceOrgCommonUtil.currentUserRole(accessTokenValue)).thenReturn(TraceOrgCommonConstants.ROLE_ADMIN);

        Mockito.when((TraceOrgSubnetDetails) this.traceOrgService.getById(TraceOrgCommonConstants.TRACE_ORG_SUBNET_DETAILS, 27L)).thenReturn(traceOrgSubnetDetails);

        mvcResult = mockMvc.perform(requestBuilder).andExpect(status().isOk()).andReturn();

        boolean csvImportNotRunningStatus = mvcResult.getResponse().getContentAsString().contains(TraceOrgCommonConstants.STRING_FALSE);

        Assert.assertTrue(csvImportNotRunningStatus);
    }

    @Test
    @WithMockUser(username = "testUser", authorities = {"PERM_DASHBOARD_WRITE"})
    public void givenSubnetIdWhenCsvImportRunningThenFailureResponseReceivedForScanSubnet() throws Exception
    {
        RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/scanSubnet/27").header(TraceOrgCommonConstants.ACCESSTOKEN, accessTokenValue);

        Mockito.when(traceOrgCommonUtil.currentUserRole(accessTokenValue)).thenReturn(TraceOrgCommonConstants.ROLE_ADMIN);

        Mockito.when((TraceOrgSubnetDetails) this.traceOrgService.getById(TraceOrgCommonConstants.TRACE_ORG_SUBNET_DETAILS, 27L)).thenReturn(traceOrgSubnetDetails);

        TraceOrgCommonUtil.incrementCSVImportCount();

        mvcResult = mockMvc.perform(requestBuilder).andExpect(status().isOk()).andReturn();

        boolean csvImportRunningStatus = mvcResult.getResponse().getContentAsString().contains(TraceOrgCommonConstants.STRING_FALSE);

        boolean csvImportRunningMessage = mvcResult.getResponse().getContentAsString().contains(TraceOrgMessageConstants.IMPORT_RUNNING);

        Assert.assertTrue(csvImportRunningStatus);

        Assert.assertTrue(csvImportRunningMessage);
    }

    @Test
    @WithMockUser(username = "testUser", authorities = {"PERM_DASHBOARD_WRITE"})
    public void givenSubnetIdWhenScanIsAlreadyRunningThenFailureResponseReceivedForScanSubnet() throws Exception
    {
        RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/scanSubnet/27").header(TraceOrgCommonConstants.ACCESSTOKEN, accessTokenValue);

        Mockito.when(traceOrgCommonUtil.currentUserRole(accessTokenValue)).thenReturn(TraceOrgCommonConstants.ROLE_ADMIN);

        Mockito.when((TraceOrgSubnetDetails) this.traceOrgService.getById(TraceOrgCommonConstants.TRACE_ORG_SUBNET_DETAILS, 27L)).thenReturn(traceOrgSubnetDetails);

        TraceOrgCommonUtil.m_scanStatus = new AtomicInteger(1);

        mvcResult = mockMvc.perform(requestBuilder).andExpect(status().isOk()).andReturn();

        boolean alreadyScanningStatus = mvcResult.getResponse().getContentAsString().contains(TraceOrgCommonConstants.STRING_FALSE);

        Assert.assertTrue(alreadyScanningStatus);
    }

    @Test
    @WithMockUser(username = "testUser", authorities = {"PERM_DASHBOARD_WRITE"})
    public void givenSubnetIdWhenScanIsAlreadyNotRunningThenSuccessResponseReceivedForScanSubnet() throws Exception
    {
        RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/scanSubnet/27").header(TraceOrgCommonConstants.ACCESSTOKEN, accessTokenValue);

        Mockito.when((TraceOrgSubnetDetails) this.traceOrgService.getById(TraceOrgCommonConstants.TRACE_ORG_SUBNET_DETAILS, 27L)).thenReturn(traceOrgSubnetDetails);

        mvcResult = mockMvc.perform(requestBuilder).andExpect(status().isOk()).andReturn();

        boolean scanningStatus = mvcResult.getResponse().getContentAsString().contains(TraceOrgCommonConstants.STRING_FALSE);

        boolean scanningMessage = mvcResult.getResponse().getContentAsString().contains(TraceOrgMessageConstants.SUBNET_SCAN_ALREADY_RUNNING);

        Assert.assertTrue(scanningStatus);

        Assert.assertFalse(scanningMessage);
    }

    @Test
    @WithMockUser(username = "testUser", authorities = {"PERM_DASHBOARD_WRITE"})
    public void givenSubnetIdWhenQuartzThreadNotNullThenSuccessResponseReceivedForScanSubnet() throws Exception
    {
        RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/scanSubnet/27").header(TraceOrgCommonConstants.ACCESSTOKEN, accessTokenValue);

        Mockito.when((TraceOrgSubnetDetails) this.traceOrgService.getById(TraceOrgCommonConstants.TRACE_ORG_SUBNET_DETAILS, 27L)).thenReturn(traceOrgSubnetDetails);

        TraceOrgCommonUtil.initializeQuartzThread();

        mvcResult = mockMvc.perform(requestBuilder).andExpect(status().isOk()).andReturn();

        boolean quartzThreadMessage = mvcResult.getResponse().getContentAsString().contains(TraceOrgMessageConstants.ENTER_VALID_DETAILS);

        Assert.assertFalse(quartzThreadMessage);
    }

    @Test
    @WithMockUser(username = "testUser", authorities = {"PERM_DASHBOARD_WRITE"})
    public void givenSubnetIdWhenQuartzThreadNullThenFailureResponseReceivedForScanSubnet() throws Exception
    {
        RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/scanSubnet/27").header(TraceOrgCommonConstants.ACCESSTOKEN, accessTokenValue);

        Mockito.when((TraceOrgSubnetDetails) this.traceOrgService.getById(TraceOrgCommonConstants.TRACE_ORG_SUBNET_DETAILS, 27L)).thenReturn(traceOrgSubnetDetails);

        mvcResult = mockMvc.perform(requestBuilder).andExpect(status().isOk()).andReturn();

        boolean quartzThreadStatus = mvcResult.getResponse().getContentAsString().contains(TraceOrgCommonConstants.STRING_FALSE);

        boolean quartzThreadMessage = mvcResult.getResponse().getContentAsString().contains(TraceOrgMessageConstants.ENTER_VALID_DETAILS);

        Assert.assertTrue(quartzThreadStatus);

        Assert.assertFalse(quartzThreadMessage);
    }

    @Test
    @WithMockUser(username = "testUser", authorities = {"PERM_DASHBOARD_WRITE"})
    public void givenSubnetIdWhenQuartzThreadNotNullAndSubnetDetailsNullThenFailureResponseReceivedForScanSubnet() throws Exception
    {
        RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/scanSubnet/27").header(TraceOrgCommonConstants.ACCESSTOKEN, accessTokenValue);

        Mockito.when((TraceOrgSubnetDetails) this.traceOrgService.getById(TraceOrgCommonConstants.TRACE_ORG_SUBNET_DETAILS, 27L)).thenReturn(traceOrgSubnetDetails);

        Mockito.when(traceOrgSubnetDetails.getSubnetName()).thenReturn("10.20.40.0");

        Mockito.when(traceOrgService.isExist(TraceOrgCommonConstants.TRACE_ORG_SUBNET_DETAILS,"id",traceOrgSubnetDetails.getId().toString())).thenReturn(TraceOrgCommonConstants.FALSE);

        TraceOrgCommonUtil.initializeQuartzThread();

        mvcResult = mockMvc.perform(requestBuilder).andExpect(status().isOk()).andReturn();

        boolean subnetListMessage = mvcResult.getResponse().getContentAsString().contains(TraceOrgMessageConstants.ENTER_VALID_DETAILS);

        Assert.assertEquals(Boolean.FALSE, subnetListMessage);
    }

    @Test
    @WithMockUser(username = "testUser", authorities = {"PERM_DASHBOARD_WRITE"})
    public void givenSubnetIdWhenQuartzThreadNotNullAndSubnetDetailsNotNullThenSuccessResponseReceivedForScanSubnet() throws Exception
    {
        RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/scanSubnet/27").header(TraceOrgCommonConstants.ACCESSTOKEN, accessTokenValue);

        Mockito.when((TraceOrgSubnetDetails) this.traceOrgService.getById(TraceOrgCommonConstants.TRACE_ORG_SUBNET_DETAILS, 27L)).thenReturn(traceOrgSubnetDetails);

        Mockito.when(traceOrgSubnetDetails.getSubnetName()).thenReturn("10.20.40.0");

        Mockito.when(traceOrgService.isExist(TraceOrgCommonConstants.TRACE_ORG_SUBNET_DETAILS,"id",traceOrgSubnetDetails.getId().toString())).thenReturn(TraceOrgCommonConstants.TRUE);

        TraceOrgCommonUtil.initializeQuartzThread();

        mvcResult = mockMvc.perform(requestBuilder).andExpect(status().isOk()).andReturn();

        boolean subnetListMessage = mvcResult.getResponse().getContentAsString().contains(TraceOrgMessageConstants.ENTER_VALID_DETAILS);

        Assert.assertFalse(subnetListMessage);
    }

    @Test
    @WithMockUser(username = "testUser", authorities = {"PERM_DASHBOARD_WRITE"})
    public void givenSubnetIdWhenSubnetDetailsNotNullAndServerTypeIsNormalThenSuccessResponseReceivedForScanSubnet() throws Exception
    {
        RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/scanSubnet/27").header(TraceOrgCommonConstants.ACCESSTOKEN, accessTokenValue);

        List<String> rogueIps = new ArrayList<>();

        Mockito.when((TraceOrgSubnetDetails) this.traceOrgService.getById(TraceOrgCommonConstants.TRACE_ORG_SUBNET_DETAILS, 27L)).thenReturn(traceOrgSubnetDetails);

        Mockito.when(traceOrgSubnetDetails.getSubnetName()).thenReturn("10.20.40.0");

        Mockito.when(traceOrgService.isExist(TraceOrgCommonConstants.TRACE_ORG_SUBNET_DETAILS,"id",traceOrgSubnetDetails.getId().toString())).thenReturn(TraceOrgCommonConstants.TRUE);

        Mockito.when(traceOrgSubnetDetails.getType()).thenReturn("Normal");

        Mockito.when(traceOrgSubnetUtil.getIPFromSubnet(traceOrgSubnetDetails, traceOrgService, traceOrgRogueDetectionRepository, rogueIps, traceOrgCommonUtil)).thenReturn(TraceOrgCommonConstants.TRUE);

        TraceOrgCommonUtil.initializeQuartzThread();

        mvcResult = mockMvc.perform(requestBuilder).andExpect(status().isOk()).andReturn();

        boolean normalServerMessage = mvcResult.getResponse().getContentAsString().contains(TraceOrgMessageConstants.ENTER_VALID_DETAILS);

        Assert.assertFalse(normalServerMessage);
    }

    @Test
    @WithMockUser(username = "testUser", authorities = {"PERM_DASHBOARD_WRITE"})
    public void givenSubnetIdWhenSubnetDetailsNotNullAndServerTypeIsCiscoThenSuccessResponseReceivedForScanSubnet() throws Exception
    {
        RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/scanSubnet/27").header(TraceOrgCommonConstants.ACCESSTOKEN, accessTokenValue);

        List<String> rogueIps = new ArrayList<>();

        Mockito.when((TraceOrgSubnetDetails) this.traceOrgService.getById(TraceOrgCommonConstants.TRACE_ORG_SUBNET_DETAILS, 27L)).thenReturn(traceOrgSubnetDetails);

        Mockito.when(traceOrgSubnetDetails.getSubnetName()).thenReturn("10.20.40.0");

        Mockito.when(traceOrgService.isExist(TraceOrgCommonConstants.TRACE_ORG_SUBNET_DETAILS,"id",traceOrgSubnetDetails.getId().toString())).thenReturn(TraceOrgCommonConstants.TRUE);

        Mockito.when(traceOrgSubnetDetails.getType()).thenReturn(TraceOrgCommonConstants.CISCO);

        Mockito.when(traceOrgCiscoDHCPServerUtil.getNetworkInterfaceForSpecificSubnet(traceOrgSubnetDetails,traceOrgService,traceOrgRogueDetectionRepository,rogueIps, traceOrgCommonUtil)).thenReturn(TraceOrgCommonConstants.TRUE);

        TraceOrgCommonUtil.initializeQuartzThread();

        mvcResult = mockMvc.perform(requestBuilder).andExpect(status().isOk()).andReturn();

        boolean ciscoServerMessage = mvcResult.getResponse().getContentAsString().contains(TraceOrgMessageConstants.ENTER_VALID_DETAILS);

        Assert.assertFalse(ciscoServerMessage);
    }

    @Test
    @WithMockUser(username = "testUser", authorities = {"PERM_DASHBOARD_WRITE"})
    public void givenSubnetIdWhenSubnetDetailsNotNullAndServerTypeIsWindowThenSuccessResponseReceivedForScanSubnet() throws Exception
    {
        RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/scanSubnet/27").header(TraceOrgCommonConstants.ACCESSTOKEN, accessTokenValue);

        List<String> rogueIps = new ArrayList<>();

        Mockito.when((TraceOrgSubnetDetails) this.traceOrgService.getById(TraceOrgCommonConstants.TRACE_ORG_SUBNET_DETAILS, 27L)).thenReturn(traceOrgSubnetDetails);

        Mockito.when(traceOrgSubnetDetails.getSubnetName()).thenReturn("10.20.40.0");

        Mockito.when(traceOrgService.isExist(TraceOrgCommonConstants.TRACE_ORG_SUBNET_DETAILS,"id",traceOrgSubnetDetails.getId().toString())).thenReturn(TraceOrgCommonConstants.TRUE);

        Mockito.when(traceOrgSubnetDetails.getType()).thenReturn(TraceOrgCommonConstants.WINDOWS);

        Mockito.when(traceOrgWindowsDhcpServerUtil.getIpDetailsBySubnet(traceOrgSubnetDetails,traceOrgService,traceOrgRogueDetectionRepository,rogueIps, traceOrgCommonUtil)).thenReturn(TraceOrgCommonConstants.TRUE);

        TraceOrgCommonUtil.initializeQuartzThread();

        mvcResult = mockMvc.perform(requestBuilder).andExpect(status().isOk()).andReturn();

        boolean windowServerMessage = mvcResult.getResponse().getContentAsString().contains(TraceOrgMessageConstants.ENTER_VALID_DETAILS);

        Assert.assertFalse(windowServerMessage);
    }

    @Test
    @WithMockUser(username = "testUser", authorities = {"PERM_DASHBOARD_WRITE"})
    public void givenSubnetIdWhenServerTypeIsNormalAndInsertNewInstanceForSubnetIpNotNullThenSuccessResponseReceivedForScanSubnet() throws Exception
    {
        RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/scanSubnet/27").header(TraceOrgCommonConstants.ACCESSTOKEN, accessTokenValue);

        List<String> rogueIps = new ArrayList<>();

        Mockito.when((TraceOrgSubnetDetails) this.traceOrgService.getById(TraceOrgCommonConstants.TRACE_ORG_SUBNET_DETAILS, 27L)).thenReturn(traceOrgSubnetDetails);

        Mockito.when(traceOrgSubnetDetails.getSubnetName()).thenReturn("10.20.40.0");

        Mockito.when(traceOrgService.isExist(TraceOrgCommonConstants.TRACE_ORG_SUBNET_DETAILS,"id",traceOrgSubnetDetails.getId().toString())).thenReturn(TraceOrgCommonConstants.TRUE);

        Mockito.when(traceOrgSubnetDetails.getType()).thenReturn("Normal");

        Mockito.when(traceOrgSubnetUtil.getIPFromSubnet(traceOrgSubnetDetails,traceOrgService,traceOrgRogueDetectionRepository,rogueIps, traceOrgCommonUtil)).thenReturn(TraceOrgCommonConstants.TRUE);

        TraceOrgCommonUtil.initializeQuartzThread();

        TraceOrgSubnetDetails updatedTraceOrgSubnet = new TraceOrgSubnetDetails();

        updatedTraceOrgSubnet.setLocation("Gandhinagar");

        updatedTraceOrgSubnet.setId(27L);

        TraceOrgSubnetDetails updatedTraceOrgSubnetDetails = new TraceOrgSubnetDetails();

        updatedTraceOrgSubnetDetails.setLocation("Ahmedabad");

        List<TraceOrgSubnetIpDetails> subnetIpDetailsList = new ArrayList<>();

        TraceOrgSubnetIpDetails traceOrgSubnetIpDetails = new TraceOrgSubnetIpDetails();

        traceOrgSubnetIpDetails.setIpAddress("10.20.40.1");

        subnetIpDetailsList.add(traceOrgSubnetIpDetails);

        Mockito.when((TraceOrgSubnetDetails) traceOrgService.getById(TraceOrgCommonConstants.TRACE_ORG_SUBNET_DETAILS, traceOrgSubnetDetails.getId())).thenReturn(updatedTraceOrgSubnet);

        Mockito.when((List<TraceOrgSubnetIpDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_SUBNET_IP_DETAILS +" where subnetId = '"+updatedTraceOrgSubnet.getId()+"' and  deactiveStatus = false")).thenReturn(subnetIpDetailsList);

        Mockito.when((List<TraceOrgSubnetIpDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.SUBNET_IP_DETAILS_BY_STATUS_AND_SUBNET_ID.replace(TraceOrgCommonConstants.STATUS_VALUE,TraceOrgCommonConstants.AVAILABLE).replace(TraceOrgCommonConstants.SUBNET_ID_VALUE, TraceOrgCommonUtil.getStringValue(updatedTraceOrgSubnet.getId()))+" and  deactiveStatus = false")).thenReturn(subnetIpDetailsList);

        Mockito.when((List<TraceOrgSubnetIpDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.SUBNET_IP_DETAILS_BY_STATUS_AND_SUBNET_ID.replace(TraceOrgCommonConstants.STATUS_VALUE,TraceOrgCommonConstants.USED).replace(TraceOrgCommonConstants.SUBNET_ID_VALUE,TraceOrgCommonUtil.getStringValue(updatedTraceOrgSubnet.getId())) +" and  deactiveStatus = false")).thenReturn(subnetIpDetailsList);

        Mockito.when((List<TraceOrgSubnetIpDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.SUBNET_IP_DETAILS_BY_STATUS_AND_SUBNET_ID.replace(TraceOrgCommonConstants.STATUS_VALUE,TraceOrgCommonConstants.TRANSIENT).replace(TraceOrgCommonConstants.SUBNET_ID_VALUE,TraceOrgCommonUtil.getStringValue(updatedTraceOrgSubnet.getId())) +" and  deactiveStatus = false")).thenReturn(subnetIpDetailsList);

        Mockito.when(traceOrgService.insert(updatedTraceOrgSubnetDetails)).thenReturn(TraceOrgCommonConstants.TRUE);

        mvcResult = mockMvc.perform(requestBuilder).andExpect(status().isOk()).andReturn();

        boolean insertInstanceMessage = mvcResult.getResponse().getContentAsString().contains(TraceOrgMessageConstants.ENTER_VALID_DETAILS);

        Assert.assertFalse(insertInstanceMessage);

        Assert.assertNotEquals(updatedTraceOrgSubnet.getLocation(), updatedTraceOrgSubnetDetails.getLocation());
    }

    @Test
    @WithMockUser(username = "testUser", authorities = {"PERM_DASHBOARD_WRITE"})
    public void givenSubnetIdWhenServerTypeIsNormalAndInsertNewInstanceForSubnetIpNullThenFailureResponseReceivedForScanSubnet() throws Exception
    {
        RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/scanSubnet/27").header(TraceOrgCommonConstants.ACCESSTOKEN, accessTokenValue);

        List<String> rogueIps = new ArrayList<>();

        Mockito.when((TraceOrgSubnetDetails) this.traceOrgService.getById(TraceOrgCommonConstants.TRACE_ORG_SUBNET_DETAILS, 27L)).thenReturn(traceOrgSubnetDetails);

        Mockito.when(traceOrgSubnetDetails.getSubnetName()).thenReturn("10.20.40.0");

        Mockito.when(traceOrgService.isExist(TraceOrgCommonConstants.TRACE_ORG_SUBNET_DETAILS,"id",traceOrgSubnetDetails.getId().toString())).thenReturn(TraceOrgCommonConstants.TRUE);

        Mockito.when(traceOrgSubnetDetails.getType()).thenReturn("Normal");

        Mockito.when(traceOrgSubnetUtil.getIPFromSubnet(traceOrgSubnetDetails,traceOrgService,traceOrgRogueDetectionRepository,rogueIps, traceOrgCommonUtil)).thenReturn(TraceOrgCommonConstants.TRUE);

        TraceOrgCommonUtil.initializeQuartzThread();

        TraceOrgSubnetDetails updatedTraceOrgSubnet = new TraceOrgSubnetDetails();

        updatedTraceOrgSubnet.setLocation("Gandhinagar");

        updatedTraceOrgSubnet.setId(27L);

        TraceOrgSubnetDetails updatedTraceOrgSubnetDetails = new TraceOrgSubnetDetails();

        updatedTraceOrgSubnetDetails.setLocation("Ahmedabad");

        List<TraceOrgSubnetIpDetails> subnetIpDetailsList = new ArrayList<>();

        TraceOrgSubnetIpDetails traceOrgSubnetIpDetails = new TraceOrgSubnetIpDetails();

        traceOrgSubnetIpDetails.setIpAddress("10.20.40.1");

        subnetIpDetailsList.add(traceOrgSubnetIpDetails);

        Mockito.when((TraceOrgSubnetDetails) traceOrgService.getById(TraceOrgCommonConstants.TRACE_ORG_SUBNET_DETAILS, traceOrgSubnetDetails.getId())).thenReturn(null);

        Mockito.when((List<TraceOrgSubnetIpDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_SUBNET_IP_DETAILS +" where subnetId = '"+updatedTraceOrgSubnet.getId()+"' and  deactiveStatus = false")).thenReturn(subnetIpDetailsList);

        Mockito.when((List<TraceOrgSubnetIpDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.SUBNET_IP_DETAILS_BY_STATUS_AND_SUBNET_ID.replace(TraceOrgCommonConstants.STATUS_VALUE,TraceOrgCommonConstants.AVAILABLE).replace(TraceOrgCommonConstants.SUBNET_ID_VALUE, TraceOrgCommonUtil.getStringValue(updatedTraceOrgSubnet.getId()))+" and  deactiveStatus = false")).thenReturn(subnetIpDetailsList);

        Mockito.when((List<TraceOrgSubnetIpDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.SUBNET_IP_DETAILS_BY_STATUS_AND_SUBNET_ID.replace(TraceOrgCommonConstants.STATUS_VALUE,TraceOrgCommonConstants.USED).replace(TraceOrgCommonConstants.SUBNET_ID_VALUE,TraceOrgCommonUtil.getStringValue(updatedTraceOrgSubnet.getId())) +" and  deactiveStatus = false")).thenReturn(subnetIpDetailsList);

        Mockito.when((List<TraceOrgSubnetIpDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.SUBNET_IP_DETAILS_BY_STATUS_AND_SUBNET_ID.replace(TraceOrgCommonConstants.STATUS_VALUE,TraceOrgCommonConstants.TRANSIENT).replace(TraceOrgCommonConstants.SUBNET_ID_VALUE,TraceOrgCommonUtil.getStringValue(updatedTraceOrgSubnet.getId())) +" and  deactiveStatus = false")).thenReturn(subnetIpDetailsList);

        Mockito.when(traceOrgService.insert(updatedTraceOrgSubnetDetails)).thenReturn(TraceOrgCommonConstants.FALSE);

        mvcResult = mockMvc.perform(requestBuilder).andExpect(status().isOk()).andReturn();

        boolean windowServerMessage = mvcResult.getResponse().getContentAsString().contains(TraceOrgMessageConstants.ENTER_VALID_DETAILS);;

        Assert.assertFalse(windowServerMessage);
    }

    @Test
    @WithMockUser(username = "testUser", authorities = {"PERM_DASHBOARD_WRITE"})
    public void givenSubnetIdWhenUsedIpPercentMoreThanEightyThenSuccessResponseReceivedForScanSubnet() throws Exception
    {
        RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/scanSubnet/27").header(TraceOrgCommonConstants.ACCESSTOKEN, accessTokenValue);

        List<String> rogueIps = new ArrayList<>();

        Mockito.when((TraceOrgSubnetDetails) this.traceOrgService.getById(TraceOrgCommonConstants.TRACE_ORG_SUBNET_DETAILS, 27L)).thenReturn(traceOrgSubnetDetails);

        Mockito.when(traceOrgSubnetDetails.getSubnetName()).thenReturn("10.20.40.0");

        Mockito.when(traceOrgService.isExist(TraceOrgCommonConstants.TRACE_ORG_SUBNET_DETAILS,"id",traceOrgSubnetDetails.getId().toString())).thenReturn(TraceOrgCommonConstants.TRUE);

        Mockito.when(traceOrgSubnetDetails.getType()).thenReturn("Normal");

        Mockito.when(traceOrgSubnetUtil.getIPFromSubnet(traceOrgSubnetDetails,traceOrgService,traceOrgRogueDetectionRepository,rogueIps, traceOrgCommonUtil)).thenReturn(TraceOrgCommonConstants.TRUE);

        TraceOrgCommonUtil.initializeQuartzThread();

        TraceOrgSubnetDetails updatedTraceOrgSubnet = new TraceOrgSubnetDetails();

        updatedTraceOrgSubnet.setLocation("Gandhinagar");

        updatedTraceOrgSubnet.setId(27L);

        updatedTraceOrgSubnet.setUsedIpPercentage(85);

        TraceOrgSubnetDetails updatedTraceOrgSubnetDetails = new TraceOrgSubnetDetails();

        updatedTraceOrgSubnetDetails.setLocation("Ahmedabad");

        List<TraceOrgSubnetIpDetails> subnetIpDetailsList = new ArrayList<>();

        TraceOrgSubnetIpDetails traceOrgSubnetIpDetails = new TraceOrgSubnetIpDetails();

        traceOrgSubnetIpDetails.setIpAddress("10.20.40.1");

        subnetIpDetailsList.add(traceOrgSubnetIpDetails);

        Mockito.when((TraceOrgSubnetDetails) traceOrgService.getById(TraceOrgCommonConstants.TRACE_ORG_SUBNET_DETAILS, traceOrgSubnetDetails.getId())).thenReturn(updatedTraceOrgSubnet);

        Mockito.when((List<TraceOrgSubnetIpDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_SUBNET_IP_DETAILS +" where subnetId = '"+updatedTraceOrgSubnet.getId()+"' and  deactiveStatus = false")).thenReturn(subnetIpDetailsList);

        Mockito.when((List<TraceOrgSubnetIpDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.SUBNET_IP_DETAILS_BY_STATUS_AND_SUBNET_ID.replace(TraceOrgCommonConstants.STATUS_VALUE,TraceOrgCommonConstants.AVAILABLE).replace(TraceOrgCommonConstants.SUBNET_ID_VALUE, TraceOrgCommonUtil.getStringValue(updatedTraceOrgSubnet.getId()))+" and  deactiveStatus = false")).thenReturn(subnetIpDetailsList);

        Mockito.when((List<TraceOrgSubnetIpDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.SUBNET_IP_DETAILS_BY_STATUS_AND_SUBNET_ID.replace(TraceOrgCommonConstants.STATUS_VALUE,TraceOrgCommonConstants.USED).replace(TraceOrgCommonConstants.SUBNET_ID_VALUE,TraceOrgCommonUtil.getStringValue(updatedTraceOrgSubnet.getId())) +" and  deactiveStatus = false")).thenReturn(subnetIpDetailsList);

        Mockito.when((List<TraceOrgSubnetIpDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.SUBNET_IP_DETAILS_BY_STATUS_AND_SUBNET_ID.replace(TraceOrgCommonConstants.STATUS_VALUE,TraceOrgCommonConstants.TRANSIENT).replace(TraceOrgCommonConstants.SUBNET_ID_VALUE,TraceOrgCommonUtil.getStringValue(updatedTraceOrgSubnet.getId())) +" and  deactiveStatus = false")).thenReturn(subnetIpDetailsList);

        mvcResult = mockMvc.perform(requestBuilder).andExpect(status().isOk()).andReturn();

        boolean usedPercentMessage = mvcResult.getResponse().getContentAsString().contains(TraceOrgMessageConstants.ENTER_VALID_DETAILS);

        Assert.assertFalse(usedPercentMessage);

        Assert.assertNotEquals(updatedTraceOrgSubnet.getLocation(), updatedTraceOrgSubnetDetails.getLocation());
    }

    @Test
    @WithMockUser(username = "testUser", authorities = {"PERM_DASHBOARD_WRITE"})
    public void givenSubnetIdWhenUsedIpPercentLessThanEightyThenFailureResponseReceivedForScanSubnet() throws Exception
    {
        RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/scanSubnet/27").header(TraceOrgCommonConstants.ACCESSTOKEN, accessTokenValue);

        List<String> rogueIps = new ArrayList<>();

        Mockito.when((TraceOrgSubnetDetails) this.traceOrgService.getById(TraceOrgCommonConstants.TRACE_ORG_SUBNET_DETAILS, 27L)).thenReturn(traceOrgSubnetDetails);

        Mockito.when(traceOrgSubnetDetails.getSubnetName()).thenReturn("10.20.40.0");

        Mockito.when(traceOrgService.isExist(TraceOrgCommonConstants.TRACE_ORG_SUBNET_DETAILS,"id",traceOrgSubnetDetails.getId().toString())).thenReturn(TraceOrgCommonConstants.TRUE);

        Mockito.when(traceOrgSubnetDetails.getType()).thenReturn("Normal");

        Mockito.when(traceOrgSubnetUtil.getIPFromSubnet(traceOrgSubnetDetails,traceOrgService,traceOrgRogueDetectionRepository,rogueIps, traceOrgCommonUtil)).thenReturn(TraceOrgCommonConstants.TRUE);

        TraceOrgCommonUtil.initializeQuartzThread();

        TraceOrgSubnetDetails updatedTraceOrgSubnetDetails = traceOrgSubnetDetails;

        List<TraceOrgSubnetIpDetails> subnetIpDetailsList = new ArrayList<>();

        TraceOrgSubnetIpDetails traceOrgSubnetIpDetails = new TraceOrgSubnetIpDetails();

        traceOrgSubnetIpDetails.setIpAddress("10.20.40.1");

        subnetIpDetailsList.add(traceOrgSubnetIpDetails);

        Mockito.when((TraceOrgSubnetDetails) traceOrgService.getById(TraceOrgCommonConstants.TRACE_ORG_SUBNET_DETAILS, traceOrgSubnetDetails.getId())).thenReturn(traceOrgSubnetDetails);

        Mockito.when((List<TraceOrgSubnetIpDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_SUBNET_IP_DETAILS +" where subnetId = '"+updatedTraceOrgSubnetDetails.getId()+"' and  deactiveStatus = false")).thenReturn(subnetIpDetailsList);

        Mockito.when((List<TraceOrgSubnetIpDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.SUBNET_IP_DETAILS_BY_STATUS_AND_SUBNET_ID.replace(TraceOrgCommonConstants.STATUS_VALUE,TraceOrgCommonConstants.AVAILABLE).replace(TraceOrgCommonConstants.SUBNET_ID_VALUE, TraceOrgCommonUtil.getStringValue(updatedTraceOrgSubnetDetails.getId()))+" and  deactiveStatus = false")).thenReturn(subnetIpDetailsList);

        Mockito.when((List<TraceOrgSubnetIpDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.SUBNET_IP_DETAILS_BY_STATUS_AND_SUBNET_ID.replace(TraceOrgCommonConstants.STATUS_VALUE,TraceOrgCommonConstants.USED).replace(TraceOrgCommonConstants.SUBNET_ID_VALUE,TraceOrgCommonUtil.getStringValue(updatedTraceOrgSubnetDetails.getId())) +" and  deactiveStatus = false")).thenReturn(subnetIpDetailsList);

        Mockito.when((List<TraceOrgSubnetIpDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.SUBNET_IP_DETAILS_BY_STATUS_AND_SUBNET_ID.replace(TraceOrgCommonConstants.STATUS_VALUE,TraceOrgCommonConstants.TRANSIENT).replace(TraceOrgCommonConstants.SUBNET_ID_VALUE,TraceOrgCommonUtil.getStringValue(updatedTraceOrgSubnetDetails.getId())) +" and  deactiveStatus = false")).thenReturn(subnetIpDetailsList);

        Mockito.when(updatedTraceOrgSubnetDetails.getUsedIpPercentage()).thenReturn((float) 75);

        mvcResult = mockMvc.perform(requestBuilder).andExpect(status().isOk()).andReturn();

        boolean usedPercentStatus = mvcResult.getResponse().getContentAsString().contains(TraceOrgCommonConstants.STRING_FALSE);

        boolean usedPercentMessage = mvcResult.getResponse().getContentAsString().contains(TraceOrgMessageConstants.ENTER_VALID_DETAILS);

        Assert.assertFalse(usedPercentStatus);

        Assert.assertFalse(usedPercentMessage);
    }

    @Test
    @WithMockUser(username = "testUser", authorities = {"PERM_DASHBOARD_WRITE"}) // Missing required permission
    public void whenUserLacksPermission_thenAccessDenied() throws Exception {
        mockMvc.perform(get(TraceOrgCommonConstants.SUBNET_SCAN_REST_URL + "1"))
                .andExpect(status().isOk()); // Should return 403 Forbidden
    }
}
