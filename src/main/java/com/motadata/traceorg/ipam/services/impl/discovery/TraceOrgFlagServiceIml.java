package com.motadata.traceorg.ipam.services.impl.discovery;


import com.motadata.traceorg.ipam.entity.discovery.TraceOrgFlags;
import com.motadata.traceorg.ipam.logger.TraceOrgLogger;
import com.motadata.traceorg.ipam.repository.discovery.TraceOrgFlagRepository;
import com.motadata.traceorg.ipam.services.discovery.TraceOrgFlagService;
import com.motadata.traceorg.ipam.util.TraceOrgCommonConstants;
import org.springframework.stereotype.Service;

@Service
public class TraceOrgFlagServiceIml implements TraceOrgFlagService
{

    private static final TraceOrgLogger _logger = new TraceOrgLogger(TraceOrgFlagServiceIml.class,"Flag Service");

    private final TraceOrgFlagRepository traceOrgFlagRepository;

    public TraceOrgFlagServiceIml(TraceOrgFlagRepository traceOrgFlagRepository)
    {
        this.traceOrgFlagRepository = traceOrgFlagRepository;
    }

    /**
     * IPAM-146 System should have automatic discovery of IPv4 & IPv6 without manual configuration
     * Added method to check if the subnet is auto discovered
     * */
    public boolean isAutoDiscovered()
    {
        TraceOrgFlags flag = null;

        try
        {
            flag = traceOrgFlagRepository.findByFlag(TraceOrgCommonConstants.IS_AUTO_DISCOVERED);
        }
        catch (Exception exception)
        {
            _logger.error(exception);
        }

        return flag != null && flag.isValue();
    }
}
