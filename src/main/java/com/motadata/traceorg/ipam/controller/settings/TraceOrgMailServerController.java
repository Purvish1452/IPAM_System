package com.motadata.traceorg.ipam.controller.settings;

import com.motadata.traceorg.ipam.entity.Response;
import com.motadata.traceorg.ipam.entity.settings.TraceOrgMailServer;
import com.motadata.traceorg.ipam.services.settings.TraceOrgMailServerService;
import com.motadata.traceorg.ipam.util.TraceOrgCommonConstants;
import com.motadata.traceorg.ipam.util.TraceOrgCommonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@SuppressWarnings("ALL")
@RestController
public class TraceOrgMailServerController
{
    @Autowired
    private TraceOrgCommonUtil traceOrgCommonUtil;

    @Autowired
    private TraceOrgMailServerService traceOrgMailServerService;

    @RequestMapping(value = TraceOrgCommonConstants.MAIL_REST_URL, method = RequestMethod.GET)
    @PreAuthorize("hasAuthority('PERM_SETTINGS_READ')")
    public ResponseEntity<?> listAllMailServer(HttpServletRequest request)
    {
        Response response = new Response();

        traceOrgCommonUtil.buildResponse(traceOrgMailServerService.listAllMailServer(), response);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    @RequestMapping(value = TraceOrgCommonConstants.MAIL_REST_URL + "{id}", method = RequestMethod.GET)
    @PreAuthorize("hasAuthority('PERM_SETTINGS_READ')")
    public ResponseEntity<?> getMailServer(@PathVariable(TraceOrgCommonConstants.ID) Long id, HttpServletRequest request)
    {
        Response response = new Response();

        traceOrgCommonUtil.buildResponse(traceOrgMailServerService.getMailServer(id), response);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @RequestMapping(value = TraceOrgCommonConstants.MAIL_REST_URL + "{id}", method = RequestMethod.PUT)
    @PreAuthorize("hasAuthority('PERM_SETTINGS_WRITE')")
    public ResponseEntity<?> updateMailServer(@PathVariable Long id, HttpServletRequest request, @RequestBody TraceOrgMailServer traceOrgMailServer)
    {
        Response response = new Response();

        traceOrgCommonUtil.buildResponse(traceOrgMailServerService.updateMailServer(id, traceOrgMailServer), response);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    @RequestMapping(value = TraceOrgCommonConstants.INSERT_MAIL_REST_URL, method = RequestMethod.POST)
    @PreAuthorize("hasAuthority('PERM_REPORTS_WRITE')")
    public ResponseEntity<?> insertMailServer(HttpServletRequest request, @RequestParam String mailToEmail)
    {
        Response response = new Response();

        traceOrgCommonUtil.buildResponse(traceOrgMailServerService.insertMailServer(mailToEmail), response);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    @RequestMapping(value = TraceOrgCommonConstants.MAIL_REST_URL, method = RequestMethod.POST)
    @PreAuthorize("hasAuthority('PERM_SETTINGS_WRITE')")
    public ResponseEntity<?> testMailServer(HttpServletRequest request, @RequestBody TraceOrgMailServer traceOrgMailServer)
    {
        Response response = new Response();

        traceOrgCommonUtil.buildResponse(traceOrgMailServerService.testMailServer(traceOrgMailServer), response);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
