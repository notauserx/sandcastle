package com.sandcastle.services.product.composite.services;

import com.sandcastle.api.composite.product.*;
import com.sandcastle.api.core.product.Product;
import com.sandcastle.api.core.recommendation.Recommendation;
import com.sandcastle.api.core.review.Review;
import com.sandcastle.api.exceptions.NotFoundException;
import com.sandcastle.common.http.ServiceUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
public class ProductCompositeServiceImpl implements ProductCompositeService {
    private final ProductCompositeIntegration productCompositeIntegration;
    private final ServiceUtil serviceUtil;

    @Autowired
    public ProductCompositeServiceImpl(ProductCompositeIntegration productCompositeIntegration, ServiceUtil serviceUtil) {
        this.productCompositeIntegration = productCompositeIntegration;
        this.serviceUtil = serviceUtil;
    }

    @Override
    public ProductAggregate getProduct(int productId) {
        Product product = productCompositeIntegration.getProduct(productId);
        if (product == null) {
            throw new NotFoundException("No product found for productId: " + productId);
        }

        List<Recommendation> recommendations = productCompositeIntegration.getRecommendations(productId);

        List<Review> reviews = productCompositeIntegration.getReviews(productId);

        return createProductAggregate(product, recommendations, reviews, "some service address");
    }

    private ProductAggregate createProductAggregate(
            Product product,
            List<Recommendation> recommendations,
            List<Review> reviews,
            String serviceAddress) {

        // 1. Setup product info
        int productId = product.getProductId();
        String name = product.getName();
        int weight = product.getWeight();

        // 2. Copy summary recommendation info, if available
        List<RecommendationSummary> recommendationSummaries =
                (recommendations == null) ? null : recommendations.stream()
                        .map(r -> new RecommendationSummary(r.getRecommendationId(), r.getAuthor(), r.getRate()))
                        .collect(Collectors.toList());

        // 3. Copy summary review info, if available
        List<ReviewSummary> reviewSummaries =
                (reviews == null) ? null : reviews.stream()
                        .map(r -> new ReviewSummary(r.getReviewId(), r.getAuthor(), r.getSubject()))
                        .collect(Collectors.toList());

        // 4. Create info regarding the involved microservices addresses
        String productAddress = product.getServiceAddress();
        String reviewAddress = (reviews != null && reviews.size() > 0) ? reviews.get(0).getServiceAddress() : "";
        String recommendationAddress = (recommendations != null && recommendations.size() > 0) ? recommendations.get(0).getServiceAddress() : "";
        ServiceAddresses serviceAddresses = new ServiceAddresses(serviceAddress, productAddress, reviewAddress, recommendationAddress);

        return new ProductAggregate(productId, name, weight, recommendationSummaries, reviewSummaries, serviceAddresses);
    }
}
