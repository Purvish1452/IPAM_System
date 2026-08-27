package com.motadata.traceorg.ipam.util;

import com.motadata.traceorg.ipam.logger.TraceOrgLogger;
import com.motadata.traceorg.ipam.entity.*;
import com.motadata.traceorg.ipam.entity.dashboard.TraceOrgVendor;
import com.motadata.traceorg.ipam.entity.dhcp.TraceOrgDhcpCredentialDetails;
import com.motadata.traceorg.ipam.entity.dhcp.TraceOrgDhcpUtilization;
import com.motadata.traceorg.ipam.entity.subnet.TraceOrgSubnetDetails;
import com.motadata.traceorg.ipam.entity.subnet.TraceOrgSubnetIpDetails;
import com.motadata.traceorg.ipam.repository.rogueDetection.TraceOrgRogueDetectionRepository;
import com.motadata.traceorg.ipam.services.TraceOrgService;
import org.apache.commons.net.util.SubnetUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

@SuppressWarnings("ALL")
public class TraceOrgCiscoDHCPServerUtil
{
    @Autowired
    private TraceOrgCommonUtil traceOrgCommonUtil;

    private static final TraceOrgLogger _logger = new TraceOrgLogger(TraceOrgCiscoDHCPServerUtil.class, "GUI / Cisco DHCP Util");

    public Response discover(TraceOrgDhcpCredentialDetails traceOrgDhcpCredentialDetails) throws Exception
    {
        Response response = new Response();

        boolean status;

        TraceOrgSSHUtil sshUtil = null;

        String host = null;

        if(traceOrgDhcpCredentialDetails != null)
        {
            try
            {
                host = TraceOrgCommonUtil.getStringValue(traceOrgDhcpCredentialDetails.getHostAddress());

                if(traceOrgDhcpCredentialDetails.getUserName() != null)
                {
                    _logger.debug("discovery request:" + host);

                    int port = 22;

                    if (traceOrgDhcpCredentialDetails.getPort() != null)
                    {
                        port = TraceOrgCommonUtil.getIntegerValue(traceOrgDhcpCredentialDetails.getPort());
                    }

                    _logger.info("Cisco DHCP server discovery received for host "+host + " and port "+port);

                    status = TraceOrgCommonUtil.isHostReachable(host);

                    if(status)
                    {
                        status =  TraceOrgCommonUtil.isPortReachable(host,port);

                        if(status)
                        {
                            sshUtil = new TraceOrgSSHUtil(host,port,TraceOrgCommonUtil.getStringValue(traceOrgDhcpCredentialDetails.getUserName()),traceOrgDhcpCredentialDetails.getPassword(),60);

                            if(sshUtil.init())
                            {
                                String output = sshUtil.executeCommand("sh ip arp");

                                _logger.debug("Cisco DHCP server discovery result for host "+host +" is "+output);

                                if (output != null && output.trim().length()>0 && !output.trim().startsWith("Line has invalid autocommand"))
                                {
                                    response.setMessage(TraceOrgMessageConstants.DHCP_CREDENTIAL_ADD_SUCCESS);

                                    response.setSuccess(TraceOrgCommonConstants.TRUE);
                                }
                                else
                                {
                                    response.setSuccess(TraceOrgCommonConstants.FALSE);

                                    response.setMessage("DHCP service not started on "+host);

                                    _logger.warn("DHCP service not started on "+host);
                                }
                            }

                            else
                            {
                                response.setSuccess(TraceOrgCommonConstants.FALSE);

                                response.setMessage(String.format(TraceOrgCommonConstants.ERROR_AUTH, host));

                                _logger.warn(String.format(TraceOrgCommonConstants.ERROR_AUTH, host));
                            }
                        }
                        else
                        {
                            response.setSuccess(TraceOrgCommonConstants.FALSE);

                            response.setMessage(String.format(TraceOrgCommonConstants.ERROR_SERVICE_DOWN, port, host));

                            _logger.warn(String.format(TraceOrgCommonConstants.ERROR_SERVICE_DOWN, port, host));
                        }
                    }
                    else
                    {
                        response.setSuccess(TraceOrgCommonConstants.FALSE);

                        response.setMessage(String.format(TraceOrgCommonConstants.ERROR_PING_FAILED, host));

                        _logger.warn(String.format(TraceOrgCommonConstants.ERROR_PING_FAILED, host));
                    }
                }
                else
                {
                    response.setSuccess(TraceOrgCommonConstants.FALSE);

                    response.setMessage("No credentials found");

                    _logger.info("No credentials found");
                }
            }

            catch (Exception exception)
            {
                _logger.error(exception);

                _logger.warn("failed to discover "+host);

                response.setSuccess(TraceOrgCommonConstants.FALSE);

                response.setMessage("failed to discover "+host);

            }
            finally
            {
                if(sshUtil !=null)
                {
                    sshUtil.destroy();
                }
            }

        }
        return response;
    }

