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

	@Bean
	RestTemplate restTemplate() {
		return new RestTemplate();
	}

	@Bean
	public OpenAPI getOpenApiDocumentation() {
		return new OpenAPI()
				.components(new Components())
				.info(new Info().title("Books API").version("1")
						.license(new License().name("Apache 2.0").url("http://springdoc.org")));
	}
	public static void main(String[] args) {
		SpringApplication.run(ProductCompositeApplication.class, args);
	}

}
