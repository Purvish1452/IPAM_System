package com.motadata.traceorg.ipam.controller.dhcp;

import com.motadata.traceorg.ipam.logger.TraceOrgLogger;
import com.motadata.traceorg.ipam.entity.Response;
import com.motadata.traceorg.ipam.entity.dhcp.TraceOrgDhcpUtilization;
import com.motadata.traceorg.ipam.services.TraceOrgService;
import com.motadata.traceorg.ipam.util.TraceOrgCommonConstants;
import com.motadata.traceorg.ipam.util.TraceOrgCommonUtil;
import com.motadata.traceorg.ipam.util.TraceOrgMessageConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@SuppressWarnings("ALL")
@RestController
public class TraceOrgDhcpUtilizationController
{
    @Autowired
    private TraceOrgService traceOrgService;

    @Autowired
    private TraceOrgCommonUtil traceOrgCommonUtil;

    private static final TraceOrgLogger _logger = new TraceOrgLogger(TraceOrgDhcpUtilizationController.class, "DHCP  Utilization Controller");

    @RequestMapping(value = TraceOrgCommonConstants.DHCP_UTILIZATION_REST_URL, method = RequestMethod.GET)
    public ResponseEntity<?> listDhcpUtilizationDetails(HttpServletRequest request)
    {
        Response response = new Response();

        try
        {
            List<TraceOrgDhcpUtilization> traceOrgDhcpUtilizationList = (List<TraceOrgDhcpUtilization>) this.traceOrgService.commonQuery("",TraceOrgCommonConstants.TRACE_ORG_DHCP_UTILIZATION);

            if(traceOrgDhcpUtilizationList !=null && !traceOrgDhcpUtilizationList.isEmpty())
            {
                response.setData(traceOrgDhcpUtilizationList);

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


    @RequestMapping(value = TraceOrgCommonConstants.DHCP_UTILIZATION_REST_URL+"{id}", method = RequestMethod.GET)
    public ResponseEntity<?> listDhcpUtilizationByDhcpCredentialId(@PathVariable(TraceOrgCommonConstants.ID) Long id, HttpServletRequest request)
    {
        Response response = new Response();
        if (id != null)
        {
            try
            {
                List<TraceOrgDhcpUtilization> traceOrgDhcpUtilizationList = (List<TraceOrgDhcpUtilization>) this.traceOrgService.commonQuery("",TraceOrgCommonConstants.TRACE_ORG_DHCP_UTILIZATION +" where dhcpCredentialDetailId = '"+id+"'");

                if(traceOrgDhcpUtilizationList !=null && !traceOrgDhcpUtilizationList.isEmpty())
                {
                    response.setData(traceOrgDhcpUtilizationList.get(0));

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

                response.setMessage(TraceOrgMessageConstants.NO_DATA_AVAILABLE);
            }
        }
        else
        {
            response.setSuccess(TraceOrgCommonConstants.FALSE);

            response.setMessage(TraceOrgMessageConstants.ENTER_VALID_DETAILS);
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}
