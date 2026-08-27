package com.motadata.traceorg.ipam.controller.subnet;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.motadata.traceorg.ipam.entity.rogueDetection.TraceOrgRogueDetection;
import com.motadata.traceorg.ipam.entity.settings.TraceOrgCustomColumn;
import com.motadata.traceorg.ipam.logger.TraceOrgLogger;
import com.motadata.traceorg.ipam.entity.*;
import com.motadata.traceorg.ipam.entity.subnet.TraceOrgIPChangeLog;
import com.motadata.traceorg.ipam.entity.subnet.TraceOrgSubnetDetails;
import com.motadata.traceorg.ipam.entity.subnet.TraceOrgSubnetIpDetails;
import com.motadata.traceorg.ipam.repository.rogueDetection.TraceOrgRogueDetectionRepository;
import com.motadata.traceorg.ipam.repository.settings.TraceOrgCustomColumnRepository;
import com.motadata.traceorg.ipam.repository.subnet.TraceOrgSubnetIpDetailsRepository;
import com.motadata.traceorg.ipam.services.TraceOrgService;
import com.motadata.traceorg.ipam.services.subnet.TraceOrgSubnetIpService;
import com.motadata.traceorg.ipam.util.TraceOrgCommonConstants;
import com.motadata.traceorg.ipam.util.TraceOrgCommonUtil;
import com.motadata.traceorg.ipam.util.TraceOrgMessageConstants;
import com.motadata.traceorg.ipam.util.TraceOrgPDFBuilder;
import de.siegmar.fastcsv.reader.CsvContainer;
import de.siegmar.fastcsv.reader.CsvReader;
import de.siegmar.fastcsv.reader.CsvRow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.*;

/**
 * @author Krunal Thakkar
 *
 */

@SuppressWarnings("ALL")
@RestController
public class TraceOrgSubnetIpController
{
    private TraceOrgService traceOrgService;

    private TraceOrgRogueDetectionRepository traceOrgRogueDetectionRepository;

    private TraceOrgCommonUtil traceOrgCommonUtil;

    private TraceOrgSubnetIpService traceOrgSubnetIpService;

    private TraceOrgCustomColumnRepository traceOrgCustomColumnRepository;

    private TraceOrgSubnetIpDetailsRepository traceOrgSubnetIpDetailsRepository;

    public TraceOrgSubnetIpController(TraceOrgService traceOrgService, TraceOrgRogueDetectionRepository traceOrgRogueDetectionRepository, TraceOrgCommonUtil traceOrgCommonUtil, TraceOrgSubnetIpService traceOrgSubnetIpService, TraceOrgCustomColumnRepository traceOrgCustomColumnRepository, TraceOrgSubnetIpDetailsRepository traceOrgSubnetIpDetailsRepository) {
        this.traceOrgService = traceOrgService;
        this.traceOrgRogueDetectionRepository = traceOrgRogueDetectionRepository;
        this.traceOrgCommonUtil = traceOrgCommonUtil;
        this.traceOrgSubnetIpService = traceOrgSubnetIpService;
        this.traceOrgCustomColumnRepository = traceOrgCustomColumnRepository;
        this.traceOrgSubnetIpDetailsRepository = traceOrgSubnetIpDetailsRepository;
    }

    private static final TraceOrgLogger _logger = new TraceOrgLogger(TraceOrgSubnetIpController.class, "Subnet IP Controller");

