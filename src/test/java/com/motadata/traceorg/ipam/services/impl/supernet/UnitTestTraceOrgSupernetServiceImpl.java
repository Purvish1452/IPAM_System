package com.motadata.traceorg.ipam.services.impl.supernet;

import com.motadata.traceorg.ipam.dto.supernet.TraceOrgSupernetDTO;
import com.motadata.traceorg.ipam.entity.dashboard.TraceOrgSupernetCategory;
import com.motadata.traceorg.ipam.entity.settings.TraceOrgUser;
import com.motadata.traceorg.ipam.entity.subnet.TraceOrgSubnetDetails;
import com.motadata.traceorg.ipam.entity.supernet.TraceOrgSupernetDetails;
import com.motadata.traceorg.ipam.repository.dashboard.TraceOrgSupernetCategoryRepository;
import com.motadata.traceorg.ipam.repository.event.TraceOrgEventRepository;
import com.motadata.traceorg.ipam.repository.subnet.TraceOrgSubnetDetailsRepository;
import com.motadata.traceorg.ipam.repository.supernet.TraceOrgSupernetDetailsRepository;
import com.motadata.traceorg.ipam.services.TraceOrgService;
import com.motadata.traceorg.ipam.util.TraceOrgCommonConstants;
import com.motadata.traceorg.ipam.util.TraceOrgCommonUtil;
import com.motadata.traceorg.ipam.util.TraceOrgConfigUtil;
import com.motadata.traceorg.ipam.util.TraceOrgMessageConstants;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static junit.framework.TestCase.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({TraceOrgConfigUtil.class, TraceOrgCommonUtil.class})
public class UnitTestTraceOrgSupernetServiceImpl
{
    @Mock
    private TraceOrgSubnetDetailsRepository traceOrgSubnetDetailsRepository;

    @Mock
    private TraceOrgSupernetDetailsRepository traceOrgSupernetDetailsRepository;

    @Mock
    private TraceOrgSupernetCategoryRepository traceOrgSupernetCategoryRepository;

    @Mock
    private TraceOrgCommonUtil traceOrgCommonUtil;

    @Mock
    private TraceOrgEventRepository traceOrgEventRepository;

    @Mock
    private TraceOrgService traceOrgService;

    @InjectMocks
    private TraceOrgSupernetServiceImpl traceOrgSupernetService;

    @Before
    public void setUp()
    {
        MockitoAnnotations.initMocks(this);

        PowerMockito.mockStatic(TraceOrgCommonUtil.class);

        PowerMockito.mockStatic(TraceOrgConfigUtil.class);
    }

    @Test
    public void addValidSupernetSuccess()
    {
        TraceOrgSupernetDTO dto = new TraceOrgSupernetDTO();
        dto.setNetworkAddress("192.168.0.0");
        dto.setNetworkMask("16");

        List<TraceOrgSubnetDetails> subnetDetailsList = new ArrayList<>();

        TraceOrgUser traceOrgUser = new TraceOrgUser();

        TraceOrgSubnetDetails traceOrgSubnetDetails = new TraceOrgSubnetDetails();

        traceOrgSubnetDetails.setId((long)1);

        traceOrgSubnetDetails.setSubnetCidr(24);

        traceOrgSubnetDetails.setSubnetAddress("192.168.8.0");

        subnetDetailsList.add(traceOrgSubnetDetails);

        TraceOrgSupernetCategory traceOrgSupernetCategory = new TraceOrgSupernetCategory();

        traceOrgSupernetCategory.setCategoryName(dto.getNetworkAddress()+"/"+dto.getNetworkMask());

        traceOrgSupernetCategory.setId((long)1);

        when(traceOrgCommonUtil.isIPv4Address(anyString())).thenReturn(true);

        when(traceOrgCommonUtil.currentUser(anyString())).thenReturn(traceOrgUser);

        when(traceOrgCommonUtil.currentUserName(anyString())).thenReturn("admin");

        when(traceOrgSupernetCategoryRepository.findByCategoryName(anyString())).thenReturn(null);

        when(traceOrgSupernetCategoryRepository.save(any(TraceOrgSupernetCategory.class))).thenReturn(traceOrgSupernetCategory);

        when(traceOrgSubnetDetailsRepository.findAll()).thenReturn(subnetDetailsList);

        HashMap<String, Object> result = traceOrgSupernetService.addSupernet("token", dto);

        assertTrue((Boolean) result.get("success"));

        assertEquals("Supernet is added Successfully", result.get("message"));
    }

