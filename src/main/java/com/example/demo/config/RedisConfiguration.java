package com.example.demo.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializationContext;

@Configuration
public class RedisConfiguration {

    @Autowired
    ReactiveRedisConnectionFactory connectionFactory;

    @Bean
    ReactiveRedisTemplate<String, String> reactiveRedisTemplate() {
        return new ReactiveRedisTemplate<>(
                connectionFactory,
                RedisSerializationContext.string()
        );
    }

}
