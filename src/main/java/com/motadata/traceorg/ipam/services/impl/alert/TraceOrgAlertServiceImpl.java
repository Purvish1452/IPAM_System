package com.motadata.traceorg.ipam.services.impl.alert;

import com.google.common.base.Strings;
import com.motadata.traceorg.ipam.entity.rogueDetection.TraceOrgRogueDetection;
import com.motadata.traceorg.ipam.entity.settings.TraceOrgAlertConfigure;
import com.motadata.traceorg.ipam.logger.TraceOrgLogger;
import com.motadata.traceorg.ipam.entity.alert.TraceOrgAlertStream;
import com.motadata.traceorg.ipam.repository.alert.TraceOrgAlertStreamRepository;
import com.motadata.traceorg.ipam.repository.rogueDetection.TraceOrgRogueDetectionRepository;
import com.motadata.traceorg.ipam.repository.settings.TraceOrgAlertConfigureRepository;
import com.motadata.traceorg.ipam.services.alert.TraceOrgAlertService;
import com.motadata.traceorg.ipam.services.settings.TraceOrgAlertConfigureService;
import com.motadata.traceorg.ipam.util.TraceOrgCommonConstants;
import com.motadata.traceorg.ipam.util.TraceOrgCommonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class TraceOrgAlertServiceImpl implements TraceOrgAlertService
{
    private static final TraceOrgLogger _logger = new TraceOrgLogger(TraceOrgAlertServiceImpl.class, "Alert Service");

    @Autowired
    TraceOrgAlertConfigureRepository traceOrgAlertConfigureRepository;

    @Autowired
    TraceOrgAlertConfigureService traceOrgAlertConfigureService;

    @Autowired
    TraceOrgRogueDetectionRepository traceOrgRogueDetectionRepository;

    @Autowired
    TraceOrgCommonUtil traceOrgCommonUtil;

    @Autowired
    TraceOrgAlertStreamRepository traceOrgAlertStreamRepository;

    @Override
    public HashMap<String, Object> getAlerts(String alertFilter, Integer page, Integer pageSize)
    {
        HashMap<String, Object> response = new HashMap<>();

        try
        {
            HashMap<String, Object> result = new HashMap<>();

            if (page == null || page < 1) {
                page = 1;
            }
            if (pageSize == null || pageSize < 1) {
                pageSize = 20;
            }

            Boolean status = !(!Strings.isNullOrEmpty(alertFilter) && alertFilter.equalsIgnoreCase(TraceOrgCommonConstants.ALERT_CLEAR));

            int totalCount = traceOrgAlertStreamRepository.countByStatus(status);

            PageRequest pageRequest = new PageRequest(page - 1, pageSize);

            List<TraceOrgAlertStream> alertStreams = traceOrgAlertStreamRepository.findByStatusOrderByTimestampDesc(status, pageRequest).getContent();

            if(alertStreams != null && !alertStreams.isEmpty() && totalCount > 0)
            {
                result.put("data", alertStreams);

                result.put("total", totalCount);
            }
            else
            {
                result.put("data", new ArrayList<>());

                result.put("total", 0);
            }

            response.put(TraceOrgCommonConstants.DATA, result);

            response.put(TraceOrgCommonConstants.SUCCESS, TraceOrgCommonConstants.TRUE);
        }
        catch (Exception exception)
        {
            _logger.error(exception);
        }

        return response;
    }

    @Override
    public void inspectAlert(HashMap<String, Object> message)
    {
        try
        {
            if (message != null && !message.isEmpty())
            {
                String work = TraceOrgCommonUtil.getStringValue(message.get(TraceOrgCommonConstants.WORK_TYPE));

                HashMap<String, Object> context = (HashMap<String, Object>) message.get(TraceOrgCommonConstants.WORK_CONTEXT);

                if (!Strings.isNullOrEmpty(work) && context != null && !context.isEmpty())
                {
                    String[] workTypes = work.split(TraceOrgCommonConstants.VALUE_SEPARATOR_WITH_ESCAPE);

                    for (String workType: workTypes)
                    {
                        switch (workType)
                        {
                            case TraceOrgCommonConstants.ROGUE_DETECTION:
                            {
                                if (traceOrgCommonUtil.getBoolean(traceOrgAlertConfigureService.getAlertValue(TraceOrgCommonConstants.ROGUE_DETECTION)))
                                {
                                    rougeDetection(context);
                                }

                                break;
                            }
                            case TraceOrgCommonConstants.IP_UTILIZATION:
                            {
                                if (traceOrgCommonUtil.getBoolean(traceOrgAlertConfigureService.getAlertValue(TraceOrgCommonConstants.IP_UTILIZATION_FLAG)))
                                {
                                    ipUtilization(context);
                                }

                                break;
                            }
                            case TraceOrgCommonConstants.IP_UTILIZATION_BELOW:
                            {
                                if (traceOrgCommonUtil.getBoolean(traceOrgAlertConfigureService.getAlertValue(TraceOrgCommonConstants.IP_UTILIZATION_BELOW_FLAG)))
                                {
                                    ipUtilizationBelow(context);
                                }

                                break;
                            }
                            case TraceOrgCommonConstants.NEW_SUBNETS_DISCOVERED:
                            {
                                if(traceOrgCommonUtil.getBoolean(traceOrgAlertConfigureService.getAlertValue(TraceOrgCommonConstants.NEW_SUBNETS_DISCOVERED)))
                                {
                                    newSubnetsDiscovered(context);
                                }

                                break;
                            }
                            case TraceOrgCommonConstants.REVERSE_LOOKUP_FAILED:
                            {
                                if(traceOrgCommonUtil.getBoolean(traceOrgAlertConfigureService.getAlertValue(TraceOrgCommonConstants.REVERSE_LOOKUP_FAILED)))
                                {
                                    reverseLookupFailed(context);
                                }

                                break;
                            }
                            case TraceOrgCommonConstants.FORWARD_LOOKUP_FAILED:
                            {
                                if(traceOrgCommonUtil.getBoolean(traceOrgAlertConfigureService.getAlertValue(TraceOrgCommonConstants.FORWARD_LOOKUP_FAILED)))
                                {
                                    forwardLookupFailed(context);
                                }

                                break;
                            }
                            case TraceOrgCommonConstants.FORWARD_LOOKUP_MISMATCH:
                            {
                                if(traceOrgCommonUtil.getBoolean(traceOrgAlertConfigureService.getAlertValue(TraceOrgCommonConstants.FORWARD_LOOKUP_MISMATCH)))
                                {
                                    forwardLookupMismatch(context);
                                }

                                break;
                            }

                            case TraceOrgCommonConstants.IP_STATE_CHANGE:
                            {
                                if(traceOrgCommonUtil.getBoolean(traceOrgAlertConfigureService.getAlertValue(TraceOrgCommonConstants.IP_STATE_CHANGE)))
                                {
                                    ipStateChange(context);
                                }

                                break;
                            }

                            case TraceOrgCommonConstants.IP_RESERVATION_CHANGE:
                            {
                                if(traceOrgCommonUtil.getBoolean(traceOrgAlertConfigureService.getAlertValue(TraceOrgCommonConstants.IP_RESERVATION_CHANGE)))
                                {
                                    ipReservationChange(context);
                                }

                                break;
                            }
                            case TraceOrgCommonConstants.IP_CONFLICT:
                            {
                                if(traceOrgCommonUtil.getBoolean(traceOrgAlertConfigureService.getAlertValue(TraceOrgCommonConstants.IP_CONFLICT)))
                                {
                                    ipConflict(context);
                                }

                                break;
                            }

                            case TraceOrgCommonConstants.MAC_IP_CHANGE:
                            {
                                if(traceOrgCommonUtil.getBoolean(traceOrgAlertConfigureService.getAlertValue(TraceOrgCommonConstants.MAC_IP_CHANGE_FLAG)))
                                {
                                    macIpChange(context);
                                }

                                break;
                            }
                        }
                    }

                }
            }
        }
        catch (Exception exception)
        {
            _logger.error(exception);
        }
    }

    void rougeDetection(HashMap<String, Object> context)
    {
        try
        {
            List<String> rogueIps = (List<String>) context.get("rogueIps");

            Long subnetId = (Long) context.get("subnetId");

            String subnetAddress = TraceOrgCommonUtil.getStringValue(context.get("subnetAddress"));

            if(subnetId != null && subnetAddress != null)
            {
                List<TraceOrgAlertStream> alertStreams = traceOrgAlertStreamRepository.findBySubnetIdAndAlertTypeAndStatus(
                        subnetId,
                        TraceOrgCommonConstants.ROGUE_DETECTION_ALERT_TYPE,
                        TraceOrgCommonConstants.TRUE);

                if(rogueIps != null && !rogueIps.isEmpty())
                {
                    String message = TraceOrgCommonConstants.ROGUE_DETECTION_ALERT_MESSAGE
                            .replace(TraceOrgCommonConstants.SUBNET, subnetAddress)
                            .replace(TraceOrgCommonConstants.ROGUE_IP, TraceOrgCommonUtil.getStringValue(rogueIps));

                    if(alertStreams == null || alertStreams.isEmpty())
                    {
                        saveAlertStream(subnetId, TraceOrgCommonConstants.ROGUE_DETECTION_ALERT_TYPE,
                                message, subnetAddress, Boolean.TRUE);


                        String mailMessage = TraceOrgCommonConstants.ROGUE_DETECTION_MAIL_ALERT_MESSAGE
                                .replace(TraceOrgCommonConstants.SUBNET, subnetAddress);

                        String mailBody = generateHtmlTable(mailMessage, "Rogue Ips", rogueIps);

                        String title = TraceOrgCommonConstants.ROGUE_DETECTION_ALERT_TITLE.replace(TraceOrgCommonConstants.SUBNET, subnetAddress);

                        traceOrgCommonUtil.sendMail(title, mailBody);
                    }
                    else
                    {
                        updateAlertStream(alertStreams.get(0), message, Boolean.TRUE);

                        String mailMessage = TraceOrgCommonConstants.ROGUE_DETECTION_MAIL_ALERT_MESSAGE
                                .replace(TraceOrgCommonConstants.SUBNET, subnetAddress);

                        String mailBody = generateHtmlTable(mailMessage, "Rogue Ips", rogueIps);

                        String title = TraceOrgCommonConstants.ROGUE_DETECTION_ALERT_TITLE.replace(TraceOrgCommonConstants.SUBNET, subnetAddress);

                        traceOrgCommonUtil.sendMail(title, mailBody);
                    }
                }
                else
                {
                    if(alertStreams != null && !alertStreams.isEmpty())
                    {
                        String mailMessage = TraceOrgCommonConstants.ROGUE_DETECTION_ALERT_CLEAR_MESSAGE
                                .replace(TraceOrgCommonConstants.SUBNET, subnetAddress);

                        String title = TraceOrgCommonConstants.ROGUE_DETECTION_ALERT_CLEAR_TITLE.replace(TraceOrgCommonConstants.SUBNET, subnetAddress);

                        traceOrgCommonUtil.sendMail(title, mailMessage);

                        updateAlertStream(alertStreams.get(0), mailMessage, Boolean.FALSE);
                    }
                }
            }
        }
        catch (Exception exception)
        {
            _logger.error(exception);
        }
    }

    void ipUtilization(HashMap<String, Object> context)
    {
        try
        {
            Long subnetId = (Long) context.get("subnetId");

            String subnetAddress = TraceOrgCommonUtil.getStringValue(context.get("subnetAddress"));

            Float usedIpPercentage = TraceOrgCommonUtil.getFloatValue(context.get("usedIpPercentage"));

            if(subnetId != null &&  subnetAddress != null && usedIpPercentage != null)
            {
                List<TraceOrgAlertStream> alertStreams = traceOrgAlertStreamRepository.findBySubnetIdAndAlertTypeAndStatus(
                        subnetId,
                        TraceOrgCommonConstants.IP_UTILIZATION_ALERT_TYPE,
                        TraceOrgCommonConstants.TRUE);

                String threshold = traceOrgAlertConfigureService.getAlertValue(TraceOrgCommonConstants.IP_UTILIZATION);

                if(!Strings.isNullOrEmpty(threshold))
                {
                    if(usedIpPercentage > Integer.parseInt(threshold))
                    {
                        String message = TraceOrgCommonConstants.IP_UTILIZATION_ALERT_MESSAGE
                                .replace(TraceOrgCommonConstants.SUBNET, subnetAddress)
                                .replace(TraceOrgCommonConstants.THRESHOLD, threshold)
                                .replace(TraceOrgCommonConstants.UTILIZATION, TraceOrgCommonUtil.getStringValue(usedIpPercentage));

                        if(alertStreams == null || alertStreams.isEmpty())
                        {
                            saveAlertStream(subnetId, TraceOrgCommonConstants.IP_UTILIZATION_ALERT_TYPE,
                                    message, subnetAddress, Boolean.TRUE);

                            String mailMessage = TraceOrgCommonConstants.IP_UTILIZATION_MAIL_ALERT_MESSAGE
                                    .replace(TraceOrgCommonConstants.SUBNET, subnetAddress)
                                    .replace(TraceOrgCommonConstants.THRESHOLD, threshold)
                                    .replace(TraceOrgCommonConstants.UTILIZATION, TraceOrgCommonUtil.getStringValue(usedIpPercentage));

                            String title = TraceOrgCommonConstants.IP_UTILIZATION_ALERT_TITLE.replace(TraceOrgCommonConstants.SUBNET, subnetAddress);

                            traceOrgCommonUtil.sendMail(title, mailMessage);
                        }
                        else
                        {
                            _logger.warn("IP Utilization Alert Already Active : " + message);
                        }
                    }
                    else if(alertStreams != null && !alertStreams.isEmpty())
                    {
                        String mailMessage = TraceOrgCommonConstants.IP_UTILIZATION_ALERT_CLEAR_MAIL_MESSAGE
                                .replace(TraceOrgCommonConstants.SUBNET, subnetAddress)
                                .replace(TraceOrgCommonConstants.THRESHOLD, threshold)
                                .replace(TraceOrgCommonConstants.UTILIZATION, TraceOrgCommonUtil.getStringValue(usedIpPercentage));

                        String title = TraceOrgCommonConstants.IP_UTILIZATION_ALERT_CLEAR_TITLE.replace(TraceOrgCommonConstants.SUBNET, subnetAddress);

                        traceOrgCommonUtil.sendMail(title, mailMessage);

                        String message = TraceOrgCommonConstants.IP_UTILIZATION_ALERT_CLEAR_MESSAGE
                                .replace(TraceOrgCommonConstants.SUBNET, subnetAddress)
                                .replace(TraceOrgCommonConstants.THRESHOLD, threshold)
                                .replace(TraceOrgCommonConstants.UTILIZATION, TraceOrgCommonUtil.getStringValue(usedIpPercentage));

                        updateAlertStream(alertStreams.get(0), message, Boolean.FALSE);
                    }
                }
            }
        }
        catch (Exception exception)
        {
            _logger.error(exception);
        }
    }

    void ipUtilizationBelow(HashMap<String, Object> context)
    {
        try
        {
            Long subnetId = (Long) context.get("subnetId");

            String subnetAddress = TraceOrgCommonUtil.getStringValue(context.get("subnetAddress"));

            Float usedIpPercentage = TraceOrgCommonUtil.getFloatValue(context.get("usedIpPercentage"));

            if(subnetId != null &&  subnetAddress != null && usedIpPercentage != null)
            {
                List<TraceOrgAlertStream> alertStreams = traceOrgAlertStreamRepository.findBySubnetIdAndAlertTypeAndStatus(
                        subnetId,
                        TraceOrgCommonConstants.IP_UTILIZATION_BELOW_ALERT_TYPE,
                        TraceOrgCommonConstants.TRUE);

                String threshold = traceOrgAlertConfigureService.getAlertValue(TraceOrgCommonConstants.IP_UTILIZATION_BELOW);

                if(!Strings.isNullOrEmpty(threshold))
                {
                    if(usedIpPercentage < Integer.parseInt(threshold))
                    {
                        String message = TraceOrgCommonConstants.IP_UTILIZATION_BELOW_ALERT_MESSAGE
                                .replace(TraceOrgCommonConstants.SUBNET, subnetAddress)
                                .replace(TraceOrgCommonConstants.THRESHOLD, threshold)
                                .replace(TraceOrgCommonConstants.UTILIZATION, TraceOrgCommonUtil.getStringValue(usedIpPercentage));

                        if(alertStreams == null || alertStreams.isEmpty())
                        {
                            saveAlertStream(subnetId, TraceOrgCommonConstants.IP_UTILIZATION_BELOW_ALERT_TYPE,
                                    message, subnetAddress, Boolean.TRUE);

                            String mailMessage = TraceOrgCommonConstants.IP_UTILIZATION_BELOW_MAIL_ALERT_MESSAGE
                                    .replace(TraceOrgCommonConstants.SUBNET, subnetAddress)
                                    .replace(TraceOrgCommonConstants.THRESHOLD, threshold)
                                    .replace(TraceOrgCommonConstants.UTILIZATION, TraceOrgCommonUtil.getStringValue(usedIpPercentage));

                            String title = TraceOrgCommonConstants.IP_UTILIZATION_BELOW_ALERT_TITLE.replace(TraceOrgCommonConstants.SUBNET, subnetAddress);

                            traceOrgCommonUtil.sendMail(title, mailMessage);
                        }
                        else
                        {
                            _logger.warn("IP Utilization Alert Already Active : " + message);
                        }
                    }
                    else if(alertStreams != null && !alertStreams.isEmpty())
                    {
                        String mailMessage = TraceOrgCommonConstants.IP_UTILIZATION_BELOW_ALERT_CLEAR_MAIL_MESSAGE
                                .replace(TraceOrgCommonConstants.SUBNET, subnetAddress)
                                .replace(TraceOrgCommonConstants.THRESHOLD, threshold)
                                .replace(TraceOrgCommonConstants.UTILIZATION, TraceOrgCommonUtil.getStringValue(usedIpPercentage));

                        String title = TraceOrgCommonConstants.IP_UTILIZATION_BELOW_ALERT_CLEAR_TITLE.replace(TraceOrgCommonConstants.SUBNET, subnetAddress);

                        traceOrgCommonUtil.sendMail(title, mailMessage);

                        String message = TraceOrgCommonConstants.IP_UTILIZATION_BELOW_ALERT_CLEAR_MESSAGE
                                .replace(TraceOrgCommonConstants.SUBNET, subnetAddress)
                                .replace(TraceOrgCommonConstants.THRESHOLD, threshold)
                                .replace(TraceOrgCommonConstants.UTILIZATION, TraceOrgCommonUtil.getStringValue(usedIpPercentage));


                        updateAlertStream(alertStreams.get(0), message, Boolean.FALSE);
                    }
                }
            }
        }
        catch (Exception exception)
        {
            _logger.error(exception);
        }
    }

    void newSubnetsDiscovered(HashMap<String, Object> context)
    {
        try
        {
            String subnetAddresses = TraceOrgCommonUtil.getStringValue(context.get("subnetAddress"));

            String userName = TraceOrgCommonUtil.getStringValue(context.get("currentUserName"));

            if(!Strings.isNullOrEmpty(subnetAddresses) && !Strings.isNullOrEmpty(userName))
            {
                String title = "New Subnet Added to IP Address Manager";

                String mailMessage;

                ArrayList<String> subnets = new ArrayList<>(Arrays.asList(subnetAddresses.split(TraceOrgCommonConstants.COMMA_SEPARATOR)));

                if (subnets.size() > 1)
                {
                    mailMessage = generateHtmlTable("New Subnets Added to IP Address Manager by " + userName + ".", "Subnet Address", subnets);
                }
                else
                {
                    mailMessage = "New subnet " + subnets.get(0) + " added to IP Address Manager by " + userName + ".";
                }

                traceOrgCommonUtil.sendMail(title, mailMessage);
            }
        }
        catch (Exception exception)
        {
            _logger.error(exception);
        }
    }

    private void reverseLookupFailed(HashMap<String, Object> context)
    {
        try
        {
            String subnetAddress = TraceOrgCommonUtil.getStringValue(context.get("subnetAddress"));

            String ipAddress = TraceOrgCommonUtil.getStringValue(context.get("ipAddress"));

            String dnsAddress = TraceOrgCommonUtil.getStringValue(context.get("dnsAddress"));

            if (!Strings.isNullOrEmpty(subnetAddress) && !Strings.isNullOrEmpty(ipAddress))
            {
                String title = "Reverse Lookup Failed for IP " + ipAddress + " in Subnet " + subnetAddress;

                String message;

                if (!Strings.isNullOrEmpty(dnsAddress))
                {
                    message = "Reverse lookup failed for IP " + ipAddress + " in subnet " + subnetAddress + " using DNS " + dnsAddress + " in IP Address Manager.";
                }
                else
                {
                    message = "Reverse lookup failed for IP " + ipAddress + " in subnet " + subnetAddress + " in IP Address Manager.";
                }

                traceOrgCommonUtil.sendMail(title, message);
            }
        }
        catch (Exception exception)
        {
            _logger.error(exception);
        }
    }

    private void forwardLookupFailed(HashMap<String, Object> context)
    {
        try
        {
            String subnetAddress = TraceOrgCommonUtil.getStringValue(context.get("subnetAddress"));

            String ipAddress = TraceOrgCommonUtil.getStringValue(context.get("ipAddress"));

            String dnsAddress = TraceOrgCommonUtil.getStringValue(context.get("dnsAddress"));

            if (!Strings.isNullOrEmpty(subnetAddress) && !Strings.isNullOrEmpty(ipAddress))
            {
                String title = "Forward Lookup Failed for IP " + ipAddress + " in Subnet " + subnetAddress;

                String message;

                if (!Strings.isNullOrEmpty(dnsAddress))
                {
                    message = "Forward lookup failed for IP " + ipAddress + " in subnet " + subnetAddress + " using DNS " + dnsAddress + " in IP Address Manager.";
                }
                else
                {
                    message = "Forward lookup failed for IP " + ipAddress + " in subnet " + subnetAddress + " in IP Address Manager.";
                }

                traceOrgCommonUtil.sendMail(title, message);
            }
        }
        catch (Exception exception)
        {
            _logger.error(exception);
        }
    }

    private void forwardLookupMismatch(HashMap<String, Object> context)
    {
        try
        {
            String subnetAddress = TraceOrgCommonUtil.getStringValue(context.get("subnetAddress"));

            String ipAddress = TraceOrgCommonUtil.getStringValue(context.get("ipAddress"));

            String dnsAddress = TraceOrgCommonUtil.getStringValue(context.get("dnsAddress"));

            String mismatchIp = TraceOrgCommonUtil.getStringValue(context.get("dnsToIp"));

            if (!Strings.isNullOrEmpty(subnetAddress) && !Strings.isNullOrEmpty(ipAddress)
                    && !Strings.isNullOrEmpty(mismatchIp))
            {
                String title = "Forward Lookup Mismatch for IP " + ipAddress + " in Subnet " + subnetAddress;

                String message;

                if (!Strings.isNullOrEmpty(dnsAddress))
                {
                    message = "Forward lookup mismatch for IP " + ipAddress + " in subnet " + subnetAddress + " using DNS " + dnsAddress + ", resolved to IP " + mismatchIp + " in IP Address Manager.";
                }
                else
                {
                    message = "Forward lookup mismatch for IP " + ipAddress + " in subnet " + subnetAddress + " resolved to IP " + mismatchIp + " in IP Address Manager.";
                }

                traceOrgCommonUtil.sendMail(title, message);
            }
        }
        catch (Exception exception)
        {
            _logger.error(exception);
        }
    }

    private void ipStateChange(HashMap<String, Object> context)
    {
        try
        {
            String previousStatus = TraceOrgCommonUtil.getStringValue(context.get("previousStatus"));

            String currentStatus = TraceOrgCommonUtil.getStringValue(context.get("currentStatus"));

            String subnetAddress = TraceOrgCommonUtil.getStringValue(context.get("subnetAddress"));

            String ipAddress = TraceOrgCommonUtil.getStringValue(context.get("ipAddress"));

            if (!Strings.isNullOrEmpty(previousStatus) && !Strings.isNullOrEmpty(currentStatus)
                    && !Strings.isNullOrEmpty(subnetAddress) && !Strings.isNullOrEmpty(ipAddress))
            {

                if ((previousStatus.equals(TraceOrgCommonConstants.AVAILABLE) && currentStatus.equals(TraceOrgCommonConstants.USED))
                        || (previousStatus.equals(TraceOrgCommonConstants.TRANSIENT) && currentStatus.equals(TraceOrgCommonConstants.AVAILABLE)))
                {

                    String title = "IP State Change for IP " + ipAddress + " in Subnet " + subnetAddress;

                    String message = "The state of IP " + ipAddress + " in subnet " + subnetAddress + " has changed from "
                            + previousStatus + " to " + currentStatus + " in IP Address Manager.";

                    traceOrgCommonUtil.sendMail(title, message);
                }
            }
        }
        catch (Exception exception)
        {
            _logger.error(exception);
        }
    }

    private void ipReservationChange(HashMap<String, Object> context)
    {
        try
        {
            String previousStatus = TraceOrgCommonUtil.getStringValue(context.get("previousStatus"));

            String currentStatus = TraceOrgCommonUtil.getStringValue(context.get("currentStatus"));

            String subnetAddress = TraceOrgCommonUtil.getStringValue(context.get("subnetAddress"));

            String ipAddress = TraceOrgCommonUtil.getStringValue(context.get("ipAddress"));

            if (!Strings.isNullOrEmpty(previousStatus) && !Strings.isNullOrEmpty(currentStatus)
                    && !Strings.isNullOrEmpty(subnetAddress) && !Strings.isNullOrEmpty(ipAddress))
            {
                if (previousStatus.equals(TraceOrgCommonConstants.RESERVED))
                {
                    String title = "IP Reservation Change for IP " + ipAddress + " in Subnet " + subnetAddress;

                    String message = "The reservation state of IP " + ipAddress + " in subnet " + subnetAddress
                            + " has changed from " + previousStatus + " to " + currentStatus + " in IP Address Manager.";

                    traceOrgCommonUtil.sendMail(title, message);
                }
            }
        }
        catch (Exception exception)
        {
            _logger.error(exception);
        }
    }

    private void ipConflict(HashMap<String, Object> context)
    {
        try
        {
            String macAddress = TraceOrgCommonUtil.getStringValue(context.get("macAddress"));

            String conflictMacAddress = TraceOrgCommonUtil.getStringValue(context.get("conflictMacAddress"));

            String subnetAddress = TraceOrgCommonUtil.getStringValue(context.get("subnetAddress"));

            String ipAddress = TraceOrgCommonUtil.getStringValue(context.get("ipAddress"));

            if (!Strings.isNullOrEmpty(macAddress) && !Strings.isNullOrEmpty(conflictMacAddress)
                    && !Strings.isNullOrEmpty(subnetAddress) && !Strings.isNullOrEmpty(ipAddress))
            {
                String title = "IP Conflict Detected for IP " + ipAddress + " in Subnet " + subnetAddress;

                String message = "IP conflict detected for IP " + ipAddress + " in subnet " + subnetAddress
                        + ". MAC address " + macAddress + " conflicts with MAC address " + conflictMacAddress + ".";

                traceOrgCommonUtil.sendMail(title, message);
            }
        }
        catch (Exception exception)
        {
            _logger.error(exception);
        }
    }

    private void macIpChange(HashMap<String, Object> context)
    {
        try
        {
            String macAddress = TraceOrgCommonUtil.getStringValue(context.get("macAddress"));

            String subnetAddress = TraceOrgCommonUtil.getStringValue(context.get("subnetAddress"));

            String ipAddress = TraceOrgCommonUtil.getStringValue(context.get("ipAddress"));

            ArrayList<String> ipList = (ArrayList<String>) context.get("ipList");

            if(!Strings.isNullOrEmpty(macAddress) && !Strings.isNullOrEmpty(subnetAddress)
                    && !Strings.isNullOrEmpty(ipAddress))
            {
                boolean isExclude = false;

                TraceOrgAlertConfigure alertConfig = traceOrgAlertConfigureRepository.findByAlertKey(TraceOrgCommonConstants.MAC_IP_CHANGE);

                if (alertConfig != null && !Strings.isNullOrEmpty(TraceOrgCommonUtil.getStringValue(alertConfig.getAlertValue())))
                {
                    List<String> excludedMacAddresses = Arrays.asList(TraceOrgCommonUtil.getStringValue(alertConfig.getAlertValue()).split(TraceOrgCommonConstants.COMMA_SEPARATOR));

                    if (excludedMacAddresses.contains(macAddress.toLowerCase()))
                    {
                        isExclude = true;
                    }
                }

                if(!isExclude && ipList != null && !ipList.isEmpty() && !ipList.contains(ipAddress))
                {
                    String title = "MAC-IP Change Detected for MAC " + macAddress + " in Subnet " + subnetAddress;

                    String message = "MAC-IP change detected for MAC " + macAddress + " in subnet " + subnetAddress
                            + " with new IP " + ipAddress + ".";

                    traceOrgCommonUtil.sendMail(title, message);
                }
            }
        }
        catch (Exception exception)
        {
            _logger.error(exception);
        }
    }

    public TraceOrgAlertStream saveAlertStream(Long subnetId, String alertType, String message, String subnetAddress, Boolean status)
    {
        TraceOrgAlertStream alertStream = new TraceOrgAlertStream();

        alertStream.setSubnetId(subnetId);

        alertStream.setAlertType(alertType);

        alertStream.setMessage(message);

        alertStream.setSubnet(subnetAddress);

        alertStream.setTimestamp(new Date());

        alertStream.setStatus(status);

        return traceOrgAlertStreamRepository.save(alertStream);
    }

    public TraceOrgAlertStream updateAlertStream(TraceOrgAlertStream alertStream, String message, Boolean status)
    {
        if(alertStream != null)
        {
            alertStream.setMessage(message);

            alertStream.setStatus(status);

            alertStream.setTimestamp(new Date());

            return traceOrgAlertStreamRepository.save(alertStream);
        }
        else
        {
            return null;
        }
    }
    public static String generateHtmlTable(String message, String header, List<String> data)
    {
        StringBuilder mailBody = new StringBuilder();

        mailBody.append(message).append(".<br><br>");

        mailBody.append("<table style='border: 1px solid; border-collapse: collapse;'>");

        mailBody.append("<tr>");

        mailBody.append("<th style='border: 1px solid; padding: 5px;'>").append(header).append("</th>");

        mailBody.append("</tr>");

        for (String item : data)
        {
            mailBody.append("<tr>");

            mailBody.append("<td style='border: 1px solid; padding: 5px;'>").append(item).append("</td>");

            mailBody.append("</tr>");
        }

        mailBody.append("</table>");

        return mailBody.toString();
    }

}
