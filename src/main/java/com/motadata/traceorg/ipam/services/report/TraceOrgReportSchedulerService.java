package com.motadata.traceorg.ipam.services.report;

import com.motadata.traceorg.ipam.entity.report.TraceOrgReportScheduler;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;

public interface TraceOrgReportSchedulerService
{
    HashMap<String, Object> insertReportScheduler(TraceOrgReportScheduler traceOrgReportScheduler);

    HashMap<String, Object> updateReportScheduler(TraceOrgReportScheduler traceOrgReportScheduler, Long id);

    HashMap<String, Object> exportSubnetIpPdfReportByTimeline(String subnetId, String ipStatus, Integer exportTimeline);

    HashMap<String, Object> exportSubnetIpCsvReportByTimeline(String subnetId, String ipStatus, Integer exportTimeline);

    HashMap<String, Object> exportEventCsvReport(Integer exportTimeline);

    HashMap<String, Object> exportEventPdfReport(Integer exportTimeline);

    HashMap<String, Object> removeReportScheduler(Long id, String accessToken);

    HashMap<String, Object> listReportScheduler(Long id);

    HashMap<String, Object> listAllReportScheduler();

}
