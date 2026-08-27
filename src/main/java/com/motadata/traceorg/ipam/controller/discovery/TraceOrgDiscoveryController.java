package com.motadata.traceorg.ipam.controller.discovery;

import com.motadata.traceorg.ipam.entity.Response;
import com.motadata.traceorg.ipam.services.discovery.TraceOrgDiscoveryService;
import com.motadata.traceorg.ipam.util.TraceOrgCommonConstants;
import com.motadata.traceorg.ipam.util.TraceOrgCommonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@SuppressWarnings({"ALL"})
@RestController
public class TraceOrgDiscoveryController
{
    @Autowired
    private TraceOrgCommonUtil traceOrgCommonUtil;

    @Autowired
    private TraceOrgDiscoveryService traceOrgDiscoveryService;

    @RequestMapping(value = TraceOrgCommonConstants.DISCOVERED_SUBNET_URL, method = RequestMethod.GET)
    @PreAuthorize("hasAuthority('PERM_SETTINGS_READ')")
    public ResponseEntity<?> getDiscoveredSubnets(HttpServletRequest request)
    {
        Response response = new Response();

        traceOrgCommonUtil.buildResponse(traceOrgDiscoveryService.getDiscoveredSubnets(), response);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @RequestMapping(value = TraceOrgCommonConstants.DISCOVERED_SUBNET_URL + "{id}", method = RequestMethod.DELETE)
    @PreAuthorize("hasAuthority('PERM_SETTINGS_WRITE')")
    public ResponseEntity<?> deleteDiscoveredSubnet(@PathVariable(TraceOrgCommonConstants.ID) Integer id, HttpServletRequest request)
    {
        Response response = new Response();

        traceOrgCommonUtil.buildResponse(traceOrgDiscoveryService.deleteDiscoveredSubnet(id), response);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @RequestMapping(value = TraceOrgCommonConstants.DISCOVERED_SUBNET_URL + "{id}", method = RequestMethod.GET)
    @PreAuthorize("hasAuthority('PERM_SETTINGS_READ')")
    public ResponseEntity<?> getDiscoveredSubnet(@PathVariable(TraceOrgCommonConstants.ID) Integer id, HttpServletRequest request)
    {
        Response response = new Response();

        traceOrgCommonUtil.buildResponse(traceOrgDiscoveryService.getDiscoveredSubnet(id), response);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @RequestMapping(value = TraceOrgCommonConstants.SCAN_GATEWAY_URL + "{id}", method = RequestMethod.POST)
    @PreAuthorize("hasAuthority('PERM_SETTINGS_WRITE')")
    public ResponseEntity<?> scanGateway(@PathVariable(TraceOrgCommonConstants.ID) Long id, HttpServletRequest request)
    {
        Response response = new Response();

        traceOrgCommonUtil.buildResponse(traceOrgDiscoveryService.scanGateway(id), response);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @RequestMapping(value = TraceOrgCommonConstants.STATUS_SCAN_GATEWAY, method = RequestMethod.GET)
    @PreAuthorize("hasAuthority('PERM_SETTINGS_READ')")
    public ResponseEntity<?> statusScanGateway(HttpServletRequest request)
    {
        Response response = new Response();

        traceOrgCommonUtil.buildResponse(traceOrgDiscoveryService.statusScanGateway(), response);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}