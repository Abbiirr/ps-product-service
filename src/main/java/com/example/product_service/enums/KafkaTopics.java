package com.example.product_service.enums;


public enum KafkaTopics {
    CHECKOUT_TOPIC("checkout_topic");

    private final String topicName;

    KafkaTopics(String topicName) {
        this.topicName = topicName;
    }

    public String getTopicName() {
        return topicName;
    }
}
