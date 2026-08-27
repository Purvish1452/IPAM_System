package com.motadata.traceorg.ipam.services.impl.report;

import com.motadata.traceorg.ipam.controller.report.TraceOrgReportSchedulerController;
import com.motadata.traceorg.ipam.entity.report.TraceOrgReportScheduler;
import com.motadata.traceorg.ipam.logger.TraceOrgLogger;
import com.motadata.traceorg.ipam.repository.report.TraceOrgReportScheduleRepository;
import com.motadata.traceorg.ipam.services.TraceOrgService;
import com.motadata.traceorg.ipam.services.report.TraceOrgReportSchedulerService;
import com.motadata.traceorg.ipam.util.TraceOrgCommonConstants;
import com.motadata.traceorg.ipam.util.TraceOrgCommonUtil;
import com.motadata.traceorg.ipam.util.TraceOrgMessageConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class TraceOrgReportSchedularServiceImpl implements TraceOrgReportSchedulerService
{
    private static final TraceOrgLogger _logger = new TraceOrgLogger(TraceOrgReportSchedulerController.class, "Report Scheduler Service");

    @Autowired
    private TraceOrgService traceOrgService;

    @Autowired
    private TraceOrgReportScheduleRepository traceOrgReportSchedularRepository;

    @Autowired
    private TraceOrgCommonUtil traceOrgCommonUtil;

    /**
     * IPAM-145 : System should have rogue device detection capability
     * insert the report schedular in the table.
     *
     * IPAM-173 : IPAM | Scheduler Should Not Allow Scheduling Reports in Past Time
     * Add Validation that selected time is after current time for scheduling report.
     * @param traceOrgReportScheduler
     * @return
     */
    @Override
    public HashMap<String, Object> insertReportScheduler(TraceOrgReportScheduler traceOrgReportScheduler)
    {
        HashMap<String, Object> result = new HashMap<>();

        try
        {
            if (traceOrgReportScheduler.getIpFilter()!=null && !traceOrgReportScheduler.getIpFilter().trim().isEmpty() && traceOrgReportScheduler.getSchedulerName()!=null && !traceOrgReportScheduler.getSchedulerName().trim().isEmpty() && traceOrgReportScheduler.getSchedulerTime()!=null && !traceOrgReportScheduler.getSchedulerTime().isEmpty() && ((traceOrgReportScheduler.getIpFilter().equalsIgnoreCase("All IP") || traceOrgReportScheduler.getIpFilter().equalsIgnoreCase("Used IP") || traceOrgReportScheduler.getIpFilter().equalsIgnoreCase("Reserved IP") || traceOrgReportScheduler.getIpFilter().equalsIgnoreCase("Available IP") || traceOrgReportScheduler.getIpFilter().equalsIgnoreCase("Transient IP") || traceOrgReportScheduler.getIpFilter().equalsIgnoreCase("Rogue IP") || traceOrgReportScheduler.getIpFilter().equalsIgnoreCase("Trusted IP")) ? (traceOrgReportScheduler.getSubnetId()!=null && !traceOrgReportScheduler.getSubnetId().trim().isEmpty()) : true))
            {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

                LocalTime schedulerTime = LocalTime.parse(traceOrgReportScheduler.getSchedulerTime(), formatter);

                LocalTime currentTime = LocalTime.now();

                if (schedulerTime.isAfter(currentTime))
                {
                    setSchedularData(traceOrgReportScheduler, result);

                    if(traceOrgReportScheduler.getIpFilter() !=null && traceOrgReportScheduler.getExportType()!=null)
                    {
                        traceOrgReportSchedularRepository.save(traceOrgReportScheduler);

                        traceOrgCommonUtil.scheduleCustomJob(traceOrgReportScheduler,this.traceOrgService,traceOrgCommonUtil);

                        result.put(TraceOrgCommonConstants.SUCCESS, TraceOrgCommonConstants.TRUE);

                        result.put(TraceOrgCommonConstants.MESSAGE, TraceOrgMessageConstants.SCHEDULER_ADD_SUCCESS);
                    }
                    else
                    {
                        result.put(TraceOrgCommonConstants.SUCCESS, TraceOrgCommonConstants.FALSE);

                        result.put(TraceOrgCommonConstants.MESSAGE, TraceOrgMessageConstants.ENTER_VALID_DETAILS);
                    }
                }
                else
                {
                    result.put(TraceOrgCommonConstants.SUCCESS, TraceOrgCommonConstants.FALSE);

                    result.put(TraceOrgCommonConstants.MESSAGE, TraceOrgMessageConstants.ENTER_VALID_TIMELINE);
                }
            }
            else
            {
                result.put(TraceOrgCommonConstants.SUCCESS, TraceOrgCommonConstants.FALSE);

                result.put(TraceOrgCommonConstants.MESSAGE, TraceOrgMessageConstants.ENTER_VALID_DETAILS);
            }
        }
        catch (Exception exception)
        {
            _logger.error(exception);

            result.put(TraceOrgCommonConstants.SUCCESS, TraceOrgCommonConstants.FALSE);

            result.put(TraceOrgCommonConstants.MESSAGE, TraceOrgMessageConstants.ENTER_VALID_DETAILS);
        }


        return result;
    }

    /**
     * IPAM-145 : System should have rogue device detection capability
     * update the scheduler of report.
     *
     * IPAM-179 : IPAM | Scheduler Allows Updating Reports to a Past Time Without Validation
     * Add Validation that selected time is after current time for scheduling report.
     * @param traceOrgReportScheduler
     * @param id
     * @return
     */
    @Override
    public HashMap<String, Object> updateReportScheduler(TraceOrgReportScheduler traceOrgReportScheduler, Long id)
    {
        HashMap<String, Object> result = new HashMap<>();

        try
        {
            if (id!=null && traceOrgReportScheduler.getIpFilter()!=null && !traceOrgReportScheduler.getIpFilter().trim().isEmpty() && traceOrgReportScheduler.getSchedulerName()!=null && !traceOrgReportScheduler.getSchedulerName().trim().isEmpty() && traceOrgReportScheduler.getSchedulerTime()!=null && !traceOrgReportScheduler.getSchedulerTime().isEmpty() && ((traceOrgReportScheduler.getIpFilter().equalsIgnoreCase("All IP") || traceOrgReportScheduler.getIpFilter().equalsIgnoreCase("Used IP") || traceOrgReportScheduler.getIpFilter().equalsIgnoreCase("Reserved IP") || traceOrgReportScheduler.getIpFilter().equalsIgnoreCase("Available IP") || traceOrgReportScheduler.getIpFilter().equalsIgnoreCase("Transient IP") || traceOrgReportScheduler.getIpFilter().equalsIgnoreCase("Rogue IP") || traceOrgReportScheduler.getIpFilter().equalsIgnoreCase("Trusted IP") || traceOrgReportScheduler.getIpFilter().equalsIgnoreCase("Vendor Summary")) ? (traceOrgReportScheduler.getSubnetId()!=null && !traceOrgReportScheduler.getSubnetId().trim().isEmpty()) : true))
            {
                TraceOrgReportScheduler exitstedTraceOrgReportScheduler = traceOrgReportSchedularRepository.findOne(id);

                if(exitstedTraceOrgReportScheduler !=null)
                {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

                    LocalTime schedulerTime = LocalTime.parse(traceOrgReportScheduler.getSchedulerTime(), formatter);

                    LocalTime currentTime = LocalTime.now();

                    if (schedulerTime.isAfter(currentTime))
                    {
                        exitstedTraceOrgReportScheduler = traceOrgReportScheduler ;

                        setSchedularData(traceOrgReportScheduler, result);

                        if(traceOrgReportScheduler.getIpFilter() !=null && traceOrgReportScheduler.getExportType()!=null)
                        {
                            exitstedTraceOrgReportScheduler.setSubnetId(traceOrgReportScheduler.getSubnetId());

                            exitstedTraceOrgReportScheduler.setId(id);

                            traceOrgReportSchedularRepository.save(exitstedTraceOrgReportScheduler);

                            boolean insertStatus = traceOrgReportSchedularRepository.exists(id);

                            if(insertStatus)
                            {
                                traceOrgCommonUtil.removeReportCustomJob(traceOrgReportScheduler);

                                traceOrgCommonUtil.scheduleCustomJob(traceOrgReportScheduler,this.traceOrgService,traceOrgCommonUtil);

                                result.put(TraceOrgCommonConstants.SUCCESS, TraceOrgCommonConstants.TRUE);

                                result.put(TraceOrgCommonConstants.MESSAGE, TraceOrgMessageConstants.SCHEDULER_UPDATE_SUCCESS);
                            }
                        }
                        else
                        {
                            result.put(TraceOrgCommonConstants.SUCCESS, TraceOrgCommonConstants.FALSE);

                            result.put(TraceOrgCommonConstants.MESSAGE, TraceOrgMessageConstants.ENTER_VALID_DETAILS);
                        }
                    }
                    else
                    {
                        result.put(TraceOrgCommonConstants.SUCCESS, TraceOrgCommonConstants.FALSE);

                        result.put(TraceOrgCommonConstants.MESSAGE, TraceOrgMessageConstants.ENTER_VALID_TIMELINE);
                    }
                }
                else
                {
                    result.put(TraceOrgCommonConstants.SUCCESS, TraceOrgCommonConstants.FALSE);

                    result.put(TraceOrgCommonConstants.MESSAGE, TraceOrgMessageConstants.ENTER_VALID_DETAILS);
                }
            }
            else
            {
                result.put(TraceOrgCommonConstants.SUCCESS, TraceOrgCommonConstants.FALSE);

                result.put(TraceOrgCommonConstants.MESSAGE, TraceOrgMessageConstants.ENTER_VALID_DETAILS);
            }
        }
        catch (Exception exception)
        {
            _logger.error(exception);

            result.put(TraceOrgCommonConstants.SUCCESS, TraceOrgCommonConstants.FALSE);

            result.put(TraceOrgCommonConstants.MESSAGE, TraceOrgMessageConstants.ENTER_VALID_DETAILS);
        }

        return result;
    }

    /**
     * IPAM-145 : System should have rogue device detection capability
     * export pdf type report data.
     * @param subnetId
     * @param ipStatus
     * @param exportTimeline
     * @return
     */
    @Override
    public HashMap<String, Object> exportSubnetIpPdfReportByTimeline(String subnetId, String ipStatus, Integer exportTimeline)
    {
        HashMap<String, Object> result = new HashMap<>();

        try
        {
            if(subnetId !=null && !subnetId.trim().isEmpty() && ipStatus!=null && !ipStatus.trim().isEmpty() && exportTimeline!=null)
            {
                traceOrgCommonUtil.exportSubnetIpReportByTimeline(result, subnetId, ipStatus, exportTimeline, Boolean.TRUE);
            }
            else
            {
                result.put(TraceOrgCommonConstants.SUCCESS, TraceOrgCommonConstants.FALSE);

                result.put(TraceOrgCommonConstants.MESSAGE, TraceOrgMessageConstants.ENTER_VALID_DETAILS);
            }
        }
        catch (Exception exception)
        {
            _logger.error(exception);

            result.put(TraceOrgCommonConstants.SUCCESS, TraceOrgCommonConstants.FALSE);

            result.put(TraceOrgCommonConstants.MESSAGE, TraceOrgMessageConstants.ENTER_VALID_DETAILS);
        }

        return result;
    }

    /**
     * IPAM-145 : System should have rogue device detection capability
     * export csv type report data.
     * @param subnetId
     * @param ipStatus
     * @param exportTimeline
     * @return
     */

    @Override
    public HashMap<String, Object> exportSubnetIpCsvReportByTimeline(String subnetId, String ipStatus, Integer exportTimeline)
    {
        HashMap<String, Object> result = new HashMap<>();

        try
        {
            if(subnetId !=null && !subnetId.trim().isEmpty() && ipStatus!=null && !ipStatus.trim().isEmpty() && exportTimeline!=null)
            {
                traceOrgCommonUtil.exportSubnetIpReportByTimeline(result, subnetId, ipStatus, exportTimeline, Boolean.FALSE);
            }
            else
            {
                result.put(TraceOrgCommonConstants.SUCCESS, TraceOrgCommonConstants.FALSE);

                result.put(TraceOrgCommonConstants.MESSAGE, TraceOrgMessageConstants.ENTER_VALID_DETAILS);
            }
        }
        catch (Exception exception)
        {
            _logger.error(exception);

            result.put(TraceOrgCommonConstants.SUCCESS, TraceOrgCommonConstants.FALSE);

            result.put(TraceOrgCommonConstants.MESSAGE, TraceOrgMessageConstants.ENTER_VALID_DETAILS);
        }

        return result;
    }

    /**
     * IPAM-145 : System should have rogue device detection capability
     * export the event csv report.
     * @param exportTimeline
     * @return
     */
    @Override
    public HashMap<String, Object> exportEventCsvReport(Integer exportTimeline)
    {
        HashMap<String, Object> result = new HashMap<>();

        if(exportTimeline != null)
        {
            try
            {
                String fileName = traceOrgCommonUtil.exportAllEventReportCsv(exportTimeline);

                if(fileName!=null && !fileName.isEmpty())
                {
                    result.put(TraceOrgCommonConstants.SUCCESS, TraceOrgCommonConstants.TRUE);

                    result.put(TraceOrgCommonConstants.DATA, fileName);
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

                result.put(TraceOrgCommonConstants.SUCCESS, TraceOrgCommonConstants.FALSE);

                result.put(TraceOrgCommonConstants.MESSAGE, TraceOrgMessageConstants.ENTER_VALID_DETAILS);
            }
        }
        else
        {
            result.put(TraceOrgCommonConstants.SUCCESS, TraceOrgCommonConstants.FALSE);

            result.put(TraceOrgCommonConstants.MESSAGE, TraceOrgMessageConstants.ENTER_VALID_DETAILS);
        }

        return result;
    }

    /**
     * IPAM-145 : System should have rogue device detection capability
     * export the event pdf report.
     * @param exportTimeline
     * @return
     */
    @Override
    public HashMap<String, Object> exportEventPdfReport(Integer exportTimeline)
    {
        HashMap<String, Object> result = new HashMap<>();

        if(exportTimeline != null)
        {
            try
            {
                String fileName = traceOrgCommonUtil.exportAllEventReportPdf(exportTimeline);

                if(fileName!=null && !fileName.isEmpty())
                {
                    result.put(TraceOrgCommonConstants.SUCCESS, TraceOrgCommonConstants.TRUE);

                    result.put(TraceOrgCommonConstants.DATA, fileName);
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

                result.put(TraceOrgCommonConstants.SUCCESS, TraceOrgCommonConstants.FALSE);

                result.put(TraceOrgCommonConstants.MESSAGE, TraceOrgMessageConstants.ENTER_VALID_DETAILS);
            }
        }
        else
        {
            result.put(TraceOrgCommonConstants.SUCCESS, TraceOrgCommonConstants.FALSE);

            result.put(TraceOrgCommonConstants.MESSAGE, TraceOrgMessageConstants.ENTER_VALID_DETAILS);
        }

        return result;
    }

    /**
     * IPAM-145 : System should have rogue device detection capability
     * remove the schedular report.
     * @param id
     * @param accessToken
     * @return
     */
    @Override
    public HashMap<String, Object> removeReportScheduler(Long id, String accessToken)
    {
        HashMap<String, Object> result = new HashMap<>();

        if(id!=null)
        {
            try
            {
                TraceOrgReportScheduler traceOrgReportScheduler = traceOrgReportSchedularRepository.findOne(id);

                if (traceOrgReportScheduler != null)
                {
                    traceOrgCommonUtil.removeReportCustomJob(traceOrgReportScheduler);

                    traceOrgReportSchedularRepository.delete(id);

                    boolean deleteStatus = !traceOrgReportSchedularRepository.exists(id);

                    if(deleteStatus)
                    {
                        result.put(TraceOrgCommonConstants.SUCCESS, TraceOrgCommonConstants.TRUE);

                        result.put(TraceOrgCommonConstants.MESSAGE, TraceOrgMessageConstants.REPORT_SCHEDULER_DELETE_SUCCESS);
                    }
                    else
                    {
                        result.put(TraceOrgCommonConstants.SUCCESS, TraceOrgCommonConstants.FALSE);

                        result.put(TraceOrgCommonConstants.MESSAGE, TraceOrgMessageConstants.SOMETHING_WENT_WRONG);
                    }
                }
                else
                {
                    result.put(TraceOrgCommonConstants.SUCCESS, TraceOrgCommonConstants.FALSE);

                    result.put(TraceOrgCommonConstants.MESSAGE, TraceOrgMessageConstants.ENTER_VALID_DETAILS);
                }
            }
            catch (Exception exception)
            {
                _logger.error(exception);

                result.put(TraceOrgCommonConstants.SUCCESS, TraceOrgCommonConstants.FALSE);

                result.put(TraceOrgCommonConstants.MESSAGE, TraceOrgMessageConstants.ENTER_VALID_DETAILS);
            }
        }
        else
        {
            result.put(TraceOrgCommonConstants.SUCCESS, TraceOrgCommonConstants.FALSE);

            result.put(TraceOrgCommonConstants.MESSAGE, TraceOrgMessageConstants.ENTER_VALID_DETAILS);
        }

        return result;
    }

    /**
     * IPAM-145 : System should have rogue device detection capability
     * fetch individual report.
     * @param id
     * @return
     */
    @Override
    public HashMap<String, Object> listReportScheduler(Long id)
    {
        HashMap<String, Object> result = new HashMap<>();

        if(id!=null)
        {
            try
            {
                TraceOrgReportScheduler traceOrgReportScheduler = traceOrgReportSchedularRepository.findOne(id);

                if(traceOrgReportScheduler !=null)
                {
                    result.put(TraceOrgCommonConstants.DATA, traceOrgReportScheduler);

                    result.put(TraceOrgCommonConstants.SUCCESS, TraceOrgCommonConstants.TRUE);
                }
                else
                {
                    result.put(TraceOrgCommonConstants.MESSAGE, TraceOrgMessageConstants.NO_DATA_AVAILABLE);

                    result.put(TraceOrgCommonConstants.SUCCESS, TraceOrgCommonConstants.TRUE);
                }
            }
            catch (Exception exception)
            {
                _logger.error(exception);

                result.put(TraceOrgCommonConstants.SUCCESS, TraceOrgCommonConstants.FALSE);

                result.put(TraceOrgCommonConstants.MESSAGE, TraceOrgMessageConstants.ENTER_VALID_DETAILS);
            }
        }
        else
        {
            result.put(TraceOrgCommonConstants.SUCCESS, TraceOrgCommonConstants.FALSE);

            result.put(TraceOrgCommonConstants.MESSAGE, TraceOrgMessageConstants.ENTER_VALID_DETAILS);
        }

        return result;
    }

    /**
     * IPAM-145 : System should have rogue device detection capability
     * fetch all report.
     * @return
     */
    @Override
    public HashMap<String, Object> listAllReportScheduler()
    {
        HashMap<String, Object> result = new HashMap<>();

        try
        {
            List<TraceOrgReportScheduler> traceOrgReportSchedulers = traceOrgReportSchedularRepository.findAll();

            if(traceOrgReportSchedulers !=null && !traceOrgReportSchedulers.isEmpty())
            {
                result.put(TraceOrgCommonConstants.DATA, traceOrgReportSchedulers);

                result.put(TraceOrgCommonConstants.SUCCESS, TraceOrgCommonConstants.TRUE);
            }
            else
            {
                result.put(TraceOrgCommonConstants.MESSAGE, TraceOrgMessageConstants.NO_DATA_AVAILABLE);

                result.put(TraceOrgCommonConstants.SUCCESS, TraceOrgCommonConstants.TRUE);
            }
        }
        catch (Exception exception)
        {
            _logger.error(exception);

            result.put(TraceOrgCommonConstants.SUCCESS, TraceOrgCommonConstants.FALSE);

            result.put(TraceOrgCommonConstants.MESSAGE, TraceOrgMessageConstants.ENTER_VALID_DETAILS);
        }

        return result;
    }

    /**
     * IPAM-145 : System should have rogue device detection capability
     * set the schedular data based on user input.
     * @param traceOrgReportScheduler
     * @param result
     */
    private static void setSchedularData(TraceOrgReportScheduler traceOrgReportScheduler, HashMap<String, Object> result)
    {
        try
        {
            DateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

            Date schedulerTime = simpleDateFormat.parse(traceOrgReportScheduler.getSchedulerDate()+" "+traceOrgReportScheduler.getSchedulerTime());

            if(schedulerTime.after(new Date()))
            {
                switch (traceOrgReportScheduler.getIpFilter().toUpperCase())
                {
                    case "ALL IP":
                        traceOrgReportScheduler.setIpFilter(TraceOrgCommonConstants.ALL_IP_REPORT);
                        break;
                    case "USED IP":
                        traceOrgReportScheduler.setIpFilter(TraceOrgCommonConstants.USED_IP_REPORT);
                        break;
                    case "RESERVED IP":
                        traceOrgReportScheduler.setIpFilter(TraceOrgCommonConstants.RESERVED_IP_REPORT);
                        break;
                    case "EVENT LOG":
                        traceOrgReportScheduler.setIpFilter(TraceOrgCommonConstants.EVENT_LOG_REPORT);
                        break;
                    case "AVAILABLE IP":
                        traceOrgReportScheduler.setIpFilter(TraceOrgCommonConstants.AVAILABLE_IP_REPORT);
                        break;
                    case "TRANSIENT IP":
                        traceOrgReportScheduler.setIpFilter(TraceOrgCommonConstants.TRANSIENT_IP_REPORT);
                        break;
                    case "ROGUE IP":
                        traceOrgReportScheduler.setIpFilter(TraceOrgCommonConstants.ROGUE_IP_REPORT);
                        break;
                    case "TRUSTED IP":
                        traceOrgReportScheduler.setIpFilter(TraceOrgCommonConstants.TRUSTED_IP_REPORT);
                        break;
                    case "CONFLICT IP":
                        traceOrgReportScheduler.setIpFilter(TraceOrgCommonConstants.CONFLICT_IP_REPORT);
                        break;
                    case "SUBNET UTILIZATION":
                        traceOrgReportScheduler.setIpFilter(TraceOrgCommonConstants.SUBNET_UTILIZATION_REPORT);
                        break;
                    case "DHCP UTILIZATION":
                        traceOrgReportScheduler.setIpFilter(TraceOrgCommonConstants.DHCP_UTILIZATION_REPORT);
                        break;
                    case "VENDOR SUMMARY":
                        traceOrgReportScheduler.setIpFilter(TraceOrgCommonConstants.VENDOR_SUMMARY_REPORT);
                        break;
                    default:
                        traceOrgReportScheduler.setIpFilter(null);
                        break;
                }

                switch (traceOrgReportScheduler.getExportType().toUpperCase())
                {
                    case "PDF":
                        traceOrgReportScheduler.setExportType("PDF");
                        break;
                    case "CSV":
                        traceOrgReportScheduler.setExportType("CSV");
                        break;
                    default:
                        traceOrgReportScheduler.setExportType(null);
                        break;
                }
            }
            else
            {
                result.put(TraceOrgCommonConstants.SUCCESS, TraceOrgCommonConstants.FALSE);

                result.put(TraceOrgCommonConstants.MESSAGE, "Past Time Scheduler can not be scheduled");
            }
        }
        catch (Exception exception)
        {
            _logger.error(exception);

            result.put(TraceOrgCommonConstants.SUCCESS, TraceOrgCommonConstants.FALSE);

            result.put(TraceOrgCommonConstants.MESSAGE, TraceOrgMessageConstants.ENTER_VALID_DETAILS);
        }

    }
}
