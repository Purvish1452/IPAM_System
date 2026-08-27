package com.motadata.traceorg.ipam.scheduler.dhcp;

import com.motadata.traceorg.ipam.logger.TraceOrgLogger;
import com.motadata.traceorg.ipam.entity.dhcp.TraceOrgDhcpCredentialDetails;
import com.motadata.traceorg.ipam.util.TraceOrgCommonConstants;
import com.motadata.traceorg.ipam.util.TraceOrgCommonUtil;
import org.quartz.*;

import java.util.HashMap;

public class TraceOrgDhcpScanQueue implements Job {

    private final TraceOrgLogger _logger = new TraceOrgLogger(TraceOrgDhcpScanQueue.class, "DHCP Scan Queue");

    /**
     * IPAM-145 : System should have rogue device detection capability
     * Added traceOrgRogueDetectionRepository object on subnet scan details.
     * IPAM-192 : Subnet should be added into respective supernet during DHCP Auto scan
     * added the traceOrgSupernetService into job data map
     * @param jobExecutionContext
     * @throws JobExecutionException
     */
    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException
    {
        try
        {
            JobDataMap dataMap = jobExecutionContext.getMergedJobDataMap();

            HashMap<String,Object> subnetDetails = new HashMap<>();

            TraceOrgCommonUtil traceOrgCommonUtil = (TraceOrgCommonUtil)dataMap.get(TraceOrgCommonConstants.TRACE_ORG_COMMON_UTIL);

            subnetDetails.put("traceOrgDhcpCredentialDetails",dataMap.get("traceOrgDhcpCredentialDetails"));

            subnetDetails.put(TraceOrgCommonConstants.TRACE_ORG_SERVICE,dataMap.get(TraceOrgCommonConstants.TRACE_ORG_SERVICE));

            subnetDetails.put(TraceOrgCommonConstants.TRACE_ORG_ALERT_SERVICE, dataMap.get(TraceOrgCommonConstants.TRACE_ORG_ALERT_SERVICE));

            subnetDetails.put(TraceOrgCommonConstants.TRACE_ORG_ROGUE_DETECTION_REPOSITORY,dataMap.get(TraceOrgCommonConstants.TRACE_ORG_ROGUE_DETECTION_REPOSITORY));

            subnetDetails.put(TraceOrgCommonConstants.TRACE_ORG_COMMON_UTIL,traceOrgCommonUtil);

            subnetDetails.put("traceOrgCiscoDHCPServerUtil",dataMap.get("traceOrgCiscoDHCPServerUtil"));

            subnetDetails.put("traceOrgWindowsDhcpServerUtil",dataMap.get("traceOrgWindowsDhcpServerUtil"));

            if (dataMap.get(TraceOrgCommonConstants.TRACE_ORG_SUPERNET_SERVICE) != null)
            {
                subnetDetails.put(TraceOrgCommonConstants.TRACE_ORG_SUPERNET_SERVICE,dataMap.get(TraceOrgCommonConstants.TRACE_ORG_SUPERNET_SERVICE));
            }

            subnetDetails.put(TraceOrgCommonConstants.SCAN_TYPE,TraceOrgCommonConstants.DHCP_SCAN);

            TraceOrgCommonUtil.m_scheduleScanSubnet.put(((TraceOrgDhcpCredentialDetails)dataMap.get("traceOrgDhcpCredentialDetails")).getHostAddress(),subnetDetails);

        }
        catch (Exception exception)
        {
            _logger.error(exception);
        }
    }
}
