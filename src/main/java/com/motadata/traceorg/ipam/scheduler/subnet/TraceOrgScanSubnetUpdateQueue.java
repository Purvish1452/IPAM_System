package com.motadata.traceorg.ipam.scheduler.subnet;

import com.motadata.traceorg.ipam.logger.TraceOrgLogger;
import com.motadata.traceorg.ipam.entity.subnet.TraceOrgSubnetDetails;
import com.motadata.traceorg.ipam.repository.rogueDetection.TraceOrgRogueDetectionRepository;
import com.motadata.traceorg.ipam.services.TraceOrgService;
import com.motadata.traceorg.ipam.services.alert.TraceOrgAlertService;
import com.motadata.traceorg.ipam.services.messaging.TraceOrgMessageSender;
import com.motadata.traceorg.ipam.services.supernet.TraceOrgSupernetService;
import com.motadata.traceorg.ipam.util.TraceOrgCommonConstants;
import com.motadata.traceorg.ipam.util.TraceOrgCommonUtil;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.util.HashMap;

public class TraceOrgScanSubnetUpdateQueue implements Job {

    private final TraceOrgLogger _logger = new TraceOrgLogger(TraceOrgScanSubnetUpdateQueue.class, "GUI / Scan Subnet Update Queue ExecuteJob");

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

            TraceOrgCommonUtil traceOrgCommonUtil = (TraceOrgCommonUtil) dataMap.get(TraceOrgCommonConstants.TRACE_ORG_COMMON_UTIL);

            TraceOrgService traceOrgService= (TraceOrgService) dataMap.get(TraceOrgCommonConstants.TRACE_ORG_SERVICE);

            TraceOrgRogueDetectionRepository traceOrgRogueDetectionRepository = (TraceOrgRogueDetectionRepository) dataMap.get(TraceOrgCommonConstants.TRACE_ORG_ROGUE_DETECTION_REPOSITORY);

            TraceOrgAlertService traceOrgAlertService = (TraceOrgAlertService) dataMap.get(TraceOrgCommonConstants.TRACE_ORG_ALERT_SERVICE);

            TraceOrgSubnetDetails traceOrgSubnetDetails = (TraceOrgSubnetDetails) dataMap.get("subnetDetails");

            TraceOrgSupernetService traceOrgSupernetService = (TraceOrgSupernetService) dataMap.get(TraceOrgCommonConstants.TRACE_ORG_SUPERNET_SERVICE);

            HashMap<String,Object> mapData = new HashMap<>();

            mapData.put("subnetDetails",traceOrgSubnetDetails);

            mapData.put(TraceOrgCommonConstants.TRACE_ORG_SERVICE,traceOrgService);

            mapData.put(TraceOrgCommonConstants.TRACE_ORG_ROGUE_DETECTION_REPOSITORY, traceOrgRogueDetectionRepository);

            mapData.put(TraceOrgCommonConstants.TRACE_ORG_ALERT_SERVICE, traceOrgAlertService);

            mapData.put(TraceOrgCommonConstants.TRACE_ORG_COMMON_UTIL,traceOrgCommonUtil);

            mapData.put(TraceOrgCommonConstants.TRACE_ORG_SUPERNET_SERVICE,traceOrgSupernetService);

            mapData.put("id",traceOrgSubnetDetails.getSubnetName());

            mapData.put(TraceOrgCommonConstants.SCAN_TYPE,TraceOrgCommonConstants.SUBNET_SCAN);

            TraceOrgCommonUtil.m_scheduleScanSubnet.put(traceOrgSubnetDetails.getSubnetAddress(),mapData);

        }
        catch (Exception exception)
        {
            _logger.error(exception);
        }
    }
}
