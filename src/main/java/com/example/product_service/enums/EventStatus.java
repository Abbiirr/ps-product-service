package com.example.product_service.enums;

public enum EventStatus {
    CREATED("CREATED"),
    PENDING("PENDING"),
    COMPLETED("COMPLETED"),
    FAILED("FAILED");

    private final String status;

    EventStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}
