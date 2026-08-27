package com.motadata.traceorg.ipam.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.motadata.traceorg.ipam.controller.subnet.TraceOrgSubnetIpController;
import com.motadata.traceorg.ipam.entity.settings.TraceOrgCustomColumn;
import com.motadata.traceorg.ipam.entity.subnet.TraceOrgSubnetDetails;
import com.motadata.traceorg.ipam.entity.subnet.TraceOrgSubnetIpDetails;
import com.motadata.traceorg.ipam.repository.rogueDetection.TraceOrgRogueDetectionRepository;
import com.motadata.traceorg.ipam.repository.settings.TraceOrgCustomColumnRepository;
import com.motadata.traceorg.ipam.repository.subnet.TraceOrgSubnetIpDetailsRepository;
import com.motadata.traceorg.ipam.services.TraceOrgService;
import com.motadata.traceorg.ipam.services.subnet.TraceOrgSubnetIpService;
import com.motadata.traceorg.ipam.util.TraceOrgCommonConstants;
import com.motadata.traceorg.ipam.util.TraceOrgCommonUtil;
import com.motadata.traceorg.ipam.util.TraceOrgMessageConstants;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
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
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Created by smit on 21/3/22.
 */
public class UnitTestTraceOrgSubnetIpControllerTest extends TestCasesApplicationTests
{
    @MockBean
    TraceOrgCommonUtil traceOrgCommonUtil;

    @MockBean
    TraceOrgService traceOrgService;

    @Mock
    private TraceOrgRogueDetectionRepository traceOrgRogueDetectionRepository;

    @Autowired
    HttpServletRequest request;

    @Mock
    private TraceOrgCustomColumnRepository traceOrgCustomColumnRepository;

    @Mock
    private TraceOrgSubnetIpService traceOrgSubnetIpService;

    MvcResult mvcResult;

    private TraceOrgSubnetIpController traceOrgSubnetIpController;

    @Mock
    private TraceOrgSubnetIpDetailsRepository traceOrgSubnetIpDetailsRepository;

    private static String accessTokenValue = "accessToken";

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    private TraceOrgCustomColumn customColumn;

    @Before
    public void setUp()
    {
        // Inject mocks via constructor
        traceOrgSubnetIpController = new TraceOrgSubnetIpController(
                traceOrgService,
                traceOrgRogueDetectionRepository,
                traceOrgCommonUtil,
                traceOrgSubnetIpService,
                traceOrgCustomColumnRepository,
                traceOrgSubnetIpDetailsRepository
        );

        customColumn = new TraceOrgCustomColumn();

        customColumn.setId(1L);

        customColumn.setColumnName("TestColumn");

        mockMvc = MockMvcBuilders.standaloneSetup(traceOrgSubnetIpController).build();
    }

    @Test
    @WithMockUser(username = "testUser", authorities = {"PERM_DASHBOARD_READ"})
    public void whenSubnetIdNotNullThenSuccessResponseReceivedForExportPdfSubnetIp() throws Exception
    {
        mvcResult = mockMvc.perform(get("/exportPdfSubnetIp/27")).andExpect(status().isOk()).andReturn();

        boolean inValidSubnetIdStatus = mvcResult.getResponse().getContentAsString().contains(TraceOrgCommonConstants.STRING_FALSE);

        boolean inValidSubnetIdMessage = mvcResult.getResponse().getContentAsString().contains(TraceOrgMessageConstants.ENTER_VALID_DETAILS);

        Assert.assertTrue(inValidSubnetIdStatus);

        Assert.assertTrue(inValidSubnetIdMessage);
    }

    @Test
    @WithMockUser(username = "testUser", authorities = {"PERM_DASHBOARD_READ"})
    public void givenSubnetIdAndCheckedIpIdWhenInvalidTokenReceivedThenFailureResponseReceivedForExportPdfSubnetIp() throws Exception
    {
        RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/exportPdfSubnetIp/27,920218,920219").header(TraceOrgCommonConstants.ACCESSTOKEN, accessTokenValue);

        Mockito.when(traceOrgCommonUtil.checkToken(accessTokenValue)).thenReturn(TraceOrgCommonConstants.FALSE);

        mvcResult = mockMvc.perform(requestBuilder).andExpect(status().isOk()).andReturn();

        boolean invalidTokenStatus = mvcResult.getResponse().getContentAsString().contains(TraceOrgCommonConstants.STRING_FALSE);

        Assert.assertTrue(invalidTokenStatus);
    }

