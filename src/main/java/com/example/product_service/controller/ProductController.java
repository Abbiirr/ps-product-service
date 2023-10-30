package com.example.product_service.controller;

import com.example.product_service.dto.CheckoutRequestDTO;
import com.example.product_service.dto.ProductRequestDTO;
import com.example.product_service.dto.ProductResponseDTO;
import com.example.product_service.entity.Product;
import com.example.product_service.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("product")
public class ProductController {

    @Autowired
    private ProductService service;

    @GetMapping("")
    public List<Product> getAllProducts() {
        return this.service.getAllProducts();
    }

    @PostMapping("/deduct")
    public ProductResponseDTO deduct(@RequestBody final ProductRequestDTO requestDTO){
        return this.service.deductProduct(requestDTO);
    }

    @PostMapping("/add")
    public void add(@RequestBody final ProductRequestDTO requestDTO){
        this.service.addProduct(requestDTO);
    }

    @PostMapping("/checkout")
    public String checkout(@RequestBody final CheckoutRequestDTO requestDTO){
        return this.service.checkout(requestDTO);
    }
}
