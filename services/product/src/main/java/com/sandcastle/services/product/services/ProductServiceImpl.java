package com.sandcastle.services.product.services;

import com.sandcastle.api.exceptions.InvalidInputException;
import com.sandcastle.api.exceptions.NotFoundException;
import com.sandcastle.services.product.entities.ProductEntity;
import com.sandcastle.services.product.repositories.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.sandcastle.api.core.product.Product;
import com.sandcastle.api.core.product.ProductService;
import com.sandcastle.common.http.ServiceUtil;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProductServiceImpl implements ProductService {

    private static final Logger LOG = LoggerFactory.getLogger(ProductServiceImpl.class);
    private final ServiceUtil serviceUtil;

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    public ProductServiceImpl(ServiceUtil serviceUtil,
                              ProductRepository productRepository,
                              ProductMapper productMapper) {
        this.serviceUtil = serviceUtil;
        this.productRepository = productRepository;
        this.productMapper = productMapper;
    }

    @Override
    public Product createProduct(Product body) {
        try{
            ProductEntity entity = productMapper.apiToEntity(body);
            ProductEntity newEntity = productRepository.save(entity);

            LOG.debug("createProduct : entity created for product id : " + body.getProductId());

            return productMapper.entityToApi(newEntity);
        } catch (DuplicateKeyException dke) {
            throw new InvalidInputException("A product with id " + body.getProductId() + " already exists.");
        }
    }

    @Override
    public Product getProduct(int productId) {
        if (productId < 1) {
            throw new InvalidInputException("Invalid productId: " + productId);
        }

        ProductEntity productEntity = productRepository
                .findByProductId(productId)
                .orElseThrow(() -> new NotFoundException("No product found for productId : " + productId));

        Product response = productMapper.entityToApi(productEntity);
        response.setServiceAddress(serviceUtil.getServiceAddress());

        LOG.debug("getProduct: found productId: {}", response.getProductId());

        return response;
    }

    @Override
    public void deleteProduct(int productId) {
        LOG.debug("deleteProduct: tries to delete an entity with productId: {}", productId);
        productRepository
                .findByProductId(productId)
                .ifPresent(e -> productRepository.delete(e));
    }
}
