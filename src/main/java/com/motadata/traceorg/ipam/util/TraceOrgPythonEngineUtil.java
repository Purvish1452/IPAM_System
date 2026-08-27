package com.motadata.traceorg.ipam.util;

import com.motadata.traceorg.ipam.logger.TraceOrgLogger;
import com.motadata.traceorg.ipam.entity.dhcp.TraceOrgDhcpCredentialDetails;
import org.apache.commons.codec.binary.Base64;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TraceOrgPythonEngineUtil implements TraceOrgAbstractCommonUtil
{

    private static final TraceOrgLogger _logger = new TraceOrgLogger(TraceOrgPythonEngineUtil.class, "Python Engine Util");

    @Override
    public boolean init() throws Exception
    {
        return true;
    }

    @Override
    public void destroy() throws Exception
    {

    }

    @Override
    public boolean reInit() throws Exception
    {
        return true;
    }

    public static String discover(HashMap<String, Object> context) throws Exception
    {

        String result = null;

        String requestType = "discovery";

        try
        {
            HashMap<String, Object> response = execute(context, requestType);

            if (response != null && response.containsKey(TraceOrgCommonConstants.RESULT))
            {
                result = TraceOrgCommonUtil.getStringValue(response.get(TraceOrgCommonConstants.RESULT));
            }

            else if (response != null && response.containsKey(TraceOrgCommonConstants.ERROR_CODE))
            {
                context.put(TraceOrgCommonConstants.ERROR_CODE, response.get(TraceOrgCommonConstants.ERROR_CODE));
            }
        }
        catch (Exception exception)
        {
            _logger.error(exception);
        }

        return result;
    }

    public static HashMap<String, Object> collect(HashMap<String, Object> context) throws Exception
    {
        HashMap<String, Object> response = null;

        try
        {
            HashMap<String, Object> result = execute(context, "collect");

            if (result != null && result.containsKey(TraceOrgCommonConstants.RESULT))
            {
                response = (HashMap<String, Object>) result.get(TraceOrgCommonConstants.RESULT);

            }
        }
        catch (Exception exception)
        {
            _logger.error(exception);
        }

        return response;
    }

    static String getError(HashMap<String, Object> context)
    {
        String error = null;

        try
        {
            if (context.containsKey(TraceOrgCommonConstants.ERROR_CODE) && context.get(TraceOrgCommonConstants.ERROR_CODE) != null)
            {
                error = TraceOrgCommonUtil.getStringValue(context.get(TraceOrgCommonConstants.ERROR_CODE));
            }
        }
        catch (Exception exception)
        {
            _logger.error(exception);
        }

        return error;
    }

    private static HashMap<String, Object> execute(HashMap<String, Object> context, String requestType) throws Exception
    {
        Process process;

        BufferedReader bufferedReader;

        InputStream inputStream;

        StringBuilder scriptOutput = new StringBuilder();

        HashMap<String, Object> response = null;

        HashMap<String,Object> pluginContext = new HashMap<>();

        File pluginPythonFile;

        try
        {
            TraceOrgDhcpCredentialDetails traceOrgDhcpCredentialDetails = (TraceOrgDhcpCredentialDetails) context.get(TraceOrgCommonConstants.PLUGIN_CONTEXT );

            pluginContext.put("timeout", 60);

            pluginContext.put("request-type", requestType);

            if(traceOrgDhcpCredentialDetails.getUserName() != null)
            {
                pluginContext.put("username",traceOrgDhcpCredentialDetails.getUserName());
            }

            if(traceOrgDhcpCredentialDetails.getPassword() != null)
            {
                pluginContext.put("password",traceOrgDhcpCredentialDetails.getPassword());
            }

            if(traceOrgDhcpCredentialDetails.getHostAddress() != null)
            {
                pluginContext.put("host",traceOrgDhcpCredentialDetails.getHostAddress());
            }

            if(requestType.trim().equals("collect"))
            {
                pluginContext.put("scope-ids",context.get("scope-ids"));
            }
            List<String> arguments = new ArrayList<>();

            if( TraceOrgCommonConstants.OS_NAME.equals("Windows 95"))
            {
                arguments.add("command.com");
            }
            else
            {
                arguments.add("cmd.exe");
            }

            arguments.add("/C");

            arguments.add("python");

            String pluginLocation;

            File currentFile = new File(TraceOrgCommonConstants.CURRENT_DIR);

            File parentFile = new File(currentFile.getParent());

            String parentFilePath = parentFile.getParent();

            pluginLocation = parentFilePath + TraceOrgCommonConstants.PATH_SEPARATOR+"python-engine" + TraceOrgCommonConstants.PATH_SEPARATOR +
                    "com" + TraceOrgCommonConstants.PATH_SEPARATOR + "motadata" + TraceOrgCommonConstants.PATH_SEPARATOR + "traceorg" +
                    TraceOrgCommonConstants.PATH_SEPARATOR + "python" + TraceOrgCommonConstants.PATH_SEPARATOR + "plugin.py";

            /*pluginLocation = TraceOrgCommonConstants.CURRENT_DIR + TraceOrgCommonConstants.PATH_SEPARATOR+"python-engine" + TraceOrgCommonConstants.PATH_SEPARATOR +
                "com" + TraceOrgCommonConstants.PATH_SEPARATOR + "motadata" + TraceOrgCommonConstants.PATH_SEPARATOR + "traceorg" +
                TraceOrgCommonConstants.PATH_SEPARATOR + "python" + TraceOrgCommonConstants.PATH_SEPARATOR + "plugin.py";*/

            pluginPythonFile = new File(pluginLocation);

            if (pluginPythonFile.exists())
            {
                arguments.add(pluginLocation);

                switch (requestType)
                {
                    case "discovery":

                        arguments.add("discovery");

                        break;

                    case "collector":

                        arguments.add("collector");

                        break;

                    default:

                        arguments.add("collector");

                        break;
                }

                arguments.add(new String(Base64.encodeBase64(TraceOrgCommonUtil.getJSON(pluginContext).getBytes())));

                ProcessBuilder processBuilder = new ProcessBuilder(arguments);

                // For windows only
                processBuilder.directory(new File(parentFilePath+TraceOrgCommonConstants.PATH_SEPARATOR+"python"));

                process = processBuilder.start();

                inputStream = process.getInputStream();

                bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

                String line;

                while ((line = bufferedReader.readLine()) != null)
                {
                    scriptOutput.append(line).append(TraceOrgCommonConstants.NEW_LINE);
                }

                inputStream.close();

                bufferedReader.close();

                process.getErrorStream().close();

                process.getOutputStream().close();

                process.getInputStream().close();

                process.destroy();

                if (scriptOutput.length() > 0)
                {
                    try
                    {
                        response = TraceOrgCommonUtil.deserialize(scriptOutput.toString().replace("\r", "\\r").replace("\n", "\\n"));

                        if (response != null && response.containsKey(TraceOrgCommonConstants.RESULT))
                        {
                            _logger.trace("response of python engine request :" + response.get(TraceOrgCommonConstants.RESULT).toString());
                        }
                    }

                    catch (Exception exception)
                    {
                        //This debug log would give us information due to which character from python it is not allowing to deserialize python output

                        _logger.debug("error occurred while deserializing output " + scriptOutput.toString() + " of plugin : " + context.get(TraceOrgCommonConstants.PLUGIN_ID));

                        _logger.error(exception);
                    }
                }
            }

            else
            {
                _logger.warn("plugin file is missing :" + pluginLocation);

            }
        }
        catch (Exception exception)
        {
            _logger.error(exception);
        }

        return response;
    }
}



























































































































































































































































































































































