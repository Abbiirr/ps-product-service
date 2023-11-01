package com.example.product_service.enums;


public enum KafkaTopics {
    CHECKOUT_TOPIC("checkout_topic"),
    DEDUCT_PRODUCTS("deduct_products"),
    POST_DEDUCT_PRODUCTS("post_deduct_products"),;

    private final String topicName;

    KafkaTopics(String topicName) {
        this.topicName = topicName;
    }

    public String getTopicName() {
        return topicName;
    }
}
