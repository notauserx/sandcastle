package com.sandcastle.services.product.services;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import com.sandcastle.api.core.product.Product;
import com.sandcastle.services.product.entities.ProductEntity;

@Mapper(componentModel = "spring")
public interface ProductMapper {

  @Mappings({
    @Mapping(target = "serviceAddress", ignore = true)
  })
  Product entityToApi(ProductEntity entity);

  @Mappings({
    @Mapping(target = "version", ignore = true)
  })
  ProductEntity apiToEntity(Product api);
}
