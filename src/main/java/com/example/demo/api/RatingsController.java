package com.example.demo.api;

import com.example.demo.model.Rating;
import com.example.demo.repository.RatingsRepository;
import com.example.demo.repository.TalksRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/ratings")
public class RatingsController {

    @Autowired
    KafkaTemplate<String, Rating> kafkaTemplate;

    @Autowired
    RatingsRepository ratingsRepository;

    @Autowired
    TalksRepository talksRepository;

    @PostMapping
    public ResponseEntity<Object> recordRating(@RequestBody Rating rating) throws Exception {
        if (!talksRepository.exists(rating.getTalkId())) {
            return ResponseEntity.notFound().build();
        }

        kafkaTemplate.send("ratings", rating).get();
        return ResponseEntity.accepted().build();
    }

    @GetMapping
    public Map<Integer, Integer> getRatings(@RequestParam String talkId) {
        return ratingsRepository.findAll(talkId);
    }
}
