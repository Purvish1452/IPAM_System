package com.motadata.traceorg.ipam.configuration;


import java.util.Map;

import com.motadata.traceorg.ipam.util.TraceOrgCommonConstants;
import com.motadata.traceorg.ipam.util.TraceOrgMessageConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.AccessTokenConverter;
import org.springframework.security.oauth2.provider.token.DefaultAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.TokenStore;

import com.motadata.traceorg.ipam.services.TraceOrgService;
import com.motadata.traceorg.ipam.util.TraceOrgCommonUtil;

/**
 * @author Krunal Thakkar
 *
 */


@SuppressWarnings("ALL")
public class TraceOrgCheckAccessTokenEndpoint {

	@SuppressWarnings("SpringAutowiredFieldsWarningInspection")
    @Autowired
	private static TraceOrgService service;

	@SuppressWarnings("SpringAutowiredFieldsWarningInspection")
    @Autowired
	private static TokenStore tokenStore;


	@SuppressWarnings("SpringAutowiredFieldsWarningInspection")
    @Autowired
	private static TraceOrgCommonUtil traceOrgCommonUtil;

	private static AccessTokenConverter accessTokenConverter = new DefaultAccessTokenConverter();


	public void setAccessTokenConverter(AccessTokenConverter accessTokenConverter)
	{
		this.accessTokenConverter = accessTokenConverter;
	}

	public static Map<String, ?> checkToken(String value)
	{

		OAuth2AccessToken token = tokenStore.readAccessToken(value);
		
		if (token == null) 
		{
			throw new InvalidTokenException(TraceOrgMessageConstants.TOKEN_NOT_RECOGNISED);
		}

		if (token.isExpired()) 
		{
			throw new InvalidTokenException(TraceOrgMessageConstants.TOKEN_EXPIRED);
		}

		OAuth2Authentication authentication = tokenStore.readAuthentication(token.getValue());

		Map<String, Object> response = (Map<String, Object>)accessTokenConverter.convertAccessToken(token, authentication);
		
		response.put(TraceOrgCommonConstants.AUTHORITIES,traceOrgCommonUtil.getAuthorityList(service,String.valueOf(response.get("user_name"))));

		response.put(TraceOrgCommonConstants.ACTIVE, true);

		return response;
	}
}