    @Test
    @WithMockUser(username = "testUser", authorities = {"PERM_DASHBOARD_READ"})
    public void givenSubnetIdAndCheckedIpIdWhenValidTokenReceivedThenSuccessResponseReceivedForExportPdfSubnetIp() throws Exception
    {
        RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/exportPdfSubnetIp/27,920218,920219").header(TraceOrgCommonConstants.ACCESSTOKEN, accessTokenValue);

        Mockito.when(traceOrgCommonUtil.checkToken(accessTokenValue)).thenReturn(TraceOrgCommonConstants.TRUE);

        mvcResult = mockMvc.perform(requestBuilder).andExpect(status().isOk()).andReturn();

        boolean validTokenStatus = mvcResult.getResponse().getContentAsString().contains(TraceOrgCommonConstants.STRING_FALSE);

        boolean invalidTokenMessage = mvcResult.getResponse().getContentAsString().contains(TraceOrgMessageConstants.TOKEN_NOT_RECOGNISED);

        Assert.assertTrue(validTokenStatus);

        Assert.assertFalse(invalidTokenMessage);
    }

    @Test
    @WithMockUser(username = "testUser", authorities = {"PERM_DASHBOARD_READ"})
    public void givenSubnetIdAndCheckedIpIdWhenInvalidSubnetIdReceivedThenFailureResponseReceivedForExportPdfSubnetIp() throws Exception
    {
        String idList = "27,920218,920219";

        String[] subnetIpIdString = idList.split(",");

        RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/exportPdfSubnetIp/"+idList).header(TraceOrgCommonConstants.ACCESSTOKEN, accessTokenValue);

        Mockito.when(traceOrgCommonUtil.checkToken(accessTokenValue)).thenReturn(TraceOrgCommonConstants.TRUE);

        Mockito.when(traceOrgService.isExist(TraceOrgCommonConstants.TRACE_ORG_SUBNET_DETAILS,"id",subnetIpIdString[0])).thenReturn(TraceOrgCommonConstants.FALSE);

        mvcResult = mockMvc.perform(requestBuilder).andExpect(status().isOk()).andReturn();

        boolean inValidSubnetIdStatus = mvcResult.getResponse().getContentAsString().contains(TraceOrgCommonConstants.STRING_FALSE);

        boolean inValidSubnetIdMessage = mvcResult.getResponse().getContentAsString().contains(TraceOrgMessageConstants.ENTER_VALID_DETAILS);

        Assert.assertTrue(inValidSubnetIdStatus);

        Assert.assertTrue(inValidSubnetIdMessage);
    }

    @Test
    @WithMockUser(username = "testUser", authorities = {"PERM_DASHBOARD_READ"})
    public void givenSubnetIdAndCheckedIpIdWhenValidSubnetIdReceivedThenSuccessResponseReceivedForExportPdfSubnetIp() throws Exception
    {
        String idList = "27,920218,920219";

        String[] subnetIpIdString = idList.split(",");

        RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/exportPdfSubnetIp/"+idList).header(TraceOrgCommonConstants.ACCESSTOKEN, accessTokenValue);

        Mockito.when(traceOrgCommonUtil.checkToken(accessTokenValue)).thenReturn(TraceOrgCommonConstants.TRUE);

        Mockito.when(traceOrgService.isExist(TraceOrgCommonConstants.TRACE_ORG_SUBNET_DETAILS,"id",subnetIpIdString[0])).thenReturn(TraceOrgCommonConstants.TRUE);

        mvcResult = mockMvc.perform(requestBuilder).andExpect(status().isOk()).andReturn();

        boolean validSubnetIdStatus = mvcResult.getResponse().getContentAsString().contains(TraceOrgCommonConstants.STRING_FALSE);

        boolean validSubnetIdMessage = mvcResult.getResponse().getContentAsString().contains(TraceOrgMessageConstants.ENTER_VALID_DETAILS);

        Assert.assertTrue(validSubnetIdStatus);

        Assert.assertFalse(validSubnetIdMessage);
    }

