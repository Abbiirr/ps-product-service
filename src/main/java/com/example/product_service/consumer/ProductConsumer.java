package com.example.product_service.consumer;

import com.example.product_service.dto.ProductsDTO;
import com.example.product_service.entity.Product;
import com.example.product_service.enums.KafkaTopics;
import com.example.product_service.helper.EventFinder;
import com.example.product_service.helper.KafkaMessager;
import com.example.product_service.helper.MessageToDTOConverter;
import com.example.product_service.service.ProductService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ProductConsumer {

    private final ProductService productService;

    private final KafkaMessager kafkaMessager;
        private final Set<String> deductProductEvents = ConcurrentHashMap.newKeySet();
        private final Set<String> addProductEvents = ConcurrentHashMap.newKeySet();

        private final EventFinder eventFinder;


    public ProductConsumer(ProductService productService, KafkaMessager kafkaMessager, EventFinder eventFinder) {
        this.productService = productService;
        this.kafkaMessager = kafkaMessager;
        this.eventFinder = eventFinder;
    }

    @KafkaListener(topics = "deduct_products", groupId = "group_1", containerFactory = "kafkaListenerContainerFactory")
    public String deductProductListener(String message, Acknowledgment acknowledgment) {
//        ProductsDTO paymentRequestDTO = MessageToDTOConverter.convertToProductsDTO(message);

        String eventId = MessageToDTOConverter.getField(message, "eventId");
        if(eventId == null){
            acknowledgment.acknowledge();
            return "No event id";
        }
        if(eventFinder.findDuplicateOrNot(eventId, "deductProductListener")){
            acknowledgment.acknowledge();
            return "Duplicate event";
        }
        List<Product> productsToDeduct = new ArrayList<>();

        Double totalPrice = 0.0;

        // Step 2: Iterate over the products in the productMap and check availability
        HashMap<String, Integer> productMap = MessageToDTOConverter.getProductsMapFromMessage(message, "products");
        if(productMap == null){
            message = MessageToDTOConverter.setField(message, "status", "fail");
            String response = kafkaMessager.sendMessage(KafkaTopics.POST_DEDUCT_PRODUCTS.getTopicName(), message);
            acknowledgment.acknowledge();
            return "No products found";
        }
        if (productMap == null) {
            message = MessageToDTOConverter.setField(message, "status", "fail");
            String response = kafkaMessager.sendMessage(KafkaTopics.POST_DEDUCT_PRODUCTS.getTopicName(), message);
            acknowledgment.acknowledge();
            return "No products found";
        }
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

//        DebitBalanceDTO debitBalanceDTO = new DebitBalanceDTO(MessageToDTOConverter.getUserId(message),
//                MessageToDTOConverter.getOrderId(message), totalPrice);

        message = MessageToDTOConverter.addPriceToMessage(message, totalPrice);
        // Step 5: Send a message to another Kafka topic
        String response = kafkaMessager.sendMessage(KafkaTopics.POST_DEDUCT_PRODUCTS.getTopicName(), message);
        acknowledgment.acknowledge();
        return response;


    }

    @KafkaListener(topics = "add_products", groupId = "group_1", containerFactory = "kafkaListenerContainerFactory")
    public String addProductListener(String message, Acknowledgment acknowledgment) {
        String eventId = MessageToDTOConverter.getField(message, "eventId");
        if(eventId == null){
            acknowledgment.acknowledge();
            return "No event id";
        }
        if(eventFinder.findDuplicateOrNot(eventId, "addProductListener")){
            acknowledgment.acknowledge();
            return "Duplicate event";
        }
        ProductsDTO paymentRequestDTO = MessageToDTOConverter.convertToProductsDTO(message);

        // Step 1: Initialize a list to store products that are not available
        List<Product> productsToDeduct = new ArrayList<>();

        // Step 2: Iterate over the products in the productMap and check availability
        HashMap<String, Integer> productMap = paymentRequestDTO.getProducts();


        for (Map.Entry<String, Integer> entry : productMap.entrySet()) {
            String productId = entry.getKey();
            int requestedQuantity = entry.getValue();

            String response = productService.addProduct(productId, requestedQuantity);

        }


        // Step 5: Send a message to another Kafka topic
        String response = kafkaMessager.sendMessage(KafkaTopics.POST_ADD_PRODUCTS.getTopicName(), message);
        acknowledgment.acknowledge();
        return response;


    }

}
