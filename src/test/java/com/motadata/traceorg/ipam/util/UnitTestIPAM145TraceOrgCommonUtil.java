package com.motadata.traceorg.ipam.util;

import com.motadata.traceorg.ipam.entity.subnet.TraceOrgSubnetIpDetails;
import com.motadata.traceorg.ipam.services.TraceOrgService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.*;

import java.util.*;

import static org.mockito.Mockito.*;

public class UnitTestIPAM145TraceOrgCommonUtil
{

    @InjectMocks
    private TraceOrgCommonUtil traceOrgCommonUtil;

    @Mock
    private TraceOrgService traceOrgService;

    @Before
    public void setUp()
    {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testExportSubnetIpReportByTimeline_UsedIp_Pdf()
    {
        HashMap<String, Object> result = new HashMap<>();

        List<TraceOrgSubnetIpDetails> subnetIpDetailsList = new ArrayList<>();

        subnetIpDetailsList.add(new TraceOrgSubnetIpDetails());

        when(traceOrgService.commonQuery(anyString())).thenReturn((List) subnetIpDetailsList);

        traceOrgCommonUtil.exportSubnetIpReportByTimeline(result, "1", "USED IP", 1, true);

        Assert.assertTrue(result.get(TraceOrgCommonConstants.SUCCESS).equals(TraceOrgCommonConstants.TRUE));
    }

    @Test
    public void testExportSubnetIpReportByTimeline_UsedIp_Pdf_EmptyList()
    {
        HashMap<String, Object> result = new HashMap<>();

        List<TraceOrgSubnetIpDetails> subnetIpDetailsList = new ArrayList<>();

        when(traceOrgService.commonQuery(anyString())).thenReturn((List) subnetIpDetailsList);

        traceOrgCommonUtil.exportSubnetIpReportByTimeline(result, "1", "USED IP", 1, true);

        Assert.assertTrue(result.get(TraceOrgCommonConstants.SUCCESS).equals(TraceOrgCommonConstants.FALSE));
    }

    @Test
    public void testExportSubnetIpReportByTimeline_AvailableIp_Csv()
    {
        HashMap<String, Object> result = new HashMap<>();

        List<TraceOrgSubnetIpDetails> subnetIpDetailsList = new ArrayList<>();

        subnetIpDetailsList.add(new TraceOrgSubnetIpDetails());

        when(traceOrgService.commonQuery(anyString())).thenReturn((List)subnetIpDetailsList);

        traceOrgCommonUtil.exportSubnetIpReportByTimeline(result, "1", "AVAILABLE IP", 2, false);

        Assert.assertTrue(result.get(TraceOrgCommonConstants.SUCCESS).equals(TraceOrgCommonConstants.TRUE));
    }

    @Test
    public void testExportSubnetIpReportByTimeline_AvailableIp_Csv_EmptyList()
    {
        HashMap<String, Object> result = new HashMap<>();

        List<TraceOrgSubnetIpDetails> subnetIpDetailsList = new ArrayList<>();

        when(traceOrgService.commonQuery(anyString())).thenReturn((List)subnetIpDetailsList);

        traceOrgCommonUtil.exportSubnetIpReportByTimeline(result, "1", "AVAILABLE IP", 2, false);

        Assert.assertTrue(result.get(TraceOrgCommonConstants.SUCCESS).equals(TraceOrgCommonConstants.FALSE));
    }

    @Test
    public void testExportSubnetIpReportByTimeline_ReservedIp_Pdf()
    {
        HashMap<String, Object> result = new HashMap<>();

        List<TraceOrgSubnetIpDetails> subnetIpDetailsList = new ArrayList<>();

        subnetIpDetailsList.add(new TraceOrgSubnetIpDetails());

        when(traceOrgService.commonQuery(anyString())).thenReturn((List)subnetIpDetailsList);

        traceOrgCommonUtil.exportSubnetIpReportByTimeline(result, "1", "RESERVED IP", 3, true);

        Assert.assertTrue(result.get(TraceOrgCommonConstants.SUCCESS).equals(TraceOrgCommonConstants.TRUE));
    }

    @Test
    public void testExportSubnetIpReportByTimeline_ReservedIp_Pdf_EmptyList()
    {
        HashMap<String, Object> result = new HashMap<>();

        List<TraceOrgSubnetIpDetails> subnetIpDetailsList = new ArrayList<>();

        when(traceOrgService.commonQuery(anyString())).thenReturn((List)subnetIpDetailsList);

        traceOrgCommonUtil.exportSubnetIpReportByTimeline(result, "1", "RESERVED IP", 3, true);

        Assert.assertTrue(result.get(TraceOrgCommonConstants.SUCCESS).equals(TraceOrgCommonConstants.FALSE));
    }

    @Test
    public void testExportSubnetIpReportByTimeline_TransientIp_Csv()
    {
        HashMap<String, Object> result = new HashMap<>();

        List<TraceOrgSubnetIpDetails> subnetIpDetailsList = new ArrayList<>();

        subnetIpDetailsList.add(new TraceOrgSubnetIpDetails());

        when(traceOrgService.commonQuery(anyString())).thenReturn((List)subnetIpDetailsList);

        traceOrgCommonUtil.exportSubnetIpReportByTimeline(result, "1", "TRANSIENT IP", 4, false);

        Assert.assertTrue(result.get(TraceOrgCommonConstants.SUCCESS).equals(TraceOrgCommonConstants.TRUE));
    }

    @Test
    public void testExportSubnetIpReportByTimeline_TransientIp_Csv_EmptyList()
    {
        HashMap<String, Object> result = new HashMap<>();

        List<TraceOrgSubnetIpDetails> subnetIpDetailsList = new ArrayList<>();

        when(traceOrgService.commonQuery(anyString())).thenReturn((List)subnetIpDetailsList);

        traceOrgCommonUtil.exportSubnetIpReportByTimeline(result, "1", "TRANSIENT IP", 4, false);

        Assert.assertTrue(result.get(TraceOrgCommonConstants.SUCCESS).equals(TraceOrgCommonConstants.FALSE));
    }

    @Test
    public void testExportSubnetIpReportByTimeline_AllIp_Pdf()
    {
        HashMap<String, Object> result = new HashMap<>();

        List<TraceOrgSubnetIpDetails> subnetIpDetailsList = new ArrayList<>();

        subnetIpDetailsList.add(new TraceOrgSubnetIpDetails());

        when(traceOrgService.commonQuery(anyString())).thenReturn((List)subnetIpDetailsList);

        traceOrgCommonUtil.exportSubnetIpReportByTimeline(result, "1", "ALL IP", 5, true);

        Assert.assertTrue(result.get(TraceOrgCommonConstants.SUCCESS).equals(TraceOrgCommonConstants.TRUE));
    }

    @Test
    public void testExportSubnetIpReportByTimeline_AllIp_Pdf_EmptyList()
    {
        HashMap<String, Object> result = new HashMap<>();

        List<TraceOrgSubnetIpDetails> subnetIpDetailsList = new ArrayList<>();

        when(traceOrgService.commonQuery(anyString())).thenReturn((List)subnetIpDetailsList);

        traceOrgCommonUtil.exportSubnetIpReportByTimeline(result, "1", "ALL IP", 5, true);

        Assert.assertTrue(result.get(TraceOrgCommonConstants.SUCCESS).equals(TraceOrgCommonConstants.FALSE));
    }

    @Test
    public void testExportSubnetIpReportByTimeline_RogueIp_Csv()
    {
        HashMap<String, Object> result = new HashMap<>();

        List<TraceOrgSubnetIpDetails> subnetIpDetailsList = new ArrayList<>();

        subnetIpDetailsList.add(new TraceOrgSubnetIpDetails());

        when(traceOrgService.commonQuery(anyString())).thenReturn((List)subnetIpDetailsList);

        traceOrgCommonUtil.exportSubnetIpReportByTimeline(result, "1", "ROGUE IP", 6, false);

        Assert.assertTrue(result.get(TraceOrgCommonConstants.SUCCESS).equals(TraceOrgCommonConstants.TRUE));
    }

    @Test
    public void testExportSubnetIpReportByTimeline_RogueIp_Csv_EmptyList()
    {
        HashMap<String, Object> result = new HashMap<>();

        List<TraceOrgSubnetIpDetails> subnetIpDetailsList = new ArrayList<>();

        when(traceOrgService.commonQuery(anyString())).thenReturn((List)subnetIpDetailsList);

        traceOrgCommonUtil.exportSubnetIpReportByTimeline(result, "1", "ROGUE IP", 6, false);

        Assert.assertTrue(result.get(TraceOrgCommonConstants.SUCCESS).equals(TraceOrgCommonConstants.FALSE));
    }

    @Test
    public void testExportSubnetIpReportByTimeline_TrustedIp_Pdf()
    {
        HashMap<String, Object> result = new HashMap<>();

        List<TraceOrgSubnetIpDetails> subnetIpDetailsList = new ArrayList<>();

        subnetIpDetailsList.add(new TraceOrgSubnetIpDetails());

        when(traceOrgService.commonQuery(anyString())).thenReturn((List)subnetIpDetailsList);

        traceOrgCommonUtil.exportSubnetIpReportByTimeline(result, "1", "TRUSTED IP", 7, true);

        Assert.assertTrue(result.get(TraceOrgCommonConstants.SUCCESS).equals(TraceOrgCommonConstants.TRUE));
    }

    @Test
    public void testExportSubnetIpReportByTimeline_TrustedIp_Pdf_EmptyList()
    {
        HashMap<String, Object> result = new HashMap<>();

        List<TraceOrgSubnetIpDetails> subnetIpDetailsList = new ArrayList<>();

        when(traceOrgService.commonQuery(anyString())).thenReturn((List)subnetIpDetailsList);

        traceOrgCommonUtil.exportSubnetIpReportByTimeline(result, "1", "TRUSTED IP", 7, true);

        Assert.assertTrue(result.get(TraceOrgCommonConstants.SUCCESS).equals(TraceOrgCommonConstants.FALSE));
    }

    @Test
    public void testExportSubnetIpReportByTimeline_VendorSummary_Csv_EmptyList()
    {
        HashMap<String, Object> result = new HashMap<>();

        List<Map<String, Object>> vendorSummaryList = new ArrayList<>();

        when(traceOrgService.commonQuery(anyString())).thenReturn((List)vendorSummaryList);

        traceOrgCommonUtil.exportSubnetIpReportByTimeline(result, "1", "VENDOR SUMMARY", 8, false);

        Assert.assertTrue(result.get(TraceOrgCommonConstants.SUCCESS).equals(TraceOrgCommonConstants.FALSE));
    }

    @Test
    public void testExportSubnetIpReportByTimeline_InvalidIpStatus()
    {
        HashMap<String, Object> result = new HashMap<>();

        traceOrgCommonUtil.exportSubnetIpReportByTimeline(result, "1", "INVALID IP", 9, true);

        Assert.assertTrue(result.get(TraceOrgCommonConstants.SUCCESS).equals(TraceOrgCommonConstants.FALSE));

        Assert.assertTrue(result.get(TraceOrgCommonConstants.MESSAGE).equals(TraceOrgMessageConstants.NO_DATA_AVAILABLE));
    }
}