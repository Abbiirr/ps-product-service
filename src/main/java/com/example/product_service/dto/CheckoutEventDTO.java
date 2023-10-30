package com.example.product_service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;

@Getter
@Setter
@AllArgsConstructor
public class CheckoutEventDTO {
    private String userId;

    //<productId, quantity>
    private HashMap<String, Integer> products;
    private Double totalPrice;

}