    public boolean getDhcpServerStatistics(TraceOrgDhcpCredentialDetails traceOrgDhcpCredentialDetails,TraceOrgService traceOrgService) throws Exception
    {
        boolean result = false;

        TraceOrgSSHUtil sshUtil = null;

        int port = 22;

        String host = null;

        if(traceOrgDhcpCredentialDetails != null)
        {
            _logger.debug("cisco dhcp server statistics request received for :"+traceOrgDhcpCredentialDetails.getHostAddress());

            try
            {
                if (traceOrgDhcpCredentialDetails.getUserName() != null && traceOrgDhcpCredentialDetails.getHostAddress() != null)
                {
                    host = TraceOrgCommonUtil.getStringValue(traceOrgDhcpCredentialDetails.getHostAddress());

                    if (traceOrgDhcpCredentialDetails.getPort() != null)
                    {
                        port =  TraceOrgCommonUtil.getIntegerValue(traceOrgDhcpCredentialDetails.getPort());
                    }

                    sshUtil = new TraceOrgSSHUtil(host,port,TraceOrgCommonUtil.getStringValue(traceOrgDhcpCredentialDetails.getUserName()),traceOrgDhcpCredentialDetails.getPassword(),60);

                    String output;

                    String[] outputs;

                    if (sshUtil.init())
                    {
                        try
                        {
                            output=sshUtil.executeCommand("show ip dhcp server statistics");

                            _logger.debug("Cisco DHCP server statistics result for host "+host +" is "+output);

                            if(output!=null && !output.isEmpty() && !output.trim().startsWith("Line has invalid autocommand"))
                            {
                                outputs = output.trim().replace("Pool","$$").split("\\$\\$");

                                List<TraceOrgDhcpUtilization> traceOrgDhcpUtilizationList = (List<TraceOrgDhcpUtilization>)traceOrgService.commonQuery("","TraceOrgDhcpUtilization where dhcpCredentialDetailId = '"+traceOrgDhcpCredentialDetails.getId()+"'");

                                TraceOrgDhcpUtilization traceOrgDhcpUtilization =null;

                                if(traceOrgDhcpUtilizationList !=null && !traceOrgDhcpUtilizationList.isEmpty())
                                {
                                    traceOrgDhcpUtilization = traceOrgDhcpUtilizationList.get(0);
                                }
                                else
                                {
                                    traceOrgDhcpUtilization = new TraceOrgDhcpUtilization();
                                }

                                for(String receivedStatisticsDetails : outputs)
                                {
                                    if(receivedStatisticsDetails != null && !receivedStatisticsDetails.isEmpty())
                                    {
                                        String[] token = receivedStatisticsDetails.trim().replaceAll("\\r","").split("\\n");

                                        if(token.length > 0)
                                        {
                                            traceOrgDhcpUtilization.setAddressScopes(token[1].trim().split("\\s+")[2]);

                                            traceOrgDhcpUtilization.setDeclines(token[13].trim().split("\\s+")[1]);

                                            traceOrgDhcpUtilization.setReleases(token[14].trim().split("\\s+")[1]);

                                            traceOrgDhcpUtilization.setAcks(token[20].trim().split("\\s+")[1]);

                                            traceOrgDhcpUtilization.setNaks(token[21].trim().split("\\s+")[1]);

                                            traceOrgDhcpUtilization.setOffers(token[19].trim().split("\\s+")[1]);

                                            traceOrgDhcpUtilization.setRequests(token[12].trim().split("\\s+")[1]);

                                            traceOrgDhcpUtilization.setDiscovers(token[11].trim().split("\\s+")[1]);
                                        }

                                    }
                                }
                                traceOrgDhcpUtilization.setDhcpCredentialDetailId(traceOrgDhcpCredentialDetails);

                                result = traceOrgService.insert(traceOrgDhcpUtilization);
                            }
                        }
                        catch (Exception exception)
                        {
                            _logger.error(exception);
                        }
                    }
                }
            }
            catch (Exception exception)
            {
                _logger.error(exception);

                _logger.warn("failed to collect cisco dhcp server statistics details for "+host);
            }

            finally
            {
                if (sshUtil != null)
                {
                    sshUtil.destroy();
                }
            }

        }

        return result;
    }

