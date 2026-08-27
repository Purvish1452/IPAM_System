package com.motadata.traceorg.ipam.services.impl.supernet;

import com.google.common.base.Strings;
import com.motadata.traceorg.ipam.dto.supernet.TraceOrgSupernetDTO;
import com.motadata.traceorg.ipam.entity.dashboard.TraceOrgSupernetCategory;
import com.motadata.traceorg.ipam.entity.event.TraceOrgEvent;
import com.motadata.traceorg.ipam.entity.settings.TraceOrgUser;
import com.motadata.traceorg.ipam.entity.subnet.TraceOrgSubnetDetails;
import com.motadata.traceorg.ipam.entity.supernet.TraceOrgSupernetDetails;
import com.motadata.traceorg.ipam.logger.TraceOrgLogger;
import com.motadata.traceorg.ipam.repository.dashboard.TraceOrgSupernetCategoryRepository;
import com.motadata.traceorg.ipam.repository.event.TraceOrgEventRepository;
import com.motadata.traceorg.ipam.repository.subnet.TraceOrgSubnetDetailsRepository;
import com.motadata.traceorg.ipam.repository.supernet.TraceOrgSupernetDetailsRepository;
import com.motadata.traceorg.ipam.services.supernet.TraceOrgSupernetService;
import com.motadata.traceorg.ipam.util.TraceOrgCommonConstants;
import com.motadata.traceorg.ipam.util.TraceOrgCommonUtil;
import com.motadata.traceorg.ipam.util.TraceOrgMessageConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@SuppressWarnings("SpringAutowiredFieldsWarningInspection")
@Service
public class TraceOrgSupernetServiceImpl implements TraceOrgSupernetService
{
    @Autowired
    TraceOrgSubnetDetailsRepository traceOrgSubnetDetailsRepository;

    @Autowired
    TraceOrgSupernetDetailsRepository traceOrgSupernetDetailsRepository;

    @Autowired
    TraceOrgSupernetCategoryRepository traceOrgSupernetCategoryRepository;

    @Autowired
    TraceOrgEventRepository traceOrgEventRepository;

    @Autowired
    private TraceOrgCommonUtil traceOrgCommonUtil;

    private static final TraceOrgLogger _logger = new TraceOrgLogger(TraceOrgSupernetServiceImpl.class, "Supernet Service Impl");

