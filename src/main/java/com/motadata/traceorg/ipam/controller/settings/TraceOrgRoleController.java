package com.motadata.traceorg.ipam.controller.settings;


import com.motadata.traceorg.ipam.dto.settings.TraceOrgFeatureDTO;
import com.motadata.traceorg.ipam.dto.settings.TraceOrgRoleDTO;
import com.motadata.traceorg.ipam.entity.Response;

import com.motadata.traceorg.ipam.services.settings.TraceOrgUserRoleService;
import com.motadata.traceorg.ipam.util.TraceOrgCommonConstants;
import com.motadata.traceorg.ipam.util.TraceOrgCommonUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;


/**
 * IPAM-147
 * IPAM Roadmap : Admin should be able to create Users and should be able to give specific role based access rights to specific user.
 * Added permission based access control
 */
@RestController
@RequestMapping(TraceOrgCommonConstants.USER_ROLE_REST_URL)
public class TraceOrgRoleController {

    private TraceOrgUserRoleService traceOrgUserRoleService;

    private TraceOrgCommonUtil traceOrgCommonUtil;

    public TraceOrgRoleController(TraceOrgUserRoleService traceOrgUserRoleService, TraceOrgCommonUtil traceOrgCommonUtil) {
        this.traceOrgUserRoleService = traceOrgUserRoleService;
        this.traceOrgCommonUtil = traceOrgCommonUtil;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('PERM_SETTINGS_WRITE')")
    public ResponseEntity<?> createRole(HttpServletRequest request,@RequestBody TraceOrgRoleDTO roleDTO)
    {
        Response response = new Response();

        traceOrgCommonUtil.buildResponse(traceOrgUserRoleService.createRole(roleDTO, request.getHeader(TraceOrgCommonConstants.ACCESSTOKEN)), response);

        return new ResponseEntity<>(response, HttpStatus.OK);

    }

    @GetMapping("{id}")
    @PreAuthorize("hasAuthority('PERM_SETTINGS_READ')")
    public ResponseEntity<?> getRole(@PathVariable(TraceOrgCommonConstants.ID) Long id)
    {
        Response response = new Response();

        traceOrgCommonUtil.buildResponse(traceOrgUserRoleService.getRole(id), response);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PutMapping
    @PreAuthorize("hasAuthority('PERM_SETTINGS_WRITE')")
    public ResponseEntity<?> updateRole(HttpServletRequest request,@RequestBody TraceOrgRoleDTO roleDTO) {

        Response response = new Response();

        traceOrgCommonUtil.buildResponse(traceOrgUserRoleService.updateRole(roleDTO, request.getHeader(TraceOrgCommonConstants.ACCESSTOKEN)), response);

        return new ResponseEntity<>(response, HttpStatus.OK);

    }

    @DeleteMapping("{id}")
    @PreAuthorize("hasAuthority('PERM_SETTINGS_WRITE')")
    public ResponseEntity<?> removeRole(@PathVariable(TraceOrgCommonConstants.ID) Long id,HttpServletRequest request)
    {
        Response response = new Response();

        traceOrgCommonUtil.buildResponse(traceOrgUserRoleService.removeRole(id,request.getHeader(TraceOrgCommonConstants.ACCESSTOKEN)), response);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("feature/")
    public List<TraceOrgFeatureDTO> getAllFeatures()
    {
        return traceOrgUserRoleService.getAllFeatures();
    }
}
