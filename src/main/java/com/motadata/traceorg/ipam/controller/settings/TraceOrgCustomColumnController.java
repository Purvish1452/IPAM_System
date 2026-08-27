package com.motadata.traceorg.ipam.controller.settings;

import com.motadata.traceorg.ipam.entity.Response;
import com.motadata.traceorg.ipam.entity.settings.TraceOrgCustomColumn;
import com.motadata.traceorg.ipam.services.settings.TraceOrgCustomColumnService;
import com.motadata.traceorg.ipam.util.TraceOrgCommonConstants;
import com.motadata.traceorg.ipam.util.TraceOrgCommonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * IPAM-160 IPAM Roadmap : The solution must be flexible to allow the creation of custom fields for objects in IPAM. This must be configurable via the Web GUI.
 * Added custom column feature.
 */
@RestController
@RequestMapping(TraceOrgCommonConstants.CUSTOM_COLUMN_REST_URL)
public class TraceOrgCustomColumnController {

    @Autowired
    TraceOrgCustomColumnService traceOrgCustomColumnService;

    @Autowired
    TraceOrgCommonUtil traceOrgCommonUtil;

    @PostMapping
    @PreAuthorize("hasAuthority('PERM_SETTINGS_WRITE')")
    public ResponseEntity<?> createCustomColumn(HttpServletRequest request, @RequestBody TraceOrgCustomColumn customColumn)
    {
        Response response = new Response();

        traceOrgCommonUtil.buildResponse(traceOrgCustomColumnService.createCustomColumn(customColumn, request.getHeader(TraceOrgCommonConstants.ACCESSTOKEN)), response);

        return new ResponseEntity<>(response, HttpStatus.OK);

    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('PERM_SETTINGS_READ', 'PERM_DASHBOARD_READ')")
    public ResponseEntity<?> listAllCustomColumn()
    {
        Response response = new Response();

        traceOrgCommonUtil.buildResponse(traceOrgCustomColumnService.listAllCustomColumn(), response);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    @DeleteMapping("{id}")
    @PreAuthorize("hasAuthority('PERM_SETTINGS_WRITE')")
    public ResponseEntity<?> removeCustomColumn(@PathVariable(TraceOrgCommonConstants.ID) Long id)
    {
        Response response = new Response();

        traceOrgCommonUtil.buildResponse(traceOrgCustomColumnService.removeCustomColumn(id), response);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/download")
    @PreAuthorize("hasAnyAuthority('PERM_SETTINGS_READ', 'PERM_DASHBOARD_READ')")
    public ResponseEntity<?> downloadCsv()
    {
        Response response = new Response();

        String url = traceOrgCustomColumnService.generateCsv();

        response.setSuccess(TraceOrgCommonConstants.TRUE);

        response.setData(url);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
