package com.motadata.traceorg.ipam.controller.report;

import com.motadata.traceorg.ipam.logger.TraceOrgLogger;
import com.motadata.traceorg.ipam.entity.Response;
import com.motadata.traceorg.ipam.entity.report.TraceOrgReportScheduler;
import com.motadata.traceorg.ipam.services.TraceOrgService;
import com.motadata.traceorg.ipam.services.report.TraceOrgReportSchedulerService;
import com.motadata.traceorg.ipam.util.TraceOrgCommonConstants;
import com.motadata.traceorg.ipam.util.TraceOrgCommonUtil;
import com.motadata.traceorg.ipam.util.TraceOrgMessageConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@SuppressWarnings("ALL")
@RestController
public class TraceOrgReportSchedulerController
{
    @Autowired
    private TraceOrgReportSchedulerService traceOrgReportSchedulerService;

    @Autowired
    private TraceOrgService traceOrgService;

    @Autowired
    private TraceOrgCommonUtil traceOrgCommonUtil;

    private static final TraceOrgLogger _logger = new TraceOrgLogger(TraceOrgReportSchedulerController.class, "Report Scheduler Controller");

    /**
     * IPAM-145 : System should have rogue device detection capability
     * Change the method in a structured manner.
     * @param request
     * @return
     */
    @RequestMapping(value = TraceOrgCommonConstants.REPORT_SCHEDULER_REST_URL, method = RequestMethod.GET)
    @PreAuthorize("hasAuthority('PERM_REPORTS_READ')")
    public ResponseEntity<?> listAllReportScheduler(HttpServletRequest request)
    {
        Response response = new Response();

        try
        {
            traceOrgCommonUtil.buildResponse(traceOrgReportSchedulerService.listAllReportScheduler(), response);
        }
        catch (Exception exception)
        {
            _logger.error(exception);

            response.setSuccess(TraceOrgCommonConstants.FALSE);

            response.setMessage(TraceOrgMessageConstants.ENTER_VALID_DETAILS);
        }

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * IPAM-145 : System should have rogue device detection capability
     * Change the method in a structured manner.
     * @param request
     * @param id
     * @return
     */
    @RequestMapping(value = TraceOrgCommonConstants.REPORT_SCHEDULER_REST_URL+"{id}", method = RequestMethod.GET)
    @PreAuthorize("hasAuthority('PERM_REPORTS_READ')")
    public ResponseEntity<?> listReportScheduler(HttpServletRequest request,@PathVariable(TraceOrgCommonConstants.ID) Long id)
    {
        Response response = new Response();

        try
        {
            traceOrgCommonUtil.buildResponse(traceOrgReportSchedulerService.listReportScheduler(id), response);
        }
        catch (Exception exception)
        {
            _logger.error(exception);

            response.setSuccess(TraceOrgCommonConstants.FALSE);

            response.setMessage(TraceOrgMessageConstants.ENTER_VALID_DETAILS);
        }

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * IPAM-145 : System should have rogue device detection capability
     * Change the method in a structured manner.
     * @param request
     * @param traceOrgReportScheduler
     * @return
     */
    @RequestMapping(value = TraceOrgCommonConstants.REPORT_SCHEDULER_REST_URL, method = RequestMethod.POST)
    @PreAuthorize("hasAuthority('PERM_REPORTS_WRITE')")
    public ResponseEntity<?> insertReportScheduler(HttpServletRequest request, @RequestBody TraceOrgReportScheduler traceOrgReportScheduler)
    {
        Response response = new Response();

        try
        {
            traceOrgCommonUtil.buildResponse(traceOrgReportSchedulerService.insertReportScheduler(traceOrgReportScheduler), response);
        }
        catch (Exception exception)
        {
            _logger.error(exception);

            response.setSuccess(TraceOrgCommonConstants.FALSE);

            response.setMessage(TraceOrgMessageConstants.ENTER_VALID_DETAILS);
        }

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * IPAM-145 : System should have rogue device detection capability
     * Change the method in a structured manner.
     * @param request
     * @param id
     * @param traceOrgReportScheduler
     * @return
     */
    @RequestMapping(value = TraceOrgCommonConstants.REPORT_SCHEDULER_REST_URL + "{id}", method = RequestMethod.PUT)
    @PreAuthorize("hasAuthority('PERM_REPORTS_WRITE')")
    public ResponseEntity<?> updateReportScheduler(HttpServletRequest request, @PathVariable Long id,@RequestBody TraceOrgReportScheduler traceOrgReportScheduler)
    {
        Response response = new Response();

        try
        {
            traceOrgCommonUtil.buildResponse(traceOrgReportSchedulerService.updateReportScheduler(traceOrgReportScheduler, id), response);
        }
        catch (Exception exception)
        {
            _logger.error(exception);

            response.setSuccess(TraceOrgCommonConstants.FALSE);

            response.setMessage(TraceOrgMessageConstants.ENTER_VALID_DETAILS);
        }

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * IPAM-145 : System should have rogue device detection capability
     * Change the method in a structured manner.
     * @param request
     * @param id
     * @param traceOrgReportScheduler
     * @return
     */
    @RequestMapping(value = TraceOrgCommonConstants.REPORT_SCHEDULER_REST_URL+"{id}", method = RequestMethod.DELETE)
    @PreAuthorize("hasAuthority('PERM_REPORTS_WRITE')")
    public ResponseEntity<?> removeReportScheduler(@PathVariable(TraceOrgCommonConstants.ID) Long id, HttpServletRequest request)
    {
        Response response = new Response();

        try
        {
            String accessToken = request.getHeader(TraceOrgCommonConstants.ACCESSTOKEN);

            traceOrgCommonUtil.buildResponse(traceOrgReportSchedulerService.removeReportScheduler(id, accessToken), response);
        }
        catch (Exception exception)
        {
            _logger.error(exception);

            response.setSuccess(TraceOrgCommonConstants.FALSE);

            response.setMessage(TraceOrgMessageConstants.ENTER_VALID_DETAILS);
        }

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * IPAM-145 : System should have rogue device detection capability
     * Change the method in a structured manner.
     * @param request
     * @param subnetId
     * @param ipStatus
     * @param exportTimeline
     * @return
     */
    @RequestMapping(value = TraceOrgCommonConstants.EXPORT_EVENT_PDF_REST_URL, method = RequestMethod.GET)
    @PreAuthorize("hasAuthority('PERM_REPORTS_READ')")
    public ResponseEntity<?> exportEventPdfReport(HttpServletRequest request,@RequestParam Integer exportTimeline)
    {
        Response response = new Response();

        try
        {
            traceOrgCommonUtil.buildResponse(traceOrgReportSchedulerService.exportEventPdfReport(exportTimeline), response);
        }
        catch (Exception exception)
        {
            _logger.error(exception);

            response.setSuccess(TraceOrgCommonConstants.FALSE);

            response.setMessage(TraceOrgMessageConstants.ENTER_VALID_DETAILS);
        }

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * IPAM-145 : System should have rogue device detection capability
     * Change the method in a structured manner.
     * @param request
     * @param subnetId
     * @param ipStatus
     * @param exportTimeline
     * @return
     */
    @RequestMapping(value = TraceOrgCommonConstants.EXPORT_EVENT_CSV_REST_URL, method = RequestMethod.GET)
    @PreAuthorize("hasAuthority('PERM_REPORTS_READ')")
    public ResponseEntity<?> exportEventCsvReport(HttpServletRequest request,@RequestParam Integer exportTimeline)
    {
        Response response = new Response();

        try
        {
            traceOrgCommonUtil.buildResponse(traceOrgReportSchedulerService.exportEventCsvReport(exportTimeline), response);
        }
        catch (Exception exception)
        {
            _logger.error(exception);

            response.setSuccess(TraceOrgCommonConstants.FALSE);

            response.setMessage(TraceOrgMessageConstants.ENTER_VALID_DETAILS);
        }

        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    /**
     * IPAM-145 : System should have rogue device detection capability
     * Change the method in a structured manner.
     * @param request
     * @param subnetId
     * @param ipStatus
     * @param exportTimeline
     * @return
     */
    @RequestMapping(value = TraceOrgCommonConstants.EXPORT_SUBNET_IP_BY_REPORT_TIMELINE, method = RequestMethod.GET)
    @PreAuthorize("hasAuthority('PERM_REPORTS_READ')")
    public ResponseEntity<?> exportSubnetIpReportByTimeline(HttpServletRequest request,@RequestParam String subnetId,@RequestParam String ipStatus,@RequestParam Integer exportTimeline)
    {
        Response response = new Response();

        try
        {
            traceOrgCommonUtil.buildResponse(traceOrgReportSchedulerService.exportSubnetIpPdfReportByTimeline(subnetId, ipStatus, exportTimeline), response);
        }
        catch (Exception exception)
        {
            _logger.error(exception);

            response.setSuccess(TraceOrgCommonConstants.FALSE);

            response.setMessage(TraceOrgMessageConstants.ENTER_VALID_DETAILS);
        }

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * IPAM-145 : System should have rogue device detection capability
     * Change the method in a structured manner.
     * @param request
     * @param subnetId
     * @param ipStatus
     * @param exportTimeline
     * @return
     */
    @RequestMapping(value = TraceOrgCommonConstants.EXPORT_SUBNET_IP_CSV_BY_REPORT_TIMELINE, method = RequestMethod.GET)
    @PreAuthorize("hasAuthority('PERM_REPORTS_READ')")
    public ResponseEntity<?> exportSubnetIpCsvReportByTimeline(HttpServletRequest request,@RequestParam String subnetId,@RequestParam String ipStatus,@RequestParam Integer exportTimeline)
    {
        Response response = new Response();

        try
        {
            traceOrgCommonUtil.buildResponse(traceOrgReportSchedulerService.exportSubnetIpCsvReportByTimeline(subnetId, ipStatus, exportTimeline), response);
        }
        catch (Exception exception)
        {
            _logger.error(exception);

            response.setSuccess(TraceOrgCommonConstants.FALSE);

            response.setMessage(TraceOrgMessageConstants.ENTER_VALID_DETAILS);
        }

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
