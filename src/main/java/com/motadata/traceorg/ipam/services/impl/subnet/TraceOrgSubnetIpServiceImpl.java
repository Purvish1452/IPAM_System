package com.motadata.traceorg.ipam.services.impl.subnet;

import com.motadata.traceorg.ipam.entity.rogueDetection.TraceOrgRogueDetection;
import com.motadata.traceorg.ipam.entity.subnet.TraceOrgSubnetDetails;
import com.motadata.traceorg.ipam.logger.TraceOrgLogger;
import com.motadata.traceorg.ipam.repository.rogueDetection.TraceOrgRogueDetectionRepository;
import com.motadata.traceorg.ipam.services.subnet.TraceOrgSubnetIpService;
import com.motadata.traceorg.ipam.services.subnet.TraceOrgSubnetService;
import com.motadata.traceorg.ipam.util.TraceOrgCommonConstants;
import com.motadata.traceorg.ipam.util.TraceOrgMessageConstants;
import com.motadata.traceorg.ipam.util.TraceOrgPDFBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class TraceOrgSubnetIpServiceImpl implements TraceOrgSubnetIpService
{

    private static final TraceOrgLogger _logger = new TraceOrgLogger(TraceOrgSubnetIpServiceImpl.class, "Subnet IP Service");

    @Autowired
    TraceOrgRogueDetectionRepository traceOrgRogueDetectionRepository;

    @Autowired
    TraceOrgSubnetService traceOrgSubnetService;

    /*
     *  IPAM-161 The IPAM solution should be able to create its own widget to display customized subnet reports, free IP, used IP. Dashboard
     *  Added method to export PDF of RecentlyDiscovered
     * **/
    @Override
    public HashMap<String, Object> exportPdfRecentlyDiscovered()
    {
        HashMap<String, Object> result = new HashMap<>();

        try
        {
            List<TraceOrgRogueDetection> traceOrgRogueDetections = traceOrgRogueDetectionRepository.findTop20ByAuthenticityOrderByDiscoveredAtDesc("discovered");

            if (traceOrgRogueDetections != null && !traceOrgRogueDetections.isEmpty()) {

                LinkedHashSet<String> columns = new LinkedHashSet<String>()
                {{
                    add("IP Address");

                    add("Mac Address");

                    add("Discovered At");

                }};

                List<Object> pdfResults = new ArrayList<>();

                List<Object> pdfResult;

                for (TraceOrgRogueDetection traceOrgRogueDetection : traceOrgRogueDetections)
                {
                    pdfResult = new ArrayList<>();

                    pdfResult.add(traceOrgRogueDetection.getIpAddress());

                    pdfResult.add(traceOrgRogueDetection.getMacAddress());

                    pdfResult.add(traceOrgRogueDetection.getDiscoveredAt());

                    pdfResults.add(pdfResult);
                }

                HashMap<String, Object> results = new HashMap<>();

                results.put("grid-result", pdfResults);

                results.put("columns", columns);

                List<HashMap<String, Object>> visualizationResults = new ArrayList<>();

                visualizationResults.add(results);

                HashMap<String, Object> gridReport = new HashMap<>();

                gridReport.put("Title", "Recently Discovered Mac");

                String fileName = "Recently Discovered Mac "+TraceOrgCommonConstants.VISUAL_DATE_FORMAT.format(new Date())+".pdf";

                fileName = fileName.replace(" ","_").replace(":","_").replace(",","");

                TraceOrgPDFBuilder.addGridReport(1, visualizationResults, new HashMap<String, Object>(), fileName, gridReport);

                result.put(TraceOrgCommonConstants.DATA, fileName);

                result.put(TraceOrgCommonConstants.SUCCESS, TraceOrgCommonConstants.TRUE);
            }
            else
            {
                result.put(TraceOrgCommonConstants.SUCCESS, TraceOrgCommonConstants.FALSE);

                result.put(TraceOrgCommonConstants.MESSAGE, TraceOrgMessageConstants.NO_DATA_AVAILABLE);
            }
        }
        catch (Exception exception)
        {
            _logger.error(exception);
        }

        return result;
    }

    /*
     *  IPAM-161 The IPAM solution should be able to create its own widget to display customized subnet reports, free IP, used IP. Dashboard
     *  Added method to export PDF of Top10CategoryUtilization
     * **/
    @Override
    public HashMap<String, Object> exportPdfTop10CategoryUtilization()
    {
        HashMap<String, Object> result = new HashMap<>();

        try
        {

            HashMap<String, Object> top10CategoryUtilization = traceOrgSubnetService.getTop10CategoryUtilization();

            if(top10CategoryUtilization.get(TraceOrgCommonConstants.DATA) != null)
            {
                List<HashMap<String, Object>> categoryUtilizations = (List<HashMap<String, Object>>) top10CategoryUtilization.get(TraceOrgCommonConstants.DATA);

                if (categoryUtilizations != null && !categoryUtilizations.isEmpty())
                {
                    LinkedHashSet<String> columns = new LinkedHashSet<String>()
                    {{
                        add("Category");

                        add("% in Space Used");

                    }};

                    List<Object> pdfResults = new ArrayList<>();

                    List<Object> pdfResult;

                    for (HashMap<String, Object> categoryUtilization : categoryUtilizations)
                    {
                        pdfResult = new ArrayList<>();

                        pdfResult.add(categoryUtilization.get("categoryName"));

                        pdfResult.add(categoryUtilization.get("totalUsedIpPercentage"));

                        pdfResults.add(pdfResult);
                    }

                    HashMap<String, Object> results = new HashMap<>();

                    results.put("grid-result", pdfResults);

                    results.put("columns", columns);

                    List<HashMap<String, Object>> visualizationResults = new ArrayList<>();

                    visualizationResults.add(results);

                    HashMap<String, Object> gridReport = new HashMap<>();

                    gridReport.put("Title", "Top 10 Category Utilization");

                    String fileName = "Top 10 Category Utilization "+TraceOrgCommonConstants.VISUAL_DATE_FORMAT.format(new Date())+".pdf";

                    fileName = fileName.replace(" ","_").replace(":","_").replace(",","");

                    TraceOrgPDFBuilder.addGridReport(1, visualizationResults, new HashMap<String, Object>(), fileName, gridReport);

                    result.put(TraceOrgCommonConstants.DATA, fileName);

                    result.put(TraceOrgCommonConstants.SUCCESS, TraceOrgCommonConstants.TRUE);
                }
            }
            else
            {
                result.put(TraceOrgCommonConstants.SUCCESS, TraceOrgCommonConstants.FALSE);

                result.put(TraceOrgCommonConstants.MESSAGE, TraceOrgMessageConstants.NO_DATA_AVAILABLE);
            }
        }
        catch (Exception exception)
        {
            _logger.error(exception);
        }

        return result;
    }

    /*
     *  IPAM-161 The IPAM solution should be able to create its own widget to display customized subnet reports, free IP, used IP. Dashboard
     *  Added method to export PDF of Top10SubnetUtilization
     * **/
    @Override
    public HashMap<String, Object> exportPdfTop10SubnetUtilization()
    {
        HashMap<String, Object> result = new HashMap<>();

        try
        {
            HashMap<String, Object> top10SubnetUtilization = traceOrgSubnetService.getTop10SubnetUtilization();

            if(top10SubnetUtilization.get(TraceOrgCommonConstants.DATA) != null)
            {
                List<TraceOrgSubnetDetails> traceOrgSubnetDetails = (List<TraceOrgSubnetDetails>) top10SubnetUtilization.get(TraceOrgCommonConstants.DATA);

                if (traceOrgSubnetDetails != null && !traceOrgSubnetDetails.isEmpty())
                {
                    LinkedHashSet<String> columns = new LinkedHashSet<String>()
                    {{
                        add("Subnet");

                        add("% in Space Used");

                    }};

                    List<Object> pdfResults = new ArrayList<>();

                    List<Object> pdfResult;

                    for (TraceOrgSubnetDetails traceOrgRogueDetection : traceOrgSubnetDetails)
                    {
                        pdfResult = new ArrayList<>();

                        pdfResult.add(traceOrgRogueDetection.getSubnetName());

                        pdfResult.add(traceOrgRogueDetection.getUsedIpPercentage());

                        pdfResults.add(pdfResult);
                    }

                    HashMap<String, Object> results = new HashMap<>();

                    results.put("grid-result", pdfResults);

                    results.put("columns", columns);

                    List<HashMap<String, Object>> visualizationResults = new ArrayList<>();

                    visualizationResults.add(results);

                    HashMap<String, Object> gridReport = new HashMap<>();

                    gridReport.put("Title", "Top 10 Subnet Utilization");

                    String fileName = "Top 10 Subnet Utilization" + TraceOrgCommonConstants.VISUAL_DATE_FORMAT.format(new Date())+".pdf";

                    fileName = fileName.replace(" ","_").replace(":","_").replace(",","");

                    TraceOrgPDFBuilder.addGridReport(1, visualizationResults, new HashMap<String, Object>(), fileName, gridReport);

                    result.put(TraceOrgCommonConstants.DATA, fileName);

                    result.put(TraceOrgCommonConstants.SUCCESS, TraceOrgCommonConstants.TRUE);
                }
            }
            else
            {
                result.put(TraceOrgCommonConstants.SUCCESS, TraceOrgCommonConstants.FALSE);

                result.put(TraceOrgCommonConstants.MESSAGE, TraceOrgMessageConstants.NO_DATA_AVAILABLE);
            }
        }
        catch (Exception exception)
        {
            _logger.error(exception);
        }

        return result;
    }
}
