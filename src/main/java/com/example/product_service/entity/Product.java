package com.example.product_service.entity;

import lombok.*;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

@RedisHash("Student")
@RequiredArgsConstructor
@AllArgsConstructor
@Data
@EntityScan
@Builder
public class Product {
    @Id
    private String productId;
    private String orderId;
    private String userId;
    private String status;
    private Integer quantity;
    private Double unitPrice;


    public Product(String productId, int quantity) {
        this.productId = productId;
        this.quantity = quantity;
    }
}