    /**
     * IPAM-148 : System should have the ability to locate the available subnets inside a Supernet. This is to provide assistance to users when creating subnets inside an aggregated Network.
     * added the addSupernet method to add the supernet
     * @param accessToken
     * @param traceOrgSupernetDTO
     * @return
     */
    @Override
    public HashMap<String,Object> addSupernet(String accessToken, TraceOrgSupernetDTO traceOrgSupernetDTO)
    {
        HashMap<String,Object> result = new HashMap<>();

        try
        {
            if (accessToken != null)
            {
                String supernetIPAddress = traceOrgSupernetDTO.getNetworkAddress();

                int supernetMask = Integer.parseInt(traceOrgSupernetDTO.getNetworkMask());

                if (TraceOrgCommonUtil.isIPv4Address(supernetIPAddress) && TraceOrgCommonConstants.IPV4_PATTERN.matcher(supernetIPAddress).matches())
                {
                    if (!Strings.isNullOrEmpty(supernetIPAddress) && supernetMask >= 8 && supernetMask <= 23)
                    {
                        String validSupernetAddress = getValidNetworkAddress(supernetIPAddress, supernetMask);

                        if (!Strings.isNullOrEmpty(validSupernetAddress) && validSupernetAddress.equals(supernetIPAddress))
                        {
                            if (isValidSupernet(supernetIPAddress, supernetMask))
                            {
                                TraceOrgSupernetCategory traceOrgSupernetCategory = traceOrgSupernetCategoryRepository.findByCategoryName(supernetIPAddress+"/"+supernetMask);

                                if (traceOrgSupernetCategory == null)
                                {
                                    traceOrgSupernetCategory = new TraceOrgSupernetCategory();

                                    traceOrgSupernetCategory.setCategoryName(supernetIPAddress+"/"+supernetMask);

                                    traceOrgSupernetCategory = traceOrgSupernetCategoryRepository.save(traceOrgSupernetCategory);

                                    TraceOrgEvent traceOrgEvent =  new TraceOrgEvent();

                                    traceOrgEvent.setTimestamp(new Date());

                                    if (traceOrgCommonUtil.currentUser(accessToken) != null)
                                    {
                                        traceOrgEvent.setDoneBy(traceOrgCommonUtil.currentUser(accessToken));
                                    }

                                    traceOrgEvent.setEventType("Add Supernet");

                                    String userName = traceOrgCommonUtil.currentUserName(accessToken) != null ? traceOrgCommonUtil.currentUserName(accessToken) : "";

                                    traceOrgEvent.setEventContext(supernetIPAddress+"/"+supernetMask + " Supernet Category is added in IP Address Manager by "+ userName);

                                    traceOrgEvent.setSeverity(1);

                                    traceOrgEventRepository.save(traceOrgEvent);

                                    List<TraceOrgSubnetDetails> subnetDetailsList = traceOrgSubnetDetailsRepository.findAll();

                                    if (subnetDetailsList != null && !subnetDetailsList.isEmpty())
                                    {
                                        for (TraceOrgSubnetDetails subnetDetails: subnetDetailsList)
                                        {
                                            String subnetAddress = subnetDetails.getSubnetAddress();

                                            int subnetCIDR = subnetDetails.getSubnetCidr();

                                            if (!Strings.isNullOrEmpty(subnetAddress) && subnetCIDR > 0 &&
                                                    isSubnetWithinSupernet(subnetAddress, subnetCIDR, supernetIPAddress, supernetMask))
                                            {
                                                TraceOrgSupernetDetails traceOrgSupernetDetails = new TraceOrgSupernetDetails();

                                                traceOrgSupernetDetails.setSubnetId(TraceOrgCommonUtil.getStringValue(subnetDetails.getId()));

                                                traceOrgSupernetDetails.setTraceOrgSupernetCategory(traceOrgSupernetCategory);

                                                traceOrgSupernetDetailsRepository.save(traceOrgSupernetDetails);

                                                addSupernetEvent(subnetAddress, traceOrgSupernetCategory, traceOrgCommonUtil.currentUser(accessToken), "Add Subnet in Supernet", traceOrgCommonUtil.currentUserName(accessToken));

                                                _logger.debug(subnetAddress + "/" + subnetCIDR + " is added in " + supernetIPAddress+"/"+supernetMask + " category");
                                            }
                                        }

                                    }

                                    result.put(TraceOrgCommonConstants.SUCCESS, TraceOrgCommonConstants.TRUE);

                                    result.put(TraceOrgCommonConstants.MESSAGE, TraceOrgMessageConstants.SUPERNET_ADDED_SUCCESS);
                                }
                                else
                                {
                                    result.put(TraceOrgCommonConstants.SUCCESS, TraceOrgCommonConstants.FALSE);

                                    result.put(TraceOrgCommonConstants.MESSAGE, TraceOrgMessageConstants.SUPERNET_EXIST);
                                }
                            }
                            else
                            {
                                result.put(TraceOrgCommonConstants.SUCCESS, TraceOrgCommonConstants.FALSE);

                                result.put(TraceOrgCommonConstants.MESSAGE, TraceOrgMessageConstants.INVALID_SUPERNET);
                            }
                        }
                        else
                        {
                            result.put(TraceOrgCommonConstants.SUCCESS, TraceOrgCommonConstants.FALSE);

                            result.put(TraceOrgCommonConstants.MESSAGE, TraceOrgMessageConstants.VALID_SUPERNET_WITH_CIDR + validSupernetAddress);
                        }
                    }
                    else
                    {
                        result.put(TraceOrgCommonConstants.SUCCESS, TraceOrgCommonConstants.FALSE);

                        result.put(TraceOrgCommonConstants.MESSAGE, TraceOrgMessageConstants.SUPERNET_MASK_INVALID);
                    }
                }
                else
                {
                    result.put(TraceOrgCommonConstants.SUCCESS, TraceOrgCommonConstants.FALSE);

                    result.put(TraceOrgCommonConstants.MESSAGE, TraceOrgMessageConstants.IPV4_ADDRESS_ONLY);
                }
            }
            else
            {
                result.put(TraceOrgCommonConstants.SUCCESS, TraceOrgCommonConstants.FALSE);

                result.put(TraceOrgCommonConstants.MESSAGE, TraceOrgMessageConstants.TOKEN_NULL);
            }
        }
        catch (Exception exception)
        {
            _logger.error(exception);

             result.put(TraceOrgCommonConstants.SUCCESS, TraceOrgCommonConstants.FALSE);

             result.put(TraceOrgCommonConstants.MESSAGE, TraceOrgMessageConstants.SOMETHING_WENT_WRONG);
        }

        return result;
    }

