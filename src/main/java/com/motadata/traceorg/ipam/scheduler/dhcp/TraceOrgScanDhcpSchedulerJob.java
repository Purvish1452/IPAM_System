package com.motadata.traceorg.ipam.scheduler.dhcp;

import com.motadata.traceorg.ipam.entity.settings.TraceOrgUser;
import com.motadata.traceorg.ipam.logger.TraceOrgLogger;
import com.motadata.traceorg.ipam.entity.*;
import com.motadata.traceorg.ipam.entity.dashboard.TraceOrgCategory;
import com.motadata.traceorg.ipam.entity.dhcp.TraceOrgDhcpCredentialDetails;
import com.motadata.traceorg.ipam.entity.event.TraceOrgEvent;
import com.motadata.traceorg.ipam.entity.subnet.TraceOrgSubnetDetails;
import com.motadata.traceorg.ipam.services.TraceOrgService;
import com.motadata.traceorg.ipam.services.supernet.TraceOrgSupernetService;
import com.motadata.traceorg.ipam.util.*;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class TraceOrgScanDhcpSchedulerJob implements Job
{
    private final TraceOrgLogger _logger = new TraceOrgLogger(TraceOrgScanDhcpSchedulerJob.class, "GUI / Scan DHCP ExecuteJob");

    /**
     * IPAM-148 : System should have the ability to locate the available subnets inside a Supernet. This is to provide assistance to users when creating subnets inside an aggregated Network.
     * added the insertSubnetInSupernetCategory method call to insert subnet in supernet
     * IPAM-149 : IPAM Roadmap : System should have alert notification module to configure different kind of alert notification
     * Added code for NEW_SUBNETS_DISCOVERED Alert
     * IPAM-192 : Subnet should be added into respective supernet during DHCP Auto scan
     * fetched traceOrgSupernetService from job data map to access insertSubnetInSupernetCategory
     * @param jobExecutionContext
     * @throws JobExecutionException
     *
     * IPAM-174 : IPAM | Duplicate Emails sent on the same day for repeat mode for Report Scheduler
     * added the isManualDHCPScan flag for audit
     */
    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException
    {
        Response response = new Response();

        JobDataMap dataMap = jobExecutionContext.getMergedJobDataMap();

        TraceOrgDhcpCredentialDetails traceOrgDhcpCredentialDetails = (TraceOrgDhcpCredentialDetails)dataMap.get("traceOrgDhcpCredentialDetails");

        TraceOrgCiscoDHCPServerUtil traceOrgCiscoDHCPServerUtil = (TraceOrgCiscoDHCPServerUtil)dataMap.get("traceOrgCiscoDHCPServerUtil");

        TraceOrgWindowsDhcpServerUtil traceOrgWindowsDhcpServerUtil = (TraceOrgWindowsDhcpServerUtil)dataMap.get("traceOrgWindowsDhcpServerUtil");

        TraceOrgSupernetService traceOrgSupernetService = null;

        if (dataMap.get(TraceOrgCommonConstants.TRACE_ORG_SUPERNET_SERVICE) != null)
        {
            traceOrgSupernetService = (TraceOrgSupernetService)dataMap.get(TraceOrgCommonConstants.TRACE_ORG_SUPERNET_SERVICE);
        }

        boolean isManualDHCPScan = dataMap.get(TraceOrgCommonConstants.MANUAL_DHCP_SCAN) != null && (boolean) dataMap.get(TraceOrgCommonConstants.MANUAL_DHCP_SCAN);

        String currentUserName = dataMap.get(TraceOrgCommonConstants.USER_NAME) != null ? TraceOrgCommonUtil.getStringValue(dataMap.get(TraceOrgCommonConstants.USER_NAME)): "";

        TraceOrgService traceOrgService = (TraceOrgService)dataMap.get(TraceOrgCommonConstants.TRACE_ORG_SERVICE);

        TraceOrgCommonUtil traceOrgCommonUtil = (TraceOrgCommonUtil) dataMap.get(TraceOrgCommonConstants.TRACE_ORG_COMMON_UTIL);

        TraceOrgCommonUtil.m_scanStatus = new AtomicInteger(1);

        TraceOrgCommonUtil.m_scanSubnet.put("jobKey",traceOrgDhcpCredentialDetails.getCredentialName());

        List<TraceOrgSubnetDetails> traceOrgSubnetDetails = null;

        if(traceOrgDhcpCredentialDetails.getType().equalsIgnoreCase(TraceOrgCommonConstants.CISCO))
        {
            try
            {
                traceOrgSubnetDetails  = traceOrgCiscoDHCPServerUtil.discoveryForSubnet(traceOrgDhcpCredentialDetails,traceOrgService);
            }
            catch (Exception exception)
            {
                _logger.error(exception);
            }
        }
        else if (traceOrgDhcpCredentialDetails.getType().equalsIgnoreCase(TraceOrgCommonConstants.WINDOWS))
        {
            try
            {
                traceOrgSubnetDetails  = traceOrgWindowsDhcpServerUtil.getSubnetDetails(traceOrgDhcpCredentialDetails);
            }
            catch (Exception exception)
            {
                _logger.error(exception);
            }
        }

        if(traceOrgSubnetDetails!=null && !traceOrgSubnetDetails.isEmpty())
        {
            for(TraceOrgSubnetDetails traceOrgSubnetDetail : traceOrgSubnetDetails)
            {
                if(!traceOrgService.isExist(TraceOrgCommonConstants.TRACE_ORG_SUBNET_DETAILS,TraceOrgCommonConstants.SUBNET_ADDRESS,traceOrgSubnetDetail.getSubnetAddress()) && !traceOrgService.isExist(TraceOrgCommonConstants.TRACE_ORG_SUBNET_DETAILS,"subnetName",traceOrgSubnetDetail.getSubnetName()))
                {
                    TraceOrgCategory traceOrgCategory = (TraceOrgCategory)traceOrgService.getById(TraceOrgCommonConstants.TRACE_ORG_CATEGORY,1L);

                    if(traceOrgCategory !=null)
                    {
                        traceOrgSubnetDetail.setScheduleHour(traceOrgDhcpCredentialDetails.getSubnetScheduleHour());

                        switch (traceOrgDhcpCredentialDetails.getType().toUpperCase())
                        {
                            case "CISCO":
                                traceOrgSubnetDetail.setType("Cisco");
                                break;

                            case "WINDOWS":
                                traceOrgSubnetDetail.setType("Windows");
                                break;
                        }

                        traceOrgSubnetDetail.setScheduleHour(traceOrgDhcpCredentialDetails.getSubnetScheduleHour());

                        traceOrgSubnetDetail.setDuration(traceOrgDhcpCredentialDetails.getSubnetDuration());

                        traceOrgSubnetDetail.setTraceOrgCategory(traceOrgCategory);

                        traceOrgSubnetDetail.setTraceOrgDhcpCredentialDetailsId(traceOrgDhcpCredentialDetails);

                        traceOrgSubnetDetail.setCreatedBy(traceOrgDhcpCredentialDetails.getCreatedBy());

                        traceOrgSubnetDetail.setAllowDns(TraceOrgCommonConstants.TRUE);

                        traceOrgSubnetDetail.setAllowIcmp(TraceOrgCommonConstants.TRUE);

                        traceOrgSubnetDetail.setModifiedDate(new Date());

                        traceOrgSubnetDetail.setCreatedDate(new Date());

                        traceOrgService.insert(traceOrgSubnetDetail);

                        if(traceOrgSubnetDetail.getScheduleHour() > 0 && traceOrgSubnetDetail.getDuration()!=null && !traceOrgSubnetDetail.getDuration().isEmpty())
                        {
                            String cronExpression = null;

                            switch (traceOrgSubnetDetail.getDuration())
                            {
                                case "Days" :
                                    cronExpression = "0 0 0 */"+traceOrgSubnetDetail.getScheduleHour()+" * ?";
                                    break;
                                case "Hours" :
                                    cronExpression = "0 0 */"+traceOrgSubnetDetail.getScheduleHour()+" ? * *";
                                    break;
                                case "Month" :
                                    cronExpression = "0 0 0 1 1-12/"+traceOrgSubnetDetail.getScheduleHour()+" ?";
                                    break;
                            }

                            traceOrgCommonUtil.scanSubnetCronJob(cronExpression,traceOrgSubnetDetail,traceOrgService,traceOrgCommonUtil);
                        }

                        //EVENT LOG
                        TraceOrgEvent traceOrgEvent =  new TraceOrgEvent();

                        traceOrgEvent.setTimestamp(new Date());

                        TraceOrgUser doneBy = null;

                        String eventBy = "Scheduler";

                        if(traceOrgService.findByUserName(traceOrgSubnetDetail.getCreatedBy()).getUserName()!=null)
                        {
                            doneBy = traceOrgService.findByUserName(traceOrgSubnetDetail.getCreatedBy());

                            eventBy = traceOrgSubnetDetail.getCreatedBy();

                            traceOrgEvent.setDoneBy(traceOrgService.findByUserName(traceOrgSubnetDetail.getCreatedBy()));

                            traceOrgEvent.setEventContext("Subnet "+traceOrgSubnetDetail.getSubnetAddress()+" is added by Scanning DHCP Server "+traceOrgDhcpCredentialDetails.getHostAddress()+" in IP Address Manager by "+traceOrgSubnetDetail.getCreatedBy());
                        }
                        else
                        {
                            traceOrgEvent.setEventContext("Subnet "+traceOrgSubnetDetail.getSubnetAddress()+" is added by Scanning DHCP Server "+traceOrgDhcpCredentialDetails.getHostAddress()+" in IP Address Manager by Scheduler");
                        }

                        traceOrgEvent.setEventType("Add Subnet");

                        traceOrgEvent.setSeverity(1);

                        traceOrgService.insert(traceOrgEvent);

                        HashMap<String, Object> context = new HashMap<>();

                        context.put("subnetAddress", traceOrgSubnetDetail.getSubnetAddress());

                        context.put("currentUserName", "Scanning DHCP Server");

                        traceOrgCommonUtil.sendMessage(TraceOrgCommonConstants.ALERT_QUEUE, context, TraceOrgCommonConstants.NEW_SUBNETS_DISCOVERED);

                        if (traceOrgSupernetService != null)
                        {
                            traceOrgSupernetService.insertSubnetInSupernetCategory(traceOrgSubnetDetail.getSubnetAddress(), traceOrgSubnetDetail.getSubnetCidr(), traceOrgSubnetDetail.getId(), doneBy , "by Scanning DHCP Server by "+eventBy);
                        }

                        //SUBNET IP ADDRESS
                        traceOrgCommonUtil.ipList(traceOrgSubnetDetail);
                    }
                }
            }
        }
        traceOrgDhcpCredentialDetails.setLastScanTime(new Date());

        traceOrgDhcpCredentialDetails.setModifiedDate(new Date());

        traceOrgService.insert(traceOrgDhcpCredentialDetails);

        TraceOrgEvent traceOrgEvent =  new TraceOrgEvent();

        traceOrgEvent.setTimestamp(new Date());

        if(isManualDHCPScan)
        {
            if (traceOrgService.findByUserName(currentUserName) != null)
            {
                traceOrgEvent.setDoneBy(traceOrgService.findByUserName(currentUserName));
            }

            traceOrgEvent.setEventContext("DHCP Server  "+traceOrgDhcpCredentialDetails.getHostAddress()+" is scanned in IP Address Manager by "+currentUserName);
        }
        else
        {
            traceOrgEvent.setEventContext("DHCP Server  "+traceOrgDhcpCredentialDetails.getHostAddress()+" is scanned in IP Address Manager by Scheduler");
        }

        traceOrgEvent.setEventType("Scan DHCP Server");

        traceOrgEvent.setSeverity(2);

        traceOrgService.insert(traceOrgEvent);

        if(traceOrgDhcpCredentialDetails.getType().equalsIgnoreCase(TraceOrgCommonConstants.CISCO))
        {
            try
            {
                traceOrgCiscoDHCPServerUtil.getDhcpServerStatistics(traceOrgDhcpCredentialDetails,traceOrgService);
            }
            catch (Exception exception)
            {
                _logger.error(exception);
            }
        }
        else if (traceOrgDhcpCredentialDetails.getType().equalsIgnoreCase(TraceOrgCommonConstants.WINDOWS))
        {
            try
            {
                traceOrgWindowsDhcpServerUtil.getDhcpUtilizationDetails(traceOrgDhcpCredentialDetails,traceOrgService);
            }
            catch (Exception exception)
            {
                _logger.error(exception);
            }
        }
        response.setSuccess(TraceOrgCommonConstants.TRUE);

        response.setMessage(TraceOrgMessageConstants.DHCP_SCAN_SUCCESS);

        TraceOrgCommonUtil.m_scanStatus = new AtomicInteger(0);

    }
}
