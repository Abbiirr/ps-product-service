package com.example.product_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DebitBalanceDTO {
    private String userId;
    private String orderId;
    private Double totalPrice;
}
