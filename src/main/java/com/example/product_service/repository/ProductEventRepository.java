package com.example.product_service.repository;

import com.example.product_service.entity.ProductEvent;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductEventRepository extends CrudRepository<ProductEvent, String> {}
