package com.example.demo.repository;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.stream.Collectors;

@Repository
public class RatingsRepository {

    final StringRedisTemplate redisTemplate;

    public RatingsRepository(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public Map<Integer, Integer> findAll(String talkId) {
        return redisTemplate.opsForHash()
                .entries(toKey(talkId))
                .entrySet()
                .stream()
                .collect(Collectors.toMap(
                        it -> Integer.valueOf((String) it.getKey()),
                        it -> Integer.valueOf((String) it.getValue())
                ));
    }

    public void add(String talkId, int value) {

        try {
            redisTemplate.opsForHash()
                    .increment(toKey(talkId), value + "", 1);
        } catch (Exception e) {
            throw new MaxRatingsAddedException();
        }

    }

    protected String toKey(String talkId) {
        return "ratings/" + talkId;
    }
}
