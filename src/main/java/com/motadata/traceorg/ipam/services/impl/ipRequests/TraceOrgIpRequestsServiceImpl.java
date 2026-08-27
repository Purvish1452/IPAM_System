package com.motadata.traceorg.ipam.services.impl.ipRequests;

import com.motadata.traceorg.ipam.dto.ipRequests.TraceOrgApproveIpRequestDTO;
import com.motadata.traceorg.ipam.dto.ipRequests.TraceOrgRejectIpRequestDTO;
import com.motadata.traceorg.ipam.entity.ipRequests.TraceOrgIpRequests;
import com.motadata.traceorg.ipam.entity.subnet.TraceOrgIPChangeLog;
import com.motadata.traceorg.ipam.entity.subnet.TraceOrgSubnetDetails;
import com.motadata.traceorg.ipam.entity.subnet.TraceOrgSubnetIpDetails;
import com.motadata.traceorg.ipam.enumeration.TraceOrgIpRequestsStatus;
import com.motadata.traceorg.ipam.logger.TraceOrgLogger;
import com.motadata.traceorg.ipam.repository.ipRequests.TraceOrgIpRequestsRepository;
import com.motadata.traceorg.ipam.repository.subnet.TraceOrgIpChangeLogRepository;
import com.motadata.traceorg.ipam.repository.subnet.TraceOrgSubnetDetailsRepository;
import com.motadata.traceorg.ipam.repository.subnet.TraceOrgSubnetIpDetailsRepository;
import com.motadata.traceorg.ipam.services.TraceOrgService;
import com.motadata.traceorg.ipam.services.ipRequests.TraceOrgIpRequestsService;
import com.motadata.traceorg.ipam.util.TraceOrgCommonConstants;
import com.motadata.traceorg.ipam.util.TraceOrgMessageConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;


import java.util.*;


/**
 * IPAM-159 IPAM Roadmap : Streamline IP address request creation and management with the IP Request tool.
 * Implementation of ip request management.
 */
@Service
public class TraceOrgIpRequestsServiceImpl implements TraceOrgIpRequestsService {

    private static final TraceOrgLogger _logger = new TraceOrgLogger(TraceOrgIpRequestsServiceImpl.class, "Ip Request Service");

    @Autowired
    TraceOrgIpRequestsRepository traceOrgIpRequestsRepository;

    @Autowired
    TraceOrgSubnetDetailsRepository traceOrgSubnetDetailsRepository;

    @Autowired
    TraceOrgSubnetIpDetailsRepository traceOrgSubnetIpRepository;

    @Autowired
    private TraceOrgService traceOrgService;

    @Autowired
    private TraceOrgSubnetIpDetailsRepository traceOrgSubnetIpDetailsRepository;

    @Autowired
    private TraceOrgIpChangeLogRepository traceOrgIpChangeLogRepository;

    @Override
    public HashMap<String, Object> addIpRequests(TraceOrgIpRequests traceOrgIpRequets) {

        HashMap<String, Object> result = new HashMap<>();

        traceOrgIpRequets.setStatus(TraceOrgIpRequestsStatus.PENDING);

        if(traceOrgIpRequets.getNumberOfIps()!=traceOrgIpRequets.getIps().size() && traceOrgIpRequets.getPreferredSubnet())
        {
            result.put(TraceOrgCommonConstants.SUCCESS, TraceOrgCommonConstants.FALSE);

            result.put(TraceOrgCommonConstants.MESSAGE, TraceOrgMessageConstants.SELECT_EXACT_NO_OF_IPS);
        }
        else
        {
            if(!traceOrgIpRequets.getPreferredSubnet())
            {
                traceOrgIpRequestsRepository.save(traceOrgIpRequets);

                result.put(TraceOrgCommonConstants.SUCCESS, TraceOrgCommonConstants.TRUE);

                result.put(TraceOrgCommonConstants.MESSAGE, TraceOrgMessageConstants.IP_REQUEST_ADDED);
            }
            else
            {
                if(traceOrgIpRequets.getIps().isEmpty() || traceOrgIpRequets.getSubnetId()==null)
                {
                    result.put(TraceOrgCommonConstants.SUCCESS, TraceOrgCommonConstants.FALSE);

                    result.put(TraceOrgCommonConstants.MESSAGE, TraceOrgMessageConstants.ALLOCATE_IP);
                }
                else
                {
                    traceOrgIpRequestsRepository.save(traceOrgIpRequets);

                    result.put(TraceOrgCommonConstants.SUCCESS, TraceOrgCommonConstants.TRUE);

                    result.put(TraceOrgCommonConstants.MESSAGE, TraceOrgMessageConstants.IP_REQUEST_ADDED);
                }
            }
        }

        return result;
    }

