package com.motadata.traceorg.ipam.controller.subnet;

import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.motadata.traceorg.ipam.logger.TraceOrgLogger;
import com.motadata.traceorg.ipam.entity.*;
import com.motadata.traceorg.ipam.entity.dashboard.TraceOrgCategory;
import com.motadata.traceorg.ipam.entity.event.TraceOrgEvent;
import com.motadata.traceorg.ipam.entity.discovery.TraceOrgGateway;
import com.motadata.traceorg.ipam.entity.subnet.TraceOrgSubnetDetails;
import com.motadata.traceorg.ipam.entity.subnet.TraceOrgSubnetIpDetails;
import com.motadata.traceorg.ipam.repository.rogueDetection.TraceOrgRogueDetectionRepository;
import com.motadata.traceorg.ipam.scheduler.subnet.TraceOrgSubnetControllerExecuteJob;
import com.motadata.traceorg.ipam.services.TraceOrgService;
import com.motadata.traceorg.ipam.services.alert.TraceOrgAlertService;
import com.motadata.traceorg.ipam.services.discovery.TraceOrgDiscoveryService;
import com.motadata.traceorg.ipam.services.subnet.TraceOrgSubnetService;
import com.motadata.traceorg.ipam.services.supernet.TraceOrgSupernetService;
import com.motadata.traceorg.ipam.services.messaging.TraceOrgMessageSender;
import com.motadata.traceorg.ipam.services.settings.TraceOrgAlertConfigureService;
import com.motadata.traceorg.ipam.util.*;
import de.siegmar.fastcsv.reader.CsvContainer;
import de.siegmar.fastcsv.reader.CsvReader;
import de.siegmar.fastcsv.reader.CsvRow;

import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.*;

@SuppressWarnings("ALL")
@RestController
public class TraceOrgSubnetController
{
    @Autowired
    private TraceOrgService traceOrgService;

    @Autowired
    private TraceOrgCommonUtil traceOrgCommonUtil;

    @Autowired
    private TraceOrgAlertService traceOrgAlertService;

    @Autowired
    private TraceOrgSubnetUtil traceOrgSubnetUtil;

    @Autowired
    TraceOrgRogueDetectionRepository traceOrgRogueDetectionRepository;

    @Autowired
    TraceOrgSubnetService traceOrgSubnetService;

    @Autowired
    private TraceOrgSubnetControllerExecuteJob traceOrgSubnetControllerExecuteJob;;

    @Autowired
    private TraceOrgCiscoDHCPServerUtil traceOrgCiscoDHCPServerUtil;

    @Autowired
    private TraceOrgWindowsDhcpServerUtil traceOrgWindowsDhcpServerUtil;

    @Autowired
    private TraceOrgDiscoveryService traceOrgDiscoveryService;

    @Autowired
    private TraceOrgSupernetService traceOrgSupernetService;

    private static final TraceOrgLogger _logger = new TraceOrgLogger(TraceOrgSubnetController.class, "Subnet Controller");

