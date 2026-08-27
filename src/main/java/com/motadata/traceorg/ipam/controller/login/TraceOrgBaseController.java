package com.motadata.traceorg.ipam.controller.login;

import com.motadata.traceorg.ipam.logger.TraceOrgLogger;
import com.motadata.traceorg.ipam.entity.Response;
import com.motadata.traceorg.ipam.entity.login.TraceOrgAuthTokenInfo;
import com.motadata.traceorg.ipam.entity.settings.TraceOrgBrand;
import com.motadata.traceorg.ipam.entity.settings.TraceOrgGlobalSetting;
import com.motadata.traceorg.ipam.entity.settings.TraceOrgUser;
import com.motadata.traceorg.ipam.repository.settings.TraceOrgUserRepository;
import com.motadata.traceorg.ipam.services.TraceOrgService;
import com.motadata.traceorg.ipam.util.TraceOrgCommonConstants;
import com.motadata.traceorg.ipam.util.TraceOrgCommonUtil;
import com.motadata.traceorg.ipam.util.TraceOrgMessageConstants;
import org.apache.commons.codec.binary.Base64;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * @author Krunal Thakkar
 *
 */

@SuppressWarnings("ALL")
@Controller
public class TraceOrgBaseController 
{

    private static final int OFF = 0;

    private static final int ON = 1;
    @Autowired
    TraceOrgCommonUtil traceOrgCommonUtil;

    @Autowired
    TraceOrgUserRepository traceOrgUserRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private TraceOrgService traceOrgService;

    @Autowired
    private TokenStore tokenStore;

    @Value("${version}")
    private String version;

    private static final Logger logger = LogManager.getLogger(TraceOrgBaseController.class);

    private static final TraceOrgLogger _logger = new TraceOrgLogger(TraceOrgBaseController.class, "Base Controller");

    private static final String MODULE = "TRACE-ORG-BASE-CTRL";

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String index(Model model, HttpServletRequest request, HttpSession session)
    {
        String indexUrl = TraceOrgCommonConstants.LOGIN;

        try
        {
            model.addAttribute("message",session.getAttribute(TraceOrgCommonConstants.MESSAGE));

            if (traceOrgCommonUtil.getToken(request) != null && !traceOrgCommonUtil.getToken(request).isEmpty())
            {
                indexUrl =  TraceOrgCommonConstants.REDIRECT+TraceOrgCommonConstants.HOME_URL;
            }
            else
            {
                indexUrl =  TraceOrgCommonConstants.LOGIN;
            }
        }
        catch (Exception exception)
        {
            _logger.error(exception);
        }
        return indexUrl;
    }

    @RequestMapping(value =  TraceOrgCommonConstants.LOGOUT_URL, method = RequestMethod.GET)
    private String logout(HttpServletRequest request,RestTemplate restTemplate,HttpServletResponse response)
    {
        try
        {
            restTemplate.getForObject(TraceOrgCommonConstants.AUTH_SERVER_URL+"/changeLogoutStatus/?userName="+traceOrgCommonUtil.getUserName(request), Response.class);

            if (traceOrgCommonUtil.getToken(request) != null)
            {
                String tokenValue = traceOrgCommonUtil.getToken(request);

                OAuth2AccessToken accessToken = this.tokenStore.readAccessToken(tokenValue);
            }

            request.getSession().invalidate();

            response.addHeader(TraceOrgCommonConstants.SET_COOKIE, "userName=; Path=/");

            response.addHeader(TraceOrgCommonConstants.SET_COOKIE, "token=; Path=/");
        }
        catch (Exception exception)
        {
            _logger.error(exception);
        }

        return TraceOrgCommonConstants.REDIRECT+"/";
    }