    /**
     * IPAM-145 : System should have rogue device detection capability
     * Added logic for rogue detection during scanning.
     * @param traceOrgSubnetDetail
     * @param traceOrgService
     * @param traceOrgRogueDetectionRepository
     * @param rogueIps
     * @param traceOrgCommonUtil
     * @return
     * @throws Exception
     */
    //SUBNET  IP DETAILS
    public boolean getNetworkInterfaceForSpecificSubnet(TraceOrgSubnetDetails traceOrgSubnetDetail, TraceOrgService traceOrgService, TraceOrgRogueDetectionRepository traceOrgRogueDetectionRepository, List<String> rogueIps, TraceOrgCommonUtil traceOrgCommonUtil) throws Exception
    {
        boolean result = false;

        List<TraceOrgSubnetDetails> traceOrgSubnetDetailList = null;

        TraceOrgSSHUtil sshUtil = null;

        int port = 22;

        String host = null;

        TraceOrgDhcpCredentialDetails traceOrgDhcpCredentialDetails = traceOrgSubnetDetail.getTraceOrgDhcpCredentialDetailsId();

        if(traceOrgDhcpCredentialDetails != null)
        {
            _logger.debug("cisco dhcp server network interface from subnet request received for :"+traceOrgDhcpCredentialDetails.getHostAddress());

            try
            {
                if (traceOrgDhcpCredentialDetails.getUserName() != null && traceOrgDhcpCredentialDetails.getHostAddress() != null)
                {
                    host = TraceOrgCommonUtil.getStringValue(traceOrgDhcpCredentialDetails.getHostAddress());

                    if (traceOrgDhcpCredentialDetails.getPort() != null)
                    {
                        port =  TraceOrgCommonUtil.getIntegerValue(traceOrgDhcpCredentialDetails.getPort());
                    }

                    sshUtil = new TraceOrgSSHUtil(host,port,TraceOrgCommonUtil.getStringValue(traceOrgDhcpCredentialDetails.getUserName()),traceOrgDhcpCredentialDetails.getPassword(),60);

                    if (sshUtil.init())
                    {
                        traceOrgSubnetDetailList = getSubnetDetails(sshUtil,traceOrgService,host);
                    }
                    if(traceOrgSubnetDetailList !=null && !traceOrgSubnetDetailList.isEmpty())
                    {
                        for(TraceOrgSubnetDetails subnetDetail:traceOrgSubnetDetailList)
                        {
                            if(subnetDetail.getSubnetAddress().equals(traceOrgSubnetDetail.getSubnetAddress()))
                            {
                                getSubnetIpDetails(subnetDetail,sshUtil,traceOrgService,host, traceOrgRogueDetectionRepository, rogueIps, traceOrgCommonUtil);

                                result = true;
                            }
                        }
                    }

                }
            }
            catch (Exception exception)
            {
                _logger.error(exception);

                _logger.warn("failed to collect cisco dhcp server network interface from subnet for "+host);
            }

            finally
            {
                if (sshUtil != null)
                {
                    sshUtil.destroy();
                }
            }

        }

        return result;
    }

