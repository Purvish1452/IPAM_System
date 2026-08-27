package com.motadata.traceorg.ipam.services.impl.settings;

import com.google.common.base.Strings;
import com.motadata.traceorg.ipam.entity.settings.TraceOrgAlertConfigure;
import com.motadata.traceorg.ipam.logger.TraceOrgLogger;
import com.motadata.traceorg.ipam.repository.settings.TraceOrgAlertConfigureRepository;
import com.motadata.traceorg.ipam.services.settings.TraceOrgAlertConfigureService;
import com.motadata.traceorg.ipam.util.TraceOrgCommonConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;

@Service
public class TraceOrgAlertConfigureServiceImpl implements TraceOrgAlertConfigureService
{

    private static final TraceOrgLogger _logger = new TraceOrgLogger(TraceOrgAlertConfigureServiceImpl.class, "Alert Configuration Service");

    @Autowired
    private TraceOrgAlertConfigureRepository traceOrgAlertConfigureRepository;

    @Override
    @Transactional
    @CacheEvict(value = "traceOrgAlertConfigures", allEntries = true)
    public HashMap<String, Object> updateAlertConfiguration(List<TraceOrgAlertConfigure> traceOrgAlertConfigures)
    {
        HashMap<String, Object> result = new HashMap<>();

        try
        {
            if(traceOrgAlertConfigures != null && !traceOrgAlertConfigures.isEmpty())
            {
                traceOrgAlertConfigureRepository.save(traceOrgAlertConfigures);

                result.put(TraceOrgCommonConstants.MESSAGE, "Alert Configuration Updated Successfully!");

                result.put(TraceOrgCommonConstants.SUCCESS, TraceOrgCommonConstants.TRUE);
            }
        }
        catch (Exception exception)
        {
            _logger.error(exception);
        }

        return result;
    }

    @Override
    public HashMap<String, Object> getAlertConfiguration()
    {
        HashMap<String, Object> result = new HashMap<>();

        try
        {
            result.put(TraceOrgCommonConstants.DATA, getAlertsMap());

            result.put(TraceOrgCommonConstants.SUCCESS, TraceOrgCommonConstants.TRUE);
        }
        catch (Exception exception)
        {
            _logger.error(exception);
        }

        return result;
    }

    public HashMap<String, String> getAlertsMap()
    {
        HashMap<String, String> result = new HashMap<>();

        try
        {
            List<TraceOrgAlertConfigure> alerts = traceOrgAlertConfigureRepository.findAll();

            if (alerts != null && !alerts.isEmpty())
            {
                for (TraceOrgAlertConfigure alert : alerts)
                {
                    result.put(alert.getAlertKey(), String.valueOf(alert.getAlertValue()));
                }
            }
        }
        catch (Exception exception)
        {
            _logger.error(exception);
        }

        return result;
    }

    @Cacheable(value = "traceOrgAlertConfigures", key = "#alertKey", condition = "#alertKey != null")
    public TraceOrgAlertConfigure getByAlertKey(String alertKey)
    {
        return traceOrgAlertConfigureRepository.findByAlertKey(alertKey);
    }

    @Override
    public String getAlertValue(String alertKey)
    {
        String alertValue = null;

        try
        {
            TraceOrgAlertConfigure traceOrgAlertConfigure = getByAlertKey(alertKey);

            if(traceOrgAlertConfigure != null && !Strings.isNullOrEmpty(traceOrgAlertConfigure.getAlertValue()))
            {
                alertValue = traceOrgAlertConfigure.getAlertValue();
            }
        }
        catch (Exception exception)
        {
            _logger.error(exception);
        }

        return alertValue;
    }
}
