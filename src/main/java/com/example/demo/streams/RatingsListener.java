package com.example.demo.streams;

import com.example.demo.model.Rating;
import com.example.demo.repository.RatingsRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
public class RatingsListener {

    private final RatingsRepository ratingsRepository;

    public RatingsListener(RatingsRepository ratingsRepository) {
        this.ratingsRepository = ratingsRepository;
    }

    @KafkaListener(groupId = "ratings", topics = "ratings")
    public void handle(@Payload Rating rating) {
        System.out.println("Received rating: " + rating);

        ratingsRepository.add(rating.getTalkId(), rating.getValue());
    }
}
