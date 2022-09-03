package com.sandcastle.services.product.repositories;

import com.sandcastle.services.product.entities.ProductEntity;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.Optional;

public interface ProductRepository extends PagingAndSortingRepository<ProductEntity, String> {
  Optional<ProductEntity> findByProductId(int productId);
}
