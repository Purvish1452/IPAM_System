package com.motadata.traceorg.ipam.controller.supernet;

import com.motadata.traceorg.ipam.dto.supernet.TraceOrgSupernetDTO;
import com.motadata.traceorg.ipam.entity.Response;
import com.motadata.traceorg.ipam.services.supernet.TraceOrgSupernetService;
import com.motadata.traceorg.ipam.util.TraceOrgCommonConstants;
import com.motadata.traceorg.ipam.util.TraceOrgCommonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;


@RestController
public class TraceOrgSupernetController
{
    @Autowired
    private TraceOrgSupernetService supernetService;

    @Autowired
    TraceOrgCommonUtil traceOrgCommonUtil;

    /**
     * IPAM-148 : System should have the ability to locate the available subnets inside a Supernet. This is to provide assistance to users when creating subnets inside an aggregated Network.
     * added the addSupernet method to add the supernet
     * @param request
     * @param traceOrgSupernetDTO
     * @return
     */
    @RequestMapping(value = TraceOrgCommonConstants.ADD_SUPERNET, method = RequestMethod.POST)
    @PreAuthorize("hasAuthority('PERM_DASHBOARD_WRITE')")
    public ResponseEntity<?> addSupernet(HttpServletRequest request,@RequestBody TraceOrgSupernetDTO traceOrgSupernetDTO)
    {
        Response response = new Response();

        traceOrgCommonUtil.buildResponse(supernetService.addSupernet(request.getHeader(TraceOrgCommonConstants.ACCESSTOKEN),traceOrgSupernetDTO), response);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * IPAM-148 : System should have the ability to locate the available subnets inside a Supernet. This is to provide assistance to users when creating subnets inside an aggregated Network.
     * added the listAllSupernetByCategory method to get the supernets by category
     * @param request
     * @return
     */
    @RequestMapping(value = TraceOrgCommonConstants.SUPERNET_BY_CATEGORY, method = RequestMethod.GET)
    @PreAuthorize("hasAuthority('PERM_DASHBOARD_READ')")
    public ResponseEntity<?> listAllSupernetByCategory(HttpServletRequest request)
    {
        Response response = new Response();

        traceOrgCommonUtil.buildResponse(supernetService.getSupernetDetails(request.getHeader(TraceOrgCommonConstants.ACCESSTOKEN)), response);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * IPAM-148 : System should have the ability to locate the available subnets inside a Supernet. This is to provide assistance to users when creating subnets inside an aggregated Network.
     * added the removeSupernet method to remove the supernet
     * @param id
     * @param request
     * @return
     */
    @RequestMapping(value = TraceOrgCommonConstants.REMOVE_SUPERNET+"{id}", method = RequestMethod.DELETE)
    @PreAuthorize("hasAuthority('PERM_DASHBOARD_WRITE')")
    public ResponseEntity<?> removeSupernet(@PathVariable(TraceOrgCommonConstants.ID) Long id, HttpServletRequest request)
    {
        Response response = new Response();

        traceOrgCommonUtil.buildResponse(supernetService.removeSupernet(request.getHeader(TraceOrgCommonConstants.ACCESSTOKEN),id), response);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