    @Test
    @WithMockUser(username = "testUser", authorities = {"PERM_DASHBOARD_READ"})
    public void givenSubnetIdAndCheckedIpIdWhenSubnetIpDetailsListNotReceivedThenFailureResponseReceivedForExportPdfSubnetIp() throws Exception
    {
        String idList = "27,920218";

        String[] subnetIpIdString = idList.split(",");

        List<TraceOrgSubnetIpDetails> subnetIpDetailsList = new ArrayList<>();

        RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/exportPdfSubnetIp/"+idList).header(TraceOrgCommonConstants.ACCESSTOKEN, accessTokenValue);

        Mockito.when(traceOrgCommonUtil.checkToken(accessTokenValue)).thenReturn(TraceOrgCommonConstants.TRUE);

        Mockito.when(traceOrgService.isExist(TraceOrgCommonConstants.TRACE_ORG_SUBNET_DETAILS,"id",subnetIpIdString[0])).thenReturn(TraceOrgCommonConstants.TRUE);

        Mockito.when(traceOrgCommonUtil.getSubnetIpDetailsList(subnetIpIdString)).thenReturn(subnetIpDetailsList);

        mvcResult = mockMvc.perform(requestBuilder).andExpect(status().isOk()).andReturn();

        boolean subnetIpDetailsListStatus = mvcResult.getResponse().getContentAsString().contains(TraceOrgCommonConstants.STRING_FALSE);

        boolean subnetIpDetailsListMessage = mvcResult.getResponse().getContentAsString().contains(TraceOrgMessageConstants.NO_DATA_AVAILABLE);

        Assert.assertTrue(subnetIpDetailsListStatus);

        Assert.assertTrue(subnetIpDetailsListMessage);
    }

    @Test
    @WithMockUser(username = "testUser", authorities = {"PERM_DASHBOARD_READ"})
    public void givenSubnetIdAndCheckedIpIdWhenSubnetIpDetailsListReceivedThenSuccessResponseReceivedForExportPdfSubnetIp() throws Exception
    {
        String idList = "27,920218,920219";

        String[] subnetIpIdString = idList.split(",");

        // for subnet address
        List<TraceOrgSubnetDetails> subnetIpDetails = new ArrayList<>();

        TraceOrgSubnetDetails traceOrgSubnetDetails = new TraceOrgSubnetDetails();

        traceOrgSubnetDetails.setSubnetAddress("10.20.40.0");

        subnetIpDetails.add(traceOrgSubnetDetails);

        // for subnet ip details
        List<TraceOrgSubnetIpDetails> subnetIpDetailsList = new ArrayList<>();

        TraceOrgSubnetIpDetails traceOrgSubnetIpDetails = new TraceOrgSubnetIpDetails();

        traceOrgSubnetIpDetails.setIpAddress("10.20.40.132");

        traceOrgSubnetIpDetails.setStatus(TraceOrgCommonConstants.AVAILABLE);

        traceOrgSubnetIpDetails.setSubnetName("10.20.40.0");

        traceOrgSubnetIpDetails.setSubnetId(traceOrgSubnetDetails);

        traceOrgSubnetIpDetails.setMacAddress("");

        traceOrgSubnetIpDetails.setDeviceType("");

        traceOrgSubnetIpDetails.setIpToDns("");

        traceOrgSubnetIpDetails.setDnsToIp("");

        traceOrgSubnetIpDetails.setAuthenticity("-");

        String jsonString = "{ \"column1\": \"value1\", \"column2\": \"value2\" }";

        ObjectMapper mapper = new ObjectMapper();

        JsonNode jsonNode = mapper.readTree(jsonString);

        traceOrgSubnetIpDetails.setCustomColumns(jsonNode);

        traceOrgSubnetIpDetails.setLastAliveTime(new Date());

        subnetIpDetailsList.add(traceOrgSubnetIpDetails);

        RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/exportPdfSubnetIp/"+idList).header(TraceOrgCommonConstants.ACCESSTOKEN, accessTokenValue);

        Mockito.when(traceOrgCommonUtil.checkToken(accessTokenValue)).thenReturn(TraceOrgCommonConstants.TRUE);

        Mockito.when(traceOrgService.isExist(TraceOrgCommonConstants.TRACE_ORG_SUBNET_DETAILS,"id",subnetIpIdString[0])).thenReturn(TraceOrgCommonConstants.TRUE);

        Mockito.when(this.traceOrgCommonUtil.getSubnetIpDetailsList(subnetIpIdString)).thenReturn(subnetIpDetailsList);

        when(traceOrgCustomColumnRepository.findByColumnAt("subnetIp"))
                .thenReturn(Collections.singletonList(customColumn));

        mvcResult = mockMvc.perform(requestBuilder).andExpect(status().isOk()).andReturn();

        boolean subnetIpDetailsListStatus = mvcResult.getResponse().getContentAsString().contains(TraceOrgCommonConstants.STRING_FALSE);

        boolean subnetIpDetailsListMessage = mvcResult.getResponse().getContentAsString().contains(TraceOrgMessageConstants.NO_DATA_AVAILABLE);

        Assert.assertFalse(subnetIpDetailsListStatus);

        Assert.assertFalse(subnetIpDetailsListMessage);
    }

