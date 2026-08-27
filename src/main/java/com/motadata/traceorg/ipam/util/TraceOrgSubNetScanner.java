package com.motadata.traceorg.ipam.util;

import com.motadata.traceorg.ipam.logger.TraceOrgLogger;
import org.apache.commons.lang3.SystemUtils;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

public class TraceOrgSubNetScanner implements Callable<Boolean>
{
    private static final TraceOrgLogger _logger = new TraceOrgLogger(TraceOrgSubNetScanner.class, "GUI / Subnet Scan");

    String ip;

    public TraceOrgSubNetScanner(String ip)
    {
        this.ip = ip;
    }

    @Override
    public Boolean call() throws Exception
    {
        Runtime runtime = Runtime.getRuntime();

        try
        {
            String cmd = "";

            if (SystemUtils.IS_OS_WINDOWS)
            {
                cmd = TraceOrgCommonConstants.WINDOWS_PING_QUERY;
            }
            else if(SystemUtils.IS_OS_LINUX)
            {
                cmd = TraceOrgCommonConstants.LINUX_PING_QUERY;
            }

            runtime.exec(cmd + ip).waitFor(10, TimeUnit.SECONDS);

        }
        catch (Exception e)
        {
            _logger.error(e);
        }

        return true;
    }
}
