package com.example.product_service.helper;

import com.example.product_service.entity.ProductEvent;
import com.example.product_service.enums.EventStatus;
import com.example.product_service.repository.ProductEventRepository;
import org.springframework.stereotype.Component;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Component
public class EventFinder {
    private final ProductEventRepository eventRepository;


    public EventFinder(ProductEventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    public boolean findDuplicateOrNot(String eventId, String processor) {
        ProductEvent event = eventRepository.findById(createHash(eventId, processor)).orElse(null);
        if (event != null) {
            return true;
        }
        createEvent(eventId, processor);
        return false;

    }


    public ProductEvent findEvent(String eventId, String processor) {
        ProductEvent event = eventRepository.findById(eventId).orElse(createEvent(eventId, processor));
        event.setEventStep(event.getEventStep() + 1);
        eventRepository.save(event);
        return event;
    }


    public String findAndUpdateEvent(String eventId, int maxSteps, String processor) {
        ProductEvent event = this.findEvent(eventId, processor);
        if (event.getEventStep() > maxSteps) {
            event.setEventStatus(EventStatus.COMPLETED);
            eventRepository.save(event);
            return "COMPLETED";
        }
        return event.getEventStatus().getStatus();
    }

    public ProductEvent createEvent(String eventId, String processor) {
        ProductEvent event = ProductEvent.builder()
                .productEventId(createHash(eventId, processor))
                .eventId(eventId)
                .eventStatus(EventStatus.PENDING)
                .eventStep(0)
                .build();
        eventRepository.save(event);
        return event;
    }

    public static String createHash(String eventId, String processor) {
        try {
            // Concatenate the eventId and controller as a string
            String input = eventId.toString() + processor;

            // Create a MessageDigest instance with the desired algorithm (e.g., SHA-256)
            MessageDigest md = MessageDigest.getInstance("SHA-256");

            // Compute the hash
            byte[] hashBytes = md.digest(input.getBytes());

            // Convert the hash bytes to a hexadecimal string
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                hexString.append(String.format("%02x", b));
            }

            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            // Handle the exception
            e.printStackTrace();
            return null;
        }
    }
}
