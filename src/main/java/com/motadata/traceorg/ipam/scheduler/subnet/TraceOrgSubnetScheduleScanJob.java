package com.motadata.traceorg.ipam.scheduler.subnet;

import com.motadata.traceorg.ipam.logger.TraceOrgLogger;
import com.motadata.traceorg.ipam.scheduler.dhcp.TraceOrgScanDhcpSchedulerJob;
import com.motadata.traceorg.ipam.util.TraceOrgCommonConstants;
import com.motadata.traceorg.ipam.util.TraceOrgCommonUtil;
import org.quartz.*;

import java.util.HashMap;
import java.util.Map;

public class TraceOrgSubnetScheduleScanJob implements Job
{

    private final TraceOrgLogger _logger = new TraceOrgLogger(TraceOrgSubnetScheduleScanJob.class, "GUI / Subnet Scan Schedule Job");

    /**
     * IPAM-145 : System should have rogue device detection capability
     * Added traceOrgRogueDetectionRepository object on subnet scan details.
     * IPAM-192 : Subnet should be added into respective supernet during DHCP Auto scan
     * added the traceOrgSupernetService into job data map
     * @param context
     * @throws JobExecutionException
     */
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException
    {
        try
        {
            if(TraceOrgCommonUtil.m_scheduleScanSubnet.size() > 0 && TraceOrgCommonUtil.getSubnetScanStatus() == 0)
            {
                Map.Entry<String,Object> scheduleEntry = TraceOrgCommonUtil.m_scheduleScanSubnet.entrySet().iterator().next();

                String subnetId = scheduleEntry.getKey();

                HashMap<String,Object> subnetDetails = (HashMap<String, Object>) TraceOrgCommonUtil.m_scheduleScanSubnet.get(subnetId);

                HashMap<String,Object> subnetScanDetails = new HashMap<>();

                short scanType = (short) subnetDetails.get(TraceOrgCommonConstants.SCAN_TYPE);

                if(subnetDetails.containsKey("accessToken") && subnetDetails.get("accessToken") != null)
                {
                    subnetScanDetails.put("accessToken",subnetDetails.get("accessToken"));
                }

                subnetScanDetails.put("traceOrgService",subnetDetails.get("traceOrgService"));

                subnetScanDetails.put(TraceOrgCommonConstants.TRACE_ORG_ROGUE_DETECTION_REPOSITORY, subnetDetails.get(TraceOrgCommonConstants.TRACE_ORG_ROGUE_DETECTION_REPOSITORY));

                subnetScanDetails.put(TraceOrgCommonConstants.TRACE_ORG_ALERT_SERVICE, subnetDetails.get(TraceOrgCommonConstants.TRACE_ORG_ALERT_SERVICE));

                subnetScanDetails.put("traceOrgCommonUtil",subnetDetails.get("traceOrgCommonUtil"));

                if(scanType == TraceOrgCommonConstants.SUBNET_SCAN)
                {
                    subnetScanDetails.put("subnetDetails",subnetDetails.get("subnetDetails"));

                    subnetScanDetails.put("id",subnetId);

                    JobKey jobKey = JobKey.jobKey(TraceOrgCommonConstants.SCAN_SUBNET);

                    JobDetail job = JobBuilder.newJob(TraceOrgSubnetControllerExecuteJob.class).withIdentity(jobKey).usingJobData(new JobDataMap(subnetScanDetails)).storeDurably().build();

                    TraceOrgCommonUtil.quartzThread.addJob(job, true);

                    TraceOrgCommonUtil.quartzThread.triggerJob(jobKey);
                }
                else
                {
                    if (subnetDetails.get(TraceOrgCommonConstants.TRACE_ORG_SUPERNET_SERVICE) != null)
                    {
                        subnetScanDetails.put(TraceOrgCommonConstants.TRACE_ORG_SUPERNET_SERVICE, subnetDetails.get(TraceOrgCommonConstants.TRACE_ORG_SUPERNET_SERVICE));
                    }

                    subnetScanDetails.put("traceOrgDhcpCredentialDetails",subnetDetails.get("traceOrgDhcpCredentialDetails"));

                    subnetScanDetails.put("traceOrgCiscoDHCPServerUtil",subnetDetails.get("traceOrgCiscoDHCPServerUtil"));

                    subnetScanDetails.put("traceOrgWindowsDhcpServerUtil",subnetDetails.get("traceOrgWindowsDhcpServerUtil"));

                    JobKey jobKey = JobKey.jobKey(TraceOrgCommonConstants.SCAN_SUBNET);

                    JobDetail job = JobBuilder.newJob(TraceOrgScanDhcpSchedulerJob.class).withIdentity(jobKey).usingJobData(new JobDataMap(subnetScanDetails)).storeDurably().build();

                    TraceOrgCommonUtil.quartzThread.addJob(job, true);

                    TraceOrgCommonUtil.quartzThread.triggerJob(jobKey);
                }

                _logger.info("Remove from quque "+subnetId);

                TraceOrgCommonUtil.m_scheduleScanSubnet.remove(subnetId);

                _logger.info(TraceOrgCommonUtil.m_scheduleScanSubnet.size());

            }
        }
        catch (Exception exception)
        {
            _logger.error(exception);
        }
    }
}
