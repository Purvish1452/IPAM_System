package com.motadata.traceorg.ipam.services.impl.subnet;

import com.motadata.traceorg.ipam.entity.dashboard.TraceOrgCategory;
import com.motadata.traceorg.ipam.entity.rogueDetection.TraceOrgRogueDetection;
import com.motadata.traceorg.ipam.entity.subnet.TraceOrgSubnetDetails;
import com.motadata.traceorg.ipam.logger.TraceOrgLogger;
import com.motadata.traceorg.ipam.repository.dashboard.TraceOrgCategoryRepository;
import com.motadata.traceorg.ipam.repository.rogueDetection.TraceOrgRogueDetectionRepository;
import com.motadata.traceorg.ipam.repository.subnet.TraceOrgSubnetDetailsRepository;
import com.motadata.traceorg.ipam.repository.subnet.TraceOrgSubnetIpDetailsRepository;
import com.motadata.traceorg.ipam.services.subnet.TraceOrgSubnetService;
import com.motadata.traceorg.ipam.util.TraceOrgCommonConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TraceOrgSubnetServiceImpl implements TraceOrgSubnetService
{

    private static final TraceOrgLogger _logger = new TraceOrgLogger(TraceOrgSubnetServiceImpl.class, "Subnet Service");

    @Autowired
    TraceOrgCategoryRepository traceOrgCategoryRepository;

    @Autowired
    TraceOrgSubnetDetailsRepository traceOrgSubnetDetailsRepository;

    @Autowired
    TraceOrgSubnetIpDetailsRepository traceOrgSubnetIpDetailsRepository;

    @Autowired
    TraceOrgRogueDetectionRepository traceOrgRogueDetectionRepository;

    /*
     *  IPAM-161 The IPAM solution should be able to create its own widget to display customized subnet reports, free IP, used IP. Dashboard
     *  Added method to get Top10SubnetUtilization
     * **/
    @Override
    public HashMap<String, Object> getTop10SubnetUtilization()
    {
        HashMap<String, Object>  result = new HashMap<>();

        try
        {
            List<TraceOrgSubnetDetails> traceOrgSubnetDetails = traceOrgSubnetDetailsRepository.findTop10ByUtilization();

            if(traceOrgSubnetDetails != null)
            {
                result.put(TraceOrgCommonConstants.SUCCESS, TraceOrgCommonConstants.TRUE);

                result.put(TraceOrgCommonConstants.DATA, traceOrgSubnetDetails);
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
     *  Added method to get getTop10CategoryUtilization
     * **/
    @Override
    public HashMap<String, Object> getTop10CategoryUtilization()
    {
        HashMap<String, Object>  result = new HashMap<>();

        try
        {
            List<TraceOrgCategory> traceOrgCategoryList = traceOrgCategoryRepository.findAll();

            List<TraceOrgSubnetDetails> subnetDetailsList = traceOrgSubnetDetailsRepository.findAll();

            if (!traceOrgCategoryList.isEmpty())
            {
                List<HashMap<String, Object>> subnetByCategory = traceOrgCategoryList.stream()
                        .map(traceOrgCategory -> {

                            List<TraceOrgSubnetDetails> filteredSubnets = subnetDetailsList.stream()
                                    .filter(subnet -> subnet.getTraceOrgCategory() != null &&
                                            subnet.getTraceOrgCategory().getId().equals(traceOrgCategory.getId()))
                                    .collect(Collectors.toList());

                            double totalUsedIp = filteredSubnets.stream()
                                    .mapToDouble(TraceOrgSubnetDetails::getUsedIp)
                                    .sum();

                            double totalIp = filteredSubnets.stream()
                                    .mapToDouble(TraceOrgSubnetDetails::getTotalIp)
                                    .sum();

                            double totalUsedIpPercentage = (totalIp > 0) ? (totalUsedIp * 100) / totalIp : 0.0;

                            HashMap<String, Object> categoryDetails = new HashMap<>();

                            categoryDetails.put("categoryName", traceOrgCategory.getCategoryName());

                            categoryDetails.put("id", traceOrgCategory.getId());

                            categoryDetails.put("totalUsedIpPercentage", totalUsedIpPercentage);

                            int severity = 0;

                            if(totalIp>0)
                            {
                                if(totalUsedIpPercentage < 50)
                                {
                                    severity = 3;
                                }
                                else if(totalUsedIpPercentage >= 50 && totalUsedIpPercentage <80)
                                {
                                    severity = 2;
                                }
                                else if(totalUsedIpPercentage>= 80)
                                {
                                    severity = 1;
                                }
                            }
                            else
                            {
                                severity = 3;
                            }

                            categoryDetails.put("severity", severity);

                            return categoryDetails;
                        })
                        .sorted((a, b) -> Double.compare((Double) b.get("totalUsedIpPercentage"), (Double) a.get("totalUsedIpPercentage")))
                        .limit(10)
                        .collect(Collectors.toList());

                result.put(TraceOrgCommonConstants.DATA, subnetByCategory);

                result.put(TraceOrgCommonConstants.SUCCESS, TraceOrgCommonConstants.TRUE);
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
     *  Added method to get dnsStatusSummary
     *  IPAM-197 IPAM | DNS Status Summary widget is showing incorrect percentage of the respective status of ips.
     *  Convert the count to percentage to flot value
     * **/
    @Override
    public HashMap<String, Object> dnsStatusSummary()
    {
        HashMap<String, Object> result = new HashMap<>();

        try
        {
            List<Object[]> statuses = traceOrgSubnetIpDetailsRepository.findGroupedByDnsStatus();

            HashMap<String, Float> summary = new HashMap<>();

            int totalCount = 0;

            summary.put("forwardFailed", 0F);

            summary.put("forwardMismatch", 0F);

            summary.put("success", 0F);

            summary.put("reverseFailed", 0F);

            summary.put("NA", 0F);

            for (Object[] status : statuses)
            {
                String dnsStatus = (String) status[0];

                Long count = (Long) status[1];

                totalCount += count;

                if (dnsStatus != null && !dnsStatus.isEmpty())
                {
                    switch (dnsStatus)
                    {
                        case TraceOrgCommonConstants.FORWARD_DNS_FAILED:

                            dnsStatus = "forwardFailed";

                            break;

                        case TraceOrgCommonConstants.FORWARD_DNS_IP_MISMATCH:

                            dnsStatus = "forwardMismatch";

                            break;

                        case TraceOrgCommonConstants.SUCCESS:

                            dnsStatus = "success";

                            break;

                        case TraceOrgCommonConstants.REVERSE_DNS_FAILED:

                            dnsStatus = "reverseFailed";

                            break;

                        default:

                            dnsStatus = "NA";

                            break;
                    }
                }
                else
                {
                    dnsStatus = "NA";
                }

                summary.put(dnsStatus, Float.valueOf(count));
            }

            HashMap<String, Float> percentage =  new HashMap<>();

            for (String status : summary.keySet())
            {
                percentage.put(status + "_percentage", (summary.get(status) * 100F) / totalCount);
            }

            summary.putAll(percentage);

            result.put(TraceOrgCommonConstants.DATA, summary);

            result.put(TraceOrgCommonConstants.SUCCESS, TraceOrgCommonConstants.TRUE);
        }
        catch (Exception exception)
        {
            _logger.error(exception);
        }

        return result;
    }

    /*
     *  IPAM-161 The IPAM solution should be able to create its own widget to display customized subnet reports, free IP, used IP. Dashboard
     *  Added method to get recentDiscovered
     * **/
    @Override
    public HashMap<String, Object> recentDiscovered()
    {
        HashMap<String, Object> result = new HashMap<>();

        try
        {
            List<TraceOrgRogueDetection> traceOrgRogueDetections = traceOrgRogueDetectionRepository.findTop20ByAuthenticityOrderByDiscoveredAtDesc("discovered");

            result.put(TraceOrgCommonConstants.DATA, traceOrgRogueDetections);

            result.put(TraceOrgCommonConstants.SUCCESS, TraceOrgCommonConstants.TRUE);
        }
        catch (Exception exception)
        {
            _logger.error(exception);
        }

        return result;
    }
}
