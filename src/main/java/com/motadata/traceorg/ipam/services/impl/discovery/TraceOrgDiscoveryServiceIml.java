package com.motadata.traceorg.ipam.services.impl.discovery;

import com.google.common.base.Strings;
import com.motadata.traceorg.ipam.entity.discovery.TraceOrgDiscoveredSubnet;
import com.motadata.traceorg.ipam.entity.discovery.TraceOrgGateway;
import com.motadata.traceorg.ipam.entity.subnet.TraceOrgSubnetDetails;
import com.motadata.traceorg.ipam.logger.TraceOrgLogger;
import com.motadata.traceorg.ipam.repository.dashboard.TraceOrgCategoryRepository;
import com.motadata.traceorg.ipam.repository.discovery.TraceOrgDiscoveredSubnetRepository;
import com.motadata.traceorg.ipam.repository.discovery.TraceOrgGatewayRepository;
import com.motadata.traceorg.ipam.repository.event.TraceOrgEventRepository;
import com.motadata.traceorg.ipam.repository.subnet.TraceOrgSubnetDetailsRepository;
import com.motadata.traceorg.ipam.services.discovery.TraceOrgDiscoveryService;
import com.motadata.traceorg.ipam.services.discovery.TraceOrgFlagService;
import com.motadata.traceorg.ipam.util.TraceOrgCommonConstants;
import com.motadata.traceorg.ipam.util.TraceOrgCommonUtil;
import com.motadata.traceorg.ipam.util.TraceOrgPythonProcessHandler;
import com.zaxxer.nuprocess.NuProcess;
import com.zaxxer.nuprocess.NuProcessBuilder;
import org.apache.commons.net.util.SubnetUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class TraceOrgDiscoveryServiceIml implements TraceOrgDiscoveryService
{
    private static final TraceOrgLogger _logger = new TraceOrgLogger(TraceOrgDiscoveryServiceIml.class, "Discovery Service");

    @Autowired
    TraceOrgDiscoveredSubnetRepository traceOrgDiscoveredSubnetRepository;

    @Autowired
    TraceOrgSubnetDetailsRepository traceOrgSubnetDetailsRepository;

    @Autowired
    TraceOrgEventRepository traceOrgEventRepository;

    @Autowired
    TraceOrgCategoryRepository traceOrgCategoryRepository;

    @Autowired
    TraceOrgCommonUtil traceOrgCommonUtil;

    @Autowired
    TraceOrgFlagService traceOrgFlagService;

    @Autowired
    TraceOrgGatewayRepository traceOrgGatewayRepository;

    private static Boolean isScanRunning = TraceOrgCommonConstants.FALSE;

    private static final Pattern SUBNET_PATTERN = Pattern.compile("Subnet\\s+Mask[ .:]+(\\d+\\.\\d+\\.\\d+\\.\\d+)");

    private static final Pattern IP_ADDRESS_PATTERN = Pattern.compile("IPv4\\s+Address[ .:]+(\\d+\\.\\d+\\.\\d+\\.\\d+)");

    private static final Pattern IP_PATTERN = Pattern.compile("\\d+\\.\\d+\\.\\d+\\.\\d+");

    /**
     * IPAM-146 System should have automatic discovery of IPv4 & IPv6 without manual configuration
     * Added method to get discovered subnets
     * */
    @Override
    public HashMap<String, Object> getDiscoveredSubnets()
    {
        HashMap<String, Object> result = new HashMap<>();

        try
        {
            result.put(TraceOrgCommonConstants.DATA, traceOrgDiscoveredSubnetRepository.findAll());

            result.put(TraceOrgCommonConstants.SUCCESS, TraceOrgCommonConstants.TRUE);
        }
        catch (Exception exception)
        {
            _logger.error(exception);
        }

        return result;
    }

    /**
     * IPAM-146 System should have automatic discovery of IPv4 & IPv6 without manual configuration
     * Added method to delete discovered subnet
     * */
    @Override
    public HashMap<String, Object> deleteDiscoveredSubnet(Integer id)
    {
        HashMap<String, Object> result = new HashMap<>();

        try
        {
            traceOrgDiscoveredSubnetRepository.delete(id);

            result.put(TraceOrgCommonConstants.SUCCESS, TraceOrgCommonConstants.TRUE);
        }
        catch (Exception exception)
        {
            _logger.error(exception);
        }

        return result;
    }

    /**
     * IPAM-146 System should have automatic discovery of IPv4 & IPv6 without manual configuration
     * Added method to get discovered subnet
     * */
    @Override
    public HashMap<String, Object> getDiscoveredSubnet(Integer id)
    {
        HashMap<String, Object> result = new HashMap<>();

        try
        {
            TraceOrgDiscoveredSubnet traceOrgDiscoveredSubnet = traceOrgDiscoveredSubnetRepository.findOne(id);

            if(traceOrgDiscoveredSubnet != null)
            {
                TraceOrgSubnetDetails subnetDetails = new TraceOrgSubnetDetails();

                subnetDetails.setSubnetName(traceOrgDiscoveredSubnet.getSubnet());

                subnetDetails.setSubnetAddress(traceOrgDiscoveredSubnet.getSubnet());

                subnetDetails.setSubnetMask(traceOrgDiscoveredSubnet.getSubnetMask());

                int mask = getSubnetMaskLength(traceOrgDiscoveredSubnet.getSubnetMask());

                subnetDetails.setMaskInfo(traceOrgDiscoveredSubnet.getSubnet() + "/" + mask);

                subnetDetails.setSubnetCidr(mask);

                subnetDetails.setGatewayId(traceOrgDiscoveredSubnet.getGatewayId());

                subnetDetails.setIsLocalSubnet(false);

                result.put(TraceOrgCommonConstants.DATA, subnetDetails);

                result.put(TraceOrgCommonConstants.SUCCESS, TraceOrgCommonConstants.TRUE);
            }
        }
        catch (Exception exception)
        {
            _logger.error(exception);
        }

        return result;
    }

    /**
     * IPAM-146 System should have automatic discovery of IPv4 & IPv6 without manual configuration
     * Added method to scan gateway
     * */
    @Override
    public HashMap<String, Object> scanGateway(Long id)
    {
        HashMap<String, Object> result = new HashMap<>();

        try
        {
            if(!isScanRunning)
            {
                TraceOrgGateway traceOrgGateway = traceOrgGatewayRepository.findOne(id);

                if(traceOrgGateway != null)
                {
                    updateGatewayStatus(traceOrgGateway, TraceOrgCommonConstants.GATEWAY_RUNNING_STATUS);

                    setIsScanRunning(TraceOrgCommonConstants.TRUE);

                    new Thread(() -> scanGateway(traceOrgGateway)).start();

                    result.put(TraceOrgCommonConstants.SUCCESS, TraceOrgCommonConstants.TRUE);

                    result.put(TraceOrgCommonConstants.MESSAGE, "Gateway Scan Started!");
                }
            }
            else
            {
                result.put(TraceOrgCommonConstants.SUCCESS, TraceOrgCommonConstants.FALSE);

                result.put(TraceOrgCommonConstants.MESSAGE, "Gateway Scan already running!");
            }
        }
        catch (Exception exception)
        {
            _logger.error(exception);
        }

        return result;
    }

    /**
     * IPAM-146 System should have automatic discovery of IPv4 & IPv6 without manual configuration
     * Added method to scan gateway
     * */
    private void scanGateway(TraceOrgGateway traceOrgGateway)
    {
        boolean status = TraceOrgCommonConstants.FALSE;

        try
        {
            HashMap<String, String> credentialContext = traceOrgGateway.getCredentialContext();

            if(credentialContext != null && !credentialContext.isEmpty())
            {
                String pluginLocation = TraceOrgCommonConstants.IPAM_DIR + TraceOrgCommonConstants.PATH_SEPARATOR
                        +"python-engine" + TraceOrgCommonConstants.PATH_SEPARATOR
                        + "com" + TraceOrgCommonConstants.PATH_SEPARATOR
                        + "motadata" + TraceOrgCommonConstants.PATH_SEPARATOR
                        + "traceorg" + TraceOrgCommonConstants.PATH_SEPARATOR
                        + "python" + TraceOrgCommonConstants.PATH_SEPARATOR
                        + "scansubnet.py";

                if (new File(pluginLocation).exists())
                {
                    List<String> defaultArguments = new ArrayList<>();

                    defaultArguments.add(TraceOrgCommonConstants.IPAM_DIR + TraceOrgCommonConstants.PATH_SEPARATOR + "python" + TraceOrgCommonConstants.PATH_SEPARATOR + "python");

                    defaultArguments.add(pluginLocation);

                    defaultArguments.add(TraceOrgCommonUtil.getJSON(credentialContext));

                    NuProcessBuilder nuProcessBuilder = new NuProcessBuilder(defaultArguments);

                    Path path = Paths.get(TraceOrgCommonConstants.IPAM_DIR + TraceOrgCommonConstants.PATH_SEPARATOR + "python");

                    _logger.debug("Python processs arguments " +  defaultArguments);

                    TraceOrgPythonProcessHandler pythonHandler = new TraceOrgPythonProcessHandler();

                    nuProcessBuilder.setCwd(path);

                    nuProcessBuilder.setProcessListener(pythonHandler);

                    NuProcess nuProcess = nuProcessBuilder.start();

                    int exitCode = nuProcess.waitFor(TraceOrgCommonConstants.REMOTE_SUBNET_SCAN_TIMEOUT, TimeUnit.SECONDS);

                    HashMap<String, Object> response = pythonHandler.getPythonResult();

                    _logger.debug("Python Process result: " + TraceOrgCommonUtil.getJSON(response));

                    _logger.debug("Python Process exited with code: " + exitCode);

                    if(response != null && response.get(TraceOrgCommonConstants.RESULT) != null)
                    {
                        HashMap<String, String> subnets = (HashMap<String, String>) response.get(TraceOrgCommonConstants.RESULT);

                        if(subnets != null && !subnets.isEmpty())
                        {
                            addDiscoveredSubnets(subnets, traceOrgGateway);
                        }

                        status = TraceOrgCommonConstants.TRUE;
                    }
                }
                else
                {
                    _logger.warn("scansubnet.py file not found!");
                }
            }
            else
            {
                _logger.warn("Credential context could not be found for gatewayId : " +  traceOrgGateway.getId());
            }
        }
        catch (Exception exception)
        {
            _logger.error(exception);
        }

        updateGatewayStatus(traceOrgGateway,
                status ? TraceOrgCommonConstants.GATEWAY_SUCCESS_STATUS : TraceOrgCommonConstants.GATEWAY_FAILED_STATUS);

        setIsScanRunning(TraceOrgCommonConstants.FALSE);
    }

    /**
     * IPAM-146 System should have automatic discovery of IPv4 & IPv6 without manual configuration
     * Added method to update gateway status
     * */
    void updateGatewayStatus(TraceOrgGateway traceOrgGateway, String status)
    {
        try
        {
            traceOrgGateway.setStatus(status);

            if(!status.equals(TraceOrgCommonConstants.GATEWAY_RUNNING_STATUS))
            {
                traceOrgGateway.setPreviousScan(new Date());
            }

            traceOrgGatewayRepository.save(traceOrgGateway);
        }
        catch (Exception exception)
        {
            _logger.error(exception);
        }
    }

    /**
     * IPAM-146 System should have automatic discovery of IPv4 & IPv6 without manual configuration
     * Added method to add discovered subnets
     * */
    void addDiscoveredSubnets(HashMap<String, String> subnets, TraceOrgGateway traceOrgGateway)
    {
        try
        {
            for (String subnet : subnets.keySet())
            {
                List<TraceOrgDiscoveredSubnet> discoveredSubnets = traceOrgDiscoveredSubnetRepository.findBySubnetAndSubnetMask(subnet, subnets.get(subnet));

                if(discoveredSubnets == null || discoveredSubnets.isEmpty())
                {
                    TraceOrgDiscoveredSubnet traceOrgDiscoveredSubnet = new TraceOrgDiscoveredSubnet();

                    traceOrgDiscoveredSubnet.setSubnet(subnet);

                    traceOrgDiscoveredSubnet.setSubnetMask(subnets.get(subnet));

                    traceOrgDiscoveredSubnet.setGateway(traceOrgGateway.getGateway());

                    traceOrgDiscoveredSubnet.setGatewayId(traceOrgGateway.getId());

                    traceOrgDiscoveredSubnetRepository.save(traceOrgDiscoveredSubnet);
                }
                else
                {
                    _logger.warn("Subnet " + subnet + " already discovered for gateway " + traceOrgGateway.getGateway());
                }
            }
        }
        catch (Exception exception)
        {
            _logger.error(exception);
        }
    }

    /**
     * IPAM-146 System should have automatic discovery of IPv4 & IPv6 without manual configuration
     * Added method to auto discover local subnet by scanning ARP
     * */
    @Override
    public void autoDiscoverLocalSubnet()
    {
        try
        {
            List<TraceOrgSubnetDetails> traceOrgSubnetDetails = traceOrgSubnetDetailsRepository.findAll();

            if(traceOrgSubnetDetails == null || traceOrgSubnetDetails.isEmpty())
            {
                _logger.info("Auto Discovering : Discovery started for local subnet...");

                String ipAndSubnetMask = getIPAndSubnetMask();

                if(!Strings.isNullOrEmpty(ipAndSubnetMask))
                {
                    ArrayList<String> subnets = getQualifiedSubnet(ipAndSubnetMask);

                    if(!subnets.isEmpty())
                    {
                        addSubnet(subnets, ipAndSubnetMask);
                    }
                }
            }
            else
            {
                _logger.info("Auto Discovering : Subnets already discovered...");
            }

            traceOrgCommonUtil.setFlag(TraceOrgCommonConstants.IS_AUTO_DISCOVERED, true);
        }
        catch (Exception exception)
        {
            _logger.error(exception);
        }
    }

    /**
     * IPAM-146 System should have automatic discovery of IPv4 & IPv6 without manual configuration
     * Added method to get IP Address and Subnet Mask from ipconfig
     * */
    private String getIPAndSubnetMask()
    {
        String ipAndSubnetMask = null, ipAddress = null, subnetMask = null;

        try
        {
            _logger.info("Auto Discovering : Getting IP Address and Subnet Mask...");

            ProcessBuilder processBuilder = new ProcessBuilder("ipconfig");

            Process process = processBuilder.start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream())))
            {
                String line;

                while ((line = reader.readLine()) != null)
                {
                    Matcher ipMatcher = IP_ADDRESS_PATTERN.matcher(line);

                    Matcher subnetMatcher = SUBNET_PATTERN.matcher(line);

                    if (ipMatcher.find())
                    {
                        ipAddress = ipMatcher.group(1);
                    }

                    if (subnetMatcher.find())
                    {
                        subnetMask = subnetMatcher.group(1);
                    }

                    if (ipAddress != null && subnetMask != null)
                    {
                        _logger.info("Auto Discovering : IP Address: " + ipAddress + " Subnet Mask: " + subnetMask);

                        ipAndSubnetMask = ipAddress + TraceOrgCommonConstants.VALUE_SEPARATOR + subnetMask;

                        break;
                    }
                }
            }
        }
        catch (Exception exception)
        {
            _logger.error(exception);
        }

        return ipAndSubnetMask;
    }

    /**
     * IPAM-146 System should have automatic discovery of IPv4 & IPv6 without manual configuration
     * Method doing subnet discovery based on IP Address and Subnet Mask
     * */
    private ArrayList<String> getQualifiedSubnet(String ipAndSubnetMask)
    {
        String ip = ipAndSubnetMask.split(TraceOrgCommonConstants.VALUE_SEPARATOR_WITH_ESCAPE)[0];

        String subnetMask = ipAndSubnetMask.split(TraceOrgCommonConstants.VALUE_SEPARATOR_WITH_ESCAPE)[1];

        int subnetMaskLength = getSubnetMaskLength(subnetMask);

        return filterSubnetsWithIPs(
                getSubnets(ip, subnetMaskLength),
                fetchIpsFromARP());
    }

    /**
     * IPAM-146 System should have automatic discovery of IPv4 & IPv6 without manual configuration
     * Added method to get subnet mask length
     * by counting the number of 1s in binary
     * */
    public int getSubnetMaskLength(String subnetMask)
    {
        int mask = 0;

        try
        {
            String[] octets = subnetMask.split("\\.");


            for (String octet : octets)
            {
                int value = Integer.parseInt(octet);

                mask += Integer.bitCount(value); // Count the 1s in binary
            }

            _logger.info("Auto Discovering : Subnet Mask Length: " + mask);
        }
        catch (Exception exception)
        {
            _logger.error(exception);
        }

        return mask;
    }

    /**
     * IPAM-146 System should have automatic discovery of IPv4 & IPv6 without manual configuration
     * Added method to get status of gateway scan
     * */
    @Override
    public HashMap<String, Object> statusScanGateway()
    {
        HashMap<String, Object> result = new HashMap<>();

        if(isScanRunning)
        {
            result.put(TraceOrgCommonConstants.SUCCESS, TraceOrgCommonConstants.TRUE);
        }

        return result;
    }

    /**
     * IPAM-146 System should have automatic discovery of IPv4 & IPv6 without manual configuration
     * Added method to get subnets based on IP Address and Subnet Mask
     * by calculating the number of bits that can change
     * */
    public ArrayList<String> getSubnets(String ip, int subnetMaskLength)
    {
        ArrayList<String> subnets = new ArrayList<>();

        try
        {
            if (subnetMaskLength >= 24)
            {
                String cidrNotation = ip + "/" + subnetMaskLength;

                SubnetUtils subnetUtils = new SubnetUtils(cidrNotation);

                subnets.add(subnetUtils.getInfo().getNetworkAddress());
            }
            else if (subnetMaskLength >= 16)
            {
                String[] parts = ip.split("\\.");

                int baseThirdOctet = Integer.parseInt(parts[2]);

                int varyingBits = 24 - subnetMaskLength;  // Number of bits that can change

                int maxVariations = 1 << varyingBits;  // 2^varyingBits

                for (int i = 0; i < maxVariations; i++)
                {
                    int newThirdOctet = (baseThirdOctet & -(1 << varyingBits)) | i;

                    subnets.add(parts[0] + "." + parts[1] + "." + newThirdOctet + ".0");
                }
            }
        }
        catch (Exception exception)
        {
            _logger.error(exception);
        }

        _logger.info("Auto Discovering : Subnets: " + subnets);

        return subnets;
    }


    /**
     * IPAM-146 System should have automatic discovery of IPv4 & IPv6 without manual configuration
     * Added method to fetch IPs from ARP
     * */
    HashSet<String> fetchIpsFromARP()
    {
        HashSet<String> ips = new HashSet<>();

        try
        {
            _logger.info("Auto Discovering : Fetching IPs from ARP...");

            ProcessBuilder processBuilder = new ProcessBuilder("arp", "-a");

            Process process = processBuilder.start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream())))
            {
                String line;

                while ((line = reader.readLine()) != null)
                {
                    Matcher matcher = TraceOrgDiscoveryServiceIml.IP_PATTERN.matcher(line);

                    while (matcher.find())
                    {
                        ips.add(matcher.group());
                    }
                }
            }
        }
        catch (Exception exception)
        {
            _logger.error(exception);
        }

        _logger.info("Auto Discovering : IPs: " + ips);

        return ips;
    }

    /**
     * IPAM-146 System should have automatic discovery of IPv4 & IPv6 without manual configuration
     * Added method to filter subnets with IPs
     * by checking if the IP is in the subnet
     * */
    public ArrayList<String> filterSubnetsWithIPs(List<String> subnets, HashSet<String> ips)
    {
        ArrayList<String> result = new ArrayList<>();

        try
        {
            for (String subnet : subnets)
            {
                SubnetUtils subnetUtils = new SubnetUtils(subnet + "/24");

                SubnetUtils.SubnetInfo subnetInfo = subnetUtils.getInfo();

                for (String ip : ips)
                {
                    if (subnetInfo.isInRange(ip))
                    {
                        result.add(subnet);

                        break;
                    }
                }
            }
        }
        catch (Exception exception)
        {
            _logger.error(exception);
        }

        _logger.info("Auto Discovering : Qualified Subnets: " + result);

        return result;
    }

    /**
     * IPAM-146 System should have automatic discovery of IPv4 & IPv6 without manual configuration
     * Added method to add subnet
     * */
    public void addSubnet(ArrayList<String> subnets, String ipAndSubnetMask)
    {
        try
        {
            for (String subnet : subnets)
            {
                TraceOrgSubnetDetails traceOrgSubnetDetails = new TraceOrgSubnetDetails();

                traceOrgSubnetDetails.setSubnetMask(ipAndSubnetMask.split(TraceOrgCommonConstants.VALUE_SEPARATOR_WITH_ESCAPE)[1]);

                traceOrgSubnetDetails.setTraceOrgCategory(traceOrgCategoryRepository.findOne(TraceOrgCommonConstants.DEFAULT_GATEWAY_ID));

                traceOrgSubnetDetails.setCategoryId(TraceOrgCommonConstants.DEFAULT_GATEWAY_ID);

                traceOrgSubnetDetails.setCreatedBy(TraceOrgCommonConstants.SYSTEM_USER);

                traceOrgSubnetDetails.setCreatedDate(new Date());

                traceOrgSubnetDetails.setModifiedDate(new Date());

                traceOrgSubnetDetails.setSubnetName(subnet);

                traceOrgSubnetDetails.setSubnetAddress(subnet);

                traceOrgSubnetDetails.setIsLocalSubnet(true);

                traceOrgSubnetDetails.setSubnetCidr(24);

                traceOrgSubnetDetails.setTotalIp(256L);

                traceOrgSubnetDetails.setAvailableIp(254L);

                traceOrgSubnetDetails.setType("Normal");

                traceOrgSubnetDetails.setMaskInfo(subnet + "/" + "24");

                traceOrgSubnetDetailsRepository.save(traceOrgSubnetDetails);

                traceOrgCommonUtil.ipList(traceOrgSubnetDetails);

                traceOrgCommonUtil.logEvent("Auto Discovery",
                        "Subnet " + traceOrgSubnetDetails.getSubnetAddress() + " is added in IP Address Manager by system",
                        TraceOrgCommonConstants.WARNING_SEVERITY,
                        null);

                _logger.info("Auto Discovering : Subnet " + traceOrgSubnetDetails.getSubnetAddress() + " is added in IP Address Manager by system");

            }
        }
        catch (Exception exception)
        {
            _logger.error(exception);
        }
    }

    public static Boolean getIsScanRunning()
    {
        return isScanRunning;
    }

    public static void setIsScanRunning(Boolean isScanRunning)
    {
        TraceOrgDiscoveryServiceIml.isScanRunning = isScanRunning;
    }
}