    @Test
    @WithMockUser(username = "testUser", authorities = {"PERM_DASHBOARD_READ"})
    public void whenSubnetIdNotNullThenSuccessResponseReceivedForCsv() throws Exception
    {
        mvcResult = mockMvc.perform(get("/exportCsvSubnetIp/27")).andExpect(status().isOk()).andReturn();

        boolean inValidSubnetIdStatus = mvcResult.getResponse().getContentAsString().contains(TraceOrgCommonConstants.STRING_FALSE);

        boolean inValidSubnetIdMessage = mvcResult.getResponse().getContentAsString().contains(TraceOrgMessageConstants.ENTER_VALID_DETAILS);

        Assert.assertTrue(inValidSubnetIdStatus);

        Assert.assertTrue(inValidSubnetIdMessage);
    }

    @Test
    @WithMockUser(username = "testUser", authorities = {"PERM_DASHBOARD_READ"})
    public void givenSubnetIdAndCheckedIpIdWhenInvalidTokenReceivedThenFailureResponseReceivedForExportCsvSubnetIp() throws Exception
    {
        RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/exportCsvSubnetIp/27,920218,920219").header(TraceOrgCommonConstants.ACCESSTOKEN, accessTokenValue);

        Mockito.when(traceOrgCommonUtil.checkToken(accessTokenValue)).thenReturn(TraceOrgCommonConstants.FALSE);

        mvcResult = mockMvc.perform(requestBuilder).andExpect(status().isOk()).andReturn();

        boolean invalidTokenStatus = mvcResult.getResponse().getContentAsString().contains(TraceOrgCommonConstants.STRING_FALSE);

        Assert.assertTrue(invalidTokenStatus);
    }

    @Test
    @WithMockUser(username = "testUser", authorities = {"PERM_DASHBOARD_READ"})
    public void givenSubnetIdAndCheckedIpIdWhenValidTokenReceivedThenSuccessResponseReceivedForExportCsvSubnetIp() throws Exception
    {
        RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/exportCsvSubnetIp/27,920218,920219").header(TraceOrgCommonConstants.ACCESSTOKEN, accessTokenValue);

        Mockito.when(traceOrgCommonUtil.checkToken(accessTokenValue)).thenReturn(TraceOrgCommonConstants.TRUE);

        mvcResult = mockMvc.perform(requestBuilder).andExpect(status().isOk()).andReturn();

        boolean validTokenStatus = mvcResult.getResponse().getContentAsString().contains(TraceOrgCommonConstants.STRING_FALSE);

        boolean invalidTokenMessage = mvcResult.getResponse().getContentAsString().contains(TraceOrgMessageConstants.TOKEN_NOT_RECOGNISED);

        Assert.assertTrue(validTokenStatus);

        Assert.assertFalse(invalidTokenMessage);
    }

