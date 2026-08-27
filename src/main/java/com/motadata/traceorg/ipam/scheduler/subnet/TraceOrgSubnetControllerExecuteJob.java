package com.motadata.traceorg.ipam.scheduler.subnet;

import com.motadata.traceorg.ipam.logger.TraceOrgLogger;
import com.motadata.traceorg.ipam.entity.event.TraceOrgEvent;
import com.motadata.traceorg.ipam.entity.subnet.TraceOrgSubnetDetails;
import com.motadata.traceorg.ipam.entity.subnet.TraceOrgSubnetIpDetails;
import com.motadata.traceorg.ipam.repository.rogueDetection.TraceOrgRogueDetectionRepository;
import com.motadata.traceorg.ipam.services.TraceOrgService;
import com.motadata.traceorg.ipam.services.alert.TraceOrgAlertService;
import com.motadata.traceorg.ipam.util.*;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Chaitas.
 */

@SuppressWarnings("ALL")
public class TraceOrgSubnetControllerExecuteJob implements Job
{
    private final TraceOrgLogger _logger = new TraceOrgLogger(TraceOrgSubnetControllerExecuteJob.class, "Subnet Execute Job Controller");

    /**
     * IPAM-142 : IPAM | Alert notification for Monitor IP capacity and receive alerts on IP depletion
     * Refactor execute and added inpectAlert method to inspect alert
     *
     * IPAM-145 : System should have rogue device detection capability
     * Added the logic of mark authenticity during scaning and get all rogue ips.
     *
     * IPAM-174 : IPAM | Duplicate Emails sent on the same day for repeat mode for Report Scheduler
     * added the isManualSubnetScan flag for audit
     * */
    public void execute(JobExecutionContext context) throws JobExecutionException
    {
        try
        {
            JobDataMap dataMap = context.getMergedJobDataMap();

            List<String> rogueIps = new ArrayList<>();

            TraceOrgSubnetDetails traceOrgSubnetDetails = (TraceOrgSubnetDetails) dataMap.get("subnetDetails");

            TraceOrgAlertService traceOrgAlertService = (TraceOrgAlertService) dataMap.get(TraceOrgCommonConstants.TRACE_ORG_ALERT_SERVICE);

            TraceOrgCommonUtil traceOrgCommonUtil = (TraceOrgCommonUtil) dataMap.get(TraceOrgCommonConstants.TRACE_ORG_COMMON_UTIL);

            TraceOrgCommonUtil.m_scanStatus = new AtomicInteger(TraceOrgCommonConstants.SCAN_TRUE);

            TraceOrgCommonUtil.m_scanSubnet.put("jobKey", traceOrgSubnetDetails.getSubnetName());

            _logger.info("Start Scanning subnet : " + traceOrgSubnetDetails.getSubnetAddress());

            TraceOrgService traceOrgService = (TraceOrgService) dataMap.get(TraceOrgCommonConstants.TRACE_ORG_SERVICE);

            boolean isManualSubnetScan = dataMap.get(TraceOrgCommonConstants.MANUAL_SUBNET_SCAN) != null ? (boolean)dataMap.get(TraceOrgCommonConstants.MANUAL_SUBNET_SCAN) : false;

            String currentUserName = dataMap.get(TraceOrgCommonConstants.USER_NAME) != null ? TraceOrgCommonUtil.getStringValue(dataMap.get(TraceOrgCommonConstants.USER_NAME)): "";

            TraceOrgRogueDetectionRepository traceOrgRogueDetectionRepository = (TraceOrgRogueDetectionRepository) dataMap.get(TraceOrgCommonConstants.TRACE_ORG_ROGUE_DETECTION_REPOSITORY);

            if (traceOrgSubnetDetails != null && traceOrgService.isExist(TraceOrgCommonConstants.TRACE_ORG_SUBNET_DETAILS,"id", traceOrgSubnetDetails.getId().toString()))
            {
                if(traceOrgSubnetDetails.getType().equalsIgnoreCase("Normal"))
                {
                    new TraceOrgSubnetUtil().getIPFromSubnet(traceOrgSubnetDetails, traceOrgService, traceOrgRogueDetectionRepository, rogueIps, traceOrgCommonUtil);
                }
                else if(traceOrgSubnetDetails.getType().equalsIgnoreCase(TraceOrgCommonConstants.CISCO))
                {
                    new TraceOrgCiscoDHCPServerUtil().getNetworkInterfaceForSpecificSubnet(traceOrgSubnetDetails,traceOrgService, traceOrgRogueDetectionRepository, rogueIps, traceOrgCommonUtil);
                }
                else if(traceOrgSubnetDetails.getType().equalsIgnoreCase(TraceOrgCommonConstants.WINDOWS))
                {
                    new TraceOrgWindowsDhcpServerUtil().getIpDetailsBySubnet(traceOrgSubnetDetails,traceOrgService, traceOrgRogueDetectionRepository, rogueIps, traceOrgCommonUtil);
                }

                TraceOrgSubnetDetails updatedTraceOrgSubnetDetails = (TraceOrgSubnetDetails) traceOrgService.getById(TraceOrgCommonConstants.TRACE_ORG_SUBNET_DETAILS, traceOrgSubnetDetails.getId());

                insertStatastics(traceOrgService, traceOrgSubnetDetails, updatedTraceOrgSubnetDetails);

                inspectAlert(traceOrgCommonUtil, traceOrgSubnetDetails, updatedTraceOrgSubnetDetails, rogueIps);

                insertAudit(traceOrgService, traceOrgSubnetDetails, isManualSubnetScan, currentUserName);

                _logger.debug("Subnet " + traceOrgSubnetDetails.getSubnetAddress() + " is scanned.");

                TraceOrgCommonUtil.m_scanSubnet.put("jobKey", "");

                TraceOrgCommonUtil.m_scanStatus = new AtomicInteger(TraceOrgCommonConstants.SCAN_FALSE);
            }
        }
        catch (Exception exception)
        {
            _logger.error(exception);
        }

    }

