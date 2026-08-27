package com.motadata.traceorg.ipam.services.impl.settings;

import com.motadata.traceorg.ipam.dto.settings.TraceOrgFeatureDTO;
import com.motadata.traceorg.ipam.dto.settings.TraceOrgPermissionDTO;
import com.motadata.traceorg.ipam.dto.settings.TraceOrgRoleDTO;
import com.motadata.traceorg.ipam.entity.event.TraceOrgEvent;
import com.motadata.traceorg.ipam.entity.settings.TraceOrgFeature;
import com.motadata.traceorg.ipam.entity.settings.TraceOrgRoleFeaturePermission;
import com.motadata.traceorg.ipam.entity.settings.TraceOrgUser;
import com.motadata.traceorg.ipam.entity.settings.TraceOrgUserRole;

import com.motadata.traceorg.ipam.logger.TraceOrgLogger;
import com.motadata.traceorg.ipam.repository.event.TraceOrgEventRepository;
import com.motadata.traceorg.ipam.repository.settings.TraceOrgFeatureRepository;
import com.motadata.traceorg.ipam.repository.settings.TraceOrgRoleFeaturePermissionRepository;
import com.motadata.traceorg.ipam.repository.settings.TraceOrgUserRepository;
import com.motadata.traceorg.ipam.repository.settings.TraceOrgUserRoleRepository;
import com.motadata.traceorg.ipam.services.TraceOrgService;
import com.motadata.traceorg.ipam.services.settings.TraceOrgUserRoleService;
import com.motadata.traceorg.ipam.util.TraceOrgCommonConstants;
import com.motadata.traceorg.ipam.util.TraceOrgCommonUtil;
import com.motadata.traceorg.ipam.util.TraceOrgMessageConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.*;
import java.util.stream.Collectors;
/**
 * IPAM-147
 * IPAM Roadmap : Admin should be able to create Users and should be able to give specific role based access rights to specific user.
 * Added permission based access control
 */
@Service
public class TraceOrgUserRoleServiceImpl implements TraceOrgUserRoleService {

    private static final TraceOrgLogger _logger = new TraceOrgLogger(TraceOrgUserRoleServiceImpl.class, "Role Service");

    TraceOrgUserRoleRepository traceOrgUserRoleRepository;

    TraceOrgFeatureRepository traceOrgFeatureRepository;

    TraceOrgRoleFeaturePermissionRepository traceOrgRoleFeaturePermissionRepository;

    TraceOrgEventRepository traceOrgEventRepository;

    TraceOrgCommonUtil traceOrgCommonUtil;

    TraceOrgUserRepository traceOrgUserRepository;

    TraceOrgService traceOrgService;

    private static final int OFF = 0;

    private static final int ON = 1;

    @Autowired
    private TokenStore tokenStore;

    public TraceOrgUserRoleServiceImpl(TraceOrgFeatureRepository traceOrgFeatureRepository, TraceOrgUserRoleRepository traceOrgUserRoleRepository, TraceOrgRoleFeaturePermissionRepository traceOrgRoleFeaturePermissionRepository, TraceOrgEventRepository traceOrgEventRepository, TraceOrgCommonUtil traceOrgCommonUtil, TraceOrgUserRepository traceOrgUserRepository,TraceOrgService traceOrgService) {
        this.traceOrgFeatureRepository = traceOrgFeatureRepository;
        this.traceOrgUserRoleRepository = traceOrgUserRoleRepository;
        this.traceOrgRoleFeaturePermissionRepository = traceOrgRoleFeaturePermissionRepository;
        this.traceOrgEventRepository = traceOrgEventRepository;
        this.traceOrgCommonUtil = traceOrgCommonUtil;
        this.traceOrgUserRepository = traceOrgUserRepository;
        this.traceOrgService = traceOrgService;
    }

