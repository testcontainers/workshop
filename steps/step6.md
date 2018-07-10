# Adding Redis

If you check Testcontainers' modules, there is no Redis module (at least not yet ;)):
https://github.com/testcontainers/testcontainers-java/tree/1.7.3/modules

But it doesn't mean you can't use the library if something is not wrapped with Testcontainers because you can do it yourself thanks to `GenericContainer`:  
https://www.testcontainers.org/usage/generic_containers.html#benefits


But Redis is not JDBC based and we can't just add a property. Yet, the integration is trivial.

## Rules? No thanks!
Testcontainers comes with first class support for JUnit, but in our app we want to have a single Redis instance shared between **all** tests. Luckily, there are `.start()`/`.stop()` methods of `GenericContainer` to start or stop it manually.

Just add a static block to your `AbstractIntegrationTest` with the following code:
```java
    static {
        GenericContainer redis = new GenericContainer("redis:3-alpine")
                .withExposedPorts(6379);
        redis.start();

        System.setProperty("spring.redis.host", redis.getContainerIpAddress());
        System.setProperty("spring.redis.port", redis.getFirstMappedPort() + "");
    }
```

Simple, huh?

Run the tests, now they should all pass.