    @Test
    public void addSupernetSupernetExist()
    {
        TraceOrgSupernetDTO dto = new TraceOrgSupernetDTO();
        dto.setNetworkAddress("192.168.0.0");
        dto.setNetworkMask("16");

        TraceOrgSupernetCategory traceOrgSupernetCategory = new TraceOrgSupernetCategory();

        traceOrgSupernetCategory.setCategoryName(dto.getNetworkAddress()+"/"+dto.getNetworkMask());

        traceOrgSupernetCategory.setId((long)1);

        when(traceOrgCommonUtil.isIPv4Address(anyString())).thenReturn(true);

        when(traceOrgSupernetCategoryRepository.findByCategoryName(anyString())).thenReturn(traceOrgSupernetCategory);

        HashMap<String, Object> result = traceOrgSupernetService.addSupernet("token", dto);

        assertFalse((Boolean) result.get("success"));

        assertEquals("Supernet is Exist", result.get("message"));
    }

    @Test
    public void addSupernetInvalidSupernetNetworkMask()
    {
        TraceOrgSupernetDTO dto = new TraceOrgSupernetDTO();
        dto.setNetworkAddress("192.168.0.0");
        dto.setNetworkMask("24");

        when(traceOrgCommonUtil.isIPv4Address(anyString())).thenReturn(true);
        when(traceOrgSupernetCategoryRepository.findByCategoryName(anyString())).thenReturn(null);

        HashMap<String, Object> result = traceOrgSupernetService.addSupernet("token", dto);

        assertFalse((Boolean) result.get("success"));
        assertEquals("Enter Supernet Mask between 8 to 23", result.get("message"));
    }

    @Test
    public void addSupernetInvalidSupernetIpAddress()
    {
        TraceOrgSupernetDTO dto = new TraceOrgSupernetDTO();
        dto.setNetworkAddress("192.168.1.10");
        dto.setNetworkMask("16");

        when(TraceOrgCommonUtil.isIPv4Address(anyString())).thenReturn(true);
        when(traceOrgSupernetCategoryRepository.findByCategoryName(anyString())).thenReturn(null);
        when(traceOrgCommonUtil.convertIpAddressToInterger(anyString())).thenReturn(0);

        HashMap<String, Object> result = traceOrgSupernetService.addSupernet("token", dto);

        assertFalse((Boolean) result.get("success"));
        assertEquals("Valid Supernet IP Address with Entered CIDR is 192.168.0.0", result.get(TraceOrgCommonConstants.MESSAGE));
    }

