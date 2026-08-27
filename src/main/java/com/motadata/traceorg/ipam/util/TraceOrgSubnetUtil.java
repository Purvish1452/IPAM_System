package com.motadata.traceorg.ipam.util;

import com.google.common.base.Strings;
import com.motadata.traceorg.ipam.entity.rogueDetection.TraceOrgRogueDetection;
import com.motadata.traceorg.ipam.logger.TraceOrgLogger;
import com.motadata.traceorg.ipam.entity.dashboard.TraceOrgVendor;
import com.motadata.traceorg.ipam.entity.event.TraceOrgEvent;
import com.motadata.traceorg.ipam.entity.discovery.TraceOrgGateway;
import com.motadata.traceorg.ipam.entity.subnet.TraceOrgIPChangeLog;
import com.motadata.traceorg.ipam.entity.subnet.TraceOrgSubnetDetails;
import com.motadata.traceorg.ipam.entity.subnet.TraceOrgSubnetIpDetails;
import com.motadata.traceorg.ipam.repository.rogueDetection.TraceOrgRogueDetectionRepository;
import com.motadata.traceorg.ipam.services.TraceOrgService;
import com.zaxxer.nuprocess.NuProcess;
import com.zaxxer.nuprocess.NuProcessBuilder;
import org.apache.commons.net.util.SubnetUtils;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Hardik.
 */

@SuppressWarnings("ALL")
public class TraceOrgSubnetUtil
{
    private static final TraceOrgLogger _logger = new TraceOrgLogger(TraceOrgSubnetUtil.class, "GUI / Subnet Util");

    public static AtomicInteger dnsCircuitBreakCount = new AtomicInteger(0);

    public static AtomicBoolean isDnsCircuitBreak = new AtomicBoolean(TraceOrgCommonConstants.FALSE);

    /**
     * IPAM-128 Implementation | NTPC | New Requirement
     * Ping all IPs with Go routines.
     * IPAM-145 : System should have rogue device detection capability
     * added logic for set authenticity and get rogue ips.
     * */
    public boolean getIPFromSubnet(TraceOrgSubnetDetails traceOrgSubnetDetails, TraceOrgService traceOrgService, TraceOrgRogueDetectionRepository traceOrgRogueDetectionRepository, List<String> rogueIps, TraceOrgCommonUtil traceOrgCommonUtil)
    {
        boolean result = false;

        String subnetDetails = traceOrgSubnetDetails.getSubnetAddress() + "/"+ traceOrgSubnetDetails.getSubnetCidr();

        String[] allIpAddress;

        List<String> ipList = new ArrayList<>();

        try
        {
            if(TraceOrgCommonUtil.isIPv6Address(traceOrgSubnetDetails.getSubnetAddress()))
            {
                traceOrgSubnetDetails.setIpv6(true);

                result = getMACDetails(traceOrgSubnetDetails,ipList,traceOrgService, traceOrgRogueDetectionRepository, rogueIps, traceOrgCommonUtil);
            }
            else
            {
                SubnetUtils subnetUtils = new SubnetUtils(subnetDetails);

                subnetUtils.setInclusiveHostCount(true);

                String networkAddress= subnetUtils.getInfo().getNetworkAddress();

                if(traceOrgSubnetDetails.getSubnetAddress().trim().equals(networkAddress))
                {
                    allIpAddress = subnetUtils.getInfo().getAllAddresses();

                    ipList = Arrays.asList(allIpAddress);

                    result = getMACDetails(traceOrgSubnetDetails,ipList,traceOrgService, traceOrgRogueDetectionRepository, rogueIps, traceOrgCommonUtil);
                }
                else
                {
                    _logger.warn("INVALID NETWORK ADDRESS");
                }
            }
        }
        catch (Exception exception)
        {
            _logger.error(exception);
        }
        return result;
    }

    /**
     * IPAM-128 Implementation | NTPC | New Requirement
     * Added result parsing for netsh for IPv6.
     * IPAM-140 IPAM | Add v3 Support for SNMP in Remote Subnet
     * Refactor getMACDetails
     * The status will now change based on ping for the remote subnet (No need of SNMP)
     * IPAM-145 : System should have rogue device detection capability
     * added logic for set authenticity.
     * */
    private boolean getMACDetails(TraceOrgSubnetDetails traceOrgSubnetDetails, List<String> ipList, TraceOrgService traceOrgService, TraceOrgRogueDetectionRepository traceOrgRogueDetectionRepository, List<String> rogueIps, TraceOrgCommonUtil traceOrgCommonUtil) throws InterruptedException
    {

        boolean result = false;

        List<String> usedIpList = new ArrayList<>();

        List<TraceOrgSubnetIpDetails> updateIpDetailList = new ArrayList<>();

        try
        {
            if (traceOrgSubnetDetails.isAllowIcmp())
            {
                HashMap<String, Object> pingResult = pingAllIPs(traceOrgSubnetDetails, traceOrgService, ipList);

                isDnsCircuitBreak.set(TraceOrgCommonConstants.FALSE);

                if (traceOrgSubnetDetails.isLocalSubnet())
                {
                    scanLocalSubnet(traceOrgSubnetDetails, ipList, usedIpList, updateIpDetailList, traceOrgService, traceOrgCommonUtil);
                }
                else
                {
                    scanRemoteSubnet(traceOrgSubnetDetails, usedIpList, updateIpDetailList, traceOrgService, traceOrgCommonUtil);
                }

                updateStatusFromPing(pingResult, usedIpList, traceOrgSubnetDetails, updateIpDetailList, traceOrgCommonUtil);
            }

            for(String ip : ipList)
            {
                if(!usedIpList.contains(ip))
                {
                    TraceOrgSubnetIpDetails traceOrgSubnetIpDetails = new TraceOrgSubnetIpDetails();

                    traceOrgSubnetIpDetails.setIpAddress(ip);

                    traceOrgSubnetIpDetails.setStatus(TraceOrgCommonConstants.AVAILABLE);

                    traceOrgSubnetIpDetails.setPreviousStatus(TraceOrgCommonConstants.AVAILABLE);

                    traceOrgSubnetIpDetails.setAuthenticity("-");

                    traceOrgSubnetIpDetails.setSubnetId(traceOrgSubnetDetails);

                    updateIpDetailList.add(traceOrgSubnetIpDetails);
                }
            }

            result = insertSubnetIp(updateIpDetailList,traceOrgService,traceOrgRogueDetectionRepository, rogueIps, traceOrgCommonUtil, traceOrgSubnetDetails);
        }
        catch (Exception exception)
        {
            _logger.error(exception);
        }

        return  result;
    }

