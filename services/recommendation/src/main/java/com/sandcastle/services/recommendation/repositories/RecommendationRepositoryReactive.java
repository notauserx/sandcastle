package com.sandcastle.services.recommendation.repositories;

import com.sandcastle.services.recommendation.entities.RecommendationEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface RecommendationRepositoryReactive extends ReactiveCrudRepository<RecommendationEntity, String> {
    Flux<RecommendationEntity> findByProductId(int productId);
}