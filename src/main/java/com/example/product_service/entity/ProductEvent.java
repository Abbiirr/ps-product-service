package com.example.product_service.entity;


import com.example.product_service.enums.EventStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

@RedisHash("ProductEvent")
@RequiredArgsConstructor
@AllArgsConstructor
@Data
@EntityScan
@Builder
public class ProductEvent {
    @Id
    private String productEventId;
    private String eventId;
    private int eventStep;
    private EventStatus eventStatus;

}
