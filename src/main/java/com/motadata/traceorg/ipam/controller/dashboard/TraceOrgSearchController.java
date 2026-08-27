package com.motadata.traceorg.ipam.controller.dashboard;

import com.motadata.traceorg.ipam.logger.TraceOrgLogger;
import com.motadata.traceorg.ipam.entity.Response;
import com.motadata.traceorg.ipam.entity.subnet.TraceOrgSubnetIpDetails;
import com.motadata.traceorg.ipam.services.TraceOrgService;
import com.motadata.traceorg.ipam.util.TraceOrgCommonConstants;
import com.motadata.traceorg.ipam.util.TraceOrgCommonUtil;
import com.motadata.traceorg.ipam.util.TraceOrgMessageConstants;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@SuppressWarnings("ALL")
@RestController
public class TraceOrgSearchController
{
    @Autowired
    private TraceOrgService traceOrgService;

    @Autowired
    private TraceOrgCommonUtil traceOrgCommonUtil;

    private static final TraceOrgLogger _logger = new TraceOrgLogger(TraceOrgSearchController.class, "Global Search Controller");

    /**
     * IPAM-145 : System should have rogue device detection capability
     * Added functionality find authenticity in global search.
     */

    @RequestMapping(value = "/search/", method = RequestMethod.POST)
    public ResponseEntity<?> listAllSearchData(HttpServletRequest request, @RequestParam String searchParam)
    {
        Response response = new Response();

        if(searchParam !=null && !searchParam.isEmpty() && !StringUtils.isEmpty(searchParam))
        {
            try
            {
                String accessToken = request.getHeader(TraceOrgCommonConstants.ACCESSTOKEN);

                if (traceOrgCommonUtil.checkToken(accessToken))
                {

                    List<TraceOrgSubnetIpDetails> traceOrgSubnetIpDetailList = (List<TraceOrgSubnetIpDetails>)this.traceOrgService.commonQuery("","TraceOrgSubnetIpDetails where concat(COALESCE(ipAddress,''),'',COALESCE(deviceType,''),'',COALESCE(ipToDns,''),'',COALESCE(authenticity,'')) like '%"+searchParam.trim()+"%'");

                    List<TraceOrgSubnetIpDetails> traceOrgSubnetIpDetailBySubnetList = (List<TraceOrgSubnetIpDetails>)this.traceOrgService.commonQuery("","TraceOrgSubnetIpDetails where subnetId in (Select id from  TraceOrgSubnetDetails where concat(COALESCE(subnetAddress,''),'',COALESCE(subnetName,''),'',COALESCE(description,'')) like  '%"+searchParam.trim()+"%')");

                    Set<TraceOrgSubnetIpDetails> searchOutput = new HashSet<>();

                    if(traceOrgSubnetIpDetailList !=null && !traceOrgSubnetIpDetailList.isEmpty())
                    {
                        searchOutput.addAll(traceOrgSubnetIpDetailList);
                    }

                    if(traceOrgSubnetIpDetailBySubnetList !=null && !traceOrgSubnetIpDetailBySubnetList.isEmpty())
                    {
                        searchOutput.addAll(traceOrgSubnetIpDetailBySubnetList);
                    }

                    if(!searchOutput.isEmpty())
                    {

                        searchOutput.forEach(subnetIpDetails->{
                            subnetIpDetails.setSubnetName(subnetIpDetails.getSubnetId().getSubnetName());
                        });

                        response.setData(searchOutput);

                        response.setSuccess(TraceOrgCommonConstants.TRUE);
                    }
                    else
                    {
                        response.setMessage(TraceOrgMessageConstants.NO_DATA_AVAILABLE);

                        response.setSuccess(TraceOrgCommonConstants.FALSE);
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

                response.setMessage(TraceOrgMessageConstants.ENTER_VALID_DETAILS);
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
