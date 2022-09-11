package com.sandcastle.services.recommendation.services;

import com.sandcastle.api.core.recommendation.Recommendation;
import com.sandcastle.api.core.recommendation.RecommendationService;
import com.sandcastle.api.exceptions.InvalidInputException;
import com.sandcastle.common.http.ServiceUtil;
import com.sandcastle.services.recommendation.entities.RecommendationEntity;
import com.sandcastle.services.recommendation.repositories.RecommendationRepository;
import com.sandcastle.services.recommendation.repositories.RecommendationRepositoryReactive;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

import static java.util.logging.Level.FINE;

@RestController
public class RecommendationServiceImpl implements RecommendationService {
    private static final Logger LOG = LoggerFactory.getLogger(RecommendationServiceImpl.class);

    private final RecommendationRepositoryReactive repository;

    private final RecommendationMapper mapper;

    private final ServiceUtil serviceUtil;

    @Autowired
    public RecommendationServiceImpl(RecommendationRepositoryReactive repository,
                                     RecommendationMapper mapper,
                                     ServiceUtil serviceUtil) {
        this.repository = repository;
        this.mapper = mapper;
        this.serviceUtil = serviceUtil;
    }

    @Override
    public Mono<Recommendation> createRecommendation(Recommendation body) {

        if (body.getProductId() < 1) {
            throw new InvalidInputException("Invalid productId: " + body.getProductId());
        }

        RecommendationEntity entity = mapper.apiToEntity(body);
        Mono<Recommendation> newEntity = repository.save(entity)
                .log(LOG.getName(), FINE)
                .onErrorMap(
                        DuplicateKeyException.class,
                        ex -> new InvalidInputException("Duplicate key, Product Id: " + body.getProductId() + ", Recommendation Id:" + body.getRecommendationId()))
                .map(e -> mapper.entityToApi(e));

        return newEntity;
    }

    @Override
    public Flux<Recommendation> getRecommendations(int productId) {

        if (productId < 1) {
            throw new InvalidInputException("Invalid productId: " + productId);
        }

        LOG.info("Will get recommendations for product with id={}", productId);

        return repository.findByProductId(productId)
                .log(LOG.getName(), FINE)
                .map(e -> mapper.entityToApi(e))
                .map(e -> setServiceAddress(e));
    }

    @Override
    public Mono<Void> deleteRecommendations(int productId) {

        if (productId < 1) {
            throw new InvalidInputException("Invalid productId: " + productId);
        }

        LOG.debug("deleteRecommendations: tries to delete recommendations for the product with productId: {}", productId);
        return repository.deleteAll(repository.findByProductId(productId));
    }

    private Recommendation setServiceAddress(Recommendation e) {
        e.setServiceAddress(serviceUtil.getServiceAddress());
        return e;
    }
}
