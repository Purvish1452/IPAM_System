package com.motadata.traceorg.ipam.controller.dhcp;

import com.motadata.traceorg.ipam.logger.TraceOrgLogger;
import com.motadata.traceorg.ipam.entity.*;
import com.motadata.traceorg.ipam.entity.dashboard.TraceOrgCategory;
import com.motadata.traceorg.ipam.entity.dhcp.TraceOrgDhcpCredentialDetails;
import com.motadata.traceorg.ipam.entity.event.TraceOrgEvent;
import com.motadata.traceorg.ipam.entity.subnet.TraceOrgSubnetDetails;
import com.motadata.traceorg.ipam.repository.rogueDetection.TraceOrgRogueDetectionRepository;
import com.motadata.traceorg.ipam.scheduler.dhcp.TraceOrgScanDhcpSchedulerJob;
import com.motadata.traceorg.ipam.services.TraceOrgService;
import com.motadata.traceorg.ipam.services.alert.TraceOrgAlertService;
import com.motadata.traceorg.ipam.services.supernet.TraceOrgSupernetService;
import com.motadata.traceorg.ipam.services.messaging.TraceOrgMessageSender;
import com.motadata.traceorg.ipam.util.*;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

@SuppressWarnings({"ALL"})
@RestController
public class TraceOrgDhcpCredentialController
{
    @Autowired
    private TraceOrgService traceOrgService;

    @Autowired
    private TraceOrgAlertService traceOrgAlertService;

    @Autowired
    private TraceOrgCommonUtil traceOrgCommonUtil;

    @Autowired
    private TraceOrgRogueDetectionRepository traceOrgRogueDetectionRepository;

    @Autowired
    private TraceOrgCiscoDHCPServerUtil traceOrgCiscoDHCPServerUtil;

    @Autowired
    private TraceOrgWindowsDhcpServerUtil traceOrgWindowsDhcpServerUtil;

    @Autowired
    private TraceOrgSupernetService traceOrgSupernetService;

    private static final TraceOrgLogger _logger = new TraceOrgLogger(TraceOrgDhcpCredentialController.class, "DHCP Credential Controller");