    @RequestMapping(value = TraceOrgCommonConstants.SUBNET_REST_URL, method = RequestMethod.GET)
    @PreAuthorize("hasAuthority('PERM_DASHBOARD_READ')")
    public ResponseEntity<?> listAllSubnet(HttpServletRequest request)
    {
        Response response = new Response();

        try
        {
            String accessToken = request.getHeader(TraceOrgCommonConstants.ACCESSTOKEN);

            List<TraceOrgSubnetDetails> subnetDetailsList = (List<TraceOrgSubnetDetails>) this.traceOrgService.commonQuery("",TraceOrgCommonConstants.TRACE_ORG_SUBNET_DETAILS);

            if(subnetDetailsList !=null && !subnetDetailsList.isEmpty())
            {
                response.setData(subnetDetailsList);

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

    /**
     * IPAM-128 Implementation | NTPC | New Requirement
     * Setting subnet prefix and subnet size at insertion time.
     * IPAM-140 IPAM | Add v3 Support for SNMP in Remote Subnet
     * No need to validate the gateway at the time of subnet insertion
     * IPAM-134 IPAM | Mail Server Configuration issue
     * Added generic sendMail method
     * IPAM-148 : System should have the ability to locate the available subnets inside a Supernet. This is to provide assistance to users when creating subnets inside an aggregated Network.
     * added the insertSubnetInSupernetCategory method call to insert the subnet into supernet
     * IPAM-149 : IPAM Roadmap : System should have alert notification module to configure different kind of alert notification
     * Added code for NEW_SUBNETS_DISCOVERED Alert
     * */
    @RequestMapping(value = TraceOrgCommonConstants.SUBNET_REST_URL, method = RequestMethod.POST)
    @PreAuthorize("hasAuthority('PERM_DASHBOARD_WRITE')")
    public ResponseEntity<?> insertSubnet(HttpServletRequest request, @RequestBody TraceOrgSubnetDetails traceOrgSubnetDetails)
    {
        Response response = new Response();
        try
        {
            String accessToken = request.getHeader(TraceOrgCommonConstants.ACCESSTOKEN);

            if (traceOrgSubnetDetails.getSubnetAddress()!=null && !traceOrgSubnetDetails.getSubnetAddress().trim().isEmpty() && traceOrgSubnetDetails.getSubnetName()!=null && !traceOrgSubnetDetails.getSubnetName().trim().isEmpty() && (traceOrgSubnetDetails.isScheduleStatus() ? (traceOrgSubnetDetails.getDuration()!=null && !traceOrgSubnetDetails.getDuration().isEmpty()
                    && (traceOrgSubnetDetails.getDuration().equalsIgnoreCase("Days") || traceOrgSubnetDetails.getDuration().equalsIgnoreCase("Hours") || traceOrgSubnetDetails.getDuration().equalsIgnoreCase("Month")) && traceOrgSubnetDetails.getScheduleHour() !=null) : true))
            {
                if(this.traceOrgService.isExist(TraceOrgCommonConstants.TRACE_ORG_SUBNET_DETAILS,TraceOrgCommonConstants.SUBNET_ADDRESS,traceOrgSubnetDetails.getSubnetAddress()))
                {
                    _logger.debug(traceOrgSubnetDetails.getSubnetAddress() + " is already exist...");

                    response.setSuccess(TraceOrgCommonConstants.FALSE);

                    response.setMessage(TraceOrgMessageConstants.SUBNET_ADDRESS_ALREADY_EXIST);
                }
                else if(this.traceOrgService.isExist(TraceOrgCommonConstants.TRACE_ORG_SUBNET_DETAILS,"subnetName",traceOrgSubnetDetails.getSubnetName()))
                {
                    _logger.debug(traceOrgSubnetDetails.getSubnetName() + " is already exist...");

                    response.setSuccess(TraceOrgCommonConstants.FALSE);

                    response.setMessage(TraceOrgMessageConstants.SUBNET_NAME_ALREADY_EXIST);
                }
                else
                {
                    TraceOrgCategory traceOrgCategory = (TraceOrgCategory)this.traceOrgService.getById(TraceOrgCommonConstants.TRACE_ORG_CATEGORY,traceOrgSubnetDetails.getCategoryId());

                    if(traceOrgCategory !=null)
                    {
                        if(traceOrgSubnetDetails.getMaskInfo() !=null && !traceOrgSubnetDetails.getMaskInfo().isEmpty())
                        {
                            if(TraceOrgCommonUtil.isIPv6Address(traceOrgSubnetDetails.getSubnetAddress()))
                            {
                                traceOrgSubnetDetails.setSubnetMask(traceOrgSubnetDetails.getMaskInfo().split("/")[1]);
                            }
                            else
                            {
                                traceOrgSubnetDetails.setSubnetMask(traceOrgSubnetDetails.getMaskInfo().split("/")[0]);
                            }

                            traceOrgSubnetDetails.setSubnetCidr(Integer.parseInt(traceOrgSubnetDetails.getMaskInfo().split("/")[1]));
                        }
                        else if(!Strings.isNullOrEmpty(traceOrgSubnetDetails.getSubnetMask()))
                        {
                            traceOrgSubnetDetails.setSubnetCidr(traceOrgDiscoveryService.getSubnetMaskLength(traceOrgSubnetDetails.getSubnetMask()));
                        }

                        if(traceOrgCommonUtil.checkSubnet(traceOrgSubnetDetails.getSubnetAddress(),traceOrgSubnetDetails.getSubnetCidr()))
                        {
                            traceOrgSubnetDetails.setTraceOrgCategory(traceOrgCategory);

                            traceOrgSubnetDetails.setTotalIp(traceOrgCommonUtil.countTotalIp(traceOrgSubnetDetails.getSubnetAddress(),traceOrgSubnetDetails.getSubnetCidr()));

                            traceOrgSubnetDetails.setAvailableIp(traceOrgCommonUtil.countTotalIp(traceOrgSubnetDetails.getSubnetAddress(),traceOrgSubnetDetails.getSubnetCidr()) - 2);

                            traceOrgSubnetDetails.setType("Normal");

                            if(traceOrgSubnetDetails.isScheduleStatus())
                            {
                                switch (traceOrgSubnetDetails.getDuration())
                                {
                                    case "Days" :
                                        traceOrgSubnetDetails.setDuration("Days");
                                        if(traceOrgSubnetDetails.getScheduleHour() > 32)
                                            traceOrgSubnetDetails.setScheduleHour(0);
                                        break;
                                    case "Hours" :
                                        traceOrgSubnetDetails.setDuration("Hours");
                                        if(traceOrgSubnetDetails.getScheduleHour() > 24)
                                            traceOrgSubnetDetails.setScheduleHour(0);
                                        break;
                                    case "Month" :
                                        traceOrgSubnetDetails.setDuration("Month");
                                        if(traceOrgSubnetDetails.getScheduleHour() > 12)
                                            traceOrgSubnetDetails.setScheduleHour(0);
                                        break;
                                }

                                traceOrgSubnetDetails.setScheduleHour(traceOrgSubnetDetails.getScheduleHour());
                            }

                            traceOrgSubnetDetails.setCreatedDate(new Date());

                            traceOrgSubnetDetails.setModifiedDate(new Date());

                            traceOrgSubnetDetails.setCreatedBy(traceOrgCommonUtil.currentUser(accessToken).getUserName());

                            boolean insertStatus = this.traceOrgService.insert(traceOrgSubnetDetails);

                            _logger.debug("subnet "+traceOrgSubnetDetails.getSubnetAddress()+ " database insert status is "+insertStatus);

                            if(insertStatus)
                            {
                                _logger.info("Subnet "+traceOrgSubnetDetails.getSubnetAddress()+" is added");

                                traceOrgSupernetService.insertSubnetInSupernetCategory(traceOrgSubnetDetails.getSubnetAddress(), traceOrgSubnetDetails.getSubnetCidr(), traceOrgSubnetDetails.getId(), traceOrgCommonUtil.currentUser(accessToken),traceOrgCommonUtil.currentUserName(accessToken));

                                if(traceOrgSubnetDetails.isScheduleStatus() && traceOrgSubnetDetails.getScheduleHour() > 0)
                                {
                                    String cronExpression = null;

                                    switch (traceOrgSubnetDetails.getDuration())
                                    {
                                        case "Days" :
                                            cronExpression = "0 0 0 1-31/"+traceOrgSubnetDetails.getScheduleHour()+" * ?";
                                            break;
                                        case "Hours" :
                                            cronExpression = "0 0 1-23/"+traceOrgSubnetDetails.getScheduleHour()+" ? * *";
                                            break;
                                        case "Month" :
                                            cronExpression = "0 0 0 1 1-12/"+traceOrgSubnetDetails.getScheduleHour()+" ?";
                                            break;
                                    }

                                    if(cronExpression!=null)
                                    {
                                        traceOrgCommonUtil.scanSubnetCronJob(cronExpression,traceOrgSubnetDetails,traceOrgService,traceOrgCommonUtil);
                                    }
                                }

                                //EVENT LOG
                                TraceOrgEvent traceOrgEvent =  new TraceOrgEvent();

                                traceOrgEvent.setTimestamp(new Date());

                                traceOrgEvent.setDoneBy(traceOrgCommonUtil.currentUser(accessToken));

                                traceOrgEvent.setEventType("Add Subnet");

                                traceOrgEvent.setEventContext("Subnet "+traceOrgSubnetDetails.getSubnetAddress()+" is added in IP Address Manager by "+traceOrgCommonUtil.currentUserName(accessToken)  );

                                traceOrgEvent.setSeverity(1);

                                this.traceOrgService.insert(traceOrgEvent);

                                boolean success = traceOrgCommonUtil.ipList(traceOrgSubnetDetails);

                                this.traceOrgService.insert(traceOrgSubnetDetails);

                                HashMap<String, Object> context = new HashMap<>();

                                context.put("subnetAddress", traceOrgSubnetDetails.getSubnetAddress());

                                context.put("currentUserName", traceOrgCommonUtil.currentUserName(accessToken));

                                traceOrgCommonUtil.sendMessage(TraceOrgCommonConstants.ALERT_QUEUE, context, TraceOrgCommonConstants.NEW_SUBNETS_DISCOVERED);

                                response.setSuccess(TraceOrgCommonConstants.TRUE);

                                response.setMessage(TraceOrgMessageConstants.SUBNET_ADD_SUCCESS);

                                _logger.debug("Subnet "+traceOrgSubnetDetails.getSubnetAddress() + " is added on "+ new Date());
                            }
                            else
                            {
                                response.setSuccess(TraceOrgCommonConstants.FALSE);

                                response.setMessage(TraceOrgMessageConstants.SOMETHING_WENT_WRONG);
                            }
                        }
                        else
                        {
                            _logger.debug("subnet "+traceOrgSubnetDetails.getSubnetAddress() + " is not valid");

                            response.setSuccess(TraceOrgCommonConstants.FALSE);

                            response.setMessage(TraceOrgMessageConstants.SUBNET_DETAIL_NOT_VALID);
                        }
                    }
                    else
                    {
                        _logger.debug("Category is null or not found for "+traceOrgSubnetDetails.getSubnetAddress());

                        response.setSuccess(TraceOrgCommonConstants.FALSE);

                        response.setMessage(TraceOrgMessageConstants.CATEGORY_ID_NOT_VALID);
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


    /**
     * IPAM-140 IPAM | Add v3 Support for SNMP in Remote Subnet
     * Update the gatewayId instead of the gateway or community.
     * */
    @RequestMapping(value = TraceOrgCommonConstants.SUBNET_REST_URL+"{id}", method = RequestMethod.PUT)
    @PreAuthorize("hasAuthority('PERM_DASHBOARD_WRITE')")
    public ResponseEntity<?> updateSubnet(@PathVariable Long id,HttpServletRequest request, @RequestBody TraceOrgSubnetDetails traceOrgSubnetDetails)
    {
        Response response = new Response();

        try
        {
            String accessToken = request.getHeader(TraceOrgCommonConstants.ACCESSTOKEN);

            if (traceOrgSubnetDetails.getSubnetAddress()!=null && !traceOrgSubnetDetails.getSubnetAddress().trim().isEmpty() && traceOrgSubnetDetails.getSubnetName()!=null && !traceOrgSubnetDetails.getSubnetName().trim().isEmpty() && (traceOrgSubnetDetails.isScheduleStatus() ? (traceOrgSubnetDetails.getDuration()!=null && !traceOrgSubnetDetails.getDuration().isEmpty()
                    && (traceOrgSubnetDetails.getDuration().equalsIgnoreCase("Days") || traceOrgSubnetDetails.getDuration().equalsIgnoreCase("Hours") || traceOrgSubnetDetails.getDuration().equalsIgnoreCase("Month")) && traceOrgSubnetDetails.getScheduleHour() !=null) : true))
            {
                TraceOrgSubnetDetails existedTraceOrgSubnetDetails = (TraceOrgSubnetDetails)this.traceOrgService.getById(TraceOrgCommonConstants.TRACE_ORG_SUBNET_DETAILS,id);

                if(existedTraceOrgSubnetDetails.getSubnetAddress().equalsIgnoreCase(traceOrgSubnetDetails.getSubnetAddress()))
                {
                    List<TraceOrgSubnetDetails> subnetDetailsList =  (List<TraceOrgSubnetDetails>)this.traceOrgService.commonQuery("TraceOrgSubnetDetails where subnetName = '"+traceOrgSubnetDetails.getSubnetName()+"'");

                    if (subnetDetailsList != null && !subnetDetailsList.isEmpty() && subnetDetailsList.size() < 2)
                    {
                        if(Objects.equals(subnetDetailsList.get(0).getId(), id))
                        {
                            TraceOrgCategory traceOrgCategory = (TraceOrgCategory)this.traceOrgService.getById(TraceOrgCommonConstants.TRACE_ORG_CATEGORY,traceOrgSubnetDetails.getCategoryId());

                            if(traceOrgCategory !=null)
                            {

                                traceOrgCommonUtil.removeScanSubnetCron(traceOrgSubnetDetails);

                                existedTraceOrgSubnetDetails.setTraceOrgCategory(traceOrgCategory);

                                //existedTraceOrgSubnetDetails.setSubnetName(traceOrgSubnetDetails.getSubnetName());


                                if (existedTraceOrgSubnetDetails.isLocalSubnet() == false)
                                {
                                    existedTraceOrgSubnetDetails.setGatewayId(traceOrgSubnetDetails.getGatewayId());
                                }


                                existedTraceOrgSubnetDetails.setDnsAddress(traceOrgSubnetDetails.getDnsAddress());

                                existedTraceOrgSubnetDetails.setModifiedDate(new Date());

                                existedTraceOrgSubnetDetails.setAllowIcmp(traceOrgSubnetDetails.isAllowIcmp());

                                existedTraceOrgSubnetDetails.setAllowDns(traceOrgSubnetDetails.isAllowDns());

                                existedTraceOrgSubnetDetails.setScheduleStatus(traceOrgSubnetDetails.isScheduleStatus());

                                existedTraceOrgSubnetDetails.setDescription(traceOrgSubnetDetails.getDescription());

                                existedTraceOrgSubnetDetails.setVlanName(traceOrgSubnetDetails.getVlanName());

                                existedTraceOrgSubnetDetails.setLocation(traceOrgSubnetDetails.getLocation());

                                if(existedTraceOrgSubnetDetails.isScheduleStatus() && traceOrgSubnetDetails.getDuration()!=null && !traceOrgSubnetDetails.getDuration().isEmpty())
                                {
                                    switch (traceOrgSubnetDetails.getDuration())
                                    {
                                        case "Days" :
                                            existedTraceOrgSubnetDetails.setDuration("Days");
                                            if(traceOrgSubnetDetails.getScheduleHour() > 32)
                                                existedTraceOrgSubnetDetails.setScheduleHour(0);
                                            break;
                                        case "Hours" :
                                            existedTraceOrgSubnetDetails.setDuration("Hours");
                                            if(traceOrgSubnetDetails.getScheduleHour() > 24)
                                                existedTraceOrgSubnetDetails.setScheduleHour(0);
                                            break;
                                        case "Month" :
                                            existedTraceOrgSubnetDetails.setDuration("Month");
                                            if(traceOrgSubnetDetails.getScheduleHour() > 12)
                                                existedTraceOrgSubnetDetails.setScheduleHour(0);
                                            break;
                                    }

                                    existedTraceOrgSubnetDetails.setScheduleHour(traceOrgSubnetDetails.getScheduleHour());
                                }
                                else
                                {
                                    existedTraceOrgSubnetDetails.setScheduleHour(null);

                                    existedTraceOrgSubnetDetails.setDuration(null);
                                }

                                boolean insertStatus = this.traceOrgService.insert(existedTraceOrgSubnetDetails);

                                if(insertStatus)
                                {
                                    if(existedTraceOrgSubnetDetails.isScheduleStatus() && existedTraceOrgSubnetDetails.getScheduleHour() > 0)
                                    {
                                        String cronExpression = null;

                                        switch (existedTraceOrgSubnetDetails.getDuration())
                                        {
                                            case "Days" :
                                                cronExpression = "0 0 0 1-31/"+existedTraceOrgSubnetDetails.getScheduleHour()+" * ?";
                                                break;
                                            case "Hours" :
                                                cronExpression = "0 0 1-23/"+existedTraceOrgSubnetDetails.getScheduleHour()+" ? * *";
                                                break;
                                            case "Month" :
                                                cronExpression = "0 0 0 1 1-12/"+existedTraceOrgSubnetDetails.getScheduleHour()+" ?";
                                                break;
                                        }

                                        if(cronExpression!=null)
                                        {
                                            traceOrgCommonUtil.scanSubnetCronJob(cronExpression,existedTraceOrgSubnetDetails,traceOrgService,traceOrgCommonUtil);
                                        }
                                    }
                                    response.setSuccess(TraceOrgCommonConstants.TRUE);

                                    response.setMessage(TraceOrgMessageConstants.SUBNET_UPDATE_SUCCESS);
                                }
                                else
                                {
                                    response.setSuccess(TraceOrgCommonConstants.FALSE);

                                    response.setMessage(TraceOrgMessageConstants.SOMETHING_WENT_WRONG);
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

                            response.setMessage(TraceOrgMessageConstants.SUBNET_NAME_ALREADY_EXIST);
                        }

                    }
                    else
                    {
                        TraceOrgCategory traceOrgCategory = (TraceOrgCategory)this.traceOrgService.getById(TraceOrgCommonConstants.TRACE_ORG_CATEGORY,traceOrgSubnetDetails.getCategoryId());

                        if(traceOrgCategory !=null)
                        {
                            traceOrgCommonUtil.removeScanSubnetCron(traceOrgSubnetDetails);

                            if (existedTraceOrgSubnetDetails.isLocalSubnet() == false)
                            {
                                existedTraceOrgSubnetDetails.setGatewayId(traceOrgSubnetDetails.getGatewayId());
                            }

                            existedTraceOrgSubnetDetails.setTraceOrgCategory(traceOrgCategory);

                            existedTraceOrgSubnetDetails.setSubnetName(traceOrgSubnetDetails.getSubnetName());

                            existedTraceOrgSubnetDetails.setDnsAddress(traceOrgSubnetDetails.getDnsAddress());

                            existedTraceOrgSubnetDetails.setModifiedDate(new Date());

                            existedTraceOrgSubnetDetails.setAllowIcmp(traceOrgSubnetDetails.isAllowIcmp());

                            existedTraceOrgSubnetDetails.setAllowDns(traceOrgSubnetDetails.isAllowDns());

                            existedTraceOrgSubnetDetails.setScheduleStatus(traceOrgSubnetDetails.isScheduleStatus());

                            existedTraceOrgSubnetDetails.setDescription(traceOrgSubnetDetails.getDescription());

                            existedTraceOrgSubnetDetails.setVlanName(traceOrgSubnetDetails.getVlanName());

                            existedTraceOrgSubnetDetails.setLocation(traceOrgSubnetDetails.getLocation());

                            if(existedTraceOrgSubnetDetails.isScheduleStatus() && traceOrgSubnetDetails.getDuration()!=null && !traceOrgSubnetDetails.getDuration().isEmpty())
                            {
                                switch (traceOrgSubnetDetails.getDuration())
                                {
                                    case "Days" :
                                        existedTraceOrgSubnetDetails.setDuration("Days");
                                        if(traceOrgSubnetDetails.getScheduleHour() > 32)
                                            existedTraceOrgSubnetDetails.setScheduleHour(0);
                                        break;
                                    case "Hours" :
                                        traceOrgSubnetDetails.setDuration("Hours");
                                        if(traceOrgSubnetDetails.getScheduleHour() > 24)
                                            existedTraceOrgSubnetDetails.setScheduleHour(0);
                                        break;
                                    case "Month" :
                                        existedTraceOrgSubnetDetails.setDuration("Month");
                                        if(traceOrgSubnetDetails.getScheduleHour() > 12)
                                            existedTraceOrgSubnetDetails.setScheduleHour(0);
                                        break;
                                }

                                existedTraceOrgSubnetDetails.setScheduleHour(traceOrgSubnetDetails.getScheduleHour());
                            }
                            else
                            {
                                existedTraceOrgSubnetDetails.setScheduleHour(null);

                                existedTraceOrgSubnetDetails.setDuration(null);

                            }

                            boolean insertStatus = this.traceOrgService.insert(existedTraceOrgSubnetDetails);

                            if(insertStatus)
                            {

                                if(existedTraceOrgSubnetDetails.isScheduleStatus() && existedTraceOrgSubnetDetails.getScheduleHour() > 0)
                                {
                                    String cronExpression = null;

                                    switch (existedTraceOrgSubnetDetails.getDuration())
                                    {
                                        case "Days" :
                                            cronExpression = "0 0 0 1-31/"+existedTraceOrgSubnetDetails.getScheduleHour()+" * ?";
                                            break;
                                        case "Hours" :
                                            cronExpression = "0 0 1-23/"+existedTraceOrgSubnetDetails.getScheduleHour()+" ? * *";
                                            break;
                                        case "Month" :
                                            cronExpression = "0 0 0 1 1-12/"+existedTraceOrgSubnetDetails.getScheduleHour()+" ?";
                                            break;
                                    }

                                    if(cronExpression!=null)
                                    {
                                        traceOrgCommonUtil.scanSubnetCronJob(cronExpression,existedTraceOrgSubnetDetails,traceOrgService,traceOrgCommonUtil);
                                    }
                                }

                                response.setSuccess(TraceOrgCommonConstants.TRUE);

                                response.setMessage(TraceOrgMessageConstants.SUBNET_UPDATE_SUCCESS);

                            }
                            else
                            {
                                response.setSuccess(TraceOrgCommonConstants.FALSE);

                                response.setMessage(TraceOrgMessageConstants.SOMETHING_WENT_WRONG);

                            }

                        }
                        else
                        {
                            response.setSuccess(TraceOrgCommonConstants.FALSE);

                            response.setMessage(TraceOrgMessageConstants.ENTER_VALID_DETAILS);

                        }
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
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @RequestMapping(value = TraceOrgCommonConstants.SUBNET_REST_URL+"{id}", method = RequestMethod.GET)
    @PreAuthorize("hasAuthority('PERM_DASHBOARD_READ')")
    public ResponseEntity<?> getSubnet(@PathVariable(TraceOrgCommonConstants.ID) Long id,HttpServletRequest request)
    {
        Response response =  new Response();

        if(id !=null)
        {
            try
            {
                String accessToken = request.getHeader(TraceOrgCommonConstants.ACCESSTOKEN);

                TraceOrgSubnetDetails subnetDetails = (TraceOrgSubnetDetails) this.traceOrgService.getById(TraceOrgCommonConstants.TRACE_ORG_SUBNET_DETAILS, id);

                if (subnetDetails != null)
                {
                    subnetDetails.setCategoryId(subnetDetails.getTraceOrgCategory().getId());

                    response.setSuccess(TraceOrgCommonConstants.TRUE);

                    response.setData(subnetDetails);
                }
                else
                {
                    response.setSuccess(TraceOrgCommonConstants.FALSE);

                    response.setMessage(TraceOrgMessageConstants.SUBNET_ID_NOT_VALID);
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
     * We were not able to delete subnet if there are no IPs in the subnet, so removed that code.
     * IPAM-129 RFP - Proactive | GMDC | New Requirement
     * Remove the change log when removing a subnet.
     * IPAM-142 : IPAM | Alert notification for Monitor IP capacity and receive alerts on IP depletion
     * Delete alert data when a subnet is deleted.
     * IPAM-134 IPAM | Mail Server Configuration issue
     * Added generic sendMail method
     * IPAM-148 : System should have the ability to locate the available subnets inside a Supernet. This is to provide assistance to users when creating subnets inside an aggregated Network.
     * added the  removeSubnetFromSupernetDetails method call to remove the subnet from supernet
     * */
    @RequestMapping(value = TraceOrgCommonConstants.SUBNET_REST_URL+"{id}", method = RequestMethod.DELETE)
    @PreAuthorize("hasAuthority('PERM_DASHBOARD_WRITE')")
    public ResponseEntity<?> removeSubnet(@PathVariable(TraceOrgCommonConstants.ID) Long id,HttpServletRequest request)
    {
        Response response = new Response();

        if(id !=null)
        {
            try
            {
                String accessToken = request.getHeader(TraceOrgCommonConstants.ACCESSTOKEN);

                TraceOrgSubnetDetails traceOrgSubnetDetails = (TraceOrgSubnetDetails) this.traceOrgService.getById(TraceOrgCommonConstants.TRACE_ORG_SUBNET_DETAILS,id);

                if (traceOrgSubnetDetails != null)
                {
                    if(TraceOrgCommonUtil.m_scheduleScanSubnet.containsKey(id) || (TraceOrgCommonUtil.m_scanSubnet.get("jobKey")!=null && TraceOrgCommonUtil.m_scanSubnet.get("jobKey").equals(traceOrgSubnetDetails.getSubnetName())))
                    {
                        _logger.debug("subnet "+ traceOrgSubnetDetails.getSubnetAddress()+ " delete failed: scan is running..");

                        response.setSuccess(TraceOrgCommonConstants.FALSE);

                        response.setMessage(TraceOrgMessageConstants.CANT_DELETE_SUBNET_UNDER_SCAN);
                    }
                    else if(TraceOrgCommonUtil.getCSVImportCount() > 0)
                    {
                        _logger.debug("subnet "+ traceOrgSubnetDetails.getSubnetAddress()+ " delete failed: import csv is running..");

                        response.setSuccess(TraceOrgCommonConstants.FALSE);

                        response.setMessage(TraceOrgMessageConstants.IMPORT_RUNNING);
                    }
                    else
                    {
                        boolean deleteIpOfSubnetStatus = this.traceOrgService.delete(TraceOrgCommonConstants.TRACE_ORG_SUBNET_IP_DETAILS,TraceOrgCommonConstants.SUBNET_ID,TraceOrgCommonUtil.getStringValue(id));

                        boolean deleteIPChangeLog = this.traceOrgService.delete(TraceOrgCommonConstants.TRACE_ORG_IP_CHANGE_LOG,TraceOrgCommonConstants.SUBNET_ID,TraceOrgCommonUtil.getStringValue(id));

                        boolean deleteAlert = this.traceOrgService.delete(TraceOrgCommonConstants.TRACE_ORG_ALERT_STREAM,TraceOrgCommonConstants.SUBNET_ID,TraceOrgCommonUtil.getStringValue(id));

                        _logger.debug("subnet "+traceOrgSubnetDetails.getSubnetAddress()+ " delete status is "+deleteIpOfSubnetStatus);

                        _logger.debug("subnet "+traceOrgSubnetDetails.getSubnetAddress()+ " delete change log status is " + deleteIPChangeLog);

                        _logger.debug("subnet "+traceOrgSubnetDetails.getSubnetAddress()+ " delete alert status is " + deleteAlert);

                        //removeCronOFSubnet
                        traceOrgCommonUtil.removeScanSubnetCron(traceOrgSubnetDetails);

                        this.traceOrgService.delete(TraceOrgCommonConstants.TRACE_ORG_SUBNET_DETAILS,TraceOrgCommonConstants.ID,TraceOrgCommonUtil.getStringValue(id));

                        traceOrgSupernetService.removeSubnetFromSupernetDetails(id);

                        response.setSuccess(TraceOrgCommonConstants.TRUE);

                        response.setMessage(TraceOrgMessageConstants.SUBNET_DELETE_SUCCESS);

                        traceOrgCommonUtil.sendMail("Subnet Deleted In IP Address Manager",
                                "Subnet " + traceOrgSubnetDetails.getSubnetAddress() +" Deleted  in IP Address Manager By "+traceOrgCommonUtil.currentUserName(accessToken));

                        //EVENT LOG
                        TraceOrgEvent traceOrgEvent =  new TraceOrgEvent();

                        traceOrgEvent.setTimestamp(new Date());

                        traceOrgEvent.setDoneBy(traceOrgCommonUtil.currentUser(accessToken));

                        traceOrgEvent.setEventType("Delete Subnet");

                        traceOrgEvent.setEventContext("Subnet "+traceOrgSubnetDetails.getSubnetAddress()+" is deleted from IP Address Manager by "+traceOrgCommonUtil.currentUserName(accessToken)  );

                        traceOrgEvent.setSeverity(1);

                        this.traceOrgService.insert(traceOrgEvent);
                    }
                }
                else
                {
                    response.setSuccess(TraceOrgCommonConstants.FALSE);

                    response.setMessage(TraceOrgMessageConstants.SUBNET_ID_NOT_VALID);
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

    //Check Subnet Details
    @RequestMapping(value = TraceOrgCommonConstants.SUBNET_CHECK_REST_URL, method = RequestMethod.POST)
    @PreAuthorize("hasAuthority('PERM_DASHBOARD_WRITE')")
    public ResponseEntity<?> checkSubnetDetails(HttpServletRequest request, @RequestBody TraceOrgSubnetDetails traceOrgSubnetDetails)
    {
        Response response = new Response();

        try
        {
            String accessToken = request.getHeader(TraceOrgCommonConstants.ACCESSTOKEN);

            if (traceOrgSubnetDetails.getSubnetAddress()!=null && !traceOrgSubnetDetails.getSubnetAddress().trim().isEmpty() && traceOrgSubnetDetails.getSubnetName()!=null && !traceOrgSubnetDetails.getSubnetName().trim().isEmpty())
            {
                if(this.traceOrgService.isExist(TraceOrgCommonConstants.TRACE_ORG_SUBNET_DETAILS,TraceOrgCommonConstants.SUBNET_ADDRESS,traceOrgSubnetDetails.getSubnetAddress()))
                {
                    response.setSuccess(TraceOrgCommonConstants.FALSE);

                    response.setMessage(TraceOrgMessageConstants.SUBNET_ADDRESS_ALREADY_EXIST);
                }
                else
                {
                    if(traceOrgSubnetDetails.getMaskInfo() !=null && !traceOrgSubnetDetails.getMaskInfo().trim().isEmpty())
                    {
                        traceOrgSubnetDetails.setSubnetMask(traceOrgSubnetDetails.getMaskInfo().split("/")[0]);

                        traceOrgSubnetDetails.setSubnetCidr(Integer.parseInt(traceOrgSubnetDetails.getMaskInfo().split("/")[1]));
                    }

                    if(traceOrgCommonUtil.checkSubnet(traceOrgSubnetDetails.getSubnetAddress(),traceOrgSubnetDetails.getSubnetCidr()))
                    {
                        response.setSuccess(TraceOrgCommonConstants.TRUE);

                        response.setMessage(TraceOrgMessageConstants.SUBNET_DETAIL_VALID);
                    }
                    else
                    {
                        response.setSuccess(TraceOrgCommonConstants.FALSE);

                        response.setMessage(TraceOrgMessageConstants.SUBNET_DETAIL_NOT_VALID);
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

    @RequestMapping(value = TraceOrgCommonConstants.SUBNET_BY_CATEGORY, method = RequestMethod.GET)
    @PreAuthorize("hasAuthority('PERM_DASHBOARD_READ')")
    public ResponseEntity<?> listAllSubnetByCategory(HttpServletRequest request)
    {
        Response response = new Response();

        try
        {
            String accessToken = request.getHeader(TraceOrgCommonConstants.ACCESSTOKEN);

            List<TraceOrgCategory> traceOrgCategoryList = (List<TraceOrgCategory>) this.traceOrgService.commonQuery("",TraceOrgCommonConstants.TRACE_ORG_CATEGORY);

            if(traceOrgCategoryList!=null && !traceOrgCategoryList.isEmpty())
            {
                List<TraceOrgSubnetDetails> subnetDetailsList = (List<TraceOrgSubnetDetails>) this.traceOrgService.commonQuery("",TraceOrgCommonConstants.TRACE_ORG_SUBNET_DETAILS);

                if (subnetDetailsList != null && !subnetDetailsList.isEmpty())
                {
                    List<Object> subnetByCategory =  new ArrayList<>();

                    for(TraceOrgCategory traceOrgCategory : traceOrgCategoryList)
                    {
                        float totalUsedIpPercentage = 0 ;

                        float totalUsedIp = 0;

                        float totalIp = 0;

                        HashMap<String,Object> categoryDetails = new HashMap<>();

                        List<TraceOrgSubnetDetails> traceOrgSubnetDetailsByCategory = new ArrayList<>();

                        for(TraceOrgSubnetDetails traceOrgSubnetDetails : subnetDetailsList)
                        {
                            if(Objects.equals(traceOrgSubnetDetails.getTraceOrgCategory().getId(), traceOrgCategory.getId()))
                            {
                                traceOrgSubnetDetailsByCategory.add(traceOrgSubnetDetails);

                                totalIp = totalIp + traceOrgSubnetDetails.getTotalIp();

                                totalUsedIp = totalUsedIp + traceOrgSubnetDetails.getUsedIp();

                            }
                        }

                        if(traceOrgSubnetDetailsByCategory !=null && !traceOrgSubnetDetailsByCategory.isEmpty() && totalIp > 0)
                        {
                            totalUsedIpPercentage = (totalUsedIp * 100)  / totalIp ;
                        }

                        categoryDetails.put("subnetAddress",traceOrgCategory.getCategoryName());

                        categoryDetails.put("id",traceOrgCategory.getId());

                        categoryDetails.put("subnets",traceOrgSubnetDetailsByCategory);

                        categoryDetails.put("totalUsedIpPercentage",totalUsedIpPercentage);

                        if(totalUsedIpPercentage < 50)
                        {
                            categoryDetails.put("severity",3);
                        }
                        else if(totalUsedIpPercentage >= 50 && totalUsedIpPercentage <80)
                        {
                            categoryDetails.put("severity",2);
                        }
                        else if(totalUsedIpPercentage >= 80)
                        {
                            categoryDetails.put("severity",1);
                        }

                        subnetByCategory.add(categoryDetails);
                    }

                    response.setData (subnetByCategory);

                    response.setSuccess(TraceOrgCommonConstants.TRUE);

                }
                else
                {
                    List<Object> subnetByCategory =  new ArrayList<>();

                    for(TraceOrgCategory traceOrgCategory : traceOrgCategoryList)
                    {
                        float totalUsedIpPercentage = 0 ;

                        HashMap<String,Object> categoryDetails = new HashMap<>();

                        List<TraceOrgSubnetDetails> traceOrgSubnetDetailsByCategory = new ArrayList<>();

                        categoryDetails.put("subnetAddress",traceOrgCategory.getCategoryName());

                        categoryDetails.put("id",traceOrgCategory.getId());

                        categoryDetails.put("subnets",traceOrgSubnetDetailsByCategory);

                        categoryDetails.put("totalUsedIpPercentage",totalUsedIpPercentage);

                        if(totalUsedIpPercentage < 50)
                        {
                            categoryDetails.put("severity",3);
                        }
                        else if(totalUsedIpPercentage >= 50 && totalUsedIpPercentage <80)
                        {
                            categoryDetails.put("severity",2);
                        }
                        else if(totalUsedIpPercentage >= 80)
                        {
                            categoryDetails.put("severity",1);
                        }
                        subnetByCategory.add(categoryDetails);
                    }

                    response.setData (subnetByCategory);

                    response.setSuccess(TraceOrgCommonConstants.TRUE);

                }
            }
            else
            {
                response.setSuccess(TraceOrgCommonConstants.FALSE);
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

    /**
     * IPAM-145 : System should have rogue device detection capability
     * Added autowired traceOrgRogueDetectionRepository object
     * @param id
     * @param request
     * @return
     *
     * IPAM-174 : IPAM | Duplicate Emails sent on the same day for repeat mode for Report Scheduler
     * added the flag in job context for manual subnet scan
     */
    //Scan Subnet
    @RequestMapping(value = TraceOrgCommonConstants.SUBNET_SCAN_REST_URL+"{id}", method = RequestMethod.GET)
    @PreAuthorize("hasAuthority('PERM_DASHBOARD_WRITE')")
    public ResponseEntity<?> scanSubnet(@PathVariable(TraceOrgCommonConstants.ID) Long id,HttpServletRequest request)
    {
        Response response = new Response();

        if(id !=null)
        {
            try
            {
                String accessToken = request.getHeader(TraceOrgCommonConstants.ACCESSTOKEN);

                TraceOrgSubnetDetails traceOrgSubnetDetails = (TraceOrgSubnetDetails) this.traceOrgService.getById(TraceOrgCommonConstants.TRACE_ORG_SUBNET_DETAILS, id);

                if (traceOrgSubnetDetails != null)
                {
                    if(TraceOrgCommonUtil.getCSVImportCount() > 0)
                    {
                        _logger.debug("subnet "+traceOrgSubnetDetails.getSubnetAddress()+" scan is not started: csv import is running");

                        response.setSuccess(TraceOrgCommonConstants.FALSE);

                        response.setMessage(TraceOrgMessageConstants.IMPORT_RUNNING);
                    }
                    else
                    {
                        if(TraceOrgCommonUtil.getSubnetScanStatus() == 0)
                        {
                            if(TraceOrgCommonUtil. quartzThread != null)
                            {
                                HashMap<String,Object> mapData = new HashMap<>();

                                mapData.put("subnetDetails",traceOrgSubnetDetails);

                                mapData.put(TraceOrgCommonConstants.TRACE_ORG_SERVICE,this.traceOrgService);

                                mapData.put(TraceOrgCommonConstants.TRACE_ORG_COMMON_UTIL,this.traceOrgCommonUtil);

                                mapData.put(TraceOrgCommonConstants.TRACE_ORG_ALERT_SERVICE, this.traceOrgAlertService);

                                mapData.put(TraceOrgCommonConstants.TRACE_ORG_ROGUE_DETECTION_REPOSITORY, this.traceOrgRogueDetectionRepository);

                                mapData.put(TraceOrgCommonConstants.MANUAL_SUBNET_SCAN,true);

                                mapData.put(TraceOrgCommonConstants.USER_NAME, traceOrgCommonUtil.getCurrentUserName());

                                mapData.put("id",traceOrgSubnetDetails.getSubnetAddress());

                                JobKey jobKey = JobKey.jobKey(TraceOrgCommonConstants.SCAN_SUBNET);

                                JobDetail job = JobBuilder.newJob(TraceOrgSubnetControllerExecuteJob.class).withIdentity(jobKey).usingJobData(new JobDataMap(mapData)).storeDurably().build();

                                TraceOrgCommonUtil.quartzThread.addJob(job, true);

                                TraceOrgCommonUtil.quartzThread.triggerJob(jobKey);

                                response.setSuccess(TraceOrgCommonConstants.TRUE);

                                response.setMessage(TraceOrgMessageConstants.SUBNET_SCAN_STARTED);

                                _logger.debug("Scan is started for :"+traceOrgSubnetDetails.getSubnetAddress());
                            }
                        }
                        else
                        {
                            _logger.debug("subnet "+traceOrgSubnetDetails.getSubnetAddress()+" scan is not started: scan is already running");

                            response.setSuccess(TraceOrgCommonConstants.FALSE);

                            response.setMessage(TraceOrgMessageConstants.SUBNET_SCAN_ALREADY_RUNNING);

                            HashMap<String,Object> subnetDetails = new HashMap<>();

                            subnetDetails.put("subnetDetails",traceOrgSubnetDetails);

                            subnetDetails.put(TraceOrgCommonConstants.ACCESSTOKEN,accessToken);

                            subnetDetails.put(TraceOrgCommonConstants.TRACE_ORG_SERVICE,this.traceOrgService);

                            subnetDetails.put(TraceOrgCommonConstants.TRACE_ORG_ALERT_SERVICE, this.traceOrgAlertService);

                            subnetDetails.put(TraceOrgCommonConstants.TRACE_ORG_ROGUE_DETECTION_REPOSITORY, this.traceOrgRogueDetectionRepository);

                            subnetDetails.put(TraceOrgCommonConstants.TRACE_ORG_COMMON_UTIL,this.traceOrgCommonUtil);

                            subnetDetails.put(TraceOrgCommonConstants.SCAN_TYPE,TraceOrgCommonConstants.SUBNET_SCAN);

                            TraceOrgCommonUtil.m_scheduleScanSubnet.put(traceOrgSubnetDetails.getSubnetName(),subnetDetails);
                        }
                    }

                }
                else
                {
                    _logger.debug("subnet "+traceOrgSubnetDetails.getSubnetAddress()+" scan is not started: subnet details is null");

                    response.setSuccess(TraceOrgCommonConstants.FALSE);

                    response.setMessage(TraceOrgMessageConstants.SUBNET_ID_NOT_VALID);
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

    //Ping Summary
    @RequestMapping(value = TraceOrgCommonConstants.PING_IP_SUMMARY_REST_URL, method = RequestMethod.GET)
    @PreAuthorize("hasAuthority('PERM_DASHBOARD_READ')")
    public ResponseEntity<?>  pingIpSummary(HttpServletRequest request)
    {
        Response response = new Response();

        try
        {
            String accessToken = request.getHeader(TraceOrgCommonConstants.ACCESSTOKEN);

            List<TraceOrgSubnetDetails> subnetDetailsList = (List<TraceOrgSubnetDetails>) this.traceOrgService.commonQuery("",TraceOrgCommonConstants.TRACE_ORG_SUBNET_DETAILS);

            if(subnetDetailsList!=null && !subnetDetailsList.isEmpty())
            {
                long totalIp = 0;

                long availabelIp = 0;

                long usedIp = 0;

                long transientIp = 0;

                for(TraceOrgSubnetDetails traceOrgSubnetDetails : subnetDetailsList)
                {
                    if(traceOrgSubnetDetails.isAllowIcmp())
                    {
                        if(traceOrgSubnetDetails.getTotalIp() != null)
                        {
                            totalIp = totalIp + traceOrgSubnetDetails.getTotalIp();
                        }

                        if(traceOrgSubnetDetails.getAvailableIp() != null)
                        {
                            availabelIp = availabelIp + traceOrgSubnetDetails.getAvailableIp();
                        }

                        if(traceOrgSubnetDetails.getUsedIp() != null)
                        {
                            usedIp = usedIp + traceOrgSubnetDetails.getUsedIp();
                        }

                        if(traceOrgSubnetDetails.getTransientIp() != null)
                        {
                            transientIp = transientIp + traceOrgSubnetDetails.getTransientIp();
                        }
                    }
                }

                HashMap<String,Object> ipSummaryDetails =  new HashMap<>();

                ipSummaryDetails.put(TraceOrgCommonConstants.TOTAL_IP,totalIp);

                ipSummaryDetails.put(TraceOrgCommonConstants.USED_IP,usedIp);

                ipSummaryDetails.put(TraceOrgCommonConstants.AVAILABLE_IP,availabelIp);

                ipSummaryDetails.put(TraceOrgCommonConstants.TRANSIENT_IP,transientIp);

                ipSummaryDetails.put(TraceOrgCommonConstants.TRANSIENT_IP_PERCENTAGE,((float)(transientIp * 100 )/totalIp));

                ipSummaryDetails.put(TraceOrgCommonConstants.USED_IP_PERCENTAGE,((float)(usedIp * 100 )/totalIp));

                ipSummaryDetails.put(TraceOrgCommonConstants.AVAILABLE_IP_PERCENTAGE,((float)(availabelIp * 100 )/totalIp));

                response.setData(ipSummaryDetails);

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

            response.setMessage(TraceOrgMessageConstants.SOMETHING_WENT_WRONG);
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    //IP Summary
    @RequestMapping(value = TraceOrgCommonConstants.IP_SUMMARY_REST_URL, method = RequestMethod.GET)
    @PreAuthorize("hasAuthority('PERM_DASHBOARD_READ')")
    public ResponseEntity<?>  ipSummary(HttpServletRequest request)
    {
        Response response = new Response();

        try
        {
            String accessToken = request.getHeader(TraceOrgCommonConstants.ACCESSTOKEN);

            List<TraceOrgSubnetDetails> subnetDetailsList = (List<TraceOrgSubnetDetails>) this.traceOrgService.commonQuery("",TraceOrgCommonConstants.TRACE_ORG_SUBNET_DETAILS);

            if(subnetDetailsList!=null && !subnetDetailsList.isEmpty())
            {
                long totalIp = 0;

                long availabelIp = 0;

                long usedIp = 0;

                long transientIp = 0;

                for(TraceOrgSubnetDetails traceOrgSubnetDetails : subnetDetailsList)
                {

                    if(traceOrgSubnetDetails.getTotalIp() != null)
                    {
                        totalIp = totalIp + traceOrgSubnetDetails.getTotalIp();
                    }

                    if(traceOrgSubnetDetails.getAvailableIp() != null)
                    {
                        availabelIp = availabelIp + traceOrgSubnetDetails.getAvailableIp();
                    }

                    if(traceOrgSubnetDetails.getUsedIp() != null)
                    {
                        usedIp = usedIp + traceOrgSubnetDetails.getUsedIp();
                    }

                    if(traceOrgSubnetDetails.getTransientIp() != null)
                    {
                        transientIp = transientIp + traceOrgSubnetDetails.getTransientIp();
                    }
                }

                HashMap<String,Object> ipSummaryDetails =  new HashMap<>();

                ipSummaryDetails.put(TraceOrgCommonConstants.TOTAL_IP,totalIp);

                ipSummaryDetails.put(TraceOrgCommonConstants.USED_IP,usedIp);

                ipSummaryDetails.put(TraceOrgCommonConstants.AVAILABLE_IP,availabelIp);

                ipSummaryDetails.put(TraceOrgCommonConstants.TRANSIENT_IP,transientIp);

                ipSummaryDetails.put(TraceOrgCommonConstants.TRANSIENT_IP_PERCENTAGE,((float)(transientIp * 100 )/totalIp));

                ipSummaryDetails.put(TraceOrgCommonConstants.USED_IP_PERCENTAGE,((float)(usedIp * 100 )/totalIp));

                ipSummaryDetails.put(TraceOrgCommonConstants.AVAILABLE_IP_PERCENTAGE,((float)(availabelIp * 100 )/totalIp));

                response.setData(ipSummaryDetails);

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

            response.setMessage(TraceOrgMessageConstants.SOMETHING_WENT_WRONG);
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    //IP Summary BY SUBNET ID
    @RequestMapping(value = TraceOrgCommonConstants.IP_SUMMARY_REST_URL+"{id}", method = RequestMethod.GET)
    @PreAuthorize("hasAuthority('PERM_DASHBOARD_READ')")
    public ResponseEntity<?> ipSummaryBySubnet(@PathVariable(TraceOrgCommonConstants.ID) Long id, HttpServletRequest request)
    {
        Response response = new Response();

        if(id !=null)
        {
            try
            {
                String accessToken = request.getHeader(TraceOrgCommonConstants.ACCESSTOKEN);

                TraceOrgSubnetDetails traceOrgSubnetDetails = (TraceOrgSubnetDetails) this.traceOrgService.getById(TraceOrgCommonConstants.TRACE_ORG_SUBNET_DETAILS,id);

                if(traceOrgSubnetDetails!=null)
                {
                    List<TraceOrgSubnetIpDetails> availableSubnetIpDetailsList = (List<TraceOrgSubnetIpDetails>) this.traceOrgService.commonQuery("",TraceOrgCommonConstants.SUBNET_IP_DETAILS_BY_STATUS_AND_SUBNET_ID.replace(TraceOrgCommonConstants.STATUS_VALUE,TraceOrgCommonConstants.AVAILABLE).replace(TraceOrgCommonConstants.SUBNET_ID_VALUE,TraceOrgCommonUtil.getStringValue(traceOrgSubnetDetails.getId())));

                    List<TraceOrgSubnetIpDetails> usedSubnetIpDetailsList = (List<TraceOrgSubnetIpDetails>) this.traceOrgService.commonQuery("",TraceOrgCommonConstants.SUBNET_IP_DETAILS_BY_STATUS_AND_SUBNET_ID.replace(TraceOrgCommonConstants.STATUS_VALUE,TraceOrgCommonConstants.USED).replace(TraceOrgCommonConstants.SUBNET_ID_VALUE,TraceOrgCommonUtil.getStringValue(traceOrgSubnetDetails.getId())));

                    List<TraceOrgSubnetIpDetails> transientSubnetIpDetailsList = (List<TraceOrgSubnetIpDetails>) this.traceOrgService.commonQuery("",TraceOrgCommonConstants.SUBNET_IP_DETAILS_BY_STATUS_AND_SUBNET_ID.replace(TraceOrgCommonConstants.STATUS_VALUE,TraceOrgCommonConstants.TRANSIENT).replace(TraceOrgCommonConstants.SUBNET_ID_VALUE,TraceOrgCommonUtil.getStringValue(traceOrgSubnetDetails.getId())));

                    traceOrgSubnetDetails.setAvailableIp((long) availableSubnetIpDetailsList.size());

                    traceOrgSubnetDetails.setUsedIp((long) usedSubnetIpDetailsList.size());

                    traceOrgSubnetDetails.setTransientIp((long) transientSubnetIpDetailsList.size());

                    this.traceOrgService.insert(traceOrgSubnetDetails);

                    long totalIp = 0;

                    long availabelIp = 0;

                    long usedIp = 0;

                    long transientIp = 0;

                    if(traceOrgSubnetDetails.getTotalIp() != null)
                    {
                        totalIp = totalIp + traceOrgSubnetDetails.getTotalIp();
                    }

                    if(traceOrgSubnetDetails.getAvailableIp() != null)
                    {
                        availabelIp = availabelIp + traceOrgSubnetDetails.getAvailableIp();
                    }

                    if(traceOrgSubnetDetails.getUsedIp() != null)
                    {
                        usedIp = usedIp + traceOrgSubnetDetails.getUsedIp();
                    }

                    if(traceOrgSubnetDetails.getTransientIp() != null)
                    {
                        transientIp = transientIp + traceOrgSubnetDetails.getTransientIp();
                    }

                    HashMap<String,Object> ipSummaryDetails =  new HashMap<>();

                    ipSummaryDetails.put(TraceOrgCommonConstants.TOTAL_IP,totalIp);

                    ipSummaryDetails.put(TraceOrgCommonConstants.USED_IP,usedIp);

                    ipSummaryDetails.put(TraceOrgCommonConstants.AVAILABLE_IP,availabelIp);

                    ipSummaryDetails.put(TraceOrgCommonConstants.TRANSIENT_IP,transientIp);

                    ipSummaryDetails.put(TraceOrgCommonConstants.TRANSIENT_IP_PERCENTAGE,((float)(transientIp * 100 )/totalIp));

                    ipSummaryDetails.put(TraceOrgCommonConstants.USED_IP_PERCENTAGE,((float)(usedIp * 100 )/totalIp));

                    ipSummaryDetails.put(TraceOrgCommonConstants.AVAILABLE_IP_PERCENTAGE,((float)(availabelIp * 100 )/totalIp));

                    response.setData(ipSummaryDetails);

                    response.setSuccess(TraceOrgCommonConstants.TRUE);
                }
                else
                {
                    response.setSuccess(TraceOrgCommonConstants.TRUE);

                    response.setMessage(TraceOrgMessageConstants.NO_DATA_AVAILABLE);
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
     * Setting subnet size at insertion time from CSV.
     * IPAM-140 IPAM | Add v3 Support for SNMP in Remote Subnet
     * Update the gatewayId instead of the gateway or community.
     * IPAM-134 IPAM | Mail Server Configuration issue
     * Added generic sendMail method
     * IPAM-148 : System should have the ability to locate the available subnets inside a Supernet. This is to provide assistance to users when creating subnets inside an aggregated Network.
     * added the insertSubnetInSupernetCategory method call to insert the subnet into supernet
     * IPAM-149 : IPAM Roadmap : System should have alert notification module to configure different kind of alert notification
     * Added code for NEW_SUBNETS_DISCOVERED Alert
     * */
    @RequestMapping(value = TraceOrgCommonConstants.SUBNET_CSV_REST_URL, method = RequestMethod.POST)
    @PreAuthorize("hasAuthority('PERM_DASHBOARD_WRITE')")
    public ResponseEntity<?> importSubnetFromCSV(HttpServletRequest request,@RequestParam MultipartFile subnetCsv)
    {
        Response response = new Response();

        if(subnetCsv !=null )
        {
            try
            {
                String accessToken = request.getHeader(TraceOrgCommonConstants.ACCESSTOKEN);

                if(subnetCsv.getOriginalFilename().toLowerCase().endsWith("csv"))
                {
                    boolean importStatus = traceOrgCommonUtil.importCSVFile(subnetCsv,request,TraceOrgCommonConstants.SUBNET_DETAIL_CSV_NAME);

                    if (importStatus)
                    {
                        File importFile = new File(request.getRealPath(TraceOrgCommonConstants.SUBNET_DETAIL_CSV_PATH));

                        CsvReader csvReader = new CsvReader();

                        CsvContainer csv = csvReader.read(importFile, StandardCharsets.UTF_8);

                        boolean validFileStatus = false;

                        String mailMessage = null;

                        for (CsvRow csvRow : csv.getRows())
                        {

                            if(csvRow.getOriginalLineNumber() == 1)
                            {
                                validFileStatus = traceOrgCommonUtil.checkSubnetFileData(csvRow);
                            }

                            if(validFileStatus && csvRow.getOriginalLineNumber() >1)
                            {

                                if(csvRow.getField(0) == null || csvRow.getField(0).isEmpty() || csvRow.getField(1) == null || csvRow.getField(1).isEmpty() ||csvRow.getField(2) == null || csvRow.getField(2).isEmpty() ||csvRow.getField(3) == null || csvRow.getField(3).isEmpty()
                                    || (Strings.isNullOrEmpty(csvRow.getField(11)) == false && (csvRow.getField(11).equalsIgnoreCase("false")
                                        || csvRow.getField(11).equalsIgnoreCase("No"))
                                        && (Strings.isNullOrEmpty(csvRow.getField(12)) || traceOrgCommonUtil.checkGatewayIp(csvRow.getField(12)) == false )))
                                {
                                    _logger.debug("import subnet from csv failed: something is wrong");

                                    response.setSuccess(TraceOrgCommonConstants.FALSE);

                                    response.setMessage(TraceOrgMessageConstants.ENTER_VALID_DETAILS);
                                }
                                else
                                {
                                    TraceOrgSubnetDetails traceOrgSubnetDetails = new TraceOrgSubnetDetails();

                                    traceOrgSubnetDetails.setCreatedBy(traceOrgCommonUtil.currentUserName(accessToken));

                                    traceOrgSubnetDetails.setCreatedDate(new Date());

                                    traceOrgSubnetDetails.setModifiedDate(new Date());

                                    if(csvRow.getField(0) != null && !csvRow.getField(0).isEmpty())
                                    {
                                        List<TraceOrgCategory> traceOrgCategoryList = (List<TraceOrgCategory>)this.traceOrgService.commonQuery("",TraceOrgCommonConstants.CATEGORY_BY_NAME.replace(TraceOrgCommonConstants.CATEGORY_NAME_VALUE,csvRow.getField(0)));

                                        if(traceOrgCategoryList != null && !traceOrgCategoryList.isEmpty())
                                        {
                                            traceOrgSubnetDetails.setTraceOrgCategory(traceOrgCategoryList.get(0));
                                        }
                                        else
                                        {
                                            TraceOrgCategory traceOrgCategory = new TraceOrgCategory();

                                            traceOrgCategory.setCategoryName(csvRow.getField(0));

                                            traceOrgService.insert(traceOrgCategory);

                                            List<TraceOrgCategory> categoryList = (List<TraceOrgCategory>)this.traceOrgService.commonQuery("",TraceOrgCommonConstants.CATEGORY_BY_NAME.replace(TraceOrgCommonConstants.CATEGORY_NAME_VALUE,csvRow.getField(0)));

                                            if(categoryList!=null && !categoryList.isEmpty())
                                            {
                                                traceOrgSubnetDetails.setTraceOrgCategory(categoryList.get(0));
                                            }
                                        }
                                    }

                                    if(csvRow.getField(1) != null && !csvRow.getField(1).isEmpty())
                                    {
                                        traceOrgSubnetDetails.setSubnetAddress(csvRow.getField(1));
                                    }

                                    if(csvRow.getField(2) != null && !csvRow.getField(2).isEmpty())
                                    {
                                        traceOrgSubnetDetails.setSubnetMask(csvRow.getField(2));
                                    }

                                    if(csvRow.getField(3) != null && !csvRow.getField(3).isEmpty())
                                    {
                                        traceOrgSubnetDetails.setSubnetCidr(Integer.parseInt(csvRow.getField(3)));
                                    }

                                    if(csvRow.getField(4) != null && !csvRow.getField(4).isEmpty())
                                    {
                                        traceOrgSubnetDetails.setSubnetName(csvRow.getField(4));
                                    }

                                    if(csvRow.getField(5) != null && !csvRow.getField(5).isEmpty())
                                    {
                                        traceOrgSubnetDetails.setVlanName(csvRow.getField(5));
                                    }

                                    if(csvRow.getField(6) != null && !csvRow.getField(6).isEmpty())
                                    {
                                        traceOrgSubnetDetails.setLocation(csvRow.getField(6));
                                    }

                                    if(csvRow.getField(7) != null && !csvRow.getField(7).isEmpty())
                                    {
                                        traceOrgSubnetDetails.setDescription(csvRow.getField(7));
                                    }

                                    if(csvRow.getField(8) != null && !csvRow.getField(8).isEmpty())
                                    {
                                        traceOrgSubnetDetails.setDnsAddress(csvRow.getField(8));
                                    }

                                    if(csvRow.getField(9) != null && !csvRow.getField(9).isEmpty())
                                        traceOrgSubnetDetails.setScheduleHour(Integer.parseInt(csvRow.getField(9)));

                                    if(csvRow.getField(10) != null && !csvRow.getField(10).isEmpty())
                                    {
                                        switch (csvRow.getField(10))
                                        {
                                            case "Days" :
                                                traceOrgSubnetDetails.setDuration("Days");
                                                if(traceOrgSubnetDetails.getScheduleHour() > 32)
                                                    traceOrgSubnetDetails.setScheduleHour(0);
                                                break;
                                            case "Hours" :
                                                traceOrgSubnetDetails.setDuration("Hours");
                                                if(traceOrgSubnetDetails.getScheduleHour() > 24)
                                                    traceOrgSubnetDetails.setScheduleHour(0);
                                                break;
                                            case "Month" :
                                                traceOrgSubnetDetails.setDuration("Month");
                                                if(traceOrgSubnetDetails.getScheduleHour() > 12)
                                                    traceOrgSubnetDetails.setScheduleHour(0);
                                                break;
                                        }
                                    }

                                    if (Strings.isNullOrEmpty(csvRow.getField(11)) || (csvRow.getField(11).equalsIgnoreCase("false") ||
                                            csvRow.getField(11).equalsIgnoreCase("No")) == false)
                                    {
                                        traceOrgSubnetDetails.setIsLocalSubnet(true);
                                    }
                                    else
                                    {
                                        traceOrgSubnetDetails.setIsLocalSubnet(false);

                                        List<TraceOrgGateway> traceOrgGateways = (List<TraceOrgGateway>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_GATEWAY + " where gateway = '" +  csvRow.getField(12) + "'");

                                        if(traceOrgGateways != null && !traceOrgGateways.isEmpty())
                                        {
                                            traceOrgSubnetDetails.setGatewayId(traceOrgGateways.get(0).getId());
                                        }
                                    }

                                    if (traceOrgCommonUtil.checkSubnet(traceOrgSubnetDetails.getSubnetAddress(),traceOrgSubnetDetails.getSubnetCidr()) && traceOrgSubnetDetails.getTraceOrgCategory() != null && traceOrgSubnetDetails.getSubnetCidr()<32 && traceOrgSubnetDetails.getSubnetCidr()>15)
                                    {
                                        boolean isSubnetAddressAlreadyExist = false;
                                        boolean isSubnetNameAlreadyExist = false;

                                        if(this.traceOrgService.isExist(TraceOrgCommonConstants.TRACE_ORG_SUBNET_DETAILS,TraceOrgCommonConstants.SUBNET_ADDRESS,traceOrgSubnetDetails.getSubnetAddress()))
                                        {
                                            _logger.info(traceOrgSubnetDetails.getSubnetAddress() +" "+ TraceOrgMessageConstants.SUBNET_ADDRESS_ALREADY_EXIST);

                                            response.setSuccess(TraceOrgCommonConstants.FALSE);

                                            response.setMessage(TraceOrgMessageConstants.SUBNET_ADDRESS_ALREADY_EXIST);
                                            isSubnetAddressAlreadyExist = true;
                                        }
                                        if(this.traceOrgService.isExist(TraceOrgCommonConstants.TRACE_ORG_SUBNET_DETAILS,"subnetName",traceOrgSubnetDetails.getSubnetName()))
                                        {
                                            _logger.info(traceOrgSubnetDetails.getSubnetName() +" "+ TraceOrgMessageConstants.SUBNET_NAME_ALREADY_EXIST);

                                            response.setSuccess(TraceOrgCommonConstants.FALSE);

                                            response.setMessage(TraceOrgMessageConstants.SUBNET_NAME_ALREADY_EXIST);
                                            if (isSubnetAddressAlreadyExist) {
                                                response.setMessage(TraceOrgMessageConstants.SUBNET_NAME_AND_ADDRESS_ALREADY_EXIST);
                                            }
                                            isSubnetNameAlreadyExist = true;
                                        }
                                        if ((isSubnetAddressAlreadyExist || isSubnetNameAlreadyExist) == false)
                                        {
                                            traceOrgSubnetDetails.setTotalIp(traceOrgCommonUtil.countTotalIp(traceOrgSubnetDetails.getSubnetAddress(),traceOrgSubnetDetails.getSubnetCidr()));

                                            traceOrgSubnetDetails.setAvailableIp(traceOrgCommonUtil.countTotalIp(traceOrgSubnetDetails.getSubnetAddress(),traceOrgSubnetDetails.getSubnetCidr()) - 2);

                                            traceOrgSubnetDetails.setType("Normal");

                                            traceOrgSubnetDetails.setAllowDns(true);

                                            traceOrgSubnetDetails.setAllowIcmp(true);

                                            boolean insertStatus = this.traceOrgService.insert(traceOrgSubnetDetails);

                                            traceOrgSupernetService.insertSubnetInSupernetCategory(traceOrgSubnetDetails.getSubnetAddress(), traceOrgSubnetDetails.getSubnetCidr(), traceOrgSubnetDetails.getId(), traceOrgCommonUtil.currentUser(accessToken), "by Import From CSV by " + traceOrgCommonUtil.currentUserName(accessToken));

                                            if(insertStatus)
                                            {
                                                if(mailMessage!=null)
                                                {
                                                    mailMessage = mailMessage + "," + traceOrgSubnetDetails.getSubnetAddress();
                                                }
                                                else
                                                {
                                                    mailMessage = traceOrgSubnetDetails.getSubnetAddress();
                                                }

                                                if(traceOrgSubnetDetails.getScheduleHour() > 0)
                                                {
                                                    String cronExpression = null;

                                                    switch (traceOrgSubnetDetails.getDuration())
                                                    {
                                                        case "Days" :
                                                            cronExpression = "0 0 0 1-31/"+traceOrgSubnetDetails.getScheduleHour()+" * ?";
                                                            break;
                                                        case "Hours" :
                                                            cronExpression = "0 0 1-23/"+traceOrgSubnetDetails.getScheduleHour()+" ? * *";
                                                            break;
                                                        case "Month" :
                                                            cronExpression = "0 0 0 1 1-12/"+traceOrgSubnetDetails.getScheduleHour()+" ?";
                                                            break;
                                                    }

                                                    traceOrgCommonUtil.scanSubnetCronJob(cronExpression,traceOrgSubnetDetails,traceOrgService,traceOrgCommonUtil);
                                                }

                                                response.setSuccess(TraceOrgCommonConstants.TRUE);

                                                traceOrgCommonUtil.ipList(traceOrgSubnetDetails);

                                                this.traceOrgService.insert(traceOrgSubnetDetails);

                                                TraceOrgEvent traceOrgEvent =  new TraceOrgEvent();

                                                traceOrgEvent.setTimestamp(new Date());

                                                traceOrgEvent.setDoneBy(traceOrgCommonUtil.currentUser(accessToken));

                                                traceOrgEvent.setEventType("Add Subnet");

                                                traceOrgEvent.setEventContext("Subnet "+traceOrgSubnetDetails.getSubnetAddress()+" is added in IP Address Manager by "+traceOrgCommonUtil.currentUserName(accessToken)  );

                                                traceOrgEvent.setSeverity(1);

                                                this.traceOrgService.insert(traceOrgEvent);

                                                response.setMessage(TraceOrgMessageConstants.CSV_IMPORT_SUCCESS);
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
                                        response.setSuccess(TraceOrgCommonConstants.TRUE);

                                        response.setMessage(TraceOrgMessageConstants.CSV_IMPORT_SUCCESS);
                                    }
                                }
                            }
                            else
                            {
                                response.setSuccess(TraceOrgCommonConstants.FALSE);

                                response.setMessage(TraceOrgMessageConstants.FILE__DETAIL_NOT_VALID);
                            }
                        }

                        HashMap<String, Object> context = new HashMap<>();

                        context.put("subnetAddress", mailMessage);

                        context.put("currentUserName", traceOrgCommonUtil.currentUserName(accessToken));

                        traceOrgCommonUtil.sendMessage(TraceOrgCommonConstants.ALERT_QUEUE, context, TraceOrgCommonConstants.NEW_SUBNETS_DISCOVERED);
                    }
                    else
                    {
                         response.setSuccess(TraceOrgCommonConstants.FALSE);

                        response.setMessage(TraceOrgMessageConstants.SOMETHING_WENT_WRONG);
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

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @RequestMapping(value = TraceOrgCommonConstants.EXPORT_CSV_SUBNET_REST_URL, method = RequestMethod.GET)
    @PreAuthorize("hasAuthority('PERM_DASHBOARD_READ')")
    public ResponseEntity<?> exportCsvListAllSubnet(HttpServletRequest request)
    {
        Response response = new Response();

        try
        {
            String accessToken = request.getHeader(TraceOrgCommonConstants.ACCESSTOKEN);

            List<TraceOrgSubnetDetails> subnetDetailsList = (List<TraceOrgSubnetDetails>) this.traceOrgService.commonQuery("",TraceOrgCommonConstants.TRACE_ORG_SUBNET_DETAILS);

            if(subnetDetailsList !=null && !subnetDetailsList.isEmpty())
            {
                String url = traceOrgCommonUtil.exportSubnetCsv(request,subnetDetailsList);

                response.setData(url);

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

            response.setMessage(TraceOrgMessageConstants.SOMETHING_WENT_WRONG);
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    @RequestMapping(value = TraceOrgCommonConstants.EXPORT_PDF_SUBNET_REST_URL, method = RequestMethod.GET)
    @PreAuthorize("hasAuthority('PERM_DASHBOARD_READ')")
    public ResponseEntity<?> exportPdfListAllSubnet(HttpServletRequest request)
    {
        Response response = new Response();

        try
        {
            String accessToken = request.getHeader(TraceOrgCommonConstants.ACCESSTOKEN);

            List<TraceOrgSubnetDetails> subnetDetailsList = (List<TraceOrgSubnetDetails>) this.traceOrgService.commonQuery("",TraceOrgCommonConstants.TRACE_ORG_SUBNET_DETAILS);

            if(subnetDetailsList !=null && !subnetDetailsList.isEmpty())
            {
                HashMap<String,Object> gridReport = new HashMap<>();

                gridReport.put("Title","Subnet Summary");
                try
                {
                    LinkedHashSet<String> columns  = new LinkedHashSet<String>()
                    {{
                        add("Category Name");

                        add("Subnet Address");

                        add("Subnet Type");

                        add("Subnet Mask");

                        add("Subnet CIDR");

                        add("Subnet Name");

                        add("VLAN Name");

                        add("Location");

                        add("All IP");

                        add("Used IP");

                        add("Aavailable IP");

                        add("Transient IP");

                        add("Description");

                        add("DNS Address");

                        add("Scheduled Hours");
                    }};

                    List<Object> pdfResults  =new ArrayList<>();

                    List<Object> pdfResult;

                    for(TraceOrgSubnetDetails traceOrgSubnetDetail :subnetDetailsList)
                    {
                        pdfResult = new ArrayList<>();

                        pdfResult.add(traceOrgSubnetDetail.getTraceOrgCategory().getCategoryName());

                        pdfResult.add(traceOrgSubnetDetail.getSubnetAddress());

                        pdfResult.add(traceOrgSubnetDetail.getType());

                        pdfResult.add(traceOrgSubnetDetail.getSubnetMask());

                        pdfResult.add(traceOrgSubnetDetail.getSubnetCidr());

                        pdfResult.add(traceOrgSubnetDetail.getSubnetName());

                        pdfResult.add(traceOrgSubnetDetail.getVlanName());

                        pdfResult.add(traceOrgSubnetDetail.getLocation());

                        pdfResult.add(traceOrgSubnetDetail.getTotalIp());

                        pdfResult.add(traceOrgSubnetDetail.getUsedIp());

                        pdfResult.add(traceOrgSubnetDetail.getAvailableIp());

                        pdfResult.add(traceOrgSubnetDetail.getTransientIp());

                        pdfResult.add(traceOrgSubnetDetail.getDescription());

                        pdfResult.add(traceOrgSubnetDetail.getDnsAddress());

                        pdfResult.add(traceOrgSubnetDetail.getScheduleHour());

                        pdfResults.add(pdfResult);
                    }

                    HashMap<String, Object> results = new HashMap<>();

                    results.put("grid-result",pdfResults);

                    results.put("columns",columns);

                    List<HashMap<String, Object>> visualizationResults = new ArrayList<>();

                    visualizationResults.add(results);

                    String fileName = "Subnet Summary "+TraceOrgCommonConstants.VISUAL_DATE_FORMAT.format(new Date())+".pdf";

                    fileName = fileName.replace(" ","_").replace(":","_").replace(",","");

                    TraceOrgPDFBuilder.addGridReport(1,visualizationResults,new HashMap<String, Object>(),fileName,gridReport);

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

            response.setMessage(TraceOrgMessageConstants.SOMETHING_WENT_WRONG);
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @RequestMapping(value = TraceOrgCommonConstants.EXPORT_PDF_NORMAL_SUBNET_REST_URL, method = RequestMethod.GET)
    @PreAuthorize("hasAuthority('PERM_DASHBOARD_READ')")
    public ResponseEntity<?> exportNormalPdfListAllSubnet(HttpServletRequest request)
    {
        Response response = new Response();

        try
        {
            String accessToken = request.getHeader(TraceOrgCommonConstants.ACCESSTOKEN);

            List<TraceOrgSubnetDetails> subnetDetailsList = (List<TraceOrgSubnetDetails>) this.traceOrgService.commonQuery("",TraceOrgCommonConstants.TRACE_ORG_SUBNET_DETAILS + " where type = 'NORMAL'");

            if(subnetDetailsList !=null && !subnetDetailsList.isEmpty())
            {
                HashMap<String,Object> gridReport = new HashMap<>();

                gridReport.put("Title","Subnet Summary");
                try
                {
                    LinkedHashSet<String> columns  = new LinkedHashSet<String>()
                    {{
                        add("Category Name");

                        add("Subnet Address");

                        add("Subnet Type");

                        add("Subnet Name");

                        add("All IP");

                        add("Used IP");

                        add("Aavailable IP");

                        add("Transient IP");

                        add("% In Space  Used");

                    }};

                    List<Object> pdfResults  =new ArrayList<>();

                    List<Object> pdfResult;

                    for(TraceOrgSubnetDetails traceOrgSubnetDetail :subnetDetailsList)
                    {
                        pdfResult = new ArrayList<>();

                        pdfResult.add(traceOrgSubnetDetail.getTraceOrgCategory().getCategoryName());

                        pdfResult.add(traceOrgSubnetDetail.getSubnetAddress());

                        pdfResult.add(traceOrgSubnetDetail.getType());

                        pdfResult.add(traceOrgSubnetDetail.getSubnetName());

                        pdfResult.add(traceOrgSubnetDetail.getTotalIp());

                        pdfResult.add(traceOrgSubnetDetail.getUsedIp());

                        pdfResult.add(traceOrgSubnetDetail.getAvailableIp());

                        pdfResult.add(traceOrgSubnetDetail.getTransientIp());

                        pdfResult.add(traceOrgSubnetDetail.getUsedIpPercentage());

                        pdfResults.add(pdfResult);
                    }

                    HashMap<String, Object> results = new HashMap<>();

                    results.put("grid-result",pdfResults);

                    results.put("columns",columns);

                    List<HashMap<String, Object>> visualizationResults = new ArrayList<>();

                    visualizationResults.add(results);

                    String fileName = "Normal Subnet Summary "+TraceOrgCommonConstants.VISUAL_DATE_FORMAT.format(new Date())+".pdf";

                    fileName = fileName.replace(" ","_").replace(":","_").replace(",","");

                    TraceOrgPDFBuilder.addGridReport(1,visualizationResults,new HashMap<String, Object>(),fileName,gridReport);

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

            response.setMessage(TraceOrgMessageConstants.SOMETHING_WENT_WRONG);
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    @RequestMapping(value = TraceOrgCommonConstants.EXPORT_PDF_DHCP_SUBNET_REST_URL, method = RequestMethod.GET)
    @PreAuthorize("hasAuthority('PERM_DASHBOARD_READ')")
    public ResponseEntity<?> exportDhcpPdfListAllSubnet(HttpServletRequest request)
    {
        Response response = new Response();

        try
        {
            String accessToken = request.getHeader(TraceOrgCommonConstants.ACCESSTOKEN);

            List<TraceOrgSubnetDetails> subnetDetailsList = (List<TraceOrgSubnetDetails>) this.traceOrgService.commonQuery("",TraceOrgCommonConstants.TRACE_ORG_SUBNET_DETAILS + " where type = 'CISCO' or type = 'WINDOWS'");

            if(subnetDetailsList !=null && !subnetDetailsList.isEmpty())
            {
                HashMap<String,Object> gridReport = new HashMap<>();

                gridReport.put("Title","Subnet Summary");
                try
                {
                    LinkedHashSet<String> columns  = new LinkedHashSet<String>()
                    {{
                        add("Category Name");

                        add("Subnet Address");

                        add("Subnet Type");

                        add("Subnet Name");

                        add("All IP");

                        add("Used IP");

                        add("Aavailable IP");

                        add("Transient IP");

                        add("% In Space  Used");

                    }};

                    List<Object> pdfResults  =new ArrayList<>();

                    List<Object> pdfResult;

                    for(TraceOrgSubnetDetails traceOrgSubnetDetail :subnetDetailsList)
                    {
                        pdfResult = new ArrayList<>();

                        pdfResult.add(traceOrgSubnetDetail.getTraceOrgCategory().getCategoryName());

                        pdfResult.add(traceOrgSubnetDetail.getSubnetAddress());

                        pdfResult.add(traceOrgSubnetDetail.getType());

                        pdfResult.add(traceOrgSubnetDetail.getSubnetName());

                        pdfResult.add(traceOrgSubnetDetail.getTotalIp());

                        pdfResult.add(traceOrgSubnetDetail.getUsedIp());

                        pdfResult.add(traceOrgSubnetDetail.getAvailableIp());

                        pdfResult.add(traceOrgSubnetDetail.getTransientIp());

                        pdfResult.add(traceOrgSubnetDetail.getUsedIpPercentage());

                        pdfResults.add(pdfResult);
                    }

                    HashMap<String, Object> results = new HashMap<>();

                    results.put("grid-result",pdfResults);

                    results.put("columns",columns);

                    List<HashMap<String, Object>> visualizationResults = new ArrayList<>();

                    visualizationResults.add(results);

                    String fileName = "DHCP Subnet  Summary "+TraceOrgCommonConstants.VISUAL_DATE_FORMAT.format(new Date())+".pdf";

                    fileName = fileName.replace(" ","_").replace(":","_").replace(",","");

                    TraceOrgPDFBuilder.addGridReport(1,visualizationResults,new HashMap<String, Object>(),fileName,gridReport);

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

            response.setMessage(TraceOrgMessageConstants.SOMETHING_WENT_WRONG);
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    @RequestMapping(value = TraceOrgCommonConstants.NORMAL_SUBNET_REST_URL, method = RequestMethod.GET)
    @PreAuthorize("hasAuthority('PERM_DASHBOARD_READ')")
    public ResponseEntity<?> listAllNormalSubnet(HttpServletRequest request)
    {
        Response response = new Response();

        try
        {
            String accessToken = request.getHeader(TraceOrgCommonConstants.ACCESSTOKEN);

            List<TraceOrgSubnetDetails> subnetDetailsList = (List<TraceOrgSubnetDetails>) this.traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_SUBNET_DETAILS + " where traceOrgDhcpCredentialDetailsId is  null");

            if(subnetDetailsList !=null && !subnetDetailsList.isEmpty())
            {
                response.setData(subnetDetailsList);

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

            response.setMessage(TraceOrgMessageConstants.SOMETHING_WENT_WRONG);
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @RequestMapping(value = TraceOrgCommonConstants.TOP_10_SUBNET_REST_URL, method = RequestMethod.GET)
    @PreAuthorize("hasAuthority('PERM_DASHBOARD_READ')")
    public ResponseEntity<?> getTop10SubnetUtilization(HttpServletRequest request)
    {
        Response response = new Response();

        traceOrgCommonUtil.buildResponse(traceOrgSubnetService.getTop10SubnetUtilization(), response);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @RequestMapping(value = TraceOrgCommonConstants.TOP_10_CATEGORY_REST_URL, method = RequestMethod.GET)
    @PreAuthorize("hasAuthority('PERM_DASHBOARD_READ')")
    public ResponseEntity<?> getTop10CategoryUtilization(HttpServletRequest request)
    {
        Response response = new Response();

        traceOrgCommonUtil.buildResponse(traceOrgSubnetService.getTop10CategoryUtilization(), response);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @RequestMapping(value = TraceOrgCommonConstants.DNS_STATUS_SUMMARY, method = RequestMethod.GET)
    @PreAuthorize("hasAuthority('PERM_DASHBOARD_READ')")
    public ResponseEntity<?> dnsStatusSummary(HttpServletRequest request)
    {
        Response response = new Response();

        traceOrgCommonUtil.buildResponse(traceOrgSubnetService.dnsStatusSummary(), response);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @RequestMapping(value = TraceOrgCommonConstants.RECENT_DISCOVERED_REST_API, method = RequestMethod.GET)
    @PreAuthorize("hasAuthority('PERM_DASHBOARD_READ')")
    public ResponseEntity<?> recentDiscovered(HttpServletRequest request)
    {
        Response response = new Response();

        traceOrgCommonUtil.buildResponse(traceOrgSubnetService.recentDiscovered(), response);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    @RequestMapping(value = TraceOrgCommonConstants.DHCP_SUBNET_REST_URL, method = RequestMethod.GET)
    @PreAuthorize("hasAuthority('PERM_DASHBOARD_READ')")
    public ResponseEntity<?> listAllDhcpSubnet(HttpServletRequest request)
    {
        Response response = new Response();

        try
        {
            String accessToken = request.getHeader(TraceOrgCommonConstants.ACCESSTOKEN);

            List<TraceOrgSubnetDetails> subnetDetailsList = (List<TraceOrgSubnetDetails>) this.traceOrgService.commonQuery("",TraceOrgCommonConstants.TRACE_ORG_SUBNET_DETAILS + " where traceOrgDhcpCredentialDetailsId is not null");

            if(subnetDetailsList !=null && !subnetDetailsList.isEmpty())
            {
                response.setData(subnetDetailsList);

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

            response.setMessage(TraceOrgMessageConstants.SOMETHING_WENT_WRONG);
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @RequestMapping(value = TraceOrgCommonConstants.SUBNET_SCAN_STATUS, method = RequestMethod.GET)
    @PreAuthorize("hasAuthority('PERM_DASHBOARD_READ')")
    public ResponseEntity<?> statusScanSubnet(HttpServletRequest request)
    {
        Response response = new Response();

        try
        {
            String accessToken = request.getHeader(TraceOrgCommonConstants.ACCESSTOKEN);

            response.setSuccess(TraceOrgCommonUtil.getSubnetScanStatus() == 0 ? false : true);

            if(TraceOrgCommonUtil.m_scanSubnet.containsKey("jobKey") && TraceOrgCommonUtil.m_scanSubnet.get("jobKey") != null)
            {
                response.setMessage(TraceOrgCommonUtil.m_scanSubnet.get("jobKey"));
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

    @RequestMapping(value = TraceOrgCommonConstants.SUBNET_IMPORT_STATUS, method = RequestMethod.GET)
    @PreAuthorize("hasAuthority('PERM_DASHBOARD_READ')")
    public ResponseEntity<?> importSubnetStatus(HttpServletRequest request)
    {
        Response response = new Response();

        try
        {
            String accessToken = request.getHeader(TraceOrgCommonConstants.ACCESSTOKEN);

            response.setSuccess(TraceOrgCommonUtil.getCSVImportCount() == 0 ? true : false);

            if(!response.isSuccess())
            {
                response.setMessage("Subnet Detail Import Is Running");
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


    /**
     * IPAM-145 : System should have rogue device detection capability
     * Added Trusted Ip list in the report page.
     * @param request
     * @return
     */
    @RequestMapping(value = TraceOrgCommonConstants.SUBNET_BY_REPORT, method = RequestMethod.GET)
    @PreAuthorize("hasAnyAuthority('PERM_DASHBOARD_READ', 'PERM_REPORTS_READ')")
    public ResponseEntity<?> listAllSubnetForReport(HttpServletRequest request)
    {
        Response response = new Response();

        try
        {
            String accessToken = request.getHeader(TraceOrgCommonConstants.ACCESSTOKEN);

            List<TraceOrgSubnetDetails> subnetDetailsList = (List<TraceOrgSubnetDetails>) this.traceOrgService.commonQuery("",TraceOrgCommonConstants.TRACE_ORG_SUBNET_DETAILS);

            List<TraceOrgSubnetDetails> subnetDetailsListUsed = (List<TraceOrgSubnetDetails>) this.traceOrgService.commonQuery("",TraceOrgCommonConstants.TRACE_ORG_SUBNET_DETAILS);

            List<TraceOrgSubnetDetails> subnetDetailsListAvailable = (List<TraceOrgSubnetDetails>) this.traceOrgService.commonQuery("",TraceOrgCommonConstants.TRACE_ORG_SUBNET_DETAILS);

            List<TraceOrgSubnetDetails> subnetDetailsListTransient = (List<TraceOrgSubnetDetails>) this.traceOrgService.commonQuery("",TraceOrgCommonConstants.TRACE_ORG_SUBNET_DETAILS);

            List<TraceOrgSubnetDetails> subnetDetailsListReserved = (List<TraceOrgSubnetDetails>) this.traceOrgService.commonQuery("",TraceOrgCommonConstants.TRACE_ORG_SUBNET_DETAILS);

            List<TraceOrgSubnetDetails> subnetDetailsListRogue = (List<TraceOrgSubnetDetails>) this.traceOrgService.commonQuery("",TraceOrgCommonConstants.TRACE_ORG_SUBNET_DETAILS);

            List<TraceOrgSubnetDetails> subnetDetailsListTrusted = (List<TraceOrgSubnetDetails>) this.traceOrgService.commonQuery("",TraceOrgCommonConstants.TRACE_ORG_SUBNET_DETAILS);

            List<TraceOrgSubnetDetails> subnetDetailsListVendor = (List<TraceOrgSubnetDetails>) this.traceOrgService.commonQuery("",TraceOrgCommonConstants.TRACE_ORG_SUBNET_DETAILS);

            if (subnetDetailsList != null && !subnetDetailsList.isEmpty())
            {
                List<Object> subnetByCategory =  new ArrayList<>();

                HashMap<String,Object> categoryDetailsAllIP = new HashMap<>();

                HashMap<String,Object> categoryDetailsUsedIP = new HashMap<>();

                HashMap<String,Object> categoryDetailsAvailableIP = new HashMap<>();

                HashMap<String,Object> categoryDetailsTransientIP = new HashMap<>();

                HashMap<String,Object> categoryDetailsReservedIP = new HashMap<>();

                HashMap<String,Object> categoryDetailsRogueIP = new HashMap<>();

                HashMap<String,Object> categoryDetailsTrustedIP = new HashMap<>();

                HashMap<String,Object> categoryDetailsVendor = new HashMap<>();

                float totalUsedIpPercentage = 0 ;

                float totalUsedIp = 0;

                float totalIp = 0;


                //ALL
                for(TraceOrgSubnetDetails traceOrgSubnetDetails : subnetDetailsList)
                {
                    totalIp = totalIp + traceOrgSubnetDetails.getTotalIp();

                    totalUsedIp = totalUsedIp + traceOrgSubnetDetails.getUsedIp();

                    traceOrgSubnetDetails.setNetworkInterface("ALL");
                }

                if(subnetDetailsList !=null && !subnetDetailsList.isEmpty())
                {
                    totalUsedIpPercentage = (totalUsedIp * 100)  / totalIp ;
                }

                categoryDetailsAllIP.put("subnetAddress","All IP");

                categoryDetailsAllIP.put("subnets",subnetDetailsList);

                categoryDetailsAllIP.put("totalUsedIpPercentage",totalUsedIpPercentage);

                if(totalUsedIpPercentage < 50)
                {
                    categoryDetailsAllIP.put("severity",3);
                }
                else if(totalUsedIpPercentage >= 50 && totalUsedIpPercentage <80)
                {
                    categoryDetailsAllIP.put("severity",2);
                }
                else if(totalUsedIpPercentage >= 80)
                {
                    categoryDetailsAllIP.put("severity",1);
                }

                subnetByCategory.add(categoryDetailsAllIP);

                //Used
                for(TraceOrgSubnetDetails traceOrgSubnetDetails : subnetDetailsListUsed)
                {
                    totalIp = totalIp + traceOrgSubnetDetails.getTotalIp();

                    totalUsedIp = totalUsedIp + traceOrgSubnetDetails.getUsedIp();

                    traceOrgSubnetDetails.setNetworkInterface("USED");

                }

                categoryDetailsUsedIP.put("subnetAddress","Used IP");

                categoryDetailsUsedIP.put("subnets",subnetDetailsListUsed);

                categoryDetailsUsedIP.put("totalUsedIpPercentage",totalUsedIpPercentage);

                if(totalUsedIpPercentage < 50)
                {
                    categoryDetailsUsedIP.put("severity",3);
                }
                else if(totalUsedIpPercentage >= 50 && totalUsedIpPercentage <80)
                {
                    categoryDetailsUsedIP.put("severity",2);
                }
                else if(totalUsedIpPercentage >= 80)
                {
                    categoryDetailsUsedIP.put("severity",1);
                }

                subnetByCategory.add(categoryDetailsUsedIP);

                //Available
                for(TraceOrgSubnetDetails traceOrgSubnetDetails : subnetDetailsListAvailable)
                {
                    totalIp = totalIp + traceOrgSubnetDetails.getTotalIp();

                    totalUsedIp = totalUsedIp + traceOrgSubnetDetails.getUsedIp();

                    traceOrgSubnetDetails.setNetworkInterface("AVAILABLE");

                }

                categoryDetailsAvailableIP.put("subnetAddress","Available IP");

                categoryDetailsAvailableIP.put("subnets",subnetDetailsListAvailable);

                categoryDetailsAvailableIP.put("totalUsedIpPercentage",totalUsedIpPercentage);

                if(totalUsedIpPercentage < 50)
                {
                    categoryDetailsAvailableIP.put("severity",3);
                }
                else if(totalUsedIpPercentage >= 50 && totalUsedIpPercentage <80)
                {
                    categoryDetailsAvailableIP.put("severity",2);
                }
                else if(totalUsedIpPercentage >= 80)
                {
                    categoryDetailsAvailableIP.put("severity",1);
                }

                subnetByCategory.add(categoryDetailsAvailableIP);


                //Reserved
                for(TraceOrgSubnetDetails traceOrgSubnetDetails : subnetDetailsListReserved)
                {
                    totalIp = totalIp + traceOrgSubnetDetails.getTotalIp();

                    totalUsedIp = totalUsedIp + traceOrgSubnetDetails.getUsedIp();

                    traceOrgSubnetDetails.setNetworkInterface("RESERVED");

                }

                categoryDetailsReservedIP.put("subnetAddress","Reserved IP");

                categoryDetailsReservedIP.put("subnets",subnetDetailsListReserved);

                categoryDetailsReservedIP.put("totalUsedIpPercentage",totalUsedIpPercentage);

                if(totalUsedIpPercentage < 50)
                {
                    categoryDetailsReservedIP.put("severity",3);
                }
                else if(totalUsedIpPercentage >= 50 && totalUsedIpPercentage <80)
                {
                    categoryDetailsReservedIP.put("severity",2);
                }
                else if(totalUsedIpPercentage >= 80)
                {
                    categoryDetailsReservedIP.put("severity",1);
                }

                subnetByCategory.add(categoryDetailsReservedIP);

                //Transient
                for(TraceOrgSubnetDetails traceOrgSubnetDetails : subnetDetailsListTransient)
                {
                    totalIp = totalIp + traceOrgSubnetDetails.getTotalIp();

                    totalUsedIp = totalUsedIp + traceOrgSubnetDetails.getUsedIp();

                    traceOrgSubnetDetails.setNetworkInterface("TRANSIENT");

                }
                categoryDetailsTransientIP.put("subnetAddress","Transient IP");

                categoryDetailsTransientIP.put("subnets",subnetDetailsListTransient);

                categoryDetailsTransientIP.put("totalUsedIpPercentage",totalUsedIpPercentage);

                if(totalUsedIpPercentage < 50)
                {
                    categoryDetailsTransientIP.put("severity",3);
                }
                else if(totalUsedIpPercentage >= 50 && totalUsedIpPercentage <80)
                {
                    categoryDetailsTransientIP.put("severity",2);
                }
                else if(totalUsedIpPercentage >= 80)
                {
                    categoryDetailsTransientIP.put("severity",1);
                }

                subnetByCategory.add(categoryDetailsTransientIP);


                //Rogue
                for(TraceOrgSubnetDetails traceOrgSubnetDetails : subnetDetailsListRogue)
                {
                    totalIp = totalIp + traceOrgSubnetDetails.getTotalIp();

                    totalUsedIp = totalUsedIp + traceOrgSubnetDetails.getUsedIp();

                    traceOrgSubnetDetails.setNetworkInterface("ROGUE");

                }
                categoryDetailsRogueIP.put("subnetAddress","Rogue IP");

                categoryDetailsRogueIP.put("subnets",subnetDetailsListRogue);

                categoryDetailsRogueIP.put("totalUsedIpPercentage",totalUsedIpPercentage);

                if(totalUsedIpPercentage < 50)
                {
                    categoryDetailsRogueIP.put("severity",3);
                }
                else if(totalUsedIpPercentage >= 50 && totalUsedIpPercentage <80)
                {
                    categoryDetailsRogueIP.put("severity",2);
                }
                else if(totalUsedIpPercentage >= 80)
                {
                    categoryDetailsRogueIP.put("severity",1);
                }
                subnetByCategory.add(categoryDetailsRogueIP);

                //Trusted
                for(TraceOrgSubnetDetails traceOrgSubnetDetails : subnetDetailsListTrusted)
                {
                    totalIp = totalIp + traceOrgSubnetDetails.getTotalIp();

                    totalUsedIp = totalUsedIp + traceOrgSubnetDetails.getUsedIp();

                    traceOrgSubnetDetails.setNetworkInterface("TRUSTED");

                }
                categoryDetailsTrustedIP.put("subnetAddress","Trusted IP");

                categoryDetailsTrustedIP.put("subnets",subnetDetailsListTrusted);

                categoryDetailsTrustedIP.put("totalUsedIpPercentage",totalUsedIpPercentage);

                if(totalUsedIpPercentage < 50)
                {
                    categoryDetailsTrustedIP.put("severity",3);
                }
                else if(totalUsedIpPercentage >= 50 && totalUsedIpPercentage <80)
                {
                    categoryDetailsTrustedIP.put("severity",2);
                }
                else if(totalUsedIpPercentage >= 80)
                {
                    categoryDetailsTrustedIP.put("severity",1);
                }
                subnetByCategory.add(categoryDetailsTrustedIP);

                //Vendor
                for(TraceOrgSubnetDetails traceOrgSubnetDetails : subnetDetailsListVendor)
                {
                    totalIp = totalIp + traceOrgSubnetDetails.getTotalIp();

                    totalUsedIp = totalUsedIp + traceOrgSubnetDetails.getUsedIp();

                    traceOrgSubnetDetails.setNetworkInterface("VENDOR SUMMARY");
                }

                if(subnetDetailsListVendor !=null && !subnetDetailsListVendor.isEmpty())
                {
                    totalUsedIpPercentage = (totalUsedIp * 100)  / totalIp ;
                }

                categoryDetailsVendor.put("subnetAddress","Vendor Summary");

                categoryDetailsVendor.put("subnets",subnetDetailsListVendor);

                categoryDetailsVendor.put("totalUsedIpPercentage",totalUsedIpPercentage);

                if(totalUsedIpPercentage < 50)
                {
                    categoryDetailsVendor.put("severity",3);
                }
                else if(totalUsedIpPercentage >= 50 && totalUsedIpPercentage <80)
                {
                    categoryDetailsVendor.put("severity",2);
                }
                else if(totalUsedIpPercentage >= 80)
                {
                    categoryDetailsVendor.put("severity",1);
                }
                subnetByCategory.add(categoryDetailsVendor);

                response.setData (subnetByCategory);

                response.setSuccess(TraceOrgCommonConstants.TRUE);
            }
            else
            {
                List<Object> subnetByCategory =  new ArrayList<>();

                HashMap<String,Object> categoryDetailsAllIP = new HashMap<>();

                HashMap<String,Object> categoryDetailsUsedIP = new HashMap<>();

                HashMap<String,Object> categoryDetailsAvailableIP = new HashMap<>();

                HashMap<String,Object> categoryDetailsReservedIP = new HashMap<>();

                HashMap<String,Object> categoryDetailsTransientIP = new HashMap<>();

                HashMap<String,Object> categoryDetailsRogueIP = new HashMap<>();

                HashMap<String,Object> categoryDetailsTrustedIP = new HashMap<>();

                HashMap<String,Object> categoryDetailsVendor = new HashMap<>();

                List<TraceOrgSubnetDetails> traceOrgSubnetDetails =  new ArrayList<>();

                categoryDetailsUsedIP.put("subnetAddress","Used IP");

                categoryDetailsUsedIP.put("subnets",traceOrgSubnetDetails);

                categoryDetailsAllIP.put("subnetAddress","All IP");

                categoryDetailsAllIP.put("subnets",traceOrgSubnetDetails);

                categoryDetailsAvailableIP.put("subnetAddress","Available IP");

                categoryDetailsAvailableIP.put("subnets",traceOrgSubnetDetails);

                categoryDetailsReservedIP.put("subnetAddress","Reserved IP");

                categoryDetailsReservedIP.put("subnets",traceOrgSubnetDetails);

                categoryDetailsTransientIP.put("subnetAddress","Transient IP");

                categoryDetailsTransientIP.put("subnets",traceOrgSubnetDetails);

                categoryDetailsRogueIP.put("subnetAddress","Rogue IP");

                categoryDetailsRogueIP.put("subnets",traceOrgSubnetDetails);

                categoryDetailsTrustedIP.put("subnetAddress","Trusted IP");

                categoryDetailsTrustedIP.put("subnets",traceOrgSubnetDetails);

                categoryDetailsVendor.put("subnetAddress","Vendor Summary");

                categoryDetailsVendor.put("subnets",traceOrgSubnetDetails);

                subnetByCategory.add(categoryDetailsAllIP);

                subnetByCategory.add(categoryDetailsUsedIP);

                subnetByCategory.add(categoryDetailsAvailableIP);

                subnetByCategory.add(categoryDetailsReservedIP);

                subnetByCategory.add(categoryDetailsTransientIP);

                subnetByCategory.add(categoryDetailsRogueIP);

                subnetByCategory.add(categoryDetailsVendor);

                response.setData(subnetByCategory);

                response.setSuccess(TraceOrgCommonConstants.TRUE);
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

    /**
     * IPAM-145 : System should have rogue device detection capability
     * Added logic for trusted Ip and change the logic of rogue ip to get list of all.
     * @param request
     * @param subnetId
     * @param ipStatus
     * @param exportTimeline
     * @return
     */
    @RequestMapping(value = TraceOrgCommonConstants.SUBNET_IP_BY_REPORT_TIMELINE, method = RequestMethod.GET)
    @PreAuthorize("hasAnyAuthority('PERM_DASHBOARD_READ', 'PERM_REPORTS_READ')")
    public ResponseEntity<?> listSubnetIpReportByTimeline(HttpServletRequest request,@RequestParam String subnetId,@RequestParam String ipStatus,@RequestParam Integer exportTimeline)
    {
        Response response = new Response();

        if(subnetId !=null && !subnetId.trim().isEmpty() && ipStatus!=null && !ipStatus.trim().isEmpty() && exportTimeline!=null)
        {
            try
            {
                String accessToken = request.getHeader(TraceOrgCommonConstants.ACCESSTOKEN);

                List<TraceOrgSubnetIpDetails> subnetIpDetailsList = null ;

                List<Object> vendorSummaryList = new ArrayList<>() ;

                List<TraceOrgSubnetIpDetails> outputIpDetailsList = new ArrayList<>();

                switch (ipStatus.toUpperCase())
                {
                    case "USED IP" :
                        ipStatus = TraceOrgCommonConstants.USED;
                        break;
                    case "AVAILABLE IP" :
                        ipStatus = TraceOrgCommonConstants.AVAILABLE;
                        break;
                    case "RESERVED IP" :
                        ipStatus = TraceOrgCommonConstants.RESERVED;
                        break;
                    case "TRANSIENT IP" :
                        ipStatus = TraceOrgCommonConstants.TRANSIENT;
                        break;
                }

                if(ipStatus.equals(TraceOrgCommonConstants.USED) || ipStatus.equals(TraceOrgCommonConstants.AVAILABLE) || ipStatus.equals(TraceOrgCommonConstants.TRANSIENT) || ipStatus.equals(TraceOrgCommonConstants.RESERVED))
                {
                    switch (exportTimeline)
                    {
                        case 1 :
                            subnetIpDetailsList = (List<TraceOrgSubnetIpDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_SUBNET_IP_DETAILS + " where Date(modifiedDate) = CURDATE() and  deactiveStatus = false and subnetId in ("+subnetId+") and status= '"+ipStatus+"' order by INET_ATON(ipAddress) ");
                            break;
                        case 2 :
                            subnetIpDetailsList = (List<TraceOrgSubnetIpDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_SUBNET_IP_DETAILS + " where DATE(modifiedDate) = DATE(CURDATE() -1) and  deactiveStatus = false and subnetId in ("+subnetId+") and status= '"+ipStatus+"' order by INET_ATON(ipAddress) ");
                            break;
                        case 3 :
                            subnetIpDetailsList = (List<TraceOrgSubnetIpDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_SUBNET_IP_DETAILS + " where WEEK(modifiedDate) =  WEEK(curdate()) and  deactiveStatus = false and YEAR(modifiedDate) = YEAR(CURDATE()) and subnetId in ("+subnetId+") and status= '"+ipStatus+"' order by INET_ATON(ipAddress) ");
                            break;
                        case 4 :
                            subnetIpDetailsList = (List<TraceOrgSubnetIpDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_SUBNET_IP_DETAILS + " where MONTH(modifiedDate)= MONTH(curdate()) and  deactiveStatus = false and YEAR(modifiedDate) = YEAR(CURDATE()) and subnetId in ("+subnetId+") and status= '"+ipStatus+"' order by INET_ATON(ipAddress) ");
                            break;
                        case 5 :
                            subnetIpDetailsList = (List<TraceOrgSubnetIpDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_SUBNET_IP_DETAILS + " where QUARTER(modifiedDate) = QUARTER(curdate()) and  deactiveStatus = false and YEAR(modifiedDate) = YEAR(CURDATE()) and subnetId in ("+subnetId+") and status= '"+ipStatus+"' order by INET_ATON(ipAddress)");
                            break;
                        case 6 :
                            subnetIpDetailsList = (List<TraceOrgSubnetIpDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_SUBNET_IP_DETAILS + " where QUARTER(modifiedDate) = QUARTER(curdate()) - 1 and  deactiveStatus = false and YEAR(modifiedDate) = YEAR(CURDATE()) and subnetId in ("+subnetId+") and status= '"+ipStatus+"' order by INET_ATON(ipAddress)");
                            break;
                        case 7 :
                            subnetIpDetailsList = (List<TraceOrgSubnetIpDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_SUBNET_IP_DETAILS + " where TIMESTAMPDIFF(MONTH, modifiedDate, NOW()) < 6 and  deactiveStatus = false and subnetId in ("+subnetId+") and status= '"+ipStatus+"'  order by INET_ATON(ipAddress)");
                            break;
                        case 8 :
                            subnetIpDetailsList = (List<TraceOrgSubnetIpDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_SUBNET_IP_DETAILS + " where TIMESTAMPDIFF(YEAR, modifiedDate, NOW()) < 1 and  deactiveStatus = false and subnetId in ("+subnetId+") and status= '"+ipStatus+"' order by INET_ATON(ipAddress)");
                            break;
                        case 9 :
                            subnetIpDetailsList = (List<TraceOrgSubnetIpDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_SUBNET_IP_DETAILS + " where YEAR(modifiedDate) = YEAR(curdate()) - 1 and  deactiveStatus = false and subnetId in ("+subnetId+") and status= '"+ipStatus+"' order by INET_ATON(ipAddress)");
                            break;
                        case 10 :
                            subnetIpDetailsList = (List<TraceOrgSubnetIpDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_SUBNET_IP_DETAILS + " where subnetId in ("+subnetId+") and  deactiveStatus = false and status= '"+ipStatus+"' order by INET_ATON(ipAddress)");
                            break;
                        case 11 :
                            subnetIpDetailsList = (List<TraceOrgSubnetIpDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_SUBNET_IP_DETAILS + " where WEEK(modifiedDate) =  WEEK(curdate()) - 1 and  deactiveStatus = false and YEAR(modifiedDate) = YEAR(CURDATE()) and subnetId in ("+subnetId+") and status= '"+ipStatus+"' order by INET_ATON(ipAddress)");
                            break;
                        case 12 :
                            subnetIpDetailsList = (List<TraceOrgSubnetIpDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_SUBNET_IP_DETAILS + " where MONTH(modifiedDate)= MONTH(curdate()) - 1 and  deactiveStatus = false and YEAR(modifiedDate) = YEAR(CURDATE()) and subnetId in ("+subnetId+") and status= '"+ipStatus+"' order by INET_ATON(ipAddress)");
                            break;
                        default:
                            subnetIpDetailsList = null;
                            break;
                    }
                }
                else if(ipStatus.equalsIgnoreCase("ALL IP"))
                {
                    switch (exportTimeline)
                    {
                        case 1 :
                            subnetIpDetailsList = (List<TraceOrgSubnetIpDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_SUBNET_IP_DETAILS + " where Date(modifiedDate) = CURDATE() and  deactiveStatus = false and subnetId in ("+subnetId+") order by INET_ATON(ipAddress) ");
                            break;
                        case 2 :
                            subnetIpDetailsList = (List<TraceOrgSubnetIpDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_SUBNET_IP_DETAILS + " where DATE(modifiedDate) = DATE(CURDATE() -1) and  deactiveStatus = false and subnetId in ("+subnetId+") order by INET_ATON(ipAddress) ");
                            break;
                        case 3 :
                            subnetIpDetailsList = (List<TraceOrgSubnetIpDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_SUBNET_IP_DETAILS + " where WEEK(modifiedDate) =  WEEK(curdate()) and  deactiveStatus = false and YEAR(modifiedDate) = YEAR(CURDATE()) and subnetId in ("+subnetId+") order by INET_ATON(ipAddress) ");
                            break;
                        case 4 :
                            subnetIpDetailsList = (List<TraceOrgSubnetIpDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_SUBNET_IP_DETAILS + " where MONTH(modifiedDate)= MONTH(curdate()) and  deactiveStatus = false and YEAR(modifiedDate) = YEAR(CURDATE()) and subnetId in ("+subnetId+") order by INET_ATON(ipAddress) ");
                            break;
                        case 5 :
                            subnetIpDetailsList = (List<TraceOrgSubnetIpDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_SUBNET_IP_DETAILS + " where QUARTER(modifiedDate) = QUARTER(curdate()) and  deactiveStatus = false and YEAR(modifiedDate) = YEAR(CURDATE()) and subnetId in ("+subnetId+") order by INET_ATON(ipAddress)");
                            break;
                        case 6 :
                            subnetIpDetailsList = (List<TraceOrgSubnetIpDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_SUBNET_IP_DETAILS + " where QUARTER(modifiedDate) = QUARTER(curdate()) - 1 and  deactiveStatus = false and YEAR(modifiedDate) = YEAR(CURDATE()) and subnetId in ("+subnetId+") order by INET_ATON(ipAddress)");
                            break;
                        case 7 :
                            subnetIpDetailsList = (List<TraceOrgSubnetIpDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_SUBNET_IP_DETAILS + " where TIMESTAMPDIFF(MONTH, modifiedDate, NOW()) < 6 and  deactiveStatus = false and subnetId in ("+subnetId+")  order by INET_ATON(ipAddress)");
                            break;
                        case 8 :
                            subnetIpDetailsList = (List<TraceOrgSubnetIpDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_SUBNET_IP_DETAILS + " where TIMESTAMPDIFF(YEAR, modifiedDate, NOW()) < 1 and  deactiveStatus = false and subnetId in ("+subnetId+")  order by INET_ATON(ipAddress)");
                            break;
                        case 9 :
                            subnetIpDetailsList = (List<TraceOrgSubnetIpDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_SUBNET_IP_DETAILS + " where YEAR(modifiedDate) = YEAR(curdate()) - 1 and  deactiveStatus = false and subnetId in ("+subnetId+") order by INET_ATON(ipAddress)");
                            break;
                        case 10 :
                            subnetIpDetailsList = (List<TraceOrgSubnetIpDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_SUBNET_IP_DETAILS + " where subnetId in ("+subnetId+") and  deactiveStatus = false order by INET_ATON(ipAddress)");
                            break;
                        case 11 :
                            subnetIpDetailsList = (List<TraceOrgSubnetIpDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_SUBNET_IP_DETAILS + " where WEEK(modifiedDate) =  WEEK(curdate()) - 1 and  deactiveStatus = false and YEAR(modifiedDate) = YEAR(CURDATE()) and subnetId in ("+subnetId+") order by INET_ATON(ipAddress)");
                            break;
                        case 12 :
                            subnetIpDetailsList = (List<TraceOrgSubnetIpDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_SUBNET_IP_DETAILS + " where MONTH(modifiedDate)= MONTH(curdate()) - 1 and  deactiveStatus = false and YEAR(modifiedDate) = YEAR(CURDATE()) and subnetId in ("+subnetId+") order by INET_ATON(ipAddress)");
                            break;
                        default:
                            subnetIpDetailsList = null;
                            break;
                    }
                }
                else if (ipStatus.equalsIgnoreCase("ROGUE IP"))
                {
                    switch (exportTimeline)
                    {
                        case 1 :
                            subnetIpDetailsList = (List<TraceOrgSubnetIpDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_SUBNET_IP_DETAILS + " where Date(modifiedDate) = CURDATE() and  deactiveStatus = false and subnetId in ("+subnetId+") and authenticity = 'rogue'  order by INET_ATON(ipAddress) ");
                            break;
                        case 2 :
                            subnetIpDetailsList = (List<TraceOrgSubnetIpDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_SUBNET_IP_DETAILS + " where DATE(modifiedDate) = DATE(CURDATE() -1) and  deactiveStatus = false and subnetId in ("+subnetId+") and authenticity = 'rogue'  order by INET_ATON(ipAddress) ");
                            break;
                        case 3 :
                            subnetIpDetailsList = (List<TraceOrgSubnetIpDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_SUBNET_IP_DETAILS + " where WEEK(modifiedDate) =  WEEK(curdate()) and  deactiveStatus = false and YEAR(modifiedDate) = YEAR(CURDATE()) and subnetId in ("+subnetId+") and authenticity = 'rogue' order by INET_ATON(ipAddress) ");
                            break;
                        case 4 :
                            subnetIpDetailsList = (List<TraceOrgSubnetIpDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_SUBNET_IP_DETAILS + " where MONTH(modifiedDate)= MONTH(curdate()) and  deactiveStatus = false and YEAR(modifiedDate) = YEAR(CURDATE()) and subnetId in ("+subnetId+") and authenticity = 'rogue' order by INET_ATON(ipAddress) ");
                            break;
                        case 5 :
                            subnetIpDetailsList = (List<TraceOrgSubnetIpDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_SUBNET_IP_DETAILS + " where QUARTER(modifiedDate) = QUARTER(curdate()) and  deactiveStatus = false and YEAR(modifiedDate) = YEAR(CURDATE()) and subnetId in ("+subnetId+") and authenticity = 'rogue' order by INET_ATON(ipAddress)");
                            break;
                        case 6 :
                            subnetIpDetailsList = (List<TraceOrgSubnetIpDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_SUBNET_IP_DETAILS + " where QUARTER(modifiedDate) = QUARTER(curdate()) - 1 and  deactiveStatus = false and YEAR(modifiedDate) = YEAR(CURDATE()) and subnetId in ("+subnetId+") and authenticity = 'rogue' order by INET_ATON(ipAddress)");
                            break;
                        case 7 :
                            subnetIpDetailsList = (List<TraceOrgSubnetIpDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_SUBNET_IP_DETAILS + " where TIMESTAMPDIFF(MONTH, modifiedDate, NOW()) < 6 and  deactiveStatus = false and subnetId in ("+subnetId+") and authenticity = 'rogue'  order by INET_ATON(ipAddress)");
                            break;
                        case 8 :
                            subnetIpDetailsList = (List<TraceOrgSubnetIpDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_SUBNET_IP_DETAILS + " where TIMESTAMPDIFF(YEAR, modifiedDate, NOW()) < 1 and  deactiveStatus = false and subnetId in ("+subnetId+") and authenticity = 'rogue'  order by INET_ATON(ipAddress)");
                            break;
                        case 9 :
                            subnetIpDetailsList = (List<TraceOrgSubnetIpDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_SUBNET_IP_DETAILS + " where YEAR(modifiedDate) = YEAR(curdate()) - 1 and  deactiveStatus = false and subnetId in ("+subnetId+") and authenticity = 'rogue' order by INET_ATON(ipAddress)");
                            break;
                        case 10 :
                            subnetIpDetailsList = (List<TraceOrgSubnetIpDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_SUBNET_IP_DETAILS + " where subnetId in ("+subnetId+") and  deactiveStatus = false and authenticity = 'rogue' order by INET_ATON(ipAddress)");
                            break;
                        case 11 :
                            subnetIpDetailsList = (List<TraceOrgSubnetIpDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_SUBNET_IP_DETAILS + " where WEEK(modifiedDate) =  WEEK(curdate()) - 1 and  deactiveStatus = false and YEAR(modifiedDate) = YEAR(CURDATE()) and subnetId in ("+subnetId+") and authenticity = 'rogue' order by INET_ATON(ipAddress)");
                            break;
                        case 12 :
                            subnetIpDetailsList = (List<TraceOrgSubnetIpDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_SUBNET_IP_DETAILS + " where MONTH(modifiedDate)= MONTH(curdate()) - 1 and  deactiveStatus = false and YEAR(modifiedDate) = YEAR(CURDATE()) and subnetId in ("+subnetId+") and authenticity = 'rogue' order by INET_ATON(ipAddress)");
                            break;
                        default:
                            subnetIpDetailsList = null;
                            break;
                    }
                }
                else if (ipStatus.equalsIgnoreCase("TRUSTED IP"))
                {
                    switch (exportTimeline)
                    {
                        case 1 :
                            subnetIpDetailsList = (List<TraceOrgSubnetIpDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_SUBNET_IP_DETAILS + " where Date(modifiedDate) = CURDATE() and  deactiveStatus = false and subnetId in ("+subnetId+") and authenticity = 'trusted'  order by INET_ATON(ipAddress) ");
                            break;
                        case 2 :
                            subnetIpDetailsList = (List<TraceOrgSubnetIpDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_SUBNET_IP_DETAILS + " where DATE(modifiedDate) = DATE(CURDATE() -1) and  deactiveStatus = false and subnetId in ("+subnetId+") and authenticity = 'trusted'  order by INET_ATON(ipAddress) ");
                            break;
                        case 3 :
                            subnetIpDetailsList = (List<TraceOrgSubnetIpDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_SUBNET_IP_DETAILS + " where WEEK(modifiedDate) =  WEEK(curdate()) and  deactiveStatus = false and YEAR(modifiedDate) = YEAR(CURDATE()) and subnetId in ("+subnetId+") and authenticity = 'trusted' order by INET_ATON(ipAddress) ");
                            break;
                        case 4 :
                            subnetIpDetailsList = (List<TraceOrgSubnetIpDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_SUBNET_IP_DETAILS + " where MONTH(modifiedDate)= MONTH(curdate()) and  deactiveStatus = false and YEAR(modifiedDate) = YEAR(CURDATE()) and subnetId in ("+subnetId+") and authenticity = 'trusted' order by INET_ATON(ipAddress) ");
                            break;
                        case 5 :
                            subnetIpDetailsList = (List<TraceOrgSubnetIpDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_SUBNET_IP_DETAILS + " where QUARTER(modifiedDate) = QUARTER(curdate()) and  deactiveStatus = false and YEAR(modifiedDate) = YEAR(CURDATE()) and subnetId in ("+subnetId+") and authenticity = 'trusted' order by INET_ATON(ipAddress)");
                            break;
                        case 6 :
                            subnetIpDetailsList = (List<TraceOrgSubnetIpDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_SUBNET_IP_DETAILS + " where QUARTER(modifiedDate) = QUARTER(curdate()) - 1 and  deactiveStatus = false and YEAR(modifiedDate) = YEAR(CURDATE()) and subnetId in ("+subnetId+") and authenticity = 'trusted' order by INET_ATON(ipAddress)");
                            break;
                        case 7 :
                            subnetIpDetailsList = (List<TraceOrgSubnetIpDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_SUBNET_IP_DETAILS + " where TIMESTAMPDIFF(MONTH, modifiedDate, NOW()) < 6 and  deactiveStatus = false and subnetId in ("+subnetId+") and authenticity = 'trusted'  order by INET_ATON(ipAddress)");
                            break;
                        case 8 :
                            subnetIpDetailsList = (List<TraceOrgSubnetIpDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_SUBNET_IP_DETAILS + " where TIMESTAMPDIFF(YEAR, modifiedDate, NOW()) < 1 and  deactiveStatus = false and subnetId in ("+subnetId+") and authenticity = 'trusted'  order by INET_ATON(ipAddress)");
                            break;
                        case 9 :
                            subnetIpDetailsList = (List<TraceOrgSubnetIpDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_SUBNET_IP_DETAILS + " where YEAR(modifiedDate) = YEAR(curdate()) - 1 and  deactiveStatus = false and subnetId in ("+subnetId+") and authenticity = 'trusted' order by INET_ATON(ipAddress)");
                            break;
                        case 10 :
                            subnetIpDetailsList = (List<TraceOrgSubnetIpDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_SUBNET_IP_DETAILS + " where subnetId in ("+subnetId+") and  deactiveStatus = false and authenticity = 'trusted' order by INET_ATON(ipAddress)");
                            break;
                        case 11 :
                            subnetIpDetailsList = (List<TraceOrgSubnetIpDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_SUBNET_IP_DETAILS + " where WEEK(modifiedDate) =  WEEK(curdate()) - 1 and  deactiveStatus = false and YEAR(modifiedDate) = YEAR(CURDATE()) and subnetId in ("+subnetId+") and authenticity = 'trusted' order by INET_ATON(ipAddress)");
                            break;
                        case 12 :
                            subnetIpDetailsList = (List<TraceOrgSubnetIpDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_SUBNET_IP_DETAILS + " where MONTH(modifiedDate)= MONTH(curdate()) - 1 and  deactiveStatus = false and YEAR(modifiedDate) = YEAR(CURDATE()) and subnetId in ("+subnetId+") and authenticity = 'trusted' order by INET_ATON(ipAddress)");
                            break;
                        default:
                            subnetIpDetailsList = null;
                            break;
                    }
                }
                else if(ipStatus.equalsIgnoreCase("VENDOR SUMMARY"))
                {
                    List<Object> vendorList = null;

                    switch (exportTimeline)
                    {
                        case 1 :
                            vendorList = (List<Object>) this.traceOrgService.commonQuery(TraceOrgCommonConstants.SELECT_VENDOR_WITH_COUNT_FOR_REPORT.replace("subnetIdValue",subnetId),"TraceOrgSubnetIpDetails where deactiveStatus = false and subnetId in("+subnetId+")  and Date(modifiedDate) = CURDATE() and YEAR(modifiedDate) = YEAR(CURDATE()) group by deviceType order by devicenumber desc");
                            break;
                        case 2 :
                            vendorList = (List<Object>) this.traceOrgService.commonQuery(TraceOrgCommonConstants.SELECT_VENDOR_WITH_COUNT_FOR_REPORT.replace("subnetIdValue",subnetId),"TraceOrgSubnetIpDetails where deactiveStatus = false and subnetId in("+subnetId+")  and DATE(modifiedDate) = DATE(CURDATE() -1) and YEAR(modifiedDate) = YEAR(CURDATE()) group by deviceType order by devicenumber desc");
                            break;
                        case 3 :
                            vendorList = (List<Object>) this.traceOrgService.commonQuery(TraceOrgCommonConstants.SELECT_VENDOR_WITH_COUNT_FOR_REPORT.replace("subnetIdValue",subnetId),"TraceOrgSubnetIpDetails where deactiveStatus = false and subnetId in("+subnetId+")  and WEEK(modifiedDate) =  WEEK(curdate()) and YEAR(modifiedDate) = YEAR(CURDATE()) group by deviceType order by devicenumber desc");
                            break;
                        case 4 :
                            vendorList = (List<Object>) this.traceOrgService.commonQuery(TraceOrgCommonConstants.SELECT_VENDOR_WITH_COUNT_FOR_REPORT.replace("subnetIdValue",subnetId),"TraceOrgSubnetIpDetails where deactiveStatus = false and subnetId in("+subnetId+")  and MONTH(modifiedDate)= MONTH(curdate()) and YEAR(modifiedDate) = YEAR(CURDATE()) group by deviceType order by devicenumber desc");
                            break;
                        case 5 :
                            vendorList = (List<Object>) this.traceOrgService.commonQuery(TraceOrgCommonConstants.SELECT_VENDOR_WITH_COUNT_FOR_REPORT.replace("subnetIdValue",subnetId),"TraceOrgSubnetIpDetails where deactiveStatus = false and subnetId in("+subnetId+")  and QUARTER(modifiedDate) = QUARTER(curdate()) and YEAR(modifiedDate) = YEAR(CURDATE()) group by deviceType order by devicenumber desc");
                            break;
                        case 6 :
                            vendorList = (List<Object>) this.traceOrgService.commonQuery(TraceOrgCommonConstants.SELECT_VENDOR_WITH_COUNT_FOR_REPORT.replace("subnetIdValue",subnetId),"TraceOrgSubnetIpDetails where deactiveStatus = false and subnetId in("+subnetId+")  and QUARTER(modifiedDate) = QUARTER(curdate()) - 1 and YEAR(modifiedDate) = YEAR(CURDATE()) group by deviceType order by devicenumber desc");
                            break;
                        case 7 :
                            vendorList = (List<Object>) this.traceOrgService.commonQuery(TraceOrgCommonConstants.SELECT_VENDOR_WITH_COUNT_FOR_REPORT.replace("subnetIdValue",subnetId),"TraceOrgSubnetIpDetails where deactiveStatus = false and subnetId in("+subnetId+")  and TIMESTAMPDIFF(MONTH, modifiedDate, NOW()) < 6  group by deviceType order by devicenumber desc");
                            break;
                        case 8 :
                            vendorList = (List<Object>) this.traceOrgService.commonQuery(TraceOrgCommonConstants.SELECT_VENDOR_WITH_COUNT_FOR_REPORT.replace("subnetIdValue",subnetId),"TraceOrgSubnetIpDetails where deactiveStatus = false and subnetId in("+subnetId+")  and TIMESTAMPDIFF(YEAR, modifiedDate, NOW()) < 1 group by deviceType order by devicenumber desc");
                            break;
                        case 9 :
                            vendorList = (List<Object>) this.traceOrgService.commonQuery(TraceOrgCommonConstants.SELECT_VENDOR_WITH_COUNT_FOR_REPORT.replace("subnetIdValue",subnetId),"TraceOrgSubnetIpDetails where deactiveStatus = false and subnetId in("+subnetId+")  and YEAR(modifiedDate) = YEAR(curdate()) - 1 group by deviceType order by devicenumber desc");
                            break;
                        case 10 :
                            vendorList = (List<Object>) this.traceOrgService.commonQuery(TraceOrgCommonConstants.SELECT_VENDOR_WITH_COUNT_FOR_REPORT.replace("subnetIdValue",subnetId),"TraceOrgSubnetIpDetails where deactiveStatus = false and subnetId in("+subnetId+")  group by deviceType order by devicenumber desc");
                            break;
                        case 11 :
                            vendorList = (List<Object>) this.traceOrgService.commonQuery(TraceOrgCommonConstants.SELECT_VENDOR_WITH_COUNT_FOR_REPORT.replace("subnetIdValue",subnetId),"TraceOrgSubnetIpDetails where deactiveStatus = false and subnetId in("+subnetId+")  and WEEK(modifiedDate) =  WEEK(curdate()) - 1 and YEAR(modifiedDate) = YEAR(CURDATE()) group by deviceType order by devicenumber desc");
                            break;
                        case 12 :
                            vendorList = (List<Object>) this.traceOrgService.commonQuery(TraceOrgCommonConstants.SELECT_VENDOR_WITH_COUNT_FOR_REPORT.replace("subnetIdValue",subnetId),"TraceOrgSubnetIpDetails where deactiveStatus = false and subnetId in("+subnetId+")  and MONTH(modifiedDate)= MONTH(curdate()) - 1 and YEAR(modifiedDate) = YEAR(CURDATE()) group by deviceType order by devicenumber desc");
                            break;
                        default:
                            subnetIpDetailsList = null;
                            break;
                    }

                    if(vendorList != null && !vendorList.isEmpty())
                    {
                        Integer totalCount = 0;

                        for(Object vendorOutputs : vendorList)
                        {
                            Map<String,Object> vendorDetails = new HashMap<>();

                            Gson gson= new Gson();

                            Type listType = new TypeToken<List<String>>() {}.getType();

                            List<String> vendorOutputsList = gson.fromJson(gson.toJson(vendorOutputs), listType);

                            totalCount = totalCount + Integer.parseInt(vendorOutputsList.get(0));
                        }

                        for(Object vendorOutputs : vendorList)
                        {
                            Map<String,Object> vendorDetails = new HashMap<>();

                            Gson gson= new Gson();

                            Type listType = new TypeToken<List<String>>() {}.getType();

                            List<String> vendorOutputsList = gson.fromJson(gson.toJson(vendorOutputs), listType);

                            DecimalFormat decimalFormat = new DecimalFormat();

                            decimalFormat.setMaximumFractionDigits(2);

                            if(vendorOutputsList !=null && !vendorOutputsList.isEmpty())
                            {
                                if(vendorOutputsList.get(1) == null)
                                {
                                    vendorDetails.put(TraceOrgCommonConstants.VENDOR_NAME,"Others");
                                }
                                else
                                {
                                    vendorDetails.put(TraceOrgCommonConstants.VENDOR_NAME,vendorOutputsList.get(1));
                                }
                                vendorDetails.put(TraceOrgCommonConstants.VENDOR_COUNT,Long.parseLong(vendorOutputsList.get(0)));

                                vendorDetails.put(TraceOrgCommonConstants.VENDOR_PERCENTAGE,decimalFormat.format((float)(Long.parseLong(vendorOutputsList.get(0))*100)/totalCount));
                            }
                            if(vendorDetails!=null && !vendorDetails.isEmpty())
                            {
                                vendorSummaryList.add(vendorDetails);
                            }
                        }
                    }
                }

                if(!ipStatus.equalsIgnoreCase("VENDOR SUMMARY"))
                {
                    if(subnetIpDetailsList!=null && !subnetIpDetailsList.isEmpty())
                    {
                        subnetIpDetailsList.forEach(subnetIpDetails->{
                            subnetIpDetails.setSubnetName(subnetIpDetails.getSubnetId().getSubnetName());
                        });

                        response.setData(subnetIpDetailsList);

                        response.setSuccess(TraceOrgCommonConstants.TRUE);
                    }
                    else
                    {
                        response.setSuccess(TraceOrgCommonConstants.FALSE);

                        response.setMessage(TraceOrgMessageConstants.NO_DATA_AVAILABLE);
                    }
                }
                else
                {
                    if(vendorSummaryList!=null && !vendorSummaryList.isEmpty())
                    {
                        response.setData(vendorSummaryList);

                        response.setSuccess(TraceOrgCommonConstants.TRUE);
                    }
                    else
                    {
                        response.setSuccess(TraceOrgCommonConstants.FALSE);

                        response.setMessage(TraceOrgMessageConstants.NO_DATA_AVAILABLE);
                    }
                }
            }
            catch (Exception exception)
            {
                _logger.error(exception);

                response.setSuccess(TraceOrgCommonConstants.FALSE);

                response.setMessage(TraceOrgMessageConstants.SOMETHING_WENT_WRONG);
            }
        }
        else
        {
            response.setSuccess(TraceOrgCommonConstants.FALSE);

            response.setMessage(TraceOrgMessageConstants.ENTER_VALID_DETAILS);
        }

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @RequestMapping(value = TraceOrgCommonConstants.SUBNET_ROGUE_IP_BY_REPORT_TIMELINE, method = RequestMethod.GET)
    @PreAuthorize("hasAuthority('PERM_DASHBOARD_READ')")
    public ResponseEntity<?> listSubnetRogueIpReportByTimeline(HttpServletRequest request,@RequestParam Long subnetId,@RequestParam Integer exportTimeline)
    {
        Response response = new Response();

        if(subnetId !=null && exportTimeline!=null)
        {
            try
            {
                String accessToken = request.getHeader(TraceOrgCommonConstants.ACCESSTOKEN);

                List<TraceOrgSubnetIpDetails> subnetIpDetailsList = null ;

                switch (exportTimeline)
                {
                    case 1 :
                        subnetIpDetailsList = (List<TraceOrgSubnetIpDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_SUBNET_IP_DETAILS + " where rogueStatus = true and  Date(modifiedDate) = CURDATE() and subnetId = '"+subnetId+"' ");
                        break;
                    case 2 :
                        subnetIpDetailsList = (List<TraceOrgSubnetIpDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_SUBNET_IP_DETAILS + " where rogueStatus = true and DATE(modifiedDate) = DATE(CURDATE() -1) and subnetId = '"+subnetId+"' ");
                        break;
                    case 3 :
                        subnetIpDetailsList = (List<TraceOrgSubnetIpDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_SUBNET_IP_DETAILS + " where rogueStatus = true and WEEK(modifiedDate) =  WEEK(curdate()) and YEAR(modifiedDate) = YEAR(CURDATE()) and subnetId = '"+subnetId+"' ");
                        break;
                    case 4 :
                        subnetIpDetailsList = (List<TraceOrgSubnetIpDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_SUBNET_IP_DETAILS + " where rogueStatus = true and MONTH(modifiedDate)= MONTH(curdate()) and YEAR(modifiedDate) = YEAR(CURDATE()) and subnetId = '"+subnetId+"' ");
                        break;
                    case 5 :
                        subnetIpDetailsList = (List<TraceOrgSubnetIpDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_SUBNET_IP_DETAILS + " where rogueStatus = true and QUARTER(modifiedDate) = QUARTER(curdate()) and YEAR(modifiedDate) = YEAR(CURDATE()) and subnetId = '"+subnetId+"' ");
                        break;
                    case 6 :
                        subnetIpDetailsList = (List<TraceOrgSubnetIpDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_SUBNET_IP_DETAILS + " where rogueStatus = true and QUARTER(modifiedDate) = QUARTER(curdate()) - 1 and YEAR(modifiedDate) = YEAR(CURDATE()) and subnetId = '"+subnetId+"' ");
                        break;
                    case 7 :
                        subnetIpDetailsList = (List<TraceOrgSubnetIpDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_SUBNET_IP_DETAILS + " where rogueStatus = true and TIMESTAMPDIFF(MONTH, modifiedDate, NOW()) < 6 and subnetId = '"+subnetId+"'  ");
                        break;
                    case 8 :
                        subnetIpDetailsList = (List<TraceOrgSubnetIpDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_SUBNET_IP_DETAILS + " where rogueStatus = true and TIMESTAMPDIFF(YEAR, modifiedDate, NOW()) < 1 and subnetId = '"+subnetId+"' ");
                        break;
                    case 9 :
                        subnetIpDetailsList = (List<TraceOrgSubnetIpDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_SUBNET_IP_DETAILS + " where rogueStatus = true and YEAR(modifiedDate) = YEAR(curdate()) - 1 and subnetId = '"+subnetId+"' ");
                        break;
                    case 10 :
                        subnetIpDetailsList = (List<TraceOrgSubnetIpDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_SUBNET_IP_DETAILS + " where rogueStatus = true and subnetId = '"+subnetId+"'");
                        break;
                    case 11 :
                        subnetIpDetailsList = (List<TraceOrgSubnetIpDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_SUBNET_IP_DETAILS + " where rogueStatus = true and WEEK(modifiedDate) =  WEEK(curdate()) - 1 and YEAR(modifiedDate) = YEAR(CURDATE()) and subnetId = '"+subnetId+"' ");
                        break;
                    case 12 :
                        subnetIpDetailsList = (List<TraceOrgSubnetIpDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_SUBNET_IP_DETAILS + " where rogueStatus = true and MONTH(modifiedDate)= MONTH(curdate()) - 1 and YEAR(modifiedDate) = YEAR(CURDATE()) and subnetId = '"+subnetId+"' ");
                        break;
                    default:
                        subnetIpDetailsList = null;
                        break;
                }

                if(subnetIpDetailsList!=null && !subnetIpDetailsList.isEmpty())
                {
                    response.setData(subnetIpDetailsList);

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
        }
        else
        {
            response.setSuccess(TraceOrgCommonConstants.FALSE);

            response.setMessage(TraceOrgMessageConstants.ENTER_VALID_DETAILS);
        }

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @RequestMapping(value = TraceOrgCommonConstants.SUBNET_CATEGORY_REST_URL, method = RequestMethod.POST)
    @PreAuthorize("hasAuthority('PERM_DASHBOARD_WRITE')")
    public ResponseEntity<?> updateSubnetCategory(@RequestParam Long subnetId,HttpServletRequest request, @RequestParam Long categoryId)
    {
        String accessToken = request.getHeader(TraceOrgCommonConstants.ACCESSTOKEN);

        Response response = new Response();

        try
        {
            if(subnetId!=null && categoryId!=null)
            {
                TraceOrgSubnetDetails traceOrgSubnetDetails = (TraceOrgSubnetDetails )this.traceOrgService.getById(TraceOrgCommonConstants.TRACE_ORG_SUBNET_DETAILS,subnetId);

                if(traceOrgSubnetDetails!=null)
                {
                    TraceOrgCategory traceOrgCategory = (TraceOrgCategory)this.traceOrgService.getById(TraceOrgCommonConstants.TRACE_ORG_CATEGORY,categoryId);

                    if(traceOrgCategory!=null)
                    {
                        traceOrgSubnetDetails.setTraceOrgCategory(traceOrgCategory);

                        this.traceOrgService.insert(traceOrgSubnetDetails);

                        response.setSuccess(TraceOrgCommonConstants.TRUE);

                        response.setMessage(TraceOrgMessageConstants.SUBNET_UPDATE_SUCCESS);

                        _logger.debug("category "+traceOrgCategory.getCategoryName()+ " is updated for subnet "+traceOrgSubnetDetails.getSubnetAddress());
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
            response.setSuccess(TraceOrgCommonConstants.FALSE);

            response.setMessage(TraceOrgMessageConstants.ENTER_VALID_DETAILS);
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
