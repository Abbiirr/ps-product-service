package com.example.product_service.helper;

import com.example.product_service.dto.ProductsDTO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class MessageToDTOConverter {


    public static ProductsDTO convertToProductsDTO(String message) {
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            // Parse the JSON message into a MyEventData object
            ProductsDTO productsDTO = objectMapper.readValue(MessageToDTOConverter.getField(message, "products"), ProductsDTO.class);

            return productsDTO;
        } catch (Exception e) {
            // Handle parsing or other exceptions
            return null; // Or throw an exception or return a default value as needed
        }
    }

    public static String getUserId(String message) {
        return "User Id";
    }

    public static String getOrderId(String message) {
        return "Order Id";
    }

    public static String addPriceToMessage(String message, Double price) {
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            // Parse the JSON message
            JsonNode jsonNode = objectMapper.readTree(message);

            JsonNode dataNode = objectMapper.valueToTree(price);

            // Create a new field "status" with the value "pending"
            ((ObjectNode) jsonNode).put("totalPrice", dataNode);

            // Serialize the updated JSON back to a string
            String updatedMessage = objectMapper.writeValueAsString(jsonNode);

            return updatedMessage;
        } catch (Exception e) {
            // Handle parsing or other exceptions
            return null; // Or throw an exception or return a default value as needed
        }
    }

    public static String getField(String message, String fieldName) {
        ObjectMapper objectMapper = new ObjectMapper();
        message.replace("\\", "");
        try {
            // Parse the JSON message
            JsonNode jsonNode = objectMapper.readTree(message);

            // Extract the "eventId" field
            String eventId = jsonNode.get(fieldName).asText();

            return eventId;
        } catch (Exception e) {
            // Handle parsing or other exceptions
            return null; // Or throw an exception or return a default value as needed
        }
    }


    public static HashMap<String, Integer> getProductsMapFromMessage(String message, String fieldName) {
        if (message == null || fieldName == null) {
            return null; // Handle null parameters
        }

        ObjectMapper objectMapper = new ObjectMapper();
        HashMap<String, Integer> fieldMap = new HashMap<>();

        try {
            // Parse the JSON message
            JsonNode jsonNode = objectMapper.readTree(message);

            // Check if the field exists in the JSON
            JsonNode fieldNode = jsonNode.get(fieldName);

            if (fieldNode != null && fieldNode.isObject()) {
                // Iterate through the fields within the object
                fieldNode.fields().forEachRemaining(entry -> {
                    String key = entry.getKey();
                    if (entry.getValue().isInt()) {
                        fieldMap.put(key, entry.getValue().asInt());
                    }
                });
            }

            return fieldMap;
        } catch (Exception e) {
            // Handle parsing or other exceptions
            e.printStackTrace(); // Print the exception for debugging
            return null; // Or throw an exception or return a default value as needed
        }
    }

    public static String setField(String message, String fieldName, Object value) {
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            // Parse the JSON message
            JsonNode jsonNode = objectMapper.readTree(message);

            JsonNode dataNode = objectMapper.valueToTree(value);

            // Create a new field "status" with the value "pending"
            ((ObjectNode) jsonNode).put(fieldName, dataNode);

            // Serialize the updated JSON back to a string
            String updatedMessage = objectMapper.writeValueAsString(jsonNode);

            return updatedMessage;
        } catch (Exception e) {
            // Handle parsing or other exceptions
            return null; // Or throw an exception or return a default value as needed
        }
    }
}

