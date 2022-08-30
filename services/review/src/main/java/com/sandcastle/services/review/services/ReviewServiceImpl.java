package com.sandcastle.services.review.services;

import com.sandcastle.api.core.review.Review;
import com.sandcastle.api.core.review.ReviewService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;


@RestController
public class ReviewServiceImpl implements ReviewService {
    private static final Logger LOG = LoggerFactory.getLogger(ReviewServiceImpl.class);
    @Override
    public List<Review> getReviews(int productId) {
        if (productId == 213) {
            LOG.debug("No reviews found for productId: {}", productId);
            return new ArrayList<>();
        }

        List<Review> list = new ArrayList<>();
        list.add(new Review(productId, 1, "Author 1", "Subject 1", "Content 1", "service address 1"));
        list.add(new Review(productId, 2, "Author 2", "Subject 2", "Content 2", "service address 2"));
        list.add(new Review(productId, 3, "Author 3", "Subject 3", "Content 3", "service address 3"));

        LOG.debug("/reviews response size: {}", list.size());

        return list;
    }
}