    private  List<TraceOrgSubnetDetails> getSubnetDetails(TraceOrgSSHUtil sshUtil, TraceOrgService traceOrgService, String host) throws Exception
    {
        String output;

        String[] outputs;

        List<TraceOrgSubnetDetails> traceOrgSubnetDetailsList = new ArrayList<>();

        try
        {
            if(sshUtil != null && sshUtil.reInit())
            {
                output=sshUtil.executeCommand("show ip interface brief");

                _logger.debug("Cisco DHCP server subnet details result for host "+host +" is "+output);

                if(output!=null && !output.isEmpty() && !output.trim().startsWith("Line has invalid autocommand"))
                {
                    outputs = output.trim().split("\\n");

                    for(String receivedInterfaceMetrics : outputs)
                    {
                        String[] token = receivedInterfaceMetrics.trim().split("\\s+");

                        if(token.length>0 && !token[0].contains("Interface") && !token[1].contains("unassigned"))
                        {
                            List<TraceOrgSubnetDetails> traceOrgSubnetDetailList = (List<TraceOrgSubnetDetails>)traceOrgService.commonQuery("",TraceOrgCommonConstants.SUBNET_BY_SUBNET_ADDRESS.replace(TraceOrgCommonConstants.SUBNET_ADDRESS_VALUE,token[1].substring(0,token[1].lastIndexOf("."))+".0"));

                            if(traceOrgSubnetDetailList!=null && !traceOrgSubnetDetailList.isEmpty())
                            {
                                TraceOrgSubnetDetails traceOrgSubnetDetails = traceOrgSubnetDetailList.get(0);

                                traceOrgSubnetDetails.setNetworkInterface(token[0]);

                                traceOrgSubnetDetailsList.add(traceOrgSubnetDetails);
                            }
                        }
                    }
                }
            }
        }
        catch (Exception exception)
        {
            _logger.error(exception);

            _logger.warn("failed to collect cisco dhcp server subnet details of host "+host);
        }

        return traceOrgSubnetDetailsList;
    }

    /**
     * IPAM-145 : System should have rogue device detection capability
     * mark authenticity based on mac and status.
     *
     * @param traceOrgSubnetDetails
     * @param sshUtil
     * @param traceOrgService
     * @param host
     * @param traceOrgRogueDetectionRepository
     * @param rogueIps
     * @param traceOrgCommonUtil
     * @throws Exception
     */
    private void getSubnetIpDetails(TraceOrgSubnetDetails traceOrgSubnetDetails, TraceOrgSSHUtil sshUtil, TraceOrgService traceOrgService, String host, TraceOrgRogueDetectionRepository traceOrgRogueDetectionRepository, List<String> rogueIps, TraceOrgCommonUtil traceOrgCommonUtil) throws Exception
    {
        TraceOrgSubnetUtil traceOrgSubnetUtil = new TraceOrgSubnetUtil();

        try
        {
            if(sshUtil != null && sshUtil.reInit())
            {
                String arpOutput = sshUtil.executeCommand("sh ip arp " + traceOrgSubnetDetails.getNetworkInterface());

                _logger.debug("Cisco DHCP server subnet ip address result for host "+host +" is "+arpOutput);

                if (arpOutput != null && !arpOutput.isEmpty() && !arpOutput.trim().startsWith("Line has invalid autocommand"))
                {
                    String[] arpOutputs = arpOutput.trim().split("\\n");

                    SubnetUtils subnetUtils = new SubnetUtils(traceOrgSubnetDetails.getSubnetAddress() + "/"+ traceOrgSubnetDetails.getSubnetCidr());

                    String[] ipArrayList = subnetUtils.getInfo().getAllAddresses();

                    List<String> ipList = new ArrayList<>(Arrays.asList(ipArrayList));

                    for (String arpDetails : arpOutputs)
                    {
                        String[] arpToken = arpDetails.trim().split("\\s+");

                        if (arpToken.length > 0 && !arpToken[0].contains("Protocol") && !arpToken[1].contains("Address") && !arpToken[3].contains("Incomplete"))
                        {
                            ipList.remove(arpToken[1]);

                            TraceOrgSubnetIpDetails traceOrgSubnetIpDetails = new TraceOrgSubnetIpDetails();

                            traceOrgSubnetIpDetails.setSubnetId(traceOrgSubnetDetails);

                            traceOrgSubnetIpDetails.setIpAddress(arpToken[1]);

                            String tempMac = arpToken[3].replaceAll("\\.","");

                            traceOrgSubnetIpDetails.setMacAddress(tempMac.substring(0,2)+":"+tempMac.substring(2,4)+":"+tempMac.substring(4,6)+":"+tempMac.substring(6,8)+":"+tempMac.substring(8,10)+":"+tempMac.substring(10,12));

                            List<TraceOrgVendor> vendorDetails = (List<TraceOrgVendor>)traceOrgService.commonQuery("",TraceOrgCommonConstants.VENDOR_BY_MAC_ADDRESS.replace(TraceOrgCommonConstants.VENDOR_MAC_VALUE,traceOrgSubnetIpDetails.getMacAddress().substring(0,8).replace(":","")));

                            if(vendorDetails != null && !vendorDetails.isEmpty())
                            {
                                traceOrgSubnetIpDetails.setDeviceType(vendorDetails.get(0).getVendorName());
                            }
                            traceOrgSubnetIpDetails.setStatus(TraceOrgCommonConstants.USED);

                            traceOrgSubnetIpDetails.setAuthenticity("discoverd");

                            traceOrgSubnetIpDetails.setLastAliveTime(new Date());

                            traceOrgSubnetUtil.insertSubnetIp(traceOrgSubnetIpDetails,traceOrgService, traceOrgRogueDetectionRepository, rogueIps, traceOrgSubnetDetails, traceOrgCommonUtil);
                        }
                    }

                    for(String ip : ipList)
                    {
                        TraceOrgSubnetIpDetails traceOrgSubnetIpDetails = new TraceOrgSubnetIpDetails();

                        traceOrgSubnetIpDetails.setSubnetId(traceOrgSubnetDetails);

                        traceOrgSubnetIpDetails.setIpAddress(ip);

                        traceOrgSubnetIpDetails.setStatus(TraceOrgCommonConstants.AVAILABLE);

                        traceOrgSubnetIpDetails.setAuthenticity("-");

                        traceOrgSubnetUtil.insertSubnetIp(traceOrgSubnetIpDetails,traceOrgService, traceOrgRogueDetectionRepository, rogueIps, traceOrgSubnetDetails, traceOrgCommonUtil);
                    }
                }
            }
        }
        catch (Exception exception)
        {
            _logger.error(exception);

            _logger.warn("failed to collect cisco dhcp server subnet ip address for "+host);
        }
    }