    private void insertStatastics(TraceOrgService traceOrgService, TraceOrgSubnetDetails traceOrgSubnetDetails, TraceOrgSubnetDetails updatedTraceOrgSubnetDetails)
    {
        try
        {
            List<TraceOrgSubnetIpDetails> totalSubnetIpDetailsList = (List<TraceOrgSubnetIpDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_SUBNET_IP_DETAILS +" where subnetId = '"+updatedTraceOrgSubnetDetails.getId()+"' and  deactiveStatus = false");

            List<TraceOrgSubnetIpDetails> availableSubnetIpDetailsList = (List<TraceOrgSubnetIpDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.SUBNET_IP_DETAILS_BY_STATUS_AND_SUBNET_ID.replace(TraceOrgCommonConstants.STATUS_VALUE,TraceOrgCommonConstants.AVAILABLE).replace(TraceOrgCommonConstants.SUBNET_ID_VALUE, TraceOrgCommonUtil.getStringValue(updatedTraceOrgSubnetDetails.getId()))+" and  deactiveStatus = false");

            List<TraceOrgSubnetIpDetails> usedSubnetIpDetailsList = (List<TraceOrgSubnetIpDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.SUBNET_IP_DETAILS_BY_STATUS_AND_SUBNET_ID.replace(TraceOrgCommonConstants.STATUS_VALUE,TraceOrgCommonConstants.USED).replace(TraceOrgCommonConstants.SUBNET_ID_VALUE,TraceOrgCommonUtil.getStringValue(updatedTraceOrgSubnetDetails.getId())) +" and  deactiveStatus = false");

            List<TraceOrgSubnetIpDetails> transientSubnetIpDetailsList = (List<TraceOrgSubnetIpDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.SUBNET_IP_DETAILS_BY_STATUS_AND_SUBNET_ID.replace(TraceOrgCommonConstants.STATUS_VALUE,TraceOrgCommonConstants.TRANSIENT).replace(TraceOrgCommonConstants.SUBNET_ID_VALUE,TraceOrgCommonUtil.getStringValue(updatedTraceOrgSubnetDetails.getId())) +" and  deactiveStatus = false");

            updatedTraceOrgSubnetDetails.setAvailableIp((long) availableSubnetIpDetailsList.size());

            updatedTraceOrgSubnetDetails.setUsedIp((long) usedSubnetIpDetailsList.size());

            updatedTraceOrgSubnetDetails.setTransientIp((long) transientSubnetIpDetailsList.size());

            updatedTraceOrgSubnetDetails.setTotalIp((long) totalSubnetIpDetailsList.size());

            updatedTraceOrgSubnetDetails.setLastScanTime(new Date());

            updatedTraceOrgSubnetDetails.setModifiedDate(new Date());

            traceOrgService.insert(updatedTraceOrgSubnetDetails);
        }
        catch (Exception exception)
        {
            _logger.error(exception);
        }
    }

