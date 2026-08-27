package com.motadata.traceorg.ipam.services.impl.discovery;


import com.google.common.base.Strings;
import com.motadata.traceorg.ipam.entity.discovery.TraceOrgGateway;
import com.motadata.traceorg.ipam.entity.subnet.TraceOrgSubnetDetails;
import com.motadata.traceorg.ipam.logger.TraceOrgLogger;
import com.motadata.traceorg.ipam.repository.discovery.TraceOrgGatewayRepository;
import com.motadata.traceorg.ipam.repository.subnet.TraceOrgSubnetDetailsRepository;
import com.motadata.traceorg.ipam.services.discovery.TraceOrgGatewayService;
import com.motadata.traceorg.ipam.util.TraceOrgCommonConstants;
import com.motadata.traceorg.ipam.util.TraceOrgCommonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
public class TraceOrgGatewayServiceImpl implements TraceOrgGatewayService
{

    private static final TraceOrgLogger _logger = new TraceOrgLogger(TraceOrgGatewayServiceImpl.class, "Gateway Service");

    @Autowired
    TraceOrgGatewayRepository traceOrgGatewayRepository;

    @Autowired
    TraceOrgCommonUtil traceOrgCommonUtil;

    @Autowired
    TraceOrgSubnetDetailsRepository traceOrgSubnetDetailsRepository;

