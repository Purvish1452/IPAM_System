package com.motadata.traceorg.ipam.util;

import com.esotericsoftware.yamlbeans.YamlReader;
import com.motadata.traceorg.ipam.logger.TraceOrgLogger;

import java.io.File;
import java.io.FileReader;
import java.util.HashMap;

public class TraceOrgConfigUtil
{
    private static final TraceOrgLogger _logger = new TraceOrgLogger(TraceOrgConfigUtil.class, "Config Util");

    public static HashMap<String, String> loadConfigFile(String configFileName)
    {
        HashMap configDetails = null;

        try
        {
            _logger.debug("loading " + configFileName);

            File configFile = new File(TraceOrgCommonConstants.CURRENT_DIR + TraceOrgCommonConstants.PATH_SEPARATOR + TraceOrgCommonConstants.CONFIG_DIR + TraceOrgCommonConstants.PATH_SEPARATOR + configFileName);

            if (configFile.exists())
            {
                YamlReader reader = new YamlReader(new FileReader(configFile));

                configDetails = reader.read(HashMap.class);

                _logger.debug(configFileName + " loaded:" + configDetails.toString());
            }
            else
            {
                _logger.warn(configFileName + " not found");
            }

        }

        catch (Exception exception)
        {
            _logger.error(exception);
        }

        return configDetails;
    }


    public static String getDatabaseHost()
    {
        String host = "localhost";

        try
        {
            HashMap configDetails = loadConfigFile(TraceOrgCommonConstants.IPM_CONF);

            if ((configDetails != null) && configDetails.get("db-host") != null && (configDetails.size() > 0))
            {
                host = (String) configDetails.get("db-host");
            }
        }
        catch (Exception exception)
        {
            _logger.error(exception);
        }

        return host;
    }


    public static int getDatabasePort()
    {
        int port = 3306;

        try
        {
            HashMap configDetails = loadConfigFile(TraceOrgCommonConstants.IPM_CONF);

            if ((configDetails != null) && configDetails.get("db-port") != null && (configDetails.size() > 0))
            {
                port = TraceOrgCommonUtil.getIntegerValue(configDetails.get("db-port"));
            }
        }
        catch (Exception exception)
        {
            _logger.error(exception);
        }

        return port;
    }
}
