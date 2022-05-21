package com.example.demo.api;

import com.example.demo.model.Rating;
import com.example.demo.repository.RatingsRepository;
import com.example.demo.repository.TalksRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/ratings")
public class RatingsController {

    private final KafkaTemplate<String, Rating> kafkaTemplate;

    private final RatingsRepository ratingsRepository;

    private final TalksRepository talksRepository;

    public RatingsController(KafkaTemplate<String, Rating> kafkaTemplate, RatingsRepository ratingsRepository, TalksRepository talksRepository) {
        this.kafkaTemplate = kafkaTemplate;
        this.ratingsRepository = ratingsRepository;
        this.talksRepository = talksRepository;
    }

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
