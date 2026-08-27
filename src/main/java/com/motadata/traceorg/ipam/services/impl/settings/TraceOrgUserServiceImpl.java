package com.motadata.traceorg.ipam.services.impl.settings;

import com.motadata.traceorg.ipam.controller.settings.TraceOrgGlobalSettingController;
import com.motadata.traceorg.ipam.entity.event.TraceOrgEvent;
import com.motadata.traceorg.ipam.entity.settings.TraceOrgUser;
import com.motadata.traceorg.ipam.entity.settings.TraceOrgUserRole;
import com.motadata.traceorg.ipam.logger.TraceOrgLogger;
import com.motadata.traceorg.ipam.repository.event.TraceOrgEventRepository;
import com.motadata.traceorg.ipam.repository.login.TraceOrgForgotPasswordRepository;
import com.motadata.traceorg.ipam.repository.settings.TraceOrgUserRepository;
import com.motadata.traceorg.ipam.repository.settings.TraceOrgUserRoleRepository;
import com.motadata.traceorg.ipam.services.TraceOrgService;
import com.motadata.traceorg.ipam.services.settings.TraceOrgUserService;
import com.motadata.traceorg.ipam.util.TraceOrgCommonConstants;
import com.motadata.traceorg.ipam.util.TraceOrgCommonUtil;
import com.motadata.traceorg.ipam.util.TraceOrgMessageConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.util.*;

@Service
public class TraceOrgUserServiceImpl implements TraceOrgUserService
{
    private static final TraceOrgLogger _logger = new TraceOrgLogger(TraceOrgGlobalSettingController.class, "User Service");

    @Autowired
    TraceOrgUserRepository traceOrgUserRepository;

    @Autowired
    TraceOrgUserRoleRepository traceOrgUserRoleRepository;

    @Autowired
    TraceOrgEventRepository traceOrgEventRepository;

    @Autowired
    TraceOrgCommonUtil traceOrgCommonUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    TraceOrgForgotPasswordRepository traceOrgForgotPasswordRepository;

    @Autowired
    private TokenStore tokenStore;

    @Autowired
    private TraceOrgService traceOrgService;

    private static final int OFF = 0;

    private static final int ON = 1;

    @Override
    public HashMap<String, Object> listAllUsers()
    {
        HashMap<String, Object> result = new HashMap<>();

        try
        {
            List<TraceOrgUser> traceOrgUserList = traceOrgUserRepository.findAll();

            for(TraceOrgUser traceOrgUser : traceOrgUserList)
            {
                if(traceOrgUser.isStatus())
                {
                    traceOrgUser.setActiveStatus("Enable");
                }
                else
                {
                    traceOrgUser.setActiveStatus("Disable");
                }
            }

            result.put(TraceOrgCommonConstants.SUCCESS, TraceOrgCommonConstants.TRUE);

            result.put(TraceOrgCommonConstants.DATA, traceOrgUserList);
        }
        catch (Exception exception)
        {
            _logger.error(exception);
        }

        return result;
    }

