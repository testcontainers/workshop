package com.example.demo.repository;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
public class RatingsRepositoryTest {

    final String talkId = "testcontainers";

    RatingsRepository repository;

    @Container
    public GenericContainer redis = new GenericContainer("redis:3-alpine")
            .withExposedPorts(6379);

    @BeforeEach
    public void setUp() {
        LettuceConnectionFactory connectionFactory = new LettuceConnectionFactory(
                redis.getHost(),
                redis.getFirstMappedPort()
        );
        connectionFactory.afterPropertiesSet();
        repository = new RatingsRepository(new StringRedisTemplate(connectionFactory));
    }

    @Test
    public void testEmptyIfNoKey() {
        assertThat(repository.findAll(talkId)).isEmpty();
    }

    @Test
    public void testLimits() {
        repository.redisTemplate.opsForHash()
                .put(repository.toKey(talkId), "5", Long.MAX_VALUE + "");

        Assertions.assertThrows(MaxRatingsAddedException.class, () ->  repository.add(talkId, 5));
    }
}