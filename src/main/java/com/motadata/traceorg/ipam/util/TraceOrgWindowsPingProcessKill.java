package com.motadata.traceorg.ipam.util;

import com.motadata.traceorg.ipam.logger.TraceOrgLogger;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class TraceOrgWindowsPingProcessKill
{
    private static final TraceOrgLogger _logger = new TraceOrgLogger(TraceOrgWindowsPingProcessKill.class, "GUI / WINDOWS Ping Process Kill");

    public static void killPingProcess()
    {
        String line;

        try
        {
            Process process = Runtime.getRuntime().exec("tasklist /fi \"cputime gt 00:05:00\"");

            BufferedReader bufferedInputStream = new BufferedReader(new InputStreamReader(process.getInputStream()));

            while ((line = bufferedInputStream.readLine())!=null)
            {
                if(!line.isEmpty())
                {
                    String[] outputs = line.trim().replace("(","").replace(")","").split("\\n");

                    for(String token : outputs)
                    {
                        String[] results = token.trim().replace("-", ":").split("\\s+");

                        if(results[0].equals("PING.EXE"))
                        {
                            _logger.info(line);

                            Runtime.getRuntime().exec("TASKKILL /F /PID "+results[1]+"");
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
}

