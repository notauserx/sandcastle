package com.sandcastle.services.product.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.sandcastle.api.core.product.Product;
import com.sandcastle.api.core.product.ProductService;
import com.sandcastle.common.http.ServiceUtil;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProductServiceImpl implements ProductService {

    private static final Logger LOG = LoggerFactory.getLogger(ProductServiceImpl.class);
    private final ServiceUtil serviceUtil;

    public ProductServiceImpl(ServiceUtil serviceUtil) {
        this.serviceUtil = serviceUtil;
    }
    @Override
    public Product getProduct(int productId) {
        return new Product(productId, "name" + productId, 10, serviceUtil.getServiceAddress());
    }
}