    @Test
    @WithMockUser(username = "testUser", authorities = {"PERM_DASHBOARD_READ"})
    public void givenSubnetIdAndCheckedIpIdWhenInvalidSubnetIdReceivedThenFailureResponseReceivedForExportCsvSubnetIp() throws Exception
    {
        String idList = "27,920218,920219";

        String[] subnetIpIdString = idList.split(",");

        RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/exportCsvSubnetIp/"+idList).header(TraceOrgCommonConstants.ACCESSTOKEN, accessTokenValue);

        Mockito.when(traceOrgCommonUtil.checkToken(accessTokenValue)).thenReturn(TraceOrgCommonConstants.TRUE);

        Mockito.when(traceOrgService.isExist(TraceOrgCommonConstants.TRACE_ORG_SUBNET_DETAILS,"id",subnetIpIdString[0])).thenReturn(TraceOrgCommonConstants.FALSE);

        mvcResult = mockMvc.perform(requestBuilder).andExpect(status().isOk()).andReturn();

        boolean inValidSubnetIdStatus = mvcResult.getResponse().getContentAsString().contains(TraceOrgCommonConstants.STRING_FALSE);

        boolean inValidSubnetIdMessage = mvcResult.getResponse().getContentAsString().contains(TraceOrgMessageConstants.ENTER_VALID_DETAILS);

        Assert.assertTrue(inValidSubnetIdStatus);

        Assert.assertTrue(inValidSubnetIdMessage);
    }

    @Test
    @WithMockUser(username = "testUser", authorities = {"PERM_DASHBOARD_READ"})
    public void givenSubnetIdAndCheckedIpIdWhenValidSubnetIdReceivedThenSuccessResponseReceivedForExportCsvSubnetIp() throws Exception
    {
        String idList = "27,920218,920219";

        String[] subnetIpIdString = idList.split(",");

        RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/exportCsvSubnetIp/"+idList).header(TraceOrgCommonConstants.ACCESSTOKEN, accessTokenValue);

        Mockito.when(traceOrgCommonUtil.checkToken(accessTokenValue)).thenReturn(TraceOrgCommonConstants.TRUE);

        Mockito.when(traceOrgService.isExist(TraceOrgCommonConstants.TRACE_ORG_SUBNET_DETAILS,"id",subnetIpIdString[0])).thenReturn(TraceOrgCommonConstants.TRUE);

        mvcResult = mockMvc.perform(requestBuilder).andExpect(status().isOk()).andReturn();

        boolean validSubnetIdStatus = mvcResult.getResponse().getContentAsString().contains(TraceOrgCommonConstants.STRING_FALSE);

        boolean validSubnetIdMessage = mvcResult.getResponse().getContentAsString().contains(TraceOrgMessageConstants.ENTER_VALID_DETAILS);

        Assert.assertTrue(validSubnetIdStatus);

        Assert.assertFalse(validSubnetIdMessage);
    }

