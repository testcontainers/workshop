# Step 8: Using Testcontainers without frameworks' support

Redis has its own limits. 
Are there any limits of Hash's increment? 
Let's figure out!

## `RatingsRepositoryTest`

We're going to create an isolated test for the Redis-based repository and verify our edge cases.

```java
package com.example.demo.repository;

import org.junit.jupiter.api.Test;

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
    @BeforeEach
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

We will use Testcontainers' JUnit Jupiter extension for starting Redis:

```java
    @Container
    public GenericContainer redis = new GenericContainer("redis:3-alpine")
            .withExposedPorts(6379);
```

And add the `@Testcontainers` annotation to the class: 
```java
@Testcontainers
public class RatingsRepositoryTest {
```
And the code for initializing the connection factory:
```java
LettuceConnectionFactory connectionFactory = new LettuceConnectionFactory(
        redis.getHost(),
        redis.getFirstMappedPort()
);
```

The test should fail with a somewhat cryptic error:
```text
Error in execution; nested exception is io.lettuce.core.RedisCommandExecutionException: ERR increment or decrement would overflow
```
And there's nothing on your side to fix, the test is pushing the boundaries of Redis.  
But on the bright side we learned how to use Testcontainers outside of the Spring Framework. 
We also saw how we can utilize to learn about the limitations and behavior of extra components.

Delete the test before anyone notices. 
Just kidding, let's turn this into a useful test by asserting we throw a custom exception `MaxRatingsAddedException`,
which indicates that our repository recorded the maximum amount of ratings. 
In the future we can still make a different decision of how our business logic should deal with this (and if this is even an edge-case worth solving),
but with this test, we consciously documented our knowledge of the limitations of the systems we integrate against.
```java
@Test
public void testLimits() {
    repository.redisTemplate.opsForHash()
        .put(repository.toKey(talkId), "5", Long.MAX_VALUE + "");

        Assertions.assertThrows(MaxRatingsAddedException.class, () ->  repository.add(talkId, 5));
}
```

The final exercise is now to adapt the implementation of `RatingsRepository.add()` accordingly, to make the test pass.


