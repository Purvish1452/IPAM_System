package com.motadata.traceorg.ipam.services.impl.settings;

import com.motadata.traceorg.ipam.dto.settings.TraceOrgFeatureDTO;
import com.motadata.traceorg.ipam.dto.settings.TraceOrgPermissionDTO;
import com.motadata.traceorg.ipam.dto.settings.TraceOrgRoleDTO;
import com.motadata.traceorg.ipam.entity.event.TraceOrgEvent;
import com.motadata.traceorg.ipam.entity.settings.TraceOrgFeature;
import com.motadata.traceorg.ipam.entity.settings.TraceOrgRoleFeaturePermission;
import com.motadata.traceorg.ipam.entity.settings.TraceOrgUser;
import com.motadata.traceorg.ipam.entity.settings.TraceOrgUserRole;
import com.motadata.traceorg.ipam.repository.event.TraceOrgEventRepository;
import com.motadata.traceorg.ipam.repository.settings.TraceOrgFeatureRepository;
import com.motadata.traceorg.ipam.repository.settings.TraceOrgRoleFeaturePermissionRepository;
import com.motadata.traceorg.ipam.repository.settings.TraceOrgUserRepository;
import com.motadata.traceorg.ipam.repository.settings.TraceOrgUserRoleRepository;
import com.motadata.traceorg.ipam.services.TraceOrgService;
import com.motadata.traceorg.ipam.util.TraceOrgCommonConstants;
import com.motadata.traceorg.ipam.util.TraceOrgCommonUtil;
import com.motadata.traceorg.ipam.util.TraceOrgMessageConstants;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.token.TokenStore;


import static org.junit.Assert.*;
import static org.mockito.Mockito.*;



import java.util.*;

public class UnitTestTraceOrgUserRoleServiceImplTest {

    @Mock
    private TraceOrgUserRoleRepository traceOrgUserRoleRepository;

    @Mock
    private TraceOrgFeatureRepository traceOrgFeatureRepository;

    @Mock
    private TraceOrgRoleFeaturePermissionRepository traceOrgRoleFeaturePermissionRepository;

    @Mock
    private TraceOrgEventRepository traceOrgEventRepository;

    @Mock
    private TraceOrgCommonUtil traceOrgCommonUtil;

    @Mock
    private TraceOrgUserRepository traceOrgUserRepository;

    @InjectMocks
    private TraceOrgUserRoleServiceImpl traceOrgUserRoleService;

    @Mock
    private TokenStore tokenStore;

    @Mock
    TraceOrgService traceOrgService;

    private TraceOrgRoleDTO roleDTO;

    private TraceOrgUserRole traceOrgUserRole;

    private TraceOrgPermissionDTO permissionDTO;

    private TraceOrgFeature feature;

    private TraceOrgRoleFeaturePermission roleFeaturePermission;

    private TraceOrgUser traceOrgUser;

    private OAuth2AccessToken mockToken;

    private String accessToken = "mockAccessToken";

    private TraceOrgFeature feature1;

    private TraceOrgFeature feature2;

