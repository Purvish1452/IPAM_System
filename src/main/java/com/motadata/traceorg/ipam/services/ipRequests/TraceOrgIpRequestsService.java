package com.motadata.traceorg.ipam.services.ipRequests;

import com.motadata.traceorg.ipam.dto.ipRequests.TraceOrgApproveIpRequestDTO;
import com.motadata.traceorg.ipam.dto.ipRequests.TraceOrgRejectIpRequestDTO;
import com.motadata.traceorg.ipam.entity.ipRequests.TraceOrgIpRequests;

import java.util.HashMap;

public interface TraceOrgIpRequestsService
{
    HashMap<String, Object> addIpRequests(TraceOrgIpRequests traceOrgIpRequets);

    HashMap<String, Object> listAllIpRequests();

    HashMap<String, Object> getIpRequest(Long id);

    HashMap<String, Object> ipRequestApproved(TraceOrgApproveIpRequestDTO id);

    HashMap<String, Object> ipRequestRejected(TraceOrgRejectIpRequestDTO id);
}