    /**
     * IPAM-165 IPAM | Errors occurred in Logs when we run Database Maintenance Scheduler
     * We are getting an error because, in the case of a DHCP scan, we do not get traceOrgAlertService (since we do not need to inspect alerts in the case of DHCP scan).
     *
     * IPAM-145 : System should have rogue device detection capability
     * Added the logic of new rogue ip if  rogue detection flag is on and found any rogue ips genrate the alert.
     * IPAM-149 : IPAM Roadmap : System should have alert notification module to configure different kind of alert notification
     * Added a queue system using ActiveMQ to ensure that the actual subnet scan is not blocked for all alerts.
     * **/
    private void  inspectAlert(TraceOrgCommonUtil traceOrgCommonUtil, TraceOrgSubnetDetails traceOrgSubnetDetails, TraceOrgSubnetDetails updatedTraceOrgSubnetDetails, List<String> rogueIps)
    {
        try
        {
            String workType = TraceOrgCommonConstants.IP_UTILIZATION
                    + TraceOrgCommonConstants.VALUE_SEPARATOR + TraceOrgCommonConstants.ROGUE_DETECTION
                    + TraceOrgCommonConstants.VALUE_SEPARATOR + TraceOrgCommonConstants.IP_UTILIZATION_BELOW;

            HashMap<String, Object> context = new HashMap<>();

            context.put("subnetAddress", traceOrgSubnetDetails.getSubnetAddress());

            context.put("subnetId", traceOrgSubnetDetails.getId());

            context.put("usedIpPercentage", updatedTraceOrgSubnetDetails.getUsedIpPercentage());

            context.put("rogueIps", rogueIps);

            traceOrgCommonUtil.sendMessage(TraceOrgCommonConstants.ALERT_QUEUE, context, workType);

        }
        catch (Exception exception)
        {
            _logger.error(exception);
        }
    }

    /**
     * IPAM-174 : IPAM | Duplicate Emails sent on the same day for repeat mode for Report Scheduler
     * changes in insertAudit method for manual subnet scan audit
     *
     * @param traceOrgService
     * @param traceOrgSubnetDetails
     * @param isManualSubnetScan
     * @param currentUserName
     */
    private void insertAudit(TraceOrgService traceOrgService, TraceOrgSubnetDetails traceOrgSubnetDetails, boolean isManualSubnetScan, String currentUserName)
    {
        try
        {
            TraceOrgEvent traceOrgEvent = new TraceOrgEvent();

            traceOrgEvent.setTimestamp(new Date());

            if (isManualSubnetScan)
            {
                if (traceOrgService.findByUserName(currentUserName) != null)
                {
                    traceOrgEvent.setDoneBy(traceOrgService.findByUserName(currentUserName));
                }

                traceOrgEvent.setEventContext("Subnet " + traceOrgSubnetDetails.getSubnetAddress() + " is scanned in IP Address Manager by " + currentUserName);
            }
            else
            {
                traceOrgEvent.setEventContext("Subnet " + traceOrgSubnetDetails.getSubnetAddress() + " is scanned in IP Address Manager by Scheduler");
            }

            traceOrgEvent.setEventType("Scan Subnet");

            traceOrgEvent.setSeverity(2);

            traceOrgService.insert(traceOrgEvent);
        }
        catch (Exception exception)
        {
            _logger.error(exception);
        }
    }
}
