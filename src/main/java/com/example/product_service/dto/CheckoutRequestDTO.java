package com.example.product_service.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashMap;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CheckoutRequestDTO {
    private String userId;

    //<productId, quantity>
    private HashMap<String, Integer> products;

}
