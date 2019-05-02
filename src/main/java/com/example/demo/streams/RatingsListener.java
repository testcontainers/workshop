package com.example.demo.streams;

import com.example.demo.model.Rating;
import com.example.demo.repository.RatingsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
public class RatingsListener {

    @Autowired
    RatingsRepository ratingsRepository;

    @KafkaListener(groupId = "ratings", topics = "ratings")
    public void handle(@Payload Rating rating) throws Exception {
        System.out.println("Received rating: " + rating);

        ratingsRepository.add(rating.getTalkId(), rating.getValue());
    }
}
