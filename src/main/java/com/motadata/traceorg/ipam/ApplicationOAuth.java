package com.motadata.traceorg.ipam;

import com.motadata.traceorg.ipam.logger.TraceOrgLogger;
import com.motadata.traceorg.ipam.util.TraceOrgCommonConstants;
import com.motadata.traceorg.ipam.util.TraceOrgCommonUtil;
import com.motadata.traceorg.ipam.util.TraceOrgConfigUtil;
import com.motadata.traceorg.ipam.util.TraceOrgInitServlet;
import org.flywaydb.core.Flyway;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.sql.DataSource;
import java.security.cert.X509Certificate;
import java.util.Properties;

/**
 * @author Krunal Thakkar
 *
 */

@SpringBootApplication
@EnableAuthorizationServer
@Configuration
@EnableCaching
@EnableTransactionManagement
@EnableJpaAuditing
public class ApplicationOAuth extends SpringBootServletInitializer implements CommandLineRunner
{
	private static final TraceOrgLogger _logger = new TraceOrgLogger(ApplicationOAuth.class,"Application OAuth");

	// It will not verify ssl certificate in Rest Templatea

	static {
		try
		{
			disableSslVerification();
		}
		catch (Exception exception)
		{
			_logger.error(exception);
		}
	}

	private static void disableSslVerification()
	{
		try
		{
			// Create a trust manager that does not validate certificate chains
			TrustManager[] trustAllCerts = new TrustManager[] {new X509TrustManager()
			{
				public java.security.cert.X509Certificate[] getAcceptedIssuers()
				{
					return null;
				}

				public void checkClientTrusted(X509Certificate[] certs, String authType)
				{
				}

				public void checkServerTrusted(X509Certificate[] certs, String authType)
				{
				}
			}
			};

			// Install the all-trusting trust manager
			SSLContext sc = SSLContext.getInstance("SSL");

			sc.init(null, trustAllCerts, new java.security.SecureRandom());

			HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

			// Create all-trusting host name verifier
			HostnameVerifier allHostsValid = new HostnameVerifier()
			{
				public boolean verify(String hostname, SSLSession session) {
					return true;
				}
			};

			// Install the all-trusting host verifier
			HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);

		}
		catch (Exception exception)
		{
			_logger.error(exception);
		}
	}

	@Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application)
	{
        return application.sources(ApplicationOAuth.class);
    }

	public static void main(String[] args)
	{
		try
		{
			SpringApplication.run(new Class[]{ApplicationOAuth.class,TraceOrgInitServlet.class}, args);
		}
		catch (Exception exception)
		{
			_logger.fatal("Application not started");
			
			_logger.error(exception);
		}
    }

	public void run(String... args) {
		_logger.info("DataSource = " + getDataSource());
	}

	@Bean(name = "dataSource")
	public DataSource getDataSource() {
		org.apache.tomcat.jdbc.pool.DataSource dataSource = (org.apache.tomcat.jdbc.pool.DataSource) DataSourceBuilder
				.create().username("root").password(TraceOrgCommonUtil.decrypt("ba03YfDjVoJ3NELSbea67w=="))
				.url("jdbc:mysql://" + TraceOrgConfigUtil.getDatabaseHost() + ":" + TraceOrgConfigUtil.getDatabasePort() + "/ipam?max-connections=1000&createDatabaseIfNotExist=true&useSSL=false&autoReconnect=true")
				.driverClassName("com.mysql.jdbc.Driver")
				.build();

		dataSource.setMaxActive(10);
		dataSource.setInitialSize(5);
		dataSource.setMaxIdle(5);
		dataSource.setMinIdle(3);
		dataSource.setTestWhileIdle(true);
		dataSource.setTestOnBorrow(true);
		dataSource.setValidationQuery("SELECT 1");
		dataSource.setTimeBetweenEvictionRunsMillis(1800000);
		dataSource.setMinEvictableIdleTimeMillis(1800000);
		return dataSource;
	}

	@Bean(name = "flyway")
	public Flyway flyway()
	{
		Flyway flyway = new Flyway();

		flyway.setDataSource(getDataSource());

		flyway.setBaselineOnMigrate(true);

		Properties properties = new Properties();

		properties.setProperty("flyway.user","root");

		properties.setProperty("flyway.password", TraceOrgCommonUtil.decrypt("ba03YfDjVoJ3NELSbea67w=="));

		flyway.configure(properties);

		flyway.setLocations("classpath:db/migration");

		flyway.setSchemas("ipam");

		return flyway;
	}

	@Bean
	public EmbeddedServletContainerCustomizer containerCustomizer()
    {
		return (container -> {container.setPort(Integer.parseInt(TraceOrgCommonConstants.SERVER_PORT));});
	}
}