     //SUBNET DETAILS
    public List<TraceOrgSubnetDetails> discoveryForSubnet(TraceOrgDhcpCredentialDetails traceOrgDhcpCredentialDetails,TraceOrgService traceOrgService) throws Exception
    {
        List<TraceOrgSubnetDetails> traceOrgSubnetDetails = null;

        TraceOrgSSHUtil sshUtil = null;

        int port = 22;

        String host = null;

        if(traceOrgDhcpCredentialDetails != null)
        {
            _logger.debug("cisco dhcp server discovery for subnet request received for: "+traceOrgDhcpCredentialDetails.getHostAddress());

            try
            {
                if (traceOrgDhcpCredentialDetails.getUserName() != null && traceOrgDhcpCredentialDetails.getHostAddress() != null)
                {
                    host = TraceOrgCommonUtil.getStringValue(traceOrgDhcpCredentialDetails.getHostAddress());

                    if (traceOrgDhcpCredentialDetails.getPort() != null)
                    {
                        port =  TraceOrgCommonUtil.getIntegerValue(traceOrgDhcpCredentialDetails.getPort());
                    }

                    sshUtil = new TraceOrgSSHUtil(host,port,TraceOrgCommonUtil.getStringValue(traceOrgDhcpCredentialDetails.getUserName()),traceOrgDhcpCredentialDetails.getPassword(),60);

                    if (sshUtil.init())
                    {
                        traceOrgSubnetDetails =  getInterfaceDetailsForSubnet(sshUtil,traceOrgDhcpCredentialDetails,traceOrgService,host);
                    }

                }
            }
            catch (Exception exception)
            {
                _logger.error(exception);

                _logger.warn("failed to collect cisco dhcp server discovery of subnet for "+host);
            }

            finally
            {
                if (sshUtil != null)
                {
                    sshUtil.destroy();
                }
            }

        }
        return traceOrgSubnetDetails;
    }

