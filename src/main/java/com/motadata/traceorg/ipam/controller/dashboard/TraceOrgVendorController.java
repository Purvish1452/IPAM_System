package com.motadata.traceorg.ipam.controller.dashboard;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.motadata.traceorg.ipam.logger.TraceOrgLogger;
import com.motadata.traceorg.ipam.entity.Response;
import com.motadata.traceorg.ipam.services.TraceOrgService;
import com.motadata.traceorg.ipam.util.TraceOrgCommonConstants;
import com.motadata.traceorg.ipam.util.TraceOrgCommonUtil;
import com.motadata.traceorg.ipam.util.TraceOrgMessageConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@SuppressWarnings({"All"})
@RestController
public class TraceOrgVendorController
{
    @Autowired
    private TraceOrgService traceOrgService;

    @Autowired
    private TraceOrgCommonUtil traceOrgCommonUtil;

    private static final TraceOrgLogger _logger = new TraceOrgLogger(TraceOrgVendorController.class, "Vendor Controller");

    @RequestMapping(value = TraceOrgCommonConstants.VENDOR_REST_URL, method = RequestMethod.GET)
    @PreAuthorize("hasAuthority('PERM_DASHBOARD_READ')")
    public ResponseEntity<?> listAllVendorByUsedIp(HttpServletRequest request)
    {
        Response response = new Response();

        try
        {
            String accessToken = request.getHeader(TraceOrgCommonConstants.ACCESSTOKEN);

            List<Object> vendorList = (List<Object>) this.traceOrgService.commonQuery(TraceOrgCommonConstants.SELECT_VENDOR_WITH_COUNT,TraceOrgCommonConstants.VENDOR_COUNT_BY_USED_IP);

            if(vendorList != null && !vendorList.isEmpty())
            {
                List<Object> vendorDetailsList = new LinkedList<>();

                for(Object vendorOutputs : vendorList)
                {
                    Map<String,Object> vendorDetails = new HashMap<>();

                    Gson gson= new Gson();

                    Type listType = new TypeToken<List<String>>() {}.getType();

                    List<String> vendorOutputsList = gson.fromJson(gson.toJson(vendorOutputs), listType);

                    if(vendorOutputsList !=null && !vendorOutputsList.isEmpty() && vendorOutputsList.get(1) != null)
                    {
                        vendorDetails.put(TraceOrgCommonConstants.VENDOR_NAME,vendorOutputsList.get(1));

                        vendorDetails.put(TraceOrgCommonConstants.VENDOR_COUNT,Long.parseLong(vendorOutputsList.get(0)));
                    }
                    if(vendorDetails!=null && !vendorDetails.isEmpty())
                    {
                        vendorDetailsList.add(vendorDetails);
                    }
                }

                if(vendorDetailsList!=null && !vendorDetailsList.isEmpty())
                {
                    if(vendorDetailsList.size() > 10)
                    {
                        response.setData(vendorDetailsList.subList(0,10));
                    }
                    else
                    {
                        response.setData(vendorDetailsList);
                    }
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

   /* @RequestMapping(value = TraceOrgCommonConstants.VENDOR_REST_URL, method = RequestMethod.GET)
    public ResponseEntity<?> listAllVendorByUsedIp(HttpServletRequest request)
    {
        Response response = new Response();

        try
        {
            String accessToken = request.getHeader(TraceOrgCommonConstants.ACCESSTOKEN);

            if(traceOrgCommonUtil.checkToken(accessToken))
            {
                List<Object> vendorList = (List<Object>) this.traceOrgService.commonQuery(TraceOrgCommonConstants.SELECT_VENDOR_WITH_COUNT,TraceOrgCommonConstants.VENDOR_COUNT_BY_USED_IP);

                if(vendorList != null && !vendorList.isEmpty())
                {
                    Map<String,List> vendorDetails = new HashMap<>();

                    List<String> vendorName = new LinkedList<>();

                    List<Long> vendorCount = new LinkedList<>();

                    for(Object vendorOutputs : vendorList)
                    {
                        Gson gson= new Gson();

                        Type listType = new TypeToken<List<String>>() {}.getType();

                        List<String> vendorOutputsList = gson.fromJson(gson.toJson(vendorOutputs), listType);

                        if(vendorOutputsList !=null && !vendorOutputsList.isEmpty() && vendorOutputsList.get(1) != null)
                        {
                            vendorName.add(vendorOutputsList.get(1));

                            vendorCount.add(Long.parseLong(vendorOutputsList.get(0)));
                        }
                    }

                    if(vendorName!=null && !vendorName.isEmpty() && vendorCount!=null && !vendorCount.isEmpty())
                    {
                        vendorDetails.put(TraceOrgCommonConstants.VENDOR_NAME,vendorName);

                        vendorDetails.put(TraceOrgCommonConstants.VENDOR_COUNT,vendorCount);
                    }

                    if(vendorDetails!=null && !vendorDetails.isEmpty())
                    {
                        response.setData(vendorDetails);
                    }

                    response.setSuccess(TraceOrgCommonConstants.TRUE);

                    response.setCurrentUserRole(traceOrgCommonUtil.currentUserRole(accessToken));
                }
                else
                {
                    response.setSuccess(TraceOrgCommonConstants.FALSE);

                    response.setMessage(TraceOrgMessageConstants.NO_DATA_AVAILABLE);
                }
            }
            else
            {
                response.setSuccess(TraceOrgCommonConstants.FALSE);

                response.setMessage(TraceOrgMessageConstants.TOKEN_NOT_RECOGNISED);
            }
        }
        catch (Exception exception)
        {
            _logger.error(exception);

            response.setSuccess(TraceOrgCommonConstants.FALSE);

            response.setMessage(TraceOrgMessageConstants.SOMETHING_WENT_WRONG);
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }*/

}