    /**
     * IPAM-148 : System should have the ability to locate the available subnets inside a Supernet. This is to provide assistance to users when creating subnets inside an aggregated Network.
     * added the getSupernetDetails method to get the supernet details
     * @param accessToken
     */
    @Override
    public HashMap<String,Object> getSupernetDetails(String accessToken)
    {
        HashMap<String,Object> result = new HashMap<>();

        try
        {
            if (accessToken != null)
            {
                List<TraceOrgSupernetCategory> traceOrgSupernetCategoryList = traceOrgSupernetCategoryRepository.findAll();

                if(traceOrgSupernetCategoryList !=null && !traceOrgSupernetCategoryList.isEmpty())
                {
                    List<TraceOrgSubnetDetails> subnetDetailsList = traceOrgSubnetDetailsRepository.findAll();

                    if (subnetDetailsList != null && !subnetDetailsList.isEmpty())
                    {
                        List<Object> subnetByCategory =  new ArrayList<>();

                        List<TraceOrgSupernetDetails> traceOrgSupernetDetailsList = traceOrgSupernetDetailsRepository.findAll();

                        Map<Long, List<TraceOrgSupernetDetails>> supernetDetailsByCategory = traceOrgSupernetDetailsList.stream()
                                .collect(Collectors.groupingBy(t -> t.getTraceOrgSupernetCategory().getId()));

                        Map<Long, TraceOrgSubnetDetails> subnetDetailsById = subnetDetailsList.stream()
                                .collect(Collectors.toMap(TraceOrgSubnetDetails::getId, Function.identity()));

                        for(TraceOrgSupernetCategory traceOrgSupernetCategory : traceOrgSupernetCategoryList)
                        {
                            long categoryId = traceOrgSupernetCategory.getId();

                            List<TraceOrgSubnetDetails> traceOrgSubnetDetailsByCategory = new ArrayList<>();

                            float totalIp = 0;

                            float totalUsedIp = 0;

                            List<TraceOrgSupernetDetails> supernets = supernetDetailsByCategory.getOrDefault(categoryId, Collections.emptyList());

                            for (TraceOrgSupernetDetails traceOrgSupernetDetails : supernets)
                            {
                                long subnetId = Long.parseLong(traceOrgSupernetDetails.getSubnetId());

                                TraceOrgSubnetDetails subnetDetails = subnetDetailsById.get(subnetId);

                                if (subnetDetails != null)
                                {
                                    traceOrgSubnetDetailsByCategory.add(subnetDetails);

                                    totalIp += subnetDetails.getTotalIp();

                                    totalUsedIp += subnetDetails.getUsedIp();
                                }
                            }

                            float totalUsedPercentage = (totalIp > 0) ? (totalUsedIp * 100) / totalIp : 0;

                            Map<String, Object> categoryDetails = new HashMap<>();

                            categoryDetails.put("subnetAddress", traceOrgSupernetCategory.getCategoryName());

                            categoryDetails.put("id", traceOrgSupernetCategory.getId());

                            categoryDetails.put("subnets",traceOrgSubnetDetailsByCategory);

                            categoryDetails.put("totalUsedIpPercentage",totalUsedPercentage);

                            if(totalUsedPercentage < 50)
                            {
                                categoryDetails.put("severity",3);
                            }
                            else if(totalUsedPercentage >= 50 && totalUsedPercentage <80)
                            {
                                categoryDetails.put("severity",2);
                            }
                            else if(totalUsedPercentage >= 80)
                            {
                                categoryDetails.put("severity",1);
                            }

                            subnetByCategory.add(categoryDetails);
                        }

                        result.put(TraceOrgCommonConstants.SUCCESS, TraceOrgCommonConstants.TRUE);

                        result.put(TraceOrgCommonConstants.DATA, subnetByCategory);
                    }
                    else
                    {
                        List<Object> subnetByCategory =  new ArrayList<>();

                        for(TraceOrgSupernetCategory traceOrgSupernetCategory : traceOrgSupernetCategoryList)
                        {
                            float totalUsedPercentage = 0 ;

                            HashMap<String,Object> categoryDetails = new HashMap<>();

                            List<TraceOrgSubnetDetails> traceOrgSubnetDetailsByCategory = new ArrayList<>();

                            categoryDetails.put("subnetAddress", traceOrgSupernetCategory.getCategoryName());

                            categoryDetails.put("id", traceOrgSupernetCategory.getId());

                            categoryDetails.put("subnets",traceOrgSubnetDetailsByCategory);

                            categoryDetails.put("totalUsedIpPercentage",totalUsedPercentage);

                            categoryDetails.put("severity",3);

                            subnetByCategory.add(categoryDetails);
                        }

                        result.put(TraceOrgCommonConstants.SUCCESS, TraceOrgCommonConstants.TRUE);

                        result.put(TraceOrgCommonConstants.DATA, subnetByCategory);
                    }
                }
                else
                {
                    result.put(TraceOrgCommonConstants.SUCCESS, TraceOrgCommonConstants.FALSE);
                }
            }
            else
            {
                result.put(TraceOrgCommonConstants.SUCCESS, TraceOrgCommonConstants.FALSE);

                result.put(TraceOrgCommonConstants.MESSAGE, TraceOrgMessageConstants.TOKEN_NULL);
            }

        }
        catch (Exception exception)
        {
            _logger.error(exception);

            result.put(TraceOrgCommonConstants.SUCCESS, TraceOrgCommonConstants.FALSE);

            result.put(TraceOrgCommonConstants.MESSAGE, TraceOrgMessageConstants.SOMETHING_WENT_WRONG);
        }

        return result;
    }

