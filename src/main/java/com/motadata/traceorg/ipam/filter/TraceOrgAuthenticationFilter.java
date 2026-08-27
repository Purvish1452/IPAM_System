package com.motadata.traceorg.ipam.filter;

import com.motadata.traceorg.ipam.logger.TraceOrgLogger;
import com.motadata.traceorg.ipam.util.TraceOrgCommonConstants;
import com.motadata.traceorg.ipam.util.TraceOrgCommonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.common.exceptions.UnauthorizedUserException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


@Component
public class TraceOrgAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    TraceOrgCommonUtil traceOrgCommonUtil;

    private static final TraceOrgLogger _logger = new TraceOrgLogger(TraceOrgAuthenticationFilter.class,"AuthenticationFilter");

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException
    {
        String accessToken = request.getHeader(TraceOrgCommonConstants.ACCESSTOKEN);

        if (accessToken != null && !accessToken.isEmpty())
        {
            if (!traceOrgCommonUtil.checkToken(accessToken))
            {
                throw new UnauthorizedUserException("Invalid access token");
            }
        }

        filterChain.doFilter(request, response);
    }
}
