package com.motadata.traceorg;

import java.util.concurrent.CountDownLatch;

public class TraceOrgIPAMProcessHelper implements Runnable
{
    private static final TraceOrgLogger _logger = new TraceOrgLogger(TraceOrgIPAMProcessHelper.class, "IPAM Kernel / Helper");

    private final ProcessBuilder m_builder;

    private Process m_process;

    private final CountDownLatch m_waitHandler;

    public TraceOrgIPAMProcessHelper(ProcessBuilder builder, CountDownLatch waitHandler)
    {
        m_builder = builder;

        m_waitHandler = waitHandler;
    }

    public void run()
    {
        try
        {
            if (m_builder != null)
            {
                m_process = m_builder.start();

                m_waitHandler.countDown();
            }
        }
        catch (Exception exception)
        {
            _logger.error(exception);
        }
    }

    public Process getProcess()
    {
        return m_process;
    }
}
