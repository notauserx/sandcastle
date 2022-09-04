package com.sandcastle.services.recommendation.repositories;

import java.util.List;
import org.springframework.data.repository.CrudRepository;
import com.sandcastle.services.recommendation.entities.RecommendationEntity;


public interface RecommendationRepository extends CrudRepository<RecommendationEntity, String> {
    List<RecommendationEntity> findByProductId(int productId);
}