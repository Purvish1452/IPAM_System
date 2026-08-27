package com.motadata.traceorg.ipam.controller.login;

import com.google.common.io.Files;
import com.motadata.traceorg.ipam.logger.TraceOrgLogger;
import com.motadata.traceorg.ipam.entity.Response;
import com.motadata.traceorg.ipam.entity.settings.TraceOrgUser;
import com.motadata.traceorg.ipam.services.TraceOrgService;
import com.motadata.traceorg.ipam.util.TraceOrgCommonConstants;
import com.motadata.traceorg.ipam.util.TraceOrgCommonUtil;
import com.motadata.traceorg.ipam.util.TraceOrgMessageConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.Date;

@SuppressWarnings("ALL")
@RestController
public class TraceOrgLoginStatusController {

	@Autowired
	private TraceOrgService traceOrgService;

    @Autowired
    private TraceOrgCommonUtil traceOrgCommonUtil;

	private static final TraceOrgLogger _logger = new TraceOrgLogger(TraceOrgLoginStatusController.class, "Login Status Controller");

	@RequestMapping(value = TraceOrgCommonConstants.CHANGE_LOGIN_STATUS_URL, method = RequestMethod.GET)
	public ResponseEntity<?> loginManage(Response response,@RequestParam String userName)
	{
        try
        {
            TraceOrgUser traceOrgUser =  traceOrgService.findByUserName(userName);

            if (traceOrgUser != null)
            {
                traceOrgUser.setCurrentLoginStatus(new Date());

                if(traceOrgUser.getPreviousLoginStatus() == null)
                {
                    traceOrgUser.setPreviousLoginStatus(new Date());
                }

                traceOrgService.insert(traceOrgUser);

                response.setSuccess(true);
            }
            else
            {
                _logger.warn("User "+userName +" is not found..");
            }
        }
        catch (Exception exception)
        {
            _logger.error(exception);
        }
        return new ResponseEntity<>(response,HttpStatus.OK);
	}


	@RequestMapping(value = TraceOrgCommonConstants.CHANGE_LOGOUT_STATUS_URL , method = RequestMethod.GET)
	public ResponseEntity<?> logoutManage(Response response,@RequestParam String userName)
	{
        try
        {
            TraceOrgUser traceOrgUser =  traceOrgService.findByUserName(userName);

            if (traceOrgUser != null)
            {
                if(traceOrgUser.getCurrentLoginStatus()!= null)
                {
                    traceOrgUser.setPreviousLoginStatus(new Date(traceOrgUser.getCurrentLoginStatus()));
                }

                traceOrgUser.setCurrentLoginStatus(null);

                traceOrgService.insert(traceOrgUser);

                response.setSuccess(true);
            }
            else
            {
                _logger.warn("User "+userName +" is not found..");
            }
        }
        catch (Exception exception)
        {
            _logger.error(exception);
        }
		return new ResponseEntity<>(response,HttpStatus.OK);
    }

    @RequestMapping(value = "/downloadPdf/{id}", method = RequestMethod.GET)
    public void downloadPdf(HttpServletRequest request, HttpServletResponse response, @PathVariable(TraceOrgCommonConstants.ID) String id)
    {
        try
        {
            String filePath = TraceOrgCommonConstants.CURRENT_DIR + TraceOrgCommonConstants.PATH_SEPARATOR + "Report" + TraceOrgCommonConstants.PATH_SEPARATOR + id + ".pdf";

            File file = new File(filePath);

            if (file.exists())
            {
                response.setContentType("application/pdf");

                response.addHeader("Content-Disposition", "attachment; filename="+id+".pdf");

                try
                {
                    Files.copy(file, response.getOutputStream());

                    response.getOutputStream().flush();
                }
                catch (IOException ex)
                {
                    ex.printStackTrace();
                }
            }
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
        }

    }

    @RequestMapping(value = "/downloadCsv/{id}", method = RequestMethod.GET)
    public void downloadCsv(HttpServletRequest request,HttpServletResponse response, @PathVariable(TraceOrgCommonConstants.ID) String id)
    {
        try
        {
            String filePath = TraceOrgCommonConstants.CURRENT_DIR +TraceOrgCommonConstants.PATH_SEPARATOR+"Report"+TraceOrgCommonConstants.PATH_SEPARATOR + id + ".csv";

            File file = new File(filePath);

            if (file.exists())
            {
                response.addHeader("Content-Disposition", "attachment; filename="+id+".csv");

                try
                {
                    Files.copy(file, response.getOutputStream());

                    response.getOutputStream().flush();
                }
                catch (IOException ex)
                {
                    ex.printStackTrace();
                }
            }
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
        }

    }

    @RequestMapping(value = "/validatePermission/", method = RequestMethod.GET)
    public ResponseEntity<?> getCurrentUserRole(HttpServletRequest request)
    {
        Response response = new Response();

        try
        {
            String accessToken = request.getHeader(TraceOrgCommonConstants.ACCESSTOKEN);

            response.setSuccess(TraceOrgCommonConstants.TRUE);

            response.setCurrentUserRole(traceOrgCommonUtil.currentUserRole(accessToken));
        }
        catch (Exception exception)
        {
            _logger.error(exception);

            response.setSuccess(TraceOrgCommonConstants.FALSE);
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
