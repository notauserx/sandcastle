package com.sandcastle.services.product.services;

import com.sandcastle.api.core.product.Product;
import com.sandcastle.api.core.product.ProductService;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProductServiceImpl implements ProductService {
    @Override
    public Product getProduct(int productId) {
        return new Product(productId, "name" + productId, 10, "someserviceaddress");
    }
}