    @Before
    public void setUp()
    {
        traceOrgUserRoleService = new TraceOrgUserRoleServiceImpl(traceOrgFeatureRepository, traceOrgUserRoleRepository, traceOrgRoleFeaturePermissionRepository, traceOrgEventRepository, traceOrgCommonUtil, traceOrgUserRepository,traceOrgService);

        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testCreateRole()
    {
        roleDTO = new TraceOrgRoleDTO();

        roleDTO.setRoleName("Admin");

        roleDTO.setDescription("Administrator Role");

        permissionDTO = new TraceOrgPermissionDTO();

        permissionDTO.setFeatureName("Dashboard");

        permissionDTO.setRead(true);

        permissionDTO.setWrite(false);

        roleDTO.setPermissions(Collections.singletonList(permissionDTO));

        traceOrgUserRole = new TraceOrgUserRole();

        traceOrgUserRole.setId(1L);

        traceOrgUserRole.setRole("Admin");

        traceOrgUserRole.setDescription("Administrator Role");

        feature = new TraceOrgFeature();

        feature.setId(1L);

        feature.setName("Dashboard");

        roleFeaturePermission = new TraceOrgRoleFeaturePermission();

        roleFeaturePermission.setRole(1L);

        roleFeaturePermission.setFeature(feature);

        roleFeaturePermission.setReadPermission(true);

        roleFeaturePermission.setWritePermission(false);

        when(traceOrgUserRoleRepository.existsByRole(roleDTO.getRoleName())).thenReturn(false);

        when(traceOrgUserRoleRepository.save(any(TraceOrgUserRole.class))).thenReturn(traceOrgUserRole);

        when(traceOrgFeatureRepository.findByName(permissionDTO.getFeatureName())).thenReturn(feature);

        when(traceOrgCommonUtil.currentUserName(accessToken)).thenReturn("testUser");

        HashMap<String, Object> result = traceOrgUserRoleService.createRole(roleDTO, accessToken);

        verify(traceOrgUserRoleRepository, times(1)).save(any(TraceOrgUserRole.class));

        verify(traceOrgRoleFeaturePermissionRepository, times(1)).save(any(TraceOrgRoleFeaturePermission.class));

        verify(traceOrgEventRepository, times(1)).save(any(TraceOrgEvent.class));

        assertTrue((Boolean) result.get(TraceOrgCommonConstants.SUCCESS));

        assertEquals(TraceOrgMessageConstants.ROLE_ADD_SUCCESS, result.get(TraceOrgCommonConstants.MESSAGE));
    }

    @Test
    public void testCreateRoleAlreadyExists()
    {
        TraceOrgRoleDTO roleDTO = new TraceOrgRoleDTO();

        roleDTO.setRoleName("Admin");

        when(traceOrgUserRoleRepository.existsByRole(roleDTO.getRoleName())).thenReturn(true);

        HashMap<String, Object> result = traceOrgUserRoleService.createRole(roleDTO, "dummyToken");

        assertEquals(Boolean.FALSE, result.get("success"));
    }

    @Test
    public void testListAllRoles()
    {
        List<TraceOrgUserRole> roles = Arrays.asList(new TraceOrgUserRole(), new TraceOrgUserRole());

        when(traceOrgUserRoleRepository.findAll()).thenReturn(roles);

        HashMap<String, Object> result = traceOrgUserRoleService.listAllRoles();

        assertEquals(Boolean.TRUE, result.get("success"));

        assertEquals(roles, result.get("data"));
    }

    @Test
    public void testGetRoleSuccess()
    {
        TraceOrgUserRole role = new TraceOrgUserRole();

        role.setId(1L);

        when(traceOrgUserRoleRepository.findOne(1L)).thenReturn(role);

        HashMap<String, Object> result = traceOrgUserRoleService.getRole(1L);

        assertEquals(Boolean.TRUE, result.get("success"));

        assertEquals(role, result.get("data"));
    }

    @Test
    public void testGetRoleNotFound()
    {
        when(traceOrgUserRoleRepository.findOne(1L)).thenReturn(null);

        HashMap<String, Object> result = traceOrgUserRoleService.getRole(1L);

        assertEquals(Boolean.FALSE, result.get("success"));
    }

    @Test
    public void testUpdateRoleSuccess()
    {
        roleDTO = new TraceOrgRoleDTO();

        roleDTO.setId(1L);

        roleDTO.setRoleName("Admin");

        roleDTO.setDescription("Administrator Role");

        permissionDTO = new TraceOrgPermissionDTO();

        permissionDTO.setFeatureName("Dashboard");

        permissionDTO.setRead(true);

        permissionDTO.setWrite(false);

        roleDTO.setPermissions(Collections.singletonList(permissionDTO));

        traceOrgUserRole = new TraceOrgUserRole();

        traceOrgUserRole.setId(1L);

        traceOrgUserRole.setRole("Admin");

        traceOrgUserRole.setDescription("Administrator Role");

        feature = new TraceOrgFeature();

        feature.setId(1L);

        feature.setName("Dashboard");

        roleFeaturePermission = new TraceOrgRoleFeaturePermission();

        roleFeaturePermission.setRole(1L);

        roleFeaturePermission.setFeature(feature);

        roleFeaturePermission.setReadPermission(true);

        roleFeaturePermission.setWritePermission(false);

        traceOrgUser = new TraceOrgUser();

        traceOrgUser.setId(1L);

        traceOrgUser.setUserName("testUser");

        mockToken = mock(OAuth2AccessToken.class);

        when(traceOrgUserRoleRepository.findOne(roleDTO.getId())).thenReturn(traceOrgUserRole);

        when(traceOrgUserRoleRepository.save(any(TraceOrgUserRole.class))).thenReturn(traceOrgUserRole);

        when(traceOrgRoleFeaturePermissionRepository.findByRole(traceOrgUserRole.getId()))
                .thenReturn(Collections.singletonList(roleFeaturePermission));

        when(traceOrgCommonUtil.currentUserName(accessToken)).thenReturn("testUser");

        when(traceOrgUserRepository.findByUserRoleId_Id(traceOrgUserRole.getId()))
                .thenReturn(Collections.singletonList(traceOrgUser));

        when(tokenStore.findTokensByClientIdAndUserName(anyString(), anyString()))
                .thenReturn(Collections.singletonList(mockToken));

        HashMap<String, Object> result = traceOrgUserRoleService.updateRole(roleDTO, accessToken);

        verify(traceOrgUserRoleRepository, times(1)).save(any(TraceOrgUserRole.class));

        verify(traceOrgRoleFeaturePermissionRepository, times(1)).save(any(TraceOrgRoleFeaturePermission.class));

        verify(traceOrgEventRepository, times(1)).save(any(TraceOrgEvent.class));

        verify(tokenStore, times(1)).removeAccessToken(mockToken);

        assertTrue((Boolean) result.get(TraceOrgCommonConstants.SUCCESS));

        assertEquals(TraceOrgMessageConstants.ROLE_UPDATED_SUCCESS, result.get(TraceOrgCommonConstants.MESSAGE));
    }

    @Test
    public void testRemoveRoleSuccess()
    {
        when(traceOrgUserRepository.findByUserRoleId_Id(1L)).thenReturn(Collections.emptyList());

        when(traceOrgUserRoleRepository.findOne(1L)).thenReturn(new TraceOrgUserRole());

        HashMap<String, Object> result = traceOrgUserRoleService.removeRole(1L, accessToken);

        assertEquals(Boolean.TRUE, result.get("success"));

        verify(traceOrgUserRoleRepository, times(1)).delete(1L);
    }

    @Test
    public void testRemoveRoleHasUsers()
    {
        when(traceOrgUserRepository.findByUserRoleId_Id(1L)).thenReturn(Collections.singletonList(new TraceOrgUser()));

        HashMap<String, Object> result = traceOrgUserRoleService.removeRole(1L, accessToken);

        assertEquals(Boolean.FALSE, result.get("success"));
    }

    @Test
    public void testGetAllFeaturesEmptyList() {
        when(traceOrgFeatureRepository.findAll()).thenReturn(Collections.emptyList());

        List<TraceOrgFeatureDTO> featureDTOs = traceOrgUserRoleService.getAllFeatures();

        verify(traceOrgFeatureRepository, times(1)).findAll();

        assertNotNull(featureDTOs);

        assertTrue(featureDTOs.isEmpty());
    }

}