    @Test
    public void getSupernetDetails_validToken_success()
    {
        List<TraceOrgSupernetCategory> traceOrgSupernetCategoryList = new ArrayList<>();

        TraceOrgSupernetCategory traceOrgSupernetCategory = new TraceOrgSupernetCategory();

        traceOrgSupernetCategory.setCategoryName("192.168.0.0/16");

        traceOrgSupernetCategory.setId((long)1);

        traceOrgSupernetCategoryList.add(traceOrgSupernetCategory);

        List<TraceOrgSubnetDetails> subnetDetailsList = new ArrayList<>();

        TraceOrgSubnetDetails traceOrgSubnetDetails = new TraceOrgSubnetDetails();

        traceOrgSubnetDetails.setId((long)1);

        traceOrgSubnetDetails.setSubnetCidr(24);

        traceOrgSubnetDetails.setSubnetAddress("192.168.8.0");

        subnetDetailsList.add(traceOrgSubnetDetails);

        List<TraceOrgSupernetDetails> traceOrgSupernetDetailsList = new ArrayList<>();

        TraceOrgSupernetDetails traceOrgSupernetDetails = new TraceOrgSupernetDetails();

        traceOrgSupernetDetails.setId((long)1);

        traceOrgSupernetDetails.setSubnetId("1");

        traceOrgSupernetDetails.setTraceOrgSupernetCategory(traceOrgSupernetCategory);

        traceOrgSupernetDetailsList.add(traceOrgSupernetDetails);

        when(traceOrgSupernetCategoryRepository.findAll()).thenReturn(traceOrgSupernetCategoryList);

        when(traceOrgSubnetDetailsRepository.findAll()).thenReturn(subnetDetailsList);

        when(traceOrgSupernetDetailsRepository.findAll()).thenReturn(traceOrgSupernetDetailsList);

        HashMap<String, Object> resultData = traceOrgSupernetService.getSupernetDetails("token");

        assertEquals(resultData.get(TraceOrgCommonConstants.SUCCESS), TraceOrgCommonConstants.TRUE);
    }

    @Test
    public void getSupernetDetails_validToken_success_case2()
    {
        List<TraceOrgSupernetCategory> traceOrgSupernetCategoryList = new ArrayList<>();

        TraceOrgSupernetCategory traceOrgSupernetCategory = new TraceOrgSupernetCategory();

        traceOrgSupernetCategory.setCategoryName("192.168.0.0/16");

        traceOrgSupernetCategory.setId((long)1);

        traceOrgSupernetCategoryList.add(traceOrgSupernetCategory);

        List<TraceOrgSubnetDetails> subnetDetailsList = new ArrayList<>();

        TraceOrgSubnetDetails traceOrgSubnetDetails = new TraceOrgSubnetDetails();

        traceOrgSubnetDetails.setId((long)1);

        traceOrgSubnetDetails.setSubnetCidr(24);

        traceOrgSubnetDetails.setSubnetAddress("192.168.8.0");

        when(traceOrgSupernetCategoryRepository.findAll()).thenReturn(traceOrgSupernetCategoryList);

        when(traceOrgSubnetDetailsRepository.findAll()).thenReturn(subnetDetailsList);

        HashMap<String, Object> resultData = traceOrgSupernetService.getSupernetDetails("token");

        assertEquals(resultData.get(TraceOrgCommonConstants.SUCCESS), TraceOrgCommonConstants.TRUE);
    }

    @Test
    public void getSupernetDetailsWithInvalidTokenFailureCas()
    {
        HashMap<String, Object> resultData = traceOrgSupernetService.getSupernetDetails(null);

        assertEquals(resultData.get(TraceOrgCommonConstants.SUCCESS), TraceOrgCommonConstants.FALSE);

        assertEquals(resultData.get(TraceOrgCommonConstants.MESSAGE), TraceOrgMessageConstants.TOKEN_NULL);
    }

    @Test
    public void removeSupernet_validSupernet_success()
    {
        Long id = 1L;

        TraceOrgUser traceOrgUser = new TraceOrgUser();

        TraceOrgSupernetCategory traceOrgSupernetCategory = new TraceOrgSupernetCategory();

        traceOrgSupernetCategory.setId((long)1);

        traceOrgSupernetCategory.setCategoryName("192.168.0.0/16");

        when(traceOrgSupernetCategoryRepository.exists(anyLong())).thenReturn(true);

        when(traceOrgSupernetCategoryRepository.findOne(id)).thenReturn(traceOrgSupernetCategory);

        when(traceOrgCommonUtil.currentUserName(anyString())).thenReturn("admin");

        when(traceOrgCommonUtil.currentUser(anyString())).thenReturn(traceOrgUser);

        HashMap<String, Object> resultData = traceOrgSupernetService.removeSupernet("token",id);

        assertEquals(resultData.get(TraceOrgCommonConstants.SUCCESS), TraceOrgCommonConstants.TRUE);

        assertEquals(resultData.get(TraceOrgCommonConstants.MESSAGE), TraceOrgMessageConstants.SUPERNET_DELETE_SUCCESS);
    }

