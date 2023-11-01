package com.example.product_service.helper;

import com.example.product_service.dto.ProductsDTO;
import org.springframework.stereotype.Component;

@Component
public class MessageToDTOConverter {


    public static ProductsDTO convertToProductsDTO(String message) {
        return new ProductsDTO();
    }
    public static String getUserId(String message) {
        return "User Id";
    }

    public static String getOrderId(String message) {
        return "Order Id";
    }
}
