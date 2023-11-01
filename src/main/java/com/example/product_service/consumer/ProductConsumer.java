package com.example.product_service.consumer;

import com.example.product_service.dto.DebitBalanceDTO;
import com.example.product_service.dto.ProductsDTO;
import com.example.product_service.entity.Product;
import com.example.product_service.enums.KafkaTopics;
import com.example.product_service.helper.KafkaMessager;
import com.example.product_service.helper.MessageToDTOConverter;
import com.example.product_service.service.ProductService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ProductConsumer {

    private final ProductService productService;

    private final KafkaMessager kafkaMessager;

    public ProductConsumer(ProductService productService, KafkaMessager kafkaMessager) {
        this.productService = productService;
        this.kafkaMessager = kafkaMessager;
    }

    @KafkaListener(topics = "deduct_products", groupId = "group_1", containerFactory = "kafkaListenerContainerFactory")
    public String deductProductListener(String message) {
        ProductsDTO paymentRequestDTO = MessageToDTOConverter.convertToProductsDTO(message);

        // Step 1: Initialize a list to store products that are not available
        List<Product> productsToDeduct = new ArrayList<>();

        Double totalPrice = 0.0;

        // Step 2: Iterate over the products in the productMap and check availability
        HashMap<String, Integer> productMap = paymentRequestDTO.getProducts();
        for (Map.Entry<String, Integer> entry : productMap.entrySet()) {
            String productId = entry.getKey();
            int requestedQuantity = entry.getValue();

            // Step 3: Check product availability
            Product product = productService.checkProductAvailablity(productId, requestedQuantity);
            totalPrice += product.getUnitPrice() * requestedQuantity;
            if (product == null) {
                // Product is not available; return an error message
                return "Product not available: " + productId;
            } else {
                // Product is available; add it to the list for deduction
                productsToDeduct.add(product);
            }
        }

        for (Map.Entry<String, Integer> entry : productMap.entrySet()) {
            String productId = entry.getKey();
            int requestedQuantity = entry.getValue();

            String response = productService.deductProduct(productId, requestedQuantity);

        }

        DebitBalanceDTO debitBalanceDTO = new DebitBalanceDTO(MessageToDTOConverter.getUserId(message),
                MessageToDTOConverter.getOrderId(message), totalPrice);


        // Step 5: Send a message to another Kafka topic
        return kafkaMessager.sendMessage(KafkaTopics.POST_DEDUCT_PRODUCTS.getTopicName(), debitBalanceDTO);


    }

}
