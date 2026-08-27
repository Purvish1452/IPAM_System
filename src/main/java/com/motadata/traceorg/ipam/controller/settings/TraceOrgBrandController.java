package com.motadata.traceorg.ipam.controller.settings;

import com.motadata.traceorg.ipam.entity.Response;
import com.motadata.traceorg.ipam.services.settings.TraceOrgBrandService;
import com.motadata.traceorg.ipam.util.TraceOrgCommonConstants;
import com.motadata.traceorg.ipam.util.TraceOrgCommonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;

@SuppressWarnings("ALL")
@RestController
public class TraceOrgBrandController
{
    @Autowired
    private TraceOrgCommonUtil traceOrgCommonUtil;

    @Autowired
    private TraceOrgBrandService traceOrgBrandService;

    @SuppressWarnings("EqualsBetweenInconvertibleTypes")
    @RequestMapping(value = TraceOrgCommonConstants.BRAND_REST_URL + "{id}", method = RequestMethod.PUT)
    @PreAuthorize("hasAuthority('PERM_SETTINGS_WRITE')")
    public ResponseEntity<?> updateBrand(@PathVariable Long id, HttpServletRequest request, @RequestParam MultipartFile brandLogo, @RequestParam String productName)
    {
        Response response = new Response();

        traceOrgCommonUtil.buildResponse(traceOrgBrandService.updateBrand(id, brandLogo, productName, request), response);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
