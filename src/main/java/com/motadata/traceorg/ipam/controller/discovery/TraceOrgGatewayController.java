package com.motadata.traceorg.ipam.controller.discovery;

import com.motadata.traceorg.ipam.entity.*;
import com.motadata.traceorg.ipam.entity.discovery.TraceOrgGateway;
import com.motadata.traceorg.ipam.services.discovery.TraceOrgGatewayService;
import com.motadata.traceorg.ipam.util.TraceOrgCommonConstants;
import com.motadata.traceorg.ipam.util.TraceOrgCommonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;


@SuppressWarnings({"unchecked","SpringAutowiredFieldsWarningInspection"})
@RestController
public class TraceOrgGatewayController {

    @Autowired
    private TraceOrgCommonUtil traceOrgCommonUtil;

    @Autowired
    private TraceOrgGatewayService traceOrgGatewayService;

    @RequestMapping(value = TraceOrgCommonConstants.GATEWAY_REST_URL, method = RequestMethod.GET)
    @PreAuthorize("hasAnyAuthority('PERM_SETTINGS_READ', 'PERM_DASHBOARD_READ')")
    public ResponseEntity<?> listGateway(HttpServletRequest request)
    {
        Response response = new Response();

        traceOrgCommonUtil.buildResponse(traceOrgGatewayService.listGateway(), response);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @RequestMapping(value = TraceOrgCommonConstants.GATEWAY_URL, method = RequestMethod.GET)
    @PreAuthorize("hasAnyAuthority('PERM_SETTINGS_READ', 'PERM_DASHBOARD_READ')")
    public ResponseEntity<?> getGateways(HttpServletRequest request)
    {
        Response response = new Response();

        traceOrgCommonUtil.buildResponse(traceOrgGatewayService.getGateways(), response);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @RequestMapping(value = TraceOrgCommonConstants.GATEWAY_REST_URL + "{gatewayId}", method = RequestMethod.GET)
    @PreAuthorize("hasAnyAuthority('PERM_SETTINGS_READ', 'PERM_DASHBOARD_READ')")
    public ResponseEntity<?> getGateway(HttpServletRequest request,  @PathVariable Long gatewayId)
    {
        Response response = new Response();

        traceOrgCommonUtil.buildResponse(traceOrgGatewayService.getGateway(gatewayId), response);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @RequestMapping(value = TraceOrgCommonConstants.GATEWAY_REST_URL, method = RequestMethod.POST)
    @PreAuthorize("hasAnyAuthority('PERM_SETTINGS_WRITE', 'PERM_DASHBOARD_WRITE')")
    public ResponseEntity<?> addGateway(HttpServletRequest request, @RequestBody TraceOrgGateway traceOrgGateway)
    {
        Response response = new Response();

        traceOrgCommonUtil.buildResponse(traceOrgGatewayService.addGateway(traceOrgGateway), response);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @RequestMapping(value = TraceOrgCommonConstants.GATEWAY_REST_URL + "{gatewayId}", method = RequestMethod.PUT)
    @PreAuthorize("hasAnyAuthority('PERM_SETTINGS_WRITE', 'PERM_DASHBOARD_WRITE')")
    public ResponseEntity<?> updateGateway(HttpServletRequest request, @PathVariable Long gatewayId, @RequestBody TraceOrgGateway traceOrgGateway)
    {
        Response response = new Response();

        traceOrgCommonUtil.buildResponse(traceOrgGatewayService.updateGateway(gatewayId, traceOrgGateway), response);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @RequestMapping(value = TraceOrgCommonConstants.GATEWAY_REST_URL + "{gatewayId}", method = RequestMethod.DELETE)
    @PreAuthorize("hasAnyAuthority('PERM_SETTINGS_WRITE', 'PERM_DASHBOARD_WRITE')")
    public ResponseEntity<?> removeGateway(@PathVariable Long gatewayId, HttpServletRequest request)
    {
        Response response = new Response();

        traceOrgCommonUtil.buildResponse(traceOrgGatewayService.removeGateway(gatewayId), response);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