    /**
     * IPAM-146 System should have automatic discovery of IPv4 & IPv6 without manual configuration
     * Refactor code by moving the method to service layer
     * */
    @Override
    public HashMap<String, Object> listGateway()
    {
        HashMap<String, Object> result = new HashMap<>();

        try
        {
            List<TraceOrgGateway> traceOrgGateways = traceOrgGatewayRepository.findAll();

            if(traceOrgGateways == null) traceOrgGateways = new ArrayList<>();

            TraceOrgGateway gateway = new TraceOrgGateway();

            gateway.setGateway("None");

            gateway.setId(-1L);

            traceOrgGateways.add(gateway);

            result.put(TraceOrgCommonConstants.DATA, traceOrgGateways);

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
     * Refactor code by moving the method to service layer
     * */
    @Override
    public HashMap<String, Object> getGateways()
    {
        HashMap<String, Object> result = new HashMap<>();

        try
        {
            List<TraceOrgGateway> traceOrgGateways = traceOrgGatewayRepository.findAll();

            if(traceOrgGateways != null)
            {
                for (TraceOrgGateway traceOrgGateway : traceOrgGateways)
                {
                    if(Strings.isNullOrEmpty(traceOrgGateway.getName()))
                    {
                        traceOrgGateway.setName(traceOrgGateway.getGateway());
                    }
                }
            }

            result.put(TraceOrgCommonConstants.DATA, traceOrgGateways);

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
     * Refactor code by moving the method to service layer
     * */
    @Override
    public HashMap<String, Object> getGateway(Long gatewayId)
    {
        HashMap<String, Object> result = new HashMap<>();

        try
        {
            TraceOrgGateway existedGateway = traceOrgGatewayRepository.findOne(gatewayId);

            if(existedGateway != null)
            {
                existedGateway.setCommunity(TraceOrgCommonConstants.EMPTY_STRING);

                existedGateway.setPrivatePassword(TraceOrgCommonConstants.EMPTY_STRING);

                existedGateway.setAuthenticationPassword(TraceOrgCommonConstants.EMPTY_STRING);

                result.put(TraceOrgCommonConstants.DATA, existedGateway);

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
     * Refactor code by moving the method to service layer
     * */
    @Override
    public HashMap<String, Object> addGateway(TraceOrgGateway traceOrgGateway)
    {
        HashMap<String, Object> result = new HashMap<>();

        try
        {
            if(traceOrgGateway != null && !Strings.isNullOrEmpty(traceOrgGateway.getGateway()))
            {
                if(traceOrgCommonUtil.checkGatewayIp(traceOrgGateway.getGateway()))
                {
                    if (!traceOrgGatewayRepository.existsByGateway(traceOrgGateway.getGateway()))
                    {
                        traceOrgGatewayRepository.save(traceOrgGateway);

                        result.put(TraceOrgCommonConstants.MESSAGE, "Gateway added successfully!");

                        result.put(TraceOrgCommonConstants.SUCCESS, TraceOrgCommonConstants.TRUE);
                    }
                    else
                    {
                        result.put(TraceOrgCommonConstants.MESSAGE, "Gateway is already exist!");

                        result.put(TraceOrgCommonConstants.SUCCESS, TraceOrgCommonConstants.FALSE);
                    }
                }
                else
                {
                    result.put(TraceOrgCommonConstants.MESSAGE, "Enter Valid Gateway!");

                    result.put(TraceOrgCommonConstants.SUCCESS, TraceOrgCommonConstants.FALSE);
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
     * IPAM-146 System should have automatic discovery of IPv4 & IPv6 without manual configuration
     * Refactor code by moving the method to service layer
     * */
    @Override
    public HashMap<String, Object> updateGateway(Long gatewayId, TraceOrgGateway traceOrgGateway)
    {
        HashMap<String, Object> result = new HashMap<>();

        try
        {
            if(gatewayId != null)
            {
                if(traceOrgCommonUtil.checkGatewayIp(traceOrgGateway.getGateway()))
                {
                    List<TraceOrgGateway> traceOrgGateways =  traceOrgGatewayRepository.findByIdNotAndGateway(gatewayId, traceOrgGateway.getGateway());

                    if(traceOrgGateways != null && !traceOrgGateways.isEmpty())
                    {
                        result.put(TraceOrgCommonConstants.MESSAGE, "Gateway is already exist!");

                        result.put(TraceOrgCommonConstants.SUCCESS, TraceOrgCommonConstants.FALSE);
                    }
                    else
                    {
                        TraceOrgGateway existedGateway = traceOrgGatewayRepository.findOne(gatewayId);

                        if(existedGateway != null)
                        {
                            existedGateway.setGateway(traceOrgGateway.getGateway());

                            existedGateway.setVersion(traceOrgGateway.getVersion());

                            existedGateway.setCommunity(traceOrgGateway.getCommunity());

                            existedGateway.setSecurityLevel(traceOrgGateway.getSecurityLevel());

                            existedGateway.setAuthenticationProtocol(traceOrgGateway.getAuthenticationProtocol());

                            existedGateway.setPrivacyProtocol(traceOrgGateway.getPrivacyProtocol());

                            existedGateway.setSecurityUserName(traceOrgGateway.getSecurityUserName());

                            existedGateway.setAuthenticationPassword(traceOrgGateway.getAuthenticationPassword());

                            existedGateway.setPrivatePassword(traceOrgGateway.getPrivatePassword());

                            existedGateway.setName(traceOrgGateway.getName());

                            traceOrgGatewayRepository.save(existedGateway);

                            result.put(TraceOrgCommonConstants.MESSAGE, "Gateway Updated Successfully!");

                            result.put(TraceOrgCommonConstants.SUCCESS, TraceOrgCommonConstants.TRUE);
                        }
                    }
                }
                else
                {
                    result.put(TraceOrgCommonConstants.MESSAGE, "Enter Valid Gateway!");

                    result.put(TraceOrgCommonConstants.SUCCESS, TraceOrgCommonConstants.FALSE);
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
     * IPAM-146 System should have automatic discovery of IPv4 & IPv6 without manual configuration
     * Refactor code by moving the method to service layer
     * IPAM-175 IPAM | Deletion Failure for Gateway with Incorrect Validation Message
     * Getting gateway from subnet table instead of gateway table
     * */
    @Override
    public HashMap<String, Object> removeGateway(Long gatewayId)
    {
        HashMap<String, Object> result = new HashMap<>();

        try
        {
            if(gatewayId !=null)
            {
                List<TraceOrgSubnetDetails> traceOrgSubnetDetails = traceOrgSubnetDetailsRepository.findByGatewayId(gatewayId);

                if(traceOrgSubnetDetails != null && !traceOrgSubnetDetails.isEmpty())
                {
                    result.put(TraceOrgCommonConstants.MESSAGE, "Gateway is in use and cannot be deleted !");

                    result.put(TraceOrgCommonConstants.SUCCESS, TraceOrgCommonConstants.FALSE);
                }
                else
                {
                    traceOrgGatewayRepository.delete(gatewayId);

                    result.put(TraceOrgCommonConstants.MESSAGE, "Gateway deleted Successfully!");

                    result.put(TraceOrgCommonConstants.SUCCESS, TraceOrgCommonConstants.TRUE);
                }
            }
        }
        catch (Exception exception)
        {
            _logger.error(exception);
        }

        return result;
    }
}