    private List<TraceOrgSubnetDetails> getInterfaceDetailsForSubnet(TraceOrgSSHUtil sshUtil, TraceOrgDhcpCredentialDetails traceOrgDhcpCredentialDetails, TraceOrgService traceOrgService, String host) throws Exception
    {
        List<TraceOrgSubnetDetails> traceOrgSubnetDetailList = new ArrayList<>();

        String output;

        String[] outputs;

        try
        {
            if(sshUtil != null && sshUtil.reInit())
            {
                output=sshUtil.executeCommand("show ip interface brief");

                _logger.debug("Cisco DHCP server interface result for subnet of host "+host +" is "+output);

                if(output!=null && !output.isEmpty() && !output.trim().startsWith("Line has invalid autocommand"))
                {
                    outputs = output.trim().split("\\n");

                    for(String receivedInterfaceMetrics : outputs)
                    {
                        String[] token = receivedInterfaceMetrics.trim().split("\\s+");

                        if(token.length>0 && !token[0].contains("Interface") && !token[1].contains("unassigned"))
                        {
                            TraceOrgSubnetDetails traceOrgSubnetDetails = getSubnetNetworkDetails(token[0],sshUtil,traceOrgDhcpCredentialDetails,traceOrgService,host);

                            if(traceOrgSubnetDetails!=null && traceOrgSubnetDetails.getSubnetAddress()!=null && !traceOrgSubnetDetails.getSubnetAddress().trim().isEmpty())
                            {
                                traceOrgSubnetDetailList.add(traceOrgSubnetDetails);
                            }
                        }
                    }
                }
            }
        }
        catch (Exception exception)
        {
            _logger.error(exception);

            _logger.warn("failed to collect cisco dhcp server interface result of subnet for host "+host);
        }
        return  traceOrgSubnetDetailList;
    }


    private TraceOrgSubnetDetails getSubnetNetworkDetails(String interFace, TraceOrgSSHUtil sshUtil, TraceOrgDhcpCredentialDetails traceOrgDhcpCredentialDetails, TraceOrgService traceOrgService, String host) throws Exception
    {
        TraceOrgSubnetDetails traceOrgSubnetDetails = new TraceOrgSubnetDetails();

        String output;

        String[] outputs;

        String[] token;

        try
        {
            if(sshUtil != null && sshUtil.reInit())
            {
                output=sshUtil.executeCommand("show interface "+interFace);

                _logger.debug("Cisco DHCP server subnet network  result for host "+host +" is "+output);

                if(output!=null && !output.isEmpty())
                {
                    outputs = output.trim().split("\\n");

                    for(String receivedSubnetDetails : outputs)
                    {
                        if(receivedSubnetDetails != null && !receivedSubnetDetails.isEmpty() && receivedSubnetDetails.trim().startsWith("Internet address"))
                        {
                            token = receivedSubnetDetails.trim().split("\\s+");

                            if(token.length>0)
                            {
                                if(traceOrgService.isExist(TraceOrgCommonConstants.TRACE_ORG_SUBNET_DETAILS,TraceOrgCommonConstants.SUBNET_ADDRESS,token[3].trim().split("\\/")[0].substring(0,(token[3].trim().split("\\/")[0].lastIndexOf(".")))+".0"))
                                {
                                    _logger.debug(token[3].trim().split("\\/")[0].substring(0,(token[3].trim().split("\\/")[0].lastIndexOf(".")))+".0" + " is Already Exist");
                                }
                                else
                                {
                                    traceOrgSubnetDetails.setSubnetAddress(token[3].trim().split("\\/")[0].substring(0,(token[3].trim().split("\\/")[0].lastIndexOf(".")))+".0");

                                    traceOrgSubnetDetails.setSubnetCidr(Integer.parseInt(token[3].trim().split("\\/")[1]));

                                    traceOrgSubnetDetails.setSubnetName(traceOrgSubnetDetails.getSubnetAddress());

                                    traceOrgSubnetDetails.setSubnetMask(this.traceOrgCommonUtil.getSubnetMask(traceOrgSubnetDetails.getSubnetAddress(),traceOrgSubnetDetails.getSubnetCidr()));

                                    traceOrgSubnetDetails.setTotalIp(this.traceOrgCommonUtil.countTotalIp(traceOrgSubnetDetails.getSubnetAddress(),traceOrgSubnetDetails.getSubnetCidr()));

                                    traceOrgSubnetDetails.setAvailableIp(this.traceOrgCommonUtil.countTotalIp(traceOrgSubnetDetails.getSubnetAddress(),traceOrgSubnetDetails.getSubnetCidr())-2);

                                    traceOrgSubnetDetails.setUsedIp(0L);

                                    traceOrgSubnetDetails.setTransientIp(0L);
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
        return traceOrgSubnetDetails;
    }
}