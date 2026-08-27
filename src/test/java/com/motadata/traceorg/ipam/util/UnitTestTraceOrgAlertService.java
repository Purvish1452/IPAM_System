package com.motadata.traceorg.ipam.util;

import com.motadata.traceorg.ipam.entity.alert.TraceOrgAlertStream;
import com.motadata.traceorg.ipam.repository.alert.TraceOrgAlertStreamRepository;
import com.motadata.traceorg.ipam.services.TraceOrgService;
import com.motadata.traceorg.ipam.services.impl.alert.TraceOrgAlertServiceImpl;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
@PrepareForTest({  TraceOrgFactoryUtil.class, Runtime.class, TraceOrgService.class})
@PowerMockIgnore({"javax.manageme3nt.*"})
public class UnitTestTraceOrgAlertService
{

    @Mock
    private TraceOrgService traceOrgService;

    @InjectMocks
    private TraceOrgAlertServiceImpl traceOrgAlertServiceImpl;

    @Mock
    TraceOrgAlertStreamRepository traceOrgAlertStreamRepository;

    @Test
    public void testGetAlerts()
    {
        List<TraceOrgAlertStream> alertStreams = new ArrayList<>();

        TraceOrgAlertStream alertStream = new  TraceOrgAlertStream();

        alertStream.setSubnet("10.20.41.1");

        alertStream.setAlertType(TraceOrgCommonConstants.IP_UTILIZATION_ALERT_TYPE);

        alertStream.setMessage("Subnet 10.20.40.0 utilization has reached 53.91%, exceeding the threshold of 10% in the IP Address Manager.");

        alertStreams.add(alertStream);

        List<Object> count = new ArrayList<>();

        count.add(10);

        PageRequest pageable = new PageRequest(0, 10);

        when(traceOrgAlertStreamRepository.findByStatusOrderByTimestampDesc(any(),any())).thenReturn(new PageImpl<>(alertStreams, pageable, 1));

        when( traceOrgAlertStreamRepository.countByStatus(any())).thenReturn(1);

        HashMap<String, Object> result = traceOrgAlertServiceImpl.getAlerts("clear",1,20);

        Assert.assertTrue(result.get("data") != null);

        HashMap<String, Object> data= (HashMap<String, Object>) result.get("data");

        Assert.assertTrue(data.get("total") != null);
    }

    @Test
    public void testGetAlerts_1()
    {
        List<TraceOrgAlertStream> alertStreams = new ArrayList<>();

        TraceOrgAlertStream alertStream = new  TraceOrgAlertStream();

        alertStream.setSubnet("10.20.41.1");

        alertStream.setAlertType(TraceOrgCommonConstants.IP_UTILIZATION_ALERT_TYPE);

        alertStream.setMessage("Subnet 10.20.40.0 utilization has reached 53.91%, exceeding the threshold of 10% in the IP Address Manager.");

        alertStreams.add(alertStream);

        List<Object> count = new ArrayList<>();

        count.add(10);

        PageRequest pageable = new PageRequest(0, 10);

        when(traceOrgAlertStreamRepository.findByStatusOrderByTimestampDesc(any(),any())).thenReturn(new PageImpl<>(alertStreams, pageable, 1));

        when( traceOrgAlertStreamRepository.countByStatus(any())).thenReturn(1);

        HashMap<String, Object> result = traceOrgAlertServiceImpl.getAlerts("clear",1,20);

        Assert.assertTrue(result.get("data") != null);

        HashMap<String, Object> data1= (HashMap<String, Object>) result.get("data");

        Assert.assertTrue(data1.get("total") != null);
    }

}