    @RequestMapping(value = TraceOrgCommonConstants.LOGIN_USER_URL, method = RequestMethod.POST)
    public String loginUser(@RequestParam String userName, RedirectAttributes redirectAttributes, HttpSession session, @RequestParam String password, HttpServletResponse response, RestTemplate restTemplate)
    {
        String loginUrl = TraceOrgCommonConstants.REDIRECT + "/";

        try
        {
            String QPM_PASSWORD_GRANT = "?grant_type=password&username="+userName+"&password="+ URLEncoder.encode(password);

            java.util.Optional<TraceOrgUser> userOpt = traceOrgUserRepository.findByUserName(userName);

            if(userOpt.isPresent())
            {
                TraceOrgUser traceOrgUser = userOpt.get();

                _logger.info("User data : " + traceOrgUser);

                if(passwordEncoder.matches(URLEncoder.encode(password), traceOrgUser.getPassword()))
                {
                    if(traceOrgUser.isStatus())
                    {
                        TraceOrgAuthTokenInfo traceOrgAuthTokenInfo = sendTokenRequest(QPM_PASSWORD_GRANT);

                        if(traceOrgAuthTokenInfo != null)
                        {
                            Response restResponse = restTemplate.getForObject(TraceOrgCommonConstants.AUTH_SERVER_URL+"/changeLoginStatus/?userName="+ traceOrgAuthTokenInfo.getUser().get("username"), Response.class);

                            if(restResponse.isSuccess())
                            {
                                response.addHeader(TraceOrgCommonConstants.SET_COOKIE, "userName="+ traceOrgAuthTokenInfo.getUser().get("username")+"; Path=/");

                                response.addHeader(TraceOrgCommonConstants.SET_COOKIE, "token="+ traceOrgAuthTokenInfo.getAccess_token()+"; Path=/");

                                response.addHeader(TraceOrgCommonConstants.SET_COOKIE, "authorities="+ traceOrgAuthTokenInfo.getUser().get("authorities")+"; Path=/");

                                loginUrl =  TraceOrgCommonConstants.REDIRECT+ TraceOrgCommonConstants.HOME_URL;
                            }
                            else
                            {
                                _logger.debug("Login failed: Token is not authorized for User "+ userName );

                                loginUrl = TraceOrgCommonConstants.REDIRECT + "/";
                            }
                        }
                        else
                        {
                            _logger.debug("Login failed: Token is null for User "+ userName );

                            loginUrl =  TraceOrgCommonConstants.REDIRECT + "/";
                        }
                    }
                    else
                    {
                        _logger.debug("Login failed: User "+ userName+ " is disabled");

                        session.setAttribute(TraceOrgCommonConstants.MESSAGE, "Login failed: User is Disabled. Please contact to admin");

                        loginUrl = TraceOrgCommonConstants.REDIRECT +"/";
                    }
                }
                else
                {
                    _logger.debug("Login failed: User "+ userName+ " is not authenticated");

                    session.setAttribute(TraceOrgCommonConstants.MESSAGE, TraceOrgMessageConstants.BAD_CREDENTIAL);

                    loginUrl =  TraceOrgCommonConstants.REDIRECT +"/";
                }
            }
            else
            {
                _logger.debug("Login failed: User "+ userName+ " not found");

                session.setAttribute(TraceOrgCommonConstants.MESSAGE, TraceOrgMessageConstants.BAD_CREDENTIAL);

                loginUrl =  TraceOrgCommonConstants.REDIRECT +"/";
            }
        }
        catch (Exception exception)
        {
            _logger.error(exception);
            session.setAttribute(TraceOrgCommonConstants.MESSAGE, TraceOrgMessageConstants.BAD_CREDENTIAL);
            loginUrl = TraceOrgCommonConstants.REDIRECT + "/";
        }
        return loginUrl;
    }

    @RequestMapping(value = TraceOrgCommonConstants.HOME_URL, method = RequestMethod.GET)
    public String homePage(Model model, HttpServletRequest request)
    {
        String homeUrl = TraceOrgCommonConstants.REDIRECT + "/";

        try
        {
            if (traceOrgCommonUtil.getToken(request) != null && !traceOrgCommonUtil.getToken(request).isEmpty())
            {
                TraceOrgBrand traceOrgBrand = (TraceOrgBrand)this.traceOrgService.getById(TraceOrgCommonConstants.TRACE_ORG_BRAND,1L);

                TraceOrgGlobalSetting traceOrgGlobalSetting = (TraceOrgGlobalSetting)this.traceOrgService.getById(TraceOrgCommonConstants.TRACE_ORG_GLOBAL_SETTING,1L);

                model.addAttribute(TraceOrgCommonConstants.BRAND_NAME,traceOrgBrand.getProductName());

                model.addAttribute(TraceOrgCommonConstants.CSS_MODE,traceOrgGlobalSetting.getCssMode());

                model.addAttribute(TraceOrgCommonConstants.USER_NAME,traceOrgCommonUtil.currentUserName(traceOrgCommonUtil.getToken(request)));

                model.addAttribute(TraceOrgCommonConstants.AUTH_SERVER_URL);

                model.addAttribute("version",version);

                homeUrl =  TraceOrgCommonConstants.HOME_PAGE;
            }
            else
            {
                homeUrl =  TraceOrgCommonConstants.REDIRECT + "/";
            }
        }
        catch (Exception exception)
        {
            _logger.error(exception);
        }
        return homeUrl;
    }