    /**
     * IPAM-148 : System should have the ability to locate the available subnets inside a Supernet. This is to provide assistance to users when creating subnets inside an aggregated Network.
     * added the removeSupernet method to remove the supernet
     * @param accessToken
     * @param id
     */
    @Override
    public HashMap<String,Object> removeSupernet(String accessToken, Long id)
    {
        HashMap<String,Object> result = new HashMap<>();

        try
        {
            if (accessToken != null)
            {
                if (id != null)
                {
                    boolean isSupernetCategoryExist = traceOrgSupernetCategoryRepository.exists(id);

                    if (isSupernetCategoryExist)
                    {
                        TraceOrgSupernetCategory traceOrgSupernetCategory = traceOrgSupernetCategoryRepository.findOne(id);

                        traceOrgSupernetCategoryRepository.delete(id);

                        if (traceOrgSupernetCategory != null && traceOrgSupernetCategory.getCategoryName() != null)
                        {
                            TraceOrgEvent traceOrgEvent =  new TraceOrgEvent();

                            traceOrgEvent.setTimestamp(new Date());

                            if (traceOrgCommonUtil.currentUser(accessToken) != null)
                            {
                                traceOrgEvent.setDoneBy(traceOrgCommonUtil.currentUser(accessToken));
                            }

                            String userName = traceOrgCommonUtil.currentUserName(accessToken) != null ? traceOrgCommonUtil.currentUserName(accessToken) : "";

                            traceOrgEvent.setEventType("Delete Supernet");

                            traceOrgEvent.setEventContext("Supernet "+ traceOrgSupernetCategory.getCategoryName()+" is deleted from IP Address Manager by "+userName);

                            traceOrgEvent.setSeverity(1);

                            traceOrgEventRepository.save(traceOrgEvent);
                        }

                        result.put(TraceOrgCommonConstants.SUCCESS, TraceOrgCommonConstants.TRUE);

                        result.put(TraceOrgCommonConstants.MESSAGE, TraceOrgMessageConstants.SUPERNET_DELETE_SUCCESS);
                    }
                    else
                    {
                        result.put(TraceOrgCommonConstants.SUCCESS, TraceOrgCommonConstants.FALSE);

                        result.put(TraceOrgCommonConstants.MESSAGE, TraceOrgMessageConstants.SUPERNET_NOT_EXIST);
                    }
                }
                else
                {
                    result.put(TraceOrgCommonConstants.SUCCESS, TraceOrgCommonConstants.FALSE);

                    result.put(TraceOrgCommonConstants.MESSAGE, TraceOrgMessageConstants.ENTER_VALID_DETAILS);
                }
            }
            else
            {
                result.put(TraceOrgCommonConstants.SUCCESS, TraceOrgCommonConstants.FALSE);

                result.put(TraceOrgCommonConstants.MESSAGE, TraceOrgMessageConstants.TOKEN_NULL);
            }
        }
        catch (Exception exception)
        {
            result.put(TraceOrgCommonConstants.SUCCESS, TraceOrgCommonConstants.FALSE);

            result.put(TraceOrgCommonConstants.MESSAGE, TraceOrgMessageConstants.SOMETHING_WENT_WRONG);
        }

        return result;

    }

