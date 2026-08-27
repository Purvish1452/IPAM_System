package com.motadata.traceorg.ipam.controller.login;

import com.motadata.traceorg.ipam.logger.TraceOrgLogger;
import com.motadata.traceorg.ipam.entity.Response;
import com.motadata.traceorg.ipam.entity.login.TraceOrgForgotPassword;
import com.motadata.traceorg.ipam.entity.settings.TraceOrgMailServer;
import com.motadata.traceorg.ipam.entity.settings.TraceOrgUser;
import com.motadata.traceorg.ipam.services.TraceOrgService;
import com.motadata.traceorg.ipam.util.TraceOrgCommonConstants;
import com.motadata.traceorg.ipam.util.TraceOrgCommonUtil;
import com.motadata.traceorg.ipam.util.TraceOrgMessageConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.net.URLEncoder;
import java.util.Date;
import java.util.List;


/**
 * @author Krunal Thakkar
 *
 */

@SuppressWarnings("ALL")
@RestController
public class TraceOrgForgotPasswordController
{

    @Autowired
    private TraceOrgService traceOrgService;

    @Autowired
    private TraceOrgCommonUtil traceOrgCommonUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final TraceOrgLogger _logger = new TraceOrgLogger(TraceOrgForgotPasswordController.class, "Forgot Password Controller");