    @Override
    public HashMap<String, Object> listAllIpRequests() {
        HashMap<String, Object> result = new HashMap<>();

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        boolean isAdmin = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_ADMIN"::equals);

        List<TraceOrgIpRequests> ipRequetsList;
        try
        {
            if(isAdmin)
            {
                ipRequetsList = traceOrgIpRequestsRepository.findAll();
            }
            else
            {
                ipRequetsList = traceOrgIpRequestsRepository.findByCreatedBy(authentication.getName());
            }

            if(ipRequetsList != null)
            {
                ipRequetsList.sort(Comparator.comparing(TraceOrgIpRequests::getCreatedDate).reversed());

                result.put(TraceOrgCommonConstants.SUCCESS, TraceOrgCommonConstants.TRUE);

                result.put(TraceOrgCommonConstants.DATA, ipRequetsList);
            }
        }
        catch (Exception exception)
        {
            _logger.error(exception);
        }

        return result;
    }


    @Override
    public HashMap<String, Object> getIpRequest(Long id) {
        HashMap<String, Object> result = new HashMap<>();

        try
        {
            if(id != null )
            {
                TraceOrgIpRequests ipRequets = traceOrgIpRequestsRepository.findOne(id);

                if(ipRequets.getSubnetId() != null)
                {
                    TraceOrgSubnetDetails traceOrgSubnetDetails=traceOrgSubnetDetailsRepository.findOne(Long.valueOf(ipRequets.getSubnetId()));

                    ipRequets.setSubnetId(traceOrgSubnetDetails.getSubnetAddress());//+"/"+traceOrgSubnetDetails.getSubnetCidr()
                }

                if (ipRequets != null)
                {
                    result.put(TraceOrgCommonConstants.SUCCESS, TraceOrgCommonConstants.TRUE);

                    result.put(TraceOrgCommonConstants.DATA, ipRequets);
                }
                else
                {
                    result.put(TraceOrgCommonConstants.SUCCESS, TraceOrgCommonConstants.FALSE);

                    result.put(TraceOrgCommonConstants.MESSAGE, TraceOrgMessageConstants.ROLE_ID_WRONG);
                }
            }
            else
            {
                result.put(TraceOrgCommonConstants.SUCCESS, TraceOrgCommonConstants.FALSE);

                result.put(TraceOrgCommonConstants.MESSAGE, TraceOrgMessageConstants.ENTER_VALID_DETAILS);
            }
        }
        catch (Exception exception)
        {
            _logger.error(exception);
        }

        return result;
    }