    @Transactional
    @Override
    public HashMap<String, Object> createRole(TraceOrgRoleDTO roleDTO, String accessToken) {

        HashMap<String, Object> result = new HashMap<>();

        try {

            if (traceOrgUserRoleRepository.existsByRole(roleDTO.getRoleName()))
            {
                result.put(TraceOrgCommonConstants.SUCCESS, TraceOrgCommonConstants.FALSE);

                result.put(TraceOrgCommonConstants.MESSAGE, TraceOrgMessageConstants.ROLE_ALREADY_EXIST);

                _logger.debug("Role " + roleDTO.getRoleName() + " is already exist");
            }
            else
            {
                TraceOrgUserRole traceOrgUserRole = new TraceOrgUserRole();

                traceOrgUserRole.setRole(roleDTO.getRoleName());

                traceOrgUserRole.setDescription(roleDTO.getDescription());

                traceOrgUserRole = traceOrgUserRoleRepository.save(traceOrgUserRole);

                for (TraceOrgPermissionDTO permissionDTO : roleDTO.getPermissions())
                {
                    TraceOrgFeature feature = traceOrgFeatureRepository.findByName(permissionDTO.getFeatureName());

                    TraceOrgRoleFeaturePermission roleFeaturePermission = new TraceOrgRoleFeaturePermission();

                    roleFeaturePermission.setRole(traceOrgUserRole.getId());

                    roleFeaturePermission.setFeature(feature);

                    roleFeaturePermission.setWritePermission(permissionDTO.isWrite());

                    if(permissionDTO.isWrite())
                    {
                        roleFeaturePermission.setReadPermission(permissionDTO.isWrite());
                    }
                    else
                    {
                        roleFeaturePermission.setReadPermission(permissionDTO.isRead());
                    }

                    traceOrgRoleFeaturePermissionRepository.save(roleFeaturePermission);
                }

                TraceOrgEvent traceOrgEvent = new TraceOrgEvent();

                traceOrgEvent.setTimestamp(new Date());

                traceOrgEvent.setDoneBy(traceOrgCommonUtil.currentUser(accessToken));

                traceOrgEvent.setEventType("Add ROLE");

                traceOrgEvent.setEventContext("Role " + traceOrgUserRole.getRole() + " added in IP Address Manager by " + traceOrgCommonUtil.currentUserName(accessToken));

                traceOrgEvent.setSeverity(2);

                traceOrgEventRepository.save(traceOrgEvent);

                result.put(TraceOrgCommonConstants.SUCCESS, TraceOrgCommonConstants.TRUE);

                result.put(TraceOrgCommonConstants.MESSAGE, TraceOrgMessageConstants.ROLE_ADD_SUCCESS);

                _logger.debug("Role " + traceOrgUserRole.getRole() + " added successfully");
            }
        }
        catch (Exception e)
        {
            _logger.error(e);
        }

        return result;
    }

    @Override
    public HashMap<String, Object> listAllRoles() {
        HashMap<String, Object> result = new HashMap<>();

        try
        {
            List<TraceOrgUserRole> traceOrgRoleList = traceOrgUserRoleRepository.findAll();

            result.put(TraceOrgCommonConstants.SUCCESS, TraceOrgCommonConstants.TRUE);

            result.put(TraceOrgCommonConstants.DATA, traceOrgRoleList);
        }
        catch (Exception exception)
        {
            _logger.error(exception);
        }

        return result;
    }

