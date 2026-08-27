package com.motadata.traceorg.ipam.util;

import com.motadata.traceorg.ipam.logger.TraceOrgLogger;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class TraceOrgLinuxPingProcessKill
{
    private static final TraceOrgLogger _logger = new TraceOrgLogger(TraceOrgLinuxPingProcessKill.class, "Linux Ping Process Kill");

    public static void killPingProcess()
    {
        String line;

        try
        {
            Process process = Runtime.getRuntime().exec("ps -eo pid,cmd,etime");

            BufferedReader bufferedInputStream = new BufferedReader(new InputStreamReader(process.getInputStream()));

            while ((line = bufferedInputStream.readLine())!=null)
            {
                if(!line.isEmpty())
                {
                    String[] outputs = line.trim().replace("(","").replace(")","").split("\\n");

                    for(String token : outputs)
                    {
                        try
                        {
                            String[] results = token.trim().replace("-", ":").split("\\s+");

                            if(results[1].equals("ping") && Integer.parseInt(results[3].split(":")[0]) >= 5)
                            {
                                _logger.info("Process ID ::"+results[0]);

                                Runtime.getRuntime().exec("kill -9 "+results[0]);
                            }
                        }
                        catch (Exception exception)
                        {

                        }
                    }
                }
            }
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
        }
    }
}

