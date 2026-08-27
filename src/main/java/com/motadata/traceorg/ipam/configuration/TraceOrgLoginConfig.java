package com.motadata.traceorg.ipam.configuration;


import com.motadata.traceorg.ipam.logger.TraceOrgLogger;
import com.motadata.traceorg.ipam.scheduler.subnet.TraceOrgSubnetControllerExecuteJob;
import com.motadata.traceorg.ipam.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.orm.jpa.vendor.HibernateJpaSessionFactoryBean;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.persistence.EntityManagerFactory;

/**
 * @author Krunal Thakkar
 *
 */

@SuppressWarnings("deprecation")
@Configuration
@EnableWebSecurity
@Order(-20)
@RestController
public class TraceOrgLoginConfig extends WebSecurityConfigurerAdapter 
{
	private static final TraceOrgLogger _logger = new TraceOrgLogger(TraceOrgLoginConfig.class, "Login Config");

	@Resource
	private UserDetailsService userDetailsService;

	@Autowired
	public void configure(AuthenticationManagerBuilder auth) throws Exception 
	{
		try
		{
			auth.authenticationProvider(authProvider());
		}
		catch (Exception exception)
		{
			_logger.error(exception);
		}
	}
	
	@Override
	protected void configure(HttpSecurity http) throws Exception 
	{
		try
		{
			http
					.logout().logoutSuccessUrl(TraceOrgCommonConstants.LOGIN_URL).and()
					.requestMatchers()
					.antMatchers("/", TraceOrgCommonConstants.LOGIN_URL, TraceOrgCommonConstants.AUTHORIZE_URL).and()
					.authorizeRequests().anyRequest().permitAll();

			http.csrf().disable();
		}
		catch (Exception exception)
		{
			_logger.error(exception);
		}
	}

	@Bean
	public HibernateJpaSessionFactoryBean sessionFactory(EntityManagerFactory emf)
	{
		HibernateJpaSessionFactoryBean fact = new HibernateJpaSessionFactoryBean();

		fact.setEntityManagerFactory(emf);

		return fact;
	}
	
	@Bean
	public DaoAuthenticationProvider authProvider() 
	{
		final DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
	
		authProvider.setUserDetailsService(userDetailsService);

        authProvider.setPasswordEncoder(passwordEncoder());

		return authProvider;
	}
	
	@Bean
	TraceOrgCommonUtil traceOrgCommonUtil()
	{
		return new TraceOrgCommonUtil();
	}

	@Bean
	TraceOrgSubnetUtil traceOrgSubnetUtil()
	{
		return new TraceOrgSubnetUtil();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public TraceOrgCiscoDHCPServerUtil traceOrgCiscoDHCPServerUtil() {
		return new TraceOrgCiscoDHCPServerUtil();
	}

	@Bean
	public TraceOrgWindowsDhcpServerUtil traceOrgWindowsDhcpServerUtil(){return  new TraceOrgWindowsDhcpServerUtil();}

	@Bean
	TraceOrgSubnetControllerExecuteJob traceOrgSubnetControllerExecuteJob(){return new TraceOrgSubnetControllerExecuteJob();}

}
