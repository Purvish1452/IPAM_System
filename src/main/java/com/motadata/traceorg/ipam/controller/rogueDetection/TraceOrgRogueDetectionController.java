package com.motadata.traceorg.ipam.controller.rogueDetection;

import com.motadata.traceorg.ipam.entity.Response;
import com.motadata.traceorg.ipam.logger.TraceOrgLogger;
import com.motadata.traceorg.ipam.services.TraceOrgService;
import com.motadata.traceorg.ipam.services.rogueDetection.TraceOrgRogueDetectionService;
import com.motadata.traceorg.ipam.util.TraceOrgCommonConstants;
import com.motadata.traceorg.ipam.util.TraceOrgCommonUtil;
import com.motadata.traceorg.ipam.util.TraceOrgMessageConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;

@SuppressWarnings("ALL")
@RestController
public class TraceOrgRogueDetectionController
{
    @Autowired
    private TraceOrgRogueDetectionService traceOrgRogueDetectionService;

    @Autowired
    private TraceOrgCommonUtil traceOrgCommonUtil;

    private static final TraceOrgLogger _logger = new TraceOrgLogger(TraceOrgRogueDetectionController.class, "Rogue Detection Controller");

    /**
     * IPAM-145 : System should have rogue device detection capability
     * Get All Authenticity mac details.
     * @param request
     * @return
     */
    @RequestMapping(value = TraceOrgCommonConstants.ROGUE_DETECTION_URL, method = RequestMethod.GET)
    @PreAuthorize("hasAuthority('PERM_ROGUE DETECTION_READ')")
    public ResponseEntity<?> loadRogueDetectionPage(HttpServletRequest request)
    {
        Response response = new Response();

        traceOrgCommonUtil.buildResponse(traceOrgRogueDetectionService.loadRogueDetectionDetails(), response);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * IPAM-145 : System should have rogue device detection capability
     * Get specific Authenticity mac details.
     * @param page
     * @param request
     * @return
     */
    @RequestMapping(value = TraceOrgCommonConstants.ROGUE_DETECTION_URL +"{page}", method = RequestMethod.GET)
    @PreAuthorize("hasAuthority('PERM_ROGUE DETECTION_READ')")
    public ResponseEntity<?> loadIndividualRogueDetectionPage(@PathVariable(TraceOrgCommonConstants.PAGE) String page, HttpServletRequest request)
    {
        Response response = new Response();

        traceOrgCommonUtil.buildResponse(traceOrgRogueDetectionService.loadIndividualRogueDetectionDetails(page), response);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * IPAM-145 : System should have rogue device detection capability
     * Update the authenticity of mac.
     * @param id
     * @param status
     * @param request
     * @return
     */
    @RequestMapping(value = TraceOrgCommonConstants.ROGUE_DETECTION_MARKED_AUTHENTICITY, method = RequestMethod.POST)
    @PreAuthorize("hasAuthority('PERM_ROGUE DETECTION_WRITE')")
    public ResponseEntity<?> markedAuthenticityOfMAC(@RequestParam String id, @RequestParam boolean status, HttpServletRequest request)
    {
        Response response = new Response();

        String accessToken = request.getHeader(TraceOrgCommonConstants.ACCESSTOKEN);

        traceOrgCommonUtil.buildResponse(traceOrgRogueDetectionService.markedAuthenticityOfMAC(id, status, accessToken), response);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * IPAM-145 : System should have rogue device detection capability
     * If authenticity discovered then delete permenantly otherwise change the authenticity to discovered.
     * @param id
     * @param request
     * @return
     */
    @RequestMapping(value = TraceOrgCommonConstants.ROGUE_DETECTION_URL+"{id}", method = RequestMethod.DELETE)
    @PreAuthorize("hasAuthority('PERM_ROGUE DETECTION_WRITE')")
    public ResponseEntity<?> deleteMACAddresses(@PathVariable(TraceOrgCommonConstants.ID) String id,HttpServletRequest request)
    {
        Response response = new Response();

        traceOrgCommonUtil.buildResponse(traceOrgRogueDetectionService.deleteMACAddresses(id), response);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * IPAM-145 : System should have rogue device detection capability
     * Mark Trusted authenticity of given mac address in csv.
     * @param request
     * @param trustedMACAddressesCsv
     * @return
     */
    @RequestMapping(value = TraceOrgCommonConstants.ROGUE_DETECTION_TRUSTED_MAC_ADDRESS_BY_CSV, method = RequestMethod.POST)
    @PreAuthorize("hasAuthority('PERM_ROGUE DETECTION_WRITE')")
    public ResponseEntity<?> importTrustedMACAddressFromCSV(HttpServletRequest request, @RequestParam MultipartFile trustedMACAddressesCsv)
    {
        Response response = new Response();

        traceOrgCommonUtil.buildResponse(traceOrgRogueDetectionService.importTrustedMACAddressFromCSV(trustedMACAddressesCsv, request), response);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * IPAM-145 : System should have rogue device detection capability
     * Export pdf report.
     * @param url
     * @param request
     * @return
     */
    @RequestMapping(value = TraceOrgCommonConstants.ROGUE_DETECTION_EXPORT_PDF, method = RequestMethod.GET)
    @PreAuthorize("hasAuthority('PERM_ROGUE DETECTION_READ')")
    public ResponseEntity<?> exportRogueDetectionPDF(@RequestParam String url, HttpServletRequest request)
    {
        Response response = new Response();

        traceOrgCommonUtil.buildResponse(traceOrgRogueDetectionService.exportRogueDetectionDetails(TraceOrgCommonConstants.ALL_ROGUE_DETECTION_DETAILS_EXPORT, TraceOrgCommonConstants.EXPORT_PDF,null, url), response);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * IPAM-145 : System should have rogue device detection capability
     * Export selected mac pdf report.
     * @param id
     * @param request
     * @return
     */
    @RequestMapping(value = TraceOrgCommonConstants.ROGUE_DETECTION_EXPORT_PDF+"{id}", method = RequestMethod.GET)
    @PreAuthorize("hasAuthority('PERM_ROGUE DETECTION_READ')")
    public ResponseEntity<?> exportSelectedIDRogueDetectionPDF(@PathVariable(TraceOrgCommonConstants.ID) String id,HttpServletRequest request)
    {
        Response response = new Response();

        traceOrgCommonUtil.buildResponse(traceOrgRogueDetectionService.exportRogueDetectionDetails(TraceOrgCommonConstants.SELECTED_ROGUE_DETECTION_DETAILS_EXPORT, TraceOrgCommonConstants.EXPORT_PDF, id, null), response);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * IPAM-145 : System should have rogue device detection capability
     * export csv report.
     * @param url
     * @param request
     * @return
     */
    @RequestMapping(value = TraceOrgCommonConstants.ROGUE_DETECTION_EXPORT_CSV, method = RequestMethod.GET)
    @PreAuthorize("hasAuthority('PERM_ROGUE DETECTION_READ')")
    public ResponseEntity<?> exportRogueDetectionCSV(@RequestParam String url, HttpServletRequest request)
    {
        Response response = new Response();

        traceOrgCommonUtil.buildResponse(traceOrgRogueDetectionService.exportRogueDetectionDetails(TraceOrgCommonConstants.ALL_ROGUE_DETECTION_DETAILS_EXPORT, TraceOrgCommonConstants.EXPORT_CSV, null, url), response);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * IPAM-145 : System should have rogue device detection capability
     * export selected mac csv report.
     * @param id
     * @param request
     * @return
     */
    @RequestMapping(value = TraceOrgCommonConstants.ROGUE_DETECTION_EXPORT_CSV+"{id}", method = RequestMethod.GET)
    @PreAuthorize("hasAuthority('PERM_ROGUE DETECTION_READ')")
    public ResponseEntity<?> exportSelectedIDRogueDetectionCSV(@PathVariable(TraceOrgCommonConstants.ID) String id,HttpServletRequest request)
    {
        Response response = new Response();

        traceOrgCommonUtil.buildResponse(traceOrgRogueDetectionService.exportRogueDetectionDetails(TraceOrgCommonConstants.SELECTED_ROGUE_DETECTION_DETAILS_EXPORT, TraceOrgCommonConstants.EXPORT_CSV, id, null), response);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
