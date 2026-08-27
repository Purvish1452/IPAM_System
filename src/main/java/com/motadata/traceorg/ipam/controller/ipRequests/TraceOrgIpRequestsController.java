package com.motadata.traceorg.ipam.controller.ipRequests;


import com.motadata.traceorg.ipam.dto.ipRequests.TraceOrgApproveIpRequestDTO;
import com.motadata.traceorg.ipam.dto.ipRequests.TraceOrgRejectIpRequestDTO;
import com.motadata.traceorg.ipam.entity.Response;
import com.motadata.traceorg.ipam.entity.ipRequests.TraceOrgIpRequests;
import com.motadata.traceorg.ipam.services.ipRequests.TraceOrgIpRequestsService;
import com.motadata.traceorg.ipam.util.TraceOrgCommonConstants;
import com.motadata.traceorg.ipam.util.TraceOrgCommonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * IPAM-159 IPAM Roadmap : Streamline IP address request creation and management with the IP Request tool
 */
@RestController
@RequestMapping(TraceOrgCommonConstants.IP_REQUESTS_REST_URL)
public class TraceOrgIpRequestsController {

    @Autowired
    private TraceOrgIpRequestsService traceOrgIpRequestsService;

    @Autowired
    private TraceOrgCommonUtil traceOrgCommonUtil;

    @PostMapping
    @PreAuthorize("hasAuthority('PERM_IP REQUESTS_WRITE')")
    public ResponseEntity<?> createIpRequest(@RequestBody TraceOrgIpRequests traceOrgIpRequets) {

        Response response = new Response();

        traceOrgCommonUtil.buildResponse(traceOrgIpRequestsService.addIpRequests(traceOrgIpRequets), response);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('PERM_IP REQUESTS_READ')")
    public ResponseEntity<?> listAllIpRequests(HttpServletRequest request)
    {
        Response response = new Response();

        traceOrgCommonUtil.buildResponse(traceOrgIpRequestsService.listAllIpRequests(), response);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("{id}")
    @PreAuthorize("hasAuthority('PERM_IP REQUESTS_READ')")
    public ResponseEntity<?> getIpRequest(@PathVariable(TraceOrgCommonConstants.ID) Long id)
    {
        Response response = new Response();

        traceOrgCommonUtil.buildResponse(traceOrgIpRequestsService.getIpRequest(id), response);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("approved")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> ipRequestApproved(@RequestBody TraceOrgApproveIpRequestDTO traceOrgApproveIpRequestDTO) {

        Response response = new Response();

        traceOrgCommonUtil.buildResponse(traceOrgIpRequestsService.ipRequestApproved(traceOrgApproveIpRequestDTO), response);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("rejected")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> ipRequestRejected(@RequestBody TraceOrgRejectIpRequestDTO traceOrgRejectIpRequestDTO) {

        Response response = new Response();

        traceOrgCommonUtil.buildResponse(traceOrgIpRequestsService.ipRequestRejected(traceOrgRejectIpRequestDTO), response);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
