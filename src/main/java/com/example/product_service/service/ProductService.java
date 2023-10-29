package com.example.product_service.service;

import com.example.product_service.dto.ProductRequestDTO;
import com.example.product_service.dto.ProductResponseDTO;
import com.example.product_service.entity.Product;
import com.example.product_service.entity.Student;
import com.example.product_service.enums.ProductStatus;
import com.example.product_service.repository.ProductRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class ProductService {

    private Map<String, Integer> productMap;
    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
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
        Product product = productRepository.findById(productId).orElse(new Product(productId, 0));
        product.setQuantity(product.getQuantity() + requestDTO.getQuantity());
        productRepository.save(product);
    }

}