    @SuppressWarnings("unchecked")
    private TraceOrgAuthTokenInfo sendTokenRequest(String QPM_PASSWORD_GRANT)
    {
        try
        {
            if(QPM_PASSWORD_GRANT!=null && !QPM_PASSWORD_GRANT.isEmpty())
            {
                RestTemplate restTemplate = new RestTemplate();

                _logger.info(MODULE + " : " + getHeadersWithClientCredentials().toString());

                HttpEntity<String> request = new HttpEntity<>(getHeadersWithClientCredentials());

                _logger.info(MODULE + " : Authenication url : " + TraceOrgCommonConstants.AUTH_SERVER_TOKEN_URL+QPM_PASSWORD_GRANT);

                this.traceOrgService.switchSafeUpdateMode(OFF);

                if(restTemplate.exchange(TraceOrgCommonConstants.AUTH_SERVER_TOKEN_URL+QPM_PASSWORD_GRANT, HttpMethod.POST, request, Object.class) != null)
                {
                    ResponseEntity<Object> response = restTemplate.exchange(TraceOrgCommonConstants.AUTH_SERVER_TOKEN_URL+QPM_PASSWORD_GRANT, HttpMethod.POST, request, Object.class);

                    _logger.info(MODULE + " : Response from auth : " + response);

                    if(response!=null)
                    {
                        LinkedHashMap<String, Object> map = (LinkedHashMap<String, Object>)response.getBody();

                        TraceOrgAuthTokenInfo tokenInfo = null;

                        if(map!=null)
                        {
                            tokenInfo = new TraceOrgAuthTokenInfo();

                            tokenInfo.setAccess_token((String)map.get(TraceOrgCommonConstants.ACCESS_TOKEN));

                            tokenInfo.setToken_type((String)map.get(TraceOrgCommonConstants.TOKEN_TYPE));

                            tokenInfo.setRefresh_token((String)map.get(TraceOrgCommonConstants.REFRESH_TOKEN));

                            tokenInfo.setExpires_in((int)map.get(TraceOrgCommonConstants.EXPIRES_IN));

                            tokenInfo.setScope((String)map.get(TraceOrgCommonConstants.SCOPE));

                            tokenInfo.setUser((HashMap<?, ?>) map.get(TraceOrgCommonConstants.USER));
                        }

                        this.traceOrgService.switchSafeUpdateMode(ON);

                        return tokenInfo;
                    }
                    else
                    {
                        _logger.info(MODULE + " : Response is null while processing.");

                        this.traceOrgService.switchSafeUpdateMode(ON);

                        return null;
                    }
                }
                else
                {
                    _logger.info(MODULE + " : Safe update mode for query execution not turned off or token received as null.");

                    this.traceOrgService.switchSafeUpdateMode(ON);

                    return null;
                }
            }
        }
        catch (HttpStatusCodeException e)
        {
            logger.error(MODULE + e.getResponseBodyAsString());

            this.traceOrgService.switchSafeUpdateMode(ON);

            _logger.error(e);
        }
        catch(RestClientException e)
        {
            this.traceOrgService.switchSafeUpdateMode(ON);

            _logger.error(e);
        }
        catch (Exception exception)
        {
            this.traceOrgService.switchSafeUpdateMode(ON);

            _logger.error(exception);
        }

        return null;
    }

    @SuppressWarnings("ArraysAsListWithZeroOrOneArgument")
    private HttpHeaders getHeaders()
    {
        HttpHeaders headers = new HttpHeaders();
    
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        
        return headers;
    }

    private HttpHeaders getHeadersWithClientCredentials()
    {
        String plainClientCredentials=TraceOrgCommonConstants.CLIENT_KEY+":"+TraceOrgCommonConstants.SECRET_KEY;
    
        String base64ClientCredentials = new String(Base64.encodeBase64(plainClientCredentials.getBytes()));
        
        HttpHeaders headers = getHeaders();
        
        headers.add(TraceOrgCommonConstants.AUTHORIZATION, TraceOrgCommonConstants.BASIC + base64ClientCredentials);
        
        return headers;
    }
}
