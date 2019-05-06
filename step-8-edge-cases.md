# Step 8: Edge cases

Redis has it's own limits. Are there any limits of Hash's increment? Let's figure out!

## `RatingsRepositoryTest`

We're going to create an isolated test for the Redis-based repository and verify our edge cases.

```java
package com.example.demo.repository;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class RatingsRepositoryTest {

    final String talkId = "testcontainers";

    RatingsRepository repository;

    @Test
    public void testEmptyIfNoKey() {
        assertThat(repository.findAll(talkId)).isEmpty();
    }

    @Test
    public void testLimits() {
        repository.redisTemplate.opsForHash()
                .put(repository.toKey(talkId), "5", Long.MAX_VALUE + "");

        repository.add(talkId, 5);
    }
}
```

But since we're not using Spring Context here, we need to create an instance of our repository ourselves:

```java
    @Before
    public void setUp() {
        LettuceConnectionFactory connectionFactory = new LettuceConnectionFactory(
                ?,
                ?
        );
        connectionFactory.afterPropertiesSet();
        repository = new RatingsRepository(new StringRedisTemplate(connectionFactory));
    }
```

The only missing part is `LettuceConnectionFactory`'s arguments, Redis' host and port.

We will use a JUnit Rule for starting Redis:

```java
    @Rule
    public GenericContainer redis = new GenericContainer("redis:3-alpine")
            .withExposedPorts(6379);
```

And set it:

```java
LettuceConnectionFactory connectionFactory = new LettuceConnectionFactory(
        redis.getContainerIpAddress(),
        redis.getFirstMappedPort()
);
```

If we run the tests, we discover that there is a limit of 64bit number for the increment, oh well!

