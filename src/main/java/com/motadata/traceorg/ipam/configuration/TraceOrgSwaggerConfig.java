package com.motadata.traceorg.ipam.configuration;

import static springfox.documentation.builders.PathSelectors.regex;

import java.util.Collections;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.ApiKey;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * @author Krunal Thakkar
 *
 */

@Configuration
@EnableSwagger2
public class TraceOrgSwaggerConfig
{

	ApiInfo apiInfo()
    {
		return new ApiInfoBuilder().title("IPAM").description("IPAM").license("Apache 2.0")
				.licenseUrl("http://www.apache.org/licenses/LICENSE-2.0.html")
				.termsOfServiceUrl("http://IPAM.com/")
				.build();
	}

	@Bean
	public Docket api()
    {
		return new Docket(DocumentationType.SWAGGER_2).select().apis(RequestHandlerSelectors.basePackage("com.motadata.traceorg.ipam.restcontroller"))
				.paths(regex("/.*")).build().enable(true).apiInfo(apiInfo())
				.securitySchemes(Collections.singletonList(apiKey()));
	}
	
	private ApiKey apiKey()
    {
		return new ApiKey("accessToken", "accessToken", "header");
	}
}