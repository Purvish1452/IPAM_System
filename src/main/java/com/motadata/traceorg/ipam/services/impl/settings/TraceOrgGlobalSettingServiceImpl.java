package com.motadata.traceorg.ipam.services.impl.settings;

import com.motadata.traceorg.ipam.controller.settings.TraceOrgGlobalSettingController;
import com.motadata.traceorg.ipam.entity.settings.TraceOrgGlobalSetting;
import com.motadata.traceorg.ipam.logger.TraceOrgLogger;
import com.motadata.traceorg.ipam.repository.settings.TraceOrgGlobalSettingsRepository;
import com.motadata.traceorg.ipam.services.settings.TraceOrgGlobalSettingService;
import com.motadata.traceorg.ipam.util.TraceOrgCommonConstants;
import com.motadata.traceorg.ipam.util.TraceOrgCommonUtil;
import com.motadata.traceorg.ipam.util.TraceOrgMessageConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;

@Service
public class TraceOrgGlobalSettingServiceImpl implements TraceOrgGlobalSettingService
{
    private static final TraceOrgLogger _logger = new TraceOrgLogger(TraceOrgGlobalSettingController.class, "Global Setting Service");

    @Autowired
    private TraceOrgGlobalSettingsRepository traceOrgGlobalSettingsRepository;

    @Override
    public HashMap<String, Object> listAllGlobalSetting()
    {
        HashMap<String, Object> result = new HashMap<>();

        try
        {
            List<TraceOrgGlobalSetting> traceOrgGlobalSettings = traceOrgGlobalSettingsRepository.findAll();

            result.put(TraceOrgCommonConstants.SUCCESS, TraceOrgCommonConstants.TRUE);

            result.put(TraceOrgCommonConstants.DATA, traceOrgGlobalSettings);
        }
        catch (Exception exception)
        {
            _logger.error(exception);
        }

        return result;
    }

    @Override
    public HashMap<String, Object> updateGlobalSetting(Long id, TraceOrgGlobalSetting traceOrgGlobalSetting)
    {
        HashMap<String, Object> result = new HashMap<>();

        try
        {
            if(id != null && traceOrgGlobalSetting.getLoggingLevel() != null)
            {
                traceOrgGlobalSetting.setId(1L);

                traceOrgGlobalSettingsRepository.save(traceOrgGlobalSetting);

                TraceOrgCommonUtil.setLogLevel(traceOrgGlobalSetting.getLoggingLevel());

                _logger.debug("Logging level " + traceOrgGlobalSetting.getLoggingLevel() + " is applied successfully..");

                result.put(TraceOrgCommonConstants.SUCCESS, TraceOrgCommonConstants.TRUE);

                result.put(TraceOrgCommonConstants.MESSAGE, TraceOrgMessageConstants.GLOBAL_SETTING_UPDATE_SUCCESS);
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
}