    @Test
    public void removeSupernet_invalidSupernet_failure()
    {
        Long id = 1L;

        when(traceOrgSupernetCategoryRepository.exists(anyLong())).thenReturn(false);

        HashMap<String, Object> resultData = traceOrgSupernetService.removeSupernet("token",id);

        assertEquals(resultData.get(TraceOrgCommonConstants.SUCCESS), TraceOrgCommonConstants.FALSE);

        assertEquals(resultData.get(TraceOrgCommonConstants.MESSAGE), TraceOrgMessageConstants.SUPERNET_NOT_EXIST);
    }

    @Test
    public void insertSubnetInSupernetCategory_validSubnet_success()
    {
        String subnetAddress = "192.168.1.0";

        Integer subnetCidr = 24;

        Long subnetId = 1L;

        TraceOrgUser doneBy = new TraceOrgUser();

        String eventBy = "admin";

        List<TraceOrgSupernetCategory> traceOrgSupernetCategoryList = new ArrayList<>();

        TraceOrgSupernetCategory traceOrgSupernetCategory = new TraceOrgSupernetCategory();

        traceOrgSupernetCategory.setCategoryName("192.168.0.0/16");

        traceOrgSupernetCategory.setId((long)1);

        traceOrgSupernetCategoryList.add(traceOrgSupernetCategory);

        when(traceOrgSupernetCategoryRepository.findAll()).thenReturn(traceOrgSupernetCategoryList);

        traceOrgSupernetService.insertSubnetInSupernetCategory(subnetAddress, subnetCidr, subnetId, doneBy, eventBy);

        verify(traceOrgSupernetDetailsRepository, times(1)).save(any(TraceOrgSupernetDetails.class));
    }

    @Test
    public void insertSubnetInSupernetCategory_existingSubnet_noAction()
    {
        String subnetAddress = "192.168.1.0";

        Integer subnetCidr = 24;

        Long subnetId = 1L;

        TraceOrgUser doneBy = new TraceOrgUser();

        String eventBy = "admin";

        traceOrgSupernetService.insertSubnetInSupernetCategory(subnetAddress, subnetCidr, subnetId, doneBy, eventBy);

        verify(traceOrgSupernetDetailsRepository, times(0)).save(any(TraceOrgSupernetDetails.class));
    }

    @Test
    public void removeSubnetFromSupernetDetails_validSubnet_success()
    {
        Long subnetId = 1L;

        TraceOrgSupernetDetails supernetDetails = new TraceOrgSupernetDetails();

        supernetDetails.setId(1L);

        supernetDetails.setSubnetId("1");

        when(TraceOrgCommonUtil.getStringValue(subnetId)).thenReturn("1");

        when(traceOrgSupernetDetailsRepository.findBySubnetId(anyString())).thenReturn(supernetDetails);

        traceOrgSupernetService.removeSubnetFromSupernetDetails(subnetId);

        verify(traceOrgSupernetDetailsRepository, times(1)).delete(anyLong());
    }

    @Test
    public void removeSubnetFromSupernetDetails_invalidSubnet_noAction()
    {
        Long subnetId = 1L;

        when(traceOrgSupernetDetailsRepository.findBySubnetId(anyString())).thenReturn(null);

        traceOrgSupernetService.removeSubnetFromSupernetDetails(subnetId);

        verify(traceOrgSupernetDetailsRepository, times(0)).delete(anyLong());
    }
}