    @RequestMapping(value = TraceOrgCommonConstants.DHCP_CREDENTIAL_REST_URL, method = RequestMethod.GET)
    @PreAuthorize("hasAuthority('PERM_SETTINGS_READ')")
    public ResponseEntity<?> listDhcpCredentialDetails(HttpServletRequest request)
    {
        Response response = new Response();

        try
        {
            String accessToken = request.getHeader(TraceOrgCommonConstants.ACCESSTOKEN);

            List<TraceOrgDhcpCredentialDetails> traceOrgDhcpCredentialDetails = (List<TraceOrgDhcpCredentialDetails>) this.traceOrgService.commonQuery("",TraceOrgCommonConstants.TRACE_ORG_DHCP_CREDENTIAL);

            if(traceOrgDhcpCredentialDetails !=null && !traceOrgDhcpCredentialDetails.isEmpty())
            {
                response.setData(traceOrgDhcpCredentialDetails);

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

    @RequestMapping(value = TraceOrgCommonConstants.CISCO_DHCP_CREDENTIAL_REST_URL, method = RequestMethod.GET)
    @PreAuthorize("hasAuthority('PERM_SETTINGS_READ')")
    public ResponseEntity<?> listCiscoDhcpCredentialDetails(HttpServletRequest request)
    {
        Response response = new Response();

        try
        {
            String accessToken = request.getHeader(TraceOrgCommonConstants.ACCESSTOKEN);

            List<TraceOrgDhcpCredentialDetails> traceOrgDhcpCredentialDetails = new ArrayList<>();

            traceOrgDhcpCredentialDetails = (List<TraceOrgDhcpCredentialDetails>) this.traceOrgService.commonQuery("",TraceOrgCommonConstants.TRACE_ORG_DHCP_CREDENTIAL+" where type = 'cisco'");

            if(traceOrgDhcpCredentialDetails !=null && !traceOrgDhcpCredentialDetails.isEmpty())
            {

                TraceOrgDhcpCredentialDetails traceOrgDhcpCredentialDetail = new TraceOrgDhcpCredentialDetails();

                traceOrgDhcpCredentialDetail.setCredentialName("New Credential");

                traceOrgDhcpCredentialDetail.setType("Cisco");

                traceOrgDhcpCredentialDetails.add(0,traceOrgDhcpCredentialDetail);

                response.setData(traceOrgDhcpCredentialDetails);

                response.setSuccess(TraceOrgCommonConstants.TRUE);
            }
            else
            {
                TraceOrgDhcpCredentialDetails traceOrgDhcpCredentialDetail = new TraceOrgDhcpCredentialDetails();

                traceOrgDhcpCredentialDetail.setCredentialName("New Credential");

                traceOrgDhcpCredentialDetail.setType("Cisco");

                traceOrgDhcpCredentialDetails.add(0,traceOrgDhcpCredentialDetail);

                response.setSuccess(TraceOrgCommonConstants.TRUE);

                response.setData(traceOrgDhcpCredentialDetails);
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

    @RequestMapping(value = TraceOrgCommonConstants.WINDOWS_DHCP_CREDENTIAL_REST_URL, method = RequestMethod.GET)
    @PreAuthorize("hasAuthority('PERM_SETTINGS_READ')")
    public ResponseEntity<?> listWindowsDhcpCredentialDetails(HttpServletRequest request)
    {
        Response response = new Response();

        try
        {
            String accessToken = request.getHeader(TraceOrgCommonConstants.ACCESSTOKEN);

            List<TraceOrgDhcpCredentialDetails> traceOrgDhcpCredentialDetails = new ArrayList<>();

            traceOrgDhcpCredentialDetails = (List<TraceOrgDhcpCredentialDetails>) this.traceOrgService.commonQuery("",TraceOrgCommonConstants.TRACE_ORG_DHCP_CREDENTIAL+" where type = 'windows'");

            if(traceOrgDhcpCredentialDetails !=null && !traceOrgDhcpCredentialDetails.isEmpty())
            {
                TraceOrgDhcpCredentialDetails traceOrgDhcpCredentialDetail = new TraceOrgDhcpCredentialDetails();

                traceOrgDhcpCredentialDetail.setCredentialName("New Credential");

                traceOrgDhcpCredentialDetail.setType("Windows");

                traceOrgDhcpCredentialDetails.add(0,traceOrgDhcpCredentialDetail);

                response.setData(traceOrgDhcpCredentialDetails);

                response.setSuccess(TraceOrgCommonConstants.TRUE);
            }
            else
            {
                TraceOrgDhcpCredentialDetails traceOrgDhcpCredentialDetail = new TraceOrgDhcpCredentialDetails();

                traceOrgDhcpCredentialDetail.setCredentialName("New Credential");

                traceOrgDhcpCredentialDetail.setType("Windows");

                traceOrgDhcpCredentialDetails.add(0,traceOrgDhcpCredentialDetail);

                response.setSuccess(TraceOrgCommonConstants.TRUE);

                response.setData(traceOrgDhcpCredentialDetails);
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

    @RequestMapping(value = TraceOrgCommonConstants.DHCP_CREDENTIAL_REST_URL+"{id}", method = RequestMethod.GET)
    @PreAuthorize("hasAuthority('PERM_SETTINGS_READ')")
    public ResponseEntity<?> listDhcpCredentialDetailsById(@PathVariable(TraceOrgCommonConstants.ID) Long id, HttpServletRequest request)
    {
        Response response = new Response();

        try
        {
            String accessToken = request.getHeader(TraceOrgCommonConstants.ACCESSTOKEN);

            TraceOrgDhcpCredentialDetails traceOrgDhcpCredentialDetails = (TraceOrgDhcpCredentialDetails) this.traceOrgService.getById(TraceOrgCommonConstants.TRACE_ORG_DHCP_CREDENTIAL,id);

            if(traceOrgDhcpCredentialDetails !=null)
            {
                response.setData(traceOrgDhcpCredentialDetails);

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

    /**
     * IPAM-134 IPAM | Mail Server Configuration issue
     * Added generic sendMail method
     * IPAM-149 : IPAM Roadmap : System should have alert notification module to configure different kind of alert notification
     * Added code for NEW_SUBNETS_DISCOVERED Alert
     * **/
    @RequestMapping(value = TraceOrgCommonConstants.DHCP_CREDENTIAL_REST_URL, method = RequestMethod.POST)
    @PreAuthorize("hasAuthority('PERM_SETTINGS_WRITE')")
    public ResponseEntity<?> insertDhcpCredentialDetails(HttpServletRequest request, @RequestBody TraceOrgDhcpCredentialDetails traceOrgDhcpCredentialDetails)
    {
        Response response = new Response();

        try
        {
            String accessToken = request.getHeader(TraceOrgCommonConstants.ACCESSTOKEN);

            if (traceOrgDhcpCredentialDetails.getHostAddress() != null && !traceOrgDhcpCredentialDetails.getHostAddress().trim().isEmpty() && traceOrgDhcpCredentialDetails.getUserName() != null
                    && !traceOrgDhcpCredentialDetails.getUserName().trim().isEmpty() && traceOrgDhcpCredentialDetails.getPassword() != null && !traceOrgDhcpCredentialDetails.getPassword().trim().isEmpty() && traceOrgDhcpCredentialDetails.getPort() != null
                    && (traceOrgDhcpCredentialDetails.isScheduleStatus() ? (traceOrgDhcpCredentialDetails.getDuration() != null && !traceOrgDhcpCredentialDetails.getDuration().trim().isEmpty()
                    && (traceOrgDhcpCredentialDetails.getDuration().equalsIgnoreCase("Days") || traceOrgDhcpCredentialDetails.getDuration().equalsIgnoreCase("Hours") || traceOrgDhcpCredentialDetails.getDuration().equalsIgnoreCase("Month"))
                    && (traceOrgDhcpCredentialDetails.getSubnetDuration().equalsIgnoreCase("Days") || traceOrgDhcpCredentialDetails.getSubnetDuration().equalsIgnoreCase("Hours") || traceOrgDhcpCredentialDetails.getSubnetDuration().equalsIgnoreCase("Month"))) : true))
            {
                if (this.traceOrgService.isExist(TraceOrgCommonConstants.TRACE_ORG_DHCP_CREDENTIAL, TraceOrgCommonConstants.HOST_ADDRESS, traceOrgDhcpCredentialDetails.getHostAddress()))
                {
                    _logger.info("DHCP Credential for " + traceOrgDhcpCredentialDetails.getHostAddress() + " is already exist");

                    response.setSuccess(TraceOrgCommonConstants.FALSE);

                    response.setMessage(TraceOrgMessageConstants.DHCP_CREDENTIAL_ALREADY_EXIST);
                }
                else
                {

                    if (traceOrgDhcpCredentialDetails.getType().equalsIgnoreCase(TraceOrgCommonConstants.CISCO))
                    {
                        response = this.traceOrgCiscoDHCPServerUtil.discover(traceOrgDhcpCredentialDetails);
                    }
                    else if (traceOrgDhcpCredentialDetails.getType().equalsIgnoreCase(TraceOrgCommonConstants.WINDOWS))
                    {
                        response = this.traceOrgWindowsDhcpServerUtil.discover(traceOrgDhcpCredentialDetails);
                    }
                    else
                    {
                        response.setSuccess(TraceOrgCommonConstants.FALSE);

                        response.setMessage(TraceOrgMessageConstants.ENTER_VALID_DETAILS);
                    }

                    if (response.isSuccess())
                    {
                        if (this.traceOrgService.isExist(TraceOrgCommonConstants.TRACE_ORG_DHCP_CREDENTIAL, TraceOrgCommonConstants.CREDENTIAL_NAME, traceOrgDhcpCredentialDetails.getCredentialName()))
                        {
                            _logger.info("DHCP Credential name " + traceOrgDhcpCredentialDetails.getCredentialName() + " is already exist");

                            response.setSuccess(TraceOrgCommonConstants.FALSE);

                            response.setMessage(TraceOrgMessageConstants.DHCP_CREDENTIAL_NAME_ALREADY_EXIST);
                        }
                        else
                        {

                            switch (traceOrgDhcpCredentialDetails.getType().toUpperCase())
                            {
                                case "CISCO":
                                    traceOrgDhcpCredentialDetails.setType("Cisco");
                                    break;

                                case "WINDOWS":
                                    traceOrgDhcpCredentialDetails.setType("Windows");
                                    break;
                            }


                            if (traceOrgDhcpCredentialDetails.isScheduleStatus())
                            {
                                switch (traceOrgDhcpCredentialDetails.getDuration())
                                {
                                    case "Days":
                                        traceOrgDhcpCredentialDetails.setDuration("Days");
                                        if (traceOrgDhcpCredentialDetails.getScheduleHour() > 32)
                                            traceOrgDhcpCredentialDetails.setScheduleHour(0);
                                        break;
                                    case "Hours":
                                        traceOrgDhcpCredentialDetails.setDuration("Hours");
                                        if (traceOrgDhcpCredentialDetails.getScheduleHour() > 24)
                                            traceOrgDhcpCredentialDetails.setScheduleHour(0);
                                        break;
                                    case "Month":
                                        traceOrgDhcpCredentialDetails.setDuration("Month");
                                        if (traceOrgDhcpCredentialDetails.getScheduleHour() > 12)
                                            traceOrgDhcpCredentialDetails.setScheduleHour(0);
                                        break;
                                }

                                switch (traceOrgDhcpCredentialDetails.getSubnetDuration())
                                {
                                    case "Days":
                                        traceOrgDhcpCredentialDetails.setSubnetDuration("Days");
                                        if (traceOrgDhcpCredentialDetails.getScheduleHour() > 32)
                                            traceOrgDhcpCredentialDetails.setSubnetScheduleHour(0);
                                        break;
                                    case "Hours":
                                        traceOrgDhcpCredentialDetails.setSubnetDuration("Hours");
                                        if (traceOrgDhcpCredentialDetails.getScheduleHour() > 24)
                                            traceOrgDhcpCredentialDetails.setSubnetScheduleHour(0);
                                        break;
                                    case "Month":
                                        traceOrgDhcpCredentialDetails.setSubnetDuration("Month");
                                        if (traceOrgDhcpCredentialDetails.getScheduleHour() > 12)
                                            traceOrgDhcpCredentialDetails.setSubnetScheduleHour(0);
                                        break;
                                }
                            }


                            traceOrgDhcpCredentialDetails.setCreatedBy(traceOrgCommonUtil.currentUser(accessToken).getUserName());

                            traceOrgDhcpCredentialDetails.setCreatedDate(new Date());

                            traceOrgDhcpCredentialDetails.setModifiedDate(new Date());

                            traceOrgDhcpCredentialDetails.setLastScanTime(new Date());

                            boolean insertStatus = this.traceOrgService.insert(traceOrgDhcpCredentialDetails);

                            if (traceOrgDhcpCredentialDetails.isScheduleStatus() && traceOrgDhcpCredentialDetails.getScheduleHour() > 0)
                            {
                                String cronExpression = null;

                                switch (traceOrgDhcpCredentialDetails.getDuration())
                                {
                                    case "Days":
                                        cronExpression = "0 0 0 1-31/" + traceOrgDhcpCredentialDetails.getScheduleHour() + " * ?";
                                        break;
                                    case "Hours":
                                        cronExpression = "0 0 1-23/" + traceOrgDhcpCredentialDetails.getScheduleHour() + " ? * *";
                                        break;
                                    case "Month":
                                        cronExpression = "0 0 0 1 1-12/" + traceOrgDhcpCredentialDetails.getScheduleHour() + " ?";
                                        break;
                                }

                                traceOrgCommonUtil.scanDhcpCronJob(cronExpression, traceOrgDhcpCredentialDetails, traceOrgService, traceOrgCommonUtil, traceOrgCiscoDHCPServerUtil, traceOrgWindowsDhcpServerUtil);
                            }


                            if (traceOrgDhcpCredentialDetails.getType().equalsIgnoreCase(TraceOrgCommonConstants.CISCO))
                            {
                                this.traceOrgCiscoDHCPServerUtil.getDhcpServerStatistics(traceOrgDhcpCredentialDetails, traceOrgService);
                            }
                            else if (traceOrgDhcpCredentialDetails.getType().equalsIgnoreCase(TraceOrgCommonConstants.WINDOWS))
                            {
                                this.traceOrgWindowsDhcpServerUtil.getDhcpUtilizationDetails(traceOrgDhcpCredentialDetails, traceOrgService);
                            }

                            //EVENT LOG FOR DHCP
                            TraceOrgEvent traceOrgEventForDHCP = new TraceOrgEvent();

                            traceOrgEventForDHCP.setTimestamp(new Date());

                            traceOrgEventForDHCP.setDoneBy(traceOrgCommonUtil.currentUser(accessToken));

                            traceOrgEventForDHCP.setEventType("Add DHCP Server");

                            traceOrgEventForDHCP.setEventContext("DHCP  Server " + traceOrgDhcpCredentialDetails.getHostAddress() + " is  Added  in IP Address Manager by " + traceOrgCommonUtil.currentUserName(accessToken));

                            traceOrgEventForDHCP.setSeverity(1);

                            this.traceOrgService.insert(traceOrgEventForDHCP);

                            if (insertStatus)
                            {
                                List<TraceOrgSubnetDetails> traceOrgSubnetDetails = null;

                                if (traceOrgDhcpCredentialDetails.getType().equalsIgnoreCase(TraceOrgCommonConstants.CISCO))
                                {
                                    traceOrgSubnetDetails = this.traceOrgCiscoDHCPServerUtil.discoveryForSubnet(traceOrgDhcpCredentialDetails, traceOrgService);
                                }
                                else if (traceOrgDhcpCredentialDetails.getType().equalsIgnoreCase(TraceOrgCommonConstants.WINDOWS))
                                {
                                    traceOrgSubnetDetails = this.traceOrgWindowsDhcpServerUtil.getSubnetDetails(traceOrgDhcpCredentialDetails);
                                }

                                if (traceOrgSubnetDetails != null && !traceOrgSubnetDetails.isEmpty())
                                {
                                    for (TraceOrgSubnetDetails traceOrgSubnetDetail : traceOrgSubnetDetails)
                                    {
                                        traceOrgSubnetDetail.setCreatedBy(traceOrgCommonUtil.currentUser(accessToken).getUserName());

                                        if (!this.traceOrgService.isExist(TraceOrgCommonConstants.TRACE_ORG_SUBNET_DETAILS, TraceOrgCommonConstants.SUBNET_ADDRESS, traceOrgSubnetDetail.getSubnetAddress()) && !this.traceOrgService.isExist(TraceOrgCommonConstants.TRACE_ORG_SUBNET_DETAILS, "subnetName", traceOrgSubnetDetail.getSubnetName()))
                                        {
                                            TraceOrgCategory traceOrgCategory = (TraceOrgCategory) this.traceOrgService.getById(TraceOrgCommonConstants.TRACE_ORG_CATEGORY, 1L);

                                            if (traceOrgCategory != null)
                                            {
                                                traceOrgSubnetDetail.setScheduleHour(traceOrgDhcpCredentialDetails.getSubnetScheduleHour());

                                                traceOrgSubnetDetail.setDuration(traceOrgDhcpCredentialDetails.getSubnetDuration());

                                                switch (traceOrgDhcpCredentialDetails.getType().toUpperCase())
                                                {
                                                    case "CISCO":
                                                        traceOrgSubnetDetail.setType("Cisco");
                                                        break;

                                                    case "WINDOWS":
                                                        traceOrgSubnetDetail.setType("Windows");
                                                        break;
                                                }

                                                traceOrgSubnetDetail.setAllowDns(TraceOrgCommonConstants.TRUE);

                                                traceOrgSubnetDetail.setAllowIcmp(TraceOrgCommonConstants.TRUE);

                                                traceOrgSubnetDetail.setTraceOrgCategory(traceOrgCategory);

                                                traceOrgSubnetDetail.setTraceOrgDhcpCredentialDetailsId(traceOrgDhcpCredentialDetails);

                                                traceOrgSubnetDetail.setCreatedDate(new Date());

                                                traceOrgSubnetDetail.setModifiedDate(new Date());

                                                traceOrgSubnetDetail.setScheduleStatus(traceOrgDhcpCredentialDetails.isScheduleStatus());

                                                this.traceOrgService.insert(traceOrgSubnetDetail);

                                                traceOrgSupernetService.insertSubnetInSupernetCategory(traceOrgSubnetDetail.getSubnetAddress(), traceOrgSubnetDetail.getSubnetCidr(), traceOrgSubnetDetail.getId(), traceOrgCommonUtil.currentUser(accessToken), "by DHCP Server by "+traceOrgCommonUtil.currentUserName(accessToken));

                                                if(traceOrgSubnetDetail.isScheduleStatus() && traceOrgSubnetDetail.getScheduleHour() > 0 && traceOrgSubnetDetail.getDuration()!=null && !traceOrgSubnetDetail.getDuration().isEmpty())
                                                {
                                                    String cronExpression = null;

                                                    switch (traceOrgSubnetDetail.getDuration())
                                                    {
                                                        case "Days":
                                                            cronExpression = "0 0 0 1-31/" + traceOrgSubnetDetail.getScheduleHour() + " * ?";
                                                            break;
                                                        case "Hours":
                                                            cronExpression = "0 0 1-23/" + traceOrgSubnetDetail.getScheduleHour() + " ? * *";
                                                            break;
                                                        case "Month":
                                                            cronExpression = "0 0 0 1 1-12/" + traceOrgSubnetDetail.getScheduleHour() + " ?";
                                                            break;
                                                    }

                                                    traceOrgCommonUtil.scanSubnetCronJob(cronExpression, traceOrgSubnetDetail, traceOrgService, traceOrgCommonUtil);
                                                }

                                                //EVENT LOG
                                                TraceOrgEvent traceOrgEvent = new TraceOrgEvent();

                                                traceOrgEvent.setTimestamp(new Date());

                                                traceOrgEvent.setDoneBy(traceOrgCommonUtil.currentUser(accessToken));

                                                traceOrgEvent.setEventType("Add Subnet");

                                                traceOrgEvent.setEventContext("Subnet " + traceOrgSubnetDetail.getSubnetAddress() + " is added when DHCP Server " + traceOrgDhcpCredentialDetails.getHostAddress() + " Added in IP Address Manager by " + traceOrgCommonUtil.currentUserName(accessToken));

                                                traceOrgEvent.setSeverity(1);

                                                this.traceOrgService.insert(traceOrgEvent);

                                                //SUBNET IP ADDRESS
                                                boolean traceOrgSubnetIpDetailsList = traceOrgCommonUtil.ipList(traceOrgSubnetDetail);

                                                HashMap<String, Object> context = new HashMap<>();

                                                context.put("subnetAddress", traceOrgSubnetDetail.getSubnetAddress());

                                                context.put("currentUserName", traceOrgCommonUtil.currentUserName(accessToken));

                                                traceOrgCommonUtil.sendMessage(TraceOrgCommonConstants.ALERT_QUEUE, context, TraceOrgCommonConstants.NEW_SUBNETS_DISCOVERED);
                                            }
                                        }
                                        else
                                        {
                                            _logger.info("subnet details is already exist for " + traceOrgSubnetDetail.getSubnetAddress());
                                        }
                                    }
                                }

                                traceOrgCommonUtil.sendMail("DHCP Server Added In IP Address Manager",
                                        "DHCP Server " + traceOrgDhcpCredentialDetails.getHostAddress() + " Added  in IP Address Manager by " + traceOrgCommonUtil.currentUserName(accessToken));

                                response.setSuccess(TraceOrgCommonConstants.TRUE);

                                response.setMessage(TraceOrgMessageConstants.DHCP_CREDENTIAL_ADD_SUCCESS);
                            }
                            else
                            {
                                _logger.info("DHCP Server not added for " + traceOrgDhcpCredentialDetails.getCredentialName());

                                response.setSuccess(TraceOrgCommonConstants.FALSE);

                                response.setMessage(TraceOrgMessageConstants.SOMETHING_WENT_WRONG);
                            }
                        }
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

    @RequestMapping(value = TraceOrgCommonConstants.DHCP_CREDENTIAL_REST_URL + "{id}", method = RequestMethod.PUT)
    @PreAuthorize("hasAuthority('PERM_SETTINGS_WRITE')")
    public ResponseEntity<?> updateDhcpCredentialDetails(@PathVariable Long id, HttpServletRequest request, @RequestBody TraceOrgDhcpCredentialDetails traceOrgDhcpCredentialDetails)
    {
        Response response = new Response();

        try
        {
            String accessToken = request.getHeader(TraceOrgCommonConstants.ACCESSTOKEN);

            if (id != null && traceOrgDhcpCredentialDetails.getHostAddress() != null && !traceOrgDhcpCredentialDetails.getHostAddress().trim().isEmpty() && traceOrgDhcpCredentialDetails.getUserName() != null
                    && !traceOrgDhcpCredentialDetails.getUserName().trim().isEmpty() && traceOrgDhcpCredentialDetails.getPassword() != null && !traceOrgDhcpCredentialDetails.getPassword().trim().isEmpty() && traceOrgDhcpCredentialDetails.getPort() != null
                    && (traceOrgDhcpCredentialDetails.isScheduleStatus() ? (traceOrgDhcpCredentialDetails.getDuration() != null && !traceOrgDhcpCredentialDetails.getDuration().trim().isEmpty()
                    && (traceOrgDhcpCredentialDetails.getDuration().equalsIgnoreCase("Days") || traceOrgDhcpCredentialDetails.getDuration().equalsIgnoreCase("Hours") || traceOrgDhcpCredentialDetails.getDuration().equalsIgnoreCase("Month"))
                    && (traceOrgDhcpCredentialDetails.getSubnetDuration().equalsIgnoreCase("Days") || traceOrgDhcpCredentialDetails.getSubnetDuration().equalsIgnoreCase("Hours") || traceOrgDhcpCredentialDetails.getSubnetDuration().equalsIgnoreCase("Month"))) : true))
            {
                TraceOrgDhcpCredentialDetails existedTraceOrgDhcpCredentialDetails = (TraceOrgDhcpCredentialDetails) this.traceOrgService.getById(TraceOrgCommonConstants.TRACE_ORG_DHCP_CREDENTIAL, id);

                        /*if(traceOrgDhcpCredentialDetails.getScheduleHour() == null)
                        {
                            traceOrgDhcpCredentialDetails.setScheduleHour(0);
                        }

                        if(traceOrgDhcpCredentialDetails.getSubnetScheduleHour() == null)
                        {
                            traceOrgDhcpCredentialDetails.setSubnetScheduleHour(0);
                        }*/

                if (existedTraceOrgDhcpCredentialDetails != null && existedTraceOrgDhcpCredentialDetails.getHostAddress().equals(traceOrgDhcpCredentialDetails.getHostAddress()))
                {
                    List<TraceOrgDhcpCredentialDetails> traceOrgDhcpCredentialDetailsListByName = (List<TraceOrgDhcpCredentialDetails>) this.traceOrgService.commonQuery(TraceOrgCommonConstants.TRACE_ORG_DHCP_CREDENTIAL + " where credentialName = '" + traceOrgDhcpCredentialDetails.getCredentialName() + "' ");

                    if (traceOrgDhcpCredentialDetailsListByName != null && !traceOrgDhcpCredentialDetailsListByName.isEmpty() && traceOrgDhcpCredentialDetailsListByName.size() < 2 && traceOrgDhcpCredentialDetailsListByName.get(0).getId().equals(id))
                    {
                        existedTraceOrgDhcpCredentialDetails.setPort(traceOrgDhcpCredentialDetails.getPort());

                        existedTraceOrgDhcpCredentialDetails.setPassword(traceOrgDhcpCredentialDetails.getPassword());

                        existedTraceOrgDhcpCredentialDetails.setUserName(traceOrgDhcpCredentialDetails.getUserName());

                        existedTraceOrgDhcpCredentialDetails.setScheduleStatus(traceOrgDhcpCredentialDetails.isScheduleStatus());

                        if (existedTraceOrgDhcpCredentialDetails.isScheduleStatus())
                        {
                            switch (traceOrgDhcpCredentialDetails.getDuration())
                            {
                                case "Days":
                                    existedTraceOrgDhcpCredentialDetails.setDuration("Days");
                                    if (traceOrgDhcpCredentialDetails.getScheduleHour() > 32)
                                        existedTraceOrgDhcpCredentialDetails.setScheduleHour(0);
                                    break;
                                case "Hours":
                                    existedTraceOrgDhcpCredentialDetails.setDuration("Hours");
                                    if (traceOrgDhcpCredentialDetails.getScheduleHour() > 24)
                                        existedTraceOrgDhcpCredentialDetails.setScheduleHour(0);
                                    break;
                                case "Month":
                                    existedTraceOrgDhcpCredentialDetails.setDuration("Month");
                                    if (traceOrgDhcpCredentialDetails.getScheduleHour() > 12)
                                        existedTraceOrgDhcpCredentialDetails.setScheduleHour(0);
                                    break;
                            }

                            existedTraceOrgDhcpCredentialDetails.setScheduleHour(traceOrgDhcpCredentialDetails.getScheduleHour());

                            switch (traceOrgDhcpCredentialDetails.getSubnetDuration())
                            {
                                case "Days":
                                    existedTraceOrgDhcpCredentialDetails.setSubnetDuration("Days");
                                    if (traceOrgDhcpCredentialDetails.getScheduleHour() > 32)
                                        existedTraceOrgDhcpCredentialDetails.setSubnetScheduleHour(0);
                                    break;
                                case "Hours":
                                    existedTraceOrgDhcpCredentialDetails.setSubnetDuration("Hours");
                                    if (traceOrgDhcpCredentialDetails.getScheduleHour() > 24)
                                        existedTraceOrgDhcpCredentialDetails.setSubnetScheduleHour(0);
                                    break;
                                case "Month":
                                    existedTraceOrgDhcpCredentialDetails.setSubnetDuration("Month");
                                    if (traceOrgDhcpCredentialDetails.getScheduleHour() > 12)
                                        existedTraceOrgDhcpCredentialDetails.setSubnetScheduleHour(0);
                                    break;
                            }

                            existedTraceOrgDhcpCredentialDetails.setSubnetScheduleHour(traceOrgDhcpCredentialDetails.getSubnetScheduleHour());

                        }
                        else
                        {
                            existedTraceOrgDhcpCredentialDetails.setDuration(null);
                            existedTraceOrgDhcpCredentialDetails.setScheduleHour(null);
                            existedTraceOrgDhcpCredentialDetails.setSubnetScheduleHour(null);
                            existedTraceOrgDhcpCredentialDetails.setSubnetDuration(null);
                        }

                        if (existedTraceOrgDhcpCredentialDetails.getType().equalsIgnoreCase(TraceOrgCommonConstants.CISCO))
                        {
                            response = this.traceOrgCiscoDHCPServerUtil.discover(existedTraceOrgDhcpCredentialDetails);
                        }
                        else if (existedTraceOrgDhcpCredentialDetails.getType().equalsIgnoreCase(TraceOrgCommonConstants.WINDOWS))
                        {
                            response = this.traceOrgWindowsDhcpServerUtil.discover(existedTraceOrgDhcpCredentialDetails);
                        }
                        else
                        {
                            response.setSuccess(TraceOrgCommonConstants.FALSE);

                            response.setMessage(TraceOrgMessageConstants.ENTER_VALID_DETAILS);
                        }

                        if (response.isSuccess())
                        {
                            traceOrgDhcpCredentialDetails.setModifiedDate(new Date());

                            boolean insertStatus = this.traceOrgService.insert(existedTraceOrgDhcpCredentialDetails);

                            traceOrgCommonUtil.removeScanDhcpCron(existedTraceOrgDhcpCredentialDetails);

                            if (existedTraceOrgDhcpCredentialDetails.isScheduleStatus() && existedTraceOrgDhcpCredentialDetails.getScheduleHour() > 0)
                            {
                                String cronExpression = null;

                                switch (existedTraceOrgDhcpCredentialDetails.getDuration())
                                {
                                    case "Days":
                                        cronExpression = "0 0 0 1-31/" + existedTraceOrgDhcpCredentialDetails.getScheduleHour() + " * ?";
                                        break;
                                    case "Hours":
                                        cronExpression = "0 0 1-23/" + existedTraceOrgDhcpCredentialDetails.getScheduleHour() + " ? * *";
                                        break;
                                    case "Month":
                                        cronExpression = "0 0 0 1 1-12/" + existedTraceOrgDhcpCredentialDetails.getScheduleHour() + " ?";
                                        break;
                                }

                                traceOrgCommonUtil.scanDhcpCronJob(cronExpression, existedTraceOrgDhcpCredentialDetails, traceOrgService, traceOrgCommonUtil, traceOrgCiscoDHCPServerUtil, traceOrgWindowsDhcpServerUtil);
                            }

                            if (insertStatus)
                            {
                                List<TraceOrgSubnetDetails> traceOrgSubnetDetailsList = (List<TraceOrgSubnetDetails>) this.traceOrgService.commonQuery("TraceOrgSubnetDetails where traceOrgDhcpCredentialDetailsId = '" + id + "'");

                                if (traceOrgSubnetDetailsList != null && !traceOrgSubnetDetailsList.isEmpty())
                                {
                                    for (TraceOrgSubnetDetails traceOrgSubnetDetails : traceOrgSubnetDetailsList)
                                    {
                                        traceOrgSubnetDetails.setAllowDns(TraceOrgCommonConstants.TRUE);

                                        traceOrgSubnetDetails.setAllowIcmp(TraceOrgCommonConstants.TRUE);

                                        traceOrgSubnetDetails.setScheduleHour(existedTraceOrgDhcpCredentialDetails.getSubnetScheduleHour());

                                        traceOrgSubnetDetails.setDuration(existedTraceOrgDhcpCredentialDetails.getSubnetDuration());

                                        traceOrgSubnetDetails.setCreatedDate(new Date());

                                        traceOrgSubnetDetails.setModifiedDate(new Date());

                                        traceOrgSubnetDetails.setCreatedBy(traceOrgDhcpCredentialDetails.getCreatedBy());

                                        traceOrgSubnetDetails.setScheduleStatus(traceOrgDhcpCredentialDetails.isScheduleStatus());

                                        this.traceOrgService.insert(traceOrgSubnetDetails);

                                        traceOrgCommonUtil.removeScanSubnetCron(traceOrgSubnetDetails);

                                        if (traceOrgSubnetDetails.isScheduleStatus() && traceOrgSubnetDetails.getScheduleHour() > 0)
                                        {
                                            String cronExpression = null;

                                            switch (traceOrgSubnetDetails.getDuration())
                                            {
                                                case "Days":
                                                    cronExpression = "0 0 0 1-31/" + traceOrgSubnetDetails.getScheduleHour() + " * ?";
                                                    break;
                                                case "Hours":
                                                    cronExpression = "0 0 1-23/" + traceOrgSubnetDetails.getScheduleHour() + " ? * *";
                                                    break;
                                                case "Month":
                                                    cronExpression = "0 0 0 1 1-12/" + traceOrgSubnetDetails.getScheduleHour() + " ?";
                                                    break;
                                            }

                                            traceOrgCommonUtil.scanSubnetCronJob(cronExpression, traceOrgSubnetDetails, traceOrgService, traceOrgCommonUtil);
                                        }
                                    }
                                }

                                response.setSuccess(TraceOrgCommonConstants.TRUE);

                                response.setMessage(TraceOrgMessageConstants.DHCP_UPDATE_SUCCESS);
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
                        existedTraceOrgDhcpCredentialDetails.setPort(traceOrgDhcpCredentialDetails.getPort());

                        existedTraceOrgDhcpCredentialDetails.setPassword(traceOrgDhcpCredentialDetails.getPassword());

                        existedTraceOrgDhcpCredentialDetails.setUserName(traceOrgDhcpCredentialDetails.getUserName());

                        existedTraceOrgDhcpCredentialDetails.setCredentialName(traceOrgDhcpCredentialDetails.getCredentialName());

                        existedTraceOrgDhcpCredentialDetails.setScheduleStatus(traceOrgDhcpCredentialDetails.isScheduleStatus());

                        if (existedTraceOrgDhcpCredentialDetails.isScheduleStatus())
                        {
                            switch (traceOrgDhcpCredentialDetails.getDuration())
                            {
                                case "Days":
                                    existedTraceOrgDhcpCredentialDetails.setDuration("Days");
                                    if (traceOrgDhcpCredentialDetails.getScheduleHour() > 32)
                                        existedTraceOrgDhcpCredentialDetails.setScheduleHour(0);
                                    break;
                                case "Hours":
                                    existedTraceOrgDhcpCredentialDetails.setDuration("Hours");
                                    if (traceOrgDhcpCredentialDetails.getScheduleHour() > 24)
                                        existedTraceOrgDhcpCredentialDetails.setScheduleHour(0);
                                    break;
                                case "Month":
                                    existedTraceOrgDhcpCredentialDetails.setDuration("Month");
                                    if (traceOrgDhcpCredentialDetails.getScheduleHour() > 12)
                                        existedTraceOrgDhcpCredentialDetails.setScheduleHour(0);
                                    break;
                            }

                            existedTraceOrgDhcpCredentialDetails.setScheduleHour(traceOrgDhcpCredentialDetails.getScheduleHour());

                            switch (traceOrgDhcpCredentialDetails.getSubnetDuration())
                            {
                                case "Days":
                                    existedTraceOrgDhcpCredentialDetails.setSubnetDuration("Days");
                                    if (traceOrgDhcpCredentialDetails.getScheduleHour() > 32)
                                        existedTraceOrgDhcpCredentialDetails.setSubnetScheduleHour(0);
                                    break;
                                case "Hours":
                                    existedTraceOrgDhcpCredentialDetails.setSubnetDuration("Hours");
                                    if (traceOrgDhcpCredentialDetails.getScheduleHour() > 24)
                                        existedTraceOrgDhcpCredentialDetails.setSubnetScheduleHour(0);
                                    break;
                                case "Month":
                                    existedTraceOrgDhcpCredentialDetails.setSubnetDuration("Month");
                                    if (traceOrgDhcpCredentialDetails.getScheduleHour() > 12)
                                        existedTraceOrgDhcpCredentialDetails.setSubnetScheduleHour(0);
                                    break;
                            }

                            existedTraceOrgDhcpCredentialDetails.setSubnetScheduleHour(traceOrgDhcpCredentialDetails.getSubnetScheduleHour());

                        }

                        if (existedTraceOrgDhcpCredentialDetails.getType().equalsIgnoreCase(TraceOrgCommonConstants.CISCO))
                        {
                            response = this.traceOrgCiscoDHCPServerUtil.discover(existedTraceOrgDhcpCredentialDetails);
                        }
                        else if (existedTraceOrgDhcpCredentialDetails.getType().equalsIgnoreCase(TraceOrgCommonConstants.WINDOWS))
                        {
                            response = this.traceOrgWindowsDhcpServerUtil.discover(existedTraceOrgDhcpCredentialDetails);
                        }
                        else
                        {
                            response.setSuccess(TraceOrgCommonConstants.FALSE);

                            response.setMessage(TraceOrgMessageConstants.ENTER_VALID_DETAILS);
                        }

                        if (response.isSuccess())
                        {
                            traceOrgDhcpCredentialDetails.setModifiedDate(new Date());

                            boolean insertStatus = this.traceOrgService.insert(existedTraceOrgDhcpCredentialDetails);

                            traceOrgCommonUtil.removeScanDhcpCron(existedTraceOrgDhcpCredentialDetails);

                            if (existedTraceOrgDhcpCredentialDetails.isScheduleStatus() && existedTraceOrgDhcpCredentialDetails.getScheduleHour() > 0)
                            {
                                String cronExpression = null;

                                switch (existedTraceOrgDhcpCredentialDetails.getDuration())
                                {
                                    case "Days":
                                        cronExpression = "0 0 0 1-31/" + existedTraceOrgDhcpCredentialDetails.getScheduleHour() + " * ?";
                                        break;
                                    case "Hours":
                                        cronExpression = "0 0 1-23/" + existedTraceOrgDhcpCredentialDetails.getScheduleHour() + " ? * *";
                                        break;
                                    case "Month":
                                        cronExpression = "0 0 0 1 1-12/" + existedTraceOrgDhcpCredentialDetails.getScheduleHour() + " ?";
                                        break;
                                }

                                traceOrgCommonUtil.scanDhcpCronJob(cronExpression, existedTraceOrgDhcpCredentialDetails, traceOrgService, traceOrgCommonUtil, traceOrgCiscoDHCPServerUtil, traceOrgWindowsDhcpServerUtil);
                            }

                            if (insertStatus)
                            {
                                List<TraceOrgSubnetDetails> traceOrgSubnetDetailsList = (List<TraceOrgSubnetDetails>) this.traceOrgService.commonQuery("TraceOrgSubnetDetails where traceOrgDhcpCredentialDetailsId = '" + id + "'");

                                if (traceOrgSubnetDetailsList != null && !traceOrgSubnetDetailsList.isEmpty())
                                {
                                    for (TraceOrgSubnetDetails traceOrgSubnetDetails : traceOrgSubnetDetailsList)
                                    {
                                        traceOrgSubnetDetails.setAllowDns(TraceOrgCommonConstants.TRUE);

                                        traceOrgSubnetDetails.setAllowIcmp(TraceOrgCommonConstants.TRUE);

                                        traceOrgSubnetDetails.setScheduleHour(existedTraceOrgDhcpCredentialDetails.getSubnetScheduleHour());

                                        traceOrgSubnetDetails.setDuration(existedTraceOrgDhcpCredentialDetails.getSubnetDuration());

                                        traceOrgSubnetDetails.setScheduleStatus(existedTraceOrgDhcpCredentialDetails.isScheduleStatus());

                                        this.traceOrgService.insert(traceOrgSubnetDetails);

                                        traceOrgCommonUtil.removeScanSubnetCron(traceOrgSubnetDetails);

                                        if (traceOrgSubnetDetails.isScheduleStatus() && traceOrgSubnetDetails.getScheduleHour() > 0)
                                        {
                                            String cronExpression = null;

                                            switch (traceOrgSubnetDetails.getDuration())
                                            {
                                                case "Days":
                                                    cronExpression = "0 0 0 1-31/" + traceOrgSubnetDetails.getScheduleHour() + " * ?";
                                                    break;
                                                case "Hours":
                                                    cronExpression = "0 0 1-23/" + traceOrgSubnetDetails.getScheduleHour() + " ? * *";
                                                    break;
                                                case "Month":
                                                    cronExpression = "0 0 0 1 1-12/" + traceOrgSubnetDetails.getScheduleHour() + " ?";
                                                    break;
                                            }

                                            traceOrgCommonUtil.scanSubnetCronJob(cronExpression, traceOrgSubnetDetails, traceOrgService, traceOrgCommonUtil);
                                        }
                                    }
                                }

                                response.setSuccess(TraceOrgCommonConstants.TRUE);

                                response.setMessage(TraceOrgMessageConstants.DHCP_UPDATE_SUCCESS);
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
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    //Check DHCP Server Credential
    @RequestMapping(value = TraceOrgCommonConstants.CHECK_DHCP_CREDENTIAL_REST_URL, method = RequestMethod.POST)
    @PreAuthorize("hasAuthority('PERM_SETTINGS_WRITE')")
    public ResponseEntity<?> checkDhcpCredentialDetails(HttpServletRequest request, @RequestBody TraceOrgDhcpCredentialDetails traceOrgDhcpCredentialDetails)
    {
        Response response = new Response();

        try
        {
            String accessToken = request.getHeader(TraceOrgCommonConstants.ACCESSTOKEN);

            if (traceOrgDhcpCredentialDetails.getHostAddress() != null && !traceOrgDhcpCredentialDetails.getHostAddress().trim().isEmpty() && traceOrgDhcpCredentialDetails.getUserName() != null && !traceOrgDhcpCredentialDetails.getUserName().trim().isEmpty() && traceOrgDhcpCredentialDetails.getPassword() != null && !traceOrgDhcpCredentialDetails.getPassword().trim().isEmpty() && traceOrgDhcpCredentialDetails.getPort() != null)
            {
                if (this.traceOrgService.isExist(TraceOrgCommonConstants.TRACE_ORG_DHCP_CREDENTIAL, TraceOrgCommonConstants.HOST_ADDRESS, traceOrgDhcpCredentialDetails.getHostAddress()))
                {
                    response.setSuccess(TraceOrgCommonConstants.FALSE);

                    response.setMessage(TraceOrgMessageConstants.DHCP_CREDENTIAL_ALREADY_EXIST);
                }
                else
                {
                    if (traceOrgDhcpCredentialDetails.getType().equalsIgnoreCase(TraceOrgCommonConstants.CISCO))
                    {
                        response = this.traceOrgCiscoDHCPServerUtil.discover(traceOrgDhcpCredentialDetails);
                    }
                    else if (traceOrgDhcpCredentialDetails.getType().equalsIgnoreCase(TraceOrgCommonConstants.WINDOWS))
                    {
                        response = this.traceOrgWindowsDhcpServerUtil.discover(traceOrgDhcpCredentialDetails);
                    }
                    else
                    {
                        response.setSuccess(TraceOrgCommonConstants.FALSE);

                        response.setMessage(TraceOrgMessageConstants.ENTER_VALID_DETAILS);
                    }

                    if (response.isSuccess())
                    {
                        response.setSuccess(TraceOrgCommonConstants.TRUE);

                        response.setMessage(TraceOrgMessageConstants.DHCP_CREDENTIAL_VALID);
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
     * IPAM-145 : System should have rogue device detection capability
     * Added traceOrgRogueDetectionRepository object for rogue detection logic during scanning.
     * Found null pointer error for traceOrgAlertService not set during scanning.
     * IPAM-192 : Subnet should be added into respective supernet during DHCP Auto scan
     * added the traceOrgSupernetService into job data map
     * @param id
     * @param request
     * @return
     *
     * IPAM-174 : IPAM | Duplicate Emails sent on the same day for repeat mode for Report Scheduler
     * added the flag in job context for manual DHCP scan
     */
    //Scan DHCP
    @RequestMapping(value = TraceOrgCommonConstants.DHCP_SCAN_REST_URL + "{id}", method = RequestMethod.GET)
    @PreAuthorize("hasAuthority('PERM_SETTINGS_WRITE')")
    public ResponseEntity<?> scanDhcp(@PathVariable(TraceOrgCommonConstants.ID) Long id, HttpServletRequest request)
    {
        Response response = new Response();

        try
        {
            _logger.info("DHCP server scan request received for id " + id);

            String accessToken = request.getHeader(TraceOrgCommonConstants.ACCESSTOKEN);

            TraceOrgDhcpCredentialDetails traceOrgDhcpCredentialDetails = (TraceOrgDhcpCredentialDetails) this.traceOrgService.getById(TraceOrgCommonConstants.TRACE_ORG_DHCP_CREDENTIAL, id);

            if (traceOrgDhcpCredentialDetails != null)
            {
                if (traceOrgDhcpCredentialDetails.getType().equalsIgnoreCase(TraceOrgCommonConstants.CISCO))
                {
                    try
                    {
                        response = traceOrgCiscoDHCPServerUtil.discover(traceOrgDhcpCredentialDetails);
                    }
                    catch (Exception exception)
                    {
                        _logger.error(exception);
                    }
                }
                else if (traceOrgDhcpCredentialDetails.getType().equalsIgnoreCase(TraceOrgCommonConstants.WINDOWS))
                {
                    try
                    {
                        response = traceOrgWindowsDhcpServerUtil.discover(traceOrgDhcpCredentialDetails);
                    }
                    catch (Exception exception)
                    {
                        _logger.error(exception);
                    }
                }

                _logger.info("DHCP Server discovery response is " + response);

                if (response.isSuccess())
                {
                    if (TraceOrgCommonUtil.getSubnetScanStatus() == 0)
                    {
                        if (TraceOrgCommonUtil.quartzThread != null)
                        {
                            HashMap<String, Object> mapData = new HashMap<>();

                            mapData.put("traceOrgDhcpCredentialDetails", traceOrgDhcpCredentialDetails);

                            mapData.put(TraceOrgCommonConstants.ACCESSTOKEN, accessToken);

                            mapData.put(TraceOrgCommonConstants.TRACE_ORG_SERVICE, this.traceOrgService);

                            mapData.put(TraceOrgCommonConstants.TRACE_ORG_COMMON_UTIL, this.traceOrgCommonUtil);

                            mapData.put("traceOrgCiscoDHCPServerUtil", traceOrgCiscoDHCPServerUtil);

                            mapData.put("traceOrgWindowsDhcpServerUtil", traceOrgWindowsDhcpServerUtil);

                            mapData.put(TraceOrgCommonConstants.TRACE_ORG_SUPERNET_SERVICE, traceOrgSupernetService);

                            mapData.put(TraceOrgCommonConstants.MANUAL_DHCP_SCAN,true);

                            mapData.put(TraceOrgCommonConstants.USER_NAME, traceOrgCommonUtil.getCurrentUserName());

                            mapData.put("response", response);

                            JobKey jobKey = JobKey.jobKey(TraceOrgCommonConstants.SCAN_SUBNET);

                            JobDetail job = JobBuilder.newJob(TraceOrgScanDhcpSchedulerJob.class).withIdentity(jobKey).usingJobData(new JobDataMap(mapData)).storeDurably().build();

                            TraceOrgCommonUtil.quartzThread.addJob(job, true);

                            TraceOrgCommonUtil.quartzThread.triggerJob(jobKey);

                            response.setSuccess(TraceOrgCommonConstants.TRUE);

                            response.setMessage(TraceOrgMessageConstants.DHCP_SCAN_STARTED);

                            _logger.info(TraceOrgMessageConstants.DHCP_SCAN_STARTED + " for " + traceOrgDhcpCredentialDetails.getCredentialName());
                        }
                    }
                    else
                    {
                        _logger.info(TraceOrgMessageConstants.SCAN_ALREADY_RUNNING + " for " + traceOrgDhcpCredentialDetails.getCredentialName());

                        HashMap<String, Object> subnetDetails = new HashMap<>();

                        subnetDetails.put("traceOrgDhcpCredentialDetails", traceOrgDhcpCredentialDetails);

                        subnetDetails.put(TraceOrgCommonConstants.TRACE_ORG_SERVICE, this.traceOrgService);

                        subnetDetails.put(TraceOrgCommonConstants.TRACE_ORG_ALERT_SERVICE, this.traceOrgAlertService);

                        subnetDetails.put(TraceOrgCommonConstants.TRACE_ORG_ROGUE_DETECTION_REPOSITORY,this.traceOrgRogueDetectionRepository);

                        subnetDetails.put(TraceOrgCommonConstants.TRACE_ORG_COMMON_UTIL, this.traceOrgCommonUtil);

                        subnetDetails.put("traceOrgCiscoDHCPServerUtil", traceOrgCiscoDHCPServerUtil);

                        subnetDetails.put(TraceOrgCommonConstants.TRACE_ORG_SUPERNET_SERVICE, traceOrgSupernetService);

                        subnetDetails.put("traceOrgWindowsDhcpServerUtil", traceOrgWindowsDhcpServerUtil);

                        subnetDetails.put(TraceOrgCommonConstants.SCAN_TYPE, TraceOrgCommonConstants.DHCP_SCAN);

                        TraceOrgCommonUtil.m_scheduleScanSubnet.put("dhcp__" + traceOrgDhcpCredentialDetails.getCredentialName(), subnetDetails);

                        response.setSuccess(TraceOrgCommonConstants.FALSE);

                        response.setMessage(TraceOrgMessageConstants.SCAN_ALREADY_RUNNING);
                    }
                }
            }
            else
            {
                _logger.warn(TraceOrgMessageConstants.DHCP_ID_NOT_VALID + " for " + id);

                response.setSuccess(TraceOrgCommonConstants.FALSE);

                response.setMessage(TraceOrgMessageConstants.DHCP_ID_NOT_VALID);
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
     * IPAM-134 IPAM | Mail Server Configuration issue
     * Added generic sendMail method
     *
     * IPAM-148 : System should have the ability to locate the available subnets inside a Supernet. This is to provide assistance to users when creating subnets inside an aggregated Network.
     * Addd the removeSubnetFromSupernetDetails call
     * **/
    @RequestMapping(value = TraceOrgCommonConstants.DHCP_CREDENTIAL_REST_URL + "{id}", method = RequestMethod.DELETE)
    @PreAuthorize("hasAuthority('PERM_SETTINGS_WRITE')")
    public ResponseEntity<?> removeDhcpCredentialDetails(@PathVariable(TraceOrgCommonConstants.ID) Long id, HttpServletRequest request)
    {
        Response response = new Response();

        try
        {
            String accessToken = request.getHeader(TraceOrgCommonConstants.ACCESSTOKEN);

            TraceOrgDhcpCredentialDetails traceOrgDhcpCredentialDetails = (TraceOrgDhcpCredentialDetails) this.traceOrgService.getById(TraceOrgCommonConstants.TRACE_ORG_DHCP_CREDENTIAL, id);

            if (traceOrgDhcpCredentialDetails != null)
            {
                if (TraceOrgCommonUtil.m_scanSubnet.get("jobKey") != null && TraceOrgCommonUtil.m_scheduleScanSubnet.size() > 0)
                {
                    response.setSuccess(TraceOrgCommonConstants.FALSE);

                    response.setMessage(TraceOrgMessageConstants.CANT_DELETE_DHCP_UNDER_SCAN);

                    _logger.info("DHCP server " + traceOrgDhcpCredentialDetails.getCredentialName() + " not deleted as scan is running..");
                }
                else
                {
                    List<TraceOrgSubnetDetails> traceOrgSubnetDetailsList = (List<TraceOrgSubnetDetails>) this.traceOrgService.commonQuery("", "TraceOrgSubnetDetails where traceOrgDhcpCredentialDetailsId = '" + id + "'");

                    if (traceOrgSubnetDetailsList != null && !traceOrgSubnetDetailsList.isEmpty())
                    {
                        for (TraceOrgSubnetDetails traceOrgSubnetDetails : traceOrgSubnetDetailsList)
                        {
                            traceOrgCommonUtil.removeScanSubnetCron(traceOrgSubnetDetails);

                            boolean deleteIpOfSubnetStatus = this.traceOrgService.delete(TraceOrgCommonConstants.TRACE_ORG_SUBNET_IP_DETAILS, TraceOrgCommonConstants.SUBNET_ID, TraceOrgCommonUtil.getStringValue(traceOrgSubnetDetails.getId()));

                            if (deleteIpOfSubnetStatus)
                            {
                                //EVENT LOG
                                TraceOrgEvent traceOrgEvent = new TraceOrgEvent();

                                traceOrgEvent.setTimestamp(new Date());

                                traceOrgEvent.setDoneBy(traceOrgCommonUtil.currentUser(accessToken));

                                traceOrgEvent.setEventType("Delete Subnet");

                                traceOrgEvent.setEventContext("Subnet " + traceOrgSubnetDetails.getSubnetAddress() + " is deleted when DHCP server " + traceOrgDhcpCredentialDetails.getHostAddress() + " deleted  from IP Address Manager by " + traceOrgCommonUtil.currentUserName(accessToken));

                                traceOrgEvent.setSeverity(1);

                                this.traceOrgService.insert(traceOrgEvent);

                                this.traceOrgService.delete(TraceOrgCommonConstants.TRACE_ORG_SUBNET_DETAILS, TraceOrgCommonConstants.ID, TraceOrgCommonUtil.getStringValue(traceOrgSubnetDetails.getId()));

                                traceOrgSupernetService.removeSubnetFromSupernetDetails(id);

                                _logger.info("Subnet "+traceOrgSubnetDetails.getSubnetAddress()+" is deleted when DHCP server "+ traceOrgDhcpCredentialDetails.getHostAddress() +" is deleted");
                            }
                            else
                            {
                                response.setSuccess(TraceOrgCommonConstants.FALSE);

                                response.setMessage(TraceOrgMessageConstants.SOMETHING_WENT_WRONG);
                            }
                        }
                    }

                    traceOrgCommonUtil.removeScanDhcpCron(traceOrgDhcpCredentialDetails);

                    this.traceOrgService.delete(TraceOrgCommonConstants.TRACE_ORG_DHCP_UTILIZATION, "dhcpCredentialDetailId", TraceOrgCommonUtil.getStringValue(traceOrgDhcpCredentialDetails.getId()));

                    this.traceOrgService.delete(TraceOrgCommonConstants.TRACE_ORG_DHCP_CREDENTIAL, TraceOrgCommonConstants.ID, TraceOrgCommonUtil.getStringValue(id));

                    TraceOrgEvent traceOrgEvent = new TraceOrgEvent();

                    traceOrgEvent.setTimestamp(new Date());

                    traceOrgEvent.setSeverity(1);

                    traceOrgEvent.setDoneBy(traceOrgCommonUtil.currentUser(accessToken));

                    traceOrgEvent.setEventType("Delete DHCP Server");

                    traceOrgEvent.setEventContext("DHCP Server " + traceOrgDhcpCredentialDetails.getHostAddress() + " is deleted from IP Address Manager by " + traceOrgCommonUtil.currentUserName(accessToken));

                    _logger.info("DHCP Server " + traceOrgDhcpCredentialDetails.getHostAddress() + " is deleted");

                    this.traceOrgService.insert(traceOrgEvent);

                    traceOrgCommonUtil.sendMail("DHCP Server Deleted In IP Address Manager",
                            "DHCP Server " + traceOrgDhcpCredentialDetails.getHostAddress() + " Deleted  in IP Address Manager by " + traceOrgCommonUtil.currentUserName(accessToken));

                    response.setSuccess(TraceOrgCommonConstants.TRUE);

                    response.setMessage(TraceOrgMessageConstants.DHCP_CREDENTIAL_DELETE_SUCCESS);

                }


            }
            else
            {
                response.setSuccess(TraceOrgCommonConstants.FALSE);

                response.setMessage(TraceOrgMessageConstants.DHCP_ID_NOT_VALID);
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