    /**
     * IPAM-134 IPAM | Mail Server Configuration issue
     * Added generic sendMail method
     * **/
    @RequestMapping(value = TraceOrgCommonConstants.FORGOT_REST_URL, method = RequestMethod.POST)
    public ResponseEntity<?> genrateVerificationCode(HttpServletRequest request, @RequestParam String userName)
    {
        Response response = new Response();

        if (userName != null && !userName.trim().isEmpty())
        {
            try
            {
                TraceOrgUser traceOrgUser = this.traceOrgService.findByUserName(userName.trim());

                if(traceOrgUser != null)
                {
                    List<TraceOrgForgotPassword> traceOrgForgotPasswordList = (List<TraceOrgForgotPassword>)this.traceOrgService.commonQuery("",TraceOrgCommonConstants.TRACE_ORG_FORGOT_PASSWORD+" where user = '"+ traceOrgUser.getId()+"' ");

                    if(traceOrgForgotPasswordList !=null && !traceOrgForgotPasswordList.isEmpty())
                    {
                        TraceOrgForgotPassword traceOrgForgotPassword  = traceOrgForgotPasswordList.get(0);

                        if((new Date().getTime() - traceOrgForgotPassword.getTimestamp().getTime()) < 900000)
                        {

                            traceOrgCommonUtil.sendMail("Password Recovery Email From IP Address  Manager",
                                    "Your IP Address Manager verification code for reset password is: "+traceOrgForgotPassword.getUuid()+" <br><br>Enter the verification code inside of the verification code textbox in the application.<br><br>If you have any questions or trouble logging on please contact IP Address Manager administrator.");

                            response.setSuccess(TraceOrgCommonConstants.TRUE);

                            response.setMessage("Verification code already generated, kindly check your mail");

                        }
                        else
                        {
                            String uuid = traceOrgCommonUtil.generateUUID();

                            traceOrgForgotPassword.setUser(traceOrgUser);

                            traceOrgForgotPassword.setTimestamp(new Date());

                            traceOrgForgotPassword.setUuid(uuid);

                            this.traceOrgService.insert(traceOrgForgotPassword);

                            traceOrgCommonUtil.sendMail("Password Recovery Email From IP Address Manager",
                                    "Your IP Address Manager verification code for reset password is: "+uuid+" <br><br>Enter the verification code inside of the verification code textbox in the application.<br><br>If you have any questions or trouble logging on please contact IP Address Manager administrator. ");

                            response.setSuccess(TraceOrgCommonConstants.TRUE);

                            response.setMessage("Your new verification code has been sent to your registered email address.the verification code is valid for 15 minutes.");
                        }
                    }
                    else
                    {
                        String uuid = traceOrgCommonUtil.generateUUID();

                        TraceOrgForgotPassword traceOrgForgotPassword = new TraceOrgForgotPassword();

                        traceOrgForgotPassword.setUser(traceOrgUser);

                        traceOrgForgotPassword.setTimestamp(new Date());

                        traceOrgForgotPassword.setUuid(uuid);

                        this.traceOrgService.insert(traceOrgForgotPassword);

                        traceOrgCommonUtil.sendMail("Password Recovery Email From IP Address Manager",
                                "Your IP Address Manager verification code for reset password is: "+uuid+" <br><br>Enter the verification code inside of the verification code textbox in the application.<br><br>If you have any questions or trouble logging on please contact IP Address Manager administrator.");

                        response.setSuccess(TraceOrgCommonConstants.TRUE);

                        response.setMessage("Your new verification code has been sent to your registered email address.the verification code is valid for 15 minutes.");
                    }
                }
                else
                {
                    response.setSuccess(TraceOrgCommonConstants.FALSE);

                    response.setMessage(TraceOrgMessageConstants.ENTER_VALID_DETAILS);
                }
            }
            catch (Exception exception)
            {
                _logger.error(exception);

                response.setSuccess(TraceOrgCommonConstants.FALSE);

                response.setMessage(TraceOrgMessageConstants.ENTER_VALID_DETAILS);
            }
        }
        else
        {
            response.setSuccess(TraceOrgCommonConstants.FALSE);

            response.setMessage(TraceOrgMessageConstants.ENTER_VALID_DETAILS);
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    @RequestMapping(value = TraceOrgCommonConstants.NEW_PASSWORD_REST_URL, method = RequestMethod.POST)
    public ResponseEntity<?> newPassword(HttpServletRequest request, @RequestParam String userName ,@RequestParam String verificationCode,@RequestParam String password)
    {
        Response response = new Response();

        if (userName != null && !userName.trim().isEmpty() && verificationCode!=null && !verificationCode.trim().isEmpty() && password!=null && !password.trim().isEmpty())
        {
            try
            {
                TraceOrgUser traceOrgUser = this.traceOrgService.findByUserName(userName.trim());

                if(traceOrgUser != null)
                {
                    List<TraceOrgForgotPassword> traceOrgForgotPasswordList = (List<TraceOrgForgotPassword>)this.traceOrgService.commonQuery("",TraceOrgCommonConstants.TRACE_ORG_FORGOT_PASSWORD+" where uuid = '"+verificationCode.trim()+"' and user = '"+ traceOrgUser.getId()+"'");

                    if(traceOrgForgotPasswordList!=null && !traceOrgForgotPasswordList.isEmpty())
                    {
                        TraceOrgForgotPassword traceOrgForgotPassword = traceOrgForgotPasswordList.get(0);

                        long timeDifferent = (new Date().getTime() - traceOrgForgotPassword.getTimestamp().getTime()) /60000;

                        if(timeDifferent > 15)
                        {
                            response.setSuccess(TraceOrgCommonConstants.FALSE);

                            response.setMessage("Verfication Code is Expired");
                        }
                        else
                        {
                            traceOrgUser.setPassword(passwordEncoder.encode(URLEncoder.encode(password)));

                            this.traceOrgService.insert(traceOrgUser);

                            response.setSuccess(TraceOrgCommonConstants.TRUE);

                            response.setMessage(TraceOrgMessageConstants.USER_PASSWORD_UPDATE_SUCCESS);
                        }
                    }
                    else
                    {
                        response.setSuccess(TraceOrgCommonConstants.FALSE);

                        response.setMessage(TraceOrgMessageConstants.ENTER_VALID_DETAILS);
                    }
                }
                else
                {
                    response.setSuccess(TraceOrgCommonConstants.FALSE);

                    response.setMessage(TraceOrgMessageConstants.ENTER_VALID_DETAILS);
                }
            }
            catch (Exception exception)
            {
                _logger.error(exception);

                response.setSuccess(TraceOrgCommonConstants.FALSE);

                response.setMessage(TraceOrgMessageConstants.ENTER_VALID_DETAILS);
            }
        }
        else
        {
            response.setSuccess(TraceOrgCommonConstants.FALSE);

            response.setMessage(TraceOrgMessageConstants.ENTER_VALID_DETAILS);
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    @RequestMapping(value = TraceOrgCommonConstants.VERIFY_PASSWORD_TOKEN_REST_URL, method = RequestMethod.POST)
    public ResponseEntity<?> verificationCode(HttpServletRequest request, @RequestParam String userName ,@RequestParam String verificationCode)
    {
        Response response = new Response();

        if (userName != null && !userName.trim().isEmpty() && verificationCode!=null && !verificationCode.trim().isEmpty())
        {
            try
            {
                TraceOrgUser traceOrgUser = this.traceOrgService.findByUserName(userName.trim());

                if(traceOrgUser != null)
                {
                    List<TraceOrgForgotPassword> traceOrgForgotPasswordList = (List<TraceOrgForgotPassword>)this.traceOrgService.commonQuery("",TraceOrgCommonConstants.TRACE_ORG_FORGOT_PASSWORD+" where uuid = '"+verificationCode.trim()+"' and user = '"+ traceOrgUser.getId()+"'");

                    if(traceOrgForgotPasswordList!=null && !traceOrgForgotPasswordList.isEmpty())
                    {
                        TraceOrgForgotPassword traceOrgForgotPassword = traceOrgForgotPasswordList.get(0);

                        long timeDifferent = (new Date().getTime() - traceOrgForgotPassword.getTimestamp().getTime()) /60000;

                        if(timeDifferent > 15)
                        {
                            response.setSuccess(TraceOrgCommonConstants.FALSE);

                            response.setMessage("Verfication Code is Expired");
                        }
                        else
                        {
                            response.setSuccess(TraceOrgCommonConstants.TRUE);

                            response.setMessage("Verfication Code is Valid");
                        }
                    }
                    else
                    {
                        response.setSuccess(TraceOrgCommonConstants.FALSE);

                        response.setMessage(TraceOrgMessageConstants.ENTER_VALID_DETAILS);
                    }
                }
                else
                {
                    response.setSuccess(TraceOrgCommonConstants.FALSE);

                    response.setMessage(TraceOrgMessageConstants.ENTER_VALID_DETAILS);
                }
            }
            catch (Exception exception)
            {

                _logger.error(exception);

                response.setSuccess(TraceOrgCommonConstants.FALSE);

                response.setMessage(TraceOrgMessageConstants.ENTER_VALID_DETAILS);
            }
        }
        else
        {
            response.setSuccess(TraceOrgCommonConstants.FALSE);

            response.setMessage(TraceOrgMessageConstants.ENTER_VALID_DETAILS);
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}
