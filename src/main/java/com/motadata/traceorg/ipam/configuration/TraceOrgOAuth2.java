
package com.motadata.traceorg.ipam.configuration;

import com.motadata.traceorg.ipam.util.TraceOrgCommonConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.InMemoryTokenStore;
import org.springframework.security.oauth2.provider.token.store.JdbcTokenStore;

import javax.sql.DataSource;
import java.util.concurrent.TimeUnit;

/**
 * @author Krunal Thakkar
 *
 */

@SuppressWarnings("ALL")
@Configuration
public class TraceOrgOAuth2 extends AuthorizationServerConfigurerAdapter 
{

	@Autowired
	private AuthenticationManager authenticationManager;

	@Autowired
	@Qualifier("dataSource")
	private DataSource dataSource;

	@Override
	public void configure(AuthorizationServerSecurityConfigurer oauthServer) throws Exception 
	{
		oauthServer.tokenKeyAccess("permitAll()").checkTokenAccess("isAuthenticated()");
	}

	@Override
	public void configure(AuthorizationServerEndpointsConfigurer configurer) throws Exception 
	{
		configurer.authenticationManager(authenticationManager).tokenStore(tokenStore()).

		accessTokenConverter(accessTokenConverter());
	}
	
	@Override
	public void configure(ClientDetailsServiceConfigurer clients) throws Exception 
	{
		clients.inMemory().withClient(TraceOrgCommonConstants.CLIENT_KEY).secret(TraceOrgCommonConstants.SECRET_KEY)
				.scopes(TraceOrgCommonConstants.READ, TraceOrgCommonConstants.WRITE).autoApprove(true).autoApprove(".*")
				.authorizedGrantTypes(TraceOrgCommonConstants.PASSWORD, TraceOrgCommonConstants.REFRESH_TOKEN, TraceOrgCommonConstants.AUTHORIZATION_CODE);
	}

	@Bean
	public TraceOrgCustomTokenConverter accessTokenConverter() 
	{
		return new TraceOrgCustomTokenConverter();
	}
	
	@Bean
	public TokenStore tokenStore() 
	{
		return new JdbcTokenStore(dataSource);
	}
	
	@Bean
	@Primary
    public DefaultTokenServices resourceServerTokenServices() 
	{
        DefaultTokenServices tokenServices = new DefaultTokenServices();
        
        tokenServices.setSupportRefreshToken(true);
        
        tokenServices.setTokenStore(tokenStore());
        
        return tokenServices;
    }

	static void setValidationQuery(DataSource dataSource) {
		if (dataSource instanceof org.apache.tomcat.jdbc.pool.DataSource) {
			org.apache.tomcat.jdbc.pool.DataSource ds = (org.apache.tomcat.jdbc.pool.DataSource) dataSource;
			ds.setValidationQuery("SELECT 1");
			ds.setTestOnBorrow(true);
			ds.setTestWhileIdle(true);
			ds.setValidationInterval(TimeUnit.MINUTES.toMillis(1));
			ds.setTimeBetweenEvictionRunsMillis((int) TimeUnit.MINUTES.toMillis(5));
		}
	}
}