    /**
     * IPAM-148 : System should have the ability to locate the available subnets inside a Supernet. This is to provide assistance to users when creating subnets inside an aggregated Network.
     * added the insertSubnetInSupernetCategory method to insert the subnet into supernet category
     * @param subnetAddress
     * @param subnetCidr
     * @param subnetId
     * @param doneBy
     * @param eventBy
     */
    @Override
    public void insertSubnetInSupernetCategory(String subnetAddress, Integer subnetCidr, Long subnetId, TraceOrgUser doneBy, String eventBy)
    {
        try
        {
            if (subnetAddress != null)
            {
                List<TraceOrgSupernetCategory> traceOrgSupernetCategoryList = (List<TraceOrgSupernetCategory>) traceOrgSupernetCategoryRepository.findAll();

                if (traceOrgSupernetCategoryList != null)
                {
                    for (TraceOrgSupernetCategory traceOrgSupernetCategory : traceOrgSupernetCategoryList)
                    {
                        if (traceOrgSupernetCategory != null)
                        {
                            String[] supernetCategory = traceOrgSupernetCategory.getCategoryName().split("/");

                            TraceOrgSupernetDetails existingSupernetDetails = traceOrgSupernetDetailsRepository.findBySubnetIdAndTraceOrgSupernetCategory(subnetId.toString(), traceOrgSupernetCategory);

                            if (existingSupernetDetails == null && isSubnetWithinSupernet(subnetAddress,subnetCidr,supernetCategory[0],Integer.parseInt(supernetCategory[1])))
                            {
                                TraceOrgSupernetDetails traceOrgSupernetDetails = new TraceOrgSupernetDetails();

                                traceOrgSupernetDetails.setSubnetId(TraceOrgCommonUtil.getStringValue(subnetId));

                                traceOrgSupernetDetails.setTraceOrgSupernetCategory(traceOrgSupernetCategory);

                                traceOrgSupernetDetailsRepository.save(traceOrgSupernetDetails);

                                addSupernetEvent(subnetAddress, traceOrgSupernetCategory, doneBy, "Add Subnet in Supernet",eventBy);
                                
                                _logger.debug(subnetAddress + "/" + subnetCidr + " is added in " + supernetCategory[0]+"/"+supernetCategory[1] + " category");
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

    /**
     * IPAM-148 : System should have the ability to locate the available subnets inside a Supernet. This is to provide assistance to users when creating subnets inside an aggregated Network.
     * added the method to add the Event
     * @param subnetAddress
     * @param traceOrgSupernetCategory
     * @param doneBy
     * @param eventType
     * @param eventBy
     */
    public void addSupernetEvent(String subnetAddress, TraceOrgSupernetCategory traceOrgSupernetCategory, TraceOrgUser doneBy, String eventType, String eventBy)
    {
        try
        {
            TraceOrgEvent traceOrgEvent =  new TraceOrgEvent();

            traceOrgEvent.setTimestamp(new Date());

            if (doneBy != null)
            {
                traceOrgEvent.setDoneBy(doneBy);
            }

            traceOrgEvent.setEventType(eventType);

            traceOrgEvent.setEventContext("Subnet "+subnetAddress+" is added in " + traceOrgSupernetCategory.getCategoryName() + " Supernet category in IP Address Manager by "+ eventBy);

            traceOrgEvent.setSeverity(1);

            traceOrgEventRepository.save(traceOrgEvent);
        }
        catch (Exception exception)
        {
            _logger.error(exception);
        }
    }

    /**
     * IPAM-148 : System should have the ability to locate the available subnets inside a Supernet. This is to provide assistance to users when creating subnets inside an aggregated Network.
     * added the method to remove the subnet from supernet details
     * @param subnetId
     */
    @Override
    public void removeSubnetFromSupernetDetails(Long subnetId)
    {
        try
        {
            TraceOrgSupernetDetails traceOrgSupernetDetails = traceOrgSupernetDetailsRepository.findBySubnetId(TraceOrgCommonUtil.getStringValue(subnetId));

            if (traceOrgSupernetDetails != null && traceOrgSupernetDetails.getId() != null)
            {
                traceOrgSupernetDetailsRepository.delete(traceOrgSupernetDetails.getId());
            }
        }
        catch (Exception exception)
        {
            _logger.error(exception);
        }
    }

    /**
     * IPAM-148 : System should have the ability to locate the available subnets inside a Supernet. This is to provide assistance to users when creating subnets inside an aggregated Network.
     * added the method to validate the subnet ip with cidr is valid with supernet ip and cidr
     * @param subnetIp
     * @param subnetMask
     * @param supernetIp
     * @param supernetMask
     * @return
     */
    private static boolean isSubnetWithinSupernet(String subnetIp, int subnetMask, String supernetIp, int supernetMask)
    {
        try
        {
            int subnetInt = TraceOrgCommonUtil.convertIpAddressToInterger(subnetIp);

            int supernetInt = TraceOrgCommonUtil.convertIpAddressToInterger(supernetIp);

            int subnetNetwork = subnetInt & getSubnetMask(subnetMask);

            int supernetNetwork = supernetInt & getSubnetMask(supernetMask);

            return supernetNetwork == (subnetNetwork & getSubnetMask(supernetMask));
        }
        catch (Exception exception)
        {
            _logger.error(exception);
        }

        return false;
    }

    /**
     * IPAM-148 : System should have the ability to locate the available subnets inside a Supernet. This is to provide assistance to users when creating subnets inside an aggregated Network.
     * added the method to get valid network address as per ip and cidr
     * 0xFFFFFFFF is 32 bits of all ones
     * @param ip
     * @param prefix
     * @return
     */
    public static String getValidNetworkAddress(String ip, int prefix)
    {
        String ipAddress = "";

        try
        {
            String[] parts = ip.split("\\.");

            int ipInt = 0;

            // Convert the IP string into a 32-bit integer representation
            for (int index = 0; index < 4; index++)
            {
                ipInt = (ipInt << 8) | Integer.parseInt(parts[index]);
            }

            // Create the subnet mask as a 32-bit integer by shifting left
            int mask = (int) (TraceOrgCommonConstants.MASK_32BIT << (32 - prefix));

            // Apply the subnet mask using bitwise AND to get the network address
            int network = ipInt & mask;

            // Convert the network integer back to dotted-decimal format
            ipAddress =  String.format("%d.%d.%d.%d", (network >> 24) & TraceOrgCommonConstants.MASK_8BIT, (network >> 16) & TraceOrgCommonConstants.MASK_8BIT, (network >> 8) & TraceOrgCommonConstants.MASK_8BIT, network & TraceOrgCommonConstants.MASK_8BIT);
        }
        catch (Exception exception)
        {
            _logger.error(exception);
        }

        return ipAddress;
    }

    /**
     * IPAM-148 : System should have the ability to locate the available subnets inside a Supernet. This is to provide assistance to users when creating subnets inside an aggregated Network.
     * added the method to calculates a subnet mask in integer format based on the given CIDR prefix length
     * 0xFFFFFFFFL represents a 32-bit binary number with all bits set to 1
     * @param mask
     * @return
     */
    private static int getSubnetMask(int mask)
    {
        return (int) (TraceOrgCommonConstants.MASK_32BIT_LONG << (32 - mask));
    }

    /**
     * IPAM-148 : System should have the ability to locate the available subnets inside a Supernet. This is to provide assistance to users when creating subnets inside an aggregated Network.
     * added the method to validate the supernet ip address with its cidr
     * @param supernetIPAddress
     * @param supernetMask
     * @return
     */
    private static boolean isValidSupernet(String supernetIPAddress, int supernetMask)
    {
        boolean isValidSupernet = false;

        try
        {
            int supernetInt = TraceOrgCommonUtil.convertIpAddressToInterger(supernetIPAddress);

            int supernetNetwork = supernetInt & getSubnetMask(supernetMask);

            isValidSupernet = supernetNetwork == supernetInt;
        }
        catch (Exception exception)
        {
            _logger.error(exception);
        }

        return isValidSupernet;
    }
}
