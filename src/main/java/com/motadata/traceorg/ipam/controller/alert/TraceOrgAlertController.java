package com.motadata.traceorg.ipam.controller.alert;

import com.motadata.traceorg.ipam.entity.Response;
import com.motadata.traceorg.ipam.services.alert.TraceOrgAlertService;
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
public class TraceOrgAlertController {

    @Autowired
    private TraceOrgAlertService  traceOrgAlertService;

    @Autowired
    private TraceOrgCommonUtil traceOrgCommonUtil;

    /**
     * IPAM-149 : IPAM Roadmap : System should have alert notification module to configure different kind of alert notification
     * Refactor getAlerts
     * */
    @RequestMapping(value = TraceOrgCommonConstants.ALERTS_REST_URL, method = RequestMethod.GET)
    @PreAuthorize("hasAuthority('PERM_ALERTS_READ')")
    public ResponseEntity<?> getAlerts(HttpServletRequest request, @RequestParam(required = false) String alertFilter,  @RequestParam(required = false) Integer page,  @RequestParam(required = false) Integer pageSize)
    {
        Response response = new Response();

        traceOrgCommonUtil.buildResponse(traceOrgAlertService.getAlerts(alertFilter, page, pageSize), response);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
