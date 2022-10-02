package com.sandcastle.services.product;

import com.sandcastle.api.core.product.Product;
import com.sandcastle.api.event.Event;
import com.sandcastle.api.exceptions.InvalidInputException;
import com.sandcastle.services.product.repositories.ProductRepositoryReactive;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.function.Consumer;

import static com.sandcastle.api.event.Event.Type.CREATE;
import static com.sandcastle.api.event.Event.Type.DELETE;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@SpringBootTest(webEnvironment = RANDOM_PORT,
		properties = {"eureka.client.enabled=false"})
class ProductApplicationTests extends MongoDbTestBase{

	@Autowired private WebTestClient client;
	@Autowired private ProductRepositoryReactive repository;
	@Autowired
	@Qualifier("messageProcessor")
	private Consumer<Event<Integer, Product>> messageProcessor;

	@BeforeEach
	void setupDb() {
		repository.deleteAll().block();
	}

	@Test
	void contextLoads() {
	}

	@Test
	void canGetProductById() {
		int productId = 1;

		assertNull(repository.findByProductId(productId).block());
		assertEquals(0, (long)repository.count().block());

		sendCreateProductEvent(productId);

		assertNotNull(repository.findByProductId(productId).block());
		assertEquals(1, (long)repository.count().block());

		getAndVerifyProduct(productId, OK)
				.jsonPath("$.productId").isEqualTo(productId);

	}

	@Test
	void getsDuplicateErrorWhenTryToAddProductWithNonUniqueId() {
		int productId = 1;

		assertNull(repository.findByProductId(productId).block());

		sendCreateProductEvent(productId);

		assertNotNull(repository.findByProductId(productId).block());

		InvalidInputException thrown = assertThrows(
				InvalidInputException.class,
				() -> sendCreateProductEvent(productId),
				"Expected a InvalidInputException here!");
		assertEquals("Duplicate key, Product Id: " + productId, thrown.getMessage());

	}

	@Test
	void deleteProduct() {

		int productId = 10;

		sendCreateProductEvent(productId);
		assertNotNull(repository.findByProductId(productId).block());

		sendDeleteProductEvent(productId);
		assertNull(repository.findByProductId(productId).block());

		sendDeleteProductEvent(productId);

	}

	@Test
	void getProductNotFound() {

		int productIdNotFound = 13;

		client.get()
				.uri("/product/" + productIdNotFound)
				.accept(APPLICATION_JSON)
				.exchange()
				.expectStatus().isNotFound()
				.expectHeader().contentType(APPLICATION_JSON)
				.expectBody()
				.jsonPath("$.path").isEqualTo("/product/" + productIdNotFound)
				.jsonPath("$.message").isEqualTo("No product found for productId: " + productIdNotFound);
	}


	private WebTestClient.BodyContentSpec getAndVerifyProduct(int productId, HttpStatus expectedStatus) {
		return getAndVerifyProduct("/" + productId, expectedStatus);
	}

	private WebTestClient.BodyContentSpec getAndVerifyProduct(String productIdPath, HttpStatus expectedStatus) {
		return client.get()
				.uri("/product" + productIdPath)
				.accept(APPLICATION_JSON)
				.exchange()
				.expectStatus().isEqualTo(expectedStatus)
				.expectHeader().contentType(APPLICATION_JSON)
				.expectBody();
	}

	private void sendCreateProductEvent(int productId) {
		Product product = new Product(productId, "Name " + productId, productId, "SA");
		Event<Integer, Product> event = new Event(CREATE, productId, product);
		messageProcessor.accept(event);
	}

	private void sendDeleteProductEvent(int productId) {
		Event<Integer, Product> event = new Event(DELETE, productId, null);
		messageProcessor.accept(event);
	}
}