    @Test
    @WithMockUser(username = "testUser", authorities = {"PERM_DASHBOARD_READ"})
    public void givenSubnetIdAndCheckedIpIdWhenSubnetIpDetailsListNotReceivedThenFailureResponseReceivedForExportCsvSubnetIp() throws Exception
    {
        String idList = "27,920218";

        String[] subnetIpIdString = idList.split(",");

        List<TraceOrgSubnetIpDetails> subnetIpDetailsList = new ArrayList<>();

        RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/exportCsvSubnetIp/"+idList).header(TraceOrgCommonConstants.ACCESSTOKEN, accessTokenValue);

        Mockito.when(traceOrgCommonUtil.checkToken(accessTokenValue)).thenReturn(TraceOrgCommonConstants.TRUE);

        Mockito.when(traceOrgService.isExist(TraceOrgCommonConstants.TRACE_ORG_SUBNET_DETAILS,"id",subnetIpIdString[0])).thenReturn(TraceOrgCommonConstants.TRUE);

        Mockito.when(traceOrgCommonUtil.getSubnetIpDetailsList(subnetIpIdString)).thenReturn(subnetIpDetailsList);

        mvcResult = mockMvc.perform(requestBuilder).andExpect(status().isOk()).andReturn();

        boolean subnetIpDetailsListStatus = mvcResult.getResponse().getContentAsString().contains(TraceOrgCommonConstants.STRING_FALSE);

        boolean subnetIpDetailsListMessage = mvcResult.getResponse().getContentAsString().contains(TraceOrgMessageConstants.NO_DATA_AVAILABLE);

        Assert.assertTrue(subnetIpDetailsListStatus);

        Assert.assertTrue(subnetIpDetailsListMessage);
    }

    @Test
    @WithMockUser(username = "testUser", authorities = {"PERM_DASHBOARD_READ"})
    public void givenSubnetIdAndCheckedIpIdWhenSubnetIpDetailsListReceivedThenSuccessResponseReceivedForExportCsvSubnetIp() throws Exception
    {
        String idList = "27,920218,920219";

        String[] subnetIpIdString = idList.split(",");

        // for subnet address
        List<TraceOrgSubnetDetails> subnetIpDetails = new ArrayList<>();

        TraceOrgSubnetDetails traceOrgSubnetDetails = new TraceOrgSubnetDetails();

        traceOrgSubnetDetails.setSubnetAddress("10.20.40.0");

        subnetIpDetails.add(traceOrgSubnetDetails);

        // for subnet ip details
        List<TraceOrgSubnetIpDetails> subnetIpDetailsList = new ArrayList<>();

        TraceOrgSubnetIpDetails traceOrgSubnetIpDetails = new TraceOrgSubnetIpDetails();

        traceOrgSubnetIpDetails.setIpAddress("10.20.40.132");

        traceOrgSubnetIpDetails.setStatus(TraceOrgCommonConstants.AVAILABLE);

        traceOrgSubnetIpDetails.setSubnetName("10.20.40.0");

        traceOrgSubnetIpDetails.setSubnetId(traceOrgSubnetDetails);

        traceOrgSubnetIpDetails.setMacAddress("");

        traceOrgSubnetIpDetails.setDeviceType("");

        traceOrgSubnetIpDetails.setIpToDns("");

        traceOrgSubnetIpDetails.setDnsToIp("");

        traceOrgSubnetIpDetails.setAuthenticity("-");

        traceOrgSubnetIpDetails.setLastAliveTime(new Date());

        subnetIpDetailsList.add(traceOrgSubnetIpDetails);

        RequestBuilder requestBuilder = MockMvcRequestBuilders.get("/exportCsvSubnetIp/"+idList).header(TraceOrgCommonConstants.ACCESSTOKEN, accessTokenValue);

        Mockito.when(traceOrgCommonUtil.checkToken(accessTokenValue)).thenReturn(TraceOrgCommonConstants.TRUE);

        Mockito.when(traceOrgService.isExist(TraceOrgCommonConstants.TRACE_ORG_SUBNET_DETAILS,"id",subnetIpIdString[0])).thenReturn(TraceOrgCommonConstants.TRUE);

        Mockito.when(this.traceOrgCommonUtil.getSubnetIpDetailsList(subnetIpIdString)).thenReturn(subnetIpDetailsList);

        mvcResult = mockMvc.perform(requestBuilder).andExpect(status().isOk()).andReturn();

        boolean subnetIpDetailsListStatus = mvcResult.getResponse().getContentAsString().contains(TraceOrgCommonConstants.STRING_FALSE);

        boolean subnetIpDetailsListMessage = mvcResult.getResponse().getContentAsString().contains(TraceOrgMessageConstants.NO_DATA_AVAILABLE);

        Assert.assertFalse(subnetIpDetailsListStatus);

        Assert.assertFalse(subnetIpDetailsListMessage);
    }
}

