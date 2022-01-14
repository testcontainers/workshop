# Step 6: Adding Redis

The simplest way to provide a Redis instance for your tests is to use `GenericContainer` with a Redis Docker image: [https://www.testcontainers.org/usage/generic\_containers.html](https://www.testcontainers.org/usage/generic_containers.html)
The integration between the tests code and Testcontainers is straightforward.  

## Rules? No thanks!

Testcontainers comes with first class support for JUnit, but in our app we want to have a single Redis instance shared between **all** tests. 
Luckily, there are the `.start()`/`.stop()` methods of `GenericContainer` to start or stop it manually.

Just add the following code to your `AbstractIntegrationTest` with the following code:

```java
static final GenericContainer redis = new GenericContainer("redis:6-alpine")
                                            .withExposedPorts(6379);

@DynamicPropertySource
public static void configureRedis(DynamicPropertyRegistry registry) {
  redis.start();
  registry.add("spring.redis.host", redis::getHost);
  registry.add("spring.redis.port", redis::getFirstMappedPort);
}
```

Simple and beautiful, huh?

Run the tests, now they should all pass.

