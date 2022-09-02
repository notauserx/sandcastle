package com.sandcastle.services.product.composite;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@ComponentScan("com.sandcastle")
public class ProductCompositeApplication {

	@Value("${api.common.version}") String apiVersion;
	@Value("${api.common.title}") String apiTitle;
	@Value("${api.common.description}") String apiDescription;
	@Value("${api.common.license}") String apiLicense;
	@Value("${api.common.licenseUrl}") String apiLicenseUrl;
	@Bean
	RestTemplate restTemplate() {
		return new RestTemplate();
	}

	@Bean
	public OpenAPI getOpenApiDocumentation() {
		return new OpenAPI()
				.components(new Components())
				.info(new Info().title(apiTitle).version(apiVersion).description(apiDescription)
						.license(new License().name(apiLicense).url(apiLicenseUrl)));
	}
	public static void main(String[] args) {
		SpringApplication.run(ProductCompositeApplication.class, args);
	}

}
