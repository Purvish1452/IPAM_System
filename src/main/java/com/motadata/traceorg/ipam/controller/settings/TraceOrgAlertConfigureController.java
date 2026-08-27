package com.motadata.traceorg.ipam.controller.settings;

import com.motadata.traceorg.ipam.entity.Response;
import com.motadata.traceorg.ipam.entity.settings.TraceOrgAlertConfigure;
import com.motadata.traceorg.ipam.services.settings.TraceOrgAlertConfigureService;
import com.motadata.traceorg.ipam.util.TraceOrgCommonConstants;
import com.motadata.traceorg.ipam.util.TraceOrgCommonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@SuppressWarnings({"unchecked","SpringAutowiredFieldsWarningInspection"})
@RestController
public class TraceOrgAlertConfigureController
{
    @Autowired
    private TraceOrgCommonUtil traceOrgCommonUtil;

    @Autowired
    private TraceOrgAlertConfigureService traceOrgAlertConfigureService;

    @RequestMapping(value = TraceOrgCommonConstants.ALERT_CONFIGURE_REST_URL, method = RequestMethod.PUT)
    @PreAuthorize("hasAuthority('PERM_SETTINGS_WRITE')")
    public ResponseEntity<?> updateAlertConfiguration(HttpServletRequest request, @RequestBody List<TraceOrgAlertConfigure> traceOrgAlertConfigures)
    {
        Response response = new Response();

        traceOrgCommonUtil.buildResponse(traceOrgAlertConfigureService.updateAlertConfiguration(traceOrgAlertConfigures), response);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @RequestMapping(value = TraceOrgCommonConstants.ALERT_CONFIGURE_REST_URL, method = RequestMethod.GET)
    @PreAuthorize("hasAuthority('PERM_SETTINGS_READ')")
    public ResponseEntity<?> getAlertConfiguration(HttpServletRequest request)
    {
        Response response = new Response();

        traceOrgCommonUtil.buildResponse(traceOrgAlertConfigureService.getAlertConfiguration(), response);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
