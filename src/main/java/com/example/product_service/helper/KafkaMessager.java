package com.example.product_service.helper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

@Component
public class KafkaMessager {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private  final KafkaTemplate<String, String> kafkaTemplate;

    public KafkaMessager(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public static String formatCheckoutRequest(Object requestDTO) throws JsonProcessingException {
        return objectMapper.writeValueAsString(requestDTO);
    }

    public  String sendMessage(String topic,Object requestDTO) {
        String message;
        try {
            message = KafkaMessager.formatCheckoutRequest(requestDTO);
        } catch (JsonProcessingException e) {
            String errorMessage = "Failed to format the message due to: " + e.getMessage();
            System.out.println(errorMessage);
            return errorMessage;
        }
        AtomicReference<String> responseMessage = new AtomicReference<>("Message Sent");
        CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(topic, message);
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