    /**
     * IPAM-145 : System should have rogue device detection capability
     * added logic for set authenticity.
     * @param pingResult
     * @param usedIpList
     * @param traceOrgSubnetDetails
     * @param updateIpDetailList
     * @param traceOrgCommonUtil
     */
    public void updateStatusFromPing(HashMap<String, Object> pingResult, List<String> usedIpList, TraceOrgSubnetDetails traceOrgSubnetDetails, List<TraceOrgSubnetIpDetails> updateIpDetailList, TraceOrgCommonUtil traceOrgCommonUtil)
    {
        try
        {
            if(pingResult != null && pingResult.get("up") != null)
            {
                ArrayList<String> upIpLists = (ArrayList<String>) pingResult.get("up");

                if(upIpLists != null && !upIpLists.isEmpty())
                {
                    for(String ip : upIpLists)
                    {
                        TraceOrgSubnetIpDetails traceOrgSubnetIpDetails = new TraceOrgSubnetIpDetails();

                        if (!usedIpList.contains(ip))
                        {
                            usedIpList.add(ip);

                            traceOrgSubnetIpDetails.setIpAddress(ip);

                            traceOrgSubnetIpDetails.setStatus(TraceOrgCommonConstants.USED);

                            traceOrgSubnetIpDetails.setAuthenticity("-");

                            traceOrgSubnetIpDetails.setLastAliveTime(new Date());

                            traceOrgSubnetIpDetails.setSubnetId(traceOrgSubnetDetails);

                            if(traceOrgSubnetDetails.isAllowDns() && !isDnsCircuitBreak.get())
                            {
                                scanDNS(traceOrgSubnetDetails, traceOrgSubnetIpDetails, traceOrgCommonUtil);
                            }

                            updateIpDetailList.add(traceOrgSubnetIpDetails);
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

    /**
     * IPAM-140 IPAM | Add v3 Support for SNMP in Remote Subnet
     * Added Method to get remoteSubnet result
     * It will take community & gateway from subnetDetails if gatewayId is not present
     *
     * IPAM-145 : System should have rogue device detection capability
     * added logic for set authenticity.
     * IPAM-149 : IPAM Roadmap : System should have alert notification module to configure different kind of alert notification
     * Now we will allow multiple ips for same mac.
     * */
    public void scanRemoteSubnet(TraceOrgSubnetDetails traceOrgSubnetDetails, List<String> usedIpList, List<TraceOrgSubnetIpDetails> updateIpDetailList, TraceOrgService traceOrgService, TraceOrgCommonUtil traceOrgCommonUtil)
    {
        HashMap<String,Object> metricDetails = new HashMap<>();

        try
        {
            if(traceOrgSubnetDetails.getGatewayId() != TraceOrgCommonConstants.NONE_GATEWAY_ID || traceOrgSubnetDetails.getGatewayId() == null)
            {
                HashMap<String, String> credentialContext = null;

                if(traceOrgSubnetDetails.getGatewayId() != null)
                {
                    TraceOrgGateway traceOrgGateway = (TraceOrgGateway) traceOrgService.getById(TraceOrgCommonConstants.TRACE_ORG_GATEWAY, traceOrgSubnetDetails.getGatewayId());

                    credentialContext = traceOrgGateway.getCredentialContext();
                }
                else if(!Strings.isNullOrEmpty(traceOrgSubnetDetails.getGatewayIp()) && !Strings.isNullOrEmpty(traceOrgSubnetDetails.getSnmpCommunity()))
                {
                    credentialContext = new HashMap<>();

                    credentialContext.put("version", "v2c");

                    credentialContext.put("gateway", traceOrgSubnetDetails.getGatewayIp());

                    credentialContext.put("community", traceOrgSubnetDetails.getSnmpCommunity());
                }

                if(credentialContext != null || !credentialContext.isEmpty())
                {
                    String pluginLocation = TraceOrgCommonConstants.IPAM_DIR + TraceOrgCommonConstants.PATH_SEPARATOR
                            +"python-engine" + TraceOrgCommonConstants.PATH_SEPARATOR
                            + "com" + TraceOrgCommonConstants.PATH_SEPARATOR
                            + "motadata" + TraceOrgCommonConstants.PATH_SEPARATOR
                            + "traceorg" + TraceOrgCommonConstants.PATH_SEPARATOR
                            + "python" + TraceOrgCommonConstants.PATH_SEPARATOR
                            + "remotesubnetdetails.py";

                    if (new File(pluginLocation).exists())
                    {
                        List<String> defaultArguments = new ArrayList<>();

                        defaultArguments.add(TraceOrgCommonConstants.IPAM_DIR + TraceOrgCommonConstants.PATH_SEPARATOR + "python" + TraceOrgCommonConstants.PATH_SEPARATOR + "python");

                        defaultArguments.add(pluginLocation);

                        defaultArguments.add(TraceOrgCommonUtil.getJSON(credentialContext));

                        defaultArguments.add(traceOrgSubnetDetails.getSubnetAddress());

                        defaultArguments.add(String.valueOf(traceOrgSubnetDetails.getSubnetCidr()));

                        NuProcessBuilder nuProcessBuilder = new NuProcessBuilder(defaultArguments);

                        Path path = Paths.get(TraceOrgCommonConstants.IPAM_DIR + TraceOrgCommonConstants.PATH_SEPARATOR + "python");

                        _logger.debug("Python processs arguments " +  defaultArguments);

                        TraceOrgPythonProcessHandler pythonHandler = new TraceOrgPythonProcessHandler();

                        nuProcessBuilder.setCwd(path);

                        nuProcessBuilder.setProcessListener(pythonHandler);

                        NuProcess nuProcess = nuProcessBuilder.start();

                        int exitCode = nuProcess.waitFor(TraceOrgCommonConstants.PROCESS_REQUEST_TIMEOUT, TimeUnit.SECONDS);

                        HashMap<String, Object> response = pythonHandler.getPythonResult();

                        _logger.debug("Python Process result: " + TraceOrgCommonUtil.getJSON(response));

                        _logger.debug("Python Process exited with code: " + exitCode);

                        List<HashMap<String, String>> ipMacList = (List<HashMap<String, String>>) response.get("result");

                        if (ipMacList != null && ipMacList.size() > 0)
                        {
                            for (HashMap<String, String> singleRecord : ipMacList)
                            {
                                TraceOrgSubnetIpDetails traceOrgSubnetIpDetails = new TraceOrgSubnetIpDetails();

                                String macAddress = singleRecord.values().iterator().next();

                                String ipAddress = singleRecord.keySet().iterator().next();

                                usedIpList.add(ipAddress);

                                metricDetails.put(ipAddress, macAddress);

                                traceOrgSubnetIpDetails.setIpAddress(ipAddress);

                                traceOrgSubnetIpDetails.setMacAddress(macAddress);

                                traceOrgSubnetIpDetails.setStatus(TraceOrgCommonConstants.USED);

                                traceOrgSubnetIpDetails.setAuthenticity("discovered");

                                traceOrgSubnetIpDetails.setLastAliveTime(new Date());

                                traceOrgSubnetIpDetails.setSubnetId(traceOrgSubnetDetails);

                                List<TraceOrgVendor> vendorDetails = (List<TraceOrgVendor>) traceOrgService.commonQuery("", TraceOrgCommonConstants.VENDOR_BY_MAC_ADDRESS.replace(TraceOrgCommonConstants.VENDOR_MAC_VALUE, traceOrgSubnetIpDetails.getMacAddress().substring(0, 8).replace(":", "")));

                                if (vendorDetails != null && !vendorDetails.isEmpty())
                                {
                                    traceOrgSubnetIpDetails.setDeviceType(vendorDetails.get(0).getVendorName());
                                }

                                if(traceOrgSubnetDetails.isAllowDns() && !isDnsCircuitBreak.get())
                                {
                                    scanDNS(traceOrgSubnetDetails, traceOrgSubnetIpDetails, traceOrgCommonUtil);
                                }

                                updateIpDetailList.add(traceOrgSubnetIpDetails);
                            }
                        }
                    }
                    else
                    {
                        _logger.warn("remotesubnetdetails.py file not found!");
                    }
                }
                else
                {
                    _logger.warn("Credential context could not be found for gatewayId : " +  traceOrgSubnetDetails.getGatewayId());
                }
            }
            else
            {
                _logger.warn("SNMP Gateway is not define..");
            }
        }
        catch (Exception exception)
        {
            _logger.error(exception);
        }
    }

     /**
      * IPAM-140 IPAM | Add v3 Support for SNMP in Remote Subnet
      * Added Method to get LocalSubnet result
      *
      * IPAM-145 : System should have rogue device detection capability
      * added logic for set authenticity.
      * IPAM-149 : IPAM Roadmap : System should have alert notification module to configure different kind of alert notification
      * Now we will allow multiple ips for same mac.
      * */
    public void scanLocalSubnet(TraceOrgSubnetDetails traceOrgSubnetDetails, List<String> ipList, List<String> usedIpList, List<TraceOrgSubnetIpDetails> updateIpDetailList, TraceOrgService traceOrgService, TraceOrgCommonUtil traceOrgCommonUtil)
    {
        String line;

        Process process;

        Runtime runtime = Runtime.getRuntime();

        HashMap<String,Object> metricDetails = new HashMap<>();

        try
        {
            if(traceOrgSubnetDetails.isIpv6())
            {
                process = runtime.exec(TraceOrgCommonConstants.IPV6_NETSH_COMMAND);
            }
            else
            {
                process = runtime.exec(TraceOrgCommonConstants.ARP_QUERY);
            }

            try(BufferedReader bufferedInputStream = new BufferedReader(new InputStreamReader(process.getInputStream())))
            {
                while ((line = bufferedInputStream.readLine())!=null)
                {
                    TraceOrgSubnetIpDetails traceOrgSubnetIpDetails = new TraceOrgSubnetIpDetails();

                    if (!line.isEmpty())
                    {
                        String[] outputs = line.trim().replace("(", "").replace(")", "").split("\\n");

                        for (String token : outputs)
                        {
                            String[] result = token.trim().replace("-", ":").split("\\s+");

                            if (!token.trim().contains("Interface:") && !token.trim().contains("Internet Address"))
                            {
                                if (ipList.contains(result[0]) || TraceOrgCommonUtil.isValidIp(traceOrgSubnetDetails, result[0]))
                                {
                                    if ((result.length> 2 && !result[1].trim().contains("incomplete") && !result[2].trim().equals("static")) || (result[0].trim().contains(":") && result[1].trim().contains(":")))
                                    {
                                        if (!traceOrgSubnetDetails.isIpv6() || (traceOrgSubnetDetails.isIpv6() && ((result[2].trim().toLowerCase().contains("reachable") && !result[2].trim().toLowerCase().contains("unreachable")) || result[2].trim().toLowerCase().contains("permanent") || result[2].trim().toLowerCase().contains("stale"))))
                                        {
                                            usedIpList.add(result[0]);

                                            metricDetails.put(result[0], result[1]);

                                            traceOrgSubnetIpDetails.setIpAddress(result[0]);

                                            traceOrgSubnetIpDetails.setMacAddress(result[1]);

                                            traceOrgSubnetIpDetails.setStatus(TraceOrgCommonConstants.USED);

                                            traceOrgSubnetIpDetails.setAuthenticity("discovered");

                                            traceOrgSubnetIpDetails.setLastAliveTime(new Date());

                                            traceOrgSubnetIpDetails.setSubnetId(traceOrgSubnetDetails);

                                            List<TraceOrgVendor> vendorDetails = (List<TraceOrgVendor>) traceOrgService.commonQuery("", TraceOrgCommonConstants.VENDOR_BY_MAC_ADDRESS.replace(TraceOrgCommonConstants.VENDOR_MAC_VALUE, traceOrgSubnetIpDetails.getMacAddress().substring(0, 8).replace(":", "")));

                                            if(vendorDetails != null && !vendorDetails.isEmpty())
                                            {
                                                traceOrgSubnetIpDetails.setDeviceType(vendorDetails.get(0).getVendorName());
                                            }

                                            if (traceOrgSubnetDetails.isAllowDns() && !isDnsCircuitBreak.get())
                                            {
                                               scanDNS(traceOrgSubnetDetails, traceOrgSubnetIpDetails, traceOrgCommonUtil);
                                            }

                                            updateIpDetailList.add(traceOrgSubnetIpDetails);
                                        }
                                    }
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
        catch (Exception exception)
        {
            _logger.error(exception);
        }
    }

    /**
     * IPAM-149 : IPAM Roadmap : System should have alert notification module to configure different kind of alert notification
     * Added code for FORWARD_LOOKUP_MISMATCH & FORWARD_LOOKUP_FAILED & REVERSE_LOOKUP_FAILED Alert
     * */
    public void scanDNS(TraceOrgSubnetDetails traceOrgSubnetDetails, TraceOrgSubnetIpDetails traceOrgSubnetIpDetails, TraceOrgCommonUtil traceOrgCommonUtil)
    {
        try
        {
            String ipToDns = TraceOrgCommonUtil.resolveHost(
                    TraceOrgCommonUtil.getStringValue(traceOrgSubnetIpDetails.getIpAddress()),
                    traceOrgSubnetDetails.getDnsAddress()
            );

            if (!Strings.isNullOrEmpty(ipToDns))
            {
                traceOrgSubnetIpDetails.setIpToDns(ipToDns);

                String dnsToIp = TraceOrgCommonUtil.resolveIp(ipToDns, traceOrgSubnetDetails.getDnsAddress(), traceOrgSubnetDetails.isIpv6());

                if (!Strings.isNullOrEmpty(dnsToIp))
                {
                    if (dnsToIp.equals(traceOrgSubnetIpDetails.getIpAddress()))
                    {
                        traceOrgSubnetIpDetails.setDnsToIp(dnsToIp);

                        traceOrgSubnetIpDetails.setDnsStatus(TraceOrgCommonConstants.SUCCESS);
                    }
                    else
                    {
                        traceOrgSubnetIpDetails.setDnsStatus(TraceOrgCommonConstants.FORWARD_DNS_IP_MISMATCH);

                        HashMap<String, Object> context = new HashMap<>();

                        context.put("subnetAddress", traceOrgSubnetDetails.getSubnetAddress());

                        context.put("dnsAddress", traceOrgSubnetDetails.getDnsAddress());

                        context.put("ipAddress", traceOrgSubnetIpDetails.getIpAddress());

                        context.put("dnsToIp", dnsToIp);

                        traceOrgCommonUtil.sendMessage(TraceOrgCommonConstants.ALERT_QUEUE, context, TraceOrgCommonConstants.FORWARD_LOOKUP_MISMATCH);
                    }
                }
                else
                {
                    traceOrgSubnetIpDetails.setDnsStatus(TraceOrgCommonConstants.FORWARD_DNS_FAILED);

                    HashMap<String, Object> context = new HashMap<>();

                    context.put("subnetAddress", traceOrgSubnetDetails.getSubnetAddress());

                    context.put("dnsAddress", traceOrgSubnetDetails.getDnsAddress());

                    context.put("ipAddress", traceOrgSubnetIpDetails.getIpAddress());

                    traceOrgCommonUtil.sendMessage(TraceOrgCommonConstants.ALERT_QUEUE, context, TraceOrgCommonConstants.FORWARD_LOOKUP_FAILED);
                }
            }
            else
            {
                traceOrgSubnetIpDetails.setDnsStatus(TraceOrgCommonConstants.REVERSE_DNS_FAILED);

                HashMap<String, Object> context = new HashMap<>();

                context.put("subnetAddress", traceOrgSubnetDetails.getSubnetAddress());

                context.put("dnsAddress", traceOrgSubnetDetails.getDnsAddress());

                context.put("ipAddress", traceOrgSubnetIpDetails.getIpAddress());

                traceOrgCommonUtil.sendMessage(TraceOrgCommonConstants.ALERT_QUEUE, context, TraceOrgCommonConstants.REVERSE_LOOKUP_FAILED);
            }
        }
        catch (Exception exception)
        {
            _logger.error(exception);
        }
    }

    /**
     * IPAM-128 Implementation | NTPC | New Requirement
     * Change log is added when insert subnet ip
     *
     * IPAM-145 : System should have rogue device detection capability
     * added logic for set authenticity and get rogue ips.
     * IPAM-149 : IPAM Roadmap : System should have alert notification module to configure different kind of alert notification
     * Added code for IP_STATE_CHANGE & IP_RESERVATION_CHANGE & IP_CONFLICT Alert
     * */
    public boolean insertSubnetIp(List<TraceOrgSubnetIpDetails> traceOrgSubnetIpDetailList, TraceOrgService traceOrgService, TraceOrgRogueDetectionRepository traceOrgRogueDetectionRepository, List<String> rogueIps, TraceOrgCommonUtil traceOrgCommonUtil, TraceOrgSubnetDetails traceOrgSubnetDetails) throws InterruptedException
    {
        List<TraceOrgSubnetIpDetails> updatedSubnetIpDetails = new ArrayList<>();

        List<TraceOrgIPChangeLog> ipChangeLogs = new ArrayList<>();

        _logger.debug("Update Start Time :" + new Date());

        for(TraceOrgSubnetIpDetails traceOrgSubnetIpDetails: traceOrgSubnetIpDetailList)
        {
            boolean result = false;

            try
            {
                List<TraceOrgSubnetIpDetails> traceOrgSubnetIpDetailsList = (List<TraceOrgSubnetIpDetails>) traceOrgService.commonQuery(TraceOrgCommonConstants.SUBNET_IP_DETAILS_BY_IP_ADDRESS.replace(TraceOrgCommonConstants.IP_ADDRESS_VALUE, traceOrgSubnetIpDetails.getIpAddress()) + " and subnetId = '" + traceOrgSubnetIpDetails.getSubnetId().getId() + "'");

                if(traceOrgSubnetIpDetailsList != null && !traceOrgSubnetIpDetailsList.isEmpty())
                {
                    TraceOrgSubnetIpDetails traceOrgSubnetIpDetailsExisted =  traceOrgSubnetIpDetailsList.get(0);

                    traceOrgSubnetIpDetailsExisted.setMacAddress(traceOrgSubnetIpDetails.getMacAddress());

                    setAuthenticity(traceOrgRogueDetectionRepository, traceOrgSubnetIpDetailsExisted, traceOrgSubnetIpDetails, rogueIps, traceOrgCommonUtil, traceOrgSubnetDetails);

                    if(traceOrgSubnetIpDetails.getStatus().equals(TraceOrgCommonConstants.USED) && traceOrgSubnetIpDetailsExisted.getMacAddress() != null)
                    {
                        if(traceOrgSubnetIpDetailsExisted.getPreviousMacAddress() != null && !traceOrgSubnetIpDetailsExisted.getPreviousMacAddress().isEmpty())
                        {
                            if(!traceOrgSubnetIpDetailsExisted.getPreviousMacAddress().equalsIgnoreCase(traceOrgSubnetIpDetailsExisted.getMacAddress()))
                            {
                                traceOrgSubnetIpDetailsExisted.setConflictMac(traceOrgSubnetIpDetailsExisted.getPreviousMacAddress());
                            }
                            else
                            {
                                traceOrgSubnetIpDetailsExisted.setConflictMac(null);
                            }

                            traceOrgSubnetIpDetailsExisted.setPreviousMacAddress(traceOrgSubnetIpDetailsExisted.getMacAddress());
                        }
                        else
                        {
                            traceOrgSubnetIpDetailsExisted.setPreviousMacAddress(traceOrgSubnetIpDetailsExisted.getMacAddress());
                        }
                    }

                    traceOrgSubnetIpDetailsExisted.setDescription(traceOrgSubnetIpDetails.getDescription());

                    traceOrgSubnetIpDetailsExisted.setDeviceType(traceOrgSubnetIpDetails.getDeviceType());

                    traceOrgSubnetIpDetailsExisted.setDnsStatus(traceOrgSubnetIpDetails.getDnsStatus());

                    traceOrgSubnetIpDetailsExisted.setHostName(traceOrgSubnetIpDetails.getHostName());

                    if(traceOrgSubnetIpDetails.getLastAliveTime() != null)
                    {
                        traceOrgSubnetIpDetailsExisted.setLastAliveTime(new Date(traceOrgSubnetIpDetails.getLastAliveTime()));
                    }

                    traceOrgSubnetIpDetailsExisted.setIpToDns(traceOrgSubnetIpDetails.getIpToDns());

                    traceOrgSubnetIpDetailsExisted.setDnsToIp(traceOrgSubnetIpDetails.getDnsToIp());

                    switch (traceOrgSubnetIpDetailsExisted.getStatus() + "," + traceOrgSubnetIpDetails.getStatus())
                    {
                        case TraceOrgCommonConstants.USED + "," + TraceOrgCommonConstants.AVAILABLE:

                            traceOrgSubnetIpDetailsExisted.setStatus(TraceOrgCommonConstants.TRANSIENT);

                            traceOrgSubnetIpDetailsExisted.setPreviousStatus(TraceOrgCommonConstants.USED);

                            traceOrgSubnetIpDetails.setModifiedDate(new Date());

                            break;

                        case TraceOrgCommonConstants.TRANSIENT + "," + TraceOrgCommonConstants.AVAILABLE:

                            if(traceOrgSubnetIpDetailsExisted.getLastAliveTime()!=null)
                            {
                                if((new Date().getTime() - new Date(traceOrgSubnetIpDetailsExisted.getLastAliveTime()).getTime())/(1000*60*60*24)>=7)
                                {
                                    traceOrgSubnetIpDetailsExisted.setStatus(TraceOrgCommonConstants.AVAILABLE);
                                }
                                else
                                {
                                    traceOrgSubnetIpDetailsExisted.setStatus(TraceOrgCommonConstants.TRANSIENT);
                                }
                            }
                            else
                            {
                                traceOrgSubnetIpDetailsExisted.setStatus(TraceOrgCommonConstants.TRANSIENT);
                            }

                            traceOrgSubnetIpDetailsExisted.setPreviousStatus(TraceOrgCommonConstants.TRANSIENT);

                            traceOrgSubnetIpDetails.setModifiedDate(new Date());

                            break;

                        case TraceOrgCommonConstants.TRANSIENT + "," + TraceOrgCommonConstants.USED:

                            traceOrgSubnetIpDetailsExisted.setStatus(TraceOrgCommonConstants.USED);

                            traceOrgSubnetIpDetailsExisted.setPreviousStatus(TraceOrgCommonConstants.TRANSIENT);

                            traceOrgSubnetIpDetails.setModifiedDate(new Date());

                            break;

                        case TraceOrgCommonConstants.RESERVED + "," + TraceOrgCommonConstants.USED:

                            traceOrgSubnetIpDetailsExisted.setStatus(TraceOrgCommonConstants.USED);

                            traceOrgSubnetIpDetailsExisted.setPreviousStatus(TraceOrgCommonConstants.RESERVED);

                            traceOrgSubnetIpDetails.setModifiedDate(new Date());

                            break;

                        case TraceOrgCommonConstants.RESERVED + "," + TraceOrgCommonConstants.AVAILABLE:

                            traceOrgSubnetIpDetailsExisted.setStatus(TraceOrgCommonConstants.RESERVED);

                            traceOrgSubnetIpDetailsExisted.setPreviousStatus(TraceOrgCommonConstants.RESERVED);

                            break;

                        default:

                            traceOrgSubnetIpDetailsExisted.setPreviousStatus(traceOrgSubnetIpDetailsExisted.getStatus());

                            traceOrgSubnetIpDetailsExisted.setStatus(traceOrgSubnetIpDetails.getStatus());

                            break;
                    }

                    if (!traceOrgSubnetIpDetailsExisted.getPreviousStatus().equalsIgnoreCase(traceOrgSubnetIpDetailsExisted.getStatus()))
                    {
                        TraceOrgIPChangeLog traceOrgIPChangeLog = new TraceOrgIPChangeLog(
                                TraceOrgCommonConstants.SYSTEM_USER,
                                traceOrgSubnetIpDetailsExisted.getId(),
                                traceOrgSubnetIpDetailsExisted.getSubnetId().getId(),
                                traceOrgSubnetIpDetails.getIpAddress(),
                                new Date(),
                                TraceOrgCommonConstants.CHANGE_LOG_MESSAGE.replace(TraceOrgCommonConstants.PREVIOUS_STATUS,traceOrgSubnetIpDetailsExisted.getPreviousStatus()).replace(TraceOrgCommonConstants.CURRENT_STATUS, traceOrgSubnetIpDetailsExisted.getStatus())
                        );

                        ipChangeLogs.add(traceOrgIPChangeLog);

                        HashMap<String, Object> context = new HashMap<>();

                        context.put("previousStatus", traceOrgSubnetIpDetailsExisted.getPreviousStatus());

                        context.put("currentStatus", traceOrgSubnetIpDetailsExisted.getStatus());
                        
                        context.put("ipAddress", traceOrgSubnetIpDetails.getIpAddress());
                        
                        context.put("subnetAddress", traceOrgSubnetDetails.getSubnetAddress());

                        traceOrgCommonUtil.sendMessage(TraceOrgCommonConstants.ALERT_QUEUE, context,
                                TraceOrgCommonConstants.IP_STATE_CHANGE 
                                        + TraceOrgCommonConstants.VALUE_SEPARATOR
                                        + TraceOrgCommonConstants.IP_RESERVATION_CHANGE);
                    }

                    updatedSubnetIpDetails.add(traceOrgSubnetIpDetailsExisted);

                    if(traceOrgSubnetIpDetailsExisted.getConflictMac() != null && traceOrgSubnetIpDetailsExisted.getMacAddress()!=null)
                    {
                        TraceOrgEvent traceOrgEvent =  new TraceOrgEvent();

                        traceOrgEvent.setTimestamp(new Date());

                        traceOrgEvent.setEventType("Conflict IP");

                        traceOrgEvent.setEventContext("IP Address " + traceOrgSubnetIpDetailsExisted.getIpAddress() + " with  Mac Address " + traceOrgSubnetIpDetailsExisted.getMacAddress() + " conflicted with  Mac Address " + traceOrgSubnetIpDetailsExisted.getConflictMac() + " in IP Address Manager " );

                        traceOrgEvent.setSeverity(0);

                        traceOrgService.insert(traceOrgEvent);

                        HashMap<String, Object> context = new HashMap<>();

                        context.put("macAddress", traceOrgSubnetIpDetailsExisted.getMacAddress());

                        context.put("conflictMacAddress", traceOrgSubnetIpDetailsExisted.getConflictMac());

                        context.put("ipAddress", traceOrgSubnetIpDetails.getIpAddress());

                        context.put("subnetAddress", traceOrgSubnetDetails.getSubnetAddress());

                        traceOrgCommonUtil.sendMessage(TraceOrgCommonConstants.ALERT_QUEUE, context, TraceOrgCommonConstants.IP_CONFLICT);
                    }

                }
            }
            catch (Exception exception)
            {
                _logger.error(exception);
            }
        }

        traceOrgService.updateAll(updatedSubnetIpDetails);

        traceOrgService.insertAll(ipChangeLogs);

        while (TraceOrgCommonUtil.getScanCount() != 0)
        {
            Thread.sleep(1000);
        }
        return true;
    }

    /**
     * IPAM-145 : System should have rogue device detection capability
     * added logic for set authenticity and get rogue ips.
     *
     * IPAM-172 : IPAM | Mismatched "DiscoveredAt" DateTime Format in Rogue Detection UI After Adding New Data or Importing Trusted MAC Addresses
     * Change the discovered At in date format instead of string.
     * IPAM-149 : IPAM Roadmap : System should have alert notification module to configure different kind of alert notification
     * Added code for MAC_IP_CHANGE Alert
     * @param traceOrgRogueDetectionRepository
     * @param traceOrgSubnetIpDetailsExisted
     * @param traceOrgSubnetIpDetails
     * @param rogueIps
     * @param traceOrgCommonUtil
     * @param traceOrgSubnetDetails
     */
    private void setAuthenticity(TraceOrgRogueDetectionRepository traceOrgRogueDetectionRepository, TraceOrgSubnetIpDetails traceOrgSubnetIpDetailsExisted, TraceOrgSubnetIpDetails traceOrgSubnetIpDetails, List<String> rogueIps, TraceOrgCommonUtil traceOrgCommonUtil, TraceOrgSubnetDetails traceOrgSubnetDetails)
    {
        try
        {
            if(traceOrgRogueDetectionRepository != null)
            {
                if(!Strings.isNullOrEmpty(traceOrgSubnetIpDetails.getMacAddress()))
                {
                    List<TraceOrgRogueDetection> rogueDetections = traceOrgRogueDetectionRepository.findByMacAddress(traceOrgSubnetIpDetails.getMacAddress());

                    List<String> ipList = new ArrayList<>();

                    for (TraceOrgRogueDetection detection : rogueDetections)
                    {
                        ipList.add(detection.getIpAddress());
                    }

                    if(!ipList.isEmpty())
                    {
                        HashMap<String, Object> context = new HashMap<>();

                        context.put("macAddress", traceOrgSubnetIpDetailsExisted.getMacAddress());

                        context.put("ipAddress", traceOrgSubnetIpDetails.getIpAddress());

                        context.put("subnetAddress", traceOrgSubnetDetails.getSubnetAddress());

                        context.put("ipList", ipList);

                        traceOrgCommonUtil.sendMessage(TraceOrgCommonConstants.ALERT_QUEUE, context, TraceOrgCommonConstants.MAC_IP_CHANGE);
                    }

                    if(traceOrgSubnetIpDetailsExisted.getPreviousMacAddress()!=null && !traceOrgSubnetIpDetailsExisted.getPreviousMacAddress().isEmpty() &&
                            !traceOrgSubnetIpDetailsExisted.getPreviousMacAddress().equalsIgnoreCase(traceOrgSubnetIpDetailsExisted.getMacAddress()))
                    {
                        TraceOrgRogueDetection traceOrgRogueDetection = traceOrgRogueDetectionRepository.findByMacAddressAndIpAddress(traceOrgSubnetIpDetailsExisted.getPreviousMacAddress(),traceOrgSubnetIpDetails.getIpAddress());

                        if(traceOrgRogueDetection != null)
                        {
                            traceOrgRogueDetection.setMacAddress(traceOrgSubnetIpDetailsExisted.getMacAddress());
                        }
                        else
                        {
                            traceOrgRogueDetection = new TraceOrgRogueDetection();

                            traceOrgRogueDetection.setMacAddress(traceOrgSubnetIpDetailsExisted.getMacAddress());

                            traceOrgRogueDetection.setIpAddress(traceOrgSubnetIpDetails.getIpAddress());

                            traceOrgRogueDetection.setDiscoveredAt(new Date(traceOrgSubnetIpDetails.getLastAliveTime()));

                            traceOrgRogueDetection.setNicType(traceOrgSubnetIpDetails.getDeviceType());
                        }

                        traceOrgRogueDetection.setAuthenticity("discovered");

                        traceOrgSubnetIpDetails.setAuthenticity("discovered");

                        traceOrgSubnetIpDetailsExisted.setAuthenticity("discovered");

                        traceOrgRogueDetectionRepository.save(traceOrgRogueDetection);
                    }
                    else
                    {
                        TraceOrgRogueDetection traceOrgRogueDetection = traceOrgRogueDetectionRepository.findByMacAddressAndIpAddress(traceOrgSubnetIpDetails.getMacAddress(),traceOrgSubnetIpDetails.getIpAddress());

                        if(traceOrgRogueDetection != null)
                        {
                            if(traceOrgRogueDetection.getAuthenticity().equalsIgnoreCase("rogue"))
                            {
                                rogueIps.add(traceOrgSubnetIpDetails.getIpAddress());

                                traceOrgSubnetIpDetails.setAuthenticity("rogue");

                                traceOrgSubnetIpDetailsExisted.setAuthenticity("rogue");
                            }
                            else if (traceOrgRogueDetection.getAuthenticity().equalsIgnoreCase("trusted"))
                            {
                                traceOrgSubnetIpDetails.setAuthenticity("trusted");

                                traceOrgSubnetIpDetailsExisted.setAuthenticity("trusted");
                            }
                            else if (traceOrgRogueDetection.getAuthenticity().equalsIgnoreCase("discovered"))
                            {
                                traceOrgSubnetIpDetails.setAuthenticity("discovered");

                                traceOrgSubnetIpDetailsExisted.setAuthenticity("discovered");
                            }
                        }
                        else
                        {
                            traceOrgRogueDetection = new TraceOrgRogueDetection();

                            traceOrgRogueDetection.setMacAddress(traceOrgSubnetIpDetailsExisted.getMacAddress());

                            traceOrgRogueDetection.setIpAddress(traceOrgSubnetIpDetails.getIpAddress());

                            traceOrgRogueDetection.setDiscoveredAt(new Date(traceOrgSubnetIpDetails.getLastAliveTime()));

                            traceOrgRogueDetection.setNicType(traceOrgSubnetIpDetails.getDeviceType());

                            traceOrgRogueDetection.setAuthenticity("discovered");

                            traceOrgSubnetIpDetails.setAuthenticity("discovered");

                            traceOrgSubnetIpDetailsExisted.setAuthenticity("discovered");

                            traceOrgRogueDetectionRepository.save(traceOrgRogueDetection);
                        }
                    }
                }
                else
                {
                    TraceOrgRogueDetection traceOrgRogueDetection = traceOrgRogueDetectionRepository.findByMacAddressAndIpAddress(traceOrgSubnetIpDetailsExisted.getPreviousMacAddress(),traceOrgSubnetIpDetails.getIpAddress());

                    if(traceOrgRogueDetection != null)
                    {
                        traceOrgRogueDetectionRepository.delete(traceOrgRogueDetection.getId());
                    }

                    traceOrgSubnetIpDetails.setAuthenticity("-");

                    traceOrgSubnetIpDetailsExisted.setAuthenticity("-");
                }
            }
        }
        catch (Exception exception)
        {
            _logger.error(exception);
        }
    }

    private void checkProcessStatus(LinkedHashMap<Thread, Long> processes)
    {
        try
        {
            for (Thread thread : processes.keySet())
            {
                processes.put(thread, processes.get(thread) - 1000);

                if(processes.get(thread) <= 0)
                {
                    try
                    {
                        thread.interrupt();
                    }
                    catch (Exception ignored){}

                    processes.remove(thread);
                }
            }
        }
        catch (Exception e)
        {
            _logger.error(e);
        }
    }

    /**
     * IPAM-128 Implementation | NTPC | New Requirement
     * Change log is added when insert subnet ip
     *
     * IPAM-145 : System should have rogue device detection capability
     * added logic for set authenticity and get rogue ips.
     * IPAM-149 : IPAM Roadmap : System should have alert notification module to configure different kind of alert notification
     * Added code for IP_STATE_CHANGE & IP_RESERVATION_CHANGE & IP_CONFLICT Alert
     * */
    public boolean insertSubnetIp(TraceOrgSubnetIpDetails traceOrgSubnetIpDetails, TraceOrgService traceOrgService, TraceOrgRogueDetectionRepository traceOrgRogueDetectionRepository, List<String> rogueIps, TraceOrgSubnetDetails traceOrgSubnetDetails, TraceOrgCommonUtil traceOrgCommonUtil)
    {
        boolean result = false;

        try
        {
            List<TraceOrgSubnetIpDetails> traceOrgSubnetIpDetailsList = (List<TraceOrgSubnetIpDetails>)traceOrgService.commonQuery(TraceOrgCommonConstants.SUBNET_IP_DETAILS_BY_IP_ADDRESS.replace(TraceOrgCommonConstants.IP_ADDRESS_VALUE,traceOrgSubnetIpDetails.getIpAddress()) + " and subnetId = '"+traceOrgSubnetIpDetails.getSubnetId().getId()+"'");

            if(traceOrgSubnetIpDetailsList != null && !traceOrgSubnetIpDetailsList.isEmpty())
            {
                TraceOrgSubnetIpDetails traceOrgSubnetIpDetailsExisted =  traceOrgSubnetIpDetailsList.get(0);

                traceOrgSubnetIpDetailsExisted.setMacAddress(traceOrgSubnetIpDetails.getMacAddress());

                setAuthenticity(traceOrgRogueDetectionRepository, traceOrgSubnetIpDetailsExisted, traceOrgSubnetIpDetails, rogueIps, traceOrgCommonUtil, traceOrgSubnetDetails);

                if(traceOrgSubnetIpDetails.getStatus().equals(TraceOrgCommonConstants.USED) && traceOrgSubnetIpDetailsExisted.getMacAddress()!=null && traceOrgSubnetIpDetails.getMacAddress()!=null)
                {
                    if(traceOrgSubnetIpDetailsExisted.getPreviousMacAddress()!=null && !traceOrgSubnetIpDetailsExisted.getPreviousMacAddress().isEmpty())
                    {
                        if(!traceOrgSubnetIpDetailsExisted.getPreviousMacAddress().equalsIgnoreCase(traceOrgSubnetIpDetailsExisted.getMacAddress()))
                        {
                            traceOrgSubnetIpDetailsExisted.setConflictMac(traceOrgSubnetIpDetailsExisted.getPreviousMacAddress());
                        }
                        else
                        {
                            traceOrgSubnetIpDetailsExisted.setConflictMac(null);
                        }
                        traceOrgSubnetIpDetailsExisted.setPreviousMacAddress(traceOrgSubnetIpDetailsExisted.getMacAddress());
                    }
                    else
                    {
                        traceOrgSubnetIpDetailsExisted.setPreviousMacAddress(traceOrgSubnetIpDetails.getMacAddress());
                    }
                }

                traceOrgSubnetIpDetailsExisted.setDescription(traceOrgSubnetIpDetails.getDescription());

                traceOrgSubnetIpDetailsExisted.setDeviceType(traceOrgSubnetIpDetails.getDeviceType());

                traceOrgSubnetIpDetailsExisted.setDnsStatus(traceOrgSubnetIpDetails.getDnsStatus());

                traceOrgSubnetIpDetailsExisted.setHostName(traceOrgSubnetIpDetails.getHostName());

                if(traceOrgSubnetIpDetails.getLastAliveTime()!=null)
                {
                    traceOrgSubnetIpDetailsExisted.setLastAliveTime(new Date(traceOrgSubnetIpDetails.getLastAliveTime()));
                }

                traceOrgSubnetIpDetailsExisted.setIpToDns(traceOrgSubnetIpDetails.getIpToDns());

                traceOrgSubnetIpDetailsExisted.setDnsToIp(traceOrgSubnetIpDetails.getDnsToIp());

                switch (traceOrgSubnetIpDetailsExisted.getStatus()+","+traceOrgSubnetIpDetails.getStatus())
                {

                    case TraceOrgCommonConstants.USED+","+TraceOrgCommonConstants.AVAILABLE:

                        traceOrgSubnetIpDetailsExisted.setStatus(TraceOrgCommonConstants.TRANSIENT);

                        traceOrgSubnetIpDetailsExisted.setPreviousStatus(TraceOrgCommonConstants.USED);

                        traceOrgSubnetIpDetails.setModifiedDate(new Date());

                        break;

                    case TraceOrgCommonConstants.TRANSIENT+","+TraceOrgCommonConstants.AVAILABLE:

                        if(traceOrgSubnetIpDetailsExisted.getLastAliveTime()!=null)
                        {
                            if((new Date().getTime() - new Date(traceOrgSubnetIpDetailsExisted.getLastAliveTime()).getTime())/(1000*60*60*24)>=7)
                            {
                                traceOrgSubnetIpDetailsExisted.setStatus(TraceOrgCommonConstants.AVAILABLE);
                            }
                            else
                            {
                                traceOrgSubnetIpDetailsExisted.setStatus(TraceOrgCommonConstants.TRANSIENT);
                            }
                        }
                        else
                        {
                            traceOrgSubnetIpDetailsExisted.setStatus(TraceOrgCommonConstants.TRANSIENT);
                        }

                        traceOrgSubnetIpDetailsExisted.setPreviousStatus(TraceOrgCommonConstants.TRANSIENT);

                        traceOrgSubnetIpDetails.setModifiedDate(new Date());

                        break;

                    case TraceOrgCommonConstants.TRANSIENT+","+TraceOrgCommonConstants.USED:

                        traceOrgSubnetIpDetailsExisted.setStatus(TraceOrgCommonConstants.USED);

                        traceOrgSubnetIpDetailsExisted.setPreviousStatus(TraceOrgCommonConstants.TRANSIENT);

                        traceOrgSubnetIpDetails.setModifiedDate(new Date());

                        break;
                    case TraceOrgCommonConstants.RESERVED+","+TraceOrgCommonConstants.USED:

                        traceOrgSubnetIpDetailsExisted.setStatus(TraceOrgCommonConstants.USED);

                        traceOrgSubnetIpDetailsExisted.setPreviousStatus(TraceOrgCommonConstants.RESERVED);

                        traceOrgSubnetIpDetails.setModifiedDate(new Date());

                        break;
                    case TraceOrgCommonConstants.RESERVED+","+TraceOrgCommonConstants.AVAILABLE:

                        traceOrgSubnetIpDetailsExisted.setStatus(TraceOrgCommonConstants.RESERVED);

                        traceOrgSubnetIpDetailsExisted.setPreviousStatus(TraceOrgCommonConstants.RESERVED);

                        break;

                    default:

                        traceOrgSubnetIpDetailsExisted.setPreviousStatus(traceOrgSubnetIpDetailsExisted.getStatus());

                        traceOrgSubnetIpDetailsExisted.setStatus(traceOrgSubnetIpDetails.getStatus());

                        break;
                }

                boolean updateStatus = traceOrgService.insert(traceOrgSubnetIpDetailsExisted);

                if (updateStatus)
                {
                    result = true;

                    if (!traceOrgSubnetIpDetailsExisted.getPreviousStatus().equalsIgnoreCase(traceOrgSubnetIpDetailsExisted.getStatus()))
                    {
                        TraceOrgIPChangeLog traceOrgIPChangeLog = new TraceOrgIPChangeLog(
                                TraceOrgCommonConstants.SYSTEM_USER,
                                traceOrgSubnetIpDetailsExisted.getId(),
                                traceOrgSubnetIpDetailsExisted.getSubnetId().getId(),
                                traceOrgSubnetIpDetails.getIpAddress(),
                                new Date(),
                                "Status: " + traceOrgSubnetIpDetailsExisted.getPreviousStatus() + " -> " + traceOrgSubnetIpDetailsExisted.getStatus()
                        );

                        traceOrgService.insert(traceOrgIPChangeLog);

                        HashMap<String, Object> context = new HashMap<>();

                        context.put("previousStatus", traceOrgSubnetIpDetailsExisted.getPreviousStatus());

                        context.put("currentStatus", traceOrgSubnetIpDetailsExisted.getStatus());

                        context.put("ipAddress", traceOrgSubnetIpDetails.getIpAddress());

                        context.put("subnetAddress", traceOrgSubnetDetails.getSubnetAddress());

                        traceOrgCommonUtil.sendMessage(TraceOrgCommonConstants.ALERT_QUEUE, context,
                                TraceOrgCommonConstants.IP_STATE_CHANGE
                                        + TraceOrgCommonConstants.VALUE_SEPARATOR
                                        + TraceOrgCommonConstants.IP_RESERVATION_CHANGE);
                    }

                    if(traceOrgSubnetIpDetailsExisted.getConflictMac() != null && traceOrgSubnetIpDetailsExisted.getMacAddress()!=null)
                    {
                        TraceOrgEvent traceOrgEvent =  new TraceOrgEvent();

                        traceOrgEvent.setTimestamp(new Date());

                        traceOrgEvent.setEventType("Conflict IP");

                        traceOrgEvent.setEventContext("IP Address "+traceOrgSubnetIpDetailsExisted.getIpAddress()+" with  Mac Address "+traceOrgSubnetIpDetailsExisted.getMacAddress()+" conflicted with  Mac Address "+traceOrgSubnetIpDetailsExisted.getConflictMac()+" in IP Address Manager " );

                        traceOrgEvent.setSeverity(0);

                        traceOrgService.insert(traceOrgEvent);

                        HashMap<String, Object> context = new HashMap<>();

                        context.put("macAddress", traceOrgSubnetIpDetailsExisted.getMacAddress());

                        context.put("conflictMacAddress", traceOrgSubnetIpDetailsExisted.getConflictMac());

                        context.put("ipAddress", traceOrgSubnetIpDetails.getIpAddress());

                        context.put("subnetAddress", traceOrgSubnetDetails.getSubnetAddress());

                        traceOrgCommonUtil.sendMessage(TraceOrgCommonConstants.ALERT_QUEUE, context, TraceOrgCommonConstants.IP_CONFLICT);
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


    /**
     * IPAM-128 Implementation | NTPC | New Requirement
     * Will create a context for go.exe and wait until the ping is complete or times out.
     *
     * @return
     */
    public HashMap<String, Object> pingAllIPs(TraceOrgSubnetDetails traceOrgSubnetDetails, TraceOrgService traceOrgService, List<String> ipList)
    {
        String fileName = null;

        NuProcess process = null;

        HashMap<String, Object> result =  new HashMap<>();

        try
        {
            List<TraceOrgSubnetIpDetails> traceOrgSubnetIpDetailsList = (List<TraceOrgSubnetIpDetails>) traceOrgService.commonQuery("",TraceOrgCommonConstants.TRACE_ORG_SUBNET_IP_DETAILS +" where subnetId = '"+traceOrgSubnetDetails.getId()+"' and deactiveStatus = false ");

            if(traceOrgSubnetIpDetailsList != null && !traceOrgSubnetIpDetailsList.isEmpty())
            {
                _logger.debug(traceOrgSubnetDetails.getSubnetAddress() + " Ping Start Time :: " + new Date());

                HashMap<String, Object> context = new HashMap<>();

                StringBuilder ipAddresses = new StringBuilder();

                for(TraceOrgSubnetIpDetails traceOrgSubnetIpDetails : traceOrgSubnetIpDetailsList)
                {
                    ipAddresses.append(traceOrgSubnetIpDetails.getIpAddress()).append(",");

                    if(traceOrgSubnetDetails.isIpv6())
                    {
                        ipList.add(traceOrgSubnetIpDetails.getIpAddress());
                    }
                }

                if(ipAddresses.length() > 0)
                {
                    ipAddresses.deleteCharAt(ipAddresses.length()-1);
                }

                context.put("ip-addresses", ipAddresses.toString());

                context.put("max-ping-check-timeout", TraceOrgCommonConstants.PING_TIMEOUT);

                context.put("max-ping-check-retry-count", TraceOrgCommonConstants.PING_RETRY_COUNT);

                context.put("max-concurrent-ping", String.valueOf(TraceOrgCommonConstants.MAX_CONCURRENT_PING));

                fileName = TraceOrgCommonUtil.writePluginContextFile(context);

                List<String> defaultArguments = new ArrayList<>();

                defaultArguments.add(TraceOrgCommonConstants.IPAM_DIR + TraceOrgCommonConstants.PATH_SEPARATOR + "go-engine.exe");

                defaultArguments.add(TraceOrgCommonConstants.IPAM_DIR + TraceOrgCommonConstants.PATH_SEPARATOR + "cache" + TraceOrgCommonConstants.PATH_SEPARATOR + fileName);

                NuProcessBuilder processBuilder = new NuProcessBuilder(defaultArguments);

                TraceOrgGoPingHandler goPingHandler = new TraceOrgGoPingHandler();

                processBuilder.setProcessListener(goPingHandler);

                process = processBuilder.start();

                int exitCode = process.waitFor(TraceOrgCommonConstants.PROCESS_REQUEST_TIMEOUT, TimeUnit.SECONDS);

                result = goPingHandler.getGoPingResult();

                _logger.debug("Go Process result: " + TraceOrgCommonUtil.getJSON(result));

                _logger.debug("Go Process exited with code: " + exitCode);

                _logger.debug(traceOrgSubnetDetails.getSubnetAddress() + " Ping End Time :: " + new Date());
            }
        }
        catch (Exception e)
        {
            _logger.error(e);
        }
        finally
        {
            try
            {
                File file = new File(TraceOrgCommonUtil.getIPAMPath() + TraceOrgCommonConstants.PATH_SEPARATOR  + "cache"  + TraceOrgCommonConstants.PATH_SEPARATOR + fileName);

                file.delete();

                Thread.sleep(10*1000);

                if(process != null)
                {
                    process.destroy(true);
                }
            }
            catch (Exception e)
            {
                _logger.error(e);
            }
        }

        return result;
    }
}