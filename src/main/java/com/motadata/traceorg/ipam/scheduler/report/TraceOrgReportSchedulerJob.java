package com.motadata.traceorg.ipam.scheduler.report;

import com.motadata.traceorg.ipam.logger.TraceOrgLogger;
import com.motadata.traceorg.ipam.entity.report.TraceOrgReportScheduler;
import com.motadata.traceorg.ipam.services.TraceOrgService;
import com.motadata.traceorg.ipam.util.TraceOrgCommonConstants;
import com.motadata.traceorg.ipam.util.TraceOrgCommonUtil;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.io.File;
import java.util.HashMap;


@SuppressWarnings("ALL")
public class TraceOrgReportSchedulerJob implements Job
{
    private static final TraceOrgLogger _logger = new TraceOrgLogger(TraceOrgReportSchedulerJob.class, "ReportScheduler ExecuteJob");

    /**
     * IPAM-134 IPAM | Mail Server Configuration issue
     * Added generic sendMail method
     *
     * IPAM-145 : System should have rogue device detection capability
     * Refactor the code and introduce one comman method for diffrent methods.
     * **/
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException
    {
        try
        {
            JobDataMap dataMap = context.getMergedJobDataMap();

            TraceOrgReportScheduler traceOrgReportScheduler = (TraceOrgReportScheduler) dataMap.get("traceOrgReportScheduler");

            TraceOrgService traceOrgService = (TraceOrgService) dataMap.get("traceOrgService");

            TraceOrgCommonUtil traceOrgCommonUtil = (TraceOrgCommonUtil) dataMap.get("traceOrgCommonUtil");

            _logger.info("Scheduling :: " + traceOrgReportScheduler.getSchedulerName());

            String fileName = null;

            String mailSubject = traceOrgReportScheduler.getSchedulerName();

            if(traceOrgReportScheduler.getExportType().equalsIgnoreCase("PDF"))
            {
                if(traceOrgReportScheduler.getIpFilter().equalsIgnoreCase(TraceOrgCommonConstants.EVENT_LOG_REPORT))
                {
                    fileName = traceOrgCommonUtil.exportAllEventReportPdf(traceOrgReportScheduler.getReportExportTimeline());
                }
                else if(traceOrgReportScheduler.getIpFilter().equalsIgnoreCase(TraceOrgCommonConstants.CONFLICT_IP_REPORT))
                {
                    fileName = traceOrgCommonUtil.exportAllConflictIpReportPdf(traceOrgReportScheduler.getReportExportTimeline());
                }
                else if(traceOrgReportScheduler.getIpFilter().equalsIgnoreCase(TraceOrgCommonConstants.SUBNET_UTILIZATION_REPORT))
                {
                    fileName = traceOrgCommonUtil.exportSubnetUtilizationReportPdf(traceOrgReportScheduler.getReportExportTimeline());
                }
                else if(traceOrgReportScheduler.getIpFilter().equalsIgnoreCase(TraceOrgCommonConstants.DHCP_UTILIZATION_REPORT))
                {
                    fileName = traceOrgCommonUtil.exportDHCPUtilizationReportPdf(traceOrgReportScheduler.getReportExportTimeline());
                }
                else
                {
                    HashMap<String, Object> result = new HashMap<>();

                    traceOrgCommonUtil.exportSubnetIpReportByTimeline(result, traceOrgReportScheduler.getSubnetId(), traceOrgReportScheduler.getIpFilter(), traceOrgReportScheduler.getReportExportTimeline(), Boolean.TRUE);

                    if(!result.isEmpty())
                    {
                        fileName = result.get(TraceOrgCommonConstants.DATA).toString();
                    }
                }
            }
            else if(traceOrgReportScheduler.getExportType().equalsIgnoreCase("CSV"))
            {
                if(traceOrgReportScheduler.getIpFilter().equalsIgnoreCase(TraceOrgCommonConstants.EVENT_LOG_REPORT))
                {
                    fileName = traceOrgCommonUtil.exportAllEventReportCsv(traceOrgReportScheduler.getReportExportTimeline());
                }
                else if(traceOrgReportScheduler.getIpFilter().equalsIgnoreCase(TraceOrgCommonConstants.CONFLICT_IP_REPORT))
                {
                    fileName = traceOrgCommonUtil.exportAllConflictIpReportCsv(traceOrgReportScheduler.getReportExportTimeline());
                }
                else if(traceOrgReportScheduler.getIpFilter().equalsIgnoreCase(TraceOrgCommonConstants.SUBNET_UTILIZATION_REPORT))
                {
                    fileName = traceOrgCommonUtil.exportSubnetUtilizationReportCsv(traceOrgReportScheduler.getReportExportTimeline());
                }
                else if(traceOrgReportScheduler.getIpFilter().equalsIgnoreCase(TraceOrgCommonConstants.DHCP_UTILIZATION_REPORT))
                {
                    fileName = traceOrgCommonUtil.exportDHCPUtilizationReportCsv(traceOrgReportScheduler.getReportExportTimeline());
                }
                else
                {
                    HashMap<String, Object> result = new HashMap<>();

                    traceOrgCommonUtil.exportSubnetIpReportByTimeline(result, traceOrgReportScheduler.getSubnetId(), traceOrgReportScheduler.getIpFilter(), traceOrgReportScheduler.getReportExportTimeline(), Boolean.FALSE);

                    if(!result.isEmpty())
                    {
                        fileName = result.get(TraceOrgCommonConstants.DATA).toString();
                    }
                }
            }

            if(fileName!=null && !fileName.isEmpty() && mailSubject!=null && !mailSubject.isEmpty())
            {
                File file = new File(TraceOrgCommonConstants.CURRENT_DIR +TraceOrgCommonConstants.PATH_SEPARATOR+"Report"+TraceOrgCommonConstants.PATH_SEPARATOR+fileName);

                if(traceOrgReportScheduler.getEmailTo().contains(","))
                {
                    for(String emailTo : traceOrgReportScheduler.getEmailTo().split(","))
                    {
                        traceOrgCommonUtil.sendMailWithAttachment(mailSubject,
                                "Please Find the Attachment for Report of IP Address Manager ",
                                emailTo,
                                file);
                    }
                }
                else
                {
                    traceOrgCommonUtil.sendMailWithAttachment(mailSubject,
                            "Please Find the Attachment for Report of IP Address Manager ",
                            null,
                            file);
                }

            }
        }
        catch (Exception exception)
        {
            _logger.error(exception);
        }
    }


}
