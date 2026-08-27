package com.motadata.traceorg.ipam.util;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.motadata.traceorg.ipam.logger.TraceOrgLogger;
import org.apache.commons.io.IOUtils;

import java.io.InputStream;

/**
 * Created by hardik on 17/6/18.
 */

public class TraceOrgSSHUtil implements TraceOrgAbstractCommonUtil
{

    private Session m_session = null;

    private final String m_host;

    private final int m_port;

    private final String m_userName;

    private final String m_password;

    private final int m_timeout;

    private static final TraceOrgLogger _logger = new TraceOrgLogger(TraceOrgSSHUtil.class, "GUI / SSH Util");

    public TraceOrgSSHUtil(String ipAddress, String userName, String password)
    {

        this(ipAddress, 22, userName, password, 60);
    }

    public TraceOrgSSHUtil(String ipAddress, String userName, String password, int timeout)
    {

        this(ipAddress, 22, userName, password, timeout);
    }

    public TraceOrgSSHUtil(String ipAddress, int port, String userName, String password, int timeout)
    {
        this.m_host = ipAddress;

        this.m_port = port;

        this.m_userName = userName;

        this.m_timeout = timeout;

        this.m_password = password;
    }

    @Override
    public boolean init() throws Exception
    {

        boolean connect;

        connect = connect();

        if (connect)
        {
            _logger.debug("connected to " + m_host);
        }
        else
        {
            _logger.warn("failed to connect to " + m_host);
        }

        return connect;
    }

    @Override
    public void destroy() throws Exception
    {
        disconnect();
    }

    @Override
    public boolean reInit() throws Exception
    {
        destroy();

        return init();
    }

    public boolean connect() throws Exception
    {
        _logger.debug("checking connection with " + m_host + " and port " + m_port);

        boolean connected = false;

        JSch jsch = new JSch();

        m_session = jsch.getSession(m_userName, m_host, m_port);

        if (m_password != null && m_password.trim().length() > 0)
        {
            m_session.setPassword(m_password);
        }

        m_session.setConfig("StrictHostKeyChecking", "no");

        m_session.connect(m_timeout * 1000);

        m_session.setTimeout(m_timeout * 1000);

        if (m_session.isConnected())
        {
            connected = true;

            _logger.debug(m_host + ":connection done");
        }
        else
        {
            _logger.warn(m_host + ":connection failed");
        }
        return connected;
    }

    private void disconnect()
    {
        if (m_session != null)
        {
            m_session.disconnect();
        }
    }

    public String executeCommand(String command) throws Exception
    {
        return executeCommand(command, true);
    }

    public String executeCommand(String command, boolean wait) throws Exception
    {
        _logger.debug(this.m_host + ":executing command " + command);

        ChannelExec channel = null;

        InputStream inputStream = null;

        StringBuilder output = new StringBuilder();

        if (this.m_session != null)
        {
            channel = (ChannelExec) this.m_session.openChannel("exec");

            channel.setCommand(command);

            channel.setInputStream(null);

            inputStream = channel.getInputStream();

            _logger.debug(this.m_host + ":checking channel connection");

            channel.connect();

            if (channel.isConnected())
            {
                _logger.debug(this.m_host + ":channel is currently connected");

                if (wait)
                {
                    output.append(IOUtils.toString(inputStream));

                    output.append(TraceOrgCommonConstants.NEW_LINE);

                    _logger.trace(this.m_host + ":command output " + output.toString());
                }
            }
            else
            {
                _logger.warn(this.m_host + ":channel is not connected");
            }
        }
        else
        {
            _logger.warn(this.m_host + ":session is expired");
        }

        if (inputStream != null)
        {
            inputStream.close();

            _logger.debug(this.m_host + ":channel is closed");
        }

        if (channel != null && !channel.isClosed())
        {
            channel.disconnect();

            _logger.debug(this.m_host + ":channel is disconnected");
        }
        return output.toString();
    }
}