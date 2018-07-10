package com.example.demo.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.Map;

@Repository
public class RatingsRepository {

    @Autowired
    ReactiveRedisOperations<String, String> redisOperations;

    public Mono<Map<Integer, Integer>> findAll(String talkId) {
        return redisOperations.opsForHash()
                .entries(toKey(talkId))
                .collectMap(
                        it -> Integer.parseInt((String) it.getKey()),
                        it -> Integer.parseInt((String) it.getValue())
                );
    }

    public Mono<Void> add(String talkId, int value) {
        return redisOperations.opsForHash()
                .increment(toKey(talkId), value + "", 1)
                .then();
    }

    protected String toKey(String talkId) {
        return "ratings/" + talkId;
    }
}
