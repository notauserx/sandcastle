package com.sandcastle.services.product.composite;

import com.sandcastle.services.product.composite.services.ProductCompositeIntegration;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.health.CompositeReactiveHealthContributor;
import org.springframework.boot.actuate.health.ReactiveHealthContributor;
import org.springframework.boot.actuate.health.ReactiveHealthIndicator;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.client.RestTemplate;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.util.LinkedHashMap;
import java.util.Map;

@SpringBootApplication
@ComponentScan("com.sandcastle")
public class ProductCompositeApplication {
	private static final Logger LOG = LoggerFactory.getLogger(ProductCompositeApplication.class);


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

	private final Integer threadPoolSize;
	private final Integer taskQueueSize;

	@Autowired
	public ProductCompositeApplication(
			@Value("${app.threadPoolSize:10}") Integer threadPoolSize,
			@Value("${app.taskQueueSize:100}") Integer taskQueueSize
	) {
		this.threadPoolSize = threadPoolSize;
		this.taskQueueSize = taskQueueSize;
	}

	@Bean
	public Scheduler publishEventScheduler() {
		LOG.info("Creates a messagingScheduler with connectionPoolSize = {}", threadPoolSize);
		return Schedulers.newBoundedElastic(threadPoolSize, taskQueueSize, "publish-pool");
	}



	/*
	@Autowired
	ProductCompositeIntegration integration;

	@Bean
	ReactiveHealthContributor coreServices() {

		final Map<String, ReactiveHealthIndicator> registry = new LinkedHashMap<>();

		registry.put("product", () -> integration.getProductHealth());
		registry.put("recommendation", () -> integration.getRecommendationHealth());
		registry.put("review", () -> integration.getReviewHealth());

		return CompositeReactiveHealthContributor.fromMap(registry);
	}
	*/

	public static void main(String[] args) {
		SpringApplication.run(ProductCompositeApplication.class, args);
	}

}
