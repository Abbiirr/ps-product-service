package com.example.product_service.service;

import com.example.product_service.dto.CheckoutEventDTO;
import com.example.product_service.dto.CheckoutRequestDTO;
import com.example.product_service.dto.ProductRequestDTO;
import com.example.product_service.dto.ProductResponseDTO;
import com.example.product_service.entity.Product;
import com.example.product_service.enums.KafkaTopics;
import com.example.product_service.enums.ProductStatus;
import com.example.product_service.helper.KafkaMessageFormatter;
import com.example.product_service.repository.ProductRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.annotation.PostConstruct;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class ProductService {

    private Map<String, Integer> productMap;
    private final ProductRepository productRepository;


    private final KafkaTemplate<String, String> kafkaTemplate;

    public ProductService(ProductRepository productRepository, KafkaTemplate<String, String> kafkaTemplate) {
        this.productRepository = productRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @PostConstruct
    private void init() {
        productMap = new HashMap<>();
        List<Product> products = StreamSupport
                .stream(productRepository.findAll().spliterator(), false)
                .toList();
        for (Product product : products) {
            productMap.put(product.getProductId(), product.getQuantity());
        }
    }

    public List<Product> getAllProducts() {
        Iterable<Product> products = productRepository.findAll();
        return StreamSupport.stream(products.spliterator(), false)
                .collect(Collectors.toList());

    }

    public ProductResponseDTO deductProduct(final ProductRequestDTO requestDTO) {
        String productId = requestDTO.getProductId();
        int requestedQuantity = requestDTO.getQuantity();

        Product product = productRepository.findById(productId).orElse(new Product(productId, 0));
        int availableQuantity = product.getQuantity();

        ProductResponseDTO responseDTO = new ProductResponseDTO();
        responseDTO.setOrderId(requestDTO.getOrderId());
        responseDTO.setUserId(requestDTO.getUserId());
        responseDTO.setProductId(productId);
        responseDTO.setStatus(ProductStatus.UNAVAILABLE);

        if (availableQuantity >= requestedQuantity) {
            responseDTO.setStatus(ProductStatus.AVAILABLE);
            product.setQuantity(availableQuantity - requestedQuantity);
            productRepository.save(product);
        }

        return responseDTO;
    }

    public void addProduct(final ProductRequestDTO requestDTO) {
        String productId = requestDTO.getProductId();
        Product product = productRepository.findById(productId).orElse(Product.builder()
                .productId(requestDTO.getProductId())
                .userId(requestDTO.getUserId())
                .quantity(0)
                .build());
        product.setQuantity(product.getQuantity() + requestDTO.getQuantity());
        product.setUnitPrice(requestDTO.getUnitPrice());
        productRepository.save(product);


    }

    public String checkout(CheckoutRequestDTO requestDTO) {
        //TODO: check if user exists: can't do from this service
        List<Product> products = this.productsAvailable(requestDTO.getProducts());
        if (products == null) {
            return "Products not available";
        }
        double totalPrice = this.calculateTotalPrice(requestDTO.getProducts(), products);


        //TODO: if all checks pass, publish to kafka
        CheckoutEventDTO eventDTO = new CheckoutEventDTO(requestDTO.getUserId(), requestDTO.getProducts(), totalPrice);

        return this.sendMessage(eventDTO);
    }

    private List<Product> productsAvailable(Map<String, Integer> products) {
        // Implement logic to check if products exist and have sufficient quantity
        // You might use the productRepository or a product service for this

        List<Product> availableProducts = new ArrayList<>();

        for (Map.Entry<String, Integer> entry : products.entrySet()) {
            String productId = entry.getKey();
            int quantity = entry.getValue();

            Product product = productRepository.findById(productId).orElse(null);

            if (product == null || product.getQuantity() < quantity) {
                return null; // Return null if any product doesn't meet the criteria
            }

            availableProducts.add(product);
        }

        return availableProducts;
    }


    private double calculateTotalPrice(Map<String, Integer> products, List<Product> availableProducts) {
        double totalPrice = 0.0;

        for (Map.Entry<String, Integer> entry : products.entrySet()) {
            String productId = entry.getKey();
            int quantity = entry.getValue();

            // Find the corresponding available product by productId
            Product product = findProductByProductId(availableProducts, productId);

            if (product != null) {
                totalPrice += product.getUnitPrice() * quantity;
            }
        }

        return totalPrice;
    }

    // Helper method to find a product by its productId in the availableProducts list
    private Product findProductByProductId(List<Product> availableProducts, String productId) {
        for (Product product : availableProducts) {
            if (product.getProductId().equals(productId)) {
                return product;
            }
        }
        return null; // Return null if the product is not found
    }

    public String sendMessage(CheckoutEventDTO requestDTO) {
        String message;
        try {
            message = KafkaMessageFormatter.formatCheckoutRequest(requestDTO);
        } catch (JsonProcessingException e) {
            String errorMessage = "Failed to format the message due to: " + e.getMessage();
            System.out.println(errorMessage);
            return errorMessage;
        }
        AtomicReference<String> responseMessage = new AtomicReference<>("Message Sent");
        CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(KafkaTopics.CHECKOUT_TOPIC.getTopicName(), message);
        future.whenComplete((result, ex) -> {
            if (ex == null) {
                responseMessage.set("Sent message=[" + message +
                        "] with offset=[" + result.getRecordMetadata().offset() + "]");
                System.out.println(responseMessage);
            } else {
                responseMessage.set("Unable to send message=[" +
                        message + "] due to : " + ex.getMessage());
                System.out.println(responseMessage);
            }
        });

        return responseMessage.get(); // Message sent successfully
    }

}
