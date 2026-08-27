package com.motadata.traceorg.ipam.controller.settings;

import com.motadata.traceorg.ipam.entity.Response;
import com.motadata.traceorg.ipam.entity.settings.TraceOrgUser;
import com.motadata.traceorg.ipam.services.settings.TraceOrgUserService;
import com.motadata.traceorg.ipam.util.TraceOrgCommonConstants;
import com.motadata.traceorg.ipam.util.TraceOrgCommonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@SuppressWarnings({"ALL"})
@RestController
public class TraceOrgUserController
{
	@Autowired
	private TraceOrgCommonUtil traceOrgCommonUtil;

    @Autowired
    private TraceOrgUserService traceOrgUserService;

	@RequestMapping(value = TraceOrgCommonConstants.USER_REST_URL, method = RequestMethod.GET)
    @PreAuthorize("hasAuthority('PERM_SETTINGS_READ')")
    public ResponseEntity<?> listAllUsers(HttpServletRequest request)
    {
		Response response = new Response();

        traceOrgCommonUtil.buildResponse(traceOrgUserService.listAllUsers(), response);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
	
	
	@RequestMapping(value = TraceOrgCommonConstants.USER_REST_URL, method = RequestMethod.POST)
    @PreAuthorize("hasAuthority('PERM_SETTINGS_WRITE')")
    public ResponseEntity<?> insertUser(HttpServletRequest request, @RequestBody TraceOrgUser traceOrgUser)
    {
		Response response = new Response();

        traceOrgCommonUtil.buildResponse(traceOrgUserService.insertUser(traceOrgUser, request.getHeader(TraceOrgCommonConstants.ACCESSTOKEN)), response);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
	
	
	@RequestMapping(value = TraceOrgCommonConstants.USER_REST_URL + "{id}", method = RequestMethod.GET)
    @PreAuthorize("hasAuthority('PERM_SETTINGS_READ')")
    public ResponseEntity<?> getUser(@PathVariable(TraceOrgCommonConstants.ID) Long id, HttpServletRequest request)
    {
        Response response = new Response();

        traceOrgCommonUtil.buildResponse(traceOrgUserService.getUser(id), response);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

	@RequestMapping(value = TraceOrgCommonConstants.USER_REST_URL + "{id}", method = RequestMethod.PUT)
    @PreAuthorize("hasAuthority('PERM_SETTINGS_WRITE')")
	public ResponseEntity<?> updateUser(@PathVariable Long id,HttpServletRequest request, @RequestBody TraceOrgUser traceOrgUser)
	{
        Response response = new Response();

        traceOrgCommonUtil.buildResponse(traceOrgUserService.updateUser(id, traceOrgUser, request.getHeader(TraceOrgCommonConstants.ACCESSTOKEN)), response);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
	
	@RequestMapping(value = TraceOrgCommonConstants.USER_REST_URL + "{id}", method = RequestMethod.DELETE)
    @PreAuthorize("hasAuthority('PERM_SETTINGS_WRITE')")
    public ResponseEntity<?> removeUser(@PathVariable(TraceOrgCommonConstants.ID) Long id, HttpServletRequest request)
    {
		Response response = new Response();

        traceOrgCommonUtil.buildResponse(traceOrgUserService.removeUser(id, request.getHeader(TraceOrgCommonConstants.ACCESSTOKEN)), response);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

	@SuppressWarnings("unchecked")
	@RequestMapping(value = TraceOrgCommonConstants.USER_ROLE_REST_URL, method = RequestMethod.GET)
    @PreAuthorize("hasAuthority('PERM_SETTINGS_READ')")
	public ResponseEntity<?> listAllUserRoles(HttpServletRequest request)
	{
		Response response = new Response();

        traceOrgCommonUtil.buildResponse(traceOrgUserService.listAllUserRoles(), response);

		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@RequestMapping(value = "/changePassword/{id}", method = RequestMethod.PUT)
    @PreAuthorize("hasAuthority('PERM_SETTINGS_WRITE')")
	public ResponseEntity<?> changePassword(@PathVariable Long id, HttpServletRequest request, @RequestBody TraceOrgUser traceOrgUser)
	{
		Response response = new Response();

        traceOrgCommonUtil.buildResponse(traceOrgUserService.changePassword(id, traceOrgUser), response);

		return new ResponseEntity<>(response, HttpStatus.OK);
	}

    @RequestMapping(value = "/authority/{id}", method = RequestMethod.PUT)
    @PreAuthorize("hasAuthority('PERM_SETTINGS_WRITE')")
    public ResponseEntity<?> getAuthority(@PathVariable Long id, HttpServletRequest request, @RequestBody TraceOrgUser traceOrgUser)
    {
        Response response = new Response();

        traceOrgCommonUtil.buildResponse(traceOrgUserService.changePassword(id, traceOrgUser), response);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}