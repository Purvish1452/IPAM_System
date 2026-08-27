package com.motadata.traceorg.ipam.configuration;

import com.motadata.traceorg.ipam.util.TraceOrgCommonConstants;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Krunal Thakkar
 *
 */

public class TraceOrgCustomTokenConverter extends JwtAccessTokenConverter
{

	@Override
	public OAuth2AccessToken enhance(OAuth2AccessToken accessToken, OAuth2Authentication authentication) 
	{
		Map<String, Object> info = new LinkedHashMap<>(accessToken.getAdditionalInformation());

		User user=(User)authentication.getPrincipal();

		info.put(TraceOrgCommonConstants.USER,user);

		DefaultOAuth2AccessToken customAccessToken = new DefaultOAuth2AccessToken(accessToken);

		customAccessToken.setAdditionalInformation(info);

		return super.enhance(customAccessToken, authentication);
	}
}
