package com.motadata.traceorg.ipam.controller.settings;


import com.motadata.traceorg.ipam.entity.Response;
import com.motadata.traceorg.ipam.entity.settings.TraceOrgGlobalSetting;
import com.motadata.traceorg.ipam.services.settings.TraceOrgGlobalSettingService;
import com.motadata.traceorg.ipam.util.TraceOrgCommonConstants;
import com.motadata.traceorg.ipam.util.TraceOrgCommonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
public class TraceOrgGlobalSettingController
{
    @Autowired
    private TraceOrgCommonUtil traceOrgCommonUtil;

    @Autowired
    private TraceOrgGlobalSettingService traceOrgGlobalSettingService;

    @RequestMapping(value = TraceOrgCommonConstants.GLOBAL_SETTING_REST_URL, method = RequestMethod.GET)
    @PreAuthorize("hasAuthority('PERM_SETTINGS_READ')")
    public ResponseEntity<?> listAllGlobalSetting(HttpServletRequest request)
    {
        Response response = new Response();

        traceOrgCommonUtil.buildResponse(traceOrgGlobalSettingService.listAllGlobalSetting(), response);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    @RequestMapping(value = TraceOrgCommonConstants.GLOBAL_SETTING_REST_URL + "{id}", method = RequestMethod.PUT)
    @PreAuthorize("hasAuthority('PERM_SETTINGS_WRITE')")
    public ResponseEntity<?> updateGlobalSetting(@PathVariable Long id, HttpServletRequest request, @RequestBody TraceOrgGlobalSetting traceOrgGlobalSetting)
    {
        Response response = new Response();

        traceOrgCommonUtil.buildResponse(traceOrgGlobalSettingService.updateGlobalSetting(id, traceOrgGlobalSetting), response);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