    @RequestMapping(value = TraceOrgCommonConstants.SUBNET_IP_REST_URL, method = RequestMethod.GET)
    @PreAuthorize("hasAuthority('PERM_DASHBOARD_READ')")
    public ResponseEntity<?> listAllSubnetIp(HttpServletRequest request)
    {
        Response response = new Response();

        try
        {
            String accessToken = request.getHeader(TraceOrgCommonConstants.ACCESSTOKEN);

            List<TraceOrgSubnetIpDetails> subnetIpDetailsList = (List<TraceOrgSubnetIpDetails>) this.traceOrgService.commonQuery("",TraceOrgCommonConstants.TRACE_ORG_SUBNET_IP_DETAILS);

            if(subnetIpDetailsList!=null && !subnetIpDetailsList.isEmpty())
            {
                response.setData(subnetIpDetailsList);

                response.setSuccess(TraceOrgCommonConstants.TRUE);
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

    @RequestMapping(value = TraceOrgCommonConstants.SUBNET_IP_REST_URL+"{id}", method = RequestMethod.GET)
    @PreAuthorize("hasAuthority('PERM_DASHBOARD_READ')")
    public ResponseEntity<?> getSubnetIp(@PathVariable(TraceOrgCommonConstants.ID) Long id, HttpServletRequest request)
    {
        Response response = new Response();

        if(id !=null)
        {
            try
            {
                String accessToken = request.getHeader(TraceOrgCommonConstants.ACCESSTOKEN);

                TraceOrgSubnetIpDetails subnetIpDetails = (TraceOrgSubnetIpDetails) this.traceOrgService.getById(TraceOrgCommonConstants.TRACE_ORG_SUBNET_IP_DETAILS, id);

                if (subnetIpDetails != null)
                {

                    response.setSuccess(TraceOrgCommonConstants.TRUE);

                    response.setData(subnetIpDetails);
                }
                else
                {
                    response.setSuccess(TraceOrgCommonConstants.FALSE);

                    response.setMessage(TraceOrgMessageConstants.SUBNET_IP_ID_NOT_VALID);
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

    /**
     * IPAM-129 RFP - Proactive | GMDC | New Requirement
     * Added a method to retrieve the IP change log.
     * */
    @RequestMapping(value = TraceOrgCommonConstants.CHANGE_LOG_URL+"{id}", method = RequestMethod.GET)
    @PreAuthorize("hasAuthority('PERM_DASHBOARD_READ')")
    public ResponseEntity<?> getIpChangeLog(@PathVariable(TraceOrgCommonConstants.ID) Long id, HttpServletRequest request)
    {
        Response response = new Response();

        if(id !=null)
        {
            try
            {
                String accessToken = request.getHeader(TraceOrgCommonConstants.ACCESSTOKEN);

                String date = TraceOrgCommonConstants.DATE_FORMAT.format(LocalDate.now().minusMonths(TraceOrgCommonConstants.CHANGE_LOG_MONTH_LIMIT));

                List<TraceOrgIPChangeLog> traceOrgIPChangeLogs = (List<TraceOrgIPChangeLog>) this.traceOrgService.commonQuery("TraceOrgIPChangeLog where ipAddressId=" + id + " and timestamp >= '" + date + "' order by timestamp DESC");

                if(traceOrgIPChangeLogs != null && !traceOrgIPChangeLogs.isEmpty())
                {
                    response.setSuccess(TraceOrgCommonConstants.TRUE);

                    response.setData(traceOrgIPChangeLogs);
                }
                else
                {
                    response.setSuccess(TraceOrgCommonConstants.FALSE);

                    response.setMessage(TraceOrgMessageConstants.IP_ID_NOT_VALID);
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

    @RequestMapping(value = TraceOrgCommonConstants.SUBNET_IP_REST_URL+"{id}", method = RequestMethod.DELETE)
    @PreAuthorize("hasAuthority('PERM_DASHBOARD_WRITE')")
    public ResponseEntity<?> removeSubnetIp(@PathVariable(TraceOrgCommonConstants.ID) String id,HttpServletRequest request)
    {
        Response response = new Response();

        try
        {
            String accessToken = request.getHeader(TraceOrgCommonConstants.ACCESSTOKEN);

            if(id != null && !id.isEmpty())
            {
                if(id.contains(","))
                {
                    String[] subnetIpIdString = id.split(",");

                    for (String aSubnetIpIdString : subnetIpIdString)
                    {
                        long subnetIPId = Long.parseLong(aSubnetIpIdString);

                        boolean deleteStatus = deactiveSubnetIp(subnetIPId);

                        if (deleteStatus)
                        {
                            TraceOrgSubnetIpDetails traceOrgSubnetIpDetails = (TraceOrgSubnetIpDetails) this.traceOrgService.getById(TraceOrgCommonConstants.TRACE_ORG_SUBNET_IP_DETAILS,subnetIPId);

                            TraceOrgSubnetDetails traceOrgSubnetDetail = (TraceOrgSubnetDetails)this.traceOrgService.getById(TraceOrgCommonConstants.TRACE_ORG_SUBNET_DETAILS,traceOrgSubnetIpDetails.getSubnetId().getId());

                            List<TraceOrgSubnetIpDetails> totalSubnetIpDetailsList = (List<TraceOrgSubnetIpDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_SUBNET_IP_DETAILS +" where subnetId = '"+traceOrgSubnetDetail.getId()+"' and  deactiveStatus = false");

                            List<TraceOrgSubnetIpDetails> availableSubnetIpDetailsList = (List<TraceOrgSubnetIpDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.SUBNET_IP_DETAILS_BY_STATUS_AND_SUBNET_ID.replace(TraceOrgCommonConstants.STATUS_VALUE,TraceOrgCommonConstants.AVAILABLE).replace(TraceOrgCommonConstants.SUBNET_ID_VALUE, TraceOrgCommonUtil.getStringValue(traceOrgSubnetDetail.getId()))+" and  deactiveStatus = false");

                            List<TraceOrgSubnetIpDetails> usedSubnetIpDetailsList = (List<TraceOrgSubnetIpDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.SUBNET_IP_DETAILS_BY_STATUS_AND_SUBNET_ID.replace(TraceOrgCommonConstants.STATUS_VALUE,TraceOrgCommonConstants.USED).replace(TraceOrgCommonConstants.SUBNET_ID_VALUE,TraceOrgCommonUtil.getStringValue(traceOrgSubnetDetail.getId())) +" and  deactiveStatus = false");

                            List<TraceOrgSubnetIpDetails> transientSubnetIpDetailsList = (List<TraceOrgSubnetIpDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.SUBNET_IP_DETAILS_BY_STATUS_AND_SUBNET_ID.replace(TraceOrgCommonConstants.STATUS_VALUE,TraceOrgCommonConstants.TRANSIENT).replace(TraceOrgCommonConstants.SUBNET_ID_VALUE,TraceOrgCommonUtil.getStringValue(traceOrgSubnetDetail.getId())) +" and deactiveStatus = false");

                            traceOrgSubnetDetail.setAvailableIp((long) availableSubnetIpDetailsList.size());

                            traceOrgSubnetDetail.setUsedIp((long) usedSubnetIpDetailsList.size());

                            traceOrgSubnetDetail.setTransientIp((long) transientSubnetIpDetailsList.size());

                            traceOrgSubnetDetail.setTotalIp((long)totalSubnetIpDetailsList.size());

                            traceOrgService.insert(traceOrgSubnetDetail);

                            response.setSuccess(TraceOrgCommonConstants.TRUE);

                            response.setMessage(TraceOrgMessageConstants.SUBNET_IP_DELETE_SUCCESS);

                            _logger.debug("subnet "+traceOrgSubnetDetail.getSubnetAddress()+" ip address deleted successfully..");
                        }
                        else
                        {
                            response.setSuccess(TraceOrgCommonConstants.FALSE);

                            response.setMessage(TraceOrgMessageConstants.SOMETHING_WENT_WRONG);
                        }
                    }
                }
                else
                {
                    long subnetIPId = Long.parseLong(id);

                    boolean deleteStatus = deactiveSubnetIp(subnetIPId);

                    if (deleteStatus)
                    {
                        TraceOrgSubnetIpDetails traceOrgSubnetIpDetails = (TraceOrgSubnetIpDetails) this.traceOrgService.getById(TraceOrgCommonConstants.TRACE_ORG_SUBNET_IP_DETAILS,subnetIPId);

                        TraceOrgSubnetDetails traceOrgSubnetDetail = (TraceOrgSubnetDetails)this.traceOrgService.getById(TraceOrgCommonConstants.TRACE_ORG_SUBNET_DETAILS,traceOrgSubnetIpDetails.getSubnetId().getId());

                        List<TraceOrgSubnetIpDetails> totalSubnetIpDetailsList = (List<TraceOrgSubnetIpDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_SUBNET_IP_DETAILS +" where subnetId = '"+traceOrgSubnetDetail.getId()+"' and  deactiveStatus = false");

                        List<TraceOrgSubnetIpDetails> availableSubnetIpDetailsList = (List<TraceOrgSubnetIpDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.SUBNET_IP_DETAILS_BY_STATUS_AND_SUBNET_ID.replace(TraceOrgCommonConstants.STATUS_VALUE,TraceOrgCommonConstants.AVAILABLE).replace(TraceOrgCommonConstants.SUBNET_ID_VALUE, TraceOrgCommonUtil.getStringValue(traceOrgSubnetDetail.getId()))+" and  deactiveStatus = false");

                        List<TraceOrgSubnetIpDetails> usedSubnetIpDetailsList = (List<TraceOrgSubnetIpDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.SUBNET_IP_DETAILS_BY_STATUS_AND_SUBNET_ID.replace(TraceOrgCommonConstants.STATUS_VALUE,TraceOrgCommonConstants.USED).replace(TraceOrgCommonConstants.SUBNET_ID_VALUE,TraceOrgCommonUtil.getStringValue(traceOrgSubnetDetail.getId())) +" and  deactiveStatus = false");

                        List<TraceOrgSubnetIpDetails> transientSubnetIpDetailsList = (List<TraceOrgSubnetIpDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.SUBNET_IP_DETAILS_BY_STATUS_AND_SUBNET_ID.replace(TraceOrgCommonConstants.STATUS_VALUE,TraceOrgCommonConstants.TRANSIENT).replace(TraceOrgCommonConstants.SUBNET_ID_VALUE,TraceOrgCommonUtil.getStringValue(traceOrgSubnetDetail.getId())) +" and deactiveStatus = false");

                        traceOrgSubnetDetail.setAvailableIp((long) availableSubnetIpDetailsList.size());

                        traceOrgSubnetDetail.setUsedIp((long) usedSubnetIpDetailsList.size());

                        traceOrgSubnetDetail.setTransientIp((long) transientSubnetIpDetailsList.size());

                        traceOrgSubnetDetail.setTotalIp((long)totalSubnetIpDetailsList.size());

                        traceOrgService.insert(traceOrgSubnetDetail);

                        response.setSuccess(TraceOrgCommonConstants.TRUE);

                        response.setMessage(TraceOrgMessageConstants.SUBNET_IP_DELETE_SUCCESS);

                        _logger.debug("subnet "+traceOrgSubnetDetail.getSubnetAddress()+" ip address deleted successfully..");
                    }
                    else
                    {
                        response.setSuccess(TraceOrgCommonConstants.FALSE);

                        response.setMessage(TraceOrgMessageConstants.SOMETHING_WENT_WRONG);
                    }
                }
            }
            else
            {
                response.setSuccess(TraceOrgCommonConstants.FALSE);

                response.setMessage(TraceOrgMessageConstants.ENTER_VALID_DETAILS);
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

    private boolean deactiveSubnetIp(long subnetIPId)
    {
        boolean result = false;

        try
        {
            TraceOrgSubnetIpDetails traceOrgSubnetIpDetails = (TraceOrgSubnetIpDetails) this.traceOrgService.getById(TraceOrgCommonConstants.TRACE_ORG_SUBNET_IP_DETAILS,subnetIPId);

            if (traceOrgSubnetIpDetails != null)
            {
                traceOrgSubnetIpDetails.setDeactiveStatus(true);

                traceOrgSubnetIpDetails.setModifiedDate(new Date());

                result = this.traceOrgService.insert(traceOrgSubnetIpDetails);
            }
        }
        catch (Exception exception)
        {
            _logger.error(exception);
        }

        return result;
    }

    //Available IP Address
    @RequestMapping(value = TraceOrgCommonConstants.SUBNET_AVAILABLE_IP_REST_URL, method = RequestMethod.GET)
    @PreAuthorize("hasAuthority('PERM_DASHBOARD_READ')")
    public ResponseEntity<?> listAllSubnetAvailableIp(HttpServletRequest request)
    {
        Response response = new Response();

        try
        {
            String accessToken = request.getHeader(TraceOrgCommonConstants.ACCESSTOKEN);

            List<TraceOrgSubnetIpDetails> availableSubnetIpDetailsList = (List<TraceOrgSubnetIpDetails>) this.traceOrgService.commonQuery("",TraceOrgCommonConstants.SUBNET_IP_DETAILS_BY_STATUS.replace(TraceOrgCommonConstants.STATUS_VALUE,TraceOrgCommonConstants.AVAILABLE));

            if(availableSubnetIpDetailsList!=null && !availableSubnetIpDetailsList.isEmpty())
            {
                response.setData(availableSubnetIpDetailsList);

                response.setSuccess(TraceOrgCommonConstants.TRUE);
            }
            else
            {
                response.setMessage(TraceOrgMessageConstants.NO_DATA_AVAILABLE);

                response.setSuccess(TraceOrgCommonConstants.FALSE);
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


    //Reserved IP Address
    @RequestMapping(value = TraceOrgCommonConstants.SUBNET_RESERVED_IP_REST_URL, method = RequestMethod.GET)
    @PreAuthorize("hasAuthority('PERM_DASHBOARD_READ')")
    public ResponseEntity<?> listAllSubnetReservedIp(HttpServletRequest request)
    {
        Response response = new Response();

        try
        {
            String accessToken = request.getHeader(TraceOrgCommonConstants.ACCESSTOKEN);

            List<TraceOrgSubnetIpDetails> availableSubnetIpDetailsList = (List<TraceOrgSubnetIpDetails>) this.traceOrgService.commonQuery("",TraceOrgCommonConstants.SUBNET_IP_DETAILS_BY_STATUS.replace(TraceOrgCommonConstants.STATUS_VALUE,TraceOrgCommonConstants.RESERVED));

            if(availableSubnetIpDetailsList!=null && !availableSubnetIpDetailsList.isEmpty())
            {
                response.setData(availableSubnetIpDetailsList);

                response.setSuccess(TraceOrgCommonConstants.TRUE);
            }
            else
            {
                response.setMessage(TraceOrgMessageConstants.NO_DATA_AVAILABLE);

                response.setSuccess(TraceOrgCommonConstants.FALSE);
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

    //Transient IP Address
    @RequestMapping(value = TraceOrgCommonConstants.SUBNET_TRANSIENT_IP_REST_URL, method = RequestMethod.GET)
    @PreAuthorize("hasAuthority('PERM_DASHBOARD_READ')")
    public ResponseEntity<?> listAllSubnetTransientIp(HttpServletRequest request)
    {
        Response response = new Response();

        try
        {
            String accessToken = request.getHeader(TraceOrgCommonConstants.ACCESSTOKEN);

            List<TraceOrgSubnetIpDetails> transientSubnetIpDetailsList = (List<TraceOrgSubnetIpDetails>) this.traceOrgService.commonQuery("",TraceOrgCommonConstants.SUBNET_IP_DETAILS_BY_STATUS.replace(TraceOrgCommonConstants.STATUS_VALUE,TraceOrgCommonConstants.TRANSIENT));

            if(transientSubnetIpDetailsList!=null && !transientSubnetIpDetailsList.isEmpty())
            {
                response.setData(transientSubnetIpDetailsList);

                response.setSuccess(TraceOrgCommonConstants.TRUE);
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


    //USED IP Address
    @RequestMapping(value = TraceOrgCommonConstants.SUBNET_USED_IP_REST_URL, method = RequestMethod.GET)
    @PreAuthorize("hasAuthority('PERM_DASHBOARD_READ')")
    public ResponseEntity<?> listAllSubnetUsedIp(HttpServletRequest request)
    {
        Response response = new Response();

        try
        {
            String accessToken = request.getHeader(TraceOrgCommonConstants.ACCESSTOKEN);

            List<TraceOrgSubnetIpDetails> usedSubnetIpDetailsList = (List<TraceOrgSubnetIpDetails>) this.traceOrgService.commonQuery("",TraceOrgCommonConstants.SUBNET_IP_DETAILS_BY_STATUS.replace(TraceOrgCommonConstants.STATUS_VALUE,TraceOrgCommonConstants.USED));

            if(usedSubnetIpDetailsList!=null && !usedSubnetIpDetailsList.isEmpty())
            {
                response.setData(usedSubnetIpDetailsList);

                response.setSuccess(TraceOrgCommonConstants.TRUE);
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

    //IP ADDRESSES BY SUBNET
    @RequestMapping(value = TraceOrgCommonConstants.SUBNET_IP_BY_SUBNET_REST_URL+"{id}", method = RequestMethod.GET)
    @PreAuthorize("hasAuthority('PERM_DASHBOARD_READ')")
    public ResponseEntity<?> getSubnetIPBySubnetId(@PathVariable(TraceOrgCommonConstants.ID) Long id, HttpServletRequest request)
    {
        Response response = new Response();

        if(id !=null)
        {
            try
            {
                String accessToken = request.getHeader(TraceOrgCommonConstants.ACCESSTOKEN);

                TraceOrgSubnetDetails traceOrgSubnetDetails = (TraceOrgSubnetDetails) this.traceOrgService.getById(TraceOrgCommonConstants.TRACE_ORG_SUBNET_DETAILS, id);

                if(traceOrgSubnetDetails != null)
                {
                    List<TraceOrgSubnetIpDetails> traceOrgSubnetIpDetailsList=traceOrgSubnetIpDetailsRepository.findActiveSubnetIpsOrdered(traceOrgSubnetDetails.getId());

                    if(traceOrgSubnetIpDetailsList != null && !traceOrgSubnetIpDetailsList.isEmpty())
                    {
                        traceOrgSubnetIpDetailsList.forEach(subnetIpDetails->{
                            subnetIpDetails.setSubnetName(subnetIpDetails.getSubnetId().getSubnetName());
                        });
                        response.setSuccess(TraceOrgCommonConstants.TRUE);

                        response.setData(traceOrgSubnetIpDetailsList);
                    }
                }
                else
                {
                    response.setSuccess(TraceOrgCommonConstants.FALSE);

                    response.setMessage(TraceOrgMessageConstants.SUBNET_IP_ID_NOT_VALID);
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


    @RequestMapping(value = TraceOrgCommonConstants.USED_SUBNET_IP_BY_SUBNET_REST_URL+"{id}", method = RequestMethod.GET)
    @PreAuthorize("hasAuthority('PERM_DASHBOARD_READ')")
    public ResponseEntity<?> getUsedSubnetIPBySubnetId(@PathVariable(TraceOrgCommonConstants.ID) Long id, HttpServletRequest request)
    {
        Response response = new Response();

        if(id !=null)
        {
            try
            {
                String accessToken = request.getHeader(TraceOrgCommonConstants.ACCESSTOKEN);

                TraceOrgSubnetDetails traceOrgSubnetDetails = (TraceOrgSubnetDetails) this.traceOrgService.getById(TraceOrgCommonConstants.TRACE_ORG_SUBNET_DETAILS, id);

                if(traceOrgSubnetDetails != null)
                {
                    List<TraceOrgSubnetIpDetails> traceOrgSubnetIpDetailsList = (List<TraceOrgSubnetIpDetails>) this.traceOrgService.commonQuery("",TraceOrgCommonConstants.TRACE_ORG_SUBNET_IP_DETAILS +" where subnetId = '"+id+"' and status = 'USED'");

                    if(traceOrgSubnetIpDetailsList != null && !traceOrgSubnetIpDetailsList.isEmpty())
                    {
                        response.setSuccess(TraceOrgCommonConstants.TRUE);

                        response.setData(traceOrgSubnetIpDetailsList);
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

                    response.setMessage(TraceOrgMessageConstants.SUBNET_IP_ID_NOT_VALID);
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


    @RequestMapping(value = TraceOrgCommonConstants.AVAILABLE_SUBNET_IP_BY_SUBNET_REST_URL+"{id}", method = RequestMethod.GET)
    @PreAuthorize("hasAuthority('PERM_DASHBOARD_READ')")
    public ResponseEntity<?> getAvailableSubnetIPBySubnetId(@PathVariable(TraceOrgCommonConstants.ID) Long id, HttpServletRequest request)
    {
        Response response = new Response();

        if(id !=null)
        {
            try
            {
                String accessToken = request.getHeader(TraceOrgCommonConstants.ACCESSTOKEN);

                TraceOrgSubnetDetails traceOrgSubnetDetails = (TraceOrgSubnetDetails) this.traceOrgService.getById(TraceOrgCommonConstants.TRACE_ORG_SUBNET_DETAILS, id);

                if(traceOrgSubnetDetails != null)
                {
                    List<TraceOrgSubnetIpDetails> traceOrgSubnetIpDetailsList = (List<TraceOrgSubnetIpDetails>) this.traceOrgService.commonQuery("",TraceOrgCommonConstants.TRACE_ORG_SUBNET_IP_DETAILS +" where subnetId = '"+id+"' and status = 'AVAILABLE'");

                    if(traceOrgSubnetIpDetailsList != null && !traceOrgSubnetIpDetailsList.isEmpty())
                    {
                        response.setSuccess(TraceOrgCommonConstants.TRUE);

                        response.setData(traceOrgSubnetIpDetailsList);
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

                    response.setMessage(TraceOrgMessageConstants.SUBNET_IP_ID_NOT_VALID);
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

    @RequestMapping(value = TraceOrgCommonConstants.RESERVED_SUBNET_IP_BY_SUBNET_REST_URL+"{id}", method = RequestMethod.GET)
    @PreAuthorize("hasAuthority('PERM_DASHBOARD_READ')")
    public ResponseEntity<?> getReservedSubnetIPBySubnetId(@PathVariable(TraceOrgCommonConstants.ID) Long id, HttpServletRequest request)
    {
        Response response = new Response();

        if(id !=null)
        {
            try
            {
                String accessToken = request.getHeader(TraceOrgCommonConstants.ACCESSTOKEN);

                TraceOrgSubnetDetails traceOrgSubnetDetails = (TraceOrgSubnetDetails) this.traceOrgService.getById(TraceOrgCommonConstants.TRACE_ORG_SUBNET_DETAILS, id);

                if(traceOrgSubnetDetails != null)
                {
                    List<TraceOrgSubnetIpDetails> traceOrgSubnetIpDetailsList = (List<TraceOrgSubnetIpDetails>) this.traceOrgService.commonQuery("",TraceOrgCommonConstants.TRACE_ORG_SUBNET_IP_DETAILS +" where subnetId = '"+id+"' and status = 'RESERVED'");

                    if(traceOrgSubnetIpDetailsList != null && !traceOrgSubnetIpDetailsList.isEmpty())
                    {
                        response.setSuccess(TraceOrgCommonConstants.TRUE);

                        response.setData(traceOrgSubnetIpDetailsList);
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

                    response.setMessage(TraceOrgMessageConstants.SUBNET_IP_ID_NOT_VALID);
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

    /**
     * IPAM-145 : System should have rogue device detection capability
     * When Subnet Ip import from csv we can't update authenticity if it is existed in rogue detection mark the same authenticity otherwise put '-'.
     * IPAM-149 : IPAM Roadmap : System should have alert notification module to configure different kind of alert notification
     * Added code for NEW_SUBNETS_DISCOVERED Alert
     * IPAM-160 IPAM Roadmap : The solution must be flexible to allow the creation of custom fields for objects in IPAM. This must be configurable via the Web GUI.
     * Added custom column support in import
     * @param request
     * @param subnetIpCsv
     * @param subnetId
     * @return
     */
    @RequestMapping(value = TraceOrgCommonConstants.SUBNET_IP_CSV_REST_URL, method = RequestMethod.POST)
    @PreAuthorize("hasAuthority('PERM_DASHBOARD_WRITE')")
    public ResponseEntity<?> importSubnetIpFromCSV(HttpServletRequest request,@RequestParam MultipartFile subnetIpCsv,@RequestParam Long subnetId)
    {
        Response response = new Response();

        List<String> headers = new ArrayList<>();

        if(subnetIpCsv !=null && !subnetIpCsv.isEmpty() && subnetId!=null && traceOrgService.isExist(TraceOrgCommonConstants.TRACE_ORG_SUBNET_DETAILS,"id",subnetId.toString()))
        {
            try
            {
                String accessToken = request.getHeader(TraceOrgCommonConstants.ACCESSTOKEN);

                if(subnetIpCsv.getOriginalFilename().toLowerCase().endsWith("csv"))
                {
                    boolean importStatus = traceOrgCommonUtil.importCSVFile(subnetIpCsv, request, TraceOrgCommonConstants.SUBNET_IP_DETAIL_CSV_NAME);

                    if (importStatus)
                    {
                        TraceOrgSubnetDetails traceOrgSubnetDetail = (TraceOrgSubnetDetails)traceOrgService.getById(TraceOrgCommonConstants.TRACE_ORG_SUBNET_DETAILS,subnetId);

                        File importFile = new File(request.getRealPath(TraceOrgCommonConstants.SUBNET_IP_DETAIL_CSV_PATH));

                        CsvReader csvReader = new CsvReader();

                        CsvContainer csv = csvReader.read(importFile, StandardCharsets.UTF_8);

                        boolean validFileStatus = false;

                        if(!traceOrgCommonUtil.checkSubnetIPFileData(csv.getRow(0)))
                        {
                            response.setSuccess(TraceOrgCommonConstants.FALSE);

                            response.setMessage(TraceOrgMessageConstants.INVALID_CSV_HEADER);
                        }
                        else
                        {
                            ObjectMapper objectMapper = new ObjectMapper();

                            for (CsvRow csvRow : csv.getRows())
                            {
                                if (csvRow.getOriginalLineNumber() == 1)
                                {
                                    validFileStatus = traceOrgCommonUtil.checkSubnetIPFileData(csvRow);
                                }

                                if (headers.isEmpty()) {
                                    headers.addAll(csvRow.getFields());
                                    continue;
                                }

                                if(validFileStatus && csvRow.getOriginalLineNumber() > 1)
                                {
                                    if(csvRow.getField(0) == null || csvRow.getField(0).isEmpty()  || csvRow.getField(2) == null || csvRow.getField(2).isEmpty())
                                    {
                                        response.setSuccess(TraceOrgCommonConstants.TRUE);

                                        response.setMessage(TraceOrgMessageConstants.CSV_IMPORT_SUCCESS);
                                    }
                                    else
                                    {
                                        if(csvRow.getField(1) != null && !csvRow.getField(1).isEmpty() &&  !csvRow.getField(1).contains(":"))
                                        {
                                            response.setSuccess(TraceOrgCommonConstants.TRUE);

                                            response.setMessage(TraceOrgMessageConstants.CSV_IMPORT_SUCCESS);
                                        }
                                        else
                                        {
                                            TraceOrgSubnetIpDetails traceOrgSubnetIpDetails = new TraceOrgSubnetIpDetails();

                                            if(csvRow.getField(0) != null && !csvRow.getField(0).isEmpty())
                                            {
                                                traceOrgSubnetIpDetails.setIpAddress(csvRow.getField(0));
                                            }

                                            if(csvRow.getField(1) != null && !csvRow.getField(1).isEmpty())
                                            {
                                                traceOrgSubnetIpDetails.setMacAddress(csvRow.getField(1));
                                            }

                                            if(csvRow.getField(2) != null && !csvRow.getField(2).isEmpty())
                                            {
                                                String status = csvRow.getField(2);

                                                switch(status.toUpperCase())
                                                {
                                                    case "USED" :
                                                        traceOrgSubnetIpDetails.setStatus(TraceOrgCommonConstants.USED);
                                                        break;
                                                    case "TRANSIENT" :
                                                        traceOrgSubnetIpDetails.setStatus(TraceOrgCommonConstants.TRANSIENT);
                                                        break;
                                                    case "AVAILABLE" :
                                                        traceOrgSubnetIpDetails.setStatus(TraceOrgCommonConstants.AVAILABLE);
                                                        break;
                                                    case "RESERVED" :
                                                        traceOrgSubnetIpDetails.setStatus(TraceOrgCommonConstants.RESERVED);
                                                        break;
                                                    default:
                                                        traceOrgSubnetIpDetails.setStatus(TraceOrgCommonConstants.AVAILABLE);
                                                        break;
                                                }
                                            }

                                            if(csvRow.getField(3) != null && !csvRow.getField(3).isEmpty())
                                            {
                                                traceOrgSubnetIpDetails.setIpToDns(csvRow.getField(3));
                                            }

                                            if(csvRow.getField(4) != null && !csvRow.getField(4).isEmpty())
                                            {
                                                traceOrgSubnetIpDetails.setDnsToIp(csvRow.getField(4));
                                            }

                                            if(csvRow.getField(5) != null && !csvRow.getField(5).isEmpty())
                                            {
                                                traceOrgSubnetIpDetails.setDeviceType(csvRow.getField(5));
                                            }

                                            if(csvRow.getFields().size()>8)
                                            {
                                                ObjectNode customColumns = objectMapper.createObjectNode();

                                                for(int index=8;index<csvRow.getFields().size();index++)
                                                {
                                                    customColumns.put(headers.get(index), csvRow.getField(index));
                                                }
                                                traceOrgSubnetIpDetails.setCustomColumns(customColumns);
                                            }

                                            if(traceOrgSubnetIpDetails.getMacAddress() != null && traceOrgSubnetIpDetails.getIpAddress() != null)
                                            {
                                                TraceOrgRogueDetection traceOrgRogueDetection = traceOrgRogueDetectionRepository.findByMacAddressAndIpAddress(traceOrgSubnetIpDetails.getMacAddress(), traceOrgSubnetIpDetails.getIpAddress());

                                                if(traceOrgRogueDetection != null)
                                                {
                                                    traceOrgSubnetIpDetails.setAuthenticity(traceOrgRogueDetection.getAuthenticity());
                                                }
                                                else
                                                {
                                                    traceOrgSubnetIpDetails.setAuthenticity("-");
                                                }
                                            }
                                            else
                                            {
                                                traceOrgSubnetIpDetails.setAuthenticity("-");
                                            }

                                            traceOrgSubnetIpDetails.setSubnetId(traceOrgSubnetDetail);

                                            boolean insertStatus = insertSubnetIpByIpAddress(traceOrgSubnetIpDetails, accessToken, traceOrgSubnetDetail);

                                            if(insertStatus)
                                            {
                                                response.setSuccess(TraceOrgCommonConstants.TRUE);

                                                response.setMessage(TraceOrgMessageConstants.CSV_IMPORT_SUCCESS);
                                            }
                                            else
                                            {
                                                response.setSuccess(TraceOrgCommonConstants.FALSE);

                                                response.setMessage(TraceOrgMessageConstants.SOMETHING_WENT_WRONG);
                                            }
                                        }
                                    }
                                }
                                else
                                {
                                    response.setSuccess(TraceOrgCommonConstants.FALSE);

                                    response.setMessage(TraceOrgMessageConstants.FILE_NOT_VALID);
                                }
                            }

                            List<TraceOrgSubnetIpDetails> totalSubnetIpDetailsList = (List<TraceOrgSubnetIpDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_SUBNET_IP_DETAILS +" where subnetId = '"+traceOrgSubnetDetail.getId()+"' and  deactiveStatus = false");

                            List<TraceOrgSubnetIpDetails> availableSubnetIpDetailsList = (List<TraceOrgSubnetIpDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.SUBNET_IP_DETAILS_BY_STATUS_AND_SUBNET_ID.replace(TraceOrgCommonConstants.STATUS_VALUE,TraceOrgCommonConstants.AVAILABLE).replace(TraceOrgCommonConstants.SUBNET_ID_VALUE, TraceOrgCommonUtil.getStringValue(traceOrgSubnetDetail.getId()))+" and  deactiveStatus = false");

                            List<TraceOrgSubnetIpDetails> usedSubnetIpDetailsList = (List<TraceOrgSubnetIpDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.SUBNET_IP_DETAILS_BY_STATUS_AND_SUBNET_ID.replace(TraceOrgCommonConstants.STATUS_VALUE,TraceOrgCommonConstants.USED).replace(TraceOrgCommonConstants.SUBNET_ID_VALUE,TraceOrgCommonUtil.getStringValue(traceOrgSubnetDetail.getId())) +" and  deactiveStatus = false");

                            List<TraceOrgSubnetIpDetails> transientSubnetIpDetailsList = (List<TraceOrgSubnetIpDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.SUBNET_IP_DETAILS_BY_STATUS_AND_SUBNET_ID.replace(TraceOrgCommonConstants.STATUS_VALUE,TraceOrgCommonConstants.TRANSIENT).replace(TraceOrgCommonConstants.SUBNET_ID_VALUE,TraceOrgCommonUtil.getStringValue(traceOrgSubnetDetail.getId())) +" and deactiveStatus = false");

                            traceOrgSubnetDetail.setAvailableIp((long) availableSubnetIpDetailsList.size());

                            traceOrgSubnetDetail.setUsedIp((long) usedSubnetIpDetailsList.size());

                            traceOrgSubnetDetail.setTransientIp((long) transientSubnetIpDetailsList.size());

                            traceOrgSubnetDetail.setTotalIp((long)totalSubnetIpDetailsList.size());

                            traceOrgService.insert(traceOrgSubnetDetail);

                            String workType = TraceOrgCommonConstants.IP_UTILIZATION
                                    + TraceOrgCommonConstants.VALUE_SEPARATOR
                                    + TraceOrgCommonConstants.IP_UTILIZATION_BELOW;

                            HashMap<String, Object> context = new HashMap<>();

                            context.put("subnetAddress", traceOrgSubnetDetail.getSubnetAddress());

                            context.put("subnetId", traceOrgSubnetDetail.getId());

                            context.put("usedIpPercentage", traceOrgSubnetDetail.getUsedIpPercentage());

                            traceOrgCommonUtil.sendMessage(TraceOrgCommonConstants.ALERT_QUEUE, context, workType);

                            response.setSuccess(TraceOrgCommonConstants.TRUE);

                            response.setMessage(TraceOrgMessageConstants.CSV_IMPORT_SUCCESS);
                        }
                    }
                    else
                    {
                        response.setSuccess(TraceOrgCommonConstants.FALSE);

                        response.setMessage(TraceOrgMessageConstants.FILE_NOT_VALID);
                    }
                }
                else
                {
                    response.setSuccess(TraceOrgCommonConstants.FALSE);

                    response.setMessage(TraceOrgMessageConstants.FILE_NOT_VALID);
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

        return new ResponseEntity<>(response,HttpStatus.OK);
    }

    /**
     * IPAM-129 RFP - Proactive | GMDC | New Requirement
     * Change log is added when import from CSV
     * IPAM-149 : IPAM Roadmap : System should have alert notification module to configure different kind of alert notification
     * Added code for IP_RESERVATION_CHANGE & IP_STATE_CHANGE alert.
     * */
    private boolean insertSubnetIpByIpAddress(TraceOrgSubnetIpDetails traceOrgSubnetIpDetails, String accessToken, TraceOrgSubnetDetails traceOrgSubnetDetail)
    {
        boolean result = false;

        try
        {
            if(this.traceOrgService.isExist(TraceOrgCommonConstants.TRACE_ORG_SUBNET_IP_DETAILS,TraceOrgCommonConstants.IP_ADDRESS,traceOrgSubnetIpDetails.getIpAddress()))
            {
                List<TraceOrgSubnetIpDetails> traceOrgSubnetIpDetailsList = (List<TraceOrgSubnetIpDetails>)this.traceOrgService.commonQuery("",TraceOrgCommonConstants.SUBNET_IP_DETAILS_BY_IP_ADDRESS.replace(TraceOrgCommonConstants.IP_ADDRESS_VALUE,traceOrgSubnetIpDetails.getIpAddress())+ " and subnetId = '"+traceOrgSubnetIpDetails.getSubnetId().getId()+"'");

                if(traceOrgSubnetIpDetailsList != null && !traceOrgSubnetIpDetailsList.isEmpty())
                {
                    TraceOrgSubnetIpDetails traceOrgSubnetIpDetailsExisted =  traceOrgSubnetIpDetailsList.get(0);

                    traceOrgSubnetIpDetailsExisted.setMacAddress(traceOrgSubnetIpDetails.getMacAddress());

                    traceOrgSubnetIpDetailsExisted.setDescription(traceOrgSubnetIpDetails.getDescription());

                    traceOrgSubnetIpDetailsExisted.setDeviceType(traceOrgSubnetIpDetails.getDeviceType());

                    traceOrgSubnetIpDetailsExisted.setDnsStatus(traceOrgSubnetIpDetails.getDnsStatus());

                    traceOrgSubnetIpDetailsExisted.setHostName(traceOrgSubnetIpDetails.getHostName());

                    traceOrgSubnetIpDetailsExisted.setAuthenticity(traceOrgSubnetIpDetails.getAuthenticity());

                    traceOrgSubnetIpDetailsExisted.setIpToDns(traceOrgSubnetIpDetails.getIpToDns());

                    traceOrgSubnetIpDetailsExisted.setDnsToIp(traceOrgSubnetIpDetails.getDnsToIp());

                    traceOrgSubnetIpDetailsExisted.setDeactiveStatus(TraceOrgCommonConstants.FALSE);

                    traceOrgSubnetIpDetailsExisted.setPreviousStatus(traceOrgSubnetIpDetailsExisted.getStatus());

                    traceOrgSubnetIpDetailsExisted.setCustomColumns(traceOrgSubnetIpDetails.getCustomColumns());

                    switch(traceOrgSubnetIpDetails.getStatus().toUpperCase())
                    {
                        case "AVAILABLE" :
                            traceOrgSubnetIpDetailsExisted.setStatus(TraceOrgCommonConstants.AVAILABLE);
                            break;
                        case "TRANSIENT" :
                            traceOrgSubnetIpDetailsExisted.setStatus(TraceOrgCommonConstants.TRANSIENT);
                            break;
                        case "USED" :
                            traceOrgSubnetIpDetailsExisted.setStatus(TraceOrgCommonConstants.USED);
                            break;
                        case "RESERVED" :
                            traceOrgSubnetIpDetailsExisted.setStatus(TraceOrgCommonConstants.RESERVED);
                            break;
                        default:
                            traceOrgSubnetIpDetailsExisted.setStatus(null);
                            break;
                    }

                    traceOrgSubnetIpDetails.setModifiedDate(new Date());

                    traceOrgSubnetIpDetailsExisted.setDeactiveStatus(TraceOrgCommonConstants.FALSE);

                    if(traceOrgSubnetIpDetailsExisted.getStatus() !=null)
                    {
                        result = this.traceOrgService.insert(traceOrgSubnetIpDetailsExisted);
                    }

                    if (!traceOrgSubnetIpDetailsExisted.getPreviousStatus().equalsIgnoreCase(traceOrgSubnetIpDetailsExisted.getStatus()))
                    {
                        TraceOrgIPChangeLog traceOrgIPChangeLog = new TraceOrgIPChangeLog(
                                traceOrgCommonUtil.currentUser(accessToken).getUserName(),
                                traceOrgSubnetIpDetailsExisted.getId(),
                                traceOrgSubnetIpDetailsExisted.getSubnetId().getId(),
                                traceOrgSubnetIpDetails.getIpAddress(),
                                new Date(),
                                TraceOrgCommonConstants.CHANGE_LOG_MESSAGE.replace(TraceOrgCommonConstants.PREVIOUS_STATUS,traceOrgSubnetIpDetailsExisted.getPreviousStatus()).replace(TraceOrgCommonConstants.CURRENT_STATUS, traceOrgSubnetIpDetailsExisted.getStatus())
                        );

                        traceOrgService.insert(traceOrgIPChangeLog);

                        HashMap<String, Object> context = new HashMap<>();

                        context.put("previousStatus", traceOrgSubnetIpDetailsExisted.getPreviousStatus());

                        context.put("currentStatus", traceOrgSubnetIpDetailsExisted.getStatus());

                        context.put("ipAddress", traceOrgSubnetIpDetails.getIpAddress());

                        context.put("subnetAddress", traceOrgSubnetDetail.getSubnetAddress());

                        traceOrgCommonUtil.sendMessage(TraceOrgCommonConstants.ALERT_QUEUE, context,
                                TraceOrgCommonConstants.IP_STATE_CHANGE
                                        + TraceOrgCommonConstants.VALUE_SEPARATOR
                                        + TraceOrgCommonConstants.IP_RESERVATION_CHANGE);
                    }
                }
            }
        }
        catch (Exception exception)
        {
            _logger.error(exception);
        }
        return result;
    }


    @RequestMapping(value = TraceOrgCommonConstants.ACTIVE_SUBNET_IP_RANGE_REST_URL, method = RequestMethod.POST)
    @PreAuthorize("hasAuthority('PERM_DASHBOARD_WRITE')")
    public ResponseEntity<?> activeSubnetIpRange(HttpServletRequest request, @RequestParam String startIp , @RequestParam String endIp , @RequestParam Long subnetId)
    {
        Response response = new Response();

        if( startIp != null && !startIp.trim().isEmpty() && endIp != null && !endIp.trim().isEmpty() && subnetId != null)
        {
            try
            {
                String accessToken = request.getHeader(TraceOrgCommonConstants.ACCESSTOKEN);


                TraceOrgSubnetDetails traceOrgSubnetDetail = (TraceOrgSubnetDetails)this.traceOrgService.getById(TraceOrgCommonConstants.TRACE_ORG_SUBNET_DETAILS,subnetId);

                if(traceOrgSubnetDetail != null)
                {
                    if( this.traceOrgCommonUtil.isValidIp(traceOrgSubnetDetail,startIp) &&  this.traceOrgCommonUtil.isValidIp(traceOrgSubnetDetail,endIp))
                    {

                        if(Long.parseLong(startIp.substring(startIp.lastIndexOf(".")+ 1,startIp.length())) < Long.parseLong(endIp.substring(endIp.lastIndexOf(".")+1,endIp.length())))
                        {
                            Long startIpId = 0L;

                            Long endIpId = 0L;

                            List<TraceOrgSubnetIpDetails> traceOrgSubnetIpDetailsStartList = (List<TraceOrgSubnetIpDetails>)this.traceOrgService.commonQuery("",TraceOrgCommonConstants.SUBNET_IP_DETAILS_BY_IP_ADDRESS.replace(TraceOrgCommonConstants.IP_ADDRESS_VALUE,startIp) + " and subnetId = '"+subnetId+"'");

                            if(traceOrgSubnetIpDetailsStartList != null && !traceOrgSubnetIpDetailsStartList.isEmpty())
                            {
                                startIpId = traceOrgSubnetIpDetailsStartList.get(0).getId();

                            }
                            List<TraceOrgSubnetIpDetails> traceOrgSubnetIpDetailsEndList = (List<TraceOrgSubnetIpDetails>)this.traceOrgService.commonQuery("",TraceOrgCommonConstants.SUBNET_IP_DETAILS_BY_IP_ADDRESS.replace(TraceOrgCommonConstants.IP_ADDRESS_VALUE,endIp)+ " and subnetId = '"+subnetId+"'");

                            if(traceOrgSubnetIpDetailsEndList !=null && !traceOrgSubnetIpDetailsEndList.isEmpty())
                            {
                                endIpId = traceOrgSubnetIpDetailsEndList.get(0).getId();
                            }

                            if(startIpId > 0  && endIpId > 0)
                            {
                                int count = 0 ;

                                for(; startIpId <= endIpId ; startIpId++)
                                {
                                    TraceOrgSubnetIpDetails traceOrgSubnetIpDetails = (TraceOrgSubnetIpDetails)this.traceOrgService.getById(TraceOrgCommonConstants.TRACE_ORG_SUBNET_IP_DETAILS,startIpId);

                                    if(traceOrgSubnetIpDetails.isDeactiveStatus())
                                    {
                                        traceOrgSubnetIpDetails.setDeactiveStatus(TraceOrgCommonConstants.FALSE);

                                        traceOrgSubnetIpDetails.setModifiedDate(new Date());

                                        this.traceOrgService.insert(traceOrgSubnetIpDetails);
                                        count++;
                                    }
                                }

                                List<TraceOrgSubnetIpDetails> totalSubnetIpDetailsList = (List<TraceOrgSubnetIpDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_SUBNET_IP_DETAILS +" where subnetId = '"+traceOrgSubnetDetail.getId()+"' and  deactiveStatus = false");

                                List<TraceOrgSubnetIpDetails> availableSubnetIpDetailsList = (List<TraceOrgSubnetIpDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.SUBNET_IP_DETAILS_BY_STATUS_AND_SUBNET_ID.replace(TraceOrgCommonConstants.STATUS_VALUE,TraceOrgCommonConstants.AVAILABLE).replace(TraceOrgCommonConstants.SUBNET_ID_VALUE, TraceOrgCommonUtil.getStringValue(traceOrgSubnetDetail.getId()))+" and  deactiveStatus = false");

                                List<TraceOrgSubnetIpDetails> usedSubnetIpDetailsList = (List<TraceOrgSubnetIpDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.SUBNET_IP_DETAILS_BY_STATUS_AND_SUBNET_ID.replace(TraceOrgCommonConstants.STATUS_VALUE,TraceOrgCommonConstants.USED).replace(TraceOrgCommonConstants.SUBNET_ID_VALUE,TraceOrgCommonUtil.getStringValue(traceOrgSubnetDetail.getId())) +" and  deactiveStatus = false");

                                List<TraceOrgSubnetIpDetails> transientSubnetIpDetailsList = (List<TraceOrgSubnetIpDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.SUBNET_IP_DETAILS_BY_STATUS_AND_SUBNET_ID.replace(TraceOrgCommonConstants.STATUS_VALUE,TraceOrgCommonConstants.TRANSIENT).replace(TraceOrgCommonConstants.SUBNET_ID_VALUE,TraceOrgCommonUtil.getStringValue(traceOrgSubnetDetail.getId())) +" and  deactiveStatus = false");

                                traceOrgSubnetDetail.setAvailableIp((long) availableSubnetIpDetailsList.size());

                                traceOrgSubnetDetail.setUsedIp((long) usedSubnetIpDetailsList.size());

                                traceOrgSubnetDetail.setTransientIp((long) transientSubnetIpDetailsList.size());

                                traceOrgSubnetDetail.setTotalIp((long)totalSubnetIpDetailsList.size());

                                traceOrgSubnetDetail.setLastScanTime(new Date());

                                traceOrgService.insert(traceOrgSubnetDetail);

                                response.setSuccess(TraceOrgCommonConstants.TRUE);

                                response.setMessage(TraceOrgMessageConstants.SUBNET_IP_ADD_SUCCESS);
                            }
                            else
                            {
                                response.setSuccess(TraceOrgCommonConstants.FALSE);

                                response.setMessage(TraceOrgMessageConstants.ENTER_VALID_DETAILS);
                            }
                        }
                        else
                        {
                            response.setSuccess(TraceOrgCommonConstants.FALSE);

                            response.setMessage(TraceOrgMessageConstants.ENTER_VALID_DETAILS);
                        }

                    }
                    else
                    {
                        response.setSuccess(TraceOrgCommonConstants.FALSE);

                        response.setMessage(TraceOrgMessageConstants.ENTER_VALID_DETAILS);
                    }
                }
                else
                {
                    response.setSuccess(TraceOrgCommonConstants.FALSE);

                    response.setMessage(TraceOrgMessageConstants.ENTER_VALID_DETAILS);
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


    /**
     * IPAM-128 Implementation | NTPC | New Requirement
     * Change log is added when subnet ip range is updated
     * IPAM-149 : IPAM Roadmap : System should have alert notification module to configure different kind of alert notification
     * Added code for IP_STATE_CHANGE & IP_RESERVATION_CHANGE & IP_UTILIZATION & IP_UTILIZATION_BELOW Alert
     * */
    @RequestMapping(value = TraceOrgCommonConstants.UPDATE_SUBNET_IP_RANGE_REST_URL, method = RequestMethod.POST)
    @PreAuthorize("hasAuthority('PERM_DASHBOARD_WRITE')")
    public ResponseEntity<?> updateSubnetIpRange(HttpServletRequest request, @RequestParam String startIp , @RequestParam String endIp, @RequestParam String status, @RequestParam Long subnetId)
    {
        Response response = new Response();

        if(status != null && !status.isEmpty() && startIp != null && !startIp.isEmpty() && endIp != null && !endIp.isEmpty() && subnetId !=null && (status.equalsIgnoreCase("USED") || status.equalsIgnoreCase("Available") || status.equalsIgnoreCase("Reserved") || status.equalsIgnoreCase("Transient")))
        {
            try
            {
                String accessToken = request.getHeader(TraceOrgCommonConstants.ACCESSTOKEN);

                TraceOrgSubnetDetails traceOrgSubnetDetail = (TraceOrgSubnetDetails)this.traceOrgService.getById(TraceOrgCommonConstants.TRACE_ORG_SUBNET_DETAILS,subnetId);

                if(traceOrgSubnetDetail != null)
                {
                    if( this.traceOrgCommonUtil.isValidIp(traceOrgSubnetDetail,startIp) &&  this.traceOrgCommonUtil.isValidIp(traceOrgSubnetDetail,endIp))
                    {

                        if(Long.parseLong(startIp.substring(startIp.lastIndexOf(".")+ 1,startIp.length())) < Long.parseLong(endIp.substring(endIp.lastIndexOf(".")+1,endIp.length())))
                        {
                            Long startIpId = 0L;

                            Long endIpId = 0L;

                            List<TraceOrgSubnetIpDetails> traceOrgSubnetIpDetailsStartList = (List<TraceOrgSubnetIpDetails>)this.traceOrgService.commonQuery("",TraceOrgCommonConstants.SUBNET_IP_DETAILS_BY_IP_ADDRESS.replace(TraceOrgCommonConstants.IP_ADDRESS_VALUE,startIp)+ " and subnetId = '"+subnetId+"'");

                            if(traceOrgSubnetIpDetailsStartList != null && !traceOrgSubnetIpDetailsStartList.isEmpty())
                            {
                                startIpId = traceOrgSubnetIpDetailsStartList.get(0).getId();

                            }
                            List<TraceOrgSubnetIpDetails> traceOrgSubnetIpDetailsEndList = (List<TraceOrgSubnetIpDetails>)this.traceOrgService.commonQuery("",TraceOrgCommonConstants.SUBNET_IP_DETAILS_BY_IP_ADDRESS.replace(TraceOrgCommonConstants.IP_ADDRESS_VALUE,endIp) + " and subnetId = '"+subnetId+"'");

                            if(traceOrgSubnetIpDetailsEndList !=null && !traceOrgSubnetIpDetailsEndList.isEmpty())
                            {
                                endIpId = traceOrgSubnetIpDetailsEndList.get(0).getId();
                            }

                            if(startIpId > 0  && endIpId > 0)
                            {
                                List<TraceOrgIPChangeLog> ipChangeLogs = new ArrayList<>();

                                for(; startIpId <= endIpId ; startIpId++)
                                {
                                    TraceOrgSubnetIpDetails traceOrgSubnetIpDetails = (TraceOrgSubnetIpDetails)this.traceOrgService.getById(TraceOrgCommonConstants.TRACE_ORG_SUBNET_IP_DETAILS,startIpId);

                                    if (!traceOrgSubnetIpDetails.getStatus().equalsIgnoreCase(status))
                                    {
                                        TraceOrgIPChangeLog traceOrgIPChangeLog = new TraceOrgIPChangeLog(
                                                traceOrgCommonUtil.currentUser(accessToken).getUserName(),
                                                traceOrgSubnetIpDetails.getId(),
                                                traceOrgSubnetIpDetails.getSubnetId().getId(),
                                                traceOrgSubnetIpDetails.getIpAddress(),
                                                new Date(),
                                                TraceOrgCommonConstants.CHANGE_LOG_MESSAGE.replace(TraceOrgCommonConstants.PREVIOUS_STATUS,traceOrgSubnetIpDetails.getStatus()).replace(TraceOrgCommonConstants.CURRENT_STATUS, status)
                                        );

                                        ipChangeLogs.add(traceOrgIPChangeLog);

                                        HashMap<String, Object> context = new HashMap<>();

                                        context.put("previousStatus", traceOrgSubnetIpDetails.getStatus());

                                        context.put("currentStatus", status);

                                        context.put("ipAddress", traceOrgSubnetIpDetails.getIpAddress());

                                        context.put("subnetAddress", traceOrgSubnetDetail.getSubnetAddress());

                                        traceOrgCommonUtil.sendMessage(TraceOrgCommonConstants.ALERT_QUEUE, context,
                                                TraceOrgCommonConstants.IP_STATE_CHANGE
                                                        + TraceOrgCommonConstants.VALUE_SEPARATOR
                                                        + TraceOrgCommonConstants.IP_RESERVATION_CHANGE);

                                    }
                                    switch(status.toUpperCase())
                                    {
                                        case "USED" :
                                            traceOrgSubnetIpDetails.setStatus(TraceOrgCommonConstants.USED);
                                            break;
                                        case "TRANSIENT" :
                                            traceOrgSubnetIpDetails.setStatus(TraceOrgCommonConstants.TRANSIENT);
                                            break;
                                        case "AVAILABLE" :
                                            traceOrgSubnetIpDetails.setStatus(TraceOrgCommonConstants.AVAILABLE);
                                            break;
                                        case "RESERVED" :
                                            traceOrgSubnetIpDetails.setStatus(TraceOrgCommonConstants.RESERVED);
                                            break;
                                        default:
                                            traceOrgSubnetIpDetails.setStatus(null);
                                            break;
                                    }
                                    if(traceOrgSubnetIpDetails.getStatus() !=null)
                                    {
                                        traceOrgSubnetIpDetails.setModifiedDate(new Date());

                                        this.traceOrgService.insert(traceOrgSubnetIpDetails);
                                    }
                                }

                                List<TraceOrgSubnetIpDetails> totalSubnetIpDetailsList = (List<TraceOrgSubnetIpDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_SUBNET_IP_DETAILS +" where subnetId = '"+traceOrgSubnetDetail.getId()+"' and  deactiveStatus = false");

                                List<TraceOrgSubnetIpDetails> availableSubnetIpDetailsList = (List<TraceOrgSubnetIpDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.SUBNET_IP_DETAILS_BY_STATUS_AND_SUBNET_ID.replace(TraceOrgCommonConstants.STATUS_VALUE,TraceOrgCommonConstants.AVAILABLE).replace(TraceOrgCommonConstants.SUBNET_ID_VALUE, TraceOrgCommonUtil.getStringValue(traceOrgSubnetDetail.getId()))+" and deactiveStatus = false");

                                List<TraceOrgSubnetIpDetails> usedSubnetIpDetailsList = (List<TraceOrgSubnetIpDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.SUBNET_IP_DETAILS_BY_STATUS_AND_SUBNET_ID.replace(TraceOrgCommonConstants.STATUS_VALUE,TraceOrgCommonConstants.USED).replace(TraceOrgCommonConstants.SUBNET_ID_VALUE,TraceOrgCommonUtil.getStringValue(traceOrgSubnetDetail.getId())) +" and deactiveStatus = false");

                                List<TraceOrgSubnetIpDetails> transientSubnetIpDetailsList = (List<TraceOrgSubnetIpDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.SUBNET_IP_DETAILS_BY_STATUS_AND_SUBNET_ID.replace(TraceOrgCommonConstants.STATUS_VALUE,TraceOrgCommonConstants.TRANSIENT).replace(TraceOrgCommonConstants.SUBNET_ID_VALUE,TraceOrgCommonUtil.getStringValue(traceOrgSubnetDetail.getId())) +" and deactiveStatus = false");

                                traceOrgSubnetDetail.setAvailableIp((long) availableSubnetIpDetailsList.size());

                                traceOrgSubnetDetail.setUsedIp((long) usedSubnetIpDetailsList.size());

                                traceOrgSubnetDetail.setTransientIp((long) transientSubnetIpDetailsList.size());

                                traceOrgSubnetDetail.setTotalIp((long)totalSubnetIpDetailsList.size());

                                traceOrgService.insert(traceOrgSubnetDetail);

                                traceOrgService.insertAll(ipChangeLogs);

                                String workType = TraceOrgCommonConstants.IP_UTILIZATION
                                        + TraceOrgCommonConstants.VALUE_SEPARATOR
                                        + TraceOrgCommonConstants.IP_UTILIZATION_BELOW;

                                HashMap<String, Object> context = new HashMap<>();

                                context.put("subnetAddress", traceOrgSubnetDetail.getSubnetAddress());

                                context.put("subnetId", traceOrgSubnetDetail.getId());

                                context.put("usedIpPercentage", traceOrgSubnetDetail.getUsedIpPercentage());

                                traceOrgCommonUtil.sendMessage(TraceOrgCommonConstants.ALERT_QUEUE, context, workType);

                                response.setSuccess(TraceOrgCommonConstants.TRUE);

                                response.setMessage(TraceOrgMessageConstants.SUBNET_IP_UPDATE_SUCCESS);
                            }
                            else
                            {
                                response.setSuccess(TraceOrgCommonConstants.FALSE);

                                response.setMessage(TraceOrgMessageConstants.ENTER_VALID_DETAILS);
                            }
                        }
                        else
                        {
                            response.setSuccess(TraceOrgCommonConstants.FALSE);

                            response.setMessage(TraceOrgMessageConstants.ENTER_VALID_DETAILS);
                        }

                    }
                    else
                    {
                        response.setSuccess(TraceOrgCommonConstants.FALSE);

                        response.setMessage(TraceOrgMessageConstants.ENTER_VALID_DETAILS);
                    }

                }
                else
                {
                    response.setSuccess(TraceOrgCommonConstants.FALSE);

                    response.setMessage(TraceOrgMessageConstants.ENTER_VALID_DETAILS);
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


    @RequestMapping(value = TraceOrgCommonConstants.DELETE_SUBNET_IP_RANGE_REST_URL, method = RequestMethod.POST)
    @PreAuthorize("hasAuthority('PERM_DASHBOARD_WRITE')")
    public ResponseEntity<?> deleteSubnetIpRange(HttpServletRequest request, @RequestParam String startIp , @RequestParam String endIp, @RequestParam Long subnetId)
    {
        Response response = new Response();

        if( startIp != null && !startIp.isEmpty() && endIp != null && !endIp.isEmpty() && subnetId !=null )
        {
            try
            {
                String accessToken = request.getHeader(TraceOrgCommonConstants.ACCESSTOKEN);

                TraceOrgSubnetDetails traceOrgSubnetDetail = (TraceOrgSubnetDetails)this.traceOrgService.getById(TraceOrgCommonConstants.TRACE_ORG_SUBNET_DETAILS,subnetId);

                if(traceOrgSubnetDetail != null)
                {
                    if( this.traceOrgCommonUtil.isValidIp(traceOrgSubnetDetail,startIp) &&  this.traceOrgCommonUtil.isValidIp(traceOrgSubnetDetail,endIp))
                    {

                        if(Long.parseLong(startIp.substring(startIp.lastIndexOf(".")+ 1,startIp.length())) < Long.parseLong(endIp.substring(endIp.lastIndexOf(".")+1,endIp.length())))
                        {
                            Long startIpId = 0L;

                            Long endIpId = 0L;

                            List<TraceOrgSubnetIpDetails> traceOrgSubnetIpDetailsStartList = (List<TraceOrgSubnetIpDetails>)this.traceOrgService.commonQuery(TraceOrgCommonConstants.SUBNET_IP_DETAILS_BY_IP_ADDRESS.replace(TraceOrgCommonConstants.IP_ADDRESS_VALUE,startIp)+ " and subnetId = '"+subnetId+"'");

                            if(traceOrgSubnetIpDetailsStartList != null && !traceOrgSubnetIpDetailsStartList.isEmpty())
                            {
                                startIpId = traceOrgSubnetIpDetailsStartList.get(0).getId();

                            }
                            List<TraceOrgSubnetIpDetails> traceOrgSubnetIpDetailsEndList = (List<TraceOrgSubnetIpDetails>)this.traceOrgService.commonQuery(TraceOrgCommonConstants.SUBNET_IP_DETAILS_BY_IP_ADDRESS.replace(TraceOrgCommonConstants.IP_ADDRESS_VALUE,endIp)+ " and subnetId = '"+subnetId+"'");

                            if(traceOrgSubnetIpDetailsEndList !=null && !traceOrgSubnetIpDetailsEndList.isEmpty())
                            {
                                endIpId = traceOrgSubnetIpDetailsEndList.get(0).getId();
                            }

                            if(startIpId > 0  && endIpId > 0)
                            {
                                for(; startIpId <= endIpId ; startIpId++)
                                {
                                    TraceOrgSubnetIpDetails traceOrgSubnetIpDetails = (TraceOrgSubnetIpDetails)this.traceOrgService.getById(TraceOrgCommonConstants.TRACE_ORG_SUBNET_IP_DETAILS,startIpId);

                                    traceOrgSubnetIpDetails.setDeactiveStatus(TraceOrgCommonConstants.TRUE);

                                    traceOrgSubnetIpDetails.setModifiedDate(new Date());

                                    this.traceOrgService.insert(traceOrgSubnetIpDetails);
                                }

                                response.setSuccess(TraceOrgCommonConstants.TRUE);

                                response.setMessage(TraceOrgMessageConstants.SUBNET_IP_DELETE_SUCCESS);
                            }
                            else
                            {
                                response.setSuccess(TraceOrgCommonConstants.FALSE);

                                response.setMessage(TraceOrgMessageConstants.ENTER_VALID_DETAILS);
                            }
                        }
                        else
                        {
                            response.setSuccess(TraceOrgCommonConstants.FALSE);

                            response.setMessage(TraceOrgMessageConstants.ENTER_VALID_DETAILS);
                        }

                    }
                    else
                    {
                        response.setSuccess(TraceOrgCommonConstants.FALSE);

                        response.setMessage(TraceOrgMessageConstants.ENTER_VALID_DETAILS);
                    }

                    List<TraceOrgSubnetIpDetails> totalSubnetIpDetailsList = (List<TraceOrgSubnetIpDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_SUBNET_IP_DETAILS +" where subnetId = '"+traceOrgSubnetDetail.getId()+"' and  deactiveStatus = false");

                    List<TraceOrgSubnetIpDetails> availableSubnetIpDetailsList = (List<TraceOrgSubnetIpDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.SUBNET_IP_DETAILS_BY_STATUS_AND_SUBNET_ID.replace(TraceOrgCommonConstants.STATUS_VALUE,TraceOrgCommonConstants.AVAILABLE).replace(TraceOrgCommonConstants.SUBNET_ID_VALUE, TraceOrgCommonUtil.getStringValue(traceOrgSubnetDetail.getId()))+" and  deactiveStatus = false");

                    List<TraceOrgSubnetIpDetails> usedSubnetIpDetailsList = (List<TraceOrgSubnetIpDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.SUBNET_IP_DETAILS_BY_STATUS_AND_SUBNET_ID.replace(TraceOrgCommonConstants.STATUS_VALUE,TraceOrgCommonConstants.USED).replace(TraceOrgCommonConstants.SUBNET_ID_VALUE,TraceOrgCommonUtil.getStringValue(traceOrgSubnetDetail.getId())) +" and  deactiveStatus = false");

                    List<TraceOrgSubnetIpDetails> transientSubnetIpDetailsList = (List<TraceOrgSubnetIpDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.SUBNET_IP_DETAILS_BY_STATUS_AND_SUBNET_ID.replace(TraceOrgCommonConstants.STATUS_VALUE,TraceOrgCommonConstants.TRANSIENT).replace(TraceOrgCommonConstants.SUBNET_ID_VALUE,TraceOrgCommonUtil.getStringValue(traceOrgSubnetDetail.getId())) +" and  deactiveStatus = false");

                    traceOrgSubnetDetail.setAvailableIp((long) availableSubnetIpDetailsList.size());

                    traceOrgSubnetDetail.setUsedIp((long) usedSubnetIpDetailsList.size());

                    traceOrgSubnetDetail.setTransientIp((long) transientSubnetIpDetailsList.size());

                    traceOrgSubnetDetail.setTotalIp((long)totalSubnetIpDetailsList.size());

                    traceOrgService.insert(traceOrgSubnetDetail);
                }
                else
                {
                    response.setSuccess(TraceOrgCommonConstants.FALSE);

                    response.setMessage(TraceOrgMessageConstants.ENTER_VALID_DETAILS);
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

    /**
     * IPAM-145 : System should have rogue device detection capability
     * Added Trusted ip count in the home page.
     * @param request
     * @return
     */
    @RequestMapping(value = TraceOrgCommonConstants.ROGUE_SUBNET_IP_REST_URL, method = RequestMethod.GET)
    @PreAuthorize("hasAuthority('PERM_DASHBOARD_READ')")
    public ResponseEntity<?> rogueIpSummary(HttpServletRequest request)
    {
        Response response = new Response();

        try
        {
            String accessToken = request.getHeader(TraceOrgCommonConstants.ACCESSTOKEN);

            List<TraceOrgSubnetDetails> traceOrgSubnetDetailsList = (List<TraceOrgSubnetDetails>) this.traceOrgService.commonQuery("",TraceOrgCommonConstants.TRACE_ORG_SUBNET_DETAILS);

            List<TraceOrgSubnetIpDetails> subnetIpDetailsList = (List<TraceOrgSubnetIpDetails>) this.traceOrgService.commonQuery("",TraceOrgCommonConstants.TRACE_ORG_SUBNET_IP_DETAILS + " where authenticity = 'rogue' and deactiveStatus = false");

            List<TraceOrgSubnetIpDetails> subnetDiscoveredIpDetailsList = (List<TraceOrgSubnetIpDetails>) this.traceOrgService.commonQuery("",TraceOrgCommonConstants.TRACE_ORG_SUBNET_IP_DETAILS + " where authenticity = 'discovered' and deactiveStatus = false");

            List<TraceOrgSubnetIpDetails> subnetTrustedIpDetailsList = (List<TraceOrgSubnetIpDetails>) this.traceOrgService.commonQuery("",TraceOrgCommonConstants.TRACE_ORG_SUBNET_IP_DETAILS + " where authenticity = 'trusted' and deactiveStatus = false");

            Long rogueSubnetIpCount = 0L;

            if(subnetIpDetailsList!=null && !traceOrgSubnetDetailsList.isEmpty() && traceOrgSubnetDetailsList != null)
            {
                HashMap<String,String> rogueIpSummary = new HashMap<>();

                rogueIpSummary.put("totalIp",""+subnetDiscoveredIpDetailsList.size());

                rogueIpSummary.put("rogueIp",""+subnetIpDetailsList.size());

                rogueIpSummary.put("trustedIp", ""+subnetTrustedIpDetailsList.size());

                response.setData(rogueIpSummary);

                response.setSuccess(TraceOrgCommonConstants.TRUE);
            }
            else
            {
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

    @RequestMapping(value = TraceOrgCommonConstants.EXPORT_CSV_SUBNET_IP_REST_URL+"{id}", method = RequestMethod.GET)
    @PreAuthorize("hasAuthority('PERM_DASHBOARD_READ')")
    public ResponseEntity<?> exportCsvSubnetIp(@PathVariable(TraceOrgCommonConstants.ID) String id, HttpServletRequest request)
    {
        Response response = new Response();

        if(id != null)
        {
            try
            {
                String accessToken = request.getHeader(TraceOrgCommonConstants.ACCESSTOKEN);

                String[] subnetIpIdString = id.split(",");

                if(this.traceOrgService.isExist(TraceOrgCommonConstants.TRACE_ORG_SUBNET_DETAILS,"id",subnetIpIdString[0]))
                {
                    List<TraceOrgSubnetIpDetails> subnetIpDetailsList = this.traceOrgCommonUtil.getSubnetIpDetailsList(subnetIpIdString);

                    if (subnetIpDetailsList != null && !subnetIpDetailsList.isEmpty())
                    {
                        String url = traceOrgCommonUtil.exportSubnetIpCsv(request,subnetIpDetailsList);

                        response.setSuccess(TraceOrgCommonConstants.TRUE);

                        response.setData(url);
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

                    response.setMessage(TraceOrgMessageConstants.ENTER_VALID_DETAILS);
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

    /**
     * IPAM-145 : System should have rogue device detection capability
     * Added column authenticity in export subnet ip.
     * @param id
     * @param request
     * @return
     */
    @RequestMapping(value = TraceOrgCommonConstants.EXPORT_PDF_SUBNET_IP_REST_URL+"{id}", method = RequestMethod.GET)
    @PreAuthorize("hasAuthority('PERM_DASHBOARD_READ')")
    public ResponseEntity<?> exportPdfSubnetIp(@PathVariable(TraceOrgCommonConstants.ID) String id, HttpServletRequest request)
    {
        Response response = new Response();

        if(id != null)
        {
            try
            {
                String accessToken = request.getHeader(TraceOrgCommonConstants.ACCESSTOKEN);

                String[] subnetIpIdString = id.split(",");

                if(this.traceOrgService.isExist(TraceOrgCommonConstants.TRACE_ORG_SUBNET_DETAILS,"id",subnetIpIdString[0]))
                {
                    List<TraceOrgSubnetIpDetails> subnetIpDetailsList = this.traceOrgCommonUtil.getSubnetIpDetailsList(subnetIpIdString);

                    if (subnetIpDetailsList != null && !subnetIpDetailsList.isEmpty())
                    {
                        try
                        {
                            LinkedHashSet<String> columns = new LinkedHashSet<String>()
                            {{
                                add("IP Address");

                                add("Status");

                                add("Scope");

                                add("Mac Address");

                                add("Vendor");

                                add("IP To DNS");

                                add("DNS To IP");

                                add("Authenticity");

                                add("Last Alive Time");

                            }};

                            List<TraceOrgCustomColumn> customColumnList = traceOrgCustomColumnRepository.findByColumnAt("subnetIp");

                            for(TraceOrgCustomColumn column : customColumnList)
                            {
                                columns.add(column.getColumnName());
                            }

                            List<HashMap<String, Object>> ipSummaryObject = new ArrayList<>();

                            Integer availableIp = 0;

                            Integer usedIp = 0;

                            Integer transientIp = 0;

                            List<Object> pdfResults = new ArrayList<>();

                            List<Object> pdfResult;

                            String subnetAddress = null;

                            for (TraceOrgSubnetIpDetails traceOrgSubnetIpDetail : subnetIpDetailsList)
                            {
                                JsonNode customColumnNode=traceOrgSubnetIpDetail.getCustomColumns();

                                if(traceOrgSubnetIpDetail.getStatus().equalsIgnoreCase(TraceOrgCommonConstants.AVAILABLE))
                                {
                                    availableIp++;
                                }
                                else if(traceOrgSubnetIpDetail.getStatus().equalsIgnoreCase(TraceOrgCommonConstants.USED))
                                {
                                    usedIp++;
                                }
                                else if(traceOrgSubnetIpDetail.getStatus().equalsIgnoreCase(TraceOrgCommonConstants.TRANSIENT))
                                {
                                    transientIp++;
                                }

                                pdfResult = new ArrayList<>();

                                subnetAddress = traceOrgSubnetIpDetail.getSubnetId().getSubnetAddress();

                                pdfResult.add(traceOrgSubnetIpDetail.getIpAddress());

                                pdfResult.add(traceOrgSubnetIpDetail.getStatus());

                                pdfResult.add(traceOrgSubnetIpDetail.getSubnetId().getSubnetName());

                                pdfResult.add(traceOrgSubnetIpDetail.getMacAddress());

                                pdfResult.add(traceOrgSubnetIpDetail.getDeviceType());

                                pdfResult.add(traceOrgSubnetIpDetail.getIpToDns());

                                pdfResult.add(traceOrgSubnetIpDetail.getDnsToIp());

                                pdfResult.add(traceOrgSubnetIpDetail.getAuthenticity());

                                pdfResult.add(traceOrgSubnetIpDetail.getLastAliveTime());

                                Iterator<String> fieldNames = customColumnNode.fieldNames();

                                while (fieldNames.hasNext()) {
                                    String key = fieldNames.next();
                                    pdfResult.add(customColumnNode.get(key).asText());
                                }

                                pdfResults.add(pdfResult);
                            }

                            HashMap<String, Object> results = new HashMap<>();

                            HashMap<String, Object> availableIpSummary = new HashMap<>();

                            availableIpSummary.put("status","Available (%)");

                            availableIpSummary.put("value",new DecimalFormat("#.00").format((double)(availableIp*100)/subnetIpDetailsList.size()));

                            HashMap<String, Object> usedIpSummary = new HashMap<>();

                            usedIpSummary.put("status","Used (%)");

                            usedIpSummary.put("value",new DecimalFormat("#.00").format((double)(usedIp*100)/subnetIpDetailsList.size()));

                            HashMap<String, Object> transientIpSummary = new HashMap<>();

                            transientIpSummary.put("status","Transient (%)");

                            transientIpSummary.put("value",new DecimalFormat("#.00").format((double)(transientIp*100)/subnetIpDetailsList.size()));

                            ipSummaryObject.add(availableIpSummary);

                            ipSummaryObject.add(usedIpSummary);

                            ipSummaryObject.add(transientIpSummary);

                            results.put("ipSummary", ipSummaryObject);

                            results.put("grid-result", pdfResults);

                            results.put("columns", columns);

                            results.put("logFor", "IP_REPORT");

                            List<HashMap<String, Object>> visualizationResults = new ArrayList<>();

                            visualizationResults.add(results);

                            HashMap<String, Object> gridReport = new HashMap<>();

                            gridReport.put("Title", "Subnet IP Details "+TraceOrgCommonConstants.LEFT_SQUARE_BRACKET + subnetAddress + TraceOrgCommonConstants.RIGHT_SQUARE_BRACKET);

                            String fileName = "Subnet IP Details "+TraceOrgCommonConstants.VISUAL_DATE_FORMAT.format(new Date())+".pdf";

                            fileName = fileName.replace(" ","_").replace(":","_").replace(",","");

                            TraceOrgPDFBuilder.addGridReport(1, visualizationResults, new HashMap<String, Object>(), fileName, gridReport);

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
                        response.setSuccess(TraceOrgCommonConstants.FALSE);

                        response.setMessage(TraceOrgMessageConstants.NO_DATA_AVAILABLE);
                    }
                }
                else
                {
                    response.setSuccess(TraceOrgCommonConstants.FALSE);

                    response.setMessage(TraceOrgMessageConstants.ENTER_VALID_DETAILS);
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

    @RequestMapping(value = TraceOrgCommonConstants.EXPORT_PDF_SUBNET_CONFLICT_IP_REST_URL, method = RequestMethod.GET)
    @PreAuthorize("hasAuthority('PERM_DASHBOARD_READ')")
    public ResponseEntity<?> exportPdfSubnetConflictIp(HttpServletRequest request)
    {
        Response response = new Response();

        try
        {
            String accessToken = request.getHeader(TraceOrgCommonConstants.ACCESSTOKEN);

            List<TraceOrgSubnetIpDetails> subnetIpDetailsList = (List<TraceOrgSubnetIpDetails>) this.traceOrgService.commonQuery("",TraceOrgCommonConstants.TRACE_ORG_SUBNET_IP_DETAILS + " where conflictMac is not null and macAddress is not null");

            if (subnetIpDetailsList != null && !subnetIpDetailsList.isEmpty()) {

                try
                {
                    LinkedHashSet<String> columns = new LinkedHashSet<String>()
                    {{
                        add("IP Address");

                        add("Subnet");

                        add("Subnet Category");

                        add("Assigned MAC");

                        add("Conflicting MAC");

                        add("Conflict Time");

                    }};

                    List<Object> pdfResults = new ArrayList<>();

                    List<Object> pdfResult;

                    String subnetAddress = null;

                    for (TraceOrgSubnetIpDetails traceOrgSubnetIpDetail : subnetIpDetailsList) {
                        pdfResult = new ArrayList<>();

                        subnetAddress = traceOrgSubnetIpDetail.getSubnetId().getSubnetAddress();

                        pdfResult.add(traceOrgSubnetIpDetail.getIpAddress());

                        pdfResult.add(traceOrgSubnetIpDetail.getSubnetId().getSubnetName());

                        pdfResult.add(traceOrgSubnetIpDetail.getSubnetId().getTraceOrgCategory().getCategoryName());

                        pdfResult.add(traceOrgSubnetIpDetail.getMacAddress());

                        pdfResult.add(traceOrgSubnetIpDetail.getConflictMac());

                        pdfResult.add(traceOrgSubnetIpDetail.getLastAliveTime());

                        pdfResults.add(pdfResult);
                    }

                    HashMap<String, Object> results = new HashMap<>();

                    results.put("grid-result", pdfResults);

                    results.put("columns", columns);

                    List<HashMap<String, Object>> visualizationResults = new ArrayList<>();

                    visualizationResults.add(results);

                    HashMap<String, Object> gridReport = new HashMap<>();

                    gridReport.put("Title", "Subnet Conflict IP Details");

                    String fileName = "Subnet Conflict IP Details "+TraceOrgCommonConstants.VISUAL_DATE_FORMAT.format(new Date())+".pdf";

                    fileName = fileName.replace(" ","_").replace(":","_").replace(",","");

                    TraceOrgPDFBuilder.addGridReport(1, visualizationResults, new HashMap<String, Object>(), fileName, gridReport);

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
                response.setSuccess(TraceOrgCommonConstants.FALSE);

                response.setMessage(TraceOrgMessageConstants.NO_DATA_AVAILABLE);
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

    @RequestMapping(value = TraceOrgCommonConstants.EXPORT_PDF_RECENT_DISCOVERY_REST_URL, method = RequestMethod.GET)
    @PreAuthorize("hasAuthority('PERM_DASHBOARD_READ')")
    public ResponseEntity<?> exportPdfRecentlyDiscovered(HttpServletRequest request)
    {
        Response response = new Response();

        traceOrgCommonUtil.buildResponse(traceOrgSubnetIpService.exportPdfRecentlyDiscovered(), response);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @RequestMapping(value = TraceOrgCommonConstants.EXPORT_PDF_TOP_10_CATEGORY_UTILIZATION_REST_URL, method = RequestMethod.GET)
    @PreAuthorize("hasAuthority('PERM_DASHBOARD_READ')")
    public ResponseEntity<?> exportPdfTop10CategoryUtilization(HttpServletRequest request)
    {
        Response response = new Response();

        traceOrgCommonUtil.buildResponse(traceOrgSubnetIpService.exportPdfTop10CategoryUtilization(), response);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @RequestMapping(value = TraceOrgCommonConstants.EXPORT_PDF_TOP_10_SUBNET_UTILIZATION_REST_URL, method = RequestMethod.GET)
    @PreAuthorize("hasAuthority('PERM_DASHBOARD_READ')")
    public ResponseEntity<?> exportPdfTop10SubnetUtilization(HttpServletRequest request)
    {
        Response response = new Response();

        traceOrgCommonUtil.buildResponse(traceOrgSubnetIpService.exportPdfTop10SubnetUtilization(), response);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @RequestMapping(value = TraceOrgCommonConstants.CONFLICT_SUBNET_IP_REST_URL, method = RequestMethod.GET)
    @PreAuthorize("hasAuthority('PERM_DASHBOARD_READ')")
    public ResponseEntity<?> listAllConflictSubnetIp(HttpServletRequest request)
    {
        Response response = new Response();

        try
        {
            String accessToken = request.getHeader(TraceOrgCommonConstants.ACCESSTOKEN);

            List<TraceOrgSubnetIpDetails> subnetIpDetailsList = (List<TraceOrgSubnetIpDetails>) this.traceOrgService.commonQuery("",TraceOrgCommonConstants.TRACE_ORG_SUBNET_IP_DETAILS + " where conflictMac is not null and macAddress is not null");

            if(subnetIpDetailsList!=null && !subnetIpDetailsList.isEmpty())
            {
                response.setData(subnetIpDetailsList);

                response.setSuccess(TraceOrgCommonConstants.TRUE);
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