    @Override
    public HashMap<String, Object> getRole(Long id)
    {

        HashMap<String, Object> result = new HashMap<>();

        try
        {
            if(id != null )
            {
                TraceOrgUserRole traceOrgUserRole = traceOrgUserRoleRepository.findOne(id);

                if (traceOrgUserRole != null)
                {
                    result.put(TraceOrgCommonConstants.SUCCESS, TraceOrgCommonConstants.TRUE);

                    result.put(TraceOrgCommonConstants.DATA, traceOrgUserRole);
                }
                else
                {
                    result.put(TraceOrgCommonConstants.SUCCESS, TraceOrgCommonConstants.FALSE);

                    result.put(TraceOrgCommonConstants.MESSAGE, TraceOrgMessageConstants.ROLE_ID_WRONG);
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
    public HashMap<String, Object> updateRole(TraceOrgRoleDTO roleDTO, String accessToken) {

        HashMap<String, Object> result = new HashMap<>();
        try
        {
            if(!Objects.equals(roleDTO.getRoleName(), ""))
            {
                if (roleDTO.getId() != null)
                {
                    TraceOrgUserRole traceOrgUserRole = traceOrgUserRoleRepository.findOne(roleDTO.getId());

                    if (traceOrgUserRoleRepository.existsByRole(roleDTO.getRoleName()) && !traceOrgUserRole.getRole().equals(roleDTO.getRoleName()))
                    {
                        result.put(TraceOrgCommonConstants.SUCCESS, TraceOrgCommonConstants.FALSE);

                        result.put(TraceOrgCommonConstants.MESSAGE, TraceOrgMessageConstants.ROLE_ALREADY_EXIST);
                    }
                    else
                    {
                        if (traceOrgUserRole != null)
                        {
                            traceOrgUserRole.setRole(roleDTO.getRoleName());

                            traceOrgUserRole.setDescription(roleDTO.getDescription());

                            traceOrgUserRole = traceOrgUserRoleRepository.save(traceOrgUserRole);

                            List<TraceOrgRoleFeaturePermission> roleFeaturePermissions = traceOrgRoleFeaturePermissionRepository.findByRole(traceOrgUserRole.getId());

                            for (TraceOrgRoleFeaturePermission roleFeaturePermission : roleFeaturePermissions) {

                                for (TraceOrgPermissionDTO permissionDTO : roleDTO.getPermissions()) {

                                    if (roleFeaturePermission.getFeature().getName().equals(permissionDTO.getFeatureName())) {

                                        roleFeaturePermission.setReadPermission(permissionDTO.isRead());

                                        roleFeaturePermission.setWritePermission(permissionDTO.isWrite());

                                        traceOrgRoleFeaturePermissionRepository.save(roleFeaturePermission);
                                    }

                                }
                            }

                            List<TraceOrgUser> users = traceOrgUserRepository.findByUserRoleId_Id(traceOrgUserRole.getId());

                            this.traceOrgService.switchSafeUpdateMode(OFF);

                            for (TraceOrgUser traceOrgUser : users) {

                                Collection<OAuth2AccessToken> tokens = tokenStore.findTokensByClientIdAndUserName(TraceOrgCommonConstants.CLIENT_KEY, traceOrgUser.getUserName());

                                if (tokens != null)
                                {
                                    for (OAuth2AccessToken token : tokens)
                                    {
                                        tokenStore.removeAccessToken(token);

                                        if (token.getRefreshToken() != null)
                                        {
                                            tokenStore.removeRefreshToken(token.getRefreshToken());
                                        }
                                    }
                                }

                            }

                            this.traceOrgService.switchSafeUpdateMode(ON);

                            TraceOrgEvent traceOrgEvent = new TraceOrgEvent();

                            traceOrgEvent.setTimestamp(new Date());

                            traceOrgEvent.setDoneBy(traceOrgCommonUtil.currentUser(accessToken));

                            traceOrgEvent.setEventType("Update ROLE");

                            traceOrgEvent.setEventContext("Role " + traceOrgUserRole.getRole() + " Updated in IP Address Manager by " + traceOrgCommonUtil.currentUserName(accessToken));

                            traceOrgEvent.setSeverity(2);

                            traceOrgEventRepository.save(traceOrgEvent);

                            result.put(TraceOrgCommonConstants.SUCCESS, TraceOrgCommonConstants.TRUE);

                            result.put(TraceOrgCommonConstants.MESSAGE, TraceOrgMessageConstants.ROLE_UPDATED_SUCCESS);

                            result.put(TraceOrgCommonConstants.DATA, traceOrgUserRole);
                        }
                        else
                        {
                            result.put(TraceOrgCommonConstants.SUCCESS, TraceOrgCommonConstants.FALSE);

                            result.put(TraceOrgCommonConstants.MESSAGE, TraceOrgMessageConstants.ROLE_ID_WRONG);
                        }
                    }
                }
            }
            else
            {
                result.put(TraceOrgCommonConstants.SUCCESS, TraceOrgCommonConstants.FALSE);

                result.put(TraceOrgCommonConstants.MESSAGE, TraceOrgMessageConstants.ENTER_VALID_ROLE_NAME);
            }
        } catch (Exception exception) {
            _logger.error(exception);
        }
        return result;
    }

    @Override
    public HashMap<String, Object> removeRole(Long id, String accessToken)
    {
        HashMap<String, Object> result = new HashMap<>();

        try {
            List<TraceOrgUser> traceOrgUserList = traceOrgUserRepository.findByUserRoleId_Id(id);

            if (traceOrgUserList != null && !traceOrgUserList.isEmpty()) {
                result.put(TraceOrgCommonConstants.SUCCESS, TraceOrgCommonConstants.FALSE);

                result.put(TraceOrgCommonConstants.MESSAGE, TraceOrgMessageConstants.REMOVE_USERS);
            }
            else
            {
                TraceOrgUserRole role=traceOrgUserRoleRepository.findOne(id);

                traceOrgUserRoleRepository.delete(id);

                traceOrgRoleFeaturePermissionRepository.deleteByRole(id);

                TraceOrgEvent traceOrgEvent = new TraceOrgEvent();

                traceOrgEvent.setTimestamp(new Date());

                traceOrgEvent.setDoneBy(traceOrgCommonUtil.currentUser(accessToken));

                traceOrgEvent.setEventType("Delete ROLE");

                traceOrgEvent.setEventContext("Role " + role.getRole() + " Deleted in IP Address Manager by " + traceOrgCommonUtil.currentUserName(accessToken));

                traceOrgEvent.setSeverity(2);

                traceOrgEventRepository.save(traceOrgEvent);

                result.put(TraceOrgCommonConstants.SUCCESS, TraceOrgCommonConstants.TRUE);

                result.put(TraceOrgCommonConstants.MESSAGE, TraceOrgMessageConstants.ROLE_DELETE_SUCCESS);
            }
        }
        catch (Exception exception)
        {
            _logger.error(exception);
        }
        return result;
    }

    @Override
    public List<TraceOrgFeatureDTO> getAllFeatures()
    {
        return traceOrgFeatureRepository.findAll().stream()
                .map(feature -> new TraceOrgFeatureDTO(feature.getId(), feature.getName()))
                .collect(Collectors.toList());
    }
}
