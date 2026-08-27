package com.motadata.traceorg.ipam.util;
import com.motadata.traceorg.ipam.logger.TraceOrgLogger;
import com.zaxxer.nuprocess.NuAbstractProcessHandler;
import java.nio.ByteBuffer;
import java.util.HashMap;

public class TraceOrgGoPingHandler extends NuAbstractProcessHandler {

    private static final TraceOrgLogger _logger = new TraceOrgLogger(TraceOrgSubnetUtil.class, "GUI / Go Ping Handler");

    private StringBuilder processResult = new StringBuilder(1000);

    private StringBuilder processError = new StringBuilder(1000);

    private HashMap<String, Object> goPingResult = new HashMap<>();

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

                super.onStderr(buffer, false);
            }

            if(closed && processError.length() > 0)
            {
                _logger.warn("Nu process go-ping response error: " + processError);
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
                goPingResult = TraceOrgCommonUtil.deserialize(processResult.toString().trim());
            }
        }
        catch (Exception exception)
        {
            _logger.error(exception);
        }
    }

    public HashMap<String, Object> getGoPingResult() {
        return goPingResult;
    }

    public void setGoPingResult(HashMap<String, Object> goPingResult) {
        this.goPingResult = goPingResult;
    }
}
