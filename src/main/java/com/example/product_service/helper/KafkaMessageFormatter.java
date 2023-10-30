package com.example.product_service.helper;

import com.example.product_service.dto.CheckoutEventDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class KafkaMessageFormatter {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static String formatCheckoutRequest(CheckoutEventDTO requestDTO) throws JsonProcessingException {
        return objectMapper.writeValueAsString(requestDTO);
    }
}

