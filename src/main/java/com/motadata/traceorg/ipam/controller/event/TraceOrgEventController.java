package com.motadata.traceorg.ipam.controller.event;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.motadata.traceorg.ipam.logger.TraceOrgLogger;
import com.motadata.traceorg.ipam.entity.Response;
import com.motadata.traceorg.ipam.entity.event.TraceOrgEvent;
import com.motadata.traceorg.ipam.services.TraceOrgService;
import com.motadata.traceorg.ipam.util.TraceOrgCommonConstants;
import com.motadata.traceorg.ipam.util.TraceOrgCommonUtil;
import com.motadata.traceorg.ipam.util.TraceOrgMessageConstants;
import com.motadata.traceorg.ipam.util.TraceOrgPDFBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Type;
import java.util.*;

/**
 * @author Krunal Thakkar
 *
 */

@SuppressWarnings("ALL")
@RestController
public class TraceOrgEventController
{
    @Autowired
    private TraceOrgService traceOrgService;

    @Autowired
    private TraceOrgCommonUtil traceOrgCommonUtil;

    private static final TraceOrgLogger _logger = new TraceOrgLogger(TraceOrgEventController.class, "Event Controller");

    @RequestMapping(value = TraceOrgCommonConstants.TOP_EVENT_REST_URL, method = RequestMethod.GET)
    @PreAuthorize("hasAnyAuthority('PERM_EVENT NOTIFICATIONS_READ', 'PERM_DASHBOARD_READ')")
    public ResponseEntity<?> listTopEvents(HttpServletRequest request)
    {
        Response response = new Response();

        try
        {
            String accessToken = request.getHeader(TraceOrgCommonConstants.ACCESSTOKEN);

            List<TraceOrgEvent> traceOrgCategories = (List<TraceOrgEvent>) this.traceOrgService.commonQuery("",TraceOrgCommonConstants.TRACE_ORG_EVENT + " order by id desc");

           if(traceOrgCategories !=null && !traceOrgCategories.isEmpty())
           {
               if(traceOrgCategories.size() > 25)
               {
                   response.setData(traceOrgCategories.subList(0,25));
               }
               else
               {
                   response.setData(traceOrgCategories);
               }
               response.setSuccess(TraceOrgCommonConstants.TRUE);
           }
           else
           {
               response.setSuccess(TraceOrgCommonConstants.FALSE);

               response.setMessage(TraceOrgMessageConstants.NO_DATA_AVAILABLE);
           }
        }
        catch (Exception exception)
        {
            _logger.error(exception);

            response.setSuccess(TraceOrgCommonConstants.FALSE);

            response.setMessage(TraceOrgMessageConstants.SOMETHING_WENT_WRONG);
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    @RequestMapping(value = TraceOrgCommonConstants.EVENT_REST_URL, method = RequestMethod.GET)
    @PreAuthorize("hasAnyAuthority('PERM_EVENT NOTIFICATIONS_READ', 'PERM_DASHBOARD_READ')")
    public ResponseEntity<?> listAllEvent(HttpServletRequest request,@RequestParam(required = false) Integer exportTimeline)
    {
        Response response = new Response();

        try
        {
            String accessToken = request.getHeader(TraceOrgCommonConstants.ACCESSTOKEN);

            List<TraceOrgEvent> traceOrgEventList = null;

            switch (exportTimeline)
            {
                case 1 :
                    traceOrgEventList = (List<TraceOrgEvent>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_EVENT + " where  Date(timestamp) = CURDATE() order by id desc");
                    break;
                case 2 :
                    traceOrgEventList = (List<TraceOrgEvent>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_EVENT + " where DATE(timestamp) = DATE(CURDATE() -1) order by id desc");
                    break;
                case 3 :
                    traceOrgEventList = (List<TraceOrgEvent>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_EVENT + " where WEEK(timestamp) =  WEEK(curdate()) and YEAR(timestamp) = YEAR(CURDATE()) order by id desc");
                    break;
                case 4 :
                    traceOrgEventList = (List<TraceOrgEvent>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_EVENT + " where MONTH(timestamp)= MONTH(curdate()) and YEAR(timestamp) = YEAR(CURDATE()) order by id desc");
                    break;
                case 5 :
                    traceOrgEventList = (List<TraceOrgEvent>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_EVENT + " where  QUARTER(timestamp) = QUARTER(curdate()) and YEAR(timestamp) = YEAR(CURDATE()) order by id desc");
                    break;
                case 6 :
                    traceOrgEventList = (List<TraceOrgEvent>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_EVENT + " where QUARTER(timestamp) = QUARTER(curdate()) - 1 and YEAR(timestamp) = YEAR(CURDATE()) order by id desc");
                    break;
                case 7 :
                    traceOrgEventList = (List<TraceOrgEvent>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_EVENT + " where TIMESTAMPDIFF(MONTH, timestamp, NOW()) < 6  order by id desc");
                    break;
                case 8 :
                    traceOrgEventList = (List<TraceOrgEvent>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_EVENT + " where YEAR(timestamp) = YEAR(curdate()) order by id desc ");
                    break;
                case 9 :
                    traceOrgEventList = (List<TraceOrgEvent>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_EVENT + " where YEAR(timestamp) = YEAR(curdate()) - 1 order by id desc");
                    break;
                case 10 :
                    traceOrgEventList = (List<TraceOrgEvent>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_EVENT +" order by id desc" );
                    break;
                case 11 :
                    traceOrgEventList = (List<TraceOrgEvent>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_EVENT + " where  WEEK(timestamp) =  WEEK(curdate()) - 1 and YEAR(timestamp) = YEAR(CURDATE()) order by id desc");
                    break;
                case 12 :
                    traceOrgEventList = (List<TraceOrgEvent>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_EVENT + " where  MONTH(timestamp)= MONTH(curdate()) - 1 and YEAR(timestamp) = YEAR(CURDATE()) order by id desc");
                    break;
                default:
                    traceOrgEventList = (List<TraceOrgEvent>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_EVENT +" order by id desc" );
                    break;
            }

            if(traceOrgEventList !=null && !traceOrgEventList.isEmpty())
            {
                response.setData(traceOrgEventList);

                response.setSuccess(TraceOrgCommonConstants.TRUE);
            }
            else
            {
                response.setSuccess(TraceOrgCommonConstants.FALSE);

                response.setMessage(TraceOrgMessageConstants.NO_DATA_AVAILABLE);
            }

        }
        catch (Exception exception)
        {
            _logger.error(exception);

            response.setSuccess(TraceOrgCommonConstants.FALSE);

            response.setMessage(TraceOrgMessageConstants.SOMETHING_WENT_WRONG);
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }



    @RequestMapping(value = TraceOrgCommonConstants.EVENT_SUMMARY_REST_URL, method = RequestMethod.GET)
    @PreAuthorize("hasAnyAuthority('PERM_EVENT NOTIFICATIONS_READ', 'PERM_DASHBOARD_READ')")
    public ResponseEntity<?> eventSummary(HttpServletRequest request)
    {
        Response response = new Response();

        try
        {
            String accessToken = request.getHeader(TraceOrgCommonConstants.ACCESSTOKEN);

            Calendar now = Calendar.getInstance();

            int currentYear = now.get(Calendar.YEAR);

            int currentMonth = now.get(Calendar.MONTH)+1;

            String query ="select * from (select months1,max(cn) as 'maxvalue' from (select DATE_FORMAT(event.timestamp,'%Y-%m') as months1,'severity',count(*) as cn from event group by months1,severity) as tb1 group by months1) as tb1 inner join (Select DATE_FORMAT(event.timestamp,'%Y-%m') as months,severity,count(*) as cn from event where DATE_FORMAT(event.timestamp,'%Y-%m') between '"+(currentYear-1)+"-"+(currentMonth+1)+"' and '"+currentYear+"-"+currentMonth+"' group by DATE_FORMAT(event.timestamp,'%Y-%m'),severity) as tb11 on tb1.months1 = tb11.months and tb11.cn = tb1.maxvalue";

            List<Object> traceOrgEvents = (List<Object>) this.traceOrgService.sqlQuery(query);

            if(traceOrgEvents!=null && !traceOrgEvents.isEmpty())
            {

                List<Object> eventSummary = new ArrayList<>();

                List<String> eventMonth =  new ArrayList<>();

                for(Object traceOrgEvent : traceOrgEvents)
                {
                    Map<String,Object> eventSummaryData = new HashMap<>();

                    Gson gson= new Gson();

                    Type listType = new TypeToken<List<String>>() {}.getType();

                    List<String> traceOrgEventsList = gson.fromJson(gson.toJson(traceOrgEvent), listType);

                    if(traceOrgEventsList !=null && !traceOrgEventsList.isEmpty() && traceOrgEventsList.get(1) != null && traceOrgEventsList.get(2) != null)
                    {
                        if(!eventMonth.contains(traceOrgEventsList.get(0)))
                        {
                            eventSummaryData.put("name",traceOrgEventsList.get(0));

                            eventSummaryData.put("severity",traceOrgEventsList.get(3));

                            eventSummaryData.put("count",traceOrgEventsList.get(1));

                            if(traceOrgEventsList.get(3) .equals("0"))
                            {
                                eventSummaryData.put("color","#FF0000");
                            }
                            else if(traceOrgEventsList.get(3) .equals("1"))
                            {
                                eventSummaryData.put("color","#FFA31A");
                            }
                            else if(traceOrgEventsList.get(3) .equals("2"))
                            {
                                eventSummaryData.put("color","#00b3ee");
                            }

                            eventMonth.add(traceOrgEventsList.get(0));
                        }
                    }
                    eventSummary.add(eventSummaryData);
                }

                response.setData(eventSummary);

                response.setSuccess(TraceOrgCommonConstants.TRUE);

            }
            else
            {
                response.setSuccess(TraceOrgCommonConstants.FALSE);

                response.setMessage(TraceOrgMessageConstants.NO_DATA_AVAILABLE);
            }
        }
        catch (Exception exception)
        {
            _logger.error(exception);

            response.setSuccess(TraceOrgCommonConstants.FALSE);

            response.setMessage(TraceOrgMessageConstants.SOMETHING_WENT_WRONG);
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    @RequestMapping(value = TraceOrgCommonConstants.EVENT_BY_DATE_REST_URL, method = RequestMethod.GET)
    @PreAuthorize("hasAuthority('PERM_EVENT NOTIFICATIONS_READ')")
    public ResponseEntity<?> eventSummaryByDate(HttpServletRequest request, @RequestParam String fromDate,@RequestParam String toDate)
    {
        Response response = new Response();
        try
        {
            String accessToken = request.getHeader(TraceOrgCommonConstants.ACCESSTOKEN);

            List<TraceOrgEvent> traceOrgEventList = (List<TraceOrgEvent>) this.traceOrgService.commonQuery("",TraceOrgCommonConstants.TRACE_ORG_EVENT +" where timestamp between '"+fromDate+"' and '"+toDate+"'  order by id desc");

            if(traceOrgEventList !=null && !traceOrgEventList.isEmpty())
            {
                response.setSuccess(TraceOrgCommonConstants.TRUE);

                response.setData(traceOrgEventList);
            }
            else
            {
                response.setMessage(TraceOrgMessageConstants.NO_DATA_AVAILABLE);

                response.setSuccess(TraceOrgCommonConstants.TRUE);
            }
        }
        catch (Exception exception)
        {
            _logger.error(exception);

            response.setSuccess(TraceOrgCommonConstants.FALSE);

            response.setMessage(TraceOrgMessageConstants.ENTER_VALID_DETAILS);
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    @RequestMapping(value = TraceOrgCommonConstants.EXPORT_EVENT_BY_DATE_PDF_REST_URL, method = RequestMethod.GET)
    @PreAuthorize("hasAuthority('PERM_EVENT NOTIFICATIONS_READ')")
    public ResponseEntity<?> eventSummaryPdfByDate(HttpServletRequest request, @RequestParam String fromDate,@RequestParam String toDate)
    {
        Response response = new Response();
        try
        {
            String accessToken = request.getHeader(TraceOrgCommonConstants.ACCESSTOKEN);

            List<TraceOrgEvent> traceOrgEventList = (List<TraceOrgEvent>) this.traceOrgService.commonQuery("",TraceOrgCommonConstants.TRACE_ORG_EVENT +" where timestamp between '"+fromDate+"' and '"+toDate+"'  order by id desc");

            if(traceOrgEventList !=null && !traceOrgEventList.isEmpty())
            {
                try
                {
                    String fileName = null;

                    HashMap<String, Object> gridReport = new HashMap<>();

                    gridReport.put("Title", "All Event Log Report");
                    try
                    {

                        LinkedHashSet<String> columns = new LinkedHashSet<String>()
                        {{
                            add("Event Type");

                            add("Event Context");

                            add("Time");

                            add("Done By User");

                        }};

                        List<Object> pdfResults = new ArrayList<>();

                        List<Object> pdfResult;

                        String subnetAddress = null;

                        for (TraceOrgEvent traceOrgEvent : traceOrgEventList)
                        {
                            pdfResult = new ArrayList<>();

                            pdfResult.add(traceOrgEvent.getEventType());

                            pdfResult.add(traceOrgEvent.getEventContext());

                            pdfResult.add(traceOrgEvent.getTimestamp());

                            if(traceOrgEvent.getDoneBy()!=null)
                            {
                                pdfResult.add(traceOrgEvent.getDoneBy().getUserName());
                            }
                            else
                            {
                                pdfResult.add(" ");
                            }

                            pdfResults.add(pdfResult);
                        }

                        HashMap<String, Object> results = new HashMap<>();

                        results.put("grid-result", pdfResults);

                        results.put("columns", columns);

                        List<HashMap<String, Object>> visualizationResults = new ArrayList<>();

                        visualizationResults.add(results);

                        gridReport.put("Title", "Event Log ");

                        fileName = "Event Log Report " + TraceOrgCommonConstants.VISUAL_DATE_FORMAT.format(new Date()) + ".pdf";

                        fileName = fileName.replace(" ", "_").replace(":", "_").replace(",", "");

                        TraceOrgPDFBuilder.addGridReport(1, visualizationResults, new HashMap<String, Object>(), fileName, gridReport);
                    }
                    catch (Exception exception)
                    {
                        _logger.error(exception);
                    }

                    response.setData(fileName);

                    response.setSuccess(TraceOrgCommonConstants.TRUE);
                }
                catch (Exception exception)
                {
                    _logger.error(exception);

                    response.setSuccess(TraceOrgCommonConstants.FALSE);
                }
            }
            else
            {
                response.setMessage(TraceOrgMessageConstants.NO_DATA_AVAILABLE);

                response.setSuccess(TraceOrgCommonConstants.TRUE);
            }
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
