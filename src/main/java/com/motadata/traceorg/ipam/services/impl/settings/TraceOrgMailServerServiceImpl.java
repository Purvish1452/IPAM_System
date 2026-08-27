package com.motadata.traceorg.ipam.services.impl.settings;

import com.motadata.traceorg.ipam.controller.settings.TraceOrgGlobalSettingController;
import com.motadata.traceorg.ipam.entity.Response;
import com.motadata.traceorg.ipam.entity.settings.TraceOrgMailServer;
import com.motadata.traceorg.ipam.logger.TraceOrgLogger;
import com.motadata.traceorg.ipam.repository.settings.TraceOrgMailServerRepository;
import com.motadata.traceorg.ipam.services.settings.TraceOrgMailServerService;
import com.motadata.traceorg.ipam.util.TraceOrgCommonConstants;
import com.motadata.traceorg.ipam.util.TraceOrgCommonUtil;
import com.motadata.traceorg.ipam.util.TraceOrgMessageConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;

@Service
public class TraceOrgMailServerServiceImpl implements TraceOrgMailServerService
{
    private static final TraceOrgLogger _logger = new TraceOrgLogger(TraceOrgGlobalSettingController.class, "Mail Server Service");

    @Autowired
    TraceOrgMailServerRepository traceOrgMailServerRepository;

    @Autowired
    TraceOrgCommonUtil traceOrgCommonUtil;

    @Override
    public HashMap<String, Object> listAllMailServer()
    {
        HashMap<String, Object> result = new HashMap<>();

        try
        {
            List<TraceOrgMailServer> traceOrgMailServerList = traceOrgMailServerRepository.findAll();

            result.put(TraceOrgCommonConstants.SUCCESS, TraceOrgCommonConstants.TRUE);

            result.put(TraceOrgCommonConstants.DATA, traceOrgMailServerList);
        }
        catch (Exception exception)
        {
            _logger.error(exception);
        }

        return result;
    }

    @Override
    public HashMap<String, Object> getMailServer(Long id)
    {
        HashMap<String, Object> result = new HashMap<>();

        try
        {
            if(id !=null)
            {
                TraceOrgMailServer traceOrgMailServer = traceOrgMailServerRepository.findOne(id);

                if (traceOrgMailServer != null)
                {
                    result.put(TraceOrgCommonConstants.SUCCESS, TraceOrgCommonConstants.TRUE);

                    result.put(TraceOrgCommonConstants.DATA, traceOrgMailServer);
                }
                else
                {
                    result.put(TraceOrgCommonConstants.SUCCESS, TraceOrgCommonConstants.FALSE);

                    result.put(TraceOrgCommonConstants.MESSAGE, TraceOrgMessageConstants.MAIL_SERVER_ID_NOT_VALID);
                }
            }
            else
            {
                result.put(TraceOrgCommonConstants.SUCCESS, TraceOrgCommonConstants.FALSE);

                result.put(TraceOrgCommonConstants.MESSAGE, (TraceOrgMessageConstants.ENTER_VALID_DETAILS));
            }
        }
        catch (Exception exception)
        {
            _logger.error(exception);
        }

        return result;
    }

    @Override
    public HashMap<String, Object> updateMailServer(Long id, TraceOrgMailServer traceOrgMailServer)
    {
        HashMap<String, Object> result = new HashMap<>();

        try
        {
            if(id !=null && traceOrgMailServer.getMailHost() !=null && !traceOrgMailServer.getMailHost().trim().isEmpty()
                    && traceOrgMailServer.getMailPort() !=null && traceOrgMailServer.getMailUserName()!=null && !traceOrgMailServer.getMailUserName().trim().isEmpty()
                    && traceOrgMailServer.getMailPassword() !=null && !traceOrgMailServer.getMailPassword().trim().isEmpty())
            {
                Response response2= traceOrgCommonUtil.testMailServer(traceOrgMailServer);

                if(response2.isSuccess())
                {
                    traceOrgMailServer.setId(id);

                    if(id ==1)
                        traceOrgMailServer.setMailType("Primary");
                    else if(id ==2)
                        traceOrgMailServer.setMailType("Secondary");

                    traceOrgMailServerRepository.save(traceOrgMailServer);

                    _logger.debug("Mail server " + traceOrgMailServer.getMailHost() +" updated successfully");

                    result.put(TraceOrgCommonConstants.SUCCESS, TraceOrgCommonConstants.TRUE);

                    result.put(TraceOrgCommonConstants.MESSAGE, TraceOrgMessageConstants.MAIL_SERVER_UPDATE_SUCCESS);
                }
                else
                {
                    result.put(TraceOrgCommonConstants.SUCCESS, TraceOrgCommonConstants.FALSE);

                    result.put(TraceOrgCommonConstants.MESSAGE, TraceOrgMessageConstants.MAIL_SERVER_NOT_VALID);
                }
            }
            else
            {
                result.put(TraceOrgCommonConstants.SUCCESS, TraceOrgCommonConstants.FALSE);

                result.put(TraceOrgCommonConstants.MESSAGE, TraceOrgMessageConstants.ENTER_VALID_DETAILS);
            }
        }
        catch (Exception exception)
        {
            _logger.error(exception);
        }

        return result;
    }