    @Override
    public HashMap<String, Object> ipRequestApproved(TraceOrgApproveIpRequestDTO traceOrgApproveIpRequestDTO) {
        HashMap<String, Object> result = new HashMap<>();

        try
        {
            TraceOrgIpRequests ipRequests = traceOrgIpRequestsRepository.findOne(traceOrgApproveIpRequestDTO.getId());

            ipRequests.setRemark(traceOrgApproveIpRequestDTO.getRemark());

            if(ipRequests.getNumberOfIps()!=traceOrgApproveIpRequestDTO.getIps().size() && !ipRequests.getPreferredSubnet())
            {
                result.put(TraceOrgCommonConstants.SUCCESS, TraceOrgCommonConstants.FALSE);

                result.put(TraceOrgCommonConstants.MESSAGE, TraceOrgMessageConstants.SELECT_EXACT_NO_OF_IPS);
            }
            else
            {
                if (ipRequests.getPreferredSubnet().equals(true))
                {
                    if (ipRequests != null)
                    {
                        if (verifyAvailableIpStatus(ipRequests.getIps()))
                        {
                            ipRequests.setStatus(TraceOrgIpRequestsStatus.APPROVED);

                            traceOrgSubnetIpRepository.updateStatusByIpAddresses(TraceOrgCommonConstants.RESERVED, ipRequests.getIps());

                            traceOrgIpRequestsRepository.save(ipRequests);

                            ipChangeLog(ipRequests.getIps());

                            result.put(TraceOrgCommonConstants.SUCCESS, TraceOrgCommonConstants.TRUE);

                            result.put(TraceOrgCommonConstants.MESSAGE, TraceOrgMessageConstants.IP_REQUEST_APPROVED);
                        } else {
                            result.put(TraceOrgCommonConstants.SUCCESS, TraceOrgCommonConstants.FALSE);

                            result.put(TraceOrgCommonConstants.MESSAGE, TraceOrgMessageConstants.IPS_NOT_AVAILABLE);
                        }
                    } else {
                        result.put(TraceOrgCommonConstants.SUCCESS, TraceOrgCommonConstants.FALSE);

                        result.put(TraceOrgCommonConstants.MESSAGE, TraceOrgMessageConstants.IP_REQUEST_ID_WRONG);
                    }
                }
                else
                {
                    if (traceOrgApproveIpRequestDTO.getIps().isEmpty() || traceOrgApproveIpRequestDTO.getSubnetId() == null)
                    {
                        result.put(TraceOrgCommonConstants.SUCCESS, TraceOrgCommonConstants.FALSE);

                        result.put(TraceOrgCommonConstants.MESSAGE, TraceOrgMessageConstants.ALLOCATE_IP);
                    }
                    else
                    {
                        if (verifyAvailableIpStatus(traceOrgApproveIpRequestDTO.getIps()))
                        {
                            ipRequests.setStatus(TraceOrgIpRequestsStatus.APPROVED);

                            ipRequests.setSubnetId(String.valueOf(traceOrgApproveIpRequestDTO.getSubnetId()));

                            ipRequests.setIps(traceOrgApproveIpRequestDTO.getIps());

                            ipRequests.setPreferredSubnet(true);

                            ipRequests = traceOrgIpRequestsRepository.save(ipRequests);

                            traceOrgSubnetIpRepository.updateStatusByIpAddresses(TraceOrgCommonConstants.RESERVED, ipRequests.getIps());

                            ipChangeLog(traceOrgApproveIpRequestDTO.getIps());

                            result.put(TraceOrgCommonConstants.SUCCESS, TraceOrgCommonConstants.TRUE);

                            result.put(TraceOrgCommonConstants.MESSAGE, TraceOrgMessageConstants.IP_REQUEST_APPROVED);
                        }
                        else
                        {
                            result.put(TraceOrgCommonConstants.SUCCESS, TraceOrgCommonConstants.FALSE);

                            result.put(TraceOrgCommonConstants.MESSAGE, TraceOrgMessageConstants.IPS_NOT_AVAILABLE);
                        }
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

    @Override
    public HashMap<String, Object> ipRequestRejected(TraceOrgRejectIpRequestDTO traceOrgRejectIpRequestDTO) {
        HashMap<String, Object> result = new HashMap<>();

        try
        {
            if(traceOrgRejectIpRequestDTO.getId() != null )
            {
                TraceOrgIpRequests ipRequets = traceOrgIpRequestsRepository.findOne(traceOrgRejectIpRequestDTO.getId());

                ipRequets.setRemark(traceOrgRejectIpRequestDTO.getRemark());

                if (ipRequets != null)
                {
                    ipRequets.setStatus(TraceOrgIpRequestsStatus.REJECTED);

                    traceOrgIpRequestsRepository.save(ipRequets);

                    result.put(TraceOrgCommonConstants.SUCCESS, TraceOrgCommonConstants.TRUE);

                    result.put(TraceOrgCommonConstants.MESSAGE, TraceOrgMessageConstants.IP_REQUEST_REJECTED);
                }
                else
                {
                    result.put(TraceOrgCommonConstants.SUCCESS, TraceOrgCommonConstants.FALSE);

                    result.put(TraceOrgCommonConstants.MESSAGE, TraceOrgMessageConstants.IP_REQUEST_ID_WRONG);
                }
            }
        }
        catch (Exception exception)
        {
            _logger.error(exception);
        }

        return result;
    }

    boolean verifyAvailableIpStatus(List<String> ips)
    {
        return traceOrgSubnetIpRepository.existsAvailableStatus(ips);
    }

    void ipChangeLog(List<String> ipList)
    {
        List<TraceOrgIPChangeLog> ipChangeLogs = new ArrayList<>();

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        List<TraceOrgSubnetIpDetails> ips=traceOrgSubnetIpDetailsRepository.findByIpAddressIn(ipList);

        for(TraceOrgSubnetIpDetails ip:ips)
        {
            TraceOrgIPChangeLog traceOrgIPChangeLog = new TraceOrgIPChangeLog(
                    authentication.getName(),
                    ip.getId(),
                    ip.getSubnetId().getId(),
                    ip.getIpAddress(),
                    new Date(),
                    TraceOrgCommonConstants.CHANGE_LOG_MESSAGE.replace(TraceOrgCommonConstants.PREVIOUS_STATUS,TraceOrgCommonConstants.AVAILABLE).replace(TraceOrgCommonConstants.CURRENT_STATUS, TraceOrgCommonConstants.RESERVED)
            );
            ipChangeLogs.add(traceOrgIPChangeLog);
        }

        traceOrgIpChangeLogRepository.save(ipChangeLogs);
    }
}
