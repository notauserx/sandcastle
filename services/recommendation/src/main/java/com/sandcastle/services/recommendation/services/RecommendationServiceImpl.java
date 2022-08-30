package com.sandcastle.services.recommendation.services;

import com.sandcastle.api.core.recommendation.Recommendation;
import com.sandcastle.api.core.recommendation.RecommendationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
public class RecommendationServiceImpl implements RecommendationService {
    private static final Logger LOG = LoggerFactory.getLogger(RecommendationServiceImpl.class);
    @Override
    public List<Recommendation> getRecommendations(int productId) {
        if (productId == 113) {
            LOG.debug("No recommendations found for productId: {}", productId);
            return new ArrayList<>();
        }

        List<Recommendation> list = new ArrayList<>();
        list.add(new Recommendation(productId, 1, "Author 1", 1, "Content 1", "address 1"));
        list.add(new Recommendation(productId, 2, "Author 2", 2, "Content 2", "address 2"));
        list.add(new Recommendation(productId, 3, "Author 3", 3, "Content 3", "address 3"));

        LOG.debug("/recommendation response size: {}", list.size());

        return list;
    }
}