    @Override
    public HashMap<String, Object> insertMailServer(String mailToEmail)
    {
        HashMap<String, Object> result = new HashMap<>();

        try
        {
            if(mailToEmail != null && !mailToEmail.trim().isEmpty())
            {
                Long id = traceOrgMailServerRepository.findMaxId();

                TraceOrgMailServer traceOrgMailServer = new TraceOrgMailServer();

                traceOrgMailServer.setMailToEmail(mailToEmail);

                if(id <= 2)
                {
                    traceOrgMailServer.setId(3L);
                }
                else
                {
                    traceOrgMailServer.setId(id + 1);
                }

                traceOrgMailServerRepository.save(traceOrgMailServer);

                _logger.debug("Mail server " + traceOrgMailServer.getMailHost() + " inserted successfully");

                result.put(TraceOrgCommonConstants.SUCCESS, TraceOrgCommonConstants.TRUE);

                result.put(TraceOrgCommonConstants.MESSAGE, TraceOrgMessageConstants.MAIL_ADD_SUCCESS);
            }
            else
            {
                result.put(TraceOrgCommonConstants.SUCCESS, TraceOrgCommonConstants.FALSE);

                result.put(TraceOrgCommonConstants.MESSAGE, TraceOrgMessageConstants.ENTER_VALID_DETAILS);
            }
        }
        catch (Exception exception)
        {
            _logger.error(exception);
        }

        return result;
    }

    /***
     * IPAM-134 IPAM | Mail Server Configuration issue
     * Used the userId as the username instead of FromEmail.
     * */
    @Override
    public HashMap<String, Object> testMailServer(TraceOrgMailServer traceOrgMailServer)
    {
        HashMap<String, Object> result = new HashMap<>();

        try
        {
            if(traceOrgMailServer.getMailHost() !=null && !traceOrgMailServer.getMailHost().trim().isEmpty()
                    && traceOrgMailServer.getMailPort() !=null && traceOrgMailServer.getMailUserName()!=null && !traceOrgMailServer.getMailUserName().trim().isEmpty()
                    && traceOrgMailServer.getMailPassword() !=null && !traceOrgMailServer.getMailPassword().trim().isEmpty())
            {
                TraceOrgCommonUtil.sendMail(traceOrgMailServer.getMailHost(),
                        traceOrgMailServer.getMailPort(),
                        "IPAM Test Mail",
                        "Hello " + traceOrgMailServer.getMailUserName() + ", <br><br> <t>Test Message...<br><br> Thank You.",
                        traceOrgMailServer.getMailFromEmail(),
                        traceOrgMailServer.getMailToEmail(),
                        traceOrgMailServer.getMailProtocol(),
                        traceOrgMailServer.getMailUserId(),
                        traceOrgMailServer.getMailPassword(),
                        traceOrgMailServer.getMailTimeout());

                result.put(TraceOrgCommonConstants.SUCCESS, TraceOrgCommonConstants.TRUE);

                result.put(TraceOrgCommonConstants.MESSAGE, TraceOrgMessageConstants.MAIL_SERVER_VALID);
            }
            else
            {
                result.put(TraceOrgCommonConstants.SUCCESS, TraceOrgCommonConstants.FALSE);

                result.put(TraceOrgCommonConstants.MESSAGE, TraceOrgMessageConstants.ENTER_VALID_DETAILS);
            }
        }
        catch (Exception exception)
        {
            _logger.error(exception);

            result.put(TraceOrgCommonConstants.SUCCESS, TraceOrgCommonConstants.FALSE);

            result.put(TraceOrgCommonConstants.MESSAGE, TraceOrgMessageConstants.MAIL_SERVER_NOT_VALID);
        }

        return result;
    }
}