    @Override
    public HashMap<String, Object> insertUser(TraceOrgUser traceOrgUser, String accessToken)
    {
        HashMap<String, Object> result = new HashMap<>();

        try
        {

            if(traceOrgUser.getUserName() != null && !traceOrgUser.getUserName().trim().isEmpty() && traceOrgUser.getPassword() != null && !traceOrgUser.getPassword().trim().isEmpty() && traceOrgUser.getEmail() != null && !traceOrgUser.getEmail().trim().isEmpty())
            {
                if (traceOrgUserRepository.existsByUserName(traceOrgUser.getUserName()))
                {
                    result.put(TraceOrgCommonConstants.SUCCESS, TraceOrgCommonConstants.FALSE);

                    result.put(TraceOrgCommonConstants.MESSAGE, TraceOrgMessageConstants.USER_NAME_ALREADY_EXIST);

                    _logger.debug("user " + traceOrgUser.getUserName() + " is already exist");
                }
                else
                {
                    if(traceOrgUser.getRoleId()!=null)
                    {
                        TraceOrgUserRole traceOrgUserRole = traceOrgUserRoleRepository.findOne(traceOrgUser.getRoleId());

                        traceOrgUser.setUserRoleId(traceOrgUserRole);

                    }

                    traceOrgUser.setPassword(passwordEncoder.encode(URLEncoder.encode(traceOrgUser.getPassword())));

                    if(traceOrgUser.getActiveStatus() != null && !traceOrgUser.getActiveStatus().isEmpty())
                    {
                        traceOrgUser.setStatus(traceOrgUser.getActiveStatus().equalsIgnoreCase("Enable"));
                    }

                    traceOrgUserRepository.save(traceOrgUser);

                    TraceOrgEvent traceOrgEvent =  new TraceOrgEvent();

                    traceOrgEvent.setTimestamp(new Date());

                    traceOrgEvent.setDoneBy(traceOrgCommonUtil.currentUser(accessToken));

                    traceOrgEvent.setEventType("Add USER");

                    traceOrgEvent.setEventContext("User " + traceOrgUser.getUserName() + " added in IP Address Manager by " + traceOrgCommonUtil.currentUserName(accessToken));

                    traceOrgEvent.setSeverity(2);

                    traceOrgEventRepository.save(traceOrgEvent);

                    result.put(TraceOrgCommonConstants.SUCCESS, TraceOrgCommonConstants.TRUE);

                    result.put(TraceOrgCommonConstants.MESSAGE, TraceOrgMessageConstants.USER_ADD_SUCCESS);

                    _logger.debug("User " + traceOrgUser.getUserName() + " added successfully");

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
    public HashMap<String, Object> getUser(Long id)
    {
        HashMap<String, Object> result = new HashMap<>();

        try
        {
            if(id != null )
            {
                TraceOrgUser traceOrgUser = traceOrgUserRepository.findOne(id);

                if (traceOrgUser != null)
                {
                    traceOrgUser.setRoleId(traceOrgUser.getUserRoleId().getId());

                    if(traceOrgUser.isStatus())
                    {
                        traceOrgUser.setActiveStatus("Enable");
                    }
                    else
                    {
                        traceOrgUser.setActiveStatus("Disable");
                    }

                    result.put(TraceOrgCommonConstants.SUCCESS, TraceOrgCommonConstants.TRUE);

                    result.put(TraceOrgCommonConstants.DATA, traceOrgUser);
                }
                else
                {
                    result.put(TraceOrgCommonConstants.SUCCESS, TraceOrgCommonConstants.FALSE);

                    result.put(TraceOrgCommonConstants.MESSAGE, TraceOrgMessageConstants.USER_ID_WRONG);
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
    public HashMap<String, Object> updateUser(Long id, TraceOrgUser traceOrgUser, String accessToken)
    {
        HashMap<String, Object> result = new HashMap<>();

        try
        {
            TraceOrgUser currentUser = traceOrgUserRepository.findOne(id);

            if(id != null && traceOrgUser.getUserName() != null && !traceOrgUser.getUserName().trim().isEmpty() && traceOrgUser.getEmail() != null && !traceOrgUser.getEmail().trim().isEmpty())
            {
                if (currentUser != null)
                {
                    Optional<TraceOrgUser> optionalUser = traceOrgUserRepository.findByUserName(traceOrgUser.getUserName());

                    if(optionalUser.isPresent())
                    {
                        TraceOrgUser user = optionalUser.get();

                        if (traceOrgUser.getRoleId() != null && !Objects.equals(traceOrgUser.getRoleId(), currentUser.getUserRoleId().getId()))
                        {

                            Collection<OAuth2AccessToken> tokens = tokenStore.findTokensByClientIdAndUserName(TraceOrgCommonConstants.CLIENT_KEY, currentUser.getUserName());

                            if (tokens != null)
                            {
                                this.traceOrgService.switchSafeUpdateMode(OFF);

                                for (OAuth2AccessToken token : tokens)
                                {
                                    tokenStore.removeAccessToken(token);

                                    if (token.getRefreshToken() != null)
                                    {
                                        tokenStore.removeRefreshToken(token.getRefreshToken());
                                    }
                                }

                                this.traceOrgService.switchSafeUpdateMode(ON);
                            }
                        }

                        if(user.getId().equals(id))
                        {
                            TraceOrgUser loginUser = traceOrgUserRepository.findByUserName(currentUser.getUserName()).get();

                            if(!loginUser.getId().equals(currentUser.getId()))
                            {
                                if(traceOrgUser.getActiveStatus()!=null && !traceOrgUser.getActiveStatus().isEmpty())
                                {
                                    currentUser.setStatus(traceOrgUser.getActiveStatus().equals("Enable"));
                                }
                                else
                                {
                                    currentUser.setStatus(traceOrgUser.isStatus());
                                }

                                if(traceOrgUser.getRoleId()!=null)
                                {
                                    TraceOrgUserRole traceOrgUserRole = traceOrgUserRoleRepository.findOne(traceOrgUser.getRoleId());

                                    currentUser.setUserRoleId(traceOrgUserRole);
                                }

                                currentUser.setEmail(traceOrgUser.getEmail());

                                currentUser.setUserName(traceOrgUser.getUserName());

                                currentUser.setDescription(traceOrgUser.getDescription());

                                if(traceOrgUser.getActiveStatus()!=null && !traceOrgUser.getActiveStatus().isEmpty())
                                {
                                    currentUser.setStatus(traceOrgUser.getActiveStatus().equals("Enable"));
                                }
                                else
                                {
                                    currentUser.setStatus(traceOrgUser.isStatus());
                                }

                                traceOrgUserRepository.save(currentUser);

                                result.put(TraceOrgCommonConstants.SUCCESS, TraceOrgCommonConstants.TRUE);

                                result.put(TraceOrgCommonConstants.MESSAGE, TraceOrgMessageConstants.USER_UPDATE_SUCCESS);

                                _logger.debug("User " + traceOrgUser.getUserName()+" updated successfully");
                            }
                            else
                            {
                                if(traceOrgUser.getActiveStatus() != null && !traceOrgUser.getActiveStatus().isEmpty())
                                {
                                    currentUser.setStatus(traceOrgUser.getActiveStatus().equals("Enable"));
                                }
                                else
                                {
                                    currentUser.setStatus(traceOrgUser.isStatus());
                                }


                                if(currentUser.isStatus())
                                {
                                    TraceOrgUserRole traceOrgUserRole = traceOrgUserRoleRepository.findOne(traceOrgUser.getRoleId());

                                    currentUser.setUserRoleId(traceOrgUserRole);

                                    currentUser.setEmail(traceOrgUser.getEmail());

                                    currentUser.setUserName(traceOrgUser.getUserName());

                                    currentUser.setDescription(traceOrgUser.getDescription());

                                    if(traceOrgUser.getActiveStatus() != null && !traceOrgUser.getActiveStatus().isEmpty())
                                    {
                                        currentUser.setStatus(traceOrgUser.getActiveStatus().equals("Enable"));
                                    }
                                    else
                                    {
                                        currentUser.setStatus(traceOrgUser.isStatus());
                                    }

                                    traceOrgUserRepository.save(currentUser);

                                    result.put(TraceOrgCommonConstants.SUCCESS, TraceOrgCommonConstants.TRUE);

                                    result.put(TraceOrgCommonConstants.MESSAGE, TraceOrgMessageConstants.USER_UPDATE_SUCCESS);

                                    _logger.debug("User " + traceOrgUser.getUserName() + " updated successfully");
                                }
                                else
                                {
                                    result.put(TraceOrgCommonConstants.SUCCESS, TraceOrgCommonConstants.FALSE);

                                    result.put(TraceOrgCommonConstants.MESSAGE, TraceOrgMessageConstants.USER_CAN_NOT_DISABLE_OWN);
                                }
                            }
                        }
                        else
                        {
                            result.put(TraceOrgCommonConstants.SUCCESS, TraceOrgCommonConstants.FALSE);

                            result.put(TraceOrgCommonConstants.MESSAGE, TraceOrgMessageConstants.USER_NAME_ALREADY_EXIST);
                        }
                    }
                    else
                    {
                        result.put(TraceOrgCommonConstants.SUCCESS, TraceOrgCommonConstants.FALSE);

                        result.put(TraceOrgCommonConstants.MESSAGE, "Username cannot be changed.");
                    }

                }
                else
                {
                    result.put(TraceOrgCommonConstants.SUCCESS, TraceOrgCommonConstants.FALSE);

                    result.put(TraceOrgCommonConstants.MESSAGE, TraceOrgMessageConstants.USER_ID_WRONG);
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
    public HashMap<String, Object> removeUser(Long id, String accessToken) {

        HashMap<String, Object> result = new HashMap<>();

        try
        {
            if (id != null)
            {
                TraceOrgUser traceOrgUser = traceOrgUserRepository.findOne(id);

                TraceOrgUser currentUser = traceOrgUserRepository.findByUserName(traceOrgCommonUtil.currentUserName(accessToken)).get();

                String userName = traceOrgUser.getUserName();

                if (!currentUser.getId().equals(id))
                {
                    traceOrgForgotPasswordRepository.deleteByUser(traceOrgUser);

                    traceOrgEventRepository.deleteByDoneBy(traceOrgUser);

                    traceOrgUserRepository.delete(id);

                    TraceOrgEvent traceOrgEvent = new TraceOrgEvent();

                    traceOrgEvent.setTimestamp(new Date());

                    traceOrgEvent.setDoneBy(traceOrgCommonUtil.currentUser(accessToken));

                    traceOrgEvent.setEventType("Delete USER");

                    traceOrgEvent.setEventContext("User " + userName + " deleted in IP Address Manager by " + traceOrgCommonUtil.currentUserName(accessToken));

                    _logger.debug("User " + traceOrgUser.getUserName() + " deleted successfully");

                    traceOrgEvent.setSeverity(2);

                    traceOrgEventRepository.save(traceOrgEvent);

                    result.put(TraceOrgCommonConstants.SUCCESS, TraceOrgCommonConstants.TRUE);

                    result.put(TraceOrgCommonConstants.MESSAGE, TraceOrgMessageConstants.USER_DELETE_SUCCESS);
                }
                else
                {
                    result.put(TraceOrgCommonConstants.SUCCESS, TraceOrgCommonConstants.FALSE);

                    result.put(TraceOrgCommonConstants.MESSAGE, TraceOrgMessageConstants.USER_CAN_NOT_DELETE_OWN);
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
    public HashMap<String, Object> listAllUserRoles()
    {
        HashMap<String, Object> result = new HashMap<>();

        try
        {
            List<TraceOrgUserRole> userRoleList = traceOrgUserRoleRepository.findAll();

            if(userRoleList != null)
            {
                result.put(TraceOrgCommonConstants.SUCCESS, TraceOrgCommonConstants.TRUE);

                result.put(TraceOrgCommonConstants.DATA, userRoleList);
            }
        }
        catch (Exception exception)
        {
            _logger.error(exception);
        }

        return result;
    }

    @Override
    public HashMap<String, Object> changePassword(Long id, TraceOrgUser traceOrgUser)
    {
        HashMap<String, Object> result = new HashMap<>();

        try
        {
            if(id != null && traceOrgUser.getPassword() != null  && !traceOrgUser.getPassword().trim().isEmpty())
            {
                TraceOrgUser currentTraceOrgUser = this.traceOrgUserRepository.findOne(id);

                currentTraceOrgUser.setPassword(passwordEncoder.encode(URLEncoder.encode(traceOrgUser.getPassword())));

                traceOrgUserRepository.save(currentTraceOrgUser);

                result.put(TraceOrgCommonConstants.SUCCESS, TraceOrgCommonConstants.TRUE);

                result.put(TraceOrgCommonConstants.MESSAGE, TraceOrgMessageConstants.USER_PASSWORD_UPDATE_SUCCESS);
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
}
