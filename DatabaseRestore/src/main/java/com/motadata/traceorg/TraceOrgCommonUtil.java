package com.motadata.traceorg;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.net.Socket;
import java.security.Key;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@SuppressWarnings("ALL")
public class TraceOrgCommonUtil
{
    private static final TraceOrgLogger _logger = new TraceOrgLogger(TraceOrgCommonUtil.class, "GUI / Common Util");

    private final static AtomicInteger m_logLevel = new AtomicInteger(2);

    public static Key generateKey()
    {
        return new SecretKeySpec(TraceOrgCommonConstants.ENCRYPT_DECRYPT_KEY, TraceOrgCommonConstants.ALGORITHM);
    }

    public static boolean debugEnabled()
    {
        return 1 >= getLogLevel();
    }

    public static boolean warningEnabled()
    {
        return 3 >= getLogLevel();
    }

    public static boolean infoEnabled()
    {
        return 2 >= getLogLevel();
    }

    public static boolean errorEnabled()
    {
        return 4 >= getLogLevel();
    }

    public static boolean traceEnabled()
    {
        return 0 >= getLogLevel();
    }

    public static void setLogLevel(int logLevel)
    {
        m_logLevel.set(logLevel);
    }

    public static short getLogLevel()
    {
        return m_logLevel.shortValue();
    }

    public static String getIPAMPath()
    {
        String path = null;

        try
        {
            File currentFile = new File(TraceOrgCommonConstants.CURRENT_DIR);

            File parentFile = new File(currentFile.getParent());

            path = parentFile.getParent();
        }
        catch (Exception e)
        {
            _logger.error(e);
        }

        return path;
    }

    public static String decrypt(String value)
    {

        String result = "";

        try
        {
            if (value != null && value.length() > 0)
            {
                Key key = generateKey();

                Cipher cipher = Cipher.getInstance(TraceOrgCommonConstants.ALGORITHM);

                cipher.init(Cipher.DECRYPT_MODE, key);

                byte[] decodedValue = Base64.getDecoder().decode(value);

                byte[] decodedBytes = cipher.doFinal(decodedValue);

                result = new String(decodedBytes);
            }

        }

        catch (Exception exception)
        {
            _logger.error(exception);
        }

        return result;
    }

    static int getIntegerValue(Object target)
    {
        int value = 0;

        try
        {
            if (target != null)
            {
                value = Integer.parseInt(target.toString());
            }
        }
        catch (Exception exception)
        {
            _logger.error(exception);
        }

        return value;
    }

    public static boolean isPortReachable(String ipAddress, int port)
    {
        boolean result;

        try
        {
            Socket socket = new Socket(ipAddress, port);

            socket.close();

            result = true;

            _logger.warn("port "+port + " is already running");

        }
        catch (Exception ignored)
        {
            result = false;

            _logger.info("port "+port + " is available");
        }

        return result;
    }
}
