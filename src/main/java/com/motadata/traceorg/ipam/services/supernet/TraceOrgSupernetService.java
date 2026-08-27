package com.motadata.traceorg.ipam.services.supernet;

import com.motadata.traceorg.ipam.dto.supernet.TraceOrgSupernetDTO;
import com.motadata.traceorg.ipam.entity.settings.TraceOrgUser;
import java.util.HashMap;

public interface TraceOrgSupernetService
{
    HashMap<String,Object> addSupernet(String accessToken, TraceOrgSupernetDTO traceOrgSupernetDTO);

    HashMap<String,Object> getSupernetDetails(String accessToken);

    HashMap<String,Object> removeSupernet(String accessToken, Long id);

    void insertSubnetInSupernetCategory(String subnetAddress, Integer subnetCidr, Long subnetId, TraceOrgUser doneBy, String eventBy);

    void removeSubnetFromSupernetDetails(Long subnetId);

}
