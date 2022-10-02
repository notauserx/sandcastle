package com.sandcastle.services.product.composite.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sandcastle.api.core.product.Product;
import com.sandcastle.api.core.product.ProductService;
import com.sandcastle.api.core.recommendation.Recommendation;
import com.sandcastle.api.core.recommendation.RecommendationService;
import com.sandcastle.api.core.review.Review;
import com.sandcastle.api.core.review.ReviewService;
import com.sandcastle.api.event.Event;
import com.sandcastle.api.exceptions.*;
import com.sandcastle.common.http.HttpErrorInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

import java.io.IOException;

import static com.sandcastle.api.event.Event.Type.CREATE;
import static com.sandcastle.api.event.Event.Type.DELETE;
import static java.util.logging.Level.FINE;
import static reactor.core.publisher.Flux.empty;

@Component
public class ProductCompositeIntegration
        implements ProductService, RecommendationService, ReviewService {
    private static final Logger LOG = LoggerFactory.getLogger(ProductCompositeIntegration.class);

    private final WebClient webClient;
    private final ObjectMapper mapper;

    private final StreamBridge streamBridge;

    private final Scheduler publishEventScheduler;

    private final String productServiceUrl;
    private final String recommendationServiceUrl;
    private final String reviewServiceUrl;

    @Autowired
    public ProductCompositeIntegration(
            @Qualifier("publishEventScheduler") Scheduler publishEventScheduler,

            WebClient.Builder webClient,
            ObjectMapper mapper,
            StreamBridge streamBridge) {

        this.publishEventScheduler = publishEventScheduler;

        this.webClient = webClient.build();
        this.mapper = mapper;
        this.streamBridge = streamBridge;

        productServiceUrl        = "http://product";
        recommendationServiceUrl = "http://recommendation";
        reviewServiceUrl         = "http://review";

        LOG.info("PRODUCT URL : " + productServiceUrl);
        LOG.info("REVIEW URL : " + reviewServiceUrl);
        LOG.info("RECOMMENDATION URL : " + recommendationServiceUrl);
    }


    @Override
    public Mono<Product> createProduct(Product body) {
        return Mono
                .fromCallable(() -> {
                    sendMessage("products-out-0",
                        new Event(CREATE, body.getProductId(), body));
                    return body;
                })
                .subscribeOn(publishEventScheduler);
    }

    @Override
    public Mono<Product> getProduct(int productId) {
        String url = productServiceUrl + "/product/" + productId;
        return webClient.get().uri(url).retrieve()
                .bodyToMono(Product.class)
                .log(LOG.getName(), FINE)
                .onErrorMap(WebClientResponseException.class,
                        ex -> handleException(ex)
                );
    }

    @Override
    public Mono<Void> deleteProduct(int productId) {

        return Mono
                .fromRunnable(() ->
                        sendMessage("products-out-0",
                                new Event(DELETE, productId, null)))
                .subscribeOn(publishEventScheduler)
                .then();
    }


    @Override
    public Mono<Recommendation> createRecommendation(Recommendation body) {

        return Mono
                .fromCallable(() -> {
                    sendMessage("recommendations-out-0",
                            new Event(CREATE, body.getProductId(), body));
                    return body;

                })
                .subscribeOn(publishEventScheduler);

    }

    @Override
    public Flux<Recommendation> getRecommendations(int productId) {

        String url = recommendationServiceUrl + "/recommendation?productId=" + productId;
        // Return an empty result if something goes wrong to make it
        // possible for the composite service to return partial responses
        return webClient.get().uri(url).retrieve()
                .bodyToFlux(Recommendation.class)
                .log(LOG.getName(), FINE)
                .onErrorResume(error -> empty());
    }

    @Override
    public Mono<Void> deleteRecommendations(int productId) {
        return Mono
                .fromRunnable(() ->
                        sendMessage("recommendations-out-0",
                                new Event(DELETE, productId, null)))
                .subscribeOn(publishEventScheduler)
                .then();
    }


    @Override
    public Mono<Review> createReview(Review body) {
        return Mono
                .fromCallable(() -> {
                    sendMessage("reviews-out-0",
                            new Event(CREATE, body.getProductId(), body));
                    return body;
                })
                .subscribeOn(publishEventScheduler);
    }

    @Override
    public Flux<Review> getReviews(int productId) {

        String url = reviewServiceUrl + "/review?productId=" + productId;

        LOG.debug("Will call the getReviews API on URL: {}", url);

        // Return an empty result if something goes wrong to make it possible
        // for the composite service to return partial responses
        return webClient.get()
                .uri(url)
                .retrieve()
                .bodyToFlux(Review.class)
                .log(LOG.getName(), FINE)
                .onErrorResume(error -> empty());
    }

    @Override
    public Mono<Void> deleteReviews(int productId) {
        return Mono
                .fromRunnable(() ->
                    sendMessage("reviews-out-0",
                            new Event(DELETE, productId, null)))
                .subscribeOn(publishEventScheduler)
                .then();
    }

    private void sendMessage(String bindingName, Event event) {
        LOG.debug("Sending a {} message to {}", event.getEventType(), bindingName);
        Message message = MessageBuilder.withPayload(event)
                .setHeader("partitionKey", event.getKey())
                .build();
        streamBridge.send(bindingName, message);
    }

    private Throwable handleException(Throwable ex) {

        if (!(ex instanceof WebClientResponseException)) {
            LOG.warn("Got a unexpected error: {}, will rethrow it", ex.toString());
            return ex;
        }

        WebClientResponseException wcre = (WebClientResponseException)ex;

        switch (wcre.getStatusCode()) {

            case NOT_FOUND:
                return new NotFoundException(getErrorMessage(wcre));

            case UNPROCESSABLE_ENTITY :
                return new InvalidInputException(getErrorMessage(wcre));

            default:
                LOG.warn("Got an unexpected HTTP error: {}, will rethrow it", wcre.getStatusCode());
                LOG.warn("Error body: {}", wcre.getResponseBodyAsString());
                return ex;
        }
    }

    private String getErrorMessage(WebClientResponseException ex) {
        try {
            return mapper.readValue(ex.getResponseBodyAsString(), HttpErrorInfo.class).getMessage();
        } catch (IOException ioex) {
            return ex.getMessage();
        }
    }

    public Mono<Health> getProductHealth() {
        return getHealth(productServiceUrl);
    }

    public Mono<Health> getRecommendationHealth() {
        return getHealth(recommendationServiceUrl);
    }

    public Mono<Health> getReviewHealth() {
        return getHealth(reviewServiceUrl);
    }

    private Mono<Health> getHealth(String url) {
        url += "/actuator/health";
        LOG.debug("Will call the Health API on URL: {}", url);
        return webClient.get().uri(url).retrieve().bodyToMono(String.class)
                .map(s -> new Health.Builder().up().build())
                .onErrorResume(ex -> Mono.just(new Health.Builder().down(ex).build()))
                .log(LOG.getName(), FINE);
    }
}
