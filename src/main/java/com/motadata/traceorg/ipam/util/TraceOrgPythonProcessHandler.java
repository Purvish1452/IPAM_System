package com.motadata.traceorg.ipam.util;

import com.motadata.traceorg.ipam.logger.TraceOrgLogger;
import com.zaxxer.nuprocess.NuAbstractProcessHandler;

import java.nio.ByteBuffer;
import java.util.HashMap;

public class TraceOrgPythonProcessHandler extends NuAbstractProcessHandler {

    private static final TraceOrgLogger _logger = new TraceOrgLogger(TraceOrgSubnetUtil.class, "GUI / Python Process Handler");

    private StringBuilder processResult = new StringBuilder(1000);

    private StringBuilder processError = new StringBuilder(1000);

    private HashMap<String, Object> pythonResult = new HashMap<>();

    @Override
    public void onStderr(ByteBuffer buffer, boolean closed)
    {
        try
        {
            if (!closed)
            {
                byte[] bytes = new byte[buffer.remaining()];

                buffer.get(bytes);

                String line = new String(bytes);

                if(line.trim().length() > 0)
                {
                    processError.append(line);
                }

                _logger.warn("Nu process python response error: " + processError);

                super.onStderr(buffer, false);
            }
        }
        catch (Exception exception)
        {
            _logger.error(exception);
        }
    }

    @Override
    public void onStdout(ByteBuffer buffer, boolean closed)
    {
        try
        {
            if (!closed)
            {
                byte[] bytes = new byte[buffer.remaining()];

                buffer.get(bytes);

                String line = new String(bytes);

                if(line.trim().length() > 0)
                {
                    processResult.append(line);
                }

                super.onStdout(buffer, false);
            }

            if(closed && processResult.length() > 0)
            {
                pythonResult = TraceOrgCommonUtil.deserialize(processResult.toString().trim().replace("\r", "\\r").replace("\n", "\\n"));
            }
        }
        catch (Exception exception)
        {
            _logger.error(exception);
        }
    }

    public HashMap<String, Object> getPythonResult() {
        return pythonResult;
    }

    public void setPythonResult(HashMap<String, Object> pythonResult) {
        this.pythonResult = pythonResult;
    }
